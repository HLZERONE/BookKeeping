package com.example.bookkeeping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<Category> {
    public CategoryAdapter(@NonNull Context context, @NonNull List<Category> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listitemView = convertView;
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.card_item, parent, false);
        }

        Category category = getItem(position);
        ImageView categoryItemIcon = listitemView.findViewById(R.id.categoryItemIcon);
        TextView categoryItemName = listitemView.findViewById(R.id.categoryItemName);

        categoryItemName.setText(category.getCategoryName());
        categoryItemIcon.setImageResource(category.getImageId());

        return listitemView;
    }
}
