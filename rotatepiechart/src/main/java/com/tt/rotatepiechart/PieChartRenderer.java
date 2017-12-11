package com.tt.rotatepiechart;

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

import com.tt.rotate_piechart.BasePieChartAdapter;

/**
 * 标题: 渲染器
 * 描述：饼状图绘制在这个类完成
 * 作者:tengtao
 * 创建时间：2017/11/1 11:50
 */

public class PieChartRenderer<T> {
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
    /**
     * 当前绘制开始角度
     */
    private float mCurrentStartAngle;
    private com.tt.rotatepiechart.RotatePieChart mPieChart;
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPiePaint, mCirclePaint, mIndicatorPaint;
    private Path mIndicatorPath;
    private long mEntranceTime, mOffsetStartTime;
    private BasePieChartAdapter<T> mChartAdapter;
    private RectF mRectF;
    /**
     * 标记是否中途取消了某个绘制
     */
    private boolean isCancel;
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

    public PieChartRenderer(com.tt.rotatepiechart.RotatePieChart chart, SurfaceHolder holder) {
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
     */
    public void drawBackgroundColor() {
        mCanvas = mSurfaceHolder.lockCanvas();
        clearPieChart();
        if (mCanvas != null) {
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    /**
     * 清除饼状图画面
     */
    private void clearPieChart() {
        //1.清除画面
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //2.画背景
        mCanvas.drawColor(mPieChart.getPieChartBackgroundColor());
    }

    /**
     * 初始入场绘制
     */
    public void drawEntrance() {
        isCancel = false;
        mEntranceTime = 0;
        Message message = Message.obtain();
        message.what = TAG_ENTRANCE_DRAW;
        mCanvasDrawHandler.sendMessage(message);
    }

    /**
     * 重置绘制开始角度
     */
    public void reSetStartAngle() {
        mCurrentStartAngle = mPieChart.getStartAngle();
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
        for (int i = 0; i < mChartAdapter.mValueAngles.size(); i++) {
            calculateStartAngle += mChartAdapter.mValueAngles.get(i);
            if (calculateStartAngle >= 90) {
                //当前居中需要偏移的角度
                return mChartAdapter.mValueAngles.get(i) / 2 - (calculateStartAngle - 90);
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
        for (int i = 0; i < mChartAdapter.mValueAngles.size(); i++) {
            calculateStartAngle += mChartAdapter.mValueAngles.get(i);
            if (calculateStartAngle > 90) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 线程释放
     */
    public void detachedFromWindow() {
        if (mCanvasDrawHandler != null)
            mCanvasDrawHandler.removeCallbacksAndMessages(null);
        if (mCanvasDrawThread != null) {
            mCanvasDrawThread.quit();
        }
        if (mMainThreadHandler != null) {
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
                    || mChartAdapter.mValueAngles.size() == 0) {
                return;
            }
            int[] centerPoint = mPieChart.getCenterPoint();
            int radius = mPieChart.getPieChartRadius();
            mRectF = new RectF(centerPoint[0] - radius, centerPoint[1] - radius,
                    centerPoint[0] + radius, centerPoint[1] + radius);
            mCanvas = mSurfaceHolder.lockCanvas();
            clearPieChart();
            //3.画饼状图
            switch (msg.what) {
                case TAG_ENTRANCE_DRAW:
                    if (mEntranceTime == 0) {
                        mEntranceTime = System.currentTimeMillis();
                    }
                    //获取当前动画总角度
                    float animationTotalAngle = com.tt.rotatepiechart.PieChartUtils.getAnimationTotalAngle(mPieChart
                                    .getEntranceInterpolator(),
                            mEntranceTime, 360, mPieChart.getEntranceDuration());
                    float startAngle = mCurrentStartAngle;
                    for (int i = 0; i < mChartAdapter.mValueAngles.size(); i++) {
                        mPiePaint.setColor(mChartAdapter.getItemColor(i));
                        float angle = mChartAdapter.mValueAngles.get(i) * animationTotalAngle / 360;
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
                                - mChartAdapter.mValueAngles.get(0) / 2 + mCurrentStartAngle;
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
                    mAnimationOffsetAngle = com.tt.rotatepiechart.PieChartUtils.getAnimationTotalAngle(mPieChart.getOffsetInterpolator(),
                            mOffsetStartTime, offsetAngle, mPieChart.getOffsetDuration());
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
            float strokeWidth = mPieChart.getOutsideStrokeWidth();
            mCirclePaint.setStrokeWidth(strokeWidth);
            mCirclePaint.setColor(mPieChart.getOutsideStrokeColor());
            mCanvas.drawCircle(centerPoint[0], centerPoint[1], radius - strokeWidth / 2, mCirclePaint);
            //5.画指示三角
            mIndicatorPath.reset();
            float indicatorHeight = mPieChart.getPieChartRadius() * mPieChart.getIndicatorHeightRate();
            RectF rectF = new RectF(centerPoint[0] - radius, centerPoint[1] - radius,
                    centerPoint[0] + radius, centerPoint[1] + radius);
            int indicatorAngle = mPieChart.getIndicatorAngle();
            mIndicatorPath.addArc(rectF, 90 - indicatorAngle / 2, indicatorAngle);
            mIndicatorPath.lineTo(centerPoint[0], centerPoint[1] + radius - indicatorHeight);
            mIndicatorPaint.setColor(mPieChart.getIndicatorColor());
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
                        float percent = mChartAdapter.mValueAngles.get(position) * 100 / 360;
                        mChartAdapter.onSelected(position, com.tt.rotatepiechart.PieChartUtils.round(percent, 2),
                                mChartAdapter.mPieChartDatas.get(position));
                    }
                });
            }
        }

        private void drawItemChart(float startAngle) {
            for (int i = 0; i < mChartAdapter.mValueAngles.size(); i++) {
                mPiePaint.setColor(mChartAdapter.getItemColor(i));
                mCanvas.drawArc(mRectF, startAngle, mChartAdapter.mValueAngles.get(i), true, mPiePaint);
                startAngle += mChartAdapter.mValueAngles.get(i);
            }
        }
    }
}
