package com.ranoshisdas.app.tvtgallery.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ranoshisdas.app.tvtgallery.R;
import com.ranoshisdas.app.tvtgallery.VideoPlayerActivity;

import java.util.ArrayList;

public class VideoFragment extends Fragment {

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private final ArrayList<VideoModel> videoList = new ArrayList<>();

    public VideoFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        recyclerView = view.findViewById(R.id.video_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        videoAdapter = new VideoAdapter(videoList);
        recyclerView.setAdapter(videoAdapter);

        loadVideos();
        return view;
    }

    private void loadVideos() {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION
        };

        try (Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null) {
                int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);

                while (cursor.moveToNext()) {
                    String path = cursor.getString(dataIndex);
                    String title = cursor.getString(nameIndex);
                    long duration = cursor.getLong(durationIndex);
                    videoList.add(new VideoModel(title, path, duration));
                }
                videoAdapter.notifyDataSetChanged();
            }
        }
    }

    private class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {

        private final ArrayList<VideoModel> videos;

        VideoAdapter(ArrayList<VideoModel> videos) {
            this.videos = videos;
        }

        @NonNull
        @Override
        public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_video, parent, false);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
            holder.bind(videos.get(position));
        }

        @Override
        public int getItemCount() {
            return videos.size();
        }
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder {

        private final ImageView thumbnail;
        private final TextView title;
        private final TextView duration;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.video_thumbnail);
            title = itemView.findViewById(R.id.video_title);
            duration = itemView.findViewById(R.id.video_duration);
        }

        public void bind(VideoModel video) {
            title.setText(video.getTitle());
            duration.setText(formatDuration(video.getDuration()));

            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(
                    video.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
            thumbnail.setImageBitmap(thumb);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
                intent.putExtra("video_path", video.getPath());
                startActivity(intent);
            });
        }
    }

    private static class VideoModel {
        private final String title;
        private final String path;
        private final long duration;

        public VideoModel(String title, String path, long duration) {
            this.title = title;
            this.path = path;
            this.duration = duration;
        }

        public String getTitle() { return title; }
        public String getPath() { return path; }
        public long getDuration() { return duration; }
    }

    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
