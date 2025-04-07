package com.graff.tester;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.graff.tester.models.ClothingType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FirebaseManager {
    final private FirebaseFirestore db;

    // Private constructor so no one can instantiate from outside
    public FirebaseManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void loadClothingImages(OnFirstImageLoadedCallback firstCallback,
                                   OnAllImagesLoadedCallback allCallback) {
        db.collection("clothes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean firstShirtSent = false;
                    boolean firstPantsSent = false;
                    List<String> shirtImages = new ArrayList<>();
                    List<String> pantsImages = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String imageUrl = doc.getString("imageUrl");
                        String typeString = doc.getString("type");
                        ClothingType type = ClothingType.fromString(typeString);

                        if (imageUrl != null && type != null) {
                            if (type == ClothingType.SHIRT) {
                                shirtImages.add(imageUrl);
                                if (!firstShirtSent) {
                                    firstCallback.onFirstImageLoaded(ClothingType.SHIRT, imageUrl);
                                    firstShirtSent = true;
                                }
                            } else if (type == ClothingType.PANTS) {
                                pantsImages.add(imageUrl);
                                if (!firstPantsSent) {
                                    firstCallback.onFirstImageLoaded(ClothingType.PANTS, imageUrl);
                                    firstPantsSent = true;
                                }
                            }
                        }
                    }

                    allCallback.onAllImagesLoaded(shirtImages, pantsImages);
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error loading images", e));
    }

    public void uploadImageToFirebase(Context context, Uri imageUri, ClothingType clothingType,
                                      OnImageUploadedCallback uploadCallback) {
        if (imageUri == null) {
            Toast.makeText(context, "Failed to convert image", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileExtension = getFileExtension(context, imageUri);
        String uniqueId = UUID.randomUUID().toString();  // Generates a unique ID for each image

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("clothes/" + uniqueId + "." + fileExtension);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        // Check if file exists before getting download URL
                        storageRef.getMetadata().addOnSuccessListener(storageMetadata ->
                                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String imageUrl = uri.toString();
                                    saveImageToFirestore(context, imageUrl, clothingType);
                                    uploadCallback.onImageUploaded(clothingType, imageUrl);
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
        String uniqueId = UUID.randomUUID().toString();  // Generates a unique ID for each image
        clothingItem.put("uniqueId", uniqueId);  // Store unique ID in Firestore

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

    public interface OnFirstImageLoadedCallback {
        void onFirstImageLoaded(ClothingType type, String imageUrl);
    }

    public interface OnAllImagesLoadedCallback {
        void onAllImagesLoaded(List<String> shirtImages, List<String> pantsImages);
    }

    public interface OnImageUploadedCallback {
        void onImageUploaded(ClothingType type, String imageUrl);
    }

}
