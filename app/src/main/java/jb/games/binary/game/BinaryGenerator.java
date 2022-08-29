package jb.games.binary.game;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

class BinaryGenerator {
    private int mRows;
    private int mColumns;
    private int mNumberCells;
    private int mDifficulty;
    private List<Integer> mFreeCells;
    private ValueCell[] mCells;
    private Random mRandom;
    private volatile boolean mStop;

    BinaryGenerator(int pRows, int pColumns, int pDifficulty){
        mRows = pRows;
        mColumns = pColumns;
        mDifficulty = pDifficulty;
        mNumberCells = mRows * mColumns;
        mFreeCells = new ArrayList<>();
        mCells = new ValueCell[mNumberCells];
        mRandom = new Random();
        mStop = false;
    }

    void xStop(){
        mStop = true;
    }

    ValueCell[] xGetCells(){
        return mCells;
    }

    boolean xGenerate(){
        boolean lResult;

        lResult = false;
        sInit();
        lResult = sMakeBase();
        if (lResult) {
            lResult = sDeleteCells();
            if (lResult) {
                sFinishGame();
            }
        }
        return lResult;
    }

    private void sInit(){
        int lCount;

        mFreeCells.clear();
        for (lCount = 0; lCount < mNumberCells; lCount++){
            mFreeCells.add(lCount);
            mCells[lCount] = new ValueCell();
        }
    }

    private boolean sMakeBase(){
        return sNextCell();
    }

    private boolean sDeleteCells(){
        List<Integer> lFreeCells;
        int lCount;
        int lCellNr;
        int lRandom;
        ValueCell lSaveCell;
        int lSolutions;
        int lError;
        boolean lContinue;
        boolean lResult;

        lResult = true;
        lFreeCells = new ArrayList<>();
        for (lCount = 0; lCount < mCells.length; lCount++){
            lFreeCells .add(lCount);
        }
        lError = 0;
        lContinue = true;
        do{
            if (mStop){
                lResult = false;
                break;
            }
            if (lFreeCells.size() > 0){
                lRandom = mRandom.nextInt(lFreeCells.size());
                lCellNr = lFreeCells.get(lRandom);
                lFreeCells.remove(lRandom);
                lSaveCell = mCells[lCellNr];
                mCells[lCellNr] = new ValueCell();
                lSolutions = sCountSolutions();
                if (lSolutions == 1) {
                    lError = 0;
                } else {
                    mCells[lCellNr] = lSaveCell;
                    lError++;
                    if (lError > mDifficulty){
                        lContinue = false;
                    }
                }
            } else {
                lContinue = false;
            }
        } while (lContinue);
        return lResult;
    }

    private void sFinishGame(){
        for (ValueCell bCell: mCells) {
            if (bCell.xValue() >= 0){
                bCell.xFixed(true);
            }
            bCell.xConflict(false);
        }
    }

    private boolean sNextCell(){
        int lRandom;
        int lCellNr;
        int lRow;
        int lColumn;
        boolean lResult;

        if (mStop){
            return false;
        }
        if (mFreeCells.size() > 0){
            lResult = false;
            lRandom = mRandom.nextInt(mFreeCells.size());
            lCellNr = mFreeCells.get(lRandom);
            mFreeCells.remove(lRandom);
            lRow = lCellNr / mColumns;
            lColumn = lCellNr % mColumns;
            if (mRandom.nextBoolean()){
                mCells[lCellNr].xValue(1);
            } else {
                mCells[lCellNr].xValue(0);
            }
            if (sCheckRow(lRow)){
                if (sCheckColumn(lColumn)){
                    lResult = sNextCell();
                }
            }
            if (!lResult){
                if (mCells[lCellNr].xValue() == 0){
                    mCells[lCellNr].xValue(1);
                } else {
                    mCells[lCellNr].xValue(0);
                }
                if (sCheckRow(lRow)){
                    if (sCheckColumn(lColumn)){
                        lResult = sNextCell();
                    }
                }
            }
            if (!lResult){
                mCells[lCellNr] = new ValueCell();
                mFreeCells.add(lCellNr);
            }
        } else {
            lResult = true;
        }
        return lResult;
    }

    private boolean sCheckRow(int pRow){
        return sCheckRow(mCells, pRow);
    }

    private boolean sCheckRow(ValueCell[] pCells, int pRow) {
        ValueCell[] lCells;
        int lCount;
        int lCellNr;

        lCells = new ValueCell[mColumns];
        lCellNr = pRow * mColumns;
        for (lCount = 0; lCount < lCells.length; lCount++){
            lCells[lCount] = pCells[lCellNr];
            lCellNr++;
        }
        return GameFunctions.xCheckUnit(lCells);
    }

    private boolean sCheckColumn(int pColumn){
        return sCheckColumn(mCells, pColumn);
    }

    private boolean sCheckColumn(ValueCell[] pCells, int pColumn){
        ValueCell[] lCells;
        int lCount;
        int lCellNr;

        lCells = new ValueCell[mRows];
        lCellNr = pColumn;
        for (lCount = 0; lCount < lCells.length; lCount++){
            lCells[lCount] = pCells[lCellNr];
            lCellNr += mColumns;
        }
        return GameFunctions.xCheckUnit(lCells);
    }

    private int sCountSolutions(){
        int lResult = 0;
        ValueCell[] lCells;
        int lCount;

        lCells = new ValueCell[mCells.length];
        for (lCount = 0; lCount < lCells.length; lCount++){
            lCells[lCount] = new ValueCell(mCells[lCount]);
        }
        lResult = sCountCell(lCells, 0, 0);
        return lResult;
    }

    private int sCountCell(ValueCell[] pCells, int pCellNr, int pSolutions){
        int lSolutions;
        int lRow;
        int lColumn;

        lSolutions = pSolutions;
        if (pCellNr >= pCells.length){
            lSolutions++;
        } else {
            if (pCells[pCellNr].xValue() < 0){
                lRow = pCellNr / mColumns;
                lColumn = pCellNr % mColumns;
                pCells[pCellNr].xValue(0);
                if (sCheckRow(pCells, lRow)){
                    if (sCheckColumn(pCells, lColumn)){
                        lSolutions = sCountCell(pCells, pCellNr + 1, lSolutions);
                    }
                }
                pCells[pCellNr].xValue(1);
                if (sCheckRow(pCells, lRow)){
                    if (sCheckColumn(pCells, lColumn)){
                        lSolutions = sCountCell(pCells, pCellNr + 1, lSolutions);
                    }
                }
                pCells[pCellNr].xPlayReset();
            } else {
                lSolutions = sCountCell(pCells, pCellNr + 1, lSolutions);
            }
        }
        return lSolutions;
    }
}
