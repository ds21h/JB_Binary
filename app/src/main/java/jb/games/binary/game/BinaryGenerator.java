package jb.games.binary.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BinaryGenerator {
    private final int mRows;
    private final int mColumns;
    private final int mNumberCells;
    private final int mDifficulty;
    private final int[] mFrCells;
    private int mTopFree;
    private final ValueCell[] mCells;
    private final Random mRandom;
    private volatile boolean mStop;
    private final int[] cMinFraction = {50, 40, 30, 20, 10};
    private final int mMinCells;
    private final ValueCell[] mTestRow;
    private final ValueCell[] mTestColumn;

    int xRows(){
        return mRows;
    }

    int xColumns(){
        return mColumns;
    }

    int xDifficulty(){
        return mDifficulty;
    }

    public BinaryGenerator(int pRows, int pColumns, int pDifficulty){
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
        mFrCells = new int[mNumberCells];
        mTopFree = -1;
        mCells = new ValueCell[mNumberCells];
        mRandom = new Random();
        mStop = false;
        mTestRow = new ValueCell[mColumns];
        mTestColumn = new ValueCell[mRows];
    }

    public void xStop(){
        mStop = true;
    }

    ValueCell[] xGetCells(){
        return mCells;
    }

    public boolean xGenerate(){
        boolean lResult;

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

        for (lCount = 0; lCount < mNumberCells; lCount++){
            mFrCells[lCount] = lCount;
            mCells[lCount] = new ValueCell();
        }
        mTopFree = mNumberCells - 1;
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
        if (mTopFree >= 0){
            lResult = false;
            lRandom = mRandom.nextInt(mTopFree + 1);
            lCellNr = mFrCells[lRandom];
            if (lRandom < mTopFree){
                mFrCells [lRandom] = mFrCells[mTopFree];
            }
            mTopFree--;
            lRow = lCellNr / mColumns;
            lColumn = lCellNr % mColumns;
            if ((lRandom & 0x01) == 0){
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
                mTopFree++;
                mFrCells[mTopFree] = lCellNr;
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
        int lCount;
        int lCellNr;

        lCellNr = pRow * mColumns;
        for (lCount = 0; lCount < mColumns; lCount++){
            mTestRow[lCount] = pCells[lCellNr];
            lCellNr++;
        }
        return sCheckUnit(mTestRow);
    }

    private boolean sCheckColumn(int pColumn){
        return sCheckColumn(mCells, pColumn);
    }

    private boolean sCheckColumn(ValueCell[] pCells, int pColumn){
        int lCount;
        int lCellNr;

        lCellNr = pColumn;
        for (lCount = 0; lCount < mRows; lCount++){
            mTestColumn[lCount] = pCells[lCellNr];
            lCellNr += mColumns;
        }
        return sCheckUnit(mTestColumn);
    }

    private int sCountSolutions(){
        int lResult;
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
