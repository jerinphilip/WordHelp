package in.ac.iiit.cvit.wordhelp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ImageView;
import android.widget.TextView;

public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        String output = getIntent().getStringExtra("output");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("resultImage")){
            ImageView imgView = (ImageView)findViewById(R.id.image);
            Bitmap resultImage = bundle.getParcelable("resultImage");
            imgView.setImageBitmap(resultImage);
        }
        TextView textView = (TextView)findViewById(R.id.output);
        textView.setText(output);
    }

}
