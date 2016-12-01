package in.ac.iiit.cvit.wordhelp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends AppCompatActivity {
    ImageHandle imageHandle;
    Uri imageUri;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        imageHandle = new ImageHandle();

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
        startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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
                //uploadImage(croppedImage);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("imageUri", imageUri);
                Intent touchImageview = new Intent(this, MainActivity.class);
                touchImageview.putExtras(bundle);
                startActivity(touchImageview);

            }
        }
    }
}
