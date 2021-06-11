package com.sucker777.clipboardsync;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.sucker777.clipboardsync.ui.home.HomeFragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.PeerConnectionFactory;

public class SideMenu extends AppCompatActivity implements WebRtcClient.WebRtcListener {

    private WebRtcClient rtcClient;
    private SQLQuery sql;

    private View headerView;
    private ClipboardManager mClip;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_side_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton sync = findViewById(R.id.sync);
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence data = "";
                ClipData cData = mClip.getPrimaryClip();
                if(cData != null) {
                    ClipData.Item item = mClip.getPrimaryClip().getItemAt(0);
                    data = item.getText();
                    Toast.makeText(headerView.getContext().getApplicationContext(), "已向其他裝置發送同步要求", Toast.LENGTH_SHORT).show();
                    JSONObject waitforSent = new JSONObject();
                    try {
                        waitforSent.put("timestamp", System.currentTimeMillis());
                        waitforSent.put("payload", data.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    rtcClient.sendDataMessageToAllPeer(waitforSent.toString());
                }else {
                    Toast.makeText(headerView.getContext().getApplicationContext(), "剪貼簿中沒有可以同步的資料", Toast.LENGTH_SHORT).show();
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        mClip = (ClipboardManager) getSystemService(headerView.getContext().CLIPBOARD_SERVICE);
        TextView nav_uuid = (TextView) headerView.findViewById(R.id.nav_uuid);
        SharedPreferences localSettings = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean show_uuid_in_header = localSettings.getBoolean("privacy_show_uuid_in_header", false);
        if(show_uuid_in_header) {
            SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
            String uuid = pref.getString("uuid", "");
            nav_uuid.setText(uuid);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_setting, R.id.nav_uuid)
            .setOpenableLayout(drawer)
            .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        /* Start SQLite Implementation */
        sql = new SQLQuery(headerView.getContext());
        /* End SQLite Implementation */

        /* Start WebRTC Implmentation */
        PeerConnectionFactory.initialize(PeerConnectionFactory
                .InitializationOptions
                .builder(this)
                .createInitializationOptions()
        );
        rtcClient = new WebRtcClient(headerView.getContext());
        rtcClient.setWebRtcListener(this);
        rtcClient.sendInitMessage();
        /* End WebRTC Implmentation */
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcClient.release();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /* Start WebRTC Implmentation */
    @Override
    public void onReceiveDataChannelMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(headerView.getContext(), message, Toast.LENGTH_SHORT).show();

                JSONObject receive = new JSONObject();
                try {
                    receive = new JSONObject(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ContentValues values = new ContentValues();
                String payload = "";
                try {
                    values.put("timestamp", receive.getString("timestamp"));
                    payload = receive.getString("payload");
                    values.put("data", payload);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sql.insert(values);

                NavHostFragment  navHostFragment = (NavHostFragment) getSupportFragmentManager().getFragments().get(0);
                HomeFragment home = (HomeFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                home.updateHistory();

                if(!payload.equals("")) {
                    ClipData clip = ClipData.newPlainText("message", payload);
                    mClip.setPrimaryClip(clip);
                }
            }
        });
    }
    /* End WebRTC Implmentation */
}