package jb.games.binary.game;

class ValueCell {
    private int mValue;
    private boolean mFixed;
    private boolean mConflict;

    ValueCell(){
        sFullReset();
    }

    ValueCell(int pValue, boolean pFixed, boolean pConflict){
        mValue = pValue;
        mFixed = pFixed;
        mConflict = pConflict;
    }

    ValueCell(ValueCell pValueCell){
        mValue = pValueCell.mValue;
        mFixed = pValueCell.mFixed;
        mConflict = pValueCell.mConflict;
    }

    void xPlayReset(){
        sPlayReset();
    }

    private void sFullReset(){
        mFixed = false;
        sPlayReset();
    }

    private void sPlayReset(){
        mValue = -1;
        mConflict = false;
    }

    int xValue() {
        return mValue;
    }

    void xValue(int pValue) {
        if (pValue == 0 || pValue == 1) {
            mValue = pValue;
        }
    }

    void xValueReset() {
        mValue = -1;
    }

    boolean xFixed() {
        return mFixed;
    }

    void xFixed(boolean pFixed) {
        mFixed = pFixed;
    }

    boolean xConflict() {
        return mConflict;
    }

    void xConflict(boolean pConflict) {
        mConflict = pConflict;
    }
 }
