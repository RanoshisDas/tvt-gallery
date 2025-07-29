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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_image_preview);

        viewPager = findViewById(R.id.viewPager);
        ImageButton btnShare = findViewById(R.id.btnShare);
        ImageButton btnBack = findViewById(R.id.back);
        ImageButton btnDelete = findViewById(R.id.delete);

        imageUris = getIntent().getParcelableArrayListExtra("imageUris");
        startIndex = getIntent().getIntExtra("startIndex", 0);

        adapter = new ImagePagerAdapter(this, imageUris);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startIndex, true);

        btnShare.setOnClickListener(v -> shareImage(adapter.getImageUriAt(viewPager.getCurrentItem())));

        btnBack.setOnClickListener(v ->{
                startActivity(new Intent(ImagePreviewActivity.this, MainActivity.class));
                finish();
        });

        btnDelete.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();
            Uri uriToDelete = imageUris.get(currentPosition);

            new AlertDialog.Builder(ImagePreviewActivity.this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this image?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            imageUris.remove(currentPosition);
                            adapter.notifyItemRemoved(currentPosition);

                            // Optionally delete from storage
                            getContentResolver().delete(uriToDelete, null, null);

                            if (imageUris.isEmpty()) {
                                startActivity(new Intent(ImagePreviewActivity.this, MainActivity.class));
                                finish(); // close if no images left
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
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
            Toast.makeText(this, "Failed to share image", Toast.LENGTH_SHORT).show();
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
