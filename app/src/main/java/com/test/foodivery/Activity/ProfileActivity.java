package com.test.foodivery.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.test.foodivery.R;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private Button btnLogout;
    private LinearLayout layoutAccount,layoutHistory,layoutAddress;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Debug: Check if shop data is received
        ArrayList<String> receivedShopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
        ArrayList<String> receivedShopDetailsList = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
        Log.d("ProfileActivity", "Received SHOP_ID_LIST: " + (receivedShopIdList != null ? receivedShopIdList.size() : "null"));
        Log.d("ProfileActivity", "Received SHOP_DETAILS_LIST: " + (receivedShopDetailsList != null ? receivedShopDetailsList.size() : "null"));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

// Always set the correct selected item for this activity
        ImageView backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> finish());
        bottomNavigationView.setSelectedItemId(R.id.ic_profile);
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_UNLABELED);
        layoutAccount=findViewById(R.id.layoutAccount);
        layoutHistory=findViewById(R.id.layoutHistory);
        layoutAddress=findViewById(R.id.layoutAddress);
        layoutHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HistoryViewActivity.class);
                // Pass shop data to HistoryViewActivity
                ArrayList<String> shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
                ArrayList<String> shopDetailsList = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
                if (shopIdList != null && shopDetailsList != null) {
                    intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);
                    intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsList);
                }
                startActivity(intent);
                finish();
            }
        });
        layoutAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AddressFindActivity.class);
                startActivity(intent);
                finish();
            }
        });

        layoutAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileUserDetails.class);
                startActivity(intent);
                finish();
            }
        });
        btnLogout=findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, LogOutActivity.class);
                startActivity(intent);
                finish();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.ic_home) {
                    showProgressBar();
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.ic_history) {
                    // Navigate to history page
                    showProgressBar();
                    Intent intent = new Intent(ProfileActivity.this, HistoryViewActivity.class);
                    // Pass shop data to HistoryViewActivity
                    ArrayList<String> shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
                    ArrayList<String> shopDetailsList = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
                    if (shopIdList != null && shopDetailsList != null) {
                        intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);
                        intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsList);
                    }
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.ic_cart) {
                    showProgressBar();
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.putExtra("OPEN_CART", true);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.ic_productView) {
                    // Navigate to offers page with shop data
                    showProgressBar();
                    Intent intent = new Intent(ProfileActivity.this, OfferActivity.class);
                    intent.putExtra("name", "all");
                    
                    // Get shop data from intent extras
                    ArrayList<String> shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
                    ArrayList<String> shopDetailsList = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
                    
                    if (shopIdList != null && shopDetailsList != null) {
                        intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);
                        intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsList);
                        startActivity(intent);
                        finish();
                    } else {
                        // Fallback: navigate to MainActivity if no shop data
                        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                        finish();
                    }
                    return true;
                } else if (id == R.id.ic_profile) {
                    // Already on profile page
                    return true;
                }
                return false;
            }
        });


// Prevent action on reselection of the current item
        bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                // Do nothing
            }
        });

    }

    private void showProgressBar() {
        FrameLayout progressOverlay = findViewById(R.id.progressOverlay);
        if (progressOverlay != null) {
            progressOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        FrameLayout progressOverlay = findViewById(R.id.progressOverlay);
        if (progressOverlay != null) {
            progressOverlay.setVisibility(View.GONE);
        }
    }
}