package com.example.rober.webviewtestapp.tools;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by User on 19.8.2016.
 */
public class MyApplication  {
    public static final String SHARED_PREFERENCES_NAME = "settings_preferences";

    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link MyApplication}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);


        /*
         * my fonts all commercial usage allowed
         */
//        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/desyrel.ttf");
//        FontsOverride.setDefaultFont(this, "DEFAULT_BOLD", "fonts/desyrel.ttf");
//        FontsOverride.setDefaultFont(this, "SERIF", "fonts/desyrel.ttf");
//        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/scrgunny.ttf");
//        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/scrgunny.ttf");


        /*
         *  droid serif fonts all commercial usage allowed
         */

        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/robotolight.ttf");
        FontsOverride.setDefaultFont(this, "DEFAULT_BOLD", "fonts/robotobold.ttf");
        FontsOverride.setDefaultFont(this, "SERIF", "fonts/robotobold.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/robotolight.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/robotolightitalic.ttf");






    }

//        Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/domesticmanners.ttf");
//        TextView myTextView =  (TextView) view.findViewById(R.id.date);
//        myTextView.setTypeface(myTypeface);

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);

    }




}
