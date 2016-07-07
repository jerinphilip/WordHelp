package io.github.jerinphilip.WordHelp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
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

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private ImageHandle iH = new ImageHandle();
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
}