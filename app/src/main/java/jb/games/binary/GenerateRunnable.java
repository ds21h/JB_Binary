package jb.games.binary;

import android.os.Handler;

import jb.games.binary.game.BinaryGame;

class GenerateRunnable implements Runnable{
    static final int cGenerateEnded = 0;
    static final int cGenerateFinished = 1;

    private Handler mHandler;
    private BinaryGame mGame;
    private int mRows;
    private int mColumns;
    private int mDifficulty;

    GenerateRunnable(Handler pHandler, BinaryGame pGame, int pRows, int pColumns, int pDifficulty){
        mHandler = pHandler;
        mGame = pGame;
        mRows = pRows;
        mColumns = pColumns;
        mDifficulty = pDifficulty;
    }

    @Override
    public void run() {
        int lResult;

        if (mGame.xGenerate(mRows, mColumns, mDifficulty)){
            lResult = cGenerateFinished;
        } else {
            lResult = cGenerateEnded;
        }

        mHandler.sendEmptyMessage(lResult);
    }
}
