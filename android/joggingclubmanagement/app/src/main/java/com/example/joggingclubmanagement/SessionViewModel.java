package com.example.joggingclubmanagement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;

public class SessionViewModel extends ViewModel {

    private final MutableLiveData<JSONArray> sessions =
            new MutableLiveData<>(new JSONArray());

    private final MutableLiveData<String> status =
            new MutableLiveData<>("Ready");

    public LiveData<JSONArray> getSessions() {
        return sessions;
    }

    public LiveData<String> getStatus() {
        return status;
    }

    public void setSessions(JSONArray newSessions) {
        sessions.setValue(newSessions);
    }

    public void setStatus(String newStatus) {
        status.setValue(newStatus);
    }
}