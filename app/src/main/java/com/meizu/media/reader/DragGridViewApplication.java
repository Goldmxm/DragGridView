package com.meizu.media.reader;

import android.app.Application;

import com.orhanobut.logger.Logger;

/**
 * Created by maxueming on 17-9-26.
 */

public class DragGridViewApplication extends Application{
    private static final String TAG = "DragGridView";
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init(TAG);
    }
}
