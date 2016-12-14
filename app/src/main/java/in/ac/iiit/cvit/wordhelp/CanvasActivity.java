package in.ac.iiit.cvit.wordhelp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CanvasActivity extends AppCompatActivity{
    private static int MAX_BITMAP_SIZE = 1280;
    Bitmap image;
    Bitmap croppedImage;
    TouchImageView canvas;
    DbApi db;
    Uri imageUri;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_canvas);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle!= null && bundle.containsKey("imageUri")) {
            imageUri = bundle.getParcelable("imageUri");
            Log.d("ImageURI", imageUri.getPath());
            image = loadBitmap(imageUri);
        }
        canvas = (TouchImageView)findViewById(R.id.canvas);
        canvas.setOnImageChangedListener(new TouchImageView.OnImageChangedListener() {
            @Override
            public void imageChanged(TouchImageView view) {
                process(view);
            }
        });
        canvas.setImageBitmap(image);

        db = new DbApi(getApplicationContext());

    }

    public Bitmap getResizedBitmap(Bitmap realImage) {
        float ratio = Math.min(
                (float) MAX_BITMAP_SIZE / realImage.getWidth(),
                (float) MAX_BITMAP_SIZE / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());
        Log.d("WH", WUtils.toString(new Point(width, height)));

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, true);
        return newBitmap;
    }

    public Bitmap loadBitmap(Uri fileUri){
        Bitmap ImageCaptured;
        InputStream imageStream = null;
        try {

            imageStream = getContentResolver().openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ImageCaptured = BitmapFactory.decodeStream(imageStream);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(fileUri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        ImageCaptured = rotateBitmap(ImageCaptured, orientation);
        if(ImageCaptured.getWidth() > MAX_BITMAP_SIZE || ImageCaptured.getHeight() > MAX_BITMAP_SIZE){
            ImageCaptured = getResizedBitmap(ImageCaptured);
        }
        return ImageCaptured;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onResume(){
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void process(View v){
        ArrayList<Point> path = canvas.getSwipePathImage();
       (new ImageProcessTask(image, path)).execute();

    }

    public byte[] getImageBinary(Bitmap img) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        return imageBytes;
    }

    public void launchDetails(Uri furi){
        Intent intent = new Intent(this, InfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("resultImageUri", furi);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private class ImageProcessTask extends AsyncTask<Void, Integer, Bitmap>{
        Bitmap query;
        ArrayList<Point> query_path;
        ProgressDialog progressDialog;


        public ImageProcessTask(Bitmap _query, ArrayList<Point> _query_path){
            query = _query;
            query_path = _query_path;
        }

        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CanvasActivity.this);
            progressDialog.setMessage("Processing");
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... params){
            return WImgProc.process(query, query_path);
        }
        @Override
        protected void onPostExecute(Bitmap bmp){
            croppedImage = bmp;
            ImageHandle imageHandle = new ImageHandle();

            File subImg = imageHandle.newWordImage();

            try {
                FileOutputStream fout = new FileOutputStream(subImg);
                fout.write(getImageBinary(croppedImage));
                Uri furi = Uri.fromFile(subImg);
                //db.add_entry(imageUri.getPath(), Uri.fromFile(subImg).getPath());
                launchDetails(furi);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }
        }



    }
}
