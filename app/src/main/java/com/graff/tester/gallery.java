package com.graff.tester;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.List;

public class gallery extends AppCompatActivity { // שיניתי את השם ל-GalleryActivity לפי מוסכמות השמות
    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private List<Integer> imageList = Arrays.asList(
            R.drawable.shirt1,
            R.drawable.shirt2,
            R.drawable.pants3,
            R.drawable.pants4,
            R.drawable.pants5,
            R.drawable.shirt6
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery); // הורדתי את ה-); השגוי

        recyclerView = findViewById(R.id.recyclerView_gallery);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new GalleryAdapter(this, imageList);
        recyclerView.setAdapter(adapter);
    }
}
