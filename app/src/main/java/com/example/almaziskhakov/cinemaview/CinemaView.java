package com.example.almaziskhakov.cinemaview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by almaziskhakov on 09/02/2017.
 */

public class CinemaView extends ViewGroup {

    private int mColumnCount;
    private int mRowCount;

    private Paint mGridPaint;

    private ChairCallback chairCallback;

    private ChairView[][] mChairs;

    public CinemaView(Context context) {
        super(context);
    }

    public CinemaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("cinema", "constructor");

        TypedArray a = context.obtainStyledAttributes(attrs,
            R.styleable.CinemaView, 0, 0);

        mColumnCount = a.getInteger(R.styleable.CinemaView_columnCount, 0);
        mRowCount = a.getInteger(R.styleable.CinemaView_rowCount, 0);
        a.recycle();

        mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setColor(Color.GRAY);
        mGridPaint.setStrokeWidth(4f);

        mChairs = new ChairView[mRowCount][mColumnCount];

        for (int row = mRowCount; row > 0; row--) {
            for (int column = 0; column <= mColumnCount; column++) {
                ChairView view = new ChairView(getContext(), row, column);
                if (column != 0) {
                    view.setOnClickListener(new OnClickListener() {
                        @Override public void onClick(View v) {
                            v.callOnClick();
                        }
                    });
                }
                addView(view);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("cinema", "onMeasure");
        int widthSize, heightSize;

        widthSize = getDefaultSize(0, widthMeasureSpec);
        heightSize = getDefaultSize(0, heightMeasureSpec);

        int majorDimension = Math.min(widthSize, heightSize);

        int blockDimension = majorDimension / (mColumnCount + 1);
        int blockSpec = MeasureSpec.makeMeasureSpec(blockDimension, MeasureSpec.EXACTLY);
        measureChildren(blockSpec, blockSpec);

        setMeasuredDimension(majorDimension, majorDimension / (mColumnCount + 1) * mRowCount);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.d("cinema", "dispatchDraw");
        super.dispatchDraw(canvas);

        //Draw the grid lines
        int width = getWidth() / (mColumnCount + 1);
        for (int i = 0; i <= getWidth(); i += width) {
            canvas.drawLine(i, 0, i, getHeight(), mGridPaint);
        }
        for (int i = 0; i <= width * mRowCount; i += width) {
            canvas.drawLine(0, i, getWidth(), i, mGridPaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("cinema", "onLayout");

        int row, col, left, top;
        Log.d("CHILD_COUNT", getChildCount() + "");
        for (int i = 0; i < getChildCount(); i++) {
            row = i / (mColumnCount + 1);
            col = i % (mColumnCount + 1);
            View child = getChildAt(i);
            left = col * child.getMeasuredWidth();
            top = row * child.getMeasuredHeight();

            child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
        }

    }

    @Override public void onDraw(Canvas canvas) {
        Log.d("cinema", "onDraw");
    }

    public int getColumnCount() {
        return mColumnCount;
    }

    public int getRowCount() {
        return mRowCount;
    }

    public ChairView[][] getChairs() {
        return mChairs;
    }

    public void clear() {
        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                mChairs[i][j].clear();
            }
        }
    }

    public void blockAllReserved() {
        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                if (mChairs[i][j].state == ChairView.STATE_RESERVED) {
                    mChairs[i][j].block();
                }
            }
        }
    }

    public void blockAllSold() {
        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                if (mChairs[i][j].state == ChairView.STATE_SOLD) {
                    mChairs[i][j].block();
                }
            }
        }
    }

    public void blockAllSoldAndReserved() {
        blockAllReserved();
        blockAllSold();
    }

    public void unblockAll() {
        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                if (!mChairs[i][j].blocked)
                    mChairs[i][j].unblock();
            }
        }
    }

    public void setChairCallback(ChairCallback chairCallback) {
        this.chairCallback = chairCallback;
    }

    public class ChairView extends View {

        private static final int STATE_DEFAULT  = -1;
        public static final  int STATE_FREE     = 0;
        public static final  int STATE_RESERVED = 1;
        public static final  int STATE_SOLD     = 2;

        int state = STATE_DEFAULT;

        private int row;
        private int column;

        private boolean blocked;

        public ChairView(Context context, int row, int column) {
            super(context);
            this.row = row;
            this.column = column;

            if (column != 0)
                mChairs[row - 1][column - 1] = this;
        }

        public ChairView(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ChairView, 0, 0);

            column = a.getInteger(R.styleable.ChairView_column, 0);
            row = a.getInteger(R.styleable.ChairView_row, 0);

            a.recycle();
        }

        public ChairView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override public boolean callOnClick() {

            if (chairCallback != null && column != 0) chairCallback.onChairClick(this, row, column);
            if (blocked) return false;

            switch (state) {
                case STATE_DEFAULT:
                    state = STATE_RESERVED;
                    setBackgroundColor(getResources().getColor(R.color.chairBlocked));
                    break;
                case STATE_FREE:
                    state = STATE_RESERVED;
                    setBackgroundColor(getResources().getColor(R.color.chairBlocked));
                    break;
                case STATE_RESERVED:
                    state = STATE_SOLD;
                    setBackgroundColor(getResources().getColor(R.color.chairSold));
                    break;
                case STATE_SOLD:
                    state = STATE_FREE;
                    setBackgroundColor(getResources().getColor(R.color.chairFree));
                    break;
                default:
                    break;
            }
            return false;
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            paint.setTextSize(getWidth() / 3);

            if (column == 0) {
                canvas.drawText(String.valueOf(row), getWidth() / 2.5f, getWidth() / 1.75f, paint);
            } else {
                if (state == STATE_DEFAULT) {
                    setBackgroundColor(getResources().getColor(R.color.chairFree));
                    state = STATE_FREE;
                }
                canvas.drawText(String.valueOf(column), getWidth() / 2.5f, getWidth() / 1.75f, paint);
            }
        }

        public void clear() {
            state = STATE_FREE;
            setBackgroundColor(getResources().getColor(R.color.chairFree));
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public boolean isBlocked() {
            return blocked;
        }

        public void block() {
            blocked = true;
        }

        public void unblock() {
            blocked = false;
        }
    }

    public interface ChairCallback {
        void onChairClick(ChairView view, int row, int column);
    }
}