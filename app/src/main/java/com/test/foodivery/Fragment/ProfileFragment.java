package com.test.foodivery.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.test.foodivery.Activity.AddressFindActivity;
import com.test.foodivery.Activity.HistoryViewActivity;
import com.test.foodivery.Activity.LogOutActivity;
import com.test.foodivery.Activity.ProfileUserDetails;
import com.test.foodivery.R;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        LinearLayout layoutAccount = view.findViewById(R.id.layoutAccount);
        LinearLayout layoutHistory = view.findViewById(R.id.layoutHistory);
        LinearLayout layoutAddress = view.findViewById(R.id.layoutAddress);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        layoutAccount.setOnClickListener(v -> startActivity(new Intent(getContext(), ProfileUserDetails.class)));
        layoutHistory.setOnClickListener(v -> startActivity(new Intent(getContext(), HistoryViewActivity.class)));
        layoutAddress.setOnClickListener(v -> startActivity(new Intent(getContext(), AddressFindActivity.class)));
        btnLogout.setOnClickListener(v -> startActivity(new Intent(getContext(), LogOutActivity.class)));

        return view;
    }
}


