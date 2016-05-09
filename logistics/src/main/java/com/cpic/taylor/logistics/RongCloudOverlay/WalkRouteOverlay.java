package com.cpic.taylor.logistics.RongCloudOverlay;

import java.util.List;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkStep;
/**
 * 步行路线图层类。在高德地图API里，如果要显示步行路线规划，可以用此类来创建步行路线图层。如不满足需求，也可以自己创建自定义的步行路线图层。
 * @since V2.1.0
 */
public class WalkRouteOverlay extends RouteOverlay {
	private WalkPath walkPath;
	/**
	 * 通过此构造函数创建步行路线图层。
	 * @param context 当前activity。
	 * @param amap 地图对象。
	 * @param path 步行路线规划的一个方案。详见搜索服务模块的路径查询包（com.amap.api.services.route）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/route/WalkStep.html" title="com.amap.api.services.route中的类">WalkStep</a></strong>。
	 * @param start 起点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类<strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
	 * @param end 终点。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类<strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
	 * @since V2.1.0
	 */
	public WalkRouteOverlay(Context context, AMap amap, WalkPath path,
			LatLonPoint start, LatLonPoint end) {
		super(context);
		this.mAMap = amap;
		this.walkPath = path;
		startPoint = AMapServicesUtil.convertToLatLng(start);
		endPoint = AMapServicesUtil.convertToLatLng(end);
	}
	/**
	 * 添加步行路线到地图中。
	 * @since V2.1.0
	 */
	public void addToMap() {
		try {
			List<WalkStep> walkPaths = walkPath.getSteps();
			for (int i = 0; i < walkPaths.size(); i++) {
				WalkStep walkStep = walkPaths.get(i);
				LatLng latLng = AMapServicesUtil.convertToLatLng(walkStep
						.getPolyline().get(0));
				if (i < walkPaths.size() - 1) {
					if (i == 0) {
						addWalkPolyLine(startPoint, latLng);
					}
					checkDistanceToNextStep(walkStep, walkPaths.get(i + 1));
				} else {
					LatLng latLng1 = AMapServicesUtil
							.convertToLatLng(getLastWalkPoint(walkStep));
					addWalkPolyLine(latLng1, endPoint);
				}

				addWalkStationMarkers(walkStep, latLng);
				addWalkPolyLines(walkStep);
			}
			addStartAndEndMarker();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 检查这一步的最后一点和下一步的起始点之间是否存在空隙
	 * @param driveStep
	 * @param driveStep1
	 */
	private void checkDistanceToNextStep(WalkStep walkStep,
			WalkStep walkStep1) {
		LatLonPoint lastPoint = getLastWalkPoint(walkStep);
		LatLonPoint nextFirstPoint = getFirstWalkPoint(walkStep1);
		if (!(lastPoint.equals(nextFirstPoint))) {
			addWalkPolyLine(lastPoint, nextFirstPoint);
		}
	}

	/**
	 * @param walkStep
	 * @return
	 */
	private LatLonPoint getLastWalkPoint(WalkStep walkStep) {
		return walkStep.getPolyline().get(walkStep.getPolyline().size() - 1);
	}

	/**
	 * @param walkStep
	 * @return
	 */
	private LatLonPoint getFirstWalkPoint(WalkStep walkStep) {
		return walkStep.getPolyline().get(0);
	}

	private void addWalkPolyLine(LatLonPoint pointFrom, LatLonPoint pointTo) {
		addWalkPolyLine(AMapServicesUtil.convertToLatLng(pointFrom), AMapServicesUtil.convertToLatLng(pointTo));
	}
	
	private void addWalkPolyLine(LatLng latLngFrom, LatLng latLngTo) {
		addPolyLine(new PolylineOptions()
				.add(latLngFrom, latLngTo).color(getWalkColor())
				.width(getRouteWidth()));
	}

	/**
	 * @param walkStep
	 */
	private void addWalkPolyLines(WalkStep walkStep) {
		addPolyLine(new PolylineOptions()
						.addAll(AMapServicesUtil.convertArrList(walkStep
								.getPolyline())).color(getWalkColor())
						.width(getRouteWidth()));
	}

	/**
	 * @param walkStep
	 * @param position
	 */
	private void addWalkStationMarkers(WalkStep walkStep, LatLng position) {
		addStationMarker(new MarkerOptions()
				.position(position)
				.title("\u65B9\u5411:" + walkStep.getAction()
						+ "\n\u9053\u8DEF:" + walkStep.getRoad())
				.snippet(walkStep.getInstruction()).visible(nodeIconVisible)
				.anchor(0.5f, 0.5f).icon(getWalkBitmapDescriptor()));
	}
}
