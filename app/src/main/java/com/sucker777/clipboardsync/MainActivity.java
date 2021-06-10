package com.sucker777.clipboardsync;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.webrtc.PeerConnectionFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        String uuid = pref.getString("uuid", "");
        if(uuid.equals("")) {
            Intent welcome = new Intent(this, Welcome.class);
            startActivity(welcome);
        }else {
            Intent index = new Intent(this, SideMenu.class);
            startActivity(index);
        }
    }
}

class ClipboardListener implements ClipboardManager.OnPrimaryClipChangedListener {
    private ClipboardManager mClip;
    private View mView;
    public ClipboardListener(ClipboardManager clip, View v) {
        mClip = clip;
        mView = v;
    }
    public void onPrimaryClipChanged() {
        CharSequence data = "";
        ClipData.Item item = mClip.getPrimaryClip().getItemAt(0);
        data = item.getText();
        Toast.makeText(mView.getContext().getApplicationContext(), data, Toast.LENGTH_SHORT).show();
    }
}