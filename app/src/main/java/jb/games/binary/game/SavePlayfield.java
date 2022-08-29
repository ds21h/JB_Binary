package jb.games.binary.game;

import android.content.Context;


import jb.games.binary.BinaryApp;

class SavePlayfield implements Runnable {
    private final PlayField mField;

    SavePlayfield(PlayField pField){
        mField = new PlayField(pField);
    }

    @Override
    public void run() {
        Data lData;
        Context lContext;

        lContext = BinaryApp.getApplContext();
        lData = Data.getInstance(lContext);
        synchronized(lContext){
            lData.xSavePlayField(mField);
        }
    }
}
