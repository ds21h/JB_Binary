package jb.games.binary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import jb.games.binary.databinding.ActivityMainBinaryBinding;
import jb.games.binary.game.BinaryGame;

import org.threeten.bp.Instant;

public class MainBinary extends AppCompatActivity {
    private ActivityMainBinaryBinding mBinding;

    private final Context mContext = this;
    private BinaryGame mGame;
    private BinaryView mBnrView;
    private Bundle mGameParams = null;
    private long mStartTime;
    private String mHeader;
    private GenerateRunnable mGenerate;
    private int mGenerateCount;

    ActivityResultLauncher<Intent> mStartSetup = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult pResult) {
                    Intent pInt;

                    if (pResult.getResultCode() == RESULT_OK) {
                        pInt = pResult.getData();
                        mGameParams = pInt.getExtras();
                    }
                }
            });

    Handler mGenerateHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message pMessage) {
            if (pMessage.what == GenerateRunnable.cGenerateFinished){
                sStartGame();
            }
            return true;
        }
    });

    Handler mRefreshHandler = new Handler(Looper.getMainLooper());
    Runnable mRefreshRunnable = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            Instant lInstant;
            long lNowTime;
            int lElapsed;
            int lMinute;
            int lSecond;
            StringBuilder lBuilder;
            int lCount;

            switch (mGame.xGameStatus()) {
                case BinaryGame.cStatusGenerate: {
                    lBuilder = new StringBuilder();
                    mGenerateCount++;
                    if (mGenerateCount > 5) {
                        mGenerateCount = 1;
                    }
                    lBuilder.append(getString(R.string.mnu_new));
                    for (lCount = 0; lCount < mGenerateCount; lCount++) {
                        lBuilder.append(".");
                    }
                    setTitle(lBuilder.toString());
                    mRefreshHandler.postDelayed(this, 1000);
                    break;
                }
                case BinaryGame.cStatusPlay: {
                    lInstant = Instant.now();
                    lNowTime = lInstant.getEpochSecond();
                    lElapsed = (int) (lNowTime - mStartTime);
                    lMinute = lElapsed / 60;
                    lSecond = lElapsed % 60;
                    setTitle(mHeader + String.format(" %02d:%02d", lMinute, lSecond));
                    mRefreshHandler.postDelayed(this, 100);
                    break;
                }
                case BinaryGame.cStatusSolved: {
                    lElapsed = mGame.xUsedTime();
                    lMinute = lElapsed / 60;
                    lSecond = lElapsed % 60;
                    setTitle(mHeader + String.format(" %02d:%02d", lMinute, lSecond));
                    mRefreshHandler.postDelayed(this, 500);
                    break;
                }
                case BinaryGame.cStatusSetup: {
                    setTitle(getString(R.string.app_name) + " - " + getString(R.string.head_setup));
                    mRefreshHandler.postDelayed(this, 500);
                    break;
                }
                default: {
                    setTitle(getString(R.string.app_name));
                    mRefreshHandler.postDelayed(this, 500);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityMainBinaryBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGame = new BinaryGame();
        mHeader = "";
        mGenerateCount = -1;
        mBnrView = mBinding.bvMain;
        mBnrView.setGame(mGame);
        mBnrView.setIntBinaryView(new BinaryView.intBinaryView() {
            @Override
            public void onSolved() {
                sSaveUsedTime();
                Toast.makeText(mContext, R.string.msg_solved, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart(){
        super.onStart();

        mGame = BinaryGame.xGetCurrentGame();
    }

    @Override
    protected void onResume() {
        int lRows;
        int lColumns;
        int lDifficulty;

        super.onResume();

        mBnrView.setGame(mGame);
        sSetHeader();
        if (mGame.xGameStatus() == BinaryGame.cStatusPlay) {
            sSetStartTime();
            mStartTime -= mGame.xUsedTime();
            mGame.xResetUsedTime();
        }
        mRefreshHandler.postDelayed(mRefreshRunnable, 10);
        if (mGameParams != null) {
            lRows = mGameParams.getInt(SelectGameParams.cRows);
            lColumns = mGameParams.getInt(SelectGameParams.cColumns);
            lDifficulty = mGameParams.getInt(SelectGameParams.cDifficulty, -1);
            mGameParams = null;
            if (lDifficulty < 0){
                sSetupStart(lRows, lColumns);
            } else {
                mGenerate = new GenerateRunnable(mGenerateHandler, mGame, lRows, lColumns, lDifficulty);
                BinaryApp.getInstance().xExecutor.execute(mGenerate);
            }
        }
    }

    @Override
    protected void onPause() {
        mRefreshHandler.removeCallbacks(mRefreshRunnable);
        if (mGame.xGameStatus() == BinaryGame.cStatusPlay) {
            sSaveUsedTime();
        }

        super.onPause();
    }

    @Override
    protected void onStop(){
        mGame.xGenerateStop();
        mGame.xSaveGame();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu pMenu) {
        super.onCreateOptionsMenu(pMenu);
        getMenuInflater().inflate(R.menu.binary_menu, pMenu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu pMenu) {
        super.onPrepareOptionsMenu(pMenu);

        MenuItem lMnuUndo;
        MenuItem lMnuNew;
        MenuItem lMnuSetup;
        MenuItem lMnuSetupStart;
        MenuItem lMnuSetupFinish;
        MenuItem lMnuPlayField;

        lMnuUndo = pMenu.findItem(R.id.mnuUndo);
        lMnuNew = pMenu.findItem(R.id.mnuNew);
        lMnuSetup = pMenu.findItem(R.id.mnuSetup);
        lMnuSetupStart = pMenu.findItem(R.id.mnuSetupStart);
        lMnuSetupFinish = pMenu.findItem(R.id.mnuSetupFinish);
        lMnuPlayField = pMenu.findItem(R.id.mnuFields);

        lMnuNew.setEnabled(true);
        lMnuSetup.setEnabled(true);
        lMnuSetupStart.setEnabled(true);

        switch (mGame.xGameStatus()) {
            case BinaryGame.cStatusSetup: {
                lMnuUndo.setEnabled(false);
                lMnuSetupFinish.setEnabled(true);
                lMnuPlayField.setEnabled(false);
                break;
            }
            case BinaryGame.cStatusPlay: {
                lMnuUndo.setEnabled(mGame.xUndoAvail());
                lMnuSetupFinish.setEnabled(false);
                lMnuPlayField.setEnabled(true);
                break;
            }
            case BinaryGame.cStatusSolved: {
                lMnuUndo.setEnabled(false);
                lMnuSetupFinish.setEnabled(false);
                lMnuPlayField.setEnabled(false);
                break;
            }
            default: {
                lMnuUndo.setEnabled(false);
                lMnuSetupFinish.setEnabled(false);
                lMnuPlayField.setEnabled(false);
                break;
            }
        }
        return true;
    }

    private void sSetHeader() {
        if (mGame.xGameStatus() == BinaryGame.cStatusPlay || mGame.xGameStatus() == BinaryGame.cStatusSolved) {
            switch (mGame.xDifficulty()) {
                case 1: {
                    mHeader = getString(R.string.head_level_very_easy);
                    break;
                }
                case 2: {
                    mHeader = getString(R.string.head_level_easy);
                    break;
                }
                case 3: {
                    mHeader = getString(R.string.head_level_medium);
                    break;
                }
                case 4: {
                    mHeader = getString(R.string.head_level_hard);
                    break;
                }
                case 5: {
                    mHeader = getString(R.string.head_level_very_hard);
                    break;
                }
                default: {
                    mHeader = getString(R.string.app_name);
                    break;
                }
            }
        } else {
            mHeader = getString(R.string.app_name);
        }
    }

    private void sSetStartTime() {
        Instant lInstant;

        lInstant = Instant.now();
        mStartTime = lInstant.getEpochSecond();
    }

    private void sSaveUsedTime() {
        Instant lInstant;

        lInstant = Instant.now();
        mGame.xAddUsedTime((int) (lInstant.getEpochSecond() - mStartTime));
    }

    public void hUndo(MenuItem pItem) {
        mGame.xUndo();
        mBnrView.invalidate();
    }

    public void hNew(MenuItem pItem) {
        Intent lInt;
        Bundle lBundle;

        mGame.xGenerateStop();
        lBundle = new Bundle();
        lBundle.putInt(SelectGameParams.cRows, mGame.xRows());
        lBundle.putInt(SelectGameParams.cColumns, mGame.xColumns());
        lBundle.putInt(SelectGameParams.cDifficulty, mGame.xDifficulty());
        lInt = new Intent();
        lInt.setClass(this, SelectGameParams.class);
        lInt.putExtras(lBundle);
        mStartSetup.launch(lInt);
    }

    public void hSetupStart(MenuItem pItem) {
        Intent lInt;
        Bundle lBundle;

        mGame.xGenerateStop();
        lBundle = new Bundle();
        lBundle.putInt(SelectGameParams.cRows, mGame.xRows());
        lBundle.putInt(SelectGameParams.cColumns, mGame.xColumns());
        lInt = new Intent();
        lInt.setClass(this, SelectGameParams.class);
        lInt.putExtras(lBundle);
        mStartSetup.launch(lInt);
    }

    private void sSetupStart(int pRows, int pColumns) {
        mGame.xStartSetUp(pRows, pColumns);
        mBnrView.invalidate();
    }

    public void hSetupFinish(MenuItem pItem) {
        if (mGame.xFinishSetup()) {
            sStartGame();
        }
        mBnrView.invalidate();
    }

    private void sStartGame() {
        sSetStartTime();
        mGame.xStartGame();
        sSetHeader();
        mBnrView.setEnabled(true);
        mBnrView.invalidate();
    }

    public void hReset(MenuItem pItem) {
        sSetStartTime();
        mGame.xReset();
        mBnrView.invalidate();
    }

    public void hFieldCopy(MenuItem pItem) {
        mGame.xPlayFieldCopy();
        mBnrView.invalidate();
    }

    public void hFieldSwitch(MenuItem pItem) {
        AlertDialog lDialog;
        AlertDialog.Builder lBuilder;
        final String[] lItems;
        int lCountIn;
        int lCountOut;
        int[] lFieldIds;

        lFieldIds = mGame.xPlayFieldIds();
        lItems = new String[lFieldIds.length - 1];

        lCountOut = 0;
        for (lCountIn = 0; lCountIn < lFieldIds.length; lCountIn++){
            if (lFieldIds[lCountIn] != mGame.xCurrentFieldId()){
                lItems[lCountOut] = String.valueOf(lFieldIds[lCountIn]);
                lCountOut++;
            }
        }
        if (lItems.length > 1){
            lBuilder = new AlertDialog.Builder(this);
            lBuilder.setItems(lItems, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int pChoice) {
                    sSwitchPlayField(Integer.parseInt(lItems[pChoice]));
                }
            });
            lDialog = lBuilder.create();
            lDialog.show();
        } else {
            sSwitchPlayField(Integer.parseInt(lItems[0]));
        }
    }

    private void sSwitchPlayField(int pNewId){
        mGame.xSwitchPlayField(pNewId);
        mBnrView.invalidate();
    }

    public void hFieldDelete(MenuItem pItem) {
        mGame.xDeleteCurrentPlayField();
        mBnrView.invalidate();
    }
}