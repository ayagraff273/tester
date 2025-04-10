package com.graff.tester;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private List<String> imageUrls = new ArrayList<>();  // List of URLs for images

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        RecyclerView recyclerView = findViewById(R.id.recyclerView_gallery);

        // Get the image URLs from MainActivity (passed via Intent or other mechanism)
        if (getIntent() != null) {
            imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        }

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3); // 3 columns
        recyclerView.setLayoutManager(layoutManager);

        GalleryAdapter adapter = new GalleryAdapter(this, imageUrls);
        recyclerView.setAdapter(adapter);
    }
}
