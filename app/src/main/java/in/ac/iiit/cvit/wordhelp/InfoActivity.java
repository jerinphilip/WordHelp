package in.ac.iiit.cvit.wordhelp;

import android.graphics.Bitmap;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("resultImage")){
            ImageView imgView = (ImageView)findViewById(R.id.image);
            Bitmap resultImage = bundle.getParcelable("resultImage");
            imgView.setImageBitmap(resultImage);
            uploadImage(resultImage);
        }
    }

    public void updateTextArea(String output){
        TextView textView = (TextView)findViewById(R.id.output);
        textView.setText(output);
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
                /*Toast.makeText(CanvasActivity.this, "Aaaaand you failed", Toast.LENGTH_LONG).show();*/
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
                            //Toast.makeText(InfoActivity.this, "Success!!!", Toast.LENGTH_LONG).show();
                            Log.d("response", output);
                            updateTextArea(output);

                        }
                    });

                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(InfoActivity.this, "Failure!!!", Toast.LENGTH_LONG).show();
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
