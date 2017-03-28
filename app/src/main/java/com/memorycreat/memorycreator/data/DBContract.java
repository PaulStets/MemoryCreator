package com.memorycreat.memorycreator.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by paul on 20.02.17.
 * The contract of the database.
 * Defines the table and columns of the database.
 */

public class DBContract {
    // The name of the Content Provider
    public static final String CONTENT_AUTHORITY = "com.memorycreat.memorycreator";
    // Base Uri to query the database
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MARKERS = "markers";


    public static final class MarkerData implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MARKERS)
                .build();
        // The internal name of the database
        public static final String DATABASE_NAME = "MarkersInfo.db";
        // The name of the table
        public static final String TABLE_NAME = "markers";
        // Column names
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_EXPERIENCE = "experience";
        public static final String COLUMN_LAT = "latitude";
        public static final String COLUMN_LNG = "longitude";

        public static Uri buildUriWithLatLng(double lat, double lng) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Double.toString(lat))
                    .appendPath(Double.toString(lng))
                    .build();
        }
    }
}
