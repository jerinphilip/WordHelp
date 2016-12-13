package in.ac.iiit.cvit.wordhelp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by jerin on 2/12/16.
 */
public class HistoryAdapter extends
        RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        public ImageView imageView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_thumbnail);
        }
    }



        private List<File> mFiles;
        private Context mContext;

        public HistoryAdapter(Context context, List<File> files){
            mFiles = files;
            mContext = context;
        }

        private Context getContext(){
            return mContext;
        }

        @Override
        public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View historyView = inflater.inflate(R.layout.layout_history_row, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(historyView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(HistoryAdapter.ViewHolder viewHolder, int position) {
            // Get the data model based on position
            File file = mFiles.get(position);

            // Set item views based on your views and data model

            ImageView imageView = viewHolder.imageView;
            // Load image into bitmap from URI.
            InputStream imageStream;
            try {
                imageStream = new FileInputStream(file);
                Bitmap image = BitmapFactory.decodeStream(imageStream);
                Bitmap thumbnail = generateThumbnail(image);
                imageView.setImageBitmap(thumbnail);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return mFiles.size();
        }

        public Bitmap generateThumbnail(Bitmap image){
            int THUMBSIZE = 128;
            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(image,
                    THUMBSIZE, THUMBSIZE);
            return thumbnail;
        }

    }
