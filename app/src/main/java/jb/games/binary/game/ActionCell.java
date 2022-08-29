package jb.games.binary.game;

class ActionCell {
    private int mCellNr;
    private ValueCell mCell;

    ActionCell(int pCellNr, ValueCell pCell){
        mCellNr = pCellNr;
        mCell = new ValueCell(pCell);
    }

    int xCellNr(){
        return mCellNr;
    }

    ValueCell xCell(){
        return mCell;
    }
}
