package in.ac.iiit.cvit.wordhelp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ImageHandle imageHandle;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri imageUri;
    Bitmap image;
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

        image = ((BitmapDrawable) getDrawable(R.drawable.lorem)).getBitmap();
        canvas = (TouchImageView)findViewById(R.id.canvas);
        canvas.setImageBitmap(image);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
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
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_capture:
                takeCameraPhoto();
                return true;
            case R.id.action_crop:
                process();
                return true;
            case R.id.action_confirm:
                //sendImage();
                uploadImage(image);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void process(){
        ArrayList<Point> path = canvas.getSwipePathImage();
        //WUtils.debugPoints(path);
        Bitmap result = WImgProc.process(image, path);
        image = result;
        canvas.setImageBitmap(result);
        canvas.resetCanvas();


        ImageView alter = (ImageView)findViewById(R.id.second);
        alter.setImageBitmap(result);
    }


    public void uploadImage(Bitmap img) {
        /* TODO: Make this neater? */
        String serverUrl = "http://preon.iiit.ac.in/~jerin/projects/wordhelp";
        final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

        OkHttpClient client = new OkHttpClient();
        /*
        RequestBody formBody = new FormBody.Builder()
                .add("email", "Jurassic@Park.com")
                .add("tel", "90301171XX")
                .build(); */

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("test", "test_value")
                .addFormDataPart("bitmap", "bitmap.jpg",
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
