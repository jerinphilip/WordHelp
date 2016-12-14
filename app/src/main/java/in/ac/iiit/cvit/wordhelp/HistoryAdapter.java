package in.ac.iiit.cvit.wordhelp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by jerin on 2/12/16.
 */
public class HistoryAdapter extends
        RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        public ImageView imageView;
        public TextView textView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_thumbnail);
            textView  = (TextView) itemView.findViewById(R.id.filename);
        }
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageView;
        public TextView textView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_thumbnail);
            textView  = (TextView) itemView.findViewById(R.id.filename);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            Bundle bundle = new Bundle();
            int position = getAdapterPosition();
            History history = mHistory.get(position);
            Log.d("HistoryAdapter", history.getName());
            bundle.putParcelable("imageUri", history.getUri());
            Intent intent = new Intent(context, CanvasActivity.class);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }



        private List<History> mHistory;
        private Context mContext;

        public HistoryAdapter(Context context, List<History> history){
            mHistory = history;
            mContext = context;
        }

        private Context getContext(){
            return mContext;
        }

        @Override
        public HistoryAdapter.HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View historyView = inflater.inflate(R.layout.layout_history_row, parent, false);

            HistoryViewHolder viewHolder = new HistoryViewHolder(historyView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(HistoryAdapter.HistoryViewHolder viewHolder, int position) {
            // Get the data model based on position
            History history = mHistory.get(position);

            // Set item views based on your views and data model

            ImageView imageView = viewHolder.imageView;
            TextView textView = viewHolder.textView;

            Bitmap thumbnail = history.getThumbNail();
            imageView.setImageBitmap(thumbnail);
            textView.setText(history.getName());


        }



        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return mHistory.size();
        }



}
