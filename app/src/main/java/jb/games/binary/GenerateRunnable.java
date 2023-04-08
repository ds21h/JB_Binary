package jb.games.binary;

import android.os.Handler;
import android.os.Message;

import jb.games.binary.game.BinaryGame;
import jb.games.binary.game.BinaryGenerator;

class GenerateRunnable implements Runnable{
    static final int cGenerateEnded = 0;
    static final int cGenerateFinished = 1;

    private final Handler mHandler;
    private final int mRows;
    private final int mColumns;
    private final int mDifficulty;
    private final BinaryGenerator mGenerator;

    GenerateRunnable(Handler pHandler, int pRows, int pColumns, int pDifficulty){
        mHandler = pHandler;
        mRows = pRows;
        mColumns = pColumns;
        mDifficulty = pDifficulty;
        mGenerator = new BinaryGenerator(pRows, pColumns, pDifficulty);
    }

    void xStop(){
        mGenerator.xStop();
    }

    @Override
    public void run() {
        Message lMessage;

        lMessage = mHandler.obtainMessage();
        if (mGenerator.xGenerate()){
            lMessage.obj = mGenerator;
            lMessage.what = cGenerateFinished;
        } else {
            lMessage.what = cGenerateEnded;
        }

        mHandler.sendMessage(lMessage);
    }
}
