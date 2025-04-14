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

    public static void uploadClothingDrawableToFirebase(Context context, int drawableId, ClothingType clothingType, FirebaseManager.OnImageUploadedCallback callback) {
        Uri imageUri = getImageUriFromDrawable(context, drawableId);
        FirebaseManager manager = new FirebaseManager();
        manager.uploadImageToFirebase(context, imageUri, clothingType, callback);
    }
}
