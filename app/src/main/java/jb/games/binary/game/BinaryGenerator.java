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
    private int[] cMinFraction = {50, 40, 30, 20, 10};
    private int mMinCells;

    BinaryGenerator(int pRows, int pColumns, int pDifficulty){
        mRows = pRows;
        mColumns = pColumns;
        if (pDifficulty < 1){
            mDifficulty = 1;
        } else {
            if (pDifficulty > 5){
                mDifficulty = 5;
            } else {
                mDifficulty = pDifficulty;
            }
        }
        mNumberCells = mRows * mColumns;
        mMinCells = (mNumberCells * cMinFraction[mDifficulty - 1])/100;
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
            if (lFreeCells.size() > mMinCells){
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
        return sCheckUnit(lCells);
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
        return sCheckUnit(lCells);
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

    private boolean sCheckUnit(ValueCell[] pCells){
        int lCellNr;
        ValueCell lCell;
        int lValue = -1;
        int lCount0 = 0;
        int lCount1 = 0;
        boolean lResult = true;
        int lDups = 1;
        int lMaxOcc;

        lMaxOcc = pCells.length/2;
        for (lCellNr = 0; lCellNr < pCells.length; lCellNr++){
            lCell = pCells[lCellNr];
            if (lCell.xValue() < 0){
                lValue = -1;
                lDups = 1;
            } else {
                if (lCell.xValue() == lValue){
                    lDups++;
                    if (lDups > 2){
                        lResult = false;
                        break;
                    }
                } else {
                    lValue = lCell.xValue();
                    lDups = 1;
                }
                if (lCell.xValue() == 0){
                    lCount0++;
                    if (lCount0 > lMaxOcc){
                        lResult = false;
                        break;
                    }
                } else {
                    lCount1++;
                    if (lCount1 > lMaxOcc){
                        lResult = false;
                        break;
                    }
                }
            }
        }
        return lResult;
    }
}
