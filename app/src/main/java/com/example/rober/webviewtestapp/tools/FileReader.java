package com.example.rober.webviewtestapp.tools;

import android.content.Context;
import android.util.Log;

import com.example.rober.webviewtestapp.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;

public class FileReader {

    public String readStringFromResource(Context ctx, int resourceID) {
        StringBuilder contents = new StringBuilder();
        String sep = System.getProperty("line.separator");

        try {
            InputStream is = ctx.getResources().openRawResource(R.raw.earth_shadow_base64);

            BufferedReader input =  new BufferedReader(new InputStreamReader(is), 1024*8);
            try {
                String line = null;
                while (( line = input.readLine()) != null){
                    contents.append(line);
                    contents.append(sep);
                }
            }
            finally {
                input.close();
            }
        }
        catch (FileNotFoundException ex) {
            Log.e(TAG, "Couldn't find the file " + resourceID  + " " + ex);
            return null;
        }
        catch (IOException ex){
            Log.e(TAG, "Error reading file " + resourceID + " " + ex);
            return null;
        }

        return contents.toString();
    }

    public static String getFileContents(final File file) throws IOException {
        final InputStream inputStream = new FileInputStream(file);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        final StringBuilder stringBuilder = new StringBuilder();

        boolean done = false;

        while (!done) {
            final String line = reader.readLine();
            done = (line == null);

            if (line != null) {
                stringBuilder.append(line);
            }
        }

        reader.close();
        inputStream.close();

        return stringBuilder.toString();
    }
}
