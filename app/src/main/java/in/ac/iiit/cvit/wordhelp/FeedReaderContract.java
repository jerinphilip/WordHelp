package in.ac.iiit.cvit.wordhelp;

import android.provider.BaseColumns;

/**
 * Created by jerin on 4/12/16.
 */

public final class FeedReaderContract {
        // To prevent someone from accidentally instantiating the contract class,
        // make the constructor private.
    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "parentchild";
        public static final String COLUMN_NAME_PARENT = "parent";
        public static final String COLUMN_NAME_CHILD = "child";
    }



}
