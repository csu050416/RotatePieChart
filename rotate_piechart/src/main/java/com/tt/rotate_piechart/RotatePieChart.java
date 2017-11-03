package com.tt.rotate_piechart;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * 标题：可滑动旋转的饼状图，仿随手记的一个自定义view
 * 描述：
 * 作者：tengtao
 * 创建时间：2017/11/1 11:15
 */

public class RotatePieChart extends SurfaceView implements SurfaceHolder.Callback, PieChartAdapter.Observer {
    private float mRatio = 0.9f;//饼状图的占比
    private PieChartAdapter mChartAdapter;
    private int mCenterX, mCenterY;
    private int mRadius;//半径
    private float preMoveX, preMoveY;
    //手势监听
    private VelocityTracker mVelocityTracker;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private SurfaceHolder mHolder;
    private PieChartRenderer mChartRenderer;
    private int mBackgroundColor = Color.WHITE;
    private boolean surfaceCreated;//surface是否创建完成
    private boolean finishEntrance;//是否完成初始绘制

    public RotatePieChart(Context context) {
        this(context, null);
    }

    public RotatePieChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotatePieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mChartRenderer = new PieChartRenderer(getContext(), this, mHolder);
        mMinimumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
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
        if(mChartAdapter == null)
            return true;
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                preMoveX = ev.getX();
                preMoveY = ev.getY();
                mChartRenderer.cancelDraw();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX();
                float moveY = ev.getY();
                boolean isOutsidePointMove = PieChartUtils.distanceForTwoPoint(mCenterX, mCenterY, moveX, moveY) > mRadius;
                if (!isOutsidePointMove) {
                    float preAngle = PieChartUtils.getPointAngle(mCenterX, mCenterY, preMoveX, preMoveY);//获取上一个点的角度
                    float moveAngle = PieChartUtils.getPointAngle(mCenterX, mCenterY, moveX, moveY);//获取当前点的角度
                    mChartRenderer.drawTouchRotate(moveAngle - preAngle);
                }
                preMoveX = moveX;
                preMoveY = moveY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                final VelocityTracker velocityTracker = mVelocityTracker;
                final int pointerId = ev.getPointerId(0);
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocityY = velocityTracker.getYVelocity(pointerId);
                final float velocityX = velocityTracker.getXVelocity(pointerId);
                if ((Math.abs(velocityY) > mMinimumFlingVelocity) || (Math.abs(velocityX) > mMinimumFlingVelocity)){
                    onFling(ev, velocityX, velocityY);
                } else{
                    mChartRenderer.drawAdjustOffset();
                }
                break;

        }
        return true;
    }

    /**
     * 判断当前滑动速度，并计算惯性初始角度
     * @param ev
     * @param velocityX
     * @param velocityY
     */
    private void onFling(MotionEvent ev, float velocityX, float velocityY){
        //获取触点到中心点的线与水平线正方向的夹角
        float levelAngle = PieChartUtils.getPointAngle(mCenterX, mCenterY, ev.getX(), ev.getY());
        //获取象限
        int quadrant = PieChartUtils.getQuadrant(ev.getX() - mCenterX, ev.getY() - mCenterY);
        //到中心点距离
        float distance = PieChartUtils.distanceForTwoPoint(mCenterX, mCenterY, ev.getX(), ev.getY());
        //获取惯性绘制的初始角度
        float inertiaInitAngle = PieChartUtils.calculateAngleFromVelocity(velocityX, velocityY, levelAngle, quadrant, distance);
        if (Math.abs(inertiaInitAngle) > 1) {
            mChartRenderer.drawInertiaRotate(inertiaInitAngle);
        } else {
            mChartRenderer.drawAdjustOffset();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(mChartRenderer != null) {
            mChartRenderer.reSetStartAngle();
            mChartRenderer.drawEntrance();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mChartRenderer.drawBackgroundColor(mBackgroundColor);
        surfaceCreated = true;
        if(!finishEntrance && mChartAdapter != null)
            mChartRenderer.drawEntrance();
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
    public void setPieChartAdapter(@NonNull PieChartAdapter adapter) {
        mChartAdapter = adapter;
        mChartAdapter.setObserver(this);
        if(surfaceCreated) {
            mChartRenderer.drawEntrance();
            finishEntrance = true;
        }
    }

    /**
     * 返回中心点坐标
     * @return
     */
    public int[] getCenterPoint(){
        return new int[]{mCenterX, mCenterY};
    }

    /**
     * 返回半径
     * @return
     */
    public int getPieChartRadius(){
        return mRadius;
    }

    /**
     * 获取数据适配器
     * @return
     */
    public PieChartAdapter getChartAdapter(){
        return mChartAdapter;
    }

    /**
     * 设置指示器角度
     * @param angle
     */
    public RotatePieChart setIndicatorAngle(int angle){
        mChartRenderer.setIndicatorAngle(angle);
        return this;
    }

    /**
     *
     * @param color
     */
    public RotatePieChart setPieChartBackgroundColor(int color){
        mBackgroundColor = color;
        return this;
    }

    /**
     * 设置指示器高度
     * @param indicatorHeight
     */
    public RotatePieChart setIndicatorHeight(int indicatorHeight){
        mChartRenderer.setIndicatorHeight(indicatorHeight);
        return this;
    }

    /**
     * 设置指示器颜色
     * @param indicatorColor
     */
    public RotatePieChart setIndicatorColor(int indicatorColor){
        mChartRenderer.setIndicatorColor(indicatorColor);
        return this;
    }

    /**
     * 设置入场动画时长
     * @param duration
     */
    public RotatePieChart setEntranceAnimationDuration(long duration){
        mChartRenderer.setEntranceAnimationDuration(duration);
        return this;
    }

    /**
     * 设置矫正偏移动画时长
     * @param duration
     */
    public RotatePieChart setOffsetAnimationDuration(long duration){
        mChartRenderer.setOffsetAnimationDuration(duration);
        return this;
    }

    /**
     * 返回外边框宽度
     * @return
     */
    public RotatePieChart setOutsideStrokeWidth(int strokeWidth){
        mChartRenderer.setPieChartStrokeWidth(strokeWidth);
        return this;
    }

    /**
     * 设置外边框颜色
     * @param strokeColor
     */
    public RotatePieChart setOutsideStrokeColor(int strokeColor){
        mChartRenderer.setPieChartStrokeColor(strokeColor);
        return this;
    }

    /**
     * 设置绘制起始角度
     * @param angle
     */
    public RotatePieChart setStartAngle(int angle){
        mChartRenderer.setStartAngle(angle);
        return this;
    }
}
