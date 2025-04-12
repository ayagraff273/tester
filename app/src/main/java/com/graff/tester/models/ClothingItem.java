package com.graff.tester.models;

import com.google.firebase.firestore.DocumentReference;

public class ClothingItem {
    public DocumentReference docRef;
    public String imageUrl;
    public ClothingType clothingType;

    public ClothingItem(DocumentReference docRef, String imageUrl, ClothingType clothingType) {
        this.docRef = docRef;
        this.imageUrl = imageUrl;
        this.clothingType = clothingType;
    }

    public DocumentReference getDocRef() {
        return docRef;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ClothingType getClothingType() {
        return clothingType;
    }
}
