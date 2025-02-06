package com.graff.tester;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private ImageView shirt, pants;
    private int currentShirtIndex = 0;
    private int currentPantsIndex = 0;
    private int[] shirts = {R.drawable.shirt1, R.drawable.shirt2, R.drawable.sample_shirt};
    private int[] pantsArray = {R.drawable.pants, R.drawable.pants2, R.drawable.pants3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Toast.makeText(this, "Git", Toast.LENGTH_SHORT).show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shirt = findViewById(R.id.shirt);
        pants = findViewById(R.id.imageViewPants);

        findViewById(R.id.shirtArrowLeft).setOnClickListener(v -> changeShirt(-1));
        findViewById(R.id.shirtArrowRight).setOnClickListener(v -> changeShirt(1));
        findViewById(R.id.pantsArrowLeft).setOnClickListener(v -> changePants(-1));
        findViewById(R.id.pantsArrowRight).setOnClickListener(v -> changePants(1));
        findViewById(R.id.random).setOnClickListener(v -> randomOutfit());
    }
    private void changeShirt(int direction) {
        currentShirtIndex = (currentShirtIndex + direction + shirts.length) % shirts.length;
        shirt.setImageResource(shirts[currentShirtIndex]);
    }

    private void changePants(int direction) {
        currentPantsIndex = (currentPantsIndex + direction + pantsArray.length) % pantsArray.length;
        pants.setImageResource(pantsArray[currentPantsIndex]);
    }

    private void randomOutfit() {
        Random random = new Random();
        currentShirtIndex = random.nextInt(shirts.length);
        currentPantsIndex = random.nextInt(pantsArray.length);
        shirt.setImageResource(shirts[currentShirtIndex]);
        pants.setImageResource(pantsArray[currentPantsIndex]);
    }
}