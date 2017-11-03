package com.ttao.rotatepiechart;

/**
 * 标题:饼状图对象
 * 描述：
 * 作者:滕涛
 * 创建时间：2017/4/17 15:55
 */

public class PieChartBean implements Comparable<PieChartBean> {

    private float value;
    private String title;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int compareTo(PieChartBean obj) {
        if (value > obj.getValue())
            return -1;
        return 1;
    }
}
