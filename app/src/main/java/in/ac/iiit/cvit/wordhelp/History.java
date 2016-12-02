package in.ac.iiit.cvit.wordhelp;

import android.net.Uri;

import java.io.File;

/**
 * Created by jerin on 2/12/16.
 */

public class History {
    private File mFile;
    private boolean mOnline;
    public History(File file, boolean online){
        mFile = file;
        mOnline = online;
    }

    public Uri getFile(){
        return getFile();
    }

    public boolean isOnline(){
        return mOnline;
    }
}
