package com.ttao.rotatepiechart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tt.rotate_piechart.PieChartAdapter;
import com.tt.rotate_piechart.RotatePieChart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RotatePieChart pieChart = findViewById(R.id.pie_chart);
        TextView textView = findViewById(R.id.text);


//        pieChart.setPieChartBackgroundColor(Color.RED)
//                .setIndicatorAngle(20)
//                .setIndicatorHeight(PieChartUtils.dp2px(this, 50))
//                .setIndicatorColor(Color.GREEN)
//                .setEntranceAnimationDuration(200)
//                .setOffsetAnimationDuration(800)
//                .setOutsideStrokeWidth(PieChartUtils.dp2px(this, 20))
//                .setOutsideStrokeColor(Color.BLUE)
//                .setStartAngle(0);


        List<PieChartBean> list = new ArrayList<>();

        int[] colors = new int[]{0xFFFD9033, 0xFFF98AEC, 0xFFFF3564, 0xFFF566D4, 0xFF58F4Fa,
                0xFF3D9F33, 0xFF298AEC, 0xFFFF3F64, 0xFFF566a4, 0xFFd8F42a,
                0xFFF490F3, 0xFF118AEC, 0xFFFFF564, 0xFFF56fD4, 0xFF5dFaFa,
                0xFFFDF03A, 0xFF000000, 0xFF00FF00, 0xFF000000, 0xFF0000FF};

        for(int i = 1; i < 15; i++){
            Random random = new Random();
            PieChartBean bean = new PieChartBean();
            bean.setTitle("标题"+ i);
            bean.setValue(random.nextInt(100) + i * random.nextInt(50) + random.nextInt(10));
            bean.setDescription("描述" + i);
            list.add(bean);
        }

        PieChartAdapter adapter = new PieChartAdapter<PieChartBean>(list) {
            @Override
            protected float getJudgeData(PieChartBean bean) {
                return bean.getValue();
            }

            @Override
            protected int getItemColor(int position) {
                return colors[position];
            }

            @Override
            protected void onSelected(int position, PieChartBean data) {
                textView.setText("value:" + data.getValue() + "\ntitle:" + data.getTitle() + "\ndescription:" + data.getDescription());
            }
        };

        pieChart.setPieChartAdapter(adapter);

    }

}
