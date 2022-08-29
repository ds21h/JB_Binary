package jb.games.binary.game;

import android.content.Context;

import jb.games.binary.BinaryApp;

class SaveGame implements Runnable {
    private final BinaryGame mGame;

    SaveGame(BinaryGame pGame){
        mGame = new BinaryGame(pGame);
    }

    @Override
    public void run() {
       Data lData;
       Context lContext;

       lContext = BinaryApp.getApplContext();
       lData = Data.getInstance(lContext);
       synchronized(lContext){
           lData.xSaveGame(mGame);
       }
    }
}
