package com.example.joggingclubmanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.joggingclubmanagement.databinding.FragmentDashboardInfoBinding;

public class DashboardInfoFragment extends Fragment {

    public DashboardInfoFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        FragmentDashboardInfoBinding binding =
                DataBindingUtil.inflate(
                        inflater,
                        R.layout.fragment_dashboard_info,
                        container,
                        false
                );

        DashboardInfo info = new DashboardInfo(
                "Welcome to Jogging Club Management",
                "View upcoming sessions, manage registrations and track member attendance."
        );

        binding.setDashboardInfo(info);

        return binding.getRoot();
    }
}