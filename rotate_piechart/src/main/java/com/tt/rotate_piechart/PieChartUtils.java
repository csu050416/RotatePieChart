package com.tt.rotate_piechart;

import android.content.Context;
import android.view.animation.Interpolator;

/**
 * 标题: 工具类
 * 描述：
 * 作者:滕涛
 * 创建时间：2017/10/31 17:10
 */

public class PieChartUtils {

    /**
     * 弧度换算成角度
     *
     * @return
     */
    public static float radianToAngle(double radian) {
        return (float) (radian * 180 / Math.PI);
    }


    /**
     * 角度换算成弧度
     *
     * @param angle
     * @return
     */
    public static float angleToRadian(double angle) {
        return (float) (angle * Math.PI / 180);
    }

    /**
     * dp转px
     */
    public static int dp2px(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    /**
     * 获取象限
     * @return 象限值
     */
    public static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 4 : 1;
        }
        return y >= 0 ? 3 : 2;
    }

    /**
     * 两点之间的距离
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static float distanceForTwoPoint(float x1, float y1, float x2, float y2) {
        float distanceX = Math.abs(x1 - x2);
        float distanceY = Math.abs(y1 - y2);
        return (float) Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
    }

    /**
     * 获取点的角度
     * 余弦定理是指三角形中任何一边的平方等于其它两边的平方和减去这两边与它们夹角的余弦的积的2倍，即 a2=b2+c2-2bccosA.
     *
     * @param centerX 基准点x
     * @param centerY 基准的y
     * @param x       当前点x
     * @param y       当前点y
     * @return
     */
    public static float getPointAngle(float centerX, float centerY, float x, float y) {
        float distance = distanceForTwoPoint(centerX, centerY, x, y);
        double angle = 0;//弧度
        if (x > centerX && y < centerY) {//第一象限
            angle = Math.PI * 2 - Math.asin((centerY - y) / distance);
        } else if (x < centerX && y < centerY) {//第二象限
            angle = Math.PI + Math.asin((centerY - y) / distance);
        } else if (x < centerX && y > centerY) {//第三象限
            angle = Math.PI - Math.asin((y - centerY) / distance);
        } else if (x > centerX && y > centerY) {//第四象限
            angle = Math.asin((y - centerY) / distance);
        }
        return radianToAngle(angle);//转角度
    }

    /**
     * 计算圆切线速度
     *
     * @param velocityX
     * @param velocityY
     * @param levelAngle 触点到中心点的线与水平线正方向的夹角
     * @param quadrant 象限
     * @param distance 触点到中心店的距离
     */
    public static float calculateVelocityAngle(float velocityX, float velocityY, float levelAngle, int quadrant, float distance) {
        //转换成与水平线的夹角
        if (levelAngle > 270) {
            levelAngle = 360 - levelAngle;
        } else if (levelAngle > 90 && levelAngle < 180) {
            levelAngle = 180 - levelAngle;
        } else {
            levelAngle = levelAngle % 90;
        }
        //获得线速度与水平夹角,即矢量速度与水平方向的夹角
        float lineSpeed = (float) Math.sqrt(Math.pow(velocityX, 2) + Math.pow(velocityY, 2));
        float vectorAngle = PieChartUtils.radianToAngle(Math.asin(Math.abs(velocityY) / lineSpeed));
        if (vectorAngle == levelAngle) {//不需要惯性旋转
            return 0;
        }
        //圆切速度与线速度的夹角
        float circleLineDegree;
        boolean isCW;//是否顺时针
        if(quadrant == 4){
            if(velocityX > 0 && velocityY < 0){
                isCW = false;
                circleLineDegree = vectorAngle > levelAngle ? vectorAngle - levelAngle : 90 - vectorAngle - levelAngle;
            }else if(velocityX > 0 && velocityY > 0){
                isCW = vectorAngle > levelAngle;
                circleLineDegree = vectorAngle > levelAngle ? 90 - vectorAngle + levelAngle : 90 + vectorAngle - levelAngle;
            } else if(velocityX < 0 && velocityY < 0){
                isCW = vectorAngle < levelAngle;
                circleLineDegree = vectorAngle > levelAngle ? 90 - vectorAngle + levelAngle : 90 + vectorAngle - levelAngle;
            }else {
                isCW = true;
                circleLineDegree = vectorAngle > levelAngle ? vectorAngle + levelAngle - 90 : 90 - vectorAngle - levelAngle;
            }
        }else if(quadrant == 3){
            if(velocityX > 0 && velocityY < 0){
                isCW = vectorAngle > levelAngle;
                circleLineDegree = vectorAngle > levelAngle ? 90 - vectorAngle + levelAngle : 90 + vectorAngle - levelAngle;
            }else if(velocityX > 0 && velocityY > 0){
                isCW = false;
                circleLineDegree = vectorAngle > levelAngle ? vectorAngle - levelAngle : vectorAngle + levelAngle;
            } else if(velocityX < 0 && velocityY > 0){
                isCW = vectorAngle < levelAngle;
                circleLineDegree = vectorAngle > levelAngle ? 90 - vectorAngle + levelAngle : 90 + vectorAngle - levelAngle;
            }else{
                isCW = true;
                circleLineDegree = vectorAngle > levelAngle ? vectorAngle + levelAngle - 90 : 90 - vectorAngle - levelAngle;
            }
        } else if(quadrant == 2){
            if(velocityX > 0 && velocityY < 0){
                isCW = true;
                circleLineDegree = vectorAngle > levelAngle ? vectorAngle + levelAngle - 90 : 90 - vectorAngle - levelAngle;
            }else if(velocityX > 0 && velocityY > 0){
                isCW = vectorAngle < levelAngle;
                circleLineDegree = vectorAngle > levelAngle ? 90 - vectorAngle + levelAngle : 90 + vectorAngle - levelAngle;
            } else if(velocityX < 0 && velocityY > 0){
                isCW = false;
                circleLineDegree = vectorAngle > levelAngle ? vectorAngle - levelAngle : 90 - vectorAngle - levelAngle;
            }else {
                isCW = vectorAngle > levelAngle;
                circleLineDegree = vectorAngle > levelAngle ? 90 - vectorAngle + levelAngle : 90 + vectorAngle - levelAngle;
            }
        } else {
            if(velocityX > 0 && velocityY < 0){
                isCW = vectorAngle < levelAngle;
                circleLineDegree = vectorAngle > levelAngle ? 90 - vectorAngle + levelAngle : 90 + vectorAngle - levelAngle;
            }else if(velocityX > 0 && velocityY > 0){
                isCW = true;
                circleLineDegree = vectorAngle > levelAngle ? vectorAngle + levelAngle - 90 : 90 - vectorAngle - levelAngle;
            } else if(velocityX < 0 && velocityY > 0){
                isCW = vectorAngle > levelAngle;
                circleLineDegree = vectorAngle > levelAngle ? 90 - vectorAngle + levelAngle : 90 + vectorAngle - levelAngle;
            }else{
                isCW = false;
                circleLineDegree = vectorAngle > levelAngle ? vectorAngle - levelAngle : vectorAngle + levelAngle;
            }
        }

        //计算圆切速度,通过弧度计算，角度要先转换为弧度
        double circleSpeed = Math.abs(lineSpeed * Math.cos(circleLineDegree));
        //角速度w与线速度v的关系:  wr = v
        return (float) (circleSpeed / distance * (isCW ? 1 : -1));//角速度;
    }

    /**
     * 获取初始进入动画动态总角度
     *
     * @param interpolator 插值器
     * @param judgeValue  判断值
     * @param targetValue 目标值
     * @param duration    时长
     * @return
     */
    public static float getAnimationTotalAngle(Interpolator interpolator, long judgeValue, float targetValue, long duration) {
        float percent = (System.currentTimeMillis() - judgeValue) * 1.0f / duration;
        percent = percent > 1 ? 1 : percent;
        return interpolator.getInterpolation(percent) * targetValue;
    }
}
