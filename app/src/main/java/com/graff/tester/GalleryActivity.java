package com.graff.tester;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.graff.tester.models.ClothingItem;
import com.graff.tester.models.ClothingItemRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    final private List<ClothingItem> clothingItems = new ArrayList<>();  // List of URLs for images

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        RecyclerView recyclerView = findViewById(R.id.recyclerView_gallery);

        List<ClothingItem> shirtItems = ClothingItemRepository.getInstance().getShirtItems();
        List<ClothingItem> pantsItems = ClothingItemRepository.getInstance().getPantsItems();

        clothingItems.addAll(shirtItems);
        clothingItems.addAll(pantsItems);
        Collections.shuffle(clothingItems);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3); // 3 columns
        recyclerView.setLayoutManager(layoutManager);

        GalleryAdapter adapter = new GalleryAdapter(this, clothingItems);
        recyclerView.setAdapter(adapter);
    }
}
