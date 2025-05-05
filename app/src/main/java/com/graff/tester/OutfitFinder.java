package com.graff.tester;

import com.graff.tester.models.ClothingItem;

import java.util.List;

public interface OutfitFinder {
    void findOutfit(String outfitDescription, List<ClothingItem> shirts, List<ClothingItem> pants);
}
