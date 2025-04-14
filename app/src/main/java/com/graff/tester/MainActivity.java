package com.graff.tester;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.graff.tester.models.ClothingItem;
import com.graff.tester.models.ClothingItemRepository;
import com.graff.tester.models.ClothingType;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private ImageView shirtView, pantsView;
    private int currentShirtIndex = 0;
    private int currentPantsIndex = 0;
    private ClothingType clothingType;
    private FirebaseManager firebaseManager;
    private FirebaseUser currentUser;

    private List<ClothingItem> getShirtRepository() {
        return ClothingItemRepository.getInstance().getShirtItems();
    }

    private List<ClothingItem> getPantsRepository() {
        return ClothingItemRepository.getInstance().getPantsItems();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        validateCurrentUser();

        setContentView(R.layout.activity_main);
        shirtView = findViewById(R.id.imageViewShirt);
        pantsView = findViewById(R.id.imageViewPants);

        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, view);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.main_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_gallery) {
                    startActivity(new Intent(MainActivity.this, GalleryActivity.class));
                    return true;
                } else if (itemId == R.id.menu_about) {
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    return true;
                } else if (itemId == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    ClothingItemRepository.getInstance().clearShirtItems();
                    ClothingItemRepository.getInstance().clearPantsItems();

                    //getShirtRepository().removeAll(getShirtRepository());
                    //getPantsRepository().removeAll(getPantsRepository());
                    startActivity(new Intent(MainActivity.this, Opening.class));
                    finish();
                    return true;
                }

                return false;
            });

            // הצגת התפריט
            popup.show();
        });



        ImageButton add_shirt =findViewById(R.id.addShirt);
        add_shirt.setOnClickListener(view -> {
            MainActivity.this.clothingType = ClothingType.SHIRT;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            addimageActivityResultLauncher.launch(intent);
        });
        ImageButton add_pants =findViewById(R.id.addPants);
        add_pants.setOnClickListener(view -> {
            MainActivity.this.clothingType = ClothingType.PANTS;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            addimageActivityResultLauncher.launch(intent);
        });


        findViewById(R.id.shirtArrowLeft).setOnClickListener(v -> changeShirt(-1));
        findViewById(R.id.shirtArrowRight).setOnClickListener(v -> changeShirt(1));
        findViewById(R.id.pantsArrowLeft).setOnClickListener(v -> changePants(-1));
        findViewById(R.id.pantsArrowRight).setOnClickListener(v -> changePants(1));
        findViewById(R.id.random).setOnClickListener(v -> randomOutfit());

        firebaseManager = new FirebaseManager();
        firebaseManager.downloadClothingImages(
                this::handleImageLoaded, this::onHandleItemsDownloadCompleted
        );
    }

    private void validateCurrentUser() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this,Opening.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        validateCurrentUser();
        // In case items were deleted
        if (currentShirtIndex >= getShirtRepository().size()) {
             currentShirtIndex = 0;
        }
        if (currentPantsIndex >= getPantsRepository().size()) {
            currentPantsIndex = 0;
        }
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
        currentShirtIndex = (currentShirtIndex + direction + getShirtRepository().size()) % getShirtRepository().size();
        Glide.with(this).load(getShirtRepository().get(currentShirtIndex).getImageUrl()).into(shirtView);
    }

    private void changePants(int direction) {
        currentPantsIndex = (currentPantsIndex + direction + getPantsRepository().size()) % getPantsRepository().size();
        Glide.with(this).load(getPantsRepository().get(currentPantsIndex).getImageUrl()).into(pantsView);
    }

    private void randomOutfit() {
        Random random = new Random();
        currentShirtIndex = random.nextInt(getShirtRepository().size());
        currentPantsIndex = random.nextInt(getPantsRepository().size());
        Glide.with(this).load(getShirtRepository().get(currentShirtIndex).getImageUrl()).into(shirtView);
        Glide.with(this).load(getPantsRepository().get(currentPantsIndex).getImageUrl()).into(pantsView);
    }

    private void handleImageLoaded(ClothingItem item) {
        runOnUiThread(() -> {
            if (item.clothingType == ClothingType.SHIRT) {
                if (getShirtRepository().isEmpty())
                    Glide.with(this).load(item.getImageUrl()).into(shirtView);
                ClothingItemRepository.getInstance().addShirtItem(item);
            } else if (item.clothingType == ClothingType.PANTS) {
                if (getPantsRepository().isEmpty())
                    Glide.with(this).load(item.getImageUrl()).into(pantsView);
                ClothingItemRepository.getInstance().addPantsItem(item);
            }
        });
    }

    private void onHandleItemsDownloadCompleted() {
        if (this.getShirtRepository().isEmpty()) {
            ClothingUtils.uploadClothingDrawableToFirebase(MainActivity.this,
                    R.drawable.shirt2,
                    ClothingType.SHIRT,
                    this::handleImageUploaded);
        }
        if (this.getPantsRepository().isEmpty()) {
            ClothingUtils.uploadClothingDrawableToFirebase(MainActivity.this,
                    R.drawable.pants2,
                    ClothingType.PANTS,
                    this::handleImageUploaded);
        }
    }

    private void handleImageUploaded(ClothingItem item) {
        runOnUiThread(() -> {
            if (item.getClothingType() == ClothingType.SHIRT) {
                Glide.with(this).load(item.getImageUrl()).into(shirtView);
                ClothingItemRepository.getInstance().addShirtItem(item);
            } else if (item.getClothingType() == ClothingType.PANTS) {
                Glide.with(this).load(item.getImageUrl()).into(pantsView);
                ClothingItemRepository.getInstance().addPantsItem(item);
            }
        });
    }
}