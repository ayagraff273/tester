package com.graff.tester;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.graff.tester.models.ClothingItem;
import com.graff.tester.models.ClothingType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FirebaseManager {
    final private FirebaseFirestore db;

    // Private constructor so no one can instantiate from outside
    public FirebaseManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createUser(String email, String password, OnUserAddedCallback onUserAddedCallback) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Signup success → go back to MainActivity
                        onUserAddedCallback.onUserAddedSuccessfully();
                    } else {
                        Exception e = task.getException();
                        String errorMessage = (e != null) ? e.getMessage() : "Signup failed.";
                        Log.w("Firebase", "Signup error", e);
                        onUserAddedCallback.onUserAdditionFailed(errorMessage);
                    }
                });

    }
    public void loginUser(String email, String password, OnLoginCallback callback) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onLoginSuccess();
                    } else {
                        callback.onLoginFailed(task.getException().getMessage());
                    }
                });
    }

    public interface OnLoginCallback {
        void onLoginSuccess();
        void onLoginFailed(String errorMessage);
    }


    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    public void downloadClothingImages(OnHandleItemDownloadedCallback callback,
                                       OnHandleItemsDownloadCompletedCallback completedCallback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w("Firebase", "No Logged-in User");
            return;
        }
        db.collection("users")
                .document(currentUser.getUid())
                .collection("clothes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String imageUrl = doc.getString("imageUrl");
                        String typeString = doc.getString("type");
                        ClothingType clothingType = ClothingType.fromString(typeString);

                        if (imageUrl != null && clothingType != null) {
                            ClothingItem item = new ClothingItem(
                                    doc.getReference(),
                                    imageUrl,
                                    clothingType
                            );
                            if (callback != null)
                                callback.onHandleItemDownloaded(item);
                        }
                    }
                    if (completedCallback != null)
                        completedCallback.onHandleItemsDownloadCompleted();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error loading images", e));
    }

    public void uploadImageToFirebase(Context context, Uri imageUri, ClothingType clothingType,
                                      OnImageUploadedCallback uploadCallback) {
        if (imageUri == null) {
            Toast.makeText(context, "Failed to convert image", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w("Firebase", "No Logged-in User");
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
                                    saveImageToFirestore(context, imageUrl, clothingType, uploadCallback);
                                })
                        ).addOnFailureListener(e ->
                                Toast.makeText(context, "Error: File not found!", Toast.LENGTH_SHORT).show()
                        )
                );
    }

    private void saveImageToFirestore(Context context, String imageUrl, ClothingType clothingType,
                                     OnImageUploadedCallback uploadCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w("Firebase", "No Logged-in User");
            return;
        }

        Map<String, Object> clothingItem = new HashMap<>();
        clothingItem.put("imageUrl", imageUrl);
        clothingItem.put("type", clothingType.toString());
        // using Firestore Timestamp
        Timestamp ts = Timestamp.now();
        clothingItem.put("createdAt", ts);
        clothingItem.put("updatedAt", ts);
        String uniqueId = UUID.randomUUID().toString();  // Generates a unique ID for each image
        clothingItem.put("uniqueId", uniqueId);  // Store unique ID in Firestore

        db.collection("users")
                .document(currentUser.getUid())
                .collection("clothes")
                .add(clothingItem)
                .addOnSuccessListener(documentReference -> {
                    ClothingItem item = new ClothingItem(documentReference, imageUrl, clothingType);
                    if (uploadCallback != null){
                    uploadCallback.onImageUploaded(item);
                    }
                })
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

    public void deleteItem(ClothingItem item, OnDeleteItemCallback callback) {
        FirebaseStorage.getInstance().getReferenceFromUrl(item.getImageUrl())
                .delete()
                .addOnSuccessListener(aVoid -> item.docRef.delete()
                        .addOnSuccessListener(a -> {
                            if(callback != null) {
                                // Successfully deleted the document
                                callback.onDeleteItem(item);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle the error
                            Log.w("Firebase", "Error deleting document", e);
                        }))
                .addOnFailureListener(e -> {
                    // Handle the error deleting the image
                    Log.w("Firebase", "Error deleting image", e);
                });
    }


    public interface OnDeleteItemCallback {
        void onDeleteItem(ClothingItem item);
    }

    public interface OnHandleItemDownloadedCallback {
        void onHandleItemDownloaded(ClothingItem item);
    }

    public interface OnHandleItemsDownloadCompletedCallback {
        void onHandleItemsDownloadCompleted();
    }

    public interface OnImageUploadedCallback {
        void onImageUploaded(ClothingItem item);
    }

    public interface OnUserAddedCallback {
        void onUserAddedSuccessfully();
        void onUserAdditionFailed(String errorMessage);
    }

}
