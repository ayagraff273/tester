package com.graff.tester;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.graff.tester.models.ClothingType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private ImageView shirtView, pantsView;
    private List<String> shirtImages = new ArrayList<>();
    private List<String> pantsImages = new ArrayList<>();
    private int currentShirtIndex = 0;
    private int currentPantsIndex = 0;
    private ClothingType clothingType;
    private FirebaseManager firebaseManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shirtView = findViewById(R.id.imageViewShirt);
        pantsView = findViewById(R.id.imageViewPants);

        ImageButton theCollection = findViewById(R.id.thecollection);
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

        ImageButton add_shirt =findViewById(R.id.addShirt);
        add_shirt.setOnClickListener(view -> {
            MainActivity.this.clothingType = ClothingType.SHIRT;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            addimageActivityResultLauncher.launch(intent);
        });
        ImageButton add_pants =findViewById(R.id.addPants);
        add_pants.setOnClickListener(view -> {
            MainActivity.this.clothingType = ClothingType.SHIRT;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            addimageActivityResultLauncher.launch(intent);
        });


        findViewById(R.id.shirtArrowLeft).setOnClickListener(v -> changeShirt(-1));
        findViewById(R.id.shirtArrowRight).setOnClickListener(v -> changeShirt(1));
        findViewById(R.id.pantsArrowLeft).setOnClickListener(v -> changePants(-1));
        findViewById(R.id.pantsArrowRight).setOnClickListener(v -> changePants(1));
        findViewById(R.id.random).setOnClickListener(v -> randomOutfit());

        firebaseManager = new FirebaseManager();
        firebaseManager.loadClothingImages(
                this::handleFirstImageLoaded,
                this::handleAllImagesLoaded
        );
    }

    ActivityResultLauncher<Intent> addimageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        firebaseManager.uploadImageToFirebase(
                                MainActivity.this,
                                selectedImage,
                                clothingType,
                                this::handleImageUploaded
                        );
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

    private void handleFirstImageLoaded(ClothingType type, String imageUrl) {
        runOnUiThread(() -> {
            if (type == ClothingType.SHIRT) {
                Glide.with(this).load(imageUrl).into(shirtView);
            } else if (type == ClothingType.PANTS) {
                Glide.with(this).load(imageUrl).into(pantsView);
            }
        });
    }

    private void handleAllImagesLoaded(List<String> shirtUrls, List<String> pantsUrls) {
        // TODO: enable the gallery button only after all the images have been loaded (?)
        shirtImages = shirtUrls;
        pantsImages = pantsUrls;
    }

    private void handleImageUploaded(ClothingType type, String imageUrl) {
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