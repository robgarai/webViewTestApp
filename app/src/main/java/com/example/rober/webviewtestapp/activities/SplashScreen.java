package com.example.rober.webviewtestapp.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.gpstracker.unicornsystems.eu.gpstracker.MyApplication;
import com.gpstracker.unicornsystems.eu.gpstracker.R;
import com.gpstracker.unicornsystems.eu.gpstracker.SHA256Hash;
import com.gpstracker.unicornsystems.eu.gpstracker.data_clases.LatLngDatabase;
import com.gpstracker.unicornsystems.eu.gpstracker.data_clases.RunContentProvider;

import static com.google.android.gms.wearable.DataMap.TAG;

//todo musel som vyhodit test fairy bo bez pripojenia PC na internet mi tato metoda vyhadzovala error
//import com.testfairy.TestFairy;

/**
 * Created by RGarai on 18.8.2016.
 */

/*
 * show Unicorn Systems logo then fades out to login screen
 */

public class SplashScreen extends Activity {

    private String mUsername;
    private String mPassword;
    private String myHash;



    //firebase storage variables
    private StorageReference spaceRef;
    private String mySringOfLatLongFromFirebaseJson;

    //firebase database variables
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;

    //google analytics
    public Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        TestFairy.begin(this, "14f9d2f78928cf7b85e08b0c5a3d8467b27cd3f5"); // e.g "0000111122223333444455566667777788889999";
        setContentView(R.layout.activity_splash);
        Log.i("MyMessageOnCreate", "now the app started and splash screen shows itself for 3 seconds");

        // google analytics
        // Obtain the shared Tracker instance. google analytics
        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
//        mTracker.setAppId("UA-85273203-1");
//        mTracker.set("&uid", "UA-85273203-1");

        //firebase implementation
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();


        //from previous storage imoplementation of firebase deprecated
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReferenceFromUrl("gs://gpstracker-646ed.appspot.com");
//        spaceRef = storageRef.child("myDataBaseOfRuns.txt");



        //check if permalogin was enabled and user is alredy logged in - shared preferences contains username and password
        SharedPreferences prefs = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if ((prefs.contains("username")) && (prefs.contains("pass"))) {
            //if permalogin is enabled then log into dashboard if not then show login screen
            // get data from shared preferences username and pass - neccesary for hash
            mUsername = prefs.getString("username", "");
            mPassword = prefs.getString("pass", "");
            myHash = SHA256Hash.sha256(mUsername + mPassword);

            //check for internet connection
            if (isOnline()) {
                retrieveHashDataFromDB();
            } else {
                //if no internet connection then show snackbar and inform the user that database is not synchronized.
                Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.internet_connection_failed), Snackbar.LENGTH_INDEFINITE);
                View snackbarView = snackbar.getView();
                TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                snackbarTextView.setTextColor(Color.RED);
                // Changing snackbar text size
                snackbarTextView.setTextSize(20f);
                // Changing snackbar button-text color
                snackbar.setActionTextColor(Color.RED);

                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar snackbar1 = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.data_not_synchronyzed), Snackbar.LENGTH_SHORT);
                        View snackbarView = snackbar1.getView();
                        TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                        snackbarTextView.setTextColor(ContextCompat.getColor(SplashScreen.this, R.color.colorAccent));
                        // Changing snackbar text size
                        snackbarTextView.setTextSize(20f);
                        snackbar1.show();

                        //start dashboard
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startDashboardActivity();                                }
                        }, 2000);


                    }
                });

                snackbar.show();
            }

        } else {
            //if permalogin not activated or if shared preferences not contains username and password
            //start login
            startLoginActivity();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //google analytics
        Log.i(TAG, "Setting screen name: " + "SplashScreen");
        mTracker.setScreenName("Image~" + "SplashScreen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void startDashboardActivity() {

        //start dashboard activity
        Intent i = new Intent(SplashScreen.this, DashboardActivity.class);
        startActivity(i);
        //animation transition between activities
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        // ...
    }

    public void startLoginActivity() {

        //start dashboard activity
        Intent i = new Intent(SplashScreen.this, LoginActivity.class);
        startActivity(i);
        //animation transition between activities
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        // ...
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    public void retrieveHashDataFromDB() {
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference();
        // retrieve goal value from firebase database in km (double)
        myRef.child(mUsername).child(myHash).addListenerForSingleValueEvent(

                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user values
                        // get validation secreat value and decide if user can continue
                        String validationString = dataSnapshot.child("boolean").getValue(String.class);
                        if ((validationString != null) && (validationString.equals("jou:4d5"))) {
                            //pokracuj v ziskavani dat
                            Log.i("validation", "validation succeded");

                            //synchronyzing the database
                            synchronizeFirebaseDB();

                            Log.i("MyMessageOnLoginSucess", "now the app shows the login screen and waits until button is pressed");
                        } else {
                            //vyhod hlasku ze je nieco zle
                            Log.i("validation", "validation error");

                            Snackbar snackbar1 = Snackbar.make(findViewById(R.id.coordinatorLayout), getString(R.string.login_error), Snackbar.LENGTH_SHORT);
                            View snackbarView = snackbar1.getView();
                            TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                            snackbarTextView.setTextColor(ContextCompat.getColor(SplashScreen.this, R.color.colorAccent));
                            // Changing snackbar text size
                            snackbarTextView.setTextSize(20f);
                            snackbar1.show();

                            //start dashboard
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startLoginActivity();                                }
                            }, 2000);
                        }


                    }

                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                }
        );

    }

    public void synchronizeFirebaseDB() {
        // retrieve connection value from firebase database in km (double)
        myRef.child(mUsername).child("Goal").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user values

                        float goal = dataSnapshot.getValue(Float.class);
                        SharedPreferences.Editor editor = getSharedPreferences(MyApplication.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
                        editor.putFloat("goal", goal).apply();
                        //when retrieveng the connection data is finishet then start retrievieng the rest of the data
                        retrieveRunsDataFromDB();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("connectionProblem", "getUser:onCancelled", databaseError.toException());
                        // ...
                    }
                }
        );
    }

    public void retrieveRunsDataFromDB() {
        // retrieve all previous runs values from firebase database
        /*
         * get other data exactly:
         * >runs
         *     >>0
         *     >>1
         *     >>2
         *       >>>id: long
         *       >>>date: long
         *       >>>length: double
         *       >>>time: long
         *       >>>LatLongData: JSON
         */

        //first delete local database on phone drive
        getContentResolver().delete(RunContentProvider.CONTENT_URI, null, null);

        myRef.child(mUsername).child("runs").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user values
                        // get goal walue in km (double)
                        for (DataSnapshot run : dataSnapshot.getChildren()) {

                            Long id = run.child("ID").getValue(Long.class);
                            Long date = run.child("DATE").getValue(Long.class);      //same as Long date = (Long) run.child("DATE").getValue();
                            Double length = run.child("LENGTH").getValue(Double.class);
                            Long time = run.child("TIME").getValue(Long.class);
                            String myJsonSringOfLatLong= run.child("LATLONGDATA").getValue(String.class);

                            //inserting into local database from firebase database
                            ContentValues runValues = new ContentValues();
                            runValues.put(LatLngDatabase.COLUMN_LATLONG, myJsonSringOfLatLong);
                            runValues.put(LatLngDatabase.COLUMN_ID, id);
                            runValues.put(LatLngDatabase.COLUMN_DATE, date);
                            runValues.put(LatLngDatabase.COLUMN_LENGTH, length);
                            runValues.put(LatLngDatabase.COLUMN_TIME, time);

                            getContentResolver().insert(RunContentProvider.CONTENT_URI, runValues);

                        }

                        //starting the dashboard activity
                        startDashboardActivity();
                    }

                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // ...
                    }
                });

    }


//    public void retrieveGoalDataFromDB() {
//
//        // retrieve connection value from firebase database in km (double)
//        myRef.child("connection").addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // Get user values
//
//                        int goal = dataSnapshot.getValue(Integer.class);
//                        SharedPreferences.Editor editor = getSharedPreferences("setting_preferences", MODE_PRIVATE).edit();
//                        editor.putFloat("goal", (float) goal).apply();
//                        //when retrieveng the connection data is finishet then start retrievieng the rest of the data
//                        retrieveRunsDataFromDB();
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w("connectionProblem", "getUser:onCancelled", databaseError.toException());
//                        // ...
//                    }
//                }
//        );
//
//
//    }
//
//    public void retrieveRunsDataFromDB() {
//        // retrieve all previous runs values from firebase database
//        /*
//         * get other data exactly:
//         * >runs
//         *     >>0
//         *     >>1
//         *     >>2
//         *       >>>id: long
//         *       >>>date: long
//         *       >>>length: double
//         *       >>>time: long
//         *       >>>LatLongData: JSON
//         */
//
//        //first delete local database on phone drive
//        getContentResolver().delete(RunContentProvider.CONTENT_URI, null, null);
//
//        myRef.child("runs").addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // Get user values
//                        // get goal walue in km (double)
//                        for (DataSnapshot run : dataSnapshot.getChildren()) {
//
//                            Long id = run.child("ID").getValue(Long.class);
//                            Long date = run.child("DATE").getValue(Long.class);      //same as Long date = (Long) run.child("DATE").getValue();
//                            Double length = run.child("LENGTH").getValue(Double.class);
//                            Long time = run.child("TIME").getValue(Long.class);
//                            String myJsonSringOfLatLong= run.child("LATLONGDATA").getValue(String.class);
//
//                            //inserting into local database from firebase database
//                            ContentValues runValues = new ContentValues();
//                            runValues.put(LatLngDatabase.COLUMN_LATLONG, myJsonSringOfLatLong);
//                            runValues.put(LatLngDatabase.COLUMN_ID, id);
//                            runValues.put(LatLngDatabase.COLUMN_DATE, date);
//                            runValues.put(LatLngDatabase.COLUMN_LENGTH, length);
//                            runValues.put(LatLngDatabase.COLUMN_TIME, time);
//
//                            getContentResolver().insert(RunContentProvider.CONTENT_URI, runValues);
//
//                        }
//
//                        //starting the dashboard activity
//                        startDashboardActivity();
//                    }
//
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
//                        // ...
//                    }
//                });
//
//    }
//
//
//    public void synchronizeFirebaseDB() {
//        // retrieve connection value from firebase database in km (double)
//        myRef.child(mUsername).child("Goal").addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // Get user values
//
//                        int goal = dataSnapshot.getValue(Integer.class);
//                        SharedPreferences.Editor editor = getSharedPreferences("settings_preferences", MODE_PRIVATE).edit();
//                        editor.putFloat("goal", (float) goal).apply();
//                        //when retrieveng the connection data is finishet then start retrievieng the rest of the data
//                        retrieveRunsDataFromDB();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w("connectionProblem", "getUser:onCancelled", databaseError.toException());
//                        // ...
//                    }
//                }
//        );
//    }
//
//    public void retrieveRunsDataFromDB() {
//        // retrieve all previous runs values from firebase database
//        /*
//         * get other data exactly:
//         * >runs
//         *     >>0
//         *     >>1
//         *     >>2
//         *       >>>id: long
//         *       >>>date: long
//         *       >>>length: double
//         *       >>>time: long
//         *       >>>LatLongData: JSON
//         */
//
//        //first delete local database on phone drive
//        getContentResolver().delete(RunContentProvider.CONTENT_URI, null, null);
//
//        myRef.child(mUsername).child("runs").addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // Get user values
//                        // get goal walue in km (double)
//                        for (DataSnapshot run : dataSnapshot.getChildren()) {
//
//                            Long id = run.child("ID").getValue(Long.class);
//                            Long date = run.child("DATE").getValue(Long.class);      //same as Long date = (Long) run.child("DATE").getValue();
//                            Double length = run.child("LENGTH").getValue(Double.class);
//                            Long time = run.child("TIME").getValue(Long.class);
//                            String myJsonSringOfLatLong= run.child("LATLONGDATA").getValue(String.class);
//
//                            //inserting into local database from firebase database
//                            ContentValues runValues = new ContentValues();
//                            runValues.put(LatLngDatabase.COLUMN_LATLONG, myJsonSringOfLatLong);
//                            runValues.put(LatLngDatabase.COLUMN_ID, id);
//                            runValues.put(LatLngDatabase.COLUMN_DATE, date);
//                            runValues.put(LatLngDatabase.COLUMN_LENGTH, length);
//                            runValues.put(LatLngDatabase.COLUMN_TIME, time);
//
//                            getContentResolver().insert(RunContentProvider.CONTENT_URI, runValues);
//
//                        }
//
//                        //starting the dashboard activity
//                        startDashboardActivity();
//                    }
//
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
//                        // ...
//                    }
//                });
//
//    }


//
////THIS METHOD WAS USED FOR STORING THE DATA TO FIREBASE STORAGE - deprecated
//    public void downloadFile(){
//
//        final long ONE_MEGABYTE = 1024 * 1024;
//        spaceRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                // convert byte to string
//                mySringOfLatLongFromFirebaseJson = "";
//                for (int i = 0; i < bytes.length; i++) {
//                    mySringOfLatLongFromFirebaseJson += (char) bytes[i];
//                }
//
//                Log.i("myFirebaseString",mySringOfLatLongFromFirebaseJson);
//
//                getContentResolver().delete(RunContentProvider.CONTENT_URI, null, null);
//
//                Gson myGson = new Gson();
//                Type listOfMyRunObjects = new TypeToken<List<MyRun>>(){}.getType();
//                List<MyRun> myFirebaseRuns = myGson.fromJson(mySringOfLatLongFromFirebaseJson, listOfMyRunObjects);
//                for (MyRun run : myFirebaseRuns) {
//                    ContentValues runValues = new ContentValues();
//
//                    List<LatLong> mySimplifiedRoutePoints = run.getOneRunTrackData();
//                    Type listOfMyLatLongObjects = new TypeToken<List<LatLong>>(){}.getType();
//                    String myJsonSringOfLatLong = myGson.toJson(mySimplifiedRoutePoints, listOfMyLatLongObjects);
//
//
//                    runValues.put(LatLngDatabase.COLUMN_LATLONG, myJsonSringOfLatLong);
//                    runValues.put(LatLngDatabase.COLUMN_LENGTH, run.getLength());
//                    runValues.put(LatLngDatabase.COLUMN_DATE, run.getDate().getTime());
//                    runValues.put(LatLngDatabase.COLUMN_TIME, run.getTime());
//
//                    getContentResolver().insert(RunContentProvider.CONTENT_URI, runValues);
//
//                }
//                // Start your app LoginActivity activity
//                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
//                startActivity(i);
//                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle any errors
//            }
//        });
//    }




}

