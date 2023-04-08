package jb.games.binary.game;

class ActionCell {
    private final int mCellNr;
    private final ValueCell mCell;

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
