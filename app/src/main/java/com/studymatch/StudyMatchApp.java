package com.studymatch;

import android.app.Application;

import com.studymatch.data.Repository;

public class StudyMatchApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Repository.init(this);
    }
}
