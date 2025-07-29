package com.ranoshisdas.app.tvtgallery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.ranoshisdas.app.tvtgallery.fragment.PhotoFragment;
import com.ranoshisdas.app.tvtgallery.fragment.VideoFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int STORAGE_PERMISSION_CODE = 1001;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView appName, headerTv, navTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        checkStoredPassword();
        setupDrawerWithCustomIcon();
        handleAppNameLongClick();
        requestStoragePermission();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PhotoFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_photos);
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        appName = headerView.findViewById(R.id.app_name);
        headerTv = headerView.findViewById(R.id.header_tv);
        navTv = findViewById(R.id.nav_tv);

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupDrawerWithCustomIcon() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set your custom drawer icon
        toolbar.setNavigationIcon(R.drawable.ic_menu);

        // Toggle drawer manually
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void checkStoredPassword() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String password = prefs.getString("password", null);

        if (password != null && !password.isEmpty()) {
            navTv.setVisibility(View.GONE);
            headerTv.setVisibility(View.GONE);
        }
    }

    private void handleAppNameLongClick() {
        appName.setOnLongClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String password = prefs.getString("password", null);

            Intent intent = (password != null && !password.isEmpty())
                    ? new Intent(MainActivity.this, PasswordActivity.class)
                    : new Intent(MainActivity.this, SetupPassword.class);

            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(intent);
            return true;
        });
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                new AlertDialog.Builder(this)
                        .setTitle("Allow Full Storage Access")
                        .setMessage("This app needs access to all files to work properly. Please allow this in settings.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) ->
                                Toast.makeText(this, "Permission denied! Cannot access files.", Toast.LENGTH_SHORT).show())
                        .show();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage Permission Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_photos) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PhotoFragment())
                    .commit();
        }
        if (itemId == R.id.nav_videos) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new VideoFragment())
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
