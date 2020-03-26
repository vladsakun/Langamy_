package com.langamy.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.langamy.livedata.ConnectionLiveData;

public class MainViewModel extends AndroidViewModel {

    private ConnectionLiveData mConnectionLiveData;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mConnectionLiveData = new ConnectionLiveData(application.getApplicationContext());
    }

    public ConnectionLiveData getConnectionLiveData() {
        return mConnectionLiveData;
    }
}
