package com.sucker777.clipboardsync.ui.home;

import androidx.lifecycle.ViewModelProvider;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sucker777.clipboardsync.R;
import com.sucker777.clipboardsync.SQLQuery;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private View root;

    private SQLQuery sql;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        updateHistory();

        /*final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        return root;
    }

    public void updateHistory() {
        sql = new SQLQuery(root.getContext());
        Cursor cursor = sql.getHistory();

        LinearLayout scrollView_Layout = (LinearLayout) root.findViewById(R.id.scrollView_Layout);
        scrollView_Layout.removeAllViews();

        while(cursor.moveToNext()) {
            String item = cursor.getString(cursor.getColumnIndex("data"));
            TextView valueTV = new TextView(root.getContext());
            valueTV.setText(item);
            valueTV.setId(Integer.parseInt("5"));
            valueTV.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            scrollView_Layout.addView(valueTV);
        }
        cursor.close();
    }
}