package in.ac.iiit.cvit.wordhelp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by jerin on 4/12/16.
 */

public class DbApi {
    private SQLiteDatabase db;
    private FeedReaderDbHelper mDbHelper;


    public DbApi(Context context){
        mDbHelper = new FeedReaderDbHelper(context);
        // Gets the data repository in write mode
        db = mDbHelper.getWritableDatabase();


    }

    public void add_entry(String parent, String child){
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_PARENT, parent);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CHILD, child);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
    }

    public ArrayList<String> get_entries(String parent){
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_PARENT,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CHILD
        };

        // Filter results WHERE "title" = 'My Title'
            String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_PARENT + " = ?";
            String[] selectionArgs = { parent };
        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedReaderContract.FeedEntry.COLUMN_NAME_CHILD + " DESC";

        Cursor c = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        ArrayList<String> result = new ArrayList<>();

        int childId = c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_CHILD);
        try{
            while ( c.moveToNext()){
                String child = c.getString(childId);
                result.add(child);
            }
        }
        finally{
            c.close();
        }
        return result;
    }
}
