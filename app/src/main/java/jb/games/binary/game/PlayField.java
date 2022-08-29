package jb.games.binary.game;

class PlayField {
    private int mFieldId;
    private ValueCell[] mValueCells;
    private int mSelection;
    private int mFilledCells;
    private Action[] mActions;
    private static final int cActionMax = 20;
    private int mActionStart;
    private int mActionNumber;
    private Action mAction;

    PlayField(int pSize) {
        int lCount;

        mFieldId = 0;
        mValueCells = new ValueCell[pSize];
        for (lCount = 0; lCount < mValueCells.length; lCount++) {
            mValueCells[lCount] = new ValueCell();
        }
        mSelection = 0;
        mFilledCells = 0;
        sInitActions();
    }

    PlayField(ValueCell[] pValueCells) {
        mFieldId = 0;
        mValueCells = pValueCells;
        mSelection = 0;
        sCountFilledCells();
        sInitActions();
    }

    PlayField(int pFieldId, ValueCell[] pValueCells, int pSelection) {
        mFieldId = pFieldId;
        mValueCells = pValueCells;
        mSelection = pSelection;
        sCountFilledCells();
        sInitActions();
    }

    PlayField(PlayField pField){
       sPlayField(pField.xFieldId(), pField);
    }

    PlayField(int pFieldId, PlayField pField){
        sPlayField(pFieldId, pField);
    }

    private void sPlayField(int pFieldId, PlayField pField){
        int lCount;

        mFieldId = pFieldId;
        mValueCells = new ValueCell[pField.mValueCells.length];
        for (lCount = 0; lCount < mValueCells.length; lCount++) {
            mValueCells[lCount] = new ValueCell(pField.mValueCells[lCount]);
        }
        mSelection = pField.mSelection;
        mFilledCells = pField.mFilledCells;
        sInitActions();
    }

    private void sCountFilledCells(){
        int lCount;

        mFilledCells = 0;
        for (lCount = 0; lCount < mValueCells.length; lCount++){
            if (mValueCells[lCount].xValue() >= 0){
                mFilledCells++;
            }
        }
    }

    private void sInitActions(){
        mActions = new Action[cActionMax];
        mActionStart = 0;
        mActionNumber = 0;
    }

    void xActionBegin(){
        mAction = new Action(mSelection, mFilledCells);
    }

    void xActionEnd(){
        int lIndex;

        if (mAction != null){
            lIndex = mActionStart + mActionNumber;
            while (lIndex >= cActionMax){
                lIndex -= cActionMax;
            }
            if (mActionNumber < cActionMax){
                mActionNumber++;
            } else {
                mActionStart++;
                if (mActionStart >= cActionMax){
                    mActionStart = 0;
                }
            }
            mActions[lIndex] = mAction;
            mAction = null;
        }
    }

    void xActionSaveCell(){
        xActionSaveCell(mSelection);
    }

    void xActionSaveCell(int pCellNr){
        if (mAction != null && pCellNr >= 0 && pCellNr < mValueCells.length){
            mAction.xSaveCell(pCellNr, mValueCells[pCellNr]);
        }
    }

    boolean xActionPresent(){
        return mActionNumber > 0;
    }

    void xActionUndo(){
        Action lAction;
        int lIndex;

        if (mActionNumber > 0){
            mActionNumber--;
            lIndex = mActionStart + mActionNumber;
            while (lIndex >= cActionMax){
                lIndex -= cActionMax;
            }
            lAction = mActions[lIndex];
            mActions[lIndex] = null;
            if (lAction != null){
                sActionReverse(lAction);
            }
        }
    }

    void sActionReverse(Action pAction){
        int lNumCell;
        int lCount;
        ActionCell lCell;
        int lCellNr;

        lNumCell = pAction.xNumCells();
        for (lCount = lNumCell - 1; lCount >= 0; lCount--){
            lCell = pAction.xGetCell(lCount);
            lCellNr = lCell.xCellNr();
            mValueCells[lCellNr] = new ValueCell(lCell.xCell());
        }
        mSelection = pAction.xSelection();
        mFilledCells = pAction.xFilledCells();
    }

    int xFieldId(){
        return mFieldId;
    }

    void xSelection(int pSelection){
        mSelection = pSelection;
    }

    int xSelection(){
        return mSelection;
    }

    boolean xFieldFull(){
        if (mFilledCells == mValueCells.length){
            return true;
        } else {
            return false;
        }
    }

    ValueCell xSelectedCell() {
        return mValueCells[mSelection];
    }

    ValueCell xCell(int pCellNr) {
        return mValueCells[pCellNr];
    }

    ValueCell[] xCells(){
        return mValueCells;
    }

    void xResetField(){
        int lCount;

        mFilledCells = 0;
        for (lCount = 0; lCount < mValueCells.length; lCount++){
            if (mValueCells[lCount].xFixed()){
                mFilledCells++;
            } else {
                mValueCells[lCount].xPlayReset();
            }
        }
        sInitActions();
    }

    boolean xSetSelectedCellValue(int pValue) {
        ValueCell lValueCell;
        boolean lValueSet;

        lValueCell = mValueCells[mSelection];
        if (lValueCell.xValue() == pValue) {
            lValueCell.xValueReset();
            mFilledCells--;
            lValueSet = false;
        } else {
            if (lValueCell.xValue() < 0){
                mFilledCells++;
            }
            lValueCell.xValue(pValue);
            lValueSet = true;
        }
        return lValueSet;
    }

    void xResetConflicts() {
        int lCount;

        for (lCount = 0; lCount < mValueCells.length; lCount++) {
            mValueCells[lCount].xConflict(false);
        }
    }
 }
