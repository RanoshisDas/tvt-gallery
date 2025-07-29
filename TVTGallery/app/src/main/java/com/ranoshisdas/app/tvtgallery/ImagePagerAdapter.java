package com.ranoshisdas.app.tvtgallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    private final ArrayList<Uri> imageUris;
    private final Context context;

    public ImagePagerAdapter(Context context, ArrayList<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fullscreen_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = imageUris.get(position);
        Glide.with(context)
                .load(uri)
                .into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public Uri getImageUriAt(int position) {
        return imageUris.get(position);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView photoView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
        }
    }
}
