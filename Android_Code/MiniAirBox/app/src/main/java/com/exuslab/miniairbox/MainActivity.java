package com.exuslab.miniairbox;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;

import com.exuslab.miniairbox.api.WebAPI;
import com.exuslab.miniairbox.dashboard.CircleIndicator;
import com.exuslab.miniairbox.dashboard.CircleProgress;
import com.exuslab.miniairbox.dashboard.IndicatorItem;
import com.exuslab.miniairbox.dashboard.LineIndicator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;



/**
 * Created by Wei on 2016/10/25. */
public class MainActivity extends BaseActivity {

    private static final String TAG = "Main";
    private static final boolean D = true;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService1 = null;

    // Dash borad
    private CircleIndicator ci1;
    private CircleIndicator ci2;
    private CircleIndicator ci3;
    private CircleIndicator ci4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UIInit();
    }

    private Button bt;
    private void UIInit(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
            return;
        }

        bt = (Button)findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        });
    }

    private android.support.design.widget.TabLayout mTabs;
    private ViewPager mViewPager;
    private void initTab(){
        setContentView(R.layout.activity_main_dashboard_new);
        mTabs = (android.support.design.widget.TabLayout) findViewById(R.id.tabs);
        mTabs.addTab(mTabs.newTab().setText(R.string.tab1));
        mTabs.addTab(mTabs.newTab().setText(R.string.tab2));

        mTabs.setTabMode(TabLayout.MODE_FIXED);
        mTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("onTabSelected",String.format("%d", tab.getPosition()));
                mViewPager.setCurrentItem(tab.getPosition(),true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new SamplePagerAdapter());
        mViewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));

    }

    private LinearLayout ll;
    private LineChart mChart;
    private void InitChart(){
        ll = (LinearLayout)findViewById(R.id.linelaout);
        if (mChart == null) {
            mChart = new LineChart(MainActivity.this.getApplicationContext());
//			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
//			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,RelativeLayout.TRUE);
//			params.topMargin =200;
//			params.leftMargin = 100;
//			rl.addView(mChart, params);
//			ll.addView(mChart, params);
            ll.addView(mChart);
            chart_AddExampleData(mChart);
        }
    }
    private void chart_AddExampleData(LineChart chart){
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

            }

            @Override
            public void onNothingSelected() {

            }
        });
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
//        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
//        mv.setChartView(mChart); // For bounds control
//        mChart.setMarker(mv); // Set the marker to the chart

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:yy");
//                Date d = new Date();
//                d.setTime((long) value);
//                return sdf.format(d);
//            }
//
//            @Override
//            public int getDecimalDigits() {
//                return 0;
//            }
//        });

        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
//        xAxis.addLimitLine(llXAxis); // add x-axis limit line

//        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        LimitLine ll1 = new LimitLine(100f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
//        ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(0f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
//        ll2.setTypeface(tf);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
//        setData(50, 100);

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart.animateX(2500);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setTextSize(18f);
        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();
    }

    private void setData(int count, float range) {

        ArrayList<Entry> values = new ArrayList<Entry>();
        ArrayList<Entry> values1 = new ArrayList<Entry>();
        ArrayList<Entry> values2 = new ArrayList<Entry>();
        ArrayList<Entry> values3 = new ArrayList<Entry>();

        for (int i = 0; i < envDataList.size(); i++) {
//            float val = (float) (Math.random() * range) + 3;
            float val = envDataList.get(i).timedate.getTime();
            values.add(new Entry(i, envDataList.get(i).pm2d5));
        }

        for (int i = 0; i < envDataList.size(); i++) {
            values1.add(new Entry(i, envDataList.get(i).h));
        }

        for (int i = 0; i < envDataList.size(); i++) {
            values2.add(new Entry(i, envDataList.get(i).t));
        }

        for (int i = 0; i < envDataList.size(); i++) {
            values3.add(new Entry(i, envDataList.get(i).co2));
        }

        LineDataSet set1;
        LineDataSet set2;
        LineDataSet set3;
        LineDataSet set4;
        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            Log.d("refresh","refresh");
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet)mChart.getData().getDataSetByIndex(1);
            set3 = (LineDataSet)mChart.getData().getDataSetByIndex(2);
            set4 = (LineDataSet)mChart.getData().getDataSetByIndex(3);

            set1.setValues(values);
            set2.setValues(values1);
            set3.setValues(values2);
            set4.setValues(values3);

            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.invalidate();
        } else {
            // create a dataset and give it a type
            // set the line to be drawn like this "- - - - - -"
            set1 = new LineDataSet(values, getResources().getString(R.string.tab2_1));
            set1.setValueTextSize(16f);
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(18f);

            set2 = new LineDataSet(values1, getResources().getString(R.string.tab3));
            set2.setValueTextSize(16f);
            set2.enableDashedLine(10f, 5f, 0f);
            set2.enableDashedHighlightLine(10f, 5f, 0f);
            set2.setColor(Color.RED);
            set2.setCircleColor(Color.RED);
            set2.setLineWidth(1f);
            set2.setCircleRadius(3f);
            set2.setDrawCircleHole(false);
            set2.setDrawFilled(true);
            set2.setFormLineWidth(1f);
            set2.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set2.setFormSize(18f);

            set3 = new LineDataSet(values2, getResources().getString(R.string.tab4));
            set3.setValueTextSize(16f);
            set3.enableDashedLine(10f, 5f, 0f);
            set3.enableDashedHighlightLine(10f, 5f, 0f);
            set3.setColor(Color.BLUE);
            set3.setCircleColor(Color.BLUE);
            set3.setLineWidth(1f);
            set3.setCircleRadius(3f);
            set3.setDrawCircleHole(false);
            set3.setDrawFilled(true);
            set3.setFormLineWidth(1f);
            set3.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set3.setFormSize(18f);

            set4 = new LineDataSet(values2, getResources().getString(R.string.tab5));
            set4.setValueTextSize(16f);
            set4.enableDashedLine(10f, 5f, 0f);
            set4.enableDashedHighlightLine(10f, 5f, 0f);
            set4.setColor(Color.YELLOW);
            set4.setCircleColor(Color.YELLOW);
            set4.setLineWidth(1f);
            set4.setCircleRadius(3f);
            set4.setDrawCircleHole(false);
            set4.setDrawFilled(true);
            set4.setFormLineWidth(1f);
            set4.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set4.setFormSize(18f);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
//                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
//                set1.setFillDrawable(drawable);
            }
            else {
                set1.setFillColor(Color.BLACK);
                set2.setFillColor(Color.RED);
                set3.setFillColor(Color.BLUE);
                set4.setFillColor(Color.YELLOW);
            }

//            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
//            ArrayList<ILineDataSet> dataSets1 = new ArrayList<ILineDataSet>();
//            ArrayList<ILineDataSet> dataSets2 = new ArrayList<ILineDataSet>();
//
//            dataSets.add(set1); // add the datasets
//            dataSets1.add(set2); // add the datasets
//            dataSets2.add(set3); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(set1,set2,set3,set4);

            // set data
            mChart.setData(data);
            mChart.notifyDataSetChanged();
            mChart.invalidate();
        }
    }

    private Button upload_data;
    private List<IndicatorItem> dividerIndicator;
    private List<IndicatorItem> dividerIndicator2;
    private List<IndicatorItem> dividerIndicator3;
    private List<IndicatorItem> dividerIndicator4;
    private String title1 = "濕度";
    private String content1 = "0";
    private String unit1 = "％";
    private String alert1 = " ";
    private String title2 = "溫度";
    private String content2 = "0";
    private String unit2 = "度";
    private String alert2 = " ";
    private String title3 = "PM2.5";
    private String content3 = "0";
    private String unit3 = "μg/m";
    private String alert3 = " ";
    private String title4 = "CO2";
    private String content4 = "0";
    private String unit4 = "ppm";
    private String alert4 = " ";
    private void new_dashboradInit(){
//        setContentView(R.layout.activity_main_dashboard_new);
        ci1 = (CircleIndicator) findViewById(R.id.ci1);
        ci2 = (CircleIndicator)findViewById(R.id.ci2);
        ci3 = (CircleIndicator)findViewById(R.id.ci3);
        ci4 = (CircleIndicator)findViewById(R.id.ci4);

        upload_data = (Button) findViewById(R.id.button3);

        int mCircleGreen = getResources().getColor(R.color.circle_green);
        int mCircleYellow = getResources().getColor(R.color.circle_yellow);
        int mCircleRed = getResources().getColor(R.color.circle_red);
        int mCircleBlue = getResources().getColor(R.color.color1_blue);

        dividerIndicator = new ArrayList<>();
        IndicatorItem item1 = new IndicatorItem();
        item1.start = 0;
        item1.end = 40;
        item1.value = "乾燥";
        item1.color = mCircleBlue;
        dividerIndicator.add(item1);

        IndicatorItem item2 = new IndicatorItem();
        item2.start = 40;
        item2.end = 60;
        item2.value = "舒適濕度";
        item2.color = mCircleGreen;
        dividerIndicator.add(item2);

        IndicatorItem item3 = new IndicatorItem();
        item3.start = 60;
        item3.end = 100;
        item3.value = "潮濕";
        item3.color = mCircleYellow;
        dividerIndicator.add(item3);

        ci1.setContentColor(mCircleRed, mCircleRed);
        ci1.setContent(title1, content1, unit1, alert1);
        ci1.setIndicatorValue(dividerIndicator, 1);

        //======================================

        dividerIndicator2 = new ArrayList<>();
        IndicatorItem item1_2 = new IndicatorItem();
        item1_2.start = -30;
        item1_2.end = 20;
        item1_2.value = "寒冷溫度";
        item1_2.color = mCircleBlue;
        dividerIndicator2.add(item1_2);

        IndicatorItem item2_2 = new IndicatorItem();
        item2_2.start = 20;
        item2_2.end = 25;
        item2_2.value = "舒適溫度";
        item2_2.color = mCircleGreen;
        dividerIndicator2.add(item2_2);

        IndicatorItem item3_2 = new IndicatorItem();
        item3_2.start = 25;
        item3_2.end = 60;
        item3_2.value = "炎熱溫度";
        item3_2.color = mCircleRed;
        dividerIndicator2.add(item3_2);

        ci2.setContentColor(mCircleRed, mCircleRed);
        ci2.setContent(title2, content2, unit2, alert2);
        ci2.setIndicatorValue(dividerIndicator2, 0);

//        ======================================
        int level1 = getResources().getColor(R.color.color1_2d5);
        int level2 = getResources().getColor(R.color.color2_2d5);
        int level3 = getResources().getColor(R.color.color3_2d5);
        int level4 = getResources().getColor(R.color.color4_2d5);
        dividerIndicator3 = new ArrayList<>();
        IndicatorItem item1_3 = new IndicatorItem();
        item1_3.start = 0;
        item1_3.end = 35;
        item1_3.value = "低";
        item1_3.color = level1;
        dividerIndicator3.add(item1_3);

        IndicatorItem item2_3 = new IndicatorItem();
        item2_3.start = 35;
        item2_3.end = 25;
        item2_3.value = "中";
        item2_3.color = level2;
        dividerIndicator3.add(item2_3);

        IndicatorItem item3_3 = new IndicatorItem();
        item3_3.start = 25;
        item3_3.end = 60;
        item3_3.value = "高";
        item3_3.color = level3;
        dividerIndicator3.add(item3_3);

        IndicatorItem item3_4 = new IndicatorItem();
        item3_4.start = 60;
        item3_4.end = 100;
        item3_4.value = "非常高";
        item3_4.color = level4;
        dividerIndicator3.add(item3_4);

        ci3.setContentColor(mCircleRed, mCircleRed);
        ci3.setContent(title3, content3, unit3, alert3);
        ci3.setIndicatorValue(dividerIndicator3, 0);

        //=====================================
        dividerIndicator4 = new ArrayList<>();
        IndicatorItem co2_item1 = new IndicatorItem();
        co2_item1.start = 0;
        co2_item1.end = 750;
        co2_item1.value = "健康";
        co2_item1.color = Color.GREEN;
        dividerIndicator4.add(co2_item1);

        IndicatorItem co2_item2 = new IndicatorItem();
        co2_item2.start = 750;
        co2_item2.end = 2500;
        co2_item2.value = "不健康";
        co2_item2.color = Color.YELLOW;
        dividerIndicator4.add(co2_item2);

        IndicatorItem co2_item3 = new IndicatorItem();
        co2_item3.start = 2500;
        co2_item3.end = 5000;
        co2_item3.value = "極不健康";
        co2_item3.color = Color.RED;
        dividerIndicator4.add(co2_item3);

        ci4.setContentColor(mCircleRed, mCircleRed);
        ci4.setContent(title4, content4, unit4, alert4);
        ci4.setIndicatorValue(dividerIndicator4, 0);
        //=====================================
        upload_data.setOnClickListener(click);

    }

    private View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button3:
                    if(pm2d5_data.length()>1) {
                        AsyncHttpClient client = new AsyncHttpClient();
                        client.setTimeout(3000);
                        client.setConnectTimeout(3000);
                        client.setMaxRetriesAndTimeout(0, 3000);
                        RequestParams reqPara = new RequestParams();
                        reqPara.put("ok", "1");
                        reqPara.put("pm2d5", pm2d5_data);
                        reqPara.put("co2", "0");
                        reqPara.put("temperature", t_data);
                        reqPara.put("humidity", h_data);
                        client.post(MainActivity.this, WebAPI.upload_data, reqPara, asyncMainRefresh);
                    }
                    else{
                        Toast.makeText(MainActivity.this,"No Data!!",Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
                    break;
            }
        }
    } ;


    private AsyncHttpResponseHandler asyncMainRefresh = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            if(statusCode == 200) {
                String resp = new String(responseBody);
                System.out.println(resp);
                Toast.makeText(MainActivity.this,"Upload Data!!",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Toast.makeText(MainActivity.this,"Upload Data Failed!!",Toast.LENGTH_SHORT).show();
        }
    };




    private TextView humidity,temperature,pm2d5;
    private void UIInit_Second() {
        setContentView(R.layout.activity_main_dashboard);
        humidity = (TextView)findViewById(R.id.textView3);
        temperature = (TextView)findViewById(R.id.textView2);
        pm2d5 = (TextView)findViewById(R.id.textView);
    }

    private List<EnvData> envDataList = new ArrayList<>();
    private String pm2d5_data ="";
    private String t_data="";
    private String h_data="";
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,String.format("%s",msg.what));

            switch (msg.what) {
                case MessageType.MESSAGE_STATE_CHANGE:

                    switch (msg.arg1){
                        
                        case BluetoothChatService.STATE_CONNECTED:
//                            UIInit_Second();
//                            new_dashboradInit();
                            initTab();
                            break;
                    }

                    break;
                case MessageType.MESSAGE_READ:
                    String data = (String )msg.obj;
                    System.out.println(data);
                    String[] splitData = data.split(",");

                    if(splitData.length>=3){
                        EnvData envData = new EnvData();
                        String [] H_data = splitData[0].split("E");
//                        System.out.println("Len:" + splitData[0].split("E")[1]+" "+splitData[0].split("E").length);
//                        System.err.println(String.format("pm2.5:%s h:%s t:%s",splitData[0].split("E")[1]
//                                ,splitData[1],splitData[2]) );
                        if(H_data.length>1){
                            System.out.println("Len:" + splitData[0].split("E")[1]);
//                            pm2d5.setText(String.format(getString(R.string.pm2d5_text)+"%s",splitData[0].split("E")[1]));
                            Integer pm2d5 = Integer.parseInt(splitData[0].split("E")[1]);
                            ci3.setContent(title1, pm2d5.toString(), unit1, alert1);
                            ci3.setIndicator(pm2d5);
                            pm2d5_data = pm2d5.toString();
                            envData.pm2d5 = pm2d5;
                        }else{
                            System.out.println("Len:" + splitData[0]);
//                            pm2d5.setText(String.format(getString(R.string.pm2d5_text)+"%s",splitData[0]));
                            try {
                                Integer pm2d5 = Integer.parseInt(splitData[0]);
                                ci3.setContent(title3, pm2d5.toString(), unit3, alert3);
                                ci3.setIndicator(pm2d5);
                                pm2d5_data = pm2d5.toString();
                                envData.pm2d5 = pm2d5;

                            }catch (Exception e){}

                        }
//                        temperature.setText(String.format(getString(R.string.temperature_text)+"%s",splitData[1]));
//                        humidity.setText(String.format(getString(R.string.humidity_text)+"%s",splitData[2])+"%");

                        try {
                            Float d1 = Float.parseFloat(splitData[2]); // 温度
                            Float d2 = Float.parseFloat(splitData[1]); // 濕度
                            Float d3 = 0F;                             // 二氧化碳
//                            int dtemperature = Integer.parseInt(splitData[2]);
//                            int dhumidity    = Integer.parseInt(splitData[1]);
                            Log.d("d1 d2",splitData[1]+" | "+splitData[2]);
                            Log.d("d1 d2",d1.toString()+" | "+d2.toString());

//                          = 温度 =
                            ci2.setContent(title2, splitData[1], unit2, alert2);
//                            ci2.setIndicatorValue(dividerIndicator2, d2.intValue());
                            ci2.setIndicator(d2.intValue());
                            t_data = d2.toString();
//                          = 温度 =

//                          = 濕度 =
                            ci1.setContent(title1, splitData[2], unit1, alert1);
//                            ci1.setIndicatorValue(dividerIndicator, d1.intValue());
                            ci1.setIndicator(d1.intValue());
                            h_data = d1.toString();
//                          = 濕度 =

//                          = 二氧化碳 =
                            if(splitData.length==4)
                                 d3 = Float.parseFloat(splitData[3]);
                            ci4.setContent(title4, d3.toString(), unit4, alert4);
//                            ci1.setIndicatorValue(dividerIndicator, d1.intValue());
                            ci4.setIndicator(d3.intValue());
//                          = 二氧化碳 =

                            envData.h   = d1; // 温度
                            envData.t   = d2; // 濕度
                            envData.co2 = d3; // 二氧化碳
                        }catch (Exception e){}

                        envDataList.add(envData);
                        setData(1,1);
                    }

                    /*
                    if(splitData.length>=3){

                        String [] H_data = splitData[0].split("E");
//                        System.out.println("Len:" + splitData[0].split("E")[1]+" "+splitData[0].split("E").length);
//                        System.err.println(String.format("pm2.5:%s h:%s t:%s",splitData[0].split("E")[1]
//                                ,splitData[1],splitData[2]) );
                         if(H_data.length>1){
                             System.out.println("Len:" + splitData[0].split("E")[1]);
                             pm2d5.setText(String.format(getString(R.string.pm2d5_text)+"%s",splitData[0].split("E")[1]));
                         }else{
                             System.out.println("Len:" + splitData[0]);
                             pm2d5.setText(String.format(getString(R.string.pm2d5_text)+"%s",splitData[0]));
                         }
                        temperature.setText(String.format(getString(R.string.temperature_text)+"%s",splitData[1]));
                        humidity.setText(String.format(getString(R.string.humidity_text)+"%s",splitData[2])+"%");
                    }*/
                    break;

            }

        }
    };


    /*  ========================================================================== */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    if (D) Log.d(TAG, "RESULT_OK");
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS); // Get the device MAC address
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);// Get the BLuetoothDevice object
                    mChatService1 = new BluetoothChatService(this, mHandler);
                    mChatService1.start();
                    mChatService1.connect(device);
                }
                break;
        }

    }


    private class SamplePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Item " + (position + 1);
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d("Tab",String.format("position:%d",position));
            View view = null;

            switch (position){
                case 0:
                    view = getLayoutInflater().inflate(R.layout.activity_main_dashboard_new,
                            container, false);
                    container.addView(view);
                    new_dashboradInit();
                    break;
                case 1:
                    view = getLayoutInflater().inflate(R.layout.activity_main_dashboard_chart,
                            container, false);
                    container.addView(view);
                    InitChart();
                    break;
            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.d(TAG, "onStart");
//
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(D) Log.d(TAG, "onDestroy");
        if(mChatService1 != null) mChatService1.mConnectedThread.cancel();
    }

    /**
     * Class EnvData */
    private class EnvData{

        public float pm2d5 = 0f;
        public float h = 0f;
        public float t = 0f;
        public float co2=0f;
        private Date timedate;

        public EnvData(){
            Calendar calendar = Calendar.getInstance();
            timedate = calendar.getTime();
        }

        public EnvData(float h,float t,float pm2d5,float co2){
            this.pm2d5 =pm2d5;
            this.h = h;
            this.t = t;
            this.co2 = co2;
            Calendar calendar = Calendar.getInstance();
            timedate = calendar.getTime();
        }


    }
}
