package com.graff.tester;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.graff.tester.models.ClothingItem;
import com.graff.tester.models.ClothingItemRepository;
import com.graff.tester.models.ClothingType;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private final Context context;
    private final List<ClothingItem> clothingItems;

    public GalleryAdapter(Context context, List<ClothingItem> clothingItems) {
        this.context = context;
        this.clothingItems = clothingItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int columns = 3;
        int dp = 16;
        int spacing = dpToPx(dp);
        int size = (screenWidth / columns) - spacing;

        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params != null) {
            params.width = size;
            params.height = size;
            holder.itemView.setLayoutParams(params);
        }

        ClothingItem clothingItem = clothingItems.get(position);

        Glide.with(context)
                .load(clothingItem.getImageUrl())
                .into(holder.imageView);

        holder.btnDelete.setOnClickListener(v -> {
            ClothingItem item = clothingItems.get(position);

            if (!holder.canRemoveFromList(item)) {
                Toast.makeText(context, "You must have at least 2 " + item.getClothingType() + "s!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(context)
                    .setTitle("delete clothing item")
                    .setMessage("are you sure you want to delete this item?")
                    .setPositiveButton("yes", (dialog, which) -> {
                        DatabaseManager databaseManager = DataManagerFactory.getDataManager();
                        databaseManager.deleteItem(item, item1 -> {
                            ClothingItemRepository.getInstance().removeItem(item);
                            clothingItems.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        holder.itemView.setOnClickListener(v -> {
            View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_description, null);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(bottomSheetView);

            TextView descriptionText = bottomSheetView.findViewById(R.id.descriptionText);
            Button editButton = bottomSheetView.findViewById(R.id.editButton);

            descriptionText.setText(clothingItem.getDescription());

            editButton.setOnClickListener(view -> {
                showEditDialog(clothingItem, holder.getAdapterPosition(), descriptionText);
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();
        });
    }

    private void showEditDialog(ClothingItem item, int position, TextView descriptionText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText input = new EditText(context);
        input.setText(item.getDescription());
        input.setMinLines(3);
        input.setGravity(Gravity.TOP | Gravity.START);

        builder.setTitle("עריכת תיאור")
                .setView(input)
                .setPositiveButton("שמור", (dialog, which) -> {
                    String newDesc = input.getText().toString();
                    item.setDescription(newDesc);
                    notifyItemChanged(position);
                    descriptionText.setText(newDesc);
                    DatabaseManager databaseManager = DataManagerFactory.getDataManager();
                    databaseManager.saveItemDescription(item, newDesc);


                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return clothingItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public boolean canRemoveFromList(ClothingItem item) {
            List<ClothingItem> shirts = ClothingItemRepository.getInstance().getShirtItems();
            List<ClothingItem> pants = ClothingItemRepository.getInstance().getPantsItems();
            ClothingType clothingType = item.getClothingType();
            if (clothingType == ClothingType.SHIRT && shirts.size() <= 2) {
                return false;
            } else return clothingType != ClothingType.PANTS || pants.size() > 2;
        }
    }
}
