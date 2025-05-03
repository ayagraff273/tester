package com.graff.tester.models;

import com.google.firebase.firestore.DocumentReference;

public class ClothingItem {
    public DocumentReference docRef;
    public String imageUrl;
    public ClothingType clothingType;
    public String description;

    public ClothingItem(DocumentReference docRef, String imageUrl, ClothingType clothingType,
                        String description) {
        this.docRef = docRef;
        this.imageUrl = imageUrl;
        this.clothingType = clothingType;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDesc) {
        this.description = newDesc;
    }
}
