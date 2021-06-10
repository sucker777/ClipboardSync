package com.sucker777.clipboardsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import io.socket.emitter.Emitter;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class WebRtcClient {

    private final static String TAG = "WebRtcClient";
    private final static String mSocketAddress = "https://clipboard.bernkastel.tech";

    private PeerConnectionFactory factory;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private Socket client;
    private String mClientId;
    private Map<String, Peer> peers = new HashMap<>();
    private MediaConstraints constraints = new MediaConstraints();
    private WebRtcListener webRtcListener;

    private Context mContext;

    private Emitter.Listener messageListener = args -> {
        JSONObject data = (JSONObject) args[0];
        Log.v(TAG, "messageListener call data : " + data);
        try {
            String from = data.getString("from");
            String type = data.getString("type");
            JSONObject payload = null;
            if (!type.equals("init")) {
                payload = data.getJSONObject("payload");
            }
            switch (type) {
                case "init":
                    onReceiveInit(from);
                    break;
                case "offer":
                    onReceiveOffer(from, payload);
                    break;
                case "answer":
                    onReceiveAnswer(from, payload);
                    break;
                case "candidate":
                    onReceiveCandidate(from, payload);
                    break;
                default:
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    private Emitter.Listener clientIdListener = args -> {
        mClientId = (String) args[0];
        Log.v(TAG, "clientIdListener call data : " + mClientId);
    };

    public WebRtcClient(Context context) {
        factory = new PeerConnectionFactory(new PeerConnectionFactory.Options());

        mContext = context;

        SharedPreferences pref = mContext.getSharedPreferences("data", mContext.MODE_PRIVATE);
        String uuid = pref.getString("uuid", "").toUpperCase();

        try {
            IO.Options options = IO.Options.builder()
                    .setTransports(new String[] { WebSocket.NAME })
                    .setReconnection(false)
                    .setUpgrade(false)
                    .setQuery("uuid="+uuid)
                    .build();
            client = IO.socket(mSocketAddress, options);
        } catch (URISyntaxException e) {
            Log.v(TAG, "Signaling Server's URI Syntax Error" + mSocketAddress);
            e.printStackTrace();
        }

        client.on("message", messageListener);
        client.on("id", clientIdListener);

        client.connect();

        PeerConnection.IceServer.Builder iceServerBuilder = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302");
        iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK);
        PeerConnection.IceServer iceServer = iceServerBuilder.createIceServer();
        iceServers.add(iceServer);

        PeerConnection.IceServer.Builder iceServerBuilder2 = PeerConnection.IceServer.builder("turn:numb.viagenie.ca").setUsername("partment@live.com").setPassword("50711no1");
        iceServerBuilder2.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK);
        PeerConnection.IceServer iceServer2 = iceServerBuilder2.createIceServer();
        iceServers.add(iceServer2);
    }

    public void setWebRtcListener(WebRtcListener webRtcListener) {
        this.webRtcListener = webRtcListener;
    }

    /**
     * Sending Init to Signaling Server
     * */
    public void sendInitMessage() {
        client.emit("init");
    }

    /**
     * Sending Message to Signaling Server
     *
     * @param to      id of recipient
     * @param type    type of message
     * @param payload payload of message
     * @throws JSONException
     * */
    public void sendMessage(String to, String type, JSONObject payload) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("to", to);
        message.put("type", type);
        message.put("payload", payload);
        message.put("from", mClientId);
        client.emit("message", message);
    }

    /**
     * Sending Message to All Peers
     *
     * @param message
     */
    public void sendDataMessageToAllPeer(String message) {
        for (Peer peer : peers.values()) {
            peer.sendDataChannelMessage(message);
        }
    }

    private Peer getPeer(String from) {
        Peer peer;
        if (!peers.containsKey(from)) {
            peer = addPeer(from);
        } else {
            peer = peers.get(from);
        }
        return peer;
    }

    private Peer addPeer(String id) {
        Peer peer = new Peer(id);
        peers.put(id, peer);
        return peer;
    }

    private void removePeer(String id) {
        Peer peer = peers.get(id);
        peer.release();
        peers.remove(peer.id);
    }

    public void onReceiveInit(String fromUid) {
        Log.v(TAG, "onReceiveInit fromUid:" + fromUid);
        Peer peer = getPeer(fromUid);
        peer.pc.createOffer(peer, constraints);
    }

    public void onReceiveOffer(String fromUid, JSONObject payload) {
        Log.v(TAG, "onReceiveOffer uid:" + fromUid + " data:" + payload);
        try {
            Peer peer = getPeer(fromUid);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
            peer.pc.createAnswer(peer, constraints);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onReceiveAnswer(String fromUid, JSONObject payload) {
        Log.v(TAG, "onReceiveAnswer uid:" + fromUid + " data:" + payload);
        try {
            Peer peer = getPeer(fromUid);
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.pc.setRemoteDescription(peer, sdp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onReceiveCandidate(String fromUid, JSONObject payload) {
        Log.v(TAG, "onReceiveCandidate uid:" + fromUid + " data:" + payload);
        try {
            Peer peer = getPeer(fromUid);
            if (peer.pc.getRemoteDescription() != null) {
                IceCandidate candidate = new IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                );
                peer.pc.addIceCandidate(candidate);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        for (Peer peer : peers.values()) {
            peer.release();
        }
        factory.dispose();
        client.disconnect();
        client.close();
    }

    public class Peer implements SdpObserver, PeerConnection.Observer, DataChannel.Observer {
        PeerConnection pc;
        String id;
        DataChannel dc;

        public Peer(String id) {
            Log.v(TAG, "new Peer: " + id);
            this.pc = factory.createPeerConnection(
                    iceServers, //ICE Server list
                    constraints, //MediaConstraints
                    this); //Context
            this.id = id;

            /*
            DataChannel.Init Specifiable Arguments：
            ordered：Guarantee Transmit in Order or not；
            maxRetransmitTimeMs：Retry Timeout；
            maxRetransmits：Max Retries；
             */
            DataChannel.Init init = new DataChannel.Init();
            init.ordered = true;
            dc = pc.createDataChannel("dataChannel", init);
        }

        public void sendDataChannelMessage(String message) {
            byte[] msg = message.getBytes();
            DataChannel.Buffer buffer = new DataChannel.Buffer(
                    ByteBuffer.wrap(msg),
                    false);
            dc.send(buffer);
        }

        public void release() {
            pc.dispose();
            dc.close();
            dc.dispose();
        }

        //SdpObserver-------------------------------------------------------------------------------

        @Override
        public void onCreateSuccess(SessionDescription sdp) {
            Log.v(TAG, "onCreateSuccess: " + sdp.description);
            try {
                JSONObject payload = new JSONObject();
                payload.put("type", sdp.type.canonicalForm());
                payload.put("sdp", sdp.description);
                sendMessage(id, sdp.type.canonicalForm(), payload);
                pc.setLocalDescription(Peer.this, sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSetSuccess() {

        }

        @Override
        public void onCreateFailure(String s) {

        }

        @Override
        public void onSetFailure(String s) {

        }

        //DataChannel.Observer----------------------------------------------------------------------

        @Override
        public void onBufferedAmountChange(long l) {

        }

        @Override
        public void onStateChange() {
            Log.v(TAG, "onDataChannel onStateChange:" + dc.state());
        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {
            Log.v(TAG, "onDataChannel onMessage : " + buffer);
            ByteBuffer data = buffer.data;
            byte[] bytes = new byte[data.capacity()];
            data.get(bytes);
            String msg = new String(bytes);
            if (webRtcListener != null) {
                webRtcListener.onReceiveDataChannelMessage(msg);
            }
        }

        //PeerConnection.Observer-------------------------------------------------------------------

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.v(TAG, "onIceConnectionChange : " + iceConnectionState.name());
            if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED || iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
                removePeer(id);
            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {

        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

        }

        @Override
        public void onIceCandidate(IceCandidate candidate) {
            try {
                JSONObject payload = new JSONObject();
                payload.put("label", candidate.sdpMLineIndex);
                payload.put("id", candidate.sdpMid);
                payload.put("candidate", candidate.sdp);
                sendMessage(id, "candidate", payload);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

        }

        @Override
        public void onAddStream(MediaStream mediaStream) {

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.v(TAG, "onDataChannel label:" + dataChannel.label());
            dataChannel.registerObserver(this);
        }

        @Override
        public void onRenegotiationNeeded() {

        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

        }
    }

    public interface WebRtcListener {
        void onReceiveDataChannelMessage(String message);
    }
}