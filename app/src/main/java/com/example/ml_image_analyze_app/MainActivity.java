package com.example.ml_image_analyze_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private Fragment fragment1, fragment2;
    private static final int ITEM_1_ID = R.id.item_1;
    private static final int ITEM_2_ID = R.id.item_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment1 = new ScanFragment();
        fragment2 = new HistoryFragment();

        // Set the default fragment
        setFragment(fragment1);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == ITEM_1_ID) {
                    // Respond to navigation item 1 click
                    setFragment(fragment1);
                    return true;
                } else if (item.getItemId() == ITEM_2_ID) {
                    // Respond to navigation item 2 click
                    setFragment(fragment2);
                    return true;
                } else {
                    return false;
                }
            }
        });

        bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

            }

            public void onItemReselected(@NonNull MenuItem item) {
                if (item.getItemId() == ITEM_1_ID) {
                    // Respond to navigation item 1 reselection
                } else if (item.getItemId() == ITEM_2_ID) {
                    // Respond to navigation item 2 reselection
                }
            }
        });
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.scan_frame, fragment)
                .commit();
    }
}
