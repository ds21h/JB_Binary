package jb.games.binary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import jb.games.binary.game.BinaryGame;

public class BinaryView extends View {
    interface intBinaryView {
        void onSolved();
    }

    private final int cMargin = 10;
    private final int cButtonMargin = 20;
    private int mMargin = cMargin;

    private final Paint mPaint = new Paint();

    private final int cColorBackNorm = Color.argb(255, 255, 255, 245);
    private final int cColorBackFixed = Color.argb(255, 230, 230, 230);
    private final int cColorBackSel = Color.argb(255, 255, 255, 0);
    private final int cColorForeNorm = Color.BLACK;
    private final int cColorForeField = Color.RED;
    private final int cColorForeConflict = Color.RED;
    private final int cColorButtonNorm = Color.argb(255, 255, 255, 245);

    private final int cAlphaNorm = 255;
    private final int cAlphaDisabled = 100;

    private final int cStrokeNone = 0;
    private final int cStrokeNarrow = 3;

    private BinaryGame mGame = null;
    private float mCellSize;
    private float mButtonSize = 120;
    private RectF[] mButton = new RectF[2];
    private boolean mEnabled = true;
    private boolean mButtonsEnabled = false;
    private final Context mContext;

    private intBinaryView mIntView = null;

    public BinaryView(Context pContext) {
        super(pContext);
        mContext = pContext;
    }

    public BinaryView(Context pContext, AttributeSet pAttrSet) {
        super(pContext, pAttrSet);
        mContext = pContext;
    }

    public void setGame(BinaryGame pGame) {
        mGame = pGame;
    }

    public void setIntBinaryView(intBinaryView pIntView) {
        mIntView = pIntView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent pEvent) {
        float lDispl;
        int lColumn;
        int lRow;
        boolean lCellSelect;
        RectF lRect;
        int lCount;

        if (mGame != null && mEnabled) {
            if (pEvent.getAction() == MotionEvent.ACTION_DOWN && mCellSize > 0) {
                lCellSelect = false;
                lDispl = pEvent.getX() - mMargin;
                if (lDispl >= 0) {
                    lColumn = (int) (lDispl / mCellSize);
                    if (lColumn < mGame.xColumns()) {
                        lDispl = pEvent.getY() - cMargin;
                        if (lDispl >= 0) {
                            lRow = (int) (lDispl / mCellSize);
                            if (lRow < mGame.xRows()) {
                                lCellSelect = true;
                                mGame.xSelection(lRow, lColumn);
                                invalidate();
                            }
                        }
                    }
                }
                if (!lCellSelect) {
                    if (mButtonsEnabled) {
                        lRect = new RectF(pEvent.getX(), pEvent.getY(), pEvent.getX(), pEvent.getY());
                        if (mGame.xGameStatus() == BinaryGame.cStatusSetup || mGame.xGameStatus() == BinaryGame.cStatusPlay) {
                            for (lCount = 0; lCount < mButton.length; lCount++) {
                                if (mButton[lCount].contains(lRect)) {
                                    mGame.xProcessDigit(lCount);
                                    if (mGame.xGameStatus() == BinaryGame.cStatusSolved) {
                                        if (mIntView != null) {
                                            mIntView.onSolved();
                                        }
                                    }
                                    invalidate();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onDraw(Canvas pCanvas) {
        super.onDraw(pCanvas);
        float lCellHor;
        float lCellVert;

        if (mGame != null) {
            lCellHor = (getWidth() - (2 * cMargin)) / (float) mGame.xColumns();
            lCellVert = (getHeight() - cMargin - (3 * cButtonMargin) - mButtonSize) / (mGame.xRows() + 0.5F);
            if (lCellHor < lCellVert) {
                mCellSize = lCellHor;
                mMargin = cMargin;
            } else {
                mCellSize = lCellVert;
                mMargin = (int) ((getWidth() - (mGame.xColumns() * mCellSize)) / 2);
            }
            sDrawPlayField(pCanvas);
            if (mGame.xGameStatus() == BinaryGame.cStatusPlay
                    || mGame.xGameStatus() == BinaryGame.cStatusSetup) {
                sDrawButtons(pCanvas);
                mButtonsEnabled = true;
            } else {
                mButtonsEnabled = false;
            }
        }
    }

    private void sDrawPlayField(Canvas pCanvas) {
        int lRow;
        int lColumn;
        float lRowMargin;
        float lColumnMargin;
        RectF lRect;
        int lAlpha;

        if (mEnabled) {
            lAlpha = cAlphaNorm;
        } else {
            lAlpha = cAlphaDisabled;
        }
        for (lRow = 0; lRow < mGame.xRows(); lRow++) {
            lRowMargin = (lRow * mCellSize) + cMargin;
            for (lColumn = 0; lColumn < mGame.xColumns(); lColumn++) {
                lColumnMargin = (lColumn * mCellSize) + mMargin;
                lRect = new RectF(lColumnMargin, lRowMargin,
                        lColumnMargin + mCellSize, lRowMargin + mCellSize);

                //      Background
                mPaint.setStrokeWidth(cStrokeNone);
                mPaint.setStyle(Paint.Style.FILL);
                if (mGame.xIsCellFixed(lRow, lColumn)) {
                    mPaint.setColor(cColorBackFixed);
                } else {
                    if (mGame.xIsSelection(lRow, lColumn)) {
                        mPaint.setColor(cColorBackSel);
                    } else {
                        mPaint.setColor(cColorBackNorm);
                    }
                }
                mPaint.setAlpha(lAlpha);
                pCanvas.drawRect(lRect, mPaint);

                //      Cell value
                mPaint.setStyle(Paint.Style.FILL);
                if (mGame.xCellValue(lRow, lColumn) >= 0) {
                    //  Normal
                    mPaint.setTextSize(mCellSize * 0.8F);
                    mPaint.setTextAlign(Paint.Align.CENTER);
                    if (mGame.xCellConflict(lRow, lColumn)) {
                        mPaint.setColor(cColorForeConflict);
                    } else {
                        mPaint.setColor(cColorForeNorm);
                    }
                    mPaint.setAlpha(lAlpha);
                    pCanvas.drawText(String.valueOf(mGame.xCellValue(lRow, lColumn) ),
                            lRect.centerX(),
                            lRect.bottom - mPaint.getFontMetrics().descent,
                            mPaint);
                }

                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(cColorForeNorm);
                mPaint.setAlpha(lAlpha);
                mPaint.setStrokeWidth(cStrokeNarrow);
                pCanvas.drawLine(lColumnMargin, lRowMargin, lColumnMargin, lRowMargin + mCellSize, mPaint);

                mPaint.setStrokeWidth(cStrokeNarrow);
                pCanvas.drawLine(lColumnMargin + mCellSize, lRowMargin, lColumnMargin + mCellSize, lRowMargin + mCellSize, mPaint);

                mPaint.setStrokeWidth(cStrokeNarrow);
                pCanvas.drawLine(lColumnMargin, lRowMargin, lColumnMargin + mCellSize, lRowMargin, mPaint);

                mPaint.setStrokeWidth(cStrokeNarrow);
                pCanvas.drawLine(lColumnMargin, lRowMargin + mCellSize, lColumnMargin + mCellSize, lRowMargin + mCellSize, mPaint);
            }
        }
        if (mGame.xFieldCount() > 1) {
            mPaint.setStrokeWidth(cStrokeNarrow);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextSize(mCellSize * 0.4F);
            mPaint.setTextAlign(Paint.Align.RIGHT);
            mPaint.setColor(cColorForeField);
            mPaint.setAlpha(lAlpha);
            pCanvas.drawText(String.valueOf(mGame.xCurrentFieldId()), cMargin + (mCellSize * mGame.xColumns()), (mCellSize * mGame.xRows()) - mPaint.getFontMetrics().top + (cMargin * 2), mPaint);
            mPaint.setTextAlign(Paint.Align.CENTER);
        }
    }

    private void sDrawButtons(Canvas pCanvas) {
        int lAlpha;
        int lCount;
        RectF lRectF;

        if (mEnabled) {
            lAlpha = cAlphaNorm;
        } else {
            lAlpha = cAlphaDisabled;
        }
        for (lCount = 0; lCount < mButton.length; lCount++) {
            lRectF = sRectButton(lCount);
            mButton[lCount] = lRectF;
            mPaint.setStrokeWidth(cStrokeNone);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(cColorButtonNorm);
            mPaint.setAlpha(lAlpha);
            pCanvas.drawRect(lRectF, mPaint);

            mPaint.setColor(cColorForeNorm);
            mPaint.setStrokeWidth(cStrokeNarrow);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setAlpha(lAlpha);
            pCanvas.drawRect(lRectF, mPaint);

            mPaint.setColor(cColorForeNorm);
            mPaint.setTextSize(mButtonSize * 0.8F);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setAlpha(lAlpha);
            pCanvas.drawText(String.valueOf(lCount), lRectF.centerX(), lRectF.bottom - mPaint.getFontMetrics().descent, mPaint);
        }
    }

    private RectF sRectButton(int pButton) {
        RectF lRect;

        lRect = new RectF();
        lRect.left = cButtonMargin + (pButton * (mButtonSize + (2 * cButtonMargin)));
        lRect.top = getHeight() - (2 * cButtonMargin) - mButtonSize;
        lRect.right = lRect.left + mButtonSize;
        lRect.bottom = lRect.top + mButtonSize;

        return lRect;
    }
}
