package in.ac.iiit.cvit.wordhelp;

import java.util.ArrayList;

/**
 * Created by jerin on 8/12/16.
 */

public class InfoEntry {
    private String word;
    private ArrayList<String> meanings;

    public InfoEntry(String _word, ArrayList<String> _meanings){
        word = _word;
        meanings = _meanings;
    }

    public String prettyWord(){
        return word;
    }

    public String prettyMeaning(){
        String outstring = "";
        for(String meaning: meanings){
            outstring += meaning + "\n";
        }
        return outstring;
    }
}
