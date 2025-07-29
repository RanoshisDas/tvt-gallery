package com.ranoshisdas.app.tvtgallery;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
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
        // Reset the matrix for the new image to ensure it starts at default scale
        holder.resetZoom();
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

    public static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener,
            ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

        ImageView photoView;
        Matrix matrix = new Matrix(); // Matrix for image transformations
        float minScale = 1f; // Minimum zoom scale
        float maxScale = 5f; // Maximum zoom scale
        float currentScale = 1f; // Current zoom scale

        ScaleGestureDetector scaleGestureDetector;
        GestureDetector gestureDetector;

        // Used for dragging
        PointF last = new PointF();
        PointF start = new PointF();

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
            photoView.setOnTouchListener(this); // Set touch listener on the ImageView
            photoView.setScaleType(ImageView.ScaleType.MATRIX); // Ensure scaleType is MATRIX

            scaleGestureDetector = new ScaleGestureDetector(itemView.getContext(), this);
            gestureDetector = new GestureDetector(itemView.getContext(), this);
        }

        public void resetZoom() {
            matrix.reset(); // Reset the matrix to identity
            currentScale = 1f; // Reset current scale
            photoView.setImageMatrix(matrix); // Apply the reset matrix
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // Pass touch events to both gesture detectors
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);

            // Handle ACTION_UP to ensure proper state reset or final matrix application
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // Optional: Snap to bounds or reset if needed after touch release
                // For now, we allow the image to stay at its last position/scale
            }
            return true; // Indicate that the touch event was consumed
        }

        // --- ScaleGestureDetector.OnScaleGestureListener methods ---

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float newScale = currentScale * scaleFactor;

            // Clamp the scale factor within min and max limits
            if (newScale >= minScale && newScale <= maxScale) {
                currentScale = newScale;
                // Apply scaling around the focal point of the gesture
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                photoView.setImageMatrix(matrix); // Update ImageView with new matrix
            }
            return true; // Event consumed
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true; // Ready to begin scaling
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            // Optional: Adjust scale to snap to min/max if slightly outside bounds
            // This prevents the image from being stuck slightly out of bounds after a gesture.
            if (currentScale < minScale) {
                matrix.postScale(minScale / currentScale, minScale / currentScale, photoView.getWidth() / 2f, photoView.getHeight() / 2f);
                currentScale = minScale;
                photoView.setImageMatrix(matrix);
            } else if (currentScale > maxScale) {
                matrix.postScale(maxScale / currentScale, maxScale / currentScale, photoView.getWidth() / 2f, photoView.getHeight() / 2f);
                currentScale = maxScale;
                photoView.setImageMatrix(matrix);
            }
        }

        // --- GestureDetector.OnGestureListener methods ---

        @Override
        public boolean onDown(MotionEvent e) {
            // Called when a touch event is first detected.
            // Useful for initializing drag operations.
            last.set(e.getX(), e.getY());
            start.set(last);
            return false; // Return false to allow other gesture detectors to process
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // Not used for zoom/drag in this implementation
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Not used for zoom/drag in this implementation
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Only allow dragging if the image is zoomed in (currentScale > minScale)
            if (currentScale > minScale) {
                matrix.postTranslate(-distanceX, -distanceY); // Apply translation
                photoView.setImageMatrix(matrix); // Update ImageView with new matrix
            }
            return true; // Event consumed
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Not used for zoom/drag in this implementation
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Not used for zoom/drag in this implementation
            return false;
        }
    }
}
