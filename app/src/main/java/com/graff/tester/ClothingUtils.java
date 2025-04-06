package com.graff.tester;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.graff.tester.models.ClothingType;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class ClothingUtils {
    public static Uri getImageUriFromDrawable(Context context, int drawableId) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);

        // Create a unique filename for each image by using the drawable ID
        String fileName = "temp_image_" + drawableId + ".jpg";
        File file = new File(context.getCacheDir(), fileName);  // Use drawableId to ensure uniqueness

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);  // You might want to reduce the quality to 80 or lower
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(file);
    }

    public static void uploadClothingDrawableToFirebase(Context context, int drawableId, ClothingType clothingType) {
        Uri imageUri = getImageUriFromDrawable(context, drawableId); // e.g. R.drawable.pants
        FirebaseManager manager = new FirebaseManager(context, null);
        manager.uploadImageToFirebase(context, imageUri, clothingType);
    }
/*

    public static void uploadAllLocalClothingDrawablesToFirebase(Context context) {
        int[] shirts = {
                R.drawable.shirt2,
                R.drawable.shirt3,
                R.drawable.shirt4,
                R.drawable.shirt5,
                R.drawable.shirt6
        };
        int[] pantsArray = {
                R.drawable.pants,
                R.drawable.pants2,
                R.drawable.pants3,
                R.drawable.pants4,
                R.drawable.pants5
        };
        FirebaseManager manager = new FirebaseManager(context, null);
        for (int shirt: shirts) {
            Uri imageUri = getImageUriFromDrawable(context, shirt);
            manager.uploadImageToFirebase(context, imageUri, ClothingType.SHIRT);
        }
        for (int pants: pantsArray) {
            Uri imageUri = getImageUriFromDrawable(context, pants);
            manager.uploadImageToFirebase(context, imageUri, ClothingType.PANTS);
        }
    }
    */
}
