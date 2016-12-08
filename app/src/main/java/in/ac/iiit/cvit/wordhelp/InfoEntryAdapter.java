package in.ac.iiit.cvit.wordhelp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

/**
 * Created by jerin on 2/12/16.
 */
public class InfoEntryAdapter extends
        RecyclerView.Adapter<InfoEntryAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row

        public TextView word;
        public TextView meaning;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            word = (TextView)itemView.findViewById(R.id.word);
            meaning = (TextView)itemView.findViewById(R.id.meaning);
        }
    }



    private List<InfoEntry> mEntries;
    private Context mContext;

    public InfoEntryAdapter(Context context, List<InfoEntry> entries){
        mEntries = entries;
        mContext = context;
    }

    private Context getContext(){
        return mContext;
    }

    @Override
    public InfoEntryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View infoEntryView = inflater.inflate(R.layout.layout_word_entry, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(infoEntryView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(InfoEntryAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        InfoEntry entry = mEntries.get(position);

        // Set item views based on your views and data model
        TextView word = viewHolder.word;
        TextView meaning = viewHolder.meaning;

        word.setText(entry.prettyWord());
        meaning.setText(entry.prettyMeaning());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mEntries.size();
    }



}
