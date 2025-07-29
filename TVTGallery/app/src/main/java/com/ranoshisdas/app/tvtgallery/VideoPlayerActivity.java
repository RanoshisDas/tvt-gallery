package com.ranoshisdas.app.tvtgallery;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.video_view);

        String videoPath = getIntent().getStringExtra("video_path");
        if (videoPath != null) {
            Uri videoUri = Uri.parse(videoPath);
            videoView.setVideoURI(videoUri);

            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            videoView.start();
        }
    }
}
