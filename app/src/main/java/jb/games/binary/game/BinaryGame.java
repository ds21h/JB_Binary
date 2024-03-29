package jb.games.binary.game;

import java.util.ArrayList;
import java.util.List;

import jb.games.binary.BinaryApp;

public class BinaryGame extends BinaryGameBase {
    private final List<PlayField> mPlayFields;

    public BinaryGame(){
        super();
        mPlayFields = new ArrayList<>();
        mPlayFields.add(xPlayField());
    }

    public BinaryGame(List<PlayField> pFields, int pGameRows, int pGameColumns, int pStatus, int pDifficulty, int pSelectedField, int pUsedTime){
        super();

        PlayField lPlayField;

        mPlayFields = pFields;
        if (mPlayFields.isEmpty()) {
            lPlayField = new PlayField(pGameRows * pGameColumns);
        } else {
            lPlayField = pFields.get(0);
            if (lPlayField.xFieldId() != pSelectedField){
                for (PlayField lField : mPlayFields) {
                    if (lField.xFieldId() == pSelectedField){
                        lPlayField = lField;
                        break;
                    }
                }
            }
        }
        xInitGame(lPlayField, pGameRows, pGameColumns, pStatus, pDifficulty, pUsedTime);
    }

    public BinaryGame(BinaryGame pGame){
        super(pGame);

        PlayField lField;

        mPlayFields = new ArrayList<>();
        for (PlayField bField: pGame.mPlayFields) {
            lField = new PlayField(bField);
            mPlayFields.add(lField);
        }
    }

    public static BinaryGame xGetCurrentGame(){
        Data lData;
        BinaryGame lGame;

        lData = Data.getInstance(BinaryApp.getApplContext());
        lGame = lData.xCurrentGame();
        return lGame;
    }

    public void xSaveGame(){
        SaveGame lSaveGame;

        lSaveGame = new SaveGame(this);
        BinaryApp.getInstance().xExecutor.execute(lSaveGame);
    }

    public int[] xPlayFieldIds(){
        int[] lFieldIds;
        int lCount;

        lFieldIds = new int[mPlayFields.size()];
        for (lCount = 0; lCount < lFieldIds.length; lCount++){
            lFieldIds[lCount] = mPlayFields.get(lCount).xFieldId();
        }
        return lFieldIds;
    }

    public int xCurrentFieldId(){
        return xPlayField().xFieldId();
    }

    public int xFieldCount(){
        return mPlayFields.size();
    }

    public void xPlayFieldCopy(){
        int lNewId;
        PlayField lField;

        sSaveCurrentPlayField();
        lNewId = mPlayFields.get(mPlayFields.size() - 1).xFieldId() + 1;
        lField = new PlayField(lNewId, xPlayField());
        mPlayFields.add(lField);
        xPlayField(lField);
    }

    public void xSwitchPlayField(int pNewId){
        if (xPlayField().xFieldId() != pNewId){
            sSaveCurrentPlayField();
            for (PlayField lField : mPlayFields){
                if (lField.xFieldId() == pNewId){
                    xPlayField(lField);
                    break;
                }
            }
        }
    }

    private void sSaveCurrentPlayField(){
        SavePlayField lSavePlayField;

        lSavePlayField = new SavePlayField(xPlayField());
        BinaryApp.getInstance().xExecutor.execute(lSavePlayField);
    }

    private void sDeleteSaveGame(){
        DeleteSaveGame lDeleteSaveGame;

        lDeleteSaveGame = new DeleteSaveGame();

        BinaryApp.getInstance().xExecutor.execute(lDeleteSaveGame);
    }

    private void sDeletePlayField(int pPlayFieldId){
        DeletePlayField lDeletePlayField;

        lDeletePlayField = new DeletePlayField(pPlayFieldId);
        BinaryApp.getInstance().xExecutor.execute(lDeletePlayField);
    }

    public void xDeleteCurrentPlayField(){
        if (xPlayField().xFieldId() != 0){
            if (mPlayFields.size() > 1){
                sDeletePlayField(xPlayField().xFieldId());
                mPlayFields.remove(xPlayField());
                xPlayField(mPlayFields.get(mPlayFields.size() - 1));
            }
        }
    }

    public void xStartSetUp(int pRows, int pColumns) {
        sDeleteSaveGame();
        super.xStartSetUp(pRows, pColumns);
        mPlayFields.clear();
        mPlayFields.add(xPlayField());
    }

    public void xReset(){
        sDeleteSaveGame();
        xPlayField(mPlayFields.get(0));
        super.xReset();
        mPlayFields.clear();
        mPlayFields.add(xPlayField());
    }

    public void xNewGame(BinaryGenerator pGenerator){
        super.xNewGame(pGenerator);
        sDeleteSaveGame();
        mPlayFields.clear();
        mPlayFields.add(xPlayField());
    }
}
