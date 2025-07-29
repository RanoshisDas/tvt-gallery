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

public class RvAdaptor extends RecyclerView.Adapter<RvAdaptor.vholder> {
    private final ArrayList<Uri> data;
    private final Context context;

    public RvAdaptor(ArrayList<Uri> data, Context context) {
        this.data = new ArrayList<>(data);
        this.context = context;
    }

    @NonNull
    @Override
    public RvAdaptor.vholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_view, parent, false);
        return new vholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RvAdaptor.vholder holder, int position) {
        Uri imageUri = data.get(position);

        // Load Image with Picasso
        Picasso.get()
                .load(imageUri)
                .resize(400, 400)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.iv);

        // Long Press to Delete Image
        holder.iv.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(imageUri, position);
            return true;
        });

        holder.iv.setOnClickListener(v -> {
            Intent intent = new Intent(context, SecretImagePreviewActivity.class);
            intent.putParcelableArrayListExtra("imageUris", data);
            intent.putExtra("startIndex", position);
            context.startActivity(intent);
            ((SecreteMain) context).finish();
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

    // Show Confirmation Dialog Before Deletion
    private void showDeleteConfirmationDialog(Uri imageUri, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete/Restore Image")
                .setMessage("Are you want to delete this encrypted image? Or restore into gallery?")
                .setNeutralButton("Cancel",((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setPositiveButton("Delete", (dialog, which) -> {
                    String imageName = new File(imageUri.getPath()).getName().replace(".enc", "");

                    // Delete from storage
                    if (ImageStorageHelper.deleteEncryptedImage(imageName)) {
                        Toast.makeText(context, "Image Deleted", Toast.LENGTH_SHORT).show();
                        data.remove(position);
                        notifyItemRemoved(position);
                    } else {
                        Toast.makeText(context, "Failed to Delete Image", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Restore",  (dialog, which) -> {
                    String imageName = new File(imageUri.getPath()).getName().replace(".enc", "");

                    //Decrypt and store in picture
                    if (ImageStorageHelper.restoreImage(context,imageName)) {
                        Toast.makeText(context, "Image Restored", Toast.LENGTH_SHORT).show();
                        data.remove(position);
                        notifyItemRemoved(position);
                    } else {
                        Toast.makeText(context, "Failed to Restored Image", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
