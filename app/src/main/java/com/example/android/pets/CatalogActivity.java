/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;


import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetCursorAdapter;
import com.example.android.pets.data.PetDbHelper;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private PetDbHelper mDbHelper;
    private PetCursorAdapter adapter;
    private static final int URI_LOADER = 0;
    private String[] projection = {
            PetEntry._ID,
            PetEntry.COLUMN_PET_NAME,
            PetEntry.COLUMN_PET_BREED,
            PetEntry.COLUMN_PET_GENDER,
            PetEntry.COLUMN_PET_WEIGHT };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mDbHelper = new PetDbHelper(this);

        ListView petListView = (ListView) findViewById(R.id.list_view_pet);
        petListView.setEmptyView(findViewById(R.id.empty_view));

        adapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(adapter);

        getLoaderManager().initLoader(URI_LOADER, null, this);


        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                intent.setData(currentPetUri);
                startActivity(intent);
            }
        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        displayDatabaseInfo();
//    }

//    private void displayDatabaseInfo() {
//
//        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI, projection, null, null, null);
//
//        ListView petListView = (ListView) findViewById(R.id.list_view_pet);
//        petListView.setEmptyView(findViewById(R.id.empty_view));
//
//        adapter = new PetCursorAdapter(this, cursor);
//        petListView.setAdapter(adapter);
//    }

    private void insertPet() {
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);
        getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllPets() {
        int rowsDeleted =  getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        if (rowsDeleted != 0) {
            Toast.makeText(this, getString(R.string.editor_delete_pets_successful),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_delete_pets_failed),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i){
            case URI_LOADER:
                return new CursorLoader(
                        this,
                        PetEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
