package com.graff.tester;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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


import com.bumptech.glide.Glide;
import com.graff.tester.models.ClothingType;
import java.util.ArrayList;
import java.util.List;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Random;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Handler;


public class MainActivity extends AppCompatActivity implements FirebaseManager.FirebaseCallback {
    private ImageView shirtView, pantsView;
    private List<String> shirtImages = new ArrayList<>();
    private List<String> pantsImages = new ArrayList<>();
    private int currentShirtIndex = 0;
    private int currentPantsIndex = 0;
    private ImageButton theCollection;
    private ImageButton addshirt;
	private ImageButton addpants;
    private ClothingType clothingType;

    private FirebaseManager firebaseManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shirtView = findViewById(R.id.imageViewShirt);
        pantsView = findViewById(R.id.imageViewPants);

        theCollection = findViewById(R.id.thecollection);
        theCollection.setOnClickListener(v -> {
            // Combine both shirt and pants images into one list (if needed)
            List<String> allImages = new ArrayList<>();
            allImages.addAll(shirtImages);
            allImages.addAll(pantsImages);
            // Pass image URLs to GalleryActivity
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            intent.putStringArrayListExtra("imageUrls", new ArrayList<>(allImages));
            startActivity(intent);
        });

        addshirt=findViewById(R.id.addShirt);
        addshirt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.clothingType = ClothingType.SHIRT;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                addimageActivityResultLauncher.launch(intent);
            }
        });
        addpants=findViewById(R.id.addPants);
        addpants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.clothingType = ClothingType.PANTS;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                addimageActivityResultLauncher.launch(intent);
            }
        });


        findViewById(R.id.shirtArrowLeft).setOnClickListener(v -> changeShirt(-1));
        findViewById(R.id.shirtArrowRight).setOnClickListener(v -> changeShirt(1));
        findViewById(R.id.pantsArrowLeft).setOnClickListener(v -> changePants(-1));
        findViewById(R.id.pantsArrowRight).setOnClickListener(v -> changePants(1));
        findViewById(R.id.random).setOnClickListener(v -> randomOutfit());

        firebaseManager = new FirebaseManager(this, this);
        firebaseManager.loadClothingImages();
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
                            firebaseManager.uploadImageToFirebase(MainActivity.this, selectedImage, MainActivity.this.clothingType);

                        }
                    }
                }
            });

    private void changeShirt(int direction) {
        currentShirtIndex = (currentShirtIndex + direction + shirtImages.size()) % shirtImages.size();
        Glide.with(this).load(shirtImages.get(currentShirtIndex)).into(shirtView);
    }

    private void changePants(int direction) {
        currentPantsIndex = (currentPantsIndex + direction + pantsImages.size()) % pantsImages.size();
        Glide.with(this).load(pantsImages.get(currentPantsIndex)).into(pantsView);
    }

    private void randomOutfit() {
        Random random = new Random();
        currentShirtIndex = random.nextInt(shirtImages.size());
        currentPantsIndex = random.nextInt(pantsImages.size());
        Glide.with(this).load(shirtImages.get(currentShirtIndex)).into(shirtView);
        Glide.with(this).load(pantsImages.get(currentPantsIndex)).into(pantsView);
    }

    @Override
    public void onFirstImageLoaded(ClothingType type, String imageUrl) {
        runOnUiThread(() -> {
            if (type == ClothingType.SHIRT) {
                Glide.with(this).load(imageUrl).into(shirtView);
            } else if (type == ClothingType.PANTS) {
                Glide.with(this).load(imageUrl).into(pantsView);
            }
        });
    }

    @Override
    public void onAllImagesLoaded(List<String> shirtUrls, List<String> pantsUrls) {
        // TODO: enable the gallery button only after all the images have been loaded (?)
        shirtImages = shirtUrls;
        pantsImages = pantsUrls;
    }
    @Override
    public void addurltolist(ClothingType type, String imageUrl) {
        runOnUiThread(() -> {
            if (type == ClothingType.SHIRT) {
                Glide.with(this).load(imageUrl).into(shirtView);
                shirtImages.add(imageUrl);


            } else if (type == ClothingType.PANTS) {
                Glide.with(this).load(imageUrl).into(pantsView);
                pantsImages.add(imageUrl);
            }
        });
    }
}