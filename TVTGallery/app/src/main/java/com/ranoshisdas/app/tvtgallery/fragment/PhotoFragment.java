package com.ranoshisdas.app.tvtgallery.fragment;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ranoshisdas.app.tvtgallery.R;
import com.ranoshisdas.app.tvtgallery.RvAdapterNormal;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhotoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PhotoFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static PhotoFragment newInstance(String param1, String param2) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private static final int STORAGE_PERMISSION_CODE = 1001;
    private RecyclerView recyclerView;
    private RvAdapterNormal adapter;
    private ArrayList<Uri> imageUriList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Check permission and load images
        if (hasStoragePermission()) {
            loadImages();
        } else {
            requestStoragePermission();
        }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadImages();
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private void loadImages() {
        imageUriList.clear();
        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID};
        String orderBy = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

        Cursor cursor = requireActivity().getContentResolver().query(contentUri, projection, null, null, orderBy);

        if (cursor != null) {
            Log.d("GalleryDebug", "Cursor count: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri imageUri = ContentUris.withAppendedId(contentUri, id);
                    imageUriList.add(imageUri);
                    Log.d("GalleryDebug", "Image found: " + imageUri.toString());
                } while (cursor.moveToNext());
            } else {
                Log.e("GalleryDebug", "No images found in MediaStore.");
                Toast.makeText(getContext(), "No images found in gallery", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } else {
            Log.e("GalleryDebug", "Cursor is null. Query failed.");
        }

        adapter = new RvAdapterNormal(imageUriList,requireContext());
        recyclerView.setAdapter(adapter);
    }

}