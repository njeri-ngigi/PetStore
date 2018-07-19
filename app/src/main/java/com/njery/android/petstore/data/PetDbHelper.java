package com.njery.android.petstore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.njery.android.petstore.data.PetContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = PetDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "shelter.db";
    private static final int DATABASE_VERSION = 1;

    public static final String CREATE_ENTRIES = "CREATE TABLE " + PetEntry.TABLE_NAME
            + "("
            + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PetEntry.COLUMN_NAME + " TEXT NOT NULL, "
            + PetEntry.COLUMN_BREED + " TEXT, "
            + PetEntry.COLUMN_GENDER + " INTEGER NOT NULL, "
            + PetEntry.COLUMN_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
    public static final String DELETE_ENTRIES = "DROP TABLE " + PetEntry.TABLE_NAME + " IF EXISTS;";

    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_ENTRIES);
        onCreate(db);
    }
}
