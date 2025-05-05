package com.graff.tester;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.vertexai.FirebaseVertexAI;
import com.google.firebase.vertexai.GenerativeModel;
import com.google.firebase.vertexai.java.GenerativeModelFutures;
import com.google.firebase.vertexai.type.Content;
import com.google.firebase.vertexai.type.GenerateContentResponse;
import com.graff.tester.models.ClothingItem;
import com.graff.tester.models.ClothingType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FirebaseManager implements DatabaseManager {
    public static final String Add_DESCRIPTION_NOTE = "Looks like this item needs a description. Let’s add one!";
    final private FirebaseFirestore db;
    final private GenerativeModel model;

    // Private constructor so no one can instantiate from outside
    public FirebaseManager() {
        this.db = FirebaseFirestore.getInstance();
        // Initialize the Vertex AI service and create a `GenerativeModel` instance
        // Specify a model that supports your use case
        this.model = FirebaseVertexAI.getInstance().generativeModel("gemini-2.0-flash");
    }

    @Override
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

    @Override
    public void loginUser(String email, String password, OnLoginCallback callback) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onLoginSuccess();
                    } else {
                        String errorMessage = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Unknown error occurred";
                        callback.onLoginFailed(errorMessage);
                    }
                });
    }

    @Override
    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return (currentUser != null);
    }

    @Override
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @Override
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
                        String description =  doc.contains("description") ? doc.getString("description") : Add_DESCRIPTION_NOTE;


                        if (imageUrl != null && clothingType != null) {
                            ClothingItem item = new ClothingItem(
                                    doc.getReference(),
                                    imageUrl,
                                    clothingType,
                                    description
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

    @Override
    public void uploadImageToDatabase(Context context, Uri imageUri, ClothingType clothingType,
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
                .addOnSuccessListener(taskSnapshot -> {
                    if (PreferencesManager.getUseGenAI(context)) {
                        generateAIDescription(taskSnapshot, description -> {
                            // Check if file exists before getting download URL
                            uploadMetadata(context, clothingType, uploadCallback, description, storageRef);
                        });
                    }
                    else {
                        uploadMetadata(context, clothingType, uploadCallback, Add_DESCRIPTION_NOTE, storageRef);
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(context, "Error: File not found!", Toast.LENGTH_SHORT).show()
                );
    }

    private void uploadMetadata(Context context, ClothingType clothingType, OnImageUploadedCallback uploadCallback, String description, StorageReference storageRef) {
        storageRef.getMetadata().addOnSuccessListener(storageMetadata ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveImageToFirestore(context, imageUrl, clothingType, description, uploadCallback);
                })).addOnFailureListener(e ->
                Toast.makeText(context, "Error: File not found!", Toast.LENGTH_SHORT).show()
        );
    }

    private void generateAIDescription(UploadTask.TaskSnapshot taskSnapshot, OnDescriptionGeneratedCallback callback) {
        String promptText = "You are a fashion stylist. Given an image of a clothing item, describe it in up to 16 words. you can mention season,colors, style, trendy, stylish phrasing.";

        String mimeType = Objects.requireNonNull(taskSnapshot.getMetadata()).getContentType();
        String bucket = taskSnapshot.getMetadata().getBucket();
        String filePath = taskSnapshot.getMetadata().getPath();

        if (mimeType != null && bucket != null) {
            String storageUrl = "gs://" + bucket + "/" + filePath;

            Content prompt = new Content.Builder()
                    .addFileData(storageUrl, mimeType)
                    .addText(promptText)
                    .build();

            Executor executor = Executors.newSingleThreadExecutor();
            GenerativeModelFutures modelFutures = GenerativeModelFutures.from(this.model);
            ListenableFuture<GenerateContentResponse> response = modelFutures.generateContent(prompt);

            Futures.addCallback(response, new FutureCallback<>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    callback.onDescriptionGenerated(resultText);
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    Log.w("Firebase", "Failed to generate description: " + t);
                    callback.onDescriptionGenerated("Please fill description here.");
                }
            }, executor);
        } else {
            Log.w("Firebase", "Missing MIME type or storage info");
            callback.onDescriptionGenerated("Please fill description here.");
        }
    }


    private void saveImageToFirestore(Context context, String imageUrl, ClothingType clothingType,
                                     String description, OnImageUploadedCallback uploadCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w("Firebase", "No Logged-in User");
            return;
        }

        Map<String, Object> clothingItem = new HashMap<>();
        clothingItem.put("imageUrl", imageUrl);
        clothingItem.put("type", clothingType.toString());
        clothingItem.put("description", description);
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
                    ClothingItem item = new ClothingItem(documentReference, imageUrl, clothingType, description);
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
    @Override
    public void saveItemDescription(ClothingItem item, String newDesc) {
        item.getDocRef()
                .update("description", newDesc)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Description updated"))
                .addOnFailureListener(e -> Log.w("Firebase", "Failed to update description", e));
    }
    @Override
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


    public interface OnDescriptionGeneratedCallback {
        void onDescriptionGenerated(String description);
    }
}
