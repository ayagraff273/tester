package com.graff.tester;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private ImageButton thecollection;
    private Button addclothes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Toast.makeText(this, "Git", Toast.LENGTH_SHORT).show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shirt = findViewById(R.id.shirt);
        pants = findViewById(R.id.imageViewPants);
        thecollection = findViewById(R.id.thecollection);
        thecollection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, gallery.class);
            startActivity(intent);
        });

        addclothes=findViewById(R.id.addcloths);
        addclothes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                addimageActivityResultLauncher.launch(intent);
            }
        });
        findViewById(R.id.shirtArrowLeft).setOnClickListener(v -> changeShirt(-1));
        findViewById(R.id.shirtArrowRight).setOnClickListener(v -> changeShirt(1));
        findViewById(R.id.pantsArrowLeft).setOnClickListener(v -> changePants(-1));
        findViewById(R.id.pantsArrowRight).setOnClickListener(v -> changePants(1));
        findViewById(R.id.random).setOnClickListener(v -> randomOutfit());
    }

    ActivityResultLauncher<Intent> addimageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImage = data.getData();
                            FirebaseManager.getInstance()
                                    .uploadImageToFirebase(MainActivity.this, selectedImage);
                        }

                    }
                }
            });

    private void changeShirt(int direction) {
        currentShirtIndex = (currentShirtIndex + direction + shirts.length) % shirts.length;
        shirt.setImageResource(shirts[currentShirtIndex]);
    }

    private void changePants(int direction) {
        currentPantsIndex = (currentPantsIndex + direction + pantsArray.length) % pantsArray.length;
        pants.setImageResource(pantsArray[currentPantsIndex]);
    }

    private void randomOutfit() {

        // test
        Random random = new Random();
        currentShirtIndex = random.nextInt(shirts.length);
        currentPantsIndex = random.nextInt(pantsArray.length);
        shirt.setImageResource(shirts[currentShirtIndex]);
        pants.setImageResource(pantsArray[currentPantsIndex]);
    }
}