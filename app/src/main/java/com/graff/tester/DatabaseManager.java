package com.graff.tester;

import android.content.Context;
import android.net.Uri;

import com.graff.tester.models.ClothingItem;
import com.graff.tester.models.ClothingType;

public interface DatabaseManager {
    // Users
    void createUser(String email, String password, OnUserAddedCallback onUserAddedCallback);
    void loginUser(String email, String password, OnLoginCallback callback);
    void signOut();
    boolean isUserLoggedIn();

    // clothing items
    void uploadImageToDatabase(Context context, Uri imageUri, ClothingType clothingType,
                               OnImageUploadedCallback uploadCallback);
    void downloadClothingImages(OnHandleItemDownloadedCallback callback,
                                OnHandleItemsDownloadCompletedCallback completedCallback);
    void deleteItem(ClothingItem item, OnDeleteItemCallback callback);
    void saveItemDescription(ClothingItem item, String newDesc);

    // callbacks
    interface OnLoginCallback {
        void onLoginSuccess();
        void onLoginFailed(String errorMessage);
    }

    interface OnDeleteItemCallback {
        void onDeleteItem(ClothingItem item);
    }

    interface OnHandleItemDownloadedCallback {
        void onHandleItemDownloaded(ClothingItem item);
    }

    interface OnHandleItemsDownloadCompletedCallback {
        void onHandleItemsDownloadCompleted();
    }

    interface OnImageUploadedCallback {
        void onImageUploaded(ClothingItem item);
    }

    interface OnUserAddedCallback {
        void onUserAddedSuccessfully();
        void onUserAdditionFailed(String errorMessage);
    }

}
