package com.sucker777.clipboardsync.ui.home;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sucker777.clipboardsync.R;
import com.sucker777.clipboardsync.SQLQuery;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private View root;

    RecyclerView mRecyclerView;
    MyListAdapter myListAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    SwipeRefreshLayout.OnRefreshListener swipeRefreshListener;
    ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

    private SQLQuery sql;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        sql = new SQLQuery(root.getContext());

        mRecyclerView = root.findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(root.getContext(), DividerItemDecoration.VERTICAL));
        myListAdapter = new MyListAdapter();
        mRecyclerView.setAdapter(myListAdapter);

        swipeRefreshLayout = root.findViewById(R.id.refreshLayout);
        swipeRefreshListener = () -> {
            arrayList.clear();
            makeData();
            myListAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        };
        swipeRefreshLayout.setOnRefreshListener(swipeRefreshListener);

        updateHistory();
        updatePeersAmount();

        homeViewModel.getPeers_Amount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer s) {
                TextView peers_amount = root.findViewById(R.id.peers_amount);
                if(s > 0) {
                    peers_amount.setText("連線中裝置：" + Integer.toString(s));
                }else {
                    peers_amount.setText(R.string.menu_home_connecting);
                }
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void updatePeersAmount() {
        SharedPreferences pref = root.getContext().getSharedPreferences("data", root.getContext().MODE_PRIVATE);
        Integer amount = Integer.parseInt(pref.getString("peers_amount", "0"));
        homeViewModel.setPeers_Amount(amount);
    }

    public void updateHistory() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                swipeRefreshListener.onRefresh();
            }
        });
    }

    private void makeData() {
        Cursor cursor = sql.getHistory();

        while(cursor.moveToNext()) {
            HashMap<String,String> hashMap = new HashMap<>();
            String timestamp = cursor.getString(cursor.getColumnIndex("timestamp"));
            String data = cursor.getString(cursor.getColumnIndex("data"));
            hashMap.put("timestamp", timestamp);
            hashMap.put("data", data);
            arrayList.add(hashMap);
        }

        cursor.close();
    }

    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView data;
            private Button button;
            private View mView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                data = itemView.findViewById(R.id.history_data);
                button = itemView.findViewById(R.id.history_copy);
                mView = itemView;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.data.setText(arrayList.get(position).get("data"));

            holder.button.setOnClickListener((v)->{
                ClipData clip = ClipData.newPlainText("message", holder.data.getText());
                ClipboardManager mClip = (ClipboardManager) root.getContext().getSystemService(root.getContext().CLIPBOARD_SERVICE);
                mClip.setPrimaryClip(clip);
                Toast.makeText(root.getContext(), "已複製到剪貼簿", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }
    }
}

