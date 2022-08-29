package jb.games.binary.game;

import android.content.Context;

import jb.games.binary.BinaryApp;

class DeletePlayfield implements Runnable {
    private final int mFieldId;

    DeletePlayfield(int pFieldId){
        mFieldId = pFieldId;
    }

    @Override
    public void run() {
        Data lData;
        Context lContext;

        lContext = BinaryApp.getApplContext();
        lData = Data.getInstance(lContext);
        synchronized(lContext){
            lData.xDeletePlayField(mFieldId);
        }
    }
}
