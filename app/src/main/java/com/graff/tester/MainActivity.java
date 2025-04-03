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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Random;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Handler;



public class MainActivity extends AppCompatActivity {
    private ImageView shirt, pants;
    private int currentShirtIndex = 0;
    private int currentPantsIndex = 0;
    private int[] shirts = {R.drawable.shirt1, R.drawable.shirt2, R.drawable.sample_shirt,R.drawable.shirt6,R.drawable.shirt3,R.drawable.shirt4,R.drawable.shirt5};

    private int[] pantsArray = {R.drawable.pants, R.drawable.pants2,R.drawable.pants4,R.drawable.pants5};
    private ImageButton thecollection;
    private Button addclothes;
    private String itemType;
    private ImageButton addshirt;
    private ImageButton addpants;




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

       // addclothes=findViewById(R.id.addcloths);
      //  addclothes.setOnClickListener(new View.OnClickListener() {
           // @Override
          //  public void onClick(View view) {
              //  MainActivity.this.itemType = "Shirt";
              //  Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
              //  addimageActivityResultLauncher.launch(intent);
           // }
      //  });
        addshirt=findViewById(R.id.addShirt);
        addshirt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.itemType = "Shirt";
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                addimageActivityResultLauncher.launch(intent);
            }
        });
        addpants=findViewById(R.id.addPants);
        addpants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.itemType = "Pants";
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
                                    .uploadImageToFirebase(MainActivity.this, selectedImage, MainActivity.this.itemType);
                        }

                    }
                }
            });

    private void changeShirt(int direction) {
        currentShirtIndex = (currentShirtIndex + direction + shirts.length) % shirts.length;
        shirt.setImageResource(shirts[currentShirtIndex]);
        Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
        shirt.startAnimation(fadeIn);
    }

    private void changePants(int direction) {

        currentPantsIndex = (currentPantsIndex + direction + pantsArray.length) % pantsArray.length;
        pants.setImageResource(pantsArray[currentPantsIndex]);
        Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
        pants.startAnimation(fadeIn);
    }

    private void randomOutfit() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        shirt.startAnimation(fadeOut);
        pants.startAnimation(fadeOut);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                currentShirtIndex = random.nextInt(shirts.length);
                currentPantsIndex = random.nextInt(pantsArray.length);
                shirt.setImageResource(shirts[currentShirtIndex]);
                pants.setImageResource(pantsArray[currentPantsIndex]);

                Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                shirt.startAnimation(fadeIn);
                pants.startAnimation(fadeIn);
            }
        }, 500); //
    }
}