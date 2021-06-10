package com.sucker777.clipboardsync.ui.uuid;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import com.sucker777.clipboardsync.R;

public class UUIDFragment extends Fragment {
    private UUIDViewModel uuidViewModel;
    private View root;
    private Context mContext;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        uuidViewModel = new ViewModelProvider(this).get(UUIDViewModel.class);
        root = inflater.inflate(R.layout.fragment_uuid, container, false);
        mContext = root.getContext();

        final TextView uuid_text = root.findViewById(R.id.uuid_text);
        final ImageView uuid_QRCODE = root.findViewById(R.id.uuid_QRCODE);

        final SharedPreferences pref = mContext.getSharedPreferences("data", mContext.MODE_PRIVATE);
        String uuid = pref.getString("uuid", "").toUpperCase();
        uuid_text.setText(uuid);

        BarcodeEncoder encoder = new BarcodeEncoder();
        try {
            Bitmap bit = encoder.encodeBitmap(uuid, BarcodeFormat.QR_CODE, 3840, 3840);
            uuid_QRCODE.setImageBitmap(bit);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return root;
    }
}