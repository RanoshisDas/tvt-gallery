package com.ranoshisdas.app.tvtgallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.ranoshisdas.app.tvtgallery.utils.ImageStorageHelper;

import java.io.File;
import java.util.ArrayList;

public class SecretImagePreviewActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ImagePagerAdapter adapter;
    private ArrayList<Uri> imageUris;
    private int startIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_secret_image_preview);

        viewPager = findViewById(R.id.viewPager);
        ImageButton btnBack = findViewById(R.id.back);
        ImageButton btnDelete = findViewById(R.id.delete);
        ImageButton btnRestore = findViewById(R.id.restore);

        imageUris = getIntent().getParcelableArrayListExtra("imageUris");
        startIndex = getIntent().getIntExtra("startIndex", 0);

        adapter = new ImagePagerAdapter(this, imageUris);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startIndex, true);

        btnBack.setOnClickListener(v ->{
            startActivity(new Intent(this, SecreteMain.class));
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            int position = viewPager.getCurrentItem();
            if (position < 0 || position >= imageUris.size()) {
                Toast.makeText(this, "Invalid image position", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri imageUri = imageUris.get(position);
            String imageName = new File(imageUri.getPath()).getName().replace(".enc", "");

            // Delete from storage
            if (ImageStorageHelper.deleteEncryptedImage(imageName)) {
                Toast.makeText(this, "Image Deleted", Toast.LENGTH_SHORT).show();
                imageUris.remove(position);
                adapter.notifyItemRemoved(position);

                // If no more images, close the activity
                if (imageUris.isEmpty()) {
                    startActivity(new Intent(this, SecreteMain.class));
                    finish();
                }
            } else {
                Toast.makeText(this, "Failed to Delete Image", Toast.LENGTH_SHORT).show();
            }
        });

        btnRestore.setOnClickListener(v -> {
            int position = viewPager.getCurrentItem();
            if (position < 0 || position >= imageUris.size()) {
                Toast.makeText(this, "Invalid image position", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri imageUri = imageUris.get(position);
            String imageName = new File(imageUri.getPath()).getName().replace(".enc", "");

            // Delete from storage
            if (ImageStorageHelper.restoreImage(getApplicationContext(),imageName)) {
                Toast.makeText(this, "Image Restored!", Toast.LENGTH_SHORT).show();
                imageUris.remove(position);
                adapter.notifyItemRemoved(position);

                // If no more images, close the activity
                if (imageUris.isEmpty()) {
                    startActivity(new Intent(this, SecreteMain.class));
                    finish();
                }
            } else {
                Toast.makeText(this, "Failed to Restore the Image", Toast.LENGTH_SHORT).show();
            }
        });

    }
}