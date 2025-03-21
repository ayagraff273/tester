package com.graff.tester;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class Opening extends AppCompatActivity {
    private Button loginButton;
    private Button registerButton;
    private ImageView shirtImage;
    private ImageView pantsImage;
    private int[] shirtImages = {R.drawable.shirt1, R.drawable.shirt2, R.drawable.shirt3};
    private int[] pantsImages={R.drawable.pants2,R.drawable.pants, R.drawable.pants4};
    private int currentIndex = 0;
    private Handler handler = new Handler();
    private Runnable outfitSwitcher = new Runnable() {
        @Override
        public void run() {
            currentIndex = (currentIndex + 1) % shirtImages.length;

            shirtImage.setImageResource(shirtImages[currentIndex]);
            pantsImage.setImageResource(pantsImages[currentIndex]);

            handler.postDelayed(this, 2000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        shirtImage = findViewById(R.id.shirtImage);
        pantsImage = findViewById(R.id.pantsImage);
        shirtImage.setImageResource(shirtImages[currentIndex]);
        pantsImage.setImageResource(pantsImages[currentIndex]);
        handler.postDelayed(outfitSwitcher, 2000);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Opening.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Opening.this, SignupActivity.class);
                startActivity(intent);
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(outfitSwitcher);
    }
}
