package in.ac.iiit.cvit.wordhelp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    ImageHandle imageHandle;
    Uri imageUri;
    private static final int CAPTURE_IMAGE = 100;
    private static final int PICK_IMAGE = 101;
    ArrayList<File> historyFiles;

    private static final int REQUEST_WRITE = 102;
    private static final int REQUEST_CAMERA = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeCameraPhoto();
            }
        });

        askPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE);
        askPermissions(Manifest.permission.CAMERA, REQUEST_CAMERA);

        imageHandle = new ImageHandle();
        historyFiles = imageHandle.getImageList();
        RecyclerView history = (RecyclerView)findViewById(R.id.history);
        HistoryAdapter historyAdapter = new HistoryAdapter(this, historyFiles);
        history.setAdapter(historyAdapter);
        history.setLayoutManager(new LinearLayoutManager(this));

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    private void askPermissions(String permission, int code){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        code);

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        code);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    ;
                }
                return;
            }
            case REQUEST_WRITE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    ;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void takeCameraPhoto(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = Uri.fromFile(imageHandle.newImage());
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAPTURE_IMAGE);
    }

    public void pickGalleryImage(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_gallery:
                pickGalleryImage();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("imageUri", imageUri);
                Intent touchImageview = new Intent(this, CanvasActivity.class);
                touchImageview.putExtras(bundle);
                startActivity(touchImageview);
            }
        }

        if (requestCode == PICK_IMAGE){
            if (resultCode == RESULT_OK){
                Bundle bundle = new Bundle();
                imageUri = data.getData();
                bundle.putParcelable("imageUri", imageUri);
                Intent touchImageview = new Intent(this, CanvasActivity.class);
                touchImageview.putExtras(bundle);
                startActivity(touchImageview);
            }
        }
    }

}
