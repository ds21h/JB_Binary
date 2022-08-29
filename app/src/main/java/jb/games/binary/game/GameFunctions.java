package jb.games.binary.game;

class GameFunctions {

    static boolean xCheckUnit(ValueCell[] pCells){
        int lCellNr;
        int lCellErr;
        ValueCell lCell;
        int lValue = -1;
        int lStart = 99;
        int lCount0 = 0;
        int lCount1 = 0;
        boolean lResult = true;

        for (lCellNr = 0; lCellNr < pCells.length; lCellNr++){
            lCell = pCells[lCellNr];
            if (lCell.xValue() != lValue){
                if (lCellNr - lStart > 2){
                    lResult = false;
                    for (lCellErr = lStart; lCellErr < lCellNr; lCellErr++){
                        pCells[lCellErr].xConflict(true);
                    }
                }
                lValue = lCell.xValue();
                if (lCell.xValue() < 0){
                    lStart = 99;
                } else {
                    lStart = lCellNr;
                }
            }
            if (lCell.xValue() == 0){
                lCount0++;
            } else {
                if (lCell.xValue() == 1){
                    lCount1++;
                }
            }
        }
        if (pCells.length - lStart > 2){
            lResult = false;
            for (lCellErr = lStart; lCellErr < pCells.length; lCellErr++){
                pCells[lCellErr].xConflict(true);
            }
        }
        if (lCount0 > pCells.length / 2){
            lResult = false;
            for (lCellErr = 0; lCellErr < pCells.length; lCellErr++) {
                if (pCells[lCellErr].xValue() == 0){
                    pCells[lCellErr].xConflict(true);
                }
            }
        }
        if (lCount1 > pCells.length / 2){
            lResult = false;
            for (lCellErr = 0; lCellErr < pCells.length; lCellErr++) {
                if (pCells[lCellErr].xValue() == 1){
                    pCells[lCellErr].xConflict(true);
                }
            }
        }
        return lResult;
    }
}
