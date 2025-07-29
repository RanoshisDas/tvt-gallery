package com.ranoshisdas.app.tvtgallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
        viewPager.setCurrentItem(startIndex, true); // Set the initial image to display


        btnBack.setOnClickListener(v -> {
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

            // Attempt to delete the encrypted image from storage
            if (ImageStorageHelper.deleteEncryptedImage(imageName)) {
                Toast.makeText(this, "Image Deleted", Toast.LENGTH_SHORT).show();
                imageUris.remove(position);
                adapter.notifyItemRemoved(position);

                // If no more images, close the activity and go back to SecreteMain
                if (imageUris.isEmpty()) {
                    startActivity(new Intent(this, SecreteMain.class));
                    finish();
                } else if (position == imageUris.size()) {
                    // If the last image was deleted, show the new last image
                    viewPager.setCurrentItem(imageUris.size() - 1, false);
                }
            } else {
                Toast.makeText(this, "Failed to Delete Image", Toast.LENGTH_SHORT).show();
            }
        });

        // Set OnClickListener for the Restore button
        btnRestore.setOnClickListener(v -> {
            int position = viewPager.getCurrentItem();
            if (position < 0 || position >= imageUris.size()) {
                Toast.makeText(this, "Invalid image position", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri imageUri = imageUris.get(position);
            // Extract the original image name from the URI path by removing the ".enc" extension
            String imageName = new File(imageUri.getPath()).getName().replace(".enc", "");

            // Attempt to restore the encrypted image
            if (ImageStorageHelper.restoreImage(getApplicationContext(), imageName)) {
                Toast.makeText(this, "Image Restored!", Toast.LENGTH_SHORT).show();
                // Remove the image URI from the list and notify the adapter
                imageUris.remove(position);
                adapter.notifyItemRemoved(position);

                // If no more images, close the activity and go back to SecreteMain
                if (imageUris.isEmpty()) {
                    startActivity(new Intent(this, SecreteMain.class));
                    finish();
                } else if (position == imageUris.size()) {
                    // If the last image was restored, show the new last image
                    viewPager.setCurrentItem(imageUris.size() - 1, false);
                }
            } else {
                Toast.makeText(this, "Failed to Restore the Image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, SecreteMain.class));
        finish();
    }
}