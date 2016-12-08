package in.ac.iiit.cvit.wordhelp;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jerin on 7/12/16.
 */

public class Dictionary {
    Map <String, ArrayList<String>> D;
    public Dictionary(InputStream is){
        Type type = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
        String result = loadContents(is);
        D = new Gson().fromJson(result, type);
    }

    private String loadContents(InputStream is){
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        String jsonString = writer.toString();
        return jsonString;
    }

    public ArrayList<String> getMeaning(String word){
        if(D.containsKey(word)){
            return D.get(word);
        }
        return new ArrayList<String>();
    }


}
