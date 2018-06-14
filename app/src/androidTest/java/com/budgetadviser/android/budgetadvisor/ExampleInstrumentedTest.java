package com.budgetadviser.android.budgetadvisor;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Instrumented set_project, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under set_project.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.budgetadviser.android.budgetadvisor", appContext.getPackageName());
    }

}
