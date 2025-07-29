package com.ranoshisdas.app.tvtgallery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ranoshisdas.app.tvtgallery.utils.ImageStorageHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SecreteMain extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RvAdaptor adapter;
    private ArrayList<Uri> imageUriList = new ArrayList<>();
    private ImageButton btnAddPhotos;
    private TextView notFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secrete_main);

        recyclerView = findViewById(R.id.recyclerView);
        btnAddPhotos = findViewById(R.id.btnAddPhotos);
        notFound=findViewById(R.id.not_found);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Load stored images when opening the activity
        loadImages();

        // Button to select images and encrypt them
        btnAddPhotos.setOnClickListener(v -> selectImages());
    }

    // Step 2: Select multiple images from gallery
    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "video/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        galleryLauncher.launch(intent);
    }


    // Step 3: Handle selected images
    private final androidx.activity.result.ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    List<Uri> selectedImages = new ArrayList<>();
                    if (result.getData().getClipData() != null) { // Multiple images selected
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            selectedImages.add(imageUri);
                        }
                    } else { // Single image selected
                        Uri imageUri = result.getData().getData();
                        selectedImages.add(imageUri);
                    }

                    encryptAndStoreImages(selectedImages);
                }
            });

    // Step 4: Encrypt and store images
    private void encryptAndStoreImages(List<Uri> imageUris) {
        for (Uri imageUri : imageUris) {
            try {
                // Get actual file path from URI
                File originalFile = new File(getRealPathFromURI(imageUri));

                // Encrypt and store the file
                ImageStorageHelper.saveEncryptedImage(this, originalFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(this, "Images Encrypted & Stored", Toast.LENGTH_SHORT).show();
        loadImages(); // Refresh RecyclerView
    }

    private String getRealPathFromURI(Uri contentUri) {
        String result = null;
        String orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

        Cursor cursor = getContentResolver().query(contentUri, null, null, null, orderBy);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                if (columnIndex != -1) {
                    result = cursor.getString(columnIndex);
                }
            }
            cursor.close();
        }
        return result;
    }


    // Step 5: Load encrypted images and display in RecyclerView
    private void loadImages() {
        imageUriList.clear();
        File storageDir = ImageStorageHelper.getStorageDirectory();
        File[] encryptedFiles = storageDir.listFiles();

        if (encryptedFiles != null && encryptedFiles.length > 0) {
            for (File encryptedFile : encryptedFiles) {
                if (encryptedFile.getName().endsWith(".enc")) {
                    try {
                        String originalName = encryptedFile.getName().replace(".enc", "");
                        File decryptedFile = ImageStorageHelper.getDecryptedImage(this, originalName);

                        if (decryptedFile != null) {
                            Uri imageUri = Uri.fromFile(decryptedFile);
                            imageUriList.add(imageUri);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            recyclerView.setVisibility(View.VISIBLE);
            notFound.setVisibility(View.GONE);

            adapter = new RvAdaptor(imageUriList,this);
            recyclerView.setAdapter(adapter);
        }
        else {
            recyclerView.setVisibility(View.GONE);
            notFound.setVisibility(View.VISIBLE);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
