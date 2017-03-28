package com.memorycreat.memorycreator.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import static com.memorycreat.memorycreator.data.DBContract.MarkerData.COLUMN_DATE;
import static com.memorycreat.memorycreator.data.DBContract.MarkerData.COLUMN_EXPERIENCE;
import static com.memorycreat.memorycreator.data.DBContract.MarkerData.COLUMN_ID;
import static com.memorycreat.memorycreator.data.DBContract.MarkerData.COLUMN_LAT;
import static com.memorycreat.memorycreator.data.DBContract.MarkerData.COLUMN_LNG;
import static com.memorycreat.memorycreator.data.DBContract.MarkerData.TABLE_NAME;


/**
 * Created by Paul Stetsenko on 03.11.16.
 * Simple database that contains five columns:
 * id, date, impressions, latitude and longitude.
 */

public class DBHelper extends SQLiteOpenHelper {



    DBHelper(Context context)
    {
        super(context, DBContract.MarkerData.DATABASE_NAME , null, 1);
    }

    /**
     * Constructs the database.
     * @param db database to create
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT NOT NULL, " +
                COLUMN_EXPERIENCE + " TEXT NOT NULL, " +
                COLUMN_LAT + " REAL NOT NULL, " +
                COLUMN_LNG + " REAL NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS markers");
        onCreate(db);
    }
}
