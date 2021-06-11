package com.sucker777.clipboardsync.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<Integer> Peers_Amount;

    public HomeViewModel() {
        Peers_Amount = new MutableLiveData<>();
        Peers_Amount.setValue(-1);
    }

    public void setPeers_Amount(Integer amount) {
        this.Peers_Amount.setValue(amount);
    }

    public LiveData<Integer> getPeers_Amount() {
        return Peers_Amount;
    }
}