package jb.games.binary.game;

class BinaryGameBase {
    private int mGameStatus;
    public static final int cStatusNone = 0;
    public static final int cStatusSetup = 1;
    public static final int cStatusGenerate = 2;
    public static final int cStatusPlay = 3;
    public static final int cStatusSolved = 4;

    private int mRows;
    private int mColumns;
    private PlayField mPlayField;
    private int mUsedTime;
    private int mDifficulty;

    private BinaryGenerator mGenerator = null;

    BinaryGameBase() {
        mGameStatus = cStatusNone;
        mRows = 10;
        mColumns = 8;
        mPlayField = new PlayField(mRows * mColumns);
        mUsedTime = 0;
        mDifficulty = 0;
    }

    BinaryGameBase(int pRows, int pColumns, int pDifficulty){
        mGameStatus = cStatusNone;
        mRows = pRows;
        mColumns = pColumns;
        mPlayField = new PlayField(mRows * mColumns);
        mUsedTime = 0;
        mDifficulty = pDifficulty;
    }

    BinaryGameBase(BinaryGameBase pGame) {
        mGameStatus = pGame.mGameStatus;
        mRows = pGame.mRows;
        mColumns = pGame.mColumns;
        mPlayField = new PlayField(pGame.mPlayField);
        mUsedTime = pGame.mUsedTime;
        mDifficulty = pGame.mDifficulty;
    }

    void xInitGame(PlayField pField, int pGameRows, int pGameColumns, int pStatus, int pDifficulty, int pUsedTime) {
        mRows = pGameRows;
        mColumns = pGameColumns;
        mGameStatus = pStatus;

        mPlayField = pField;
        mUsedTime = pUsedTime;
        mDifficulty = pDifficulty;
    }

    public int xRows() {
        return mRows;
    }

    public int xColumns() {
        return mColumns;
    }

    public int xGameStatus() {
        return mGameStatus;
    }

    public int xUsedTime() {
        return mUsedTime;
    }

    public void xAddUsedTime(int pCorr) {
        mUsedTime += pCorr;
    }

    public void xResetUsedTime() {
        mUsedTime = 0;
    }

    PlayField xPlayField() {
        return mPlayField;
    }

    void xPlayField(PlayField pField) {
        mPlayField = pField;
    }

    public int xDifficulty() {
        return mDifficulty;
    }

    public boolean xIsCellFixed(int pRow, int pColumn){
        return xPlayCell(pRow, pColumn).xFixed();
    }

    public boolean xCellConflict(int pRow, int pColumn){
        return xPlayCell(pRow, pColumn).xConflict();
    }

    public int xCellValue(int pRow, int pColumn) {
        return xPlayCell(pRow, pColumn).xValue();
    }

    ValueCell xPlayCell(int pRow, int pColumn) {
        return mPlayField.xCell(sCellNumber(pRow, pColumn));
    }

    private int sCellNumber(int pRow, int pColumn){
        if (pRow < mRows && pColumn < mColumns){
            return pRow * mColumns + pColumn;
        } else {
            return 0;
        }
    }

    public void xSelection(int pRow, int pColumn) {
        int lCellNr;

        lCellNr = pRow * mColumns + pColumn;
        mPlayField.xSelection(lCellNr);
    }

    public boolean xIsSelection(int pRow, int pColumn) {
        if (mPlayField.xSelection() == (pRow * mColumns) + pColumn) {
            return true;
        } else {
            return false;
        }
    }

    void xStartSetUp(int pRows, int pColumns) {
        mGameStatus = cStatusSetup;
        mRows = pRows;
        mColumns = pColumns;
        mPlayField = new PlayField(mRows * mColumns);
        mDifficulty = 0;
    }

    public boolean xFinishSetup() {
        ValueCell lValueCell;
        int lCount;
        boolean lResult;

        lResult = sCheckGame();
        if (lResult) {
            for (lCount = 0; lCount < mRows * mColumns; lCount++) {
                lValueCell = mPlayField.xCell(lCount);
                if (lValueCell.xValue() >= 0) {
                    lValueCell.xFixed(true);
                }
            }
        }
        return lResult;
    }

    public void xStartGame() {
        mGameStatus = cStatusPlay;
        xResetUsedTime();
    }

    void xReset() {
        mPlayField.xResetField();
        mGameStatus = cStatusPlay;
        mUsedTime = 0;
    }

    public void xUndo() {
        mPlayField.xActionUndo();
        sCheckGame();
    }

    public boolean xUndoAvail() {
        boolean lResult;

        if (mGameStatus == cStatusPlay) {
            lResult = mPlayField.xActionPresent();
        } else {
            lResult = false;
        }
        return lResult;
    }

    public void xProcessDigit(int pDigit) {
        ValueCell lValueCell;

        if (pDigit == 0 || pDigit == 1) {
            lValueCell = mPlayField.xSelectedCell();
            if (!lValueCell.xFixed()) {
                mPlayField.xActionBegin();
                mPlayField.xActionSaveCell();
                if (mPlayField.xSetSelectedCellValue(pDigit)) {
                    if (sCheckGame()) {
                        if (mPlayField.xFieldFull()) {
                            mGameStatus = cStatusSolved;
                        }
                    }
                } else {
                    sCheckGame();
                }
                mPlayField.xActionEnd();
            }
        }
    }

    private boolean sCheckGame() {
        boolean lResult;
        int lRow;
        int lColumn;
        int lCellNr;
        ValueCell[] lCells;
        boolean lUnitResult;

        lResult = true;
        mPlayField.xResetConflicts();
        lCellNr = 0;
        lCells = new ValueCell[mColumns];
        for (lRow = 0; lRow < mRows; lRow++) {
            for (lColumn = 0; lColumn < mColumns; lColumn++) {
                lCells[lColumn] = mPlayField.xCell(lCellNr);
                lCellNr++;
            }
            lUnitResult = GameFunctions.xCheckUnit(lCells);
            if (!lUnitResult){
                lResult = false;
            }
        }
        lCells = new ValueCell[mRows];
        for (lColumn = 0; lColumn < mColumns; lColumn++) {
            for (lRow = 0; lRow < mRows; lRow++) {
                lCells[lRow] = mPlayField.xCell((lRow * mColumns) + lColumn);
            }
            lUnitResult = GameFunctions.xCheckUnit(lCells);
            if (!lUnitResult){
                lResult = false;
            }
        }
        return lResult;
    }

    boolean xGenerate(int pRows, int pColums, int pDifficulty){
        boolean lResult;

        mGameStatus = cStatusGenerate;
        mRows = pRows;
        mColumns = pColums;
        mDifficulty = pDifficulty;
        mGenerator = new BinaryGenerator(pRows, pColums, pDifficulty);
        if (mGenerator.xGenerate()){
            mPlayField = new PlayField(mGenerator.xGetCells());
            mGenerator = null;
            mUsedTime = 0;
            mGameStatus = cStatusPlay;
            lResult = true;
        } else {
            mGameStatus = cStatusNone;
            lResult = false;
        }
        return lResult;
    }

    public void xGenerateStop(){
        if (mGenerator != null){
            mGenerator.xStop();
        }
    }
}
