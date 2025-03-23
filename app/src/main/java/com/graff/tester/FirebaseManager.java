package com.graff.tester;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    public void uploadImageToFirebase(Context context, Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(context, "Failed to convert image", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("clothes/" + System.currentTimeMillis() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Check if file exists before getting download URL
                    storageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveImageToFirestore(context, imageUrl);
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(context, "Error: File not found!", Toast.LENGTH_SHORT).show();
                    });
                });

    }

    public void saveImageToFirestore(Context context, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> clothingItem = new HashMap<>();
        clothingItem.put("imageUrl", imageUrl);
        clothingItem.put("type", "Pants"); // Example clothing type

        db.collection("clothes").add(clothingItem)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
