package com.ranoshisdas.app.tvtgallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ranoshisdas.app.tvtgallery.utils.ImageStorageHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class RvAdapterNormal extends RecyclerView.Adapter<RvAdapterNormal.vholder> {
    private final ArrayList<Uri> data;
    private final Context context;

    public RvAdapterNormal(ArrayList<Uri> data, Context context) {
        this.data = new ArrayList<>(data);
        this.context = context;
    }

    @NonNull
    @Override
    public RvAdapterNormal.vholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_view, parent, false);
        return new vholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull vholder holder, int position) {
        Uri imageUri = data.get(position);

        // Load Image with Picasso
        Picasso.get()
                .load(imageUri)
                .resize(400, 400)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.iv);

        holder.iv.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImagePreviewActivity.class);
            intent.putParcelableArrayListExtra("imageUris", data);
            intent.putExtra("startIndex", position);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class vholder extends RecyclerView.ViewHolder {
        ImageView iv;

        public vholder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv);
        }
    }

}
