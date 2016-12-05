package in.ac.iiit.cvit.wordhelp;

/**
 * Created by jerin on 26/10/16.
 */

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImageHandle {
    boolean ready;
    File mediaStorageDir;
    File subMediaStorageDir;

    public ImageHandle(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "WordHelp");

        subMediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "WordHelp/sub");
        ready = true;

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("WordHelp", "failed to create directory");
                ready = false;
            }
        }
        if (!subMediaStorageDir.exists()) {
            if (!subMediaStorageDir.mkdirs()) {
                Log.d("WordHelp", "failed to create directory");
                ready = false;
            }
        }

    }

    public File newImage(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    public File newWordImage(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(subMediaStorageDir.getPath() + File.separator +
                "SUBIMG_" + timeStamp + ".jpg");
        return mediaFile;
    }
    public File newVideo(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + timeStamp + ".mp4");
        return mediaFile;
    }


    public ArrayList<File> getImageList(){
        ArrayList<File> ls = new ArrayList<>();
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "WordHelp");
        File[] files = mediaStorageDir.listFiles();
        for(File file: files){
            if(!file.isDirectory()){
                ls.add(file);
                Log.d("FILE", file.getName());
            }
        }
        return ls;

    }


}

