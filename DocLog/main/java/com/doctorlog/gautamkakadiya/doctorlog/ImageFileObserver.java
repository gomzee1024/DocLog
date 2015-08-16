package com.doctorlog.gautamkakadiya.doctorlog;

import android.os.FileObserver;

/**
 * Created by Gautam Kakadiya on 09-Jul-15.
 */
public class ImageFileObserver extends FileObserver{

    public String absolutepath;
    MainActivity m1;
    public ImageFileObserver(String path) {
        super(path,FileObserver.DELETE_SELF);
        absolutepath=path;
    }

    @Override
    public void onEvent(int i, String s) {

        if(absolutepath==null){
            return;
        }
        if((FileObserver.DELETE_SELF & i)!=0)
        {
            m1.deleteFromDB(absolutepath);
        }
    }
}
