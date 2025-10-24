package com.test.foodivery.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.test.foodivery.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        if (!isNetworkAvailable()) {
            showNetworkAlert();
            return;
        }

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LogInActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNetworkAlert() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Internet is required to proceed. Please turn on Wi-Fi or Mobile Data.")
                .setCancelable(false)
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                })
                .show();
    }



//    private void showNetworkAlert() {
//        new AlertDialog.Builder(this)
//                .setTitle("No Internet Connection")
//                .setMessage("Internet is required to proceed. Please turn on Wi-Fi or Mobile Data.")
//                .setCancelable(false)
//                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
//                    }
//                })
//                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
//                    }
//                })
//                .show();
//    }




}
