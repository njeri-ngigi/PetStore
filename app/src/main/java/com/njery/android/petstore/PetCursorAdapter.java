package com.njery.android.petstore;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.njery.android.petstore.data.PetContract.PetEntry;

public class PetCursorAdapter extends CursorAdapter {
    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_list_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvPetName = view.findViewById(R.id.tv_pet_name);
        TextView tvPetBreed = view.findViewById(R.id.tv_pet_breed);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_NAME));
        String breed = cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_BREED));

        tvPetName.setText(name);
        tvPetBreed.setText(breed);


    }
}
