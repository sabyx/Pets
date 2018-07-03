package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PetProvider extends ContentProvider {

    private static final int PETS = 100;
    private static final int PET_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private PetDbHelper petDb;
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);
    }

    @Override
    public boolean onCreate() {
        petDb = new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = petDb.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {
        SQLiteDatabase database = petDb.getWritableDatabase();
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        int gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender != PetContract.PetEntry.GENDER_FEMALE && gender != PetContract.PetEntry.GENDER_MALE
                && gender != PetContract.PetEntry.GENDER_UNKNOWN) {
            throw new IllegalArgumentException("Pet gender can be 0, 1, 2");
        }
        int weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight < 0){
            throw new IllegalArgumentException("Pet weight needs to be a positive number");
        }

        long id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = petDb.getWritableDatabase();
        int rowsDeleted = 0;
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                rowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int rowsUpdated = 0;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                rowsUpdated = updatePet(uri, contentValues, selection, selectionArgs);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsUpdated = updatePet(uri, contentValues, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = petDb.getWritableDatabase();
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)) {
            int gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender != PetContract.PetEntry.GENDER_FEMALE && gender != PetContract.PetEntry.GENDER_MALE
                    && gender != PetContract.PetEntry.GENDER_UNKNOWN) {
                throw new IllegalArgumentException("Pet gender can be 0, 1, 2");
            }
        }
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)) {
            int weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight < 0){
                throw new IllegalArgumentException("Pet weight needs to be a positive number");
            }
        }
        return database.update(PetContract.PetEntry.TABLE_NAME, values, selection,selectionArgs);
    }
}
