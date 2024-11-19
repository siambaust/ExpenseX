package com.example.expensex;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Initialize the VideoView
        VideoView videoView = findViewById(R.id.splash_video_view);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash1);

        videoView.setVideoURI(videoUri);
        videoView.start(); // Play the video

        // Set listener to transition to MainActivity after 3 seconds
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close SplashActivity
        }, 3000); // 3 seconds delay
    }
}
