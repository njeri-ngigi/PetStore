package com.njery.android.petstore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


import com.njery.android.petstore.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {
    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private static final int PETS = 100;
    private static final int PETS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PETS_ID);
    }

    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                cursor = db.query(PetEntry.TABLE_NAME, projection,
                        selection,  selectionArgs, null, null, sortOrder);
                break;
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;
                default:
                    throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //set notification URI on the cursor such that if data at this URI changes, the cursor is updated
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int  match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values){
        String name = values.getAsString(PetEntry.COLUMN_NAME);
        if (name == null)
            throw new IllegalArgumentException("Pet Requires a name");

        Integer gender = values.getAsInteger(PetEntry.COLUMN_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender))
            throw new IllegalArgumentException("Pet requires valid gender");

        Integer weight = values.getAsInteger(PetEntry.COLUMN_WEIGHT);
        if(weight != null && weight < 0)
            throw new IllegalArgumentException("Pet requires valid weight");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(PetEntry.TABLE_NAME, null, values);

        if(id == -1){
            Log.e(LOG_TAG,"Failed to insert row for " + uri);
            return null;
        }

        // To notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not supported for " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        // check name value isn't null
        if(values.containsKey(PetEntry.COLUMN_NAME)){
            String name = values.getAsString(PetEntry.COLUMN_NAME);
            if (name == null)
                throw new IllegalArgumentException("Pet requires a name");
        }

        // check gender is valid
        if(values.containsKey(PetEntry.COLUMN_GENDER)){
            Integer gender = values.getAsInteger(PetEntry.COLUMN_GENDER);
            if(gender == null || !PetEntry.isValidGender(gender))
                throw new IllegalArgumentException("Pet requires valid gender");
        }

        //check if weight is valid
        if(values.containsKey(PetEntry.COLUMN_WEIGHT)){
            Integer weight = values.getAsInteger(PetEntry.COLUMN_WEIGHT);
            if(weight != null && weight<0){
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        //If there aren't any values to update, don't try to update the database
        if(values.size() == 0)
            return 0;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

        //if 1 or more rows were updated, notify all listeners
        if(rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        //Return the number of rows updated;
        return rowsUpdated;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                //Delete all rows matching selection and selection args
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PETS_ID:
                //delete a single row given by the id in the uri
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion not supported for " + uri);
        }

        //if 1 or more rows were deleted notify all listeners
        if(rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

}
