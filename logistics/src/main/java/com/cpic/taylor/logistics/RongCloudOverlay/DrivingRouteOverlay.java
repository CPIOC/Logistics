package com.cpic.taylor.logistics.RongCloudOverlay;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;
import com.cpic.taylor.logistics.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 驾车路线图层类。在高德地图API里，如果要显示驾车路线规划，可以用此类来创建驾车路线图层。如不满足需求，也可以自己创建自定义的驾车路线图层。
 *
 * @since V2.1.0
 */
public class DrivingRouteOverlay extends RouteOverlay {
    private DrivePath drivePath;
    private List<LatLonPoint> throughPointList;
    private List<Marker> throughPointMarkerList = new ArrayList<Marker>();
    private boolean throughPointMarkerVisible = true;
    private Context mContext;

    private PolylineOptions mPolylineOptions;


    /**
     * 通过此构造函数创建驾车路线图层,驾车路线使用一条polyline显示，避免长距离出现卡顿的问题。
     *
     * @param context 当前activity。
     * @param amap    地图对象。
     * @param path    驾车路线规划的一个方案。详见搜索服务模块的路径查询包（com.amap.api.services.route）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/route/DrivePath.html" title="com.amap.api.services.route中的类">DrivePath</a></strong>。
     * @param start   起点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
     * @param end     终点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
     * @since V2.1.0
     */
    public DrivingRouteOverlay(Context context, AMap amap, DrivePath path,
                               LatLonPoint start, LatLonPoint end) {
        this(context, amap, path, start, end, null);
        mContext = context;
    }

    /**
     * 通过此构造函数创建带有途经点的驾车路线图层。
     *
     * @param context          当前activity。
     * @param amap             地图对象。
     * @param path             驾车路线规划的一个方案。详见搜索服务模块的路径查询包（com.amap.api.services.route）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/route/DrivePath.html" title="com.amap.api.services.route中的类">DrivePath</a></strong>。
     * @param start            起点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
     * @param end              终点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类<strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
     * @param throughPointList 途经点列表，详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类<strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
     * @since V2.3.1
     */
    public DrivingRouteOverlay(Context context, AMap amap, DrivePath path,
                               LatLonPoint start, LatLonPoint end,
                               List<LatLonPoint> throughPointList) {
        super(context);
        this.mAMap = amap;
        this.drivePath = path;
        startPoint = AMapServicesUtil.convertToLatLng(start);
        endPoint = AMapServicesUtil.convertToLatLng(end);
        this.throughPointList = throughPointList;
        mContext = context;
    }

    /**
     * 添加驾车路线到地图中。
     *
     * @since V2.1.0
     */
    public void addToMap() {

        initPolylineOptions();
        try {
            List<DriveStep> drivePaths = drivePath.getSteps();
            for (int i = 0; i < drivePaths.size(); i++) {
                DriveStep driveStep = drivePaths.get(i);
                LatLng latLng = AMapServicesUtil.convertToLatLng(getFirstDrivePoint(driveStep));
                if (i < drivePaths.size() - 1) {
                    // 连接起点
                    if (i == 0) {
                        addDrivingPolyLine(startPoint, latLng);
                    }
//                    checkDistanceToNextStep(driveStep, drivePaths.get(i + 1));
                }
                 // 中间路段
                addDrivingStationMarkers(driveStep, latLng);
                addDrivingPolyLine(driveStep);

                // 连接终点，放在最后
                if(i == drivePaths.size() - 1) {
                    addDrivingPolyLine(getLastDrivePoint(driveStep), endPoint);
                }
            }
            addStartAndEndMarker();
            addThroughPointMarker();

            showPolyline();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化线段属性
     */
    private void initPolylineOptions() {

        mPolylineOptions = null;

        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(getDriveColor()).width(getRouteWidth());
    }

    private void showPolyline() {
        addPolyLine(mPolylineOptions);
    }

    /**
     * 检查这一步的最后一点和下一步的起始点之间是否存在空隙
     *
     * @param curStep
     * @param nextStep
     */
    private void checkDistanceToNextStep(DriveStep curStep,
                                         DriveStep nextStep) {
        LatLonPoint lastPoint = getLastDrivePoint(curStep);
        LatLonPoint nextFirstPoint = getFirstDrivePoint(nextStep);
        if (!(lastPoint.equals(nextFirstPoint))) {
            addDrivingPolyLine(lastPoint, nextFirstPoint);
        }
    }

    /**
     * @param driveStep
     * @return
     */
    private LatLonPoint getFirstDrivePoint(DriveStep driveStep) {
        return driveStep
                .getPolyline().get(0);
    }

    /**
     * @param driveStep
     * @return
     */
    private LatLonPoint getLastDrivePoint(DriveStep driveStep) {
        return driveStep
                .getPolyline().get(
                        driveStep.getPolyline().size() - 1);
    }


    private void addDrivingPolyLine(LatLonPoint pointFrom, LatLonPoint pointTo) {
        addDrivingPolyLine(AMapServicesUtil.convertToLatLng(pointFrom), AMapServicesUtil.convertToLatLng(pointTo));
    }

    private void addDrivingPolyLine(LatLng latLngFrom, LatLonPoint pointTo) {
        addDrivingPolyLine(latLngFrom, AMapServicesUtil.convertToLatLng(pointTo));
    }

    private void addDrivingPolyLine(LatLonPoint pointFrom, LatLng latLngTo) {
        addDrivingPolyLine(AMapServicesUtil.convertToLatLng(pointFrom), latLngTo);
    }


    private void addDrivingPolyLine(LatLng latLngFrom, LatLng latLngTo) {
        mPolylineOptions.add(latLngFrom, latLngTo);
//        addPolyLine(new PolylineOptions()
//                .add(latLngFrom, latLngTo)
//                .color(getDriveColor()).width(getRouteWidth()));
    }

    /**
     * @param driveStep
     */
    private void addDrivingPolyLine(DriveStep driveStep) {
        mPolylineOptions.addAll(AMapServicesUtil.convertArrList(driveStep.getPolyline()));
//        addPolyLine(new PolylineOptions()
//                .addAll(AMapServicesUtil.convertArrList(driveStep.getPolyline()))
//                .color(getDriveColor()).width(getRouteWidth()));
    }

    /**
     * @param driveStep
     * @param latLng
     */
    private void addDrivingStationMarkers(DriveStep driveStep, LatLng latLng) {
        addStationMarker(new MarkerOptions()
                .position(latLng)
                .title("\u65B9\u5411:" + driveStep.getAction()
                        + "\n\u9053\u8DEF:" + driveStep.getRoad())
                .snippet(driveStep.getInstruction()).visible(nodeIconVisible)
                .anchor(0.5f, 0.5f).icon(getDriveBitmapDescriptor()));
    }


    private void addThroughPointMarker() {
        if (this.throughPointList != null && this.throughPointList.size() > 0) {
            LatLonPoint latLonPoint = null;
            for (int i = 0; i < this.throughPointList.size(); i++) {
                latLonPoint = this.throughPointList.get(i);
                if (latLonPoint != null) {
                    throughPointMarkerList.add(mAMap
                            .addMarker((new MarkerOptions())
                                    .position(
                                            new LatLng(latLonPoint
                                                    .getLatitude(), latLonPoint
                                                    .getLongitude()))
                                    .visible(throughPointMarkerVisible)
                                    .icon(getThroughPointBitDes())
                                    .title("\u9014\u7ECF\u70B9")));
                }
            }
        }
    }

    @Override
    protected LatLngBounds getLatLngBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        b.include(new LatLng(startPoint.latitude, startPoint.longitude));
        b.include(new LatLng(endPoint.latitude, endPoint.longitude));
        if (this.throughPointList != null && this.throughPointList.size() > 0) {
            for (int i = 0; i < this.throughPointList.size(); i++) {
                b.include(new LatLng(
                        this.throughPointList.get(i).getLatitude(),
                        this.throughPointList.get(i).getLongitude()));
            }
        }
        return b.build();
    }

    public void setThroughPointIconVisibility(boolean visible) {
        try {
            throughPointMarkerVisible = visible;
            if (this.throughPointMarkerList != null
                    && this.throughPointMarkerList.size() > 0) {
                for (int i = 0; i < this.throughPointMarkerList.size(); i++) {
                    this.throughPointMarkerList.get(i).setVisible(visible);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private BitmapDescriptor getThroughPointBitDes() {
    	return BitmapDescriptorFactory.fromResource(R.mipmap.amap_through);
       
    }

    /**
     * 去掉DriveLineOverlay上所有的Marker。
     *
     * @since V2.1.0
     */
    @Override
    public void removeFromMap() {
        try {
            super.removeFromMap();
            if (this.throughPointMarkerList != null
                    && this.throughPointMarkerList.size() > 0) {
                for (int i = 0; i < this.throughPointMarkerList.size(); i++) {
                    this.throughPointMarkerList.get(i).remove();
                }
                this.throughPointMarkerList.clear();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
