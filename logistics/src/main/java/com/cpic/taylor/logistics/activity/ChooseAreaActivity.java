package com.cpic.taylor.logistics.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.base.BaseActivity;
import com.cpic.taylor.logistics.utils.AMapUtil;
import com.cpic.taylor.logistics.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Taylor on 2016/5/5.
 */
public class ChooseAreaActivity extends BaseActivity implements PoiSearch.OnPoiSearchListener, Inputtips.InputtipsListener {

    private ListView lvHistory;
    private TextView tvArea;
    private PopupWindow popupWindow;
    private PopupWindow popupDetails;

    private int screenWidth,screenHight;
    private ListView lvArea;
    private ArrayList<String> datas;
    private AreaAdapter adapter;
    private TextView tvLine;
    private TextView tvCancel;
    private AutoCompleteTextView etArea;

    private final static int START = 0;
    private final static int STOP = 1;

    private int action = 0;

    private Intent intent;


    @Override
    protected void getIntentData(Bundle savedInstanceState) {

       action = getIntent().getIntExtra("action",0);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHight = metrics.heightPixels;
    }

    @Override
    protected void loadXml() {
        setContentView(R.layout.activity_choose_area);
    }

    @Override
    protected void initView() {
        lvHistory = (ListView) findViewById(R.id.activity_choose_area_lv);
        tvArea = (TextView) findViewById(R.id.activity_choose_area_tv_area);
        tvLine = (TextView) findViewById(R.id.divide_line);
        etArea = (AutoCompleteTextView) findViewById(R.id.activity_choose_et_dest);
        tvCancel = (TextView)findViewById(R.id.activity_choose_tv_cancel);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void registerListener() {

        tvArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPopwin();
            }
        });
        etArea.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (popupDetails != null && popupDetails.isShowing()){
                    popupDetails.dismiss();
                }
                String newText = editable.toString().trim();
                if (!AMapUtil.IsEmptyOrNullString(newText)) {
                    InputtipsQuery inputquery = new InputtipsQuery(newText, tvArea.getText().toString());
                    Inputtips inputTips = new Inputtips(ChooseAreaActivity.this, inputquery);
                    inputTips.setInputtipsListener(ChooseAreaActivity.this);
                    inputTips.requestInputtipsAsyn();
                }
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etArea.setText("");
                if (popupDetails != null && popupDetails.isShowing()){
                    popupDetails.dismiss();
                }
            }
        });


    }

    private void openPopwin() {
        View view = View.inflate(ChooseAreaActivity.this, R.layout.popupwindow_area, null);
        popupWindow = new PopupWindow(view, screenWidth, screenHight/2);
        popupWindow.setFocusable(true);
        lvArea = (ListView) view.findViewById(R.id.pop_area_lv);

        datas = new ArrayList<>();
        AreaData();
        adapter = new AreaAdapter();
        adapter.setDatas(datas);
        lvArea.setAdapter(adapter);

        WindowManager.LayoutParams params = ChooseAreaActivity.this.getWindow().getAttributes();
        ChooseAreaActivity.this.getWindow().setAttributes(params);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAsDropDown(tvLine);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = ChooseAreaActivity.this.getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });

        lvArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                tvArea.setText(datas.get(i));
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void onGetInputtips(List<Tip> list, int rCode) {
        if (rCode == 1000) {// 正确返回
            final ArrayList<String> listString = new ArrayList<String>();
            for (int i = 0; i < list.size(); i++) {
                listString.add(list.get(i).getName());
            }
            View view = View.inflate(ChooseAreaActivity.this, R.layout.popupwindow_area, null);
            popupDetails = new PopupWindow(view, screenWidth*4/5, screenHight/3);
            popupDetails.setFocusable(false);
            lvArea = (ListView) view.findViewById(R.id.pop_area_lv);

            adapter = new AreaAdapter();
            adapter.setDatas(listString);
            lvArea.setAdapter(adapter);

            WindowManager.LayoutParams params = ChooseAreaActivity.this.getWindow().getAttributes();
            ChooseAreaActivity.this.getWindow().setAttributes(params);
            popupDetails.setBackgroundDrawable(new ColorDrawable());
            popupDetails.setOutsideTouchable(false);
            popupDetails.showAsDropDown(tvLine, DensityUtil.dip2px(ChooseAreaActivity.this,80),0);
            popupDetails.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams params = ChooseAreaActivity.this.getWindow().getAttributes();
                    params.alpha = 1f;
                    getWindow().setAttributes(params);
                }
            });
            lvArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    intent = new Intent();
                    intent.putExtra("areaName",listString.get(i));
                    setResult(RESULT_OK,intent);
                    finish();
                    popupDetails.dismiss();
                }
            });

        } else {

        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    public class AreaAdapter extends BaseAdapter{

        private ArrayList<String> datas;


       public  void setDatas(ArrayList<String> datas){
           this.datas = datas;
        }

        @Override
        public int getCount() {
            return datas == null ? 0 :datas.size();
        }

        @Override
        public Object getItem(int i) {
            return datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null){
                holder = new ViewHolder();
                view = View.inflate(ChooseAreaActivity.this,R.layout.item_area_list,null);
                holder.tvArea = (TextView) view.findViewById(R.id.item_area_tv);
                view.setTag(holder);
            }else {
                holder = (ViewHolder) view.getTag();
            }
            holder.tvArea.setText(datas.get(i));

            return view;
        }

        class ViewHolder{
            TextView tvArea;
        }
    }
    private void AreaData() {
        datas.add("北京市");
        datas.add("天津市");
        datas.add("上海市");
        datas.add("重庆市");
        datas.add("河北省");
        datas.add("河南省");
        datas.add("云南省");
        datas.add("辽宁省");
        datas.add("黑龙江省");
        datas.add("湖南省");
        datas.add("安徽省");
        datas.add("新疆维吾尔");
        datas.add("广西壮族");
        datas.add("湖北省");
        datas.add("甘肃省");
        datas.add("山西省");
        datas.add("内蒙古");
        datas.add("陕西省");
        datas.add("吉林省");
        datas.add("福建省");
        datas.add("贵州省");
        datas.add("广东省");
        datas.add("西藏");
        datas.add("四川省");
        datas.add("宁夏回族");
        datas.add("海南省");
        datas.add("台湾省");
        datas.add("香港特别行政区");
        datas.add("澳门特别行政区");
    }


}
