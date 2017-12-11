package com.ttao.rotatepiechart;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.tt.rotatepiechart.BasePieChartAdapter;
import com.tt.rotatepiechart.RotatePieChart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isAdded;
    private BasePieChartAdapter<PieChartBean> adapter;
    private List<PieChartBean> list = new ArrayList<>();
    private int[] colors = new int[]{0xFFFD9033, 0xFFF98AEC, 0xFFFF3564, 0xFFF566D4, 0xFF58F4Fa,
            0xFF3D9F33, 0xFF298AEC, 0xFFFF3F64, 0xFFF566a4, 0xFFd8F42a,
            0xFFF490F3, 0xFF118AEC, 0xFFFFF564, 0xFFF56fD4, 0xFF5dFaFa,
            0xFFFDF03A, 0xFF00AD00, 0xFF00FF00, 0xFFD0F0E0, 0xFF0000FF};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RotatePieChart pieChart = findViewById(R.id.pie_chart);
        TextView textView = findViewById(R.id.text);
        findViewById(R.id.btn_change_data).setOnClickListener(this);

        for(int i = 0; i < 15; i++){
            Random random = new Random();
            PieChartBean bean = new PieChartBean();
            bean.setTitle("标题"+ i);
            bean.setValue(random.nextInt(100) + (i + 1) * random.nextInt(50) + random.nextInt(10));
            bean.setDescription("描述" + i);
            list.add(bean);
        }

        adapter = new BasePieChartAdapter<PieChartBean>(list) {
            @Override
            protected float getJudgeData(PieChartBean bean) {
                return bean.getValue();
            }

            @Override
            protected int getItemColor(int position) {
                return colors[position];
            }

            @Override
            protected void onSelected(int position, float percentValue, PieChartBean data) {
                String text = "percent:" + percentValue + "\nvalue:" + data.getValue()
                        + "\ntitle:" + data.getTitle() + "\ndescription:" + data.getDescription();
                textView.setText(text);
            }
        };

        /**
         * 设置动画插值器
         */
        pieChart.setEntranceInterpolator(new DecelerateInterpolator())
                .setOffsetInterpolator(new AccelerateInterpolator());
        pieChart.setPieChartAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change_data:
                if (isAdded) {
                    for (int i = 0; i < 3; i++) {
                        list.remove(list.size() - 1);
                    }
                } else {
                    for(int i = 0; i < 3; i++){
                        Random random = new Random();
                        PieChartBean bean = new PieChartBean();
                        bean.setTitle("新加的条目标题"+ i);
                        bean.setValue(random.nextInt(100) +
                                (i + 1) * random.nextInt(50) + random.nextInt(10));
                        bean.setDescription("新加的条目描述" + i);
                        list.add(bean);
                    }
                }
                isAdded = !isAdded;
                ((Button)v).setText(getResources().getString(isAdded ? R.string.btn_text_deduct
                        : R.string.btn_text_add));
                adapter.notifyDatasetChanged();
                break;
            default:
                break;
        }
    }
}
