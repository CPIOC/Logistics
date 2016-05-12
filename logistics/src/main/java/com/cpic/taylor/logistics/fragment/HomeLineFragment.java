package com.cpic.taylor.logistics.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.activity.ChooseAreaActivity;
import com.cpic.taylor.logistics.overlay.DrivingRouteOverlay;
import com.cpic.taylor.logistics.utils.AMapUtil;
import com.cpic.taylor.logistics.utils.ProgressDialogHandle;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.List;

/**
 * Created by Taylor on 2016/4/29.
 */
public class HomeLineFragment extends Fragment implements LocationSource,
        AMapLocationListener, AMap.OnMapClickListener,
        AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter, RouteSearch.OnRouteSearchListener ,PoiSearch.OnPoiSearchListener {
    //定位
    private MapView mapView;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;

    //路径规划
    private RouteSearch mRouteSearch;
    private DriveRouteResult mDriveRouteResult;
    private LatLonPoint mStartPoint ;//起点，
    private LatLonPoint mEndPoint ;//终点，

    private RadioGroup mGPSModeGroup;

    private Button btnQuery;
    private Button btnBack;

    /**
     * 起点终点
     */
    private TextView tvStart,tvStop;
    private final static int START = 0;
    private final static int STOP = 1;
    private Intent intent;


    /**
     * 检索功能
     */
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private PoiResult poiResult; // poi返回的结果
    private Dialog dialog;


    /**
     * 判断是目的地还是出发地
     */
    private int status = 0;

    /**
     * 保存当前地址
     */
    private SharedPreferences sp;

    /**
     * 线路选择完成以后，隐藏该Linearlayout
     */
    private LinearLayout linearLayout;


    private HttpUtils post;
    private RequestParams params;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_line, null);
        mapView = (MapView) view.findViewById(R.id.map);
        tvStart = (TextView) view.findViewById(R.id.fragment_line_tv_start);
        tvStop = (TextView) view.findViewById(R.id.fragment_line_tv_stop);
        btnQuery = (Button) view.findViewById(R.id.fragment_line_btn_query);
        btnBack = (Button) view.findViewById(R.id.fragment_line_btn_back);
        dialog = ProgressDialogHandle.getProgressDialog(getActivity(),null);
        linearLayout = (LinearLayout) view.findViewById(R.id.fragment_home_line_linearlayout);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init(view);
        aMap.setOnMarkerClickListener(this);
        registerListener();

        return view;
    }


    private void registerListener() {
        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getActivity(), "出发地", Toast.LENGTH_SHORT).show();
                intent = new Intent(getActivity(), ChooseAreaActivity.class);
                intent.putExtra("action",START);
                status = START;
                startActivityForResult(intent,START);
            }
        });

        tvStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getActivity(), "目的地", Toast.LENGTH_SHORT).show();
                intent = new Intent(getActivity(), ChooseAreaActivity.class);
                intent.putExtra("action", STOP);
                status = STOP;
                startActivityForResult(intent, STOP);
            }
        });

        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mStartPoint&&null!=mEndPoint){
                    setfromandtoMarker();
                }else if (null == mStartPoint){
                    Toast.makeText(getActivity(), "起点未设置", Toast.LENGTH_SHORT).show();
                }else if (null == mEndPoint){
                    Toast.makeText(getActivity(), "终点未设置", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                builder.setTitle("是否结束行程?");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        aMap.clear();
                        mStartPoint = null;
                        mEndPoint = null;
                        btnBack.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.show();
            }
        });
    }

    private void setfromandtoMarker() {

        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String token = sp.getString("token","");
        String lat = sp.getString("now_latitude","");
        String lng =sp.getString("now_longitude","");
        String url = UrlUtils.POST_URL+UrlUtils.path_setRoute;
        params.addBodyParameter("token",token);
        params.addBodyParameter("start",tvStart.getText().toString());
        params.addBodyParameter("end",tvStop.getText().toString());
        if (!"".equals(lat)&&!"".equals(lng)){
            params.addBodyParameter("lat",lat);
            params.addBodyParameter("lng",lng);
        }
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {

            @Override
            public void onStart() {
                super.onStart();
                if (dialog != null){
                    dialog.show();
                }

            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (dialog != null){
                    dialog.dismiss();
                }
                JSONObject obj = JSONObject.parseObject(responseInfo.result);
                int code = obj.getIntValue("code");
                if (code == 1){
                    aMap.addMarker(new MarkerOptions().position(AMapUtil.convertToLatLng(mStartPoint))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start)));
                    aMap.addMarker(new MarkerOptions()
                            .position(AMapUtil.convertToLatLng(mEndPoint))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.end)));
                    searchRouteResult(1, RouteSearch.DrivingDefault);
                    linearLayout.setVisibility(View.GONE);
                    btnBack.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });
    }
    public void setRoutePost(){

    }

    private void init(View view) {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        mRouteSearch = new RouteSearch(getActivity());
        mRouteSearch.setRouteSearchListener(this);


    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            Toast.makeText(getActivity(), "正在定位中", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mEndPoint == null) {
            Toast.makeText(getActivity(), "终点未设置", Toast.LENGTH_SHORT).show();
        }
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);

        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null,
                null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
        mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        aMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
        aMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件
    }



    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(String keyWord,String area) {

        currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", area);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(getActivity(), query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (!"出发地".equals(tvStart.getText().toString())&&!"目的地".equals(tvStop.getText().toString())){
            btnQuery.setVisibility(View.VISIBLE);
        }else{
            btnQuery.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("now_address",aMapLocation.getAddress());
                editor.putString("now_latitude",aMapLocation.getLatitude()+"");
                editor.putString("now_longitude",aMapLocation.getLongitude()+"");

                editor.commit();

//                Log.i("oye",aMapLocation.getAddress());

            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Toast.makeText(getActivity(),"定位失败，请检查GPS是否开启",Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getActivity());
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    @Override
    public View getInfoWindow(final Marker marker) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.poikeywordsearch_uri,
                null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        title.setTextColor(getResources().getColor(R.color.home_tv_area));
        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());
        snippet.setTextColor(getResources().getColor(R.color.home_tv_area));
        ImageButton button = (ImageButton) view.findViewById(R.id.start_amap_app);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == 0){

                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            getActivity());
                    builder.setTitle("是否将此处设为出发地?");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mStartPoint = new LatLonPoint(marker.getPosition().latitude,marker.getPosition().longitude);

                            aMap.clear();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    builder.show();

                }else if (status == 1){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(
                            getActivity());
                    builder.setTitle("是否将此处设为目的地?");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mEndPoint = new LatLonPoint(marker.getPosition().latitude,marker.getPosition().longitude);
                            aMap.clear();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    builder.show();

                }
            }
        });
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

        if (i == 1000) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    mDriveRouteResult = driveRouteResult;
                    final DrivePath drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            getActivity(), aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos());
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")";

                } else if (driveRouteResult != null && driveRouteResult.getPaths() == null) {
                    Toast.makeText(getActivity(), "没有结果", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "没有结果", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "没有结果", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1){
            switch (requestCode){
                case START:
                    String name1 = data.getStringExtra("areaName");
                    String area1 =  data.getStringExtra("areaProvice");
                    tvStart.setText(area1+name1);
                    tvStart.setTextColor(getResources().getColor(R.color.home_tv_area));
                    doSearchQuery(name1,area1);
                    break;
                case STOP:
                    String name2 = data.getStringExtra("areaName");
                    String area2 =  data.getStringExtra("areaProvice");
                    tvStop.setText(area2+name2);
                    tvStop.setTextColor(getResources().getColor(R.color.home_tv_area));
                    doSearchQuery(area2,name2);
                    break;
            }
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {

        if (dialog != null){
            dialog.dismiss();
        }
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        aMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                    } else {
                    }
                }
            } else {

            }
        } else {

        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }


}
