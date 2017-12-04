package com.tt.rotate_piechart;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * 标题: 饼状图数据适配器
 * 描述：
 * 作者:tengtao
 * 创建时间：2017/11/1 11:44
 */

public abstract class BasePieChartAdapter<T> {
    /**
     * 数据集合
     */
    protected List<T> mPieChartDatas;
    /**
     * 角度
     */
    protected float[] mValueAngles;
    private Observer mObserver;

    public BasePieChartAdapter(@NonNull List<T> datas) {
        this.mPieChartDatas = datas;
        this.mValueAngles = new float[datas.size()];
        if (datas.size() > 0) {
            valueConvertToAngle();
        }
    }

    /**
     * 获取数据对应所占比的角度数组
     *
     * @return
     */
    private void valueConvertToAngle() {
        float totalValue = sumDataValue(mPieChartDatas);
        float totalAngle = 0;
        for (int i = 0; i < mPieChartDatas.size(); i++) {
            mValueAngles[i] = getJudgeData(mPieChartDatas.get(i)) * 360 * 1.0f / totalValue;
            totalAngle += mValueAngles[i];
        }
        //防止总角度小于360，暂且加在最后一个上
        if (totalAngle < 360) {
            float offsetAngle = 360 - totalAngle;
            mValueAngles[mPieChartDatas.size() - 1] += offsetAngle;
        }
        //超过部分，减在最后一个上
        if (totalAngle > 360) {
            float offsetAngle = totalAngle - 360;
            mValueAngles[mPieChartDatas.size() - 1] -= offsetAngle;
        }
    }

    /**
     * 获取总值
     *
     * @param list
     * @return
     */
    private float sumDataValue(List<T> list) {
        float total = 0;
        for (T bean : list) {
            total += getJudgeData(bean);
        }
        return total;
    }

    /**
     * 数据更新
     */
    public void notifyDatasetChanged() {
        valueConvertToAngle();
        if (mObserver != null) {
            mObserver.notifyDataSetChanged();
        }
    }

    /**
     * 注册观察者对象
     */
    public void setObserver(Observer observer) {
        mObserver = observer;
    }

    /**
     * 占比数据获取
     *
     * @param bean
     * @return
     */
    protected abstract float getJudgeData(T bean);


    /**
     * 获取颜色
     *
     * @param position
     * @return
     */
    protected abstract int getItemColor(int position);

    /**
     * 当前选中
     *
     * @param position
     * @param data
     */
    protected void onSelected(int position, T data) {
    }

    public interface Observer {

        /**
         * 更新数据
         */
        void notifyDataSetChanged();
    }
}
