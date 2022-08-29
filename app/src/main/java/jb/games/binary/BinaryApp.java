package jb.games.binary;

import android.app.Application;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BinaryApp extends Application {
    private static BinaryApp mBinaryApp;
    public ExecutorService xExecutor;

    public static BinaryApp getInstance() {
        return mBinaryApp;
    }

    public static Context getApplContext(){
        return mBinaryApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBinaryApp = this;
        xExecutor = Executors.newCachedThreadPool();
    }
}
