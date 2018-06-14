package com.budgetadviser.android.budgetadvisor;
/**
 * This class is called before onCreate an overrides the standard font
 */

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Abel-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
