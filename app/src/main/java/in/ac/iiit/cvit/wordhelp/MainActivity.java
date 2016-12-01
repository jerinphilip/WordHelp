package in.ac.iiit.cvit.wordhelp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ImageHandle imageHandle;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static int MAX_BITMAP_SIZE = 1280;
    private Uri imageUri;
    Bitmap image;
    Bitmap croppedImage;
    TouchImageView canvas;

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
        setContentView(R.layout.activity_main);

        Bundle bundle = this.getIntent().getExtras();
        if(bundle!= null && bundle.containsKey("imageUri")) {
            Uri imageUri = bundle.getParcelable("imageUri");
            image = loadBitmap(imageUri);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imageUri.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            image = rotateBitmap(image, orientation);
            if(image.getWidth() > MAX_BITMAP_SIZE || image.getHeight() > MAX_BITMAP_SIZE){
                image = getResizedBitmap(image);
            }
        }
        else {
            image = ((BitmapDrawable) getDrawable(R.drawable.lorem)).getBitmap();
        }
        canvas = (TouchImageView)findViewById(R.id.canvas);
        canvas.setImageBitmap(image);
        /*
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        */

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

    private void takeCameraPhoto(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = imageHandle.getOutputMediaFileUri(imageHandle.MEDIA_TYPE_IMAGE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates the menu from res/menu/toolbar.xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_capture:
                takeCameraPhoto();
                return true;
            case R.id.action_crop:
                //process();
                return true;
            case R.id.action_confirm:
                uploadImage(croppedImage);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void process(View v){
        ArrayList<Point> path = canvas.getSwipePathImage();
        Bitmap result = WImgProc.process(image, path);
        croppedImage = result;
        //canvas.resetCanvas();
        //
        // canvas.setImageBitmap(image);

        /*
        ImageView alter = (ImageView)findViewById(R.id.second);
        alter.setImageBitmap(result);
        */
        uploadImage(croppedImage);
    }

    public void launchDetails(String output){
        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra("output", output);
        Bundle bundle = new Bundle();
        bundle.putParcelable("resultImage", croppedImage);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    public void uploadImage(Bitmap img) {
        /* TODO: Make this neater? */
        String serverUrl = "http://ocr.iiit.ac.in/wordhelp/api.php";
        final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

        OkHttpClient client = new OkHttpClient();


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("lang", "Hindi")
                .addFormDataPart("uploaded_file", "bitmap.jpg",
                        RequestBody.create(MEDIA_TYPE_JPG, getImageBinary(img))
                ).build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                /*Toast.makeText(MainActivity.this, "Aaaaand you failed", Toast.LENGTH_LONG).show();*/
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Response resp = response;
                final String output = resp.body().string();
                //Log.v(TAG, resp);
                if (response.isSuccessful()) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String s = resp.code() + " (" + resp.message() + ")";
                            Toast.makeText(MainActivity.this, "Success!!!", Toast.LENGTH_LONG).show();
                            Log.d("response", output);
                            launchDetails(output);

                        }
                    });

                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Failure!!!", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        });
    }



    public byte[] getImageBinary(Bitmap img) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        return imageBytes;

    }

}
