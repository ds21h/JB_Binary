package jb.games.binary.game;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan on 9-2-2019.
 */

class Data extends SQLiteOpenHelper {
    private static Data mInstance = null;

    private static final String cDBName = "Binary.db";
    private static final int cDBVersion = 1;
    private static String mExternalFilesDir;

    private SQLiteDatabase mDB;

    static Data getInstance(Context pContext) {
        Context lContext;
        File lExternalFilesDir;
        /*
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information:
         * http://developer.android.com/resources/articles/avoiding-memory-leaks.html)
         *
         * use double-check locking for thread-safe initialization.
         * see https://www.geeksforgeeks.org/java-singleton-design-pattern-practices-examples/
         */
        if (mInstance == null) {
            synchronized(Data.class){
                if (mInstance == null){
                    lContext = pContext.getApplicationContext();
                    lExternalFilesDir = lContext.getExternalFilesDir(null);
                    if (lExternalFilesDir == null) {
                        mExternalFilesDir = "";
                    } else {
                        mExternalFilesDir = lExternalFilesDir.getAbsolutePath();
                    }
                    mInstance = new Data(lContext);
                }
            }
        }
        return mInstance;
    }

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private Data(Context pContext) {
        super(pContext, mExternalFilesDir + "/" + cDBName, null, cDBVersion);
        mDB = this.getWritableDatabase();
    }

    @Override
    public synchronized void close() {
        super.close();
        mInstance = null;
    }

    @Override
    public void onCreate(SQLiteDatabase pDB) {
        sDefineGame(pDB);
        sInitGame(pDB);
        sDefinePlayField(pDB);
        sDefineCell(pDB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase pDB, int pOldVersion, int pNewVersion) {
        switch (pOldVersion){
            default:
                pDB.execSQL("DROP TABLE IF EXISTS BinaryGame");
                pDB.execSQL("DROP TABLE IF EXISTS PlayField");
                pDB.execSQL("DROP TABLE IF EXISTS Cell");
                onCreate(pDB);
                break;
        }
    }

    private void sDefineGame(SQLiteDatabase pDB) {
        pDB.execSQL(
                "CREATE TABLE BinaryGame " +
                        "(ContextId Text primary key, " +
                        "Rows Integer Not Null, " +
                        "Columns Integer Not Null, " +
                        "Status Integer Not Null, " +
                        "Difficulty Integer Not Null, " +
                        "SelectedField Integer Not Null, " +
                        "UsedTime Integer Not Null" +
                        ")"
        );
    }

    private void sInitGame(SQLiteDatabase pDB) {
        pDB.execSQL(
                "INSERT INTO BinaryGame " +
                        "(ContextId, Rows, Columns, Status, Difficulty, SelectedField, UsedTime) " +
                        "VALUES " +
                        "('Binary', 10, 8, 0, 0, 0, 0)"
        );
    }

    private void sDefinePlayField(SQLiteDatabase pDB) {
        pDB.execSQL(
                "CREATE TABLE PlayField " +
                        "(FieldId Integer primary key, " +
                        "Selection Integer Not Null " +
                        ")"
        );
    }

    private void sDefineCell(SQLiteDatabase pDB) {
        pDB.execSQL(
                "CREATE TABLE Cell " +
                        "(_ID Integer primary key, " +
                        "FieldId Integer Not Null, " +
                        "CellNumber Integer Not Null, " +
                        "Value Integer Not Null, " +
                        "Fixed Integer Not Null, " +
                        "Confl Integer Not Null " +
                        ")"
        );
    }

    void xSaveGame(BinaryGame pGame) {
        mDB.beginTransaction();
        sUpdateGame(pGame);
        sSavePlayField(pGame.xPlayField());
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    void xSavePlayField(PlayField pPlayField){
        mDB.beginTransaction();
        sSavePlayField(pPlayField);
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    void xDeletePlayField(int pFieldId){
        mDB.beginTransaction();
        sDeletePlayField(pFieldId);
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    private void sSavePlayField(PlayField pPlayField) {
        ValueCell[] lValueCells;
        int lCount;

        sDeletePlayField(pPlayField.xFieldId());
        sNewPlayField(pPlayField);
        lValueCells = pPlayField.xCells();
        for (lCount = 0; lCount < lValueCells.length; lCount++) {
            sNewCell(pPlayField.xFieldId(), lCount, lValueCells[lCount]);
        }
    }

    private void sDeletePlayField(int pPlayFieldId) {
        String lSelection;
        String[] lSelectionArgs;

        lSelection = "FieldId = ?";
        lSelectionArgs = new String[1];
        lSelectionArgs[0] = String.valueOf(pPlayFieldId);

        mDB.delete("PlayField", lSelection, lSelectionArgs);

        mDB.delete("Cell", lSelection, lSelectionArgs);
    }

    private void sNewPlayField(PlayField pField) {
        ContentValues lValues;

        lValues = new ContentValues();
        lValues.put("FieldId", pField.xFieldId());
        lValues.put("Selection", pField.xSelection());

        mDB.insert("PlayField", null, lValues);
    }

    private void sNewCell(int pFieldId, int pCellNumber, ValueCell pValueCell) {
        ContentValues lValues;

        lValues = new ContentValues();
        lValues.put("FieldId", pFieldId);
        lValues.put("CellNumber", pCellNumber);
        lValues.put("Value", pValueCell.xValue());
        lValues.put("Fixed", (pValueCell.xFixed()) ? 1 : 0);
        lValues.put("Confl", (pValueCell.xConflict()) ? 1 : 0);

        mDB.insert("Cell", null, lValues);
    }

    void xDeleteSave() {
        mDB.beginTransaction();
        mDB.delete("PlayField", null, null);
        mDB.delete("Cell", null, null);
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    private void sUpdateGame(BinaryGame pGame) {
        ContentValues lValues;
        String lSelection;
        String[] lSelectionArgs;

        lValues = new ContentValues();
        lValues.put("Rows", pGame.xRows());
        lValues.put("Columns", pGame.xColumns());
        lValues.put("Status", pGame.xGameStatus());
        lValues.put("Difficulty", pGame.xDifficulty());
        lValues.put("SelectedField", pGame.xPlayField().xFieldId());
        lValues.put("UsedTime", pGame.xUsedTime());
        lSelection = "ContextId = ?";
        lSelectionArgs = new String[]{"Binary"};

        mDB.update("BinaryGame", lValues, lSelection, lSelectionArgs);
    }

    BinaryGame xCurrentGame() {
        BinaryGame lGame;
        List<PlayField> lFields;
        Cursor lCursor;
        String[] lColumns;
        String lSelection;
        String[] lSelectionArgs;
        int lGameRows = 0;
        int lGameColumns = 0;
        int lStatus = 0;
        int lDifficulty = -1;
        int lSelectedField = 0;
        int lUsedTime = 0;

        lColumns = new String[]{"Rows", "Columns", "Status", "Difficulty", "SelectedField", "UsedTime"};
        lSelection = "ContextId = ?";
        lSelectionArgs = new String[]{"Binary"};

        try {
            lCursor = mDB.query("BinaryGame", lColumns, lSelection, lSelectionArgs, null, null, null);
            if (lCursor.moveToNext()) {
                lGameRows = lCursor.getInt(0);
                lGameColumns = lCursor.getInt(1);
                lStatus = lCursor.getInt(2);
                lDifficulty = lCursor.getInt(3);
                lSelectedField = lCursor.getInt(4);
                lUsedTime = lCursor.getInt(5);
            }
            lCursor.close();
        } catch (Exception ignored) { }

        lFields = sGetFields(lGameRows * lGameColumns);

        lGame = new BinaryGame(lFields, lGameRows, lGameColumns, lStatus, lDifficulty, lSelectedField, lUsedTime);
        return lGame;
    }

    private List<PlayField> sGetFields(int pSize) {
        List<PlayField> lFields;
        PlayField lField;
        ValueCell[] lValueCells;
        Cursor lCursor;
        String[] lColumns;
        String lSequence;
        int lFieldId;
        int lSel;

        lFields = new ArrayList<>();

        lColumns = new String[]{"FieldId", "Selection"};
        lSequence = "FieldId";

        lCursor = mDB.query("PlayField", lColumns, null, null, null, null, lSequence);
        while (lCursor.moveToNext()) {
            lFieldId = lCursor.getInt(0);
            lSel = lCursor.getInt(1);
            lValueCells = sGetCells(lFieldId, pSize);
            //noinspection RedundantConditionalExpression
            lField = new PlayField(lFieldId, lValueCells, lSel);
            lFields.add(lField);
        }
        lCursor.close();

        return lFields;
    }

    private ValueCell[] sGetCells(int pFieldId, int pSize) {
        ValueCell[] lValueCells;
        ValueCell lValueCell;
        Cursor lCursor;
        String[] lColumns;
        String lSelection;
        String[] lSelectionArgs;
        String lSequence;
        int lCellNumber;
        int lValue;
        int lFixed;
        int lConflict;

        lColumns = new String[]{"CellNumber", "Value", "Fixed", "Confl"};
        lSelection = "FieldId = ?";
        lSelectionArgs = new String[1];
        lSelectionArgs[0] = String.valueOf(pFieldId);
        lSequence = "CellNumber";

        lValueCells = new ValueCell[pSize];

        lCursor = mDB.query("Cell", lColumns, lSelection, lSelectionArgs, null, null, lSequence);
        while (lCursor.moveToNext()) {
            lCellNumber = lCursor.getInt(0);
            lValue = lCursor.getInt(1);
            lFixed = lCursor.getInt(2);
            lConflict = lCursor.getInt(3);
            //noinspection RedundantConditionalExpression
            lValueCell = new ValueCell(lValue, (lFixed == 0) ? false : true, (lConflict == 0) ? false : true);
            lValueCells[lCellNumber] = lValueCell;
        }
        lCursor.close();

        return lValueCells;
    }
}
