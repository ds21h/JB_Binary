package jb.games.binary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import jb.games.binary.databinding.GameParamsLayoutBinding;

public class SelectGameParams extends AppCompatActivity {
    private GameParamsLayoutBinding mBinding;

    static final String cRows = "Rows";
    static final String cColumns = "Columns";
    static final String cDifficulty = "Diff";

    EditText mEdtRows;
    EditText mEdtColumns;
    TextView mLbDiff;
    EditText mEdtDiff;

    boolean mComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = GameParamsLayoutBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        Intent lInt;
        Bundle lBundle;
        int lDiff;

        mEdtRows = mBinding.txtRows;
        mEdtColumns = mBinding.txtColumns;
        mLbDiff = mBinding.lbDiff;
        mEdtDiff = mBinding.txtDiff;
        if (savedInstanceState == null) {
            lInt = getIntent();
            lBundle = lInt.getExtras();
            if (lBundle == null) {
                finish();
            } else {
                mEdtRows.setText(String.valueOf(lBundle.getInt(cRows)));
                mEdtColumns.setText(String.valueOf(lBundle.getInt(cColumns)));
                lDiff = lBundle.getInt(cDifficulty, -1);
                if (lDiff < 0){
                    mLbDiff.setVisibility(View.INVISIBLE);
                    mEdtDiff.setVisibility(View.INVISIBLE);
                    mComplete = false;
                } else {
                    mLbDiff.setVisibility(View.VISIBLE);
                    mEdtDiff.setVisibility(View.VISIBLE);
                    mEdtDiff.setText(String.valueOf(lDiff));
                    mComplete = true;
                }
            }
        }
    }

    public void hParamSet(View pVw){
        Intent lInt;
        Bundle lBundle;
        int lRows;
        int lColumns;
        int lDiff = 0;
        boolean lError;

        lError = false;
        lRows = sReturnValue(mEdtRows);
        if (lRows < 4){
            lError = true;
            lRows = 4;
        } else {
            if (lRows > 12){
                lError = true;
                lRows = 12;
            } else {
                if (((lRows / 2) * 2) != lRows){
                    lError = true;
                    lRows = (lRows / 2) * 2;
                }
            }
        }
        lColumns = sReturnValue(mEdtColumns);
        if (lColumns < 4){
            lError = true;
            lColumns = 4;
        } else {
            if (lColumns > 10){
                lError = true;
                lColumns = 10;
            } else {
                if (((lColumns / 2) * 2) != lColumns){
                    lError = true;
                    lColumns = (lColumns / 2) * 2;
                }
            }
        }
        if (mComplete){
            lDiff = sReturnValue(mEdtDiff);
            if (lDiff < 1){
                lError = true;
                lDiff = 1;
            } else {
                if (lDiff > 5) {
                    lError = true;
                    lDiff = 5;
                }
            }
        }
        if (lError){
            mEdtRows.setText(String.valueOf(lRows));
            mEdtColumns.setText(String.valueOf(lColumns));
            if (mComplete){
                mEdtDiff.setText(String.valueOf(lDiff));
            }
            Toast.makeText(this, R.string.msg_adjusted, Toast.LENGTH_SHORT).show();

        } else {
            lInt = new Intent();
            lBundle = new Bundle();
            lBundle.putInt(cRows, lRows);
            lBundle.putInt(cColumns, lColumns);
            if (mComplete){
                lBundle.putInt(cDifficulty, lDiff);
            }
            lInt.putExtras(lBundle);
            setResult(RESULT_OK, lInt);
            finish();
        }
    }

    private int sReturnValue(EditText pText){
        String lValue;
        int lIntValue;

        lValue = pText.getText().toString();
        try {
            lIntValue = Integer.parseInt(lValue);
        } catch (NumberFormatException pExc){
            lIntValue = -1;
        }
        return lIntValue;
    }
}