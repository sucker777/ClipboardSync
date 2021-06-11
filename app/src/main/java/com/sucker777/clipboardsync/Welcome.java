package com.sucker777.clipboardsync;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Welcome extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findViewById(R.id.old_user).setOnClickListener(this);
        findViewById(R.id.new_user).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.old_user:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(false)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                        .setPrompt("請將二維碼對準框線")
                        .setCameraId(0)
                        .setBeepEnabled(false)
                        .setCaptureActivity(ScanActivity.class)
                        .initiateScan();
                break;
            case R.id.new_user:
                UUID uuid = UUID.randomUUID();
                SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
                pref.edit().putString("uuid", uuid.toString().toUpperCase()).commit();
                Intent index = new Intent(this, SideMenu.class);
                startActivity(index);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result.getRawBytes() != null) {
            Log.v("WelcomeTEST", "QRCODE_SUCCESS");
            String CODE_RESULT = result.getContents();
            Pattern QRCODE_Pattern = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
            Matcher QRCODE_Matcher = QRCODE_Pattern.matcher(CODE_RESULT);
            if (QRCODE_Matcher.matches()) {
                Toast.makeText(this, "匯入成功", Toast.LENGTH_SHORT).show();
                SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
                pref.edit().putString("uuid", CODE_RESULT.toUpperCase()).commit();
                Intent index = new Intent(this, SideMenu.class);
                startActivity(index);
            } else {
                Toast.makeText(this, "匯入失敗，請確認掃描的QRCODE是否由Clipboard Sync產生", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.v("WelcomeTEST", "QRCODE_Failed");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}