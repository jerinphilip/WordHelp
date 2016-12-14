package in.ac.iiit.cvit.wordhelp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by jerin on 2/12/16.
 */

public class History {
    private File mFile;
    private boolean mOnline;
    public History(File file){
        mFile = file;
    }

    public Uri getUri(){
        return Uri.fromFile(mFile);
    }

    public String getName(){
        return mFile.getName();
    }

    public Bitmap getThumbNail(){
        InputStream imageStream;
        try {
            imageStream = new FileInputStream(mFile);
            Bitmap image = BitmapFactory.decodeStream(imageStream);
            Bitmap thumbnail = generateThumbnail(image);
            return thumbnail;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Bitmap generateThumbnail(Bitmap image){
        int THUMBSIZE = 128;
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(image,
                THUMBSIZE, THUMBSIZE);
        return thumbnail;
    }

}
