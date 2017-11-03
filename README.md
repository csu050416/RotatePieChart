# RotatePieChart
这是一个简单的饼状图控件。可随手势滑动而旋转，根据滑动的速度做惯性减速，停止旋转后，当前选中项自动居中。

## step1：在xml布局文件添加
`<com.tt.rotate_piechart.RotatePieChart
        android:id="@+id/pie_chart"
        android:layout_width="match_parent"       
        android:layout_height="300dp"/>`
    
## step2:创建PieChartAdapter,指定数据类型并传入数据集合;复写下面三个方法
`PieChartAdapter adapter = new PieChartAdapter<T>(list)`

//返回根据数据的拿个值进行占比判断

`protected float getJudgeData(T bean)`

//返回对应position饼状图颜色

`protected int getItemColor(int position)`

//选中项回调

`protected void onSelected(int position, T data)`

## step3:设置adapter
`pieChart.setPieChartAdapter(adapter);`

## 其他属性设置
`pieChart.setBackgroundColor(Color.RED);//饼状图背景颜色`

`pieChart.setIndicatorAngle(20);//指示器宽的度数`

`pieChart.setIndicatorHeight(PieChartUtils.dp2px(this, 50));//指示器高度`

`pieChart.setIndicatorColor(Color.GREEN);//指示器颜色`

`pieChart.setEntranceAnimationDuration(200);//饼状图入场动画时长`

`pieChart.setOffsetAnimationDuration(800);//选中项居中偏移动画时长`

`pieChart.setOutsideStrokeWidth(PieChartUtils.dp2px(this, 20));//外框宽度`

`pieChart.setOutsideStrokeColor(Color.BLUE);//外框颜色`

`pieChart.setStartAngle(0);//饼状图初始绘制起始角度`
