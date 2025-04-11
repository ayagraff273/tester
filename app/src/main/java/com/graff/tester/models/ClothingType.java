package com.graff.tester.models;

import androidx.annotation.NonNull;

public enum ClothingType {
    SHIRT, PANTS;

    // Convert Enum to String
    @NonNull
    @Override
    public String toString() {
        return name().toLowerCase(); // Converts to lowercase for consistency
    }

    // Convert String to Enum (Handles invalid input safely)
    public static ClothingType fromString(String type) {
        if (type == null) return null;
        try {
            return ClothingType.valueOf(type.toUpperCase()); // Matches stored Firestore strings
        } catch (IllegalArgumentException e) {
            return null; // Return null or throw an error if the type is invalid
        }
    }
}
