package in.ac.iiit.cvit.wordhelp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

        imageHandle = new ImageHandle();
        historyFiles = imageHandle.getImageList();
        RecyclerView history = (RecyclerView)findViewById(R.id.history);
        HistoryAdapter historyAdapter = new HistoryAdapter(this, historyFiles);
        history.setAdapter(historyAdapter);
        history.setLayoutManager(new LinearLayoutManager(this));

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates the menu from res/menu/toolbar.xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    public void viewSample(View v){
        Intent touchImageView = new Intent(this, MainActivity.class);
        startActivity(touchImageView);
    }


    private void takeCameraPhoto(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = imageHandle.getOutputMediaFileUri(imageHandle.MEDIA_TYPE_IMAGE);
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
                Intent touchImageview = new Intent(this, MainActivity.class);
                touchImageview.putExtras(bundle);
                startActivity(touchImageview);

            }
        }

        if (requestCode == PICK_IMAGE){
            if (resultCode == RESULT_OK){
                Bundle bundle = new Bundle();
                imageUri = data.getData();
                bundle.putParcelable("imageUri", imageUri);
                Intent touchImageview = new Intent(this, MainActivity.class);
                touchImageview.putExtras(bundle);
                startActivity(touchImageview);
            }
        }
    }

}
