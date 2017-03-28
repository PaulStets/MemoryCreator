package com.memorycreat.memorycreator.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.memorycreat.memorycreator.data.DBContract.MarkerData.COLUMN_LAT;
import static com.memorycreat.memorycreator.data.DBContract.MarkerData.COLUMN_LNG;

/**
 * Created by paul on 20.02.17.
 * Content provider for MemoryCreator
 * Stores the information provided by the user and location of the marker.
 */

public class DataProvider extends ContentProvider{

    public static final int CODE_ALL_MARKERS = 100;

    // Uri matcher used by this content provider
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DBHelper mDBHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DBContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DBContract.PATH_MARKERS, CODE_ALL_MARKERS);


        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_ALL_MARKERS:
                cursor = mDBHelper.getWritableDatabase().query(DBContract.MarkerData.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Not Implemented!");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        switch (sUriMatcher.match(uri)) {
            case CODE_ALL_MARKERS:
                long id = mDBHelper.getWritableDatabase().insert(DBContract.MarkerData.TABLE_NAME,
                        null,
                        contentValues);
                if (id != -1) {

                    return DBContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath(contentValues.getAsString(COLUMN_LAT))
                            .appendPath(contentValues.getAsString(COLUMN_LNG))
                            .build();
                }
                else {
                    throw new RuntimeException("The data was not inserted!");
                }
            default:
                throw new RuntimeException("Invalid Insertion URI!");
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;

        if (selection == null) { selection = "1"; }

        switch (sUriMatcher.match(uri)) {
            case CODE_ALL_MARKERS:
                numRowsDeleted = mDBHelper.getWritableDatabase()
                        .delete(DBContract.MarkerData.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new RuntimeException("Invalid Insertion URI!");
        }


        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {

        int numRowsUpdated;

        if (selection == null) { selection = "1"; }

        switch (sUriMatcher.match(uri)) {
            case CODE_ALL_MARKERS:
                numRowsUpdated = mDBHelper.getWritableDatabase()
                        .update(DBContract.MarkerData.TABLE_NAME,
                                contentValues,
                                selection,
                                selectionArgs);
                break;
            default:
                throw new RuntimeException("Invalid Insertion URI!");
        }


        return numRowsUpdated;
    }

}
