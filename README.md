# RotatePieChart
这是一个简单的饼状图控件。可随手势滑动而旋转，根据滑动的速度做惯性减速，停止旋转后，当前选中项自动居中。

# 效果图
![image](https://github.com/csu050416/MarkdownPhotos/blob/master/rotatepiechart.gif)

# JCenter 引入方法
build.gradle添加:

`compile 'com.tt.rotatepiechart:rotatepiechart:1.0.0'`


# 使用介绍
## step1：在xml布局文件添加

```
<com.tt.rotate_piechart.RotatePieChart
        android:id="@+id/pie_chart"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        app:PCIndicatorColor="#80FFFFFF"
        app:PCBackgroundColor="#FFFFFF"
        app:PCIndicatorHeightRadiusRate="0.4"
        app:PCIndicatorSpanAngle="20"
        app:PCOffsetAnimationDuration="400"
        app:PCOutsideStrokeColor="#CCCCCC"
        app:PCOutsideStrokeWidth="9dp"
        app:PCRate="0.95"
        app:PCStartAngle="-90"
        app:PCEntranceAnimationDuration="1000"/>
```

## step2:创建PieChartAdapter,指定数据类型并传入数据集合;复写下面三个方法
```
//list是数据集合
PieChartAdapter adapter = new PieChartAdapter<T>(list)

//返回占比判断值
protected float getJudgeData(T bean)

//返回对应position饼状图颜色
protected int getItemColor(int position)

//选中项回调
protected void onSelected(int position, float percentValue, T data)
```

## step3:设置adapter
`pieChart.setPieChartAdapter(adapter);`

设置adapter之后，动态改变数据需要调用：

`adapter.notifyDatasetChanged();`

## 其他属性设置

除在xml布局文件设置外，也可通过代码动态设置：
```
pieChart.setPieChartBackgroundColor(Color.RED)//饼状图背景颜色
        .setIndicatorAngle(20)//指示器宽的度数
        .setIndicatorHeight(PieChartUtils.dp2px(this, 50))//指示器高度
        .setIndicatorColor(Color.GREEN)//指示器颜色
        .setEntranceAnimationDuration(200)//饼状图入场动画时长
        .setOffsetAnimationDuration(800)//选中项居中偏移动画时长
        .setOutsideStrokeWidth(PieChartUtils.dp2px(this, 20))//外框宽度
        .setOutsideStrokeColor(Color.BLUE)//外框颜色
        .setStartAngle(0)//饼状图初始绘制起始角度
```

## 惯性旋转实现原理简单说明：
![image](https://github.com/csu050416/MarkdownPhotos/blob/master/RotatePieChart20171103151616.png)

    以下说明具体参考PieChartUtils类里面的calculateAngleFromVelocity方法。
    1、手势up时，计算出当时的velocityX和velocityY，就能得到矢量线速度lineSpeed，并且计算出其与水平线的夹角vectorAngle;
    2、然后根据up点的坐标值和中心点坐标值，计算up点到中心点的连线与水平线的夹角levelAngle;
    3、通过vectorAngle与levelAngle获取circleLineAngle;
    4、勾股定理lineSpeed与circleLineAngle获取circleSpeed；
    5、角速度 w 与线速度 v 的关系：wr = v，得到角速度，其中r是触摸点到中心点的距离；
    6、减速度绘制。

# License
```
Copyright 2017 tengtao

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
