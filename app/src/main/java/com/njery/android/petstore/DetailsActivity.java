package com.njery.android.petstore;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.njery.android.petstore.data.PetContract.PetEntry;

public class DetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int EXISTING_PET_LOADER = 0;

    private Spinner mGenderSpinner;
    private EditText mNameEditText, mBreedEditText, mWeightEditText;

    private int mGender = PetEntry.GENDER_UNKNOWN;
    private boolean mPetHasChanged = false;

    private Uri mCurrentPetUri;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        if(mCurrentPetUri == null){
            setTitle(getString(R.string.add_pet_label));

            //invalidate options menu
            invalidateOptionsMenu();
        }
        else{
            setTitle(getString(R.string.edit_pet_label));

            //initialize a loader to read data from the database and display current values in the editor
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        mNameEditText = findViewById(R.id.et_name);
        mBreedEditText = findViewById(R.id.et_breed);
        mWeightEditText = findViewById(R.id.et_weight);
        mGenderSpinner = findViewById(R.id.gender_spinner);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    private void savePet(){
        // read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();

        // check if all fields in the editor are blank
        if (mCurrentPetUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_NAME, nameString);
        values.put(PetEntry.COLUMN_BREED, breedString);
        values.put(PetEntry.COLUMN_GENDER, mGender);

        int weight = 0;
        if(!TextUtils.isEmpty(weightString)){
            weight = Integer.parseInt(weightString);
        }

        values.put(PetEntry.COLUMN_WEIGHT, weight);

        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null
        if(mCurrentPetUri == null){
            // This is a new pet
            Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            if(newUri == null)
                Toast.makeText(this, R.string.save_error, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.pet_saved, Toast.LENGTH_SHORT).show();
        }else{
            // This is an existing pet
            int rowsAffected = getContentResolver().update(mCurrentPetUri, values, null, null);

            if (rowsAffected == 0)
                Toast.makeText(this, R.string.save_edit_error, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.pet_saved, Toast.LENGTH_SHORT).show();


        }
    }

    private void setupSpinner(){
        //Create the adapter for the spinner
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.gender_array, android.R.layout.simple_spinner_item);

        //specify dropdown style  = simple list with one item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //apply the adapter to the spinner

        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        //set the slected items to the constant gender values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int index = parent.getSelectedItemPosition();
                if (index == 0){
                    mGender = PetEntry.GENDER_UNKNOWN;
                }
                if (index == 1){
                    mGender = PetEntry.GENDER_MALE;
                }
                if (index == 2){
                    mGender = PetEntry.GENDER_FEMALE;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    //Method called after invalidateOptionsMenu() to update menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(mCurrentPetUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete_details);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_details) {
            // Pop up confirmation for deletion
            showDeleteConfirmationDialog();
            return true;
        }
        if (id == R.id.action_save){
            savePet();
            finish();
            return true;
        }
        if (id == android.R.id.home){
            //if pet hasn't changed continue navigating to parent activity
            if (!mPetHasChanged){
                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                return true;
            }
            //if there are unsaved changes setup dialog to warn the user
            //Setup a click listener to handle cancellation by the user
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                        }
                    };
            showUnsavedChangesDialog(discardButtonClickListener);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!mPetHasChanged){
            super.onBackPressed();
            return;
        }

        //else if there's unsaved changes set up a dialog to warn user
        //create a click listener to handle cancellation by user
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //if user clicks discard button close current activity
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_NAME,
                PetEntry.COLUMN_BREED,
                PetEntry.COLUMN_GENDER,
                PetEntry.COLUMN_WEIGHT };

        // loader executes the ContentProvider's query method in the background
        return new CursorLoader(this,
                mCurrentPetUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //exit early if cursor is null or there's less than 1 row in the cursor
        if(cursor == null || cursor.getCount() < 1)
            return;

        if(cursor.moveToFirst()){
            //find the columns of interest
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_WEIGHT);

            //Extract values from cursor from obtained column index
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);

            //Update the views on the screen
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            //Change selection option on the gender spinner
            switch (gender){
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If loader is invalidated, clear out all the data from input fields
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.unsaved_changes_dialog_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //User clicks keep editing, so discard the dialog
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Delete pet confirm prompt
    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.delete_pet_dialog_message);
        builder.setPositiveButton(R.string.delete_pet, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog!=null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet(){
        if (mCurrentPetUri != null){
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);

            if(rowsDeleted == 0)
                Toast.makeText(this, getString(R.string.delete_pet_failed_message), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, getString(R.string.delete_pet_success_message), Toast.LENGTH_SHORT).show();
        }

        //close the activity
        finish();
    }
}
