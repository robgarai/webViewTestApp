package com.example.rober.webviewtestapp.tools;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;

public class FileManager extends AppCompatActivity {

    public static String readStringFromResource(Context ctx, int resourceID) {
        StringBuilder stringBuilder = new StringBuilder();
        //String sep = System.getProperty("line.separator");

        try {
            final InputStream inputStream = ctx.getResources().openRawResource(resourceID);
            final InputStreamReader inputStreamReader = new InputStreamReader (inputStream) ;
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader, 1024*8);

            try {
                String line = null;
                while (( line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line);
                    //stringBuilder.append(sep);
                }
            }
            finally {
                bufferedReader.close();
            }
            inputStream.close();
        }
        catch (FileNotFoundException ex) {
            Log.e(TAG, "Couldn't find the file " + resourceID  + " " + ex);
            return null;
        }
        catch (IOException ex){
            Log.e(TAG, "Error reading file " + resourceID + " " + ex);
            return null;
        }
        return stringBuilder.toString();
    }
}
