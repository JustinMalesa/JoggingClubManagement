package com.example.joggingclubmanagement;

import android.util.Log;

import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class DashboardLifecycleObserver
        implements DefaultLifecycleObserver {

    private static final String TAG = "DashboardLifecycle";

    @Override
    public void onCreate(LifecycleOwner owner) {
        Log.d(TAG, "Dashboard created");
    }

    @Override
    public void onStart(LifecycleOwner owner) {
        Log.d(TAG, "Dashboard started");
    }

    @Override
    public void onResume(LifecycleOwner owner) {
        Log.d(TAG, "Dashboard resumed");
    }

    @Override
    public void onPause(LifecycleOwner owner) {
        Log.d(TAG, "Dashboard paused");
    }

    @Override
    public void onStop(LifecycleOwner owner) {
        Log.d(TAG, "Dashboard stopped");
    }

    @Override
    public void onDestroy(LifecycleOwner owner) {
        Log.d(TAG, "Dashboard destroyed");
    }
}