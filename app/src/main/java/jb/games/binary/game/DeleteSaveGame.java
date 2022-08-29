package jb.games.binary.game;

import android.content.Context;

import jb.games.binary.BinaryApp;

class DeleteSaveGame implements Runnable {

    @Override
    public void run() {
       Data lData;
       Context lContext;

       lContext = BinaryApp.getApplContext();
       lData = Data.getInstance(lContext);
       synchronized(lContext){
           lData.xDeleteSave();
       }
    }
}
