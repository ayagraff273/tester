package com.graff.tester;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.graff.tester.models.ClothingType;

import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private static FirebaseManager instance;

    // Private constructor so no one can instantiate from outside
    private FirebaseManager() {
        // Initialize Firebase here if needed, for example:
        // FirebaseApp.initializeApp(context);
    }

    // 3. Public method to get the single instance
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public void uploadImageToFirebase(Context context, Uri imageUri, ClothingType clothingType) {
        if (imageUri == null) {
            Toast.makeText(context, "Failed to convert image", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileExtension = getFileExtension(context, imageUri);

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("clothes/" + System.currentTimeMillis() + "." + fileExtension);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                    // Check if file exists before getting download URL
                    storageRef.getMetadata().addOnSuccessListener(storageMetadata ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveImageToFirestore(context, imageUrl, clothingType);
                        })
                    ).addOnFailureListener(e ->
                        Toast.makeText(context, "Error: File not found!", Toast.LENGTH_SHORT).show()
                    )
                );

    }

    public void saveImageToFirestore(Context context, String imageUrl, ClothingType clothingType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> clothingItem = new HashMap<>();
        clothingItem.put("imageUrl", imageUrl);
        clothingItem.put("type", clothingType.toString());
        // using Firestore Timestamp
        Timestamp ts = Timestamp.now();
        clothingItem.put("createdAt", ts);
        clothingItem.put("updatedAt", ts);

        db.collection("clothes").add(clothingItem)
                .addOnSuccessListener(documentReference ->
                    Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                    Toast.makeText(context, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private static String getFileExtension(Context context, Uri uri) {
        String extension = null;

        // Check if the URI is a file:// URI
        if ("file".equals(uri.getScheme())) {
            String path = uri.getPath();
            if (path != null) {
                extension = path.substring(path.lastIndexOf(".") + 1);
            }
        }
        // If it's a content:// URI, try to get from ContentResolver
        else if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    String fileName = cursor.getString(nameIndex);
                    extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                }
                cursor.close();
            }
        }

        // Fallback: Try using MimeTypeMap if extension is still null
        if (extension == null) {
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            }
        }

        return (extension != null) ? extension : "";  // Default to empty string if null
    }

}
