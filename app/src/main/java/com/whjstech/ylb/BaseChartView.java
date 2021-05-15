package com.whjstech.ylb;



import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.ArrayList;
import java.util.List;


import static android.view.View.MeasureSpec.AT_MOST;

/**
 * Description k线图
 * Author puyantao
 * Email 1067899750@qq.com
 * Date 2018-12-4 10:43
 */

public abstract class BaseChartView extends ScrollAndScaleView {
    protected final float DEF_WIDTH = 650;
    protected final float DEF_HIGHT = 400;

    protected float mTranslateX = Float.MIN_VALUE;
    protected float mMainScaleY = 1;

    protected float mDataLen = 0;  //数据的长度

    protected float downX;
    protected float downY;
    protected float mMainMaxValue; //最大值
    protected float mMainMinValue;//最小值

    protected float mPointWidth = 10; //点的宽度

    protected float mOverScrollRange = 0;   //设置超出右方后可滑动的范围

    protected int mWidth; //试图宽
    protected int mHeight;//试图高

    protected int mMaxPointSize = 2400; //最多点的个数
    protected int mPointSize = 600; //默认点的个数
    protected int mMinPointSize = 200;//最少点的个数

    //点的索引
    protected int mStartIndex = 0;
    protected int mStopIndex = 0;
    protected int mGridRows = 8;
    protected int mGridColumns = 6;

    protected int mSelectedIndex;

    protected int mMainHeight;//主视图高
    protected int mMainWidth;//主视图高

    protected int mItemCount;//当前点的个数
    protected List<MyPoint> mPoints;

    protected Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //网格线画笔
    protected Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //背景

    protected CallOnClick mCallOnClick;//添加视图点击事件

    protected ValueAnimator mAnimator;

    protected long mAnimationDuration = 500;
    protected long mClickTime = 0; //点击时间
    protected OnSelectedChangedListener mOnSelectedChangedListener = null;


    protected int mBackgroundColor;
    protected int mLeftPadding = dp2px(25);  //左padding
    protected int mRightPadding = dp2px(25);//右padding
    protected int mTopPadding = dp2px(5);//距顶部距离;
    protected int mBottomPadding = dp2px(20);//距底部距离


    public BaseChartView(Context context) {
        super(context);
        initView();
    }

    public BaseChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BaseChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setOnViewClickListener(CallOnClick callOnClick) {
        this.mCallOnClick = callOnClick;
    }

    private void initView() {
        setWillNotDraw(false);
        mPoints = new ArrayList<>();

        mBackgroundColor = Color.parseColor("#2A2D4F");
        mBackgroundPaint.setColor(mBackgroundColor);

        mGridPaint.setColor(Color.parseColor("#15FFFFFF")); //网格线颜色
        mGridPaint.setStrokeWidth(dp2px(1));

        mDetector = new GestureDetectorCompat(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setDuration(mAnimationDuration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
    }


    //点击， 处理长安时间
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mClickTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                //一个点的时候滑动
                if (event.getPointerCount() == 1) {
                    //长按之后移动
                    if (isLongPress || !isClosePress) {
                        calculateSelectedX(event.getX());
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isClosePress) {
                    isLongPress = false;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!isClosePress) {
                    isLongPress = false;
                }
                invalidate();
                break;
        }
        this.mDetector.onTouchEvent(event);
        this.mScaleDetector.onTouchEvent(event);
        return true;
    }


    //抬起, 手指离开触摸屏时触发(长按、滚动、滑动时，不会触发这个手势)
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP: //双指点击时不会触发
                if (isClosePress) {
                    if (System.currentTimeMillis() - mClickTime < 500) {
                        downX = e.getX();
                        downY = e.getY();
                        if (downX > 0 && downX < mWidth) {
                            if (mCallOnClick != null) {
                                if (downY <= mMainHeight) {
                                    mCallOnClick.onMainViewClick();
                                }
                            }

                        }
                    }
                } else {
                    isClosePress = true;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {//横竖屏切换
        super.onConfigurationChanged(newConfig);
        mMainHeight = mHeight - mTopPadding - mBottomPadding;

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == AT_MOST && heightSpecMode == AT_MOST) {
            setMeasuredDimension((int) DEF_WIDTH, (int) DEF_HIGHT);
        } else if (widthSpecMode == AT_MOST) {
            setMeasuredDimension((int) DEF_WIDTH, heightSpecSize);
        } else if (heightSpecMode == AT_MOST) {
            setMeasuredDimension(widthSpecSize, (int) DEF_HIGHT);
        } else {
            setMeasuredDimension(widthSpecSize, heightSpecSize);
        }
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mMainWidth = mWidth - mLeftPadding - mRightPadding;
        mMainHeight = mHeight - mTopPadding - mBottomPadding;
        notifyChanged();

        setScaleValue();  //计算缩放率
        setTranslateXFromScrollX(mScrollX);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.mHeight = h;
        mMainHeight = h - mTopPadding - mBottomPadding;
        mMainWidth = w - mLeftPadding - mRightPadding;

        notifyChanged();

        setScaleValue();  //计算缩放率
        setTranslateXFromScrollX(mScrollX);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(mBackgroundColor); //绘制背景
        drawGird(canvas); //绘制网格线

    }

    //画表格
    private void drawGird(Canvas canvas) {
        //横向的grid
        float rowSpace = mMainHeight / (mGridRows - 1);
        for (int i = 0; i < mGridRows; i++) {
            canvas.drawLine(0, rowSpace * i + mTopPadding, mWidth,
                    rowSpace * i + mTopPadding, mGridPaint);
        }

        //纵向的grid
        float columnSpace = mMainWidth / (mGridColumns - 1);
        for (int i = 0; i < mGridColumns; i++) {
            canvas.drawLine(columnSpace * i + mLeftPadding, mTopPadding,
                    columnSpace * i + mRightPadding, mMainHeight, mGridPaint);

        }
    }


    private void calculateSelectedX(float x) {
        mSelectedIndex = indexOfTranslateX(xToTranslateX(x));
        Log.e("move mSelectedIndex:", "" + mSelectedIndex + "/mStartIndex:" + mStartIndex);
        if (mSelectedIndex < mStartIndex) {
            mSelectedIndex = mStartIndex;
        }
        if (mSelectedIndex > mStopIndex) {
            mSelectedIndex = mStopIndex;
        }

    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        int lastIndex = mSelectedIndex;
        calculateSelectedX(e.getX());
        if (lastIndex != mSelectedIndex) {
            onSelectedChanged(this, getItem(mSelectedIndex), mSelectedIndex);
        }
        invalidate();
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        setTranslateXFromScrollX(mScrollX);
        Log.e("scroll-change", "" + mScrollX);
    }

    @Override
    protected void onScaleChanged(float scale, float oldScale) {
        super.onScaleChanged(scale, oldScale);
        checkAndFixScrollX();
        setTranslateXFromScrollX(mScrollX);
    }


    @Override
    public int getMinScrollX() {
        return (int) -(mOverScrollRange / mScaleX);
    }

    @Override
    public int getMaxScrollX() {
        return Math.round(getMaxTranslateX() - getMinTranslateX());
    }


    //获取平移的最小值
    private float getMinTranslateX() {
        return -mDataLen + mWidth / mScaleX - mPointWidth / 2;
    }

    //获取平移的最大值
    private float getMaxTranslateX() {
        if (!isFullScreen()) { //数据不够时
            return getMinTranslateX();
        }
        return mPointWidth / 2;
    }


    //数据是否充满屏幕
    public boolean isFullScreen() {
        Log.i("mDataLen : ", mDataLen + "-->1");
        Log.i("mDataLen : ", mWidth + "-->mWidth");
        Log.i("mDataLen : ", mWidth / mScaleX + "-->mWidth / mScaleX");
        Log.i("mDataLen : ", mScaleX + "-->mScaleX");
        return mDataLen >= mWidth / mScaleX;
    }

    public float getXScale() {
        return mScaleX;
    }


    //计算Y轴位置
    public float getMainY(float value) {
        return (mMainMaxValue - value) * mMainScaleY + mTopPadding;
    }


    //重新计算并刷新线条
    public void notifyChanged() {
        if (mItemCount != 0) {
            mDataLen = (mItemCount - 1) * mPointWidth;
            checkAndFixScrollX();
            setTranslateXFromScrollX(mScrollX);

        } else {
            setScrollX(0);
        }
        invalidate();
    }

    //设置缩放率(并还原 当初设置)
    public void setScaleValue() {
        mScaleXMax = mWidth / (mMinPointSize * mPointWidth);
//        mScrollX = 0;
        mScaleX = 1;

        if (mPoints.size() < mMaxPointSize) {
            mScaleXMin = mWidth / mDataLen;
        } else {
            mScaleXMin = mWidth / (mMaxPointSize * mPointWidth);
        }
    }

    //根据索引获取实体
    public MyPoint getItem(int position) {
        if (mPoints != null && mItemCount != 0) {
            return mPoints.get(position);
        } else {
            return null;
        }
    }

    //根据索引索取x坐标
    public float getX(int position) {
        return position * mPointWidth;
    }


    //scrollX 转换为 TranslateX
    public void setTranslateXFromScrollX(int scrollX) {
        mTranslateX = scrollX + getMinTranslateX();
    }


    // view中的x转化为TranslateX
    public float xToTranslateX(float x) {
        return -mTranslateX + x / mScaleX;
    }

    //translateX转化为view中的x
    public float translateXtoX(float translateX) {
        return (translateX + mTranslateX) * mScaleX;
    }


    //转化成数组的索引
    public int indexOfTranslateX(float translateX) {
        return indexOfTranslateX(translateX, 0, mItemCount - 1);
    }

    /**
     * 二分查找当前值的index
     *
     * @return 返回当前位置
     */
    public int indexOfTranslateX(float translateX, int start, int end) {
        if (end == start) {
            return start;
        }
        if (end - start == 1) {
            float startValue = getX(start);
            float endValue = getX(end);
            return Math.abs(translateX - startValue) < Math.abs(translateX - endValue) ? start : end;
        }
        int mid = start + (end - start) / 2;
        float midValue = getX(mid);
        if (translateX < midValue) {
            return indexOfTranslateX(translateX, start, mid);
        } else if (translateX > midValue) {
            return indexOfTranslateX(translateX, mid, end);
        } else {
            return mid;
        }
    }


    //开始动画
    public void startAnimation() {
        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    //设置动画时间
    public void setAnimationDuration(long duration) {
        if (mAnimator != null) {
            mAnimator.setDuration(duration);
        }
    }

    //设置表格行数
    public void setGridRows(int gridRows) {
        if (gridRows < 1) {
            gridRows = 1;
        }
        mGridRows = gridRows;
    }

    //设置表格列数
    public void setGridColumns(int gridColumns) {
        if (gridColumns < 1) {
            gridColumns = 1;
        }
        mGridColumns = gridColumns;
    }


    //获取图的宽度
    public int getChartWidth() {
        return mWidth;
    }

    //是否长按
    public boolean isLongPress() {
        return (isLongPress || !isClosePress);
    }

    //获取选择索引
    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    //设置选择监听
    public void setOnSelectedChangedListener(OnSelectedChangedListener l) {
        this.mOnSelectedChangedListener = l;
    }

    public void onSelectedChanged(BaseChartView view, Object point, int index) {
        if (this.mOnSelectedChangedListener != null) {
            mOnSelectedChangedListener.onSelectedChanged(view, point, index);
        }
    }


    //设置超出右方后可滑动的范围
    public void setOverScrollRange(float overScrollRange) {
        if (overScrollRange < 0) {
            overScrollRange = 0;
        }
        mOverScrollRange = overScrollRange;
    }


    //设置每个点的宽度
    public void setPointWidth(float pointWidth) {
        mPointWidth = pointWidth;
    }


    //监听视图点击区域
    public interface CallOnClick {
        void onMainViewClick();
    }


    //选中点变化时的监听
    public interface OnSelectedChangedListener {
        /**
         * 当选点中变化时
         *
         * @param view  当前view
         * @param point 选中的点
         * @param index 选中点的索引
         */
        void onSelectedChanged(BaseChartView view, Object point, int index);
    }

    public float getDimension(@DimenRes int resId) {
        return getResources().getDimension(resId);
    }

    public int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(getContext(), resId);
    }


    public int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


}

