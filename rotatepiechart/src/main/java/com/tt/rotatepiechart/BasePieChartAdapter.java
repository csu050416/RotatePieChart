package com.tt.rotatepiechart;

import android.support.annotation.NonNull;
import android.util.SparseArray;

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
    protected SparseArray<Float> mValueAngles;
    private Observer mObserver;

    public BasePieChartAdapter(@NonNull List<T> datas) {
        this.mPieChartDatas = datas;
        this.mValueAngles = new SparseArray<>(datas.size());
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
        mValueAngles.clear();
        for (int i = 0; i < mPieChartDatas.size(); i++) {
            float v = getJudgeData(mPieChartDatas.get(i)) * 360 * 1.0f / totalValue;
            totalAngle += v;
            //最后一个判断总角度是否为360，偏差作用与最后一个值上
            if(i == mPieChartDatas.size() - 1 && totalAngle != 360) {
                float offsetAngle = 360 - totalAngle;
                v += offsetAngle;
            }
            mValueAngles.append(i, v);
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
     * @param percentValue 百分占比
     * @param data 数据对象
     */
    protected void onSelected(int position, float percentValue, T data) {
    }

    public interface Observer {

        /**
         * 更新数据
         */
        void notifyDataSetChanged();
    }
}
