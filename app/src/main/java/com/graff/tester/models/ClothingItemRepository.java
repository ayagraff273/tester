package com.graff.tester.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ClothingItemRepository {

    // Singleton instance
    private static ClothingItemRepository instance = null;

    // Lists to store clothing items
    private List<ClothingItem> shirtItems;
    private List<ClothingItem> pantsItems;

    // Private constructor to prevent instantiation
    private ClothingItemRepository() {
        shirtItems = new ArrayList<>();
        pantsItems = new ArrayList<>();
    }

    // Get the singleton instance
    public static ClothingItemRepository getInstance() {
        if (instance == null) {
            instance = new ClothingItemRepository();
        }
        return instance;
    }

    // Getters for the lists
    public List<ClothingItem> getShirtItems() {
        return shirtItems;
    }

    public List<ClothingItem> getPantsItems() {
        return pantsItems;
    }

    // Add item to the shirt list
    public void addShirtItem(ClothingItem item) {
        shirtItems.add(item);
    }

    // Add item to the pants list
    public void addPantsItem(ClothingItem item) {
        pantsItems.add(item);
    }

    public void removeItem(ClothingItem item) {
        if (item.clothingType == ClothingType.PANTS) {
            this.pantsItems.remove(item);
        }
        else if (item.clothingType == ClothingType.SHIRT) {
            this.shirtItems.remove(item);
        }
    }



    // Clear shirt items list
    public void clearShirtItems() {
        shirtItems.clear();
    }

    // Clear pants items list
    public void clearPantsItems() {
        pantsItems.clear();
    }

    public int getShirtIndexById(String id) {
        for (int i = 0; i < shirtItems.size(); i++) {
            if (shirtItems.get(i).getId().equals(id))
                return i;
        }
        return -1;
    }

    public int getPantsIndexById(String id) {
        for (int i = 0; i < pantsItems.size(); i++) {
            if (pantsItems.get(i).getId().equals(id))
                return i;
        }
        return -1;
    }
}

