package com.tt.rotatepiechart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.tt.rotate_piechart.BasePieChartAdapter;
import com.tt.rotate_piechart.PieChartRenderer;
import com.tt.rotate_piechart.PieChartUtils;
import com.tt.rotate_piechart.R;

/**
 * 标题：可滑动旋转的饼状图，仿随手记的一个自定义view
 * 描述：
 * 作者：tengtao
 * 创建时间：2017/11/1 11:15
 */

public class RotatePieChart extends SurfaceView implements SurfaceHolder.Callback,
        BasePieChartAdapter.Observer {
    /**
     * 饼状图的占比
     */
    private float mRatio;
    /**
     * 起始角度
     */
    private int mStartAngle;
    /**
     * 饼状图背景色
     */
    private int mBackgroundColor;
    /**
     * 指示器的角度
     */
    private int mIndicatorAngle;
    /**
     * 指示器高度与饼状图半径占比
     */
    private float mIndicatorHeightRate;
    /**
     * 指示器颜色
     */
    private int mIndicatorColor;
    /**
     * 入场动画时长
     */
    private int mEntranceDuration;
    /**
     * 偏移动画时长
     */
    private int mOffsetDuration;
    /**
     * 外框宽度
     */
    private float mOutsideStrokeWidth;
    /**
     * 外框颜色
     */
    private int mOutsideStrokeColor;
    private BasePieChartAdapter mChartAdapter;
    private int mCenterX, mCenterY;
    private int mRadius;
    private float preMoveX, preMoveY;
    private VelocityTracker mVelocityTracker;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private PieChartRenderer mChartRenderer;
    private Interpolator mEntranceInterpolator = new DecelerateInterpolator();
    private Interpolator mOffsetInterpolator = new AccelerateInterpolator();
    /**
     * surface是否创建完成
     */
    private boolean surfaceCreated;
    /**
     * 是否完成初始绘制
     */
    private boolean finishEntrance;

    public RotatePieChart(Context context) {
        this(context, null);
    }

    public RotatePieChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotatePieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RotatePieChart);
        mRatio = array.getFloat(R.styleable.RotatePieChart_PCRate, 0.9f);
        mStartAngle = array.getInt(R.styleable.RotatePieChart_PCStartAngle, -90);
        mBackgroundColor = array.getColor(R.styleable.RotatePieChart_PCBackgroundColor, Color.WHITE);
        mIndicatorAngle = array.getInt(R.styleable.RotatePieChart_PCIndicatorSpanAngle, 30);
        mIndicatorHeightRate = array.getFloat(R.styleable.RotatePieChart_PCIndicatorHeightRadiusRate, 0.4f);
        mIndicatorColor = array.getColor(R.styleable.RotatePieChart_PCIndicatorColor, 0xAAFFFFFF);
        mEntranceDuration = array.getInt(R.styleable.RotatePieChart_PCEntranceAnimationDuration, 600);
        mOffsetDuration = array.getInt(R.styleable.RotatePieChart_PCOffsetAnimationDuration, 300);
        mOutsideStrokeWidth = array.getDimension(R.styleable.RotatePieChart_PCOutsideStrokeWidth, 6f);
        mOutsideStrokeColor = array.getColor(R.styleable.RotatePieChart_PCOutsideStrokeColor, 0xFFDDDDDD);
        array.recycle();
        init();
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mChartRenderer = new PieChartRenderer(this, holder);
        mMinimumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        setPieChartBackgroundColor(mBackgroundColor);
        setIndicatorAngle(mIndicatorAngle);
        setIndicatorHeightRate(mIndicatorHeightRate);
        setIndicatorColor(mIndicatorColor);
        setEntranceAnimationDuration(mEntranceDuration);
        setOffsetAnimationDuration(mOffsetDuration);
        setOutsideStrokeWidth(mOutsideStrokeWidth);
        setOutsideStrokeColor(mOutsideStrokeColor);
        setStartAngle(mStartAngle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        mCenterX = MeasureSpec.getSize(widthMeasureSpec) / 2;
        mCenterY = MeasureSpec.getSize(heightMeasureSpec) / 2;
        mRadius = (int) (Math.min(width, height) * mRatio / 2);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mChartAdapter == null) {
            return true;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preMoveX = ev.getX();
                preMoveY = ev.getY();
                mChartRenderer.cancelDraw();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX();
                float moveY = ev.getY();
                boolean isOutsidePointMove =
                        PieChartUtils.distanceForTwoPoint(mCenterX, mCenterY, moveX, moveY) > mRadius;
                if (!isOutsidePointMove) {
                    //获取上一个点的角度
                    float preAngle = PieChartUtils.getPointAngle(mCenterX, mCenterY, preMoveX, preMoveY);
                    //获取当前点的角度
                    float moveAngle = PieChartUtils.getPointAngle(mCenterX, mCenterY, moveX, moveY);
                    mChartRenderer.drawTouchRotate(moveAngle - preAngle);
                }
                preMoveX = moveX;
                preMoveY = moveY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float upX = ev.getX();
                float upY = ev.getY();
                boolean isOutsidePointUp = PieChartUtils.distanceForTwoPoint(mCenterX, mCenterY, upX, upY) > mRadius;
                if (isOutsidePointUp) {
                    mChartRenderer.drawAdjustOffset();
                    break;
                }
                final VelocityTracker velocityTracker = mVelocityTracker;
                final int pointerId = ev.getPointerId(0);
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocityY = velocityTracker.getYVelocity(pointerId);
                final float velocityX = velocityTracker.getXVelocity(pointerId);
                if ((Math.abs(velocityY) > mMinimumFlingVelocity) || (Math.abs(velocityX) > mMinimumFlingVelocity)) {
                    onFling(ev, velocityX, velocityY);
                } else {
                    mChartRenderer.drawAdjustOffset();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 判断当前滑动速度，并计算惯性初始角度
     *
     * @param ev
     * @param velocityX
     * @param velocityY
     */
    private void onFling(MotionEvent ev, float velocityX, float velocityY) {
        //获取触点到中心点的线与水平线正方向的夹角
        float levelAngle = PieChartUtils.getPointAngle(mCenterX, mCenterY, ev.getX(), ev.getY());
        //获取象限
        int quadrant = PieChartUtils.getQuadrant(ev.getX() - mCenterX, ev.getY() - mCenterY);
        //到中心点距离
        float distance = PieChartUtils.distanceForTwoPoint(mCenterX, mCenterY, ev.getX(), ev.getY());
        //获取惯性绘制的初始角度
        float inertiaInitAngle = PieChartUtils.calculateAngleFromVelocity(velocityX, velocityY, levelAngle,
                quadrant, distance);
        if (Math.abs(inertiaInitAngle) > 1) {
            mChartRenderer.drawInertiaRotate(inertiaInitAngle);
        } else {
            mChartRenderer.drawAdjustOffset();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (mChartRenderer != null) {
            entranceRenderer();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mChartRenderer.drawBackgroundColor();
        surfaceCreated = true;
        if(mChartAdapter == null)
            return;
        if (!finishEntrance) {
            entranceRenderer();
        } else {
            mChartRenderer.drawTouchRotate(0);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceCreated = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mChartRenderer.detachedFromWindow();
    }

    /**
     * 设置数据适配器
     *
     * @param adapter
     */
    public void setPieChartAdapter(@NonNull BasePieChartAdapter adapter) {
        mChartAdapter = adapter;
        mChartAdapter.setObserver(this);
        if (surfaceCreated) {
            entranceRenderer();
        }
    }

    /**
     * 渲染入场动画
     */
    private void entranceRenderer(){
        mChartRenderer.reSetStartAngle();
        mChartRenderer.drawEntrance();
        finishEntrance = true;
    }

    /**
     * 返回中心点坐标
     *
     * @return
     */
    public int[] getCenterPoint() {
        return new int[]{mCenterX, mCenterY};
    }

    /**
     * 返回半径
     *
     * @return
     */
    public int getPieChartRadius() {
        return mRadius;
    }

    /**
     * 获取数据适配器
     *
     * @return
     */
    public BasePieChartAdapter getChartAdapter() {
        return mChartAdapter;
    }

    /**
     * 设置指示器角度
     *
     * @param angle
     */
    public RotatePieChart setIndicatorAngle(int angle) {
        this.mIndicatorAngle = angle;
        return this;
    }

    public int getIndicatorAngle() {
        return mIndicatorAngle;
    }

    /**
     * @param color
     */
    public RotatePieChart setPieChartBackgroundColor(int color) {
        this.mBackgroundColor = color;
        return this;
    }

    public int getPieChartBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * 设置指示器相对饼状图半径的比例
     *
     * @param indicatorHeightRate
     */
    public RotatePieChart setIndicatorHeightRate(float indicatorHeightRate) {
        this.mIndicatorHeightRate = indicatorHeightRate;
        return this;
    }

    public float getIndicatorHeightRate() {
        return mIndicatorHeightRate;
    }

    /**
     * 设置指示器颜色
     *
     * @param indicatorColor
     */
    public RotatePieChart setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
        return this;
    }

    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    /**
     * 设置入场动画时长
     *
     * @param duration
     */
    public RotatePieChart setEntranceAnimationDuration(int duration) {
        this.mEntranceDuration = duration;
        return this;
    }

    public int getEntranceDuration() {
        return mEntranceDuration;
    }

    /**
     * 设置矫正偏移动画时长
     *
     * @param duration
     */
    public RotatePieChart setOffsetAnimationDuration(int duration) {
        this.mOffsetDuration = duration;
        return this;
    }

    public int getOffsetDuration() {
        return mOffsetDuration;
    }

    /**
     * 返回外边框宽度
     *
     * @return
     */
    public RotatePieChart setOutsideStrokeWidth(float strokeWidth) {
        this.mOutsideStrokeWidth = strokeWidth;
        return this;
    }

    public float getOutsideStrokeWidth() {
        return mOutsideStrokeWidth;
    }

    /**
     * 设置外边框颜色
     *
     * @param strokeColor
     */
    public RotatePieChart setOutsideStrokeColor(int strokeColor) {
        this.mOutsideStrokeColor = strokeColor;
        return this;
    }

    public int getOutsideStrokeColor() {
        return mOutsideStrokeColor;
    }

    /**
     * 设置绘制起始角度
     *
     * @param angle
     */
    public RotatePieChart setStartAngle(int angle) {
        this.mStartAngle = angle;
        return this;
    }

    public int getStartAngle() {
        return mStartAngle;
    }

    /**
     * 设置入场动画插值器
     *
     * @param interpolator
     * @return
     */
    public RotatePieChart setEntranceInterpolator(Interpolator interpolator){
        this.mEntranceInterpolator = interpolator;
        return this;
    }

    public Interpolator getEntranceInterpolator() {
        return mEntranceInterpolator;
    }

    /**
     * 设置偏移动画插值器
     *
     * @param interpolator
     * @return
     */
    public RotatePieChart setOffsetInterpolator(Interpolator interpolator){
        this.mOffsetInterpolator = interpolator;
        return this;
    }

    public Interpolator getOffsetInterpolator() {
        return mOffsetInterpolator;
    }
}
