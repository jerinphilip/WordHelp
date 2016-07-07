package io.github.jerinphilip.WordHelp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private ImageHandle iH = new ImageHandle();
    private ProgressDialog progress;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private Bitmap ImageCaptured;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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
                takePhoto();
                return true;
            case R.id.action_crop:
                cropRect();
                return true;
            case R.id.action_confirm:
                //sendImage();
                uploadImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = iH.getOutputMediaFileUri(iH.MEDIA_TYPE_IMAGE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void onActivityResult (int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            loadImage();
            setCanvas();
        }
    }

    public void loadImage(){
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ImageCaptured = BitmapFactory.decodeStream(imageStream);
        if(ImageCaptured.getWidth() > ImageCaptured.getHeight()){
            Matrix m = new Matrix();
            m.postRotate(90);
            ImageCaptured = Bitmap.createBitmap(ImageCaptured, 0, 0,
                    ImageCaptured.getWidth(), ImageCaptured.getHeight(), m, true);
        }
        Log.d("Image", "Captured");
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("fileUri", fileUri);
    }

    // Recover the saved state when the activity is recreated.
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("fileUri");
        loadImage();
        setCanvas();
    }

    public void setCanvas(){
        TouchImageView img = (TouchImageView) findViewById(R.id.canvas);
        Log.d("ImageDisplay", "Attempting Display");
        img.setImageBitmap(ImageCaptured);
        //img.setImageResource(R.drawable.cannon);
        Log.d("ImageDisplay", "Done Display");
        img.setMaxZoom(4f);
    }

    public void cropRect() {
        TouchImageView img = (TouchImageView) findViewById(R.id.canvas);
        int p[] = img.getCropDimensions();
        String out = "";
        for(int i=0; i<p.length; i++)
            out += String.valueOf(p[i]+" ");
        Log.d("Dimensions", out);
        Bitmap rBmp = Bitmap.createBitmap(ImageCaptured, p[0], p[1], p[2], p[3]);
        img.reset();
        img.setImageBitmap(rBmp);
        ImageCaptured = rBmp;
    }

    public void uploadImage() {

        String serverUrl = "http://preon.iiit.ac.in/~heritage/anstuff/";
        final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");

        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("email", "Jurassic@Park.com")
                .add("tel", "90301171XX")
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("test", "test_value")
                .addFormDataPart("bitmap", "bitmap.jpg",
                        RequestBody.create(MEDIA_TYPE_JPG, getImageBinary())
                ).build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "Aaaaand you failed", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getApplicationContext(), "Failure!!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    public byte[] getImageBinary() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageCaptured.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] imageBytes = baos.toByteArray();
        return imageBytes;

    }

}

