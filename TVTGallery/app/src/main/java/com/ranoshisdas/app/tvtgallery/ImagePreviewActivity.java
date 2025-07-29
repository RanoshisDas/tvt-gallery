package com.ranoshisdas.app.tvtgallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class ImagePreviewActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImagePagerAdapter adapter;
    private ArrayList<Uri> imageUris;
    private int startIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set system UI flags for immersive full-screen experience
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_image_preview);

        viewPager = findViewById(R.id.viewPager);
        ImageButton btnShare = findViewById(R.id.btnShare);
        ImageButton btnBack = findViewById(R.id.back);
        ImageButton btnDelete = findViewById(R.id.delete);

        // Retrieve image URIs and starting index from the intent
        imageUris = getIntent().getParcelableArrayListExtra("imageUris");
        startIndex = getIntent().getIntExtra("startIndex", 0);

        // Initialize and set up the ImagePagerAdapter for the ViewPager2
        adapter = new ImagePagerAdapter(this, imageUris);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startIndex, true); // Set the initial image to display

        // Set OnClickListener for the Share button
        btnShare.setOnClickListener(v -> shareImage(adapter.getImageUriAt(viewPager.getCurrentItem())));

        // Set OnClickListener for the Back button
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(ImagePreviewActivity.this, MainActivity.class));
            finish(); // Close the current activity and return to MainActivity
        });

        // Set OnClickListener for the Delete button
        btnDelete.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();
            Uri uriToDelete = imageUris.get(currentPosition);

            // Show an AlertDialog to confirm image deletion
            new AlertDialog.Builder(ImagePreviewActivity.this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Remove the image URI from the list
                            imageUris.remove(currentPosition);
                            // Notify the adapter that an item has been removed
                            adapter.notifyItemRemoved(currentPosition);

                            // Optionally delete the image from storage using ContentResolver
                            getContentResolver().delete(uriToDelete, null, null);
                            Toast.makeText(ImagePreviewActivity.this, "Image deleted.", Toast.LENGTH_SHORT).show();


                            // If no images are left, navigate back to MainActivity
                            if (imageUris.isEmpty()) {
                                startActivity(new Intent(ImagePreviewActivity.this, MainActivity.class));
                                finish(); // Close if no images left
                            } else if (currentPosition == imageUris.size()) {
                                // If the last image was deleted, show the new last image
                                viewPager.setCurrentItem(imageUris.size() - 1, false);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null) // Do nothing if Cancel is clicked
                    .show();
        });
    }

    private void shareImage(Uri uri) {
        try {
            File file = copyToCache(uri, "shared_image.jpg");
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share image via"));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to share image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private File copyToCache(Uri uri, String filename) throws Exception {
        File cacheFile = new File(getCacheDir(), filename);
        InputStream input = getContentResolver().openInputStream(uri);
        FileOutputStream output = new FileOutputStream(cacheFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        input.close();
        output.close();
        return cacheFile;
    }
}
