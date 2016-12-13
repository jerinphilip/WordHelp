package in.ac.iiit.cvit.wordhelp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoActivity extends AppCompatActivity {
    private static int MAX_BITMAP_SIZE = 1280;
    private DbApi db;
    Dictionary D;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        InputStream is = getResources().openRawResource(R.raw.dictionary);
        D = new Dictionary(is);
        db = new DbApi(getApplicationContext());
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("resultImageUri")){
            Uri resultImageUri = bundle.getParcelable("resultImageUri");
            Bitmap resultImage = loadBitmap(resultImageUri);
            deleteImage(resultImageUri);
            uploadImage(resultImage);
            //ImageView preview = (ImageView)findViewById(R.id.preview);
            //preview.setImageBitmap(resultImage);
        }


    }

    public void deleteImage(Uri uri){
        File fdelete = new File(uri.getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                //System.out.println("file Deleted :" + uri.getPath());
            } else {
                //System.out.println("file not Deleted :" + uri.getPath());
            }
        }
    }

    public void updateContent(String output){
        String[] words = output.split(" ");
        ArrayList<InfoEntry> entries = new ArrayList<>();
        for(String word: words){
            word = word.replace(" ", "");
            if(word != null && !word.isEmpty()) {
                ArrayList<String> meanings = D.getMeaning(word);
                if(!meanings.isEmpty()) {
                    InfoEntry infoEntry = new InfoEntry(word, meanings);
                    entries.add(infoEntry);
                }
            }
        }

        RecyclerView rvInfoEntry = (RecyclerView) findViewById(R.id.rv_info_entry);

        // Initialize contacts

        // Create adapter passing in the sample user data
        InfoEntryAdapter adapter = new InfoEntryAdapter(this, entries);
        // Attach the adapter to the recyclerview to populate items
        rvInfoEntry.setAdapter(adapter);
        // Set layout manager to position the items
        rvInfoEntry.setLayoutManager(new LinearLayoutManager(this));

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
        final ProgressDialog progressDialog = new ProgressDialog(InfoActivity.this);
        progressDialog.setMessage("Sending to server");
        progressDialog.show();

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
            }



            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Response resp = response;
                final String output = resp.body().string();
                progressDialog.dismiss();
                if (response.isSuccessful()) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("response", output);
                            updateContent(output);

                        }
                    });

                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
}
