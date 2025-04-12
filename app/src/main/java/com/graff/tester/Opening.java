package com.graff.tester;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class Opening extends AppCompatActivity {
    private ImageView shirtImage, pantsImage;
    private int[] shirtImages = {
            R.drawable.shirt_bg,
            R.drawable.shirt_bg2,
            R.drawable.shirt_bg4,
            R.drawable.shirt_bg5,
            R.drawable.shirt_bg6
    };

    private int[] pantsImages = {
            R.drawable.pants_bg2,
            R.drawable.pants_bg3,
            R.drawable.pants_bg4,
            R.drawable.pants_bg5
    };


    private int shirtIndex = 0;
    private int pantsIndex = 0;

    private Handler handler = new Handler();
    private final long delay = 800; // זמן בין תחלופה במילישניות

    private Runnable changeImagesRunnable = new Runnable() {
        @Override
        public void run() {
            shirtImage.setImageResource(shirtImages[shirtIndex]);
            pantsImage.setImageResource(pantsImages[pantsIndex]);

            shirtIndex = (shirtIndex + 1) % shirtImages.length;
            pantsIndex = (pantsIndex + 1) % pantsImages.length;

            handler.postDelayed(this, delay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        shirtImage = findViewById(R.id.shirtImage);
        pantsImage = findViewById(R.id.pantsImage);

        handler.post(changeImagesRunnable);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(Opening.this, LoginActivity.class);
            startActivity(intent);
        });
        registerButton.setOnClickListener(view -> {
            Intent intent = new Intent(Opening.this, SignupActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(changeImagesRunnable);
    }
}