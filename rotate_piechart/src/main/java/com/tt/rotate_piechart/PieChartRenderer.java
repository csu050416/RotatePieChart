package com.tt.rotate_piechart;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 标题: 渲染器
 * 描述：饼状图绘制在这个类完成
 * 作者:tengtao
 * 创建时间：2017/11/1 11:50
 */

public class PieChartRenderer {
    /**
     * 绘制标记
     * 1：初始入场动画绘制
     * 2：触摸旋转绘制
     * 3：惯性滑动旋转绘制
     * 4：选中栏调整偏移绘制
     */
    public final static int TAG_ENTRANCE_DRAW = 1;
    public final static int TAG_TOUCH_ROTATE_DRAW = 2;
    public final static int TAG_INERTIA_ROTATE_DRAW = 3;
    public final static int TAG_ADJUST_OFFSET_DRAW = 4;
    private float mInitStartAngle;
    /**
     * 当前绘制开始角度
     */
    private float mCurrentStartAngle = mInitStartAngle;
    private long mEntranceAnimationDuration;
    private long mOffsetAnimationDuration;
    private RotatePieChart mPieChart;
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPiePaint, mCirclePaint, mIndicatorPaint;
    private Path mIndicatorPath;
    private Interpolator mEntranceInterpolator = new DecelerateInterpolator();
    private Interpolator mOffsetInterpolator = new AccelerateInterpolator();
    private long mEntranceTime, mOffsetStartTime;
    private BasePieChartAdapter mChartAdapter;
    private RectF mRectF;
    /**
     * 标记是否中途取消了某个绘制
     */
    private boolean isCancel;
    private int mIndicatorAngle;
    private float mIndicatorHeightRadiusRate;
    private int mIndicatorColor;
    private float mStrokeWidth;
    private int mStrokeColor;
    private int mBackgroundColor;
    /**
     * 添加绘制线程
     */
    private HandlerThread mCanvasDrawThread;
    private Handler mCanvasDrawHandler;
    private Handler mMainThreadHandler;
    private int mCurrentPosition = -1;
    /**
     * 当前偏移值
     */
    private float mAnimationOffsetAngle;

    public PieChartRenderer(RotatePieChart chart, SurfaceHolder holder) {
        this.mPieChart = chart;
        this.mSurfaceHolder = holder;
        if (mCanvasDrawThread == null) {
            mCanvasDrawThread = new HandlerThread("canvasDraw");
        }
        mCanvasDrawThread.start();
        mCanvasDrawHandler = new CanvasDrawHandler(mCanvasDrawThread.getLooper());
        mIndicatorPath = new Path();
        //饼状图
        mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPiePaint.setStyle(Paint.Style.FILL);
        //圆环
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        //三角形指示
        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 画背景
     *
     * @param backgroundColor
     */
    public void drawBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        mCanvas = mSurfaceHolder.lockCanvas();
        //1.清除画面
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //2.画背景
        mCanvas.drawColor(backgroundColor);
        if (mCanvas != null) {
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    /**
     * 初始入场绘制
     */
    public void drawEntrance() {
        isCancel = false;
        Message message = Message.obtain();
        message.what = TAG_ENTRANCE_DRAW;
        mCanvasDrawHandler.sendMessage(message);
    }

    /**
     * 重置绘制开始角度
     */
    public void reSetStartAngle() {
        mCurrentStartAngle = mInitStartAngle;
    }

    /**
     * 绘制触摸旋转
     */
    public void drawTouchRotate(float startAngle) {
        isCancel = false;
        mCurrentStartAngle += startAngle;
        mCurrentStartAngle %= 360;
        Message message = Message.obtain();
        message.what = TAG_TOUCH_ROTATE_DRAW;
        mCanvasDrawHandler.sendMessage(message);
    }

    /**
     * 绘制惯性旋转
     */
    public void drawInertiaRotate(float inertiaInitAngle) {
        isCancel = false;
        Message message = Message.obtain();
        message.what = TAG_INERTIA_ROTATE_DRAW;
        message.obj = inertiaInitAngle;
        mCanvasDrawHandler.sendMessage(message);
    }

    /**
     * 绘制选中栏调整偏移
     */
    public void drawAdjustOffset() {
        isCancel = false;
        mOffsetStartTime = 0;
        float offsetAngle = getCurrentIndicatorOffsetAngle();
        Message message = Message.obtain();
        message.what = TAG_ADJUST_OFFSET_DRAW;
        message.obj = offsetAngle + mCurrentStartAngle;
        mCanvasDrawHandler.sendMessage(message);
    }

    public void cancelDraw() {
        isCancel = true;
    }

    /**
     * 获取当前矫正产生的偏移角度值
     *
     * @return
     */
    private float getCurrentIndicatorOffsetAngle() {
        mCurrentStartAngle %= 360;
        float calculateStartAngle = mCurrentStartAngle;
        //方便计算，处理下数据
        if (mCurrentStartAngle > 90) {
            calculateStartAngle = mCurrentStartAngle - 360;
        }
        if (mCurrentStartAngle < -270) {
            calculateStartAngle = 360 + mCurrentStartAngle;
        }
        for (int i = 0; i < mChartAdapter.mValueAngles.length; i++) {
            calculateStartAngle += mChartAdapter.mValueAngles[i];
            if (calculateStartAngle >= 90) {
                //当前居中需要偏移的角度
                return mChartAdapter.mValueAngles[i] / 2 - (calculateStartAngle - 90);
            }
        }
        return 0;
    }

    /**
     * 获取当前选中的位置（指示器指示的位置）
     *
     * @return
     */
    private int getCurrentPosition() {
        mCurrentStartAngle %= 360;
        float calculateStartAngle = mCurrentStartAngle;
        //方便计算，处理下数据
        if (mCurrentStartAngle > 90) {
            calculateStartAngle = mCurrentStartAngle - 360;
        }
        if (mCurrentStartAngle < -270) {
            calculateStartAngle = 360 + mCurrentStartAngle;
        }
        for (int i = 0; i < mChartAdapter.mValueAngles.length; i++) {
            calculateStartAngle += mChartAdapter.mValueAngles[i];
            if (calculateStartAngle > 90) {
                return i;
            }
        }
        return 0;
    }

    public void setEntranceAnimationDuration(long duration) {
        mEntranceAnimationDuration = duration;
    }

    public void setOffsetAnimationDuration(long duration) {
        mOffsetAnimationDuration = duration;
    }

    public void setIndicatorAngle(int angle) {
        this.mIndicatorAngle = angle;
    }

    public void setIndicatorColor(int color) {
        this.mIndicatorColor = color;
    }

    public void setIndicatorHeightRate(float heightRadiusRate) {
        this.mIndicatorHeightRadiusRate = heightRadiusRate;
    }

    public void setPieChartStrokeColor(int color) {
        this.mStrokeColor = color;
    }

    public void setPieChartStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
    }

    public void setEntranceInterpolator(Interpolator interpolator){
        mEntranceInterpolator = interpolator;
    }

    public void setOffsetInterpolator(Interpolator interpolator){
        mOffsetInterpolator = interpolator;
    }

    public void setStartAngle(int angle) {
        mInitStartAngle = angle;
        mCurrentStartAngle = angle;
    }

    /**
     * 线程释放
     */
    public void detachedFromWindow() {
        if(mCanvasDrawHandler != null)
            mCanvasDrawHandler.removeCallbacksAndMessages(null);
        if (mCanvasDrawThread != null) {
            mCanvasDrawThread.quit();
        }
        if(mMainThreadHandler != null) {
            mMainThreadHandler.removeCallbacksAndMessages(null);
            mMainThreadHandler = null;
        }
    }

    public class CanvasDrawHandler extends Handler {

        public CanvasDrawHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mChartAdapter == null) {
                mChartAdapter = mPieChart.getChartAdapter();
            }
            if (isCancel || mChartAdapter.mValueAngles == null
                    || mChartAdapter.mValueAngles.length == 0) {
                return;
            }
            int[] centerPoint = mPieChart.getCenterPoint();
            int radius = mPieChart.getPieChartRadius();
            mRectF = new RectF(centerPoint[0] - radius, centerPoint[1] - radius,
                    centerPoint[0] + radius, centerPoint[1] + radius);
            mCanvas = mSurfaceHolder.lockCanvas();
            //1.清除画面
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            //2.画背景
            mCanvas.drawColor(mBackgroundColor);
            //3.画饼状图
            switch (msg.what) {
                case TAG_ENTRANCE_DRAW:
                    if (mEntranceTime == 0) {
                        mEntranceTime = System.currentTimeMillis();
                    }
                    //获取当前动画总角度
                    float animationTotalAngle = PieChartUtils.getAnimationTotalAngle(mEntranceInterpolator,
                            mEntranceTime, 360, mEntranceAnimationDuration);
                    float startAngle = mCurrentStartAngle;
                    for (int i = 0; i < mChartAdapter.mValueAngles.length; i++) {
                        mPiePaint.setColor(mChartAdapter.getItemColor(i));
                        float angle = mChartAdapter.mValueAngles[i] * animationTotalAngle / 360;
                        mCanvas.drawArc(mRectF, startAngle, angle, true, mPiePaint);
                        startAngle += angle;
                    }
                    if (animationTotalAngle != 360) {
                        Message message = Message.obtain();
                        message.what = TAG_ENTRANCE_DRAW;
                        mCanvasDrawHandler.sendMessage(message);
                    } else {
                        //将第一个数据中间偏移到底部，在入场动画完成之后
                        float afterStartAngle = Math.abs(90 - mCurrentStartAngle)
                                - mChartAdapter.mValueAngles[0] / 2 + mCurrentStartAngle;
                        mOffsetStartTime = 0;
                        Message message = Message.obtain();
                        message.what = TAG_ADJUST_OFFSET_DRAW;
                        message.obj = afterStartAngle;
                        mCanvasDrawHandler.sendMessage(message);
                    }
                    break;
                case TAG_TOUCH_ROTATE_DRAW:
                    drawItemChart(mCurrentStartAngle);
                    break;
                case TAG_INERTIA_ROTATE_DRAW:
                    float inertiaInitAngle = (float) msg.obj;
                    mCurrentStartAngle += inertiaInitAngle;
                    mCurrentStartAngle %= 360;
                    drawItemChart(mCurrentStartAngle);
                    inertiaInitAngle /= 1.066f;
                    if (Math.abs(inertiaInitAngle) > 1) {
                        Message message = Message.obtain();
                        message.what = TAG_INERTIA_ROTATE_DRAW;
                        message.obj = inertiaInitAngle;
                        mCanvasDrawHandler.sendMessage(message);
                    } else {
                        mOffsetStartTime = 0;
                        float offsetAngle = getCurrentIndicatorOffsetAngle();
                        Message message = Message.obtain();
                        message.what = TAG_ADJUST_OFFSET_DRAW;
                        message.obj = offsetAngle + mCurrentStartAngle;
                        mCanvasDrawHandler.sendMessage(message);
                    }
                    break;
                case TAG_ADJUST_OFFSET_DRAW:
                    float afterStartAngle = (float) msg.obj;
                    if (mOffsetStartTime == 0) {
                        mOffsetStartTime = System.currentTimeMillis();
                        mAnimationOffsetAngle = 0;
                    }
                    //获取要偏移的角度
                    float offsetAngle = afterStartAngle - mCurrentStartAngle + mAnimationOffsetAngle;
                    //获取当前动画角度
                    mAnimationOffsetAngle = PieChartUtils.getAnimationTotalAngle(mOffsetInterpolator,
                            mOffsetStartTime, offsetAngle, mOffsetAnimationDuration);
                    //起始角度偏移量
                    mCurrentStartAngle = afterStartAngle - offsetAngle + mAnimationOffsetAngle;
                    drawItemChart(mCurrentStartAngle);
                    if (mAnimationOffsetAngle == offsetAngle) {
                        mOffsetStartTime = 0;
                        mAnimationOffsetAngle = 0;
                        mCurrentStartAngle = afterStartAngle;
                    } else {
                        Message message = Message.obtain();
                        message.what = TAG_ADJUST_OFFSET_DRAW;
                        message.obj = afterStartAngle;
                        mCanvasDrawHandler.sendMessage(message);
                    }
                    break;
                default:
                    break;
            }
            //4.画外框
            mCirclePaint.setStrokeWidth(mStrokeWidth);
            mCirclePaint.setColor(mStrokeColor);
            mCanvas.drawCircle(centerPoint[0], centerPoint[1], radius - mStrokeWidth / 2, mCirclePaint);
            //5.画指示三角
            mIndicatorPath.reset();
            float indicatorHeight = mPieChart.getPieChartRadius() * mIndicatorHeightRadiusRate;
            RectF rectF = new RectF(centerPoint[0] - radius, centerPoint[1] - radius,
                    centerPoint[0] + radius, centerPoint[1] + radius);
            mIndicatorPath.addArc(rectF, 90 - mIndicatorAngle / 2, mIndicatorAngle);
            mIndicatorPath.lineTo(centerPoint[0], centerPoint[1] + radius - indicatorHeight);
            mIndicatorPaint.setColor(mIndicatorColor);
            mCanvas.drawPath(mIndicatorPath, mIndicatorPaint);
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
            //当前指示位置回调，主线程
            final int position = getCurrentPosition();
            if (mCurrentPosition != position) {
                mCurrentPosition = position;
                if (mMainThreadHandler == null) {
                    mMainThreadHandler = new Handler(Looper.getMainLooper());
                }
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mChartAdapter.onSelected(position,
                                mChartAdapter.mPieChartDatas.get(position));
                    }
                });
            }
        }

        private void drawItemChart(float startAngle) {
            for (int i = 0; i < mChartAdapter.mValueAngles.length; i++) {
                mPiePaint.setColor(mChartAdapter.getItemColor(i));
                mCanvas.drawArc(mRectF, startAngle, mChartAdapter.mValueAngles[i], true, mPiePaint);
                startAngle += mChartAdapter.mValueAngles[i];
            }
        }
    }
}
