package com.memorycreat.memorycreator;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.memorycreat.memorycreator.data.DBContract;
import java.util.Calendar;
import static com.memorycreat.memorycreator.data.DBContract.MarkerData.COLUMN_LAT;
import static com.memorycreat.memorycreator.data.DBContract.MarkerData.COLUMN_LNG;

public class MarkerInfoActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID_INFO_LOADER = 134;

    private static final int ID_ACTION_INSERT = 0;
    private static final int ID_ACTION_UPDATE = 1;
    private static final int ID_ACTION_DELETE = 2;

    private static final String[] TEXT_PROJECTION = { DBContract.MarkerData.COLUMN_DATE,
            DBContract.MarkerData.COLUMN_EXPERIENCE };

    private static final String[] COLUMNS_PROJECTION = { DBContract.MarkerData.COLUMN_DATE,
            DBContract.MarkerData.COLUMN_EXPERIENCE,
            DBContract.MarkerData.COLUMN_LAT,
            DBContract.MarkerData.COLUMN_LNG};

    private String mDateText;
    private String mExperienceText;

    private ContentValues mContentValues;


    private static final String selection = COLUMN_LAT + " = ? AND " +
            COLUMN_LNG + " = ?";

    private String[] selectionArgs;


    public static final int INDEX_DATE = 0;
    public static final int INDEX_EXPERIENCE = 1;

    private Uri mUri;
    private TextView tWDate;
    private EditText eTExp;


    private double currLat;
    private double currLng;

    private boolean exists;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_info);

        tWDate = (TextView) findViewById(R.id.tw_date);
        eTExp = (EditText) findViewById(R.id.impressions);

        mUri = getIntent().getData();
        final String[] allArguments = mUri.getPath().split("/");
        currLat = Double.valueOf(allArguments[allArguments.length - 2]);
        currLng = Double.valueOf(allArguments[allArguments.length - 1]);
        selectionArgs = new String[] { allArguments[allArguments.length - 2],
                allArguments[allArguments.length - 1] };

        if (mUri == null) throw new RuntimeException("URI cannot be null!");


        getSupportLoaderManager().initLoader(ID_INFO_LOADER, null, this);
    }

    /**
     * Loader that deals with loading data from the content provider asynchroniously.
     * @param loaderId
     * @param args arguments to query the provider
     * @return Cursor with provided data.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        mUri = getIntent().getData();
        final String[] allArguments = mUri.getPath().split("/");
        selectionArgs = new String[] { allArguments[allArguments.length - 2],
                allArguments[allArguments.length - 1] };

        switch (loaderId) {

            case ID_INFO_LOADER:

                return new CursorLoader(this,
                        DBContract.MarkerData.CONTENT_URI,
                        TEXT_PROJECTION,
                        selection,
                        selectionArgs,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }



    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        if (!data.moveToFirst()) {
            exists = false;
            return;
        }

        tWDate.setText(data.getString(INDEX_DATE));
        eTExp.setText(data.getString(INDEX_EXPERIENCE));
        exists = true;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.isStarted()) {
            loader.cancelLoad();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getLoaderManager().destroyLoader(ID_INFO_LOADER);
        getSupportLoaderManager().initLoader(ID_INFO_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.marker_info_activity_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         int itemId = item.getItemId();
        if(itemId == R.id.action_share) {
            Intent shareIntent = createShareIntent();
            if (shareIntent != null) {
                startActivity(shareIntent);
                return true;
            }
        }


        return super.onOptionsItemSelected(item);

    }

    /**
     * Creates an intent with the text from this activity.
     * @return Intent with text, null if text is empty.
     */
    @Nullable
    private Intent createShareIntent() {
        if (eTExp.getText().toString().isEmpty()) {
            Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show();
            return null;
        }
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(eTExp.getText().toString())
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    /**
     * Handles the clicking on the Sava button
     * @param view Save button
     */
    public void saveInfo(View view) {

        mDateText = tWDate.getText().toString();
        mExperienceText = eTExp.getText().toString();
        mContentValues = new ContentValues();

        if (!exists){

            if (mDateText.isEmpty() || mExperienceText.isEmpty()) {
                Toast.makeText(this,
                        "You forgot to fill all of the fields!",
                        Toast.LENGTH_LONG).show();
                return;
            }

            dataProcessing.execute(ID_ACTION_INSERT);

            resumeMapsActivity();
        }
        else {

            dataProcessing.execute(ID_ACTION_UPDATE);
            resumeMapsActivity();
        }

    }

    /**
     * Resumes maps activity.
     */
    private void resumeMapsActivity() {
        getLoaderManager().destroyLoader(ID_INFO_LOADER);
        Intent intent = new Intent(MarkerInfoActivity.this, MapsActivity.class);
        startActivity(intent);

    }

    /**
     * Handles clicking on delete button.
     * @param view Delete button
     */
    public void deleteInfo(View view) {

        dataProcessing.execute(ID_ACTION_DELETE);
        resumeMapsActivity();
    }


    /**
     * AsyncTask to update data in the Content Provider.
     * throws UnsupportedOperationException
     */
    public AsyncTask<Integer, Void, Void> dataProcessing = new AsyncTask<Integer, Void, Void>() {

        @Override
        protected Void doInBackground(Integer... integers) {
            switch (integers[0]) {

                case ID_ACTION_INSERT:
                    mContentValues.put(COLUMNS_PROJECTION[0], mDateText);
                    mContentValues.put(COLUMNS_PROJECTION[1], mExperienceText);
                    mContentValues.put(COLUMNS_PROJECTION[2], currLat);
                    mContentValues.put(COLUMNS_PROJECTION[3], currLng);
                    getContentResolver().insert(DBContract.MarkerData.CONTENT_URI, mContentValues);
                    break;
                case ID_ACTION_UPDATE:
                    mContentValues.put(COLUMNS_PROJECTION[0], mDateText);
                    mContentValues.put(COLUMNS_PROJECTION[1], mExperienceText);
                    getContentResolver().update(DBContract.MarkerData.CONTENT_URI,
                            mContentValues,
                            selection,
                            selectionArgs);
                    break;
                case ID_ACTION_DELETE:
                    int deleted = getContentResolver().delete(DBContract.MarkerData.CONTENT_URI,
                            selection,
                            selectionArgs);

                    if (deleted == 0) {
                        Log.d("Error", "Not Deleted!");
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();

            }
            return null;
        }
    };

    /**
     * An inner class that represents a date picker
     * see https://developer.android.com/guide/topics/ui/controls/pickers.html
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {


        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            TextView tWDate = (TextView) getActivity().findViewById(R.id.tw_date);
            tWDate.setText(day + "-" + (month + 1) + "-" + year);

        }
    }


    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

}
