package com.graff.tester;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private static final int CAMERA_REQUEST_CODE = 100;
    private Uri cameraImageUri;


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
                    firebaseManager.signOut();
                    FirebaseAuth.getInstance().signOut();
                    ClothingItemRepository.getInstance().clearShirtItems();
                    ClothingItemRepository.getInstance().clearPantsItems();
                    startActivity(new Intent(MainActivity.this, Opening.class));
                    finish();
                    return true;
                }

                return false;
            });

            popup.show();
        });



        ImageButton add_shirt =findViewById(R.id.addShirt);
        add_shirt.setOnClickListener(view -> {
            MainActivity.this.clothingType = ClothingType.SHIRT;
            String[] options = {"צלם תמונה", "בחר מהגלריה"};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("בחר אפשרות");
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkCameraPermissionAndOpen();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    addimageActivityResultLauncher.launch(intent);
                }
            });
            builder.show();
        });

        ImageButton add_pants =findViewById(R.id.addPants);
        add_pants.setOnClickListener(view -> {
            MainActivity.this.clothingType = ClothingType.PANTS;
            String[] options = {"צלם תמונה", "בחר מהגלריה"};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("בחר אפשרות");
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkCameraPermissionAndOpen();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    addimageActivityResultLauncher.launch(intent);
                }
            });
            builder.show();
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        cameraLauncher.launch(cameraIntent);
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    firebaseManager.uploadImageToFirebase(
                            MainActivity.this,
                            cameraImageUri,
                            clothingType,
                            this::handleImageUploaded
                    );
                }
            });

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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "כדי להשתמש במצלמה, צריך לאשר הרשאה", Toast.LENGTH_SHORT).show();
            }
        }
    }


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