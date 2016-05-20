package com.cpic.taylor.logistics.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import com.cpic.taylor.logistics.bean.SearchPointHistoryData;
import com.cpic.taylor.logistics.utils.AMapUtil;
import com.cpic.taylor.logistics.utils.DensityUtil;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

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

    private int screenWidth, screenHight;
    private ListView lvArea;
    private AreaAdapter adapter;
    private TextView tvLine;
    private TextView tvCancel;
    private AutoCompleteTextView etArea;

    private final static int START = 0;
    private final static int STOP = 1;

    private int action = 0;

    private Intent intent;


    /**
     * 数据库
     *
     * @param savedInstanceState
     */
    private DbUtils db;
    private ArrayList<SearchPointHistoryData> search_datas = new ArrayList<>();
    private HistoryAdapter adapter1;

    @Override
    protected void getIntentData(Bundle savedInstanceState) {

        action = getIntent().getIntExtra("action", 0);

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
        tvCancel = (TextView) findViewById(R.id.activity_choose_tv_cancel);
    }

    @Override
    protected void initData() {
        if (action == START){
            etArea.setHint("您从哪里出发");
        }else if(action == STOP){
            etArea.setHint("您要去哪里");
        }
        initSql();
    }

    private void initSql() {
        DbUtils.DaoConfig config = new DbUtils.DaoConfig(ChooseAreaActivity.this);
        config.setDbName("address.db");
        config.setDbVersion(1);
        config.setDbUpgradeListener(new DbUtils.DbUpgradeListener() {
            @Override
            public void onUpgrade(DbUtils dbUtils, int i, int i1) {

            }
        });
        db = DbUtils.create(config);
        try {
            db.createTableIfNotExist(SearchPointHistoryData.class);
        } catch (DbException e) {
            e.printStackTrace();
        }

        List<SearchPointHistoryData> datas = new ArrayList<>();
        try {
            datas = db.findAll(Selector.from(SearchPointHistoryData.class));
        } catch (DbException e) {
            e.printStackTrace();
        }
        for (int i = 0;i < datas.size();i++){
            search_datas.add(datas.get(i));
        }

        adapter1 = new HistoryAdapter();
        adapter1.setDatas(search_datas);
        lvHistory.setAdapter(adapter1);


    }

    @Override
    protected void registerListener() {

        tvArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseAreaActivity.this,CityPickerActivity.class);
                startActivityForResult(intent,1);
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
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

                if (popupDetails != null && popupDetails.isShowing()) {
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
                onBackPressed();
            }
        });

        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                intent = new Intent();
                intent.putExtra("areaProvice", search_datas.get(i).getArea());
                intent.putExtra("areaName",  search_datas.get(i).getDetails());
                setResult(RESULT_OK, intent);
                finish();

            }
        });


    }



    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onGetInputtips(List<Tip> list, int rCode) {
        if (rCode == 1000) {// 正确返回
            final ArrayList<String> listString = new ArrayList<String>();
            for (int i = 0; i < list.size(); i++) {
                listString.add(list.get(i).getName());
            }
            View view = View.inflate(ChooseAreaActivity.this, R.layout.popupwindow_area, null);
            popupDetails = new PopupWindow(view, screenWidth * 4 / 5, screenHight / 2);
            popupDetails.setFocusable(false);
            lvArea = (ListView) view.findViewById(R.id.pop_area_lv);

            adapter = new AreaAdapter();
            adapter.setDatas(listString);
            lvArea.setAdapter(adapter);

            WindowManager.LayoutParams params = ChooseAreaActivity.this.getWindow().getAttributes();
            ChooseAreaActivity.this.getWindow().setAttributes(params);
            popupDetails.setBackgroundDrawable(new ColorDrawable());
            popupDetails.setOutsideTouchable(false);
            popupDetails.setFocusable(true);
            popupDetails.showAsDropDown(tvLine, DensityUtil.dip2px(ChooseAreaActivity.this, 80), 0);
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

                    if (tvArea.getText().toString().equals("市区")) {
                        showShortToast("请填写市区");
                    } else {
                        intent = new Intent();
                        intent.putExtra("areaProvice", tvArea.getText().toString());
                        intent.putExtra("areaName", listString.get(i));
                        try {
                            db.save(new SearchPointHistoryData(1, tvArea.getText().toString(), listString.get(i)));
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        setResult(RESULT_OK, intent);
                        finish();
                        popupDetails.dismiss();
                    }
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

    public class AreaAdapter extends BaseAdapter {

        private ArrayList<String> datas;


        public void setDatas(ArrayList<String> datas) {
            this.datas = datas;
        }

        @Override
        public int getCount() {
            return datas == null ? 0 : datas.size();
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
            if (view == null) {
                holder = new ViewHolder();
                view = View.inflate(ChooseAreaActivity.this, R.layout.item_area_list, null);
                holder.tvArea = (TextView) view.findViewById(R.id.item_area_tv);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.tvArea.setText(datas.get(i));

            return view;
        }

        class ViewHolder {
            TextView tvArea;
        }
    }

    private class HistoryAdapter extends BaseAdapter {

        private ArrayList<SearchPointHistoryData> datas ;

        public void setDatas(ArrayList<SearchPointHistoryData> datas) {
            this.datas = datas;
        }

        @Override
        public int getCount() {
            return datas == null ? 0 : datas.size();
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
            if (view == null) {
                holder = new ViewHolder();
                view = View.inflate(ChooseAreaActivity.this, R.layout.item_history_list, null);
                holder.tvArea = (TextView) view.findViewById(R.id.item_history_list_tv_area);
                holder.tvDetails = (TextView) view.findViewById(R.id.item_history_list_tv_details);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.tvArea.setText(datas.get(i).getArea());
            holder.tvDetails.setText(datas.get(i).getDetails());

            return view;
        }

        class ViewHolder {
            TextView tvArea, tvDetails;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1&&data != null){
            tvArea.setText(data.getStringExtra("city"));
        }

    }

}
