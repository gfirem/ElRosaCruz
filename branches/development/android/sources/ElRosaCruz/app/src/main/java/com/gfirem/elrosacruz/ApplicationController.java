package com.gfirem.elrosacruz;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by GFireM on 10/14/2015.
 */
@ReportsCrashes(
        formKey = "b856e786e56842b1b8bc0395923d3377",
        formUri = "https://collector.tracepot.com/2c4882bc")
public class ApplicationController extends Application {

    private static ApplicationController fInstance;
    private Tracker mTracker;

    public static ApplicationController getInstance() {
        if (fInstance == null) {
            fInstance = new ApplicationController();
        }
        return fInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            fInstance = this;
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Nexa-Light.ttf")
                    .setFontAttrId(R.attr.fontPath).build());
            ACRA.init(this);
        } catch (Exception anEx) {
            Log.d("AppController" + "onCreate", anEx.toString());
        }
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker("UA-56060946-4");
            mTracker.enableExceptionReporting(true);
            mTracker.enableAdvertisingIdCollection(false);
            mTracker.enableAutoActivityTracking(true);
        }
        return mTracker;
    }
}
