package com.example.rober.webviewtestapp.tools;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
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

public class FileManager extends AppCompatActivity {

    public String readStringFromResource(int resourceID) {
        StringBuilder stringBuilder = new StringBuilder();
        //String sep = System.getProperty("line.separator");

        try {
            //InputStream inputStream = ctx.getResources().openRawResource(R.raw.earth_shadow_base64);
            InputStream inputStream = getResources().openRawResource(resourceID);
            InputStreamReader inputStreamReader = new InputStreamReader (inputStream) ;
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader, 1024*8);

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
            //myTextView.setText(stringBuilder.toString());
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
