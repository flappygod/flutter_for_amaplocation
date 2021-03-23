package com.yimeiduo.flutter_for_amaplocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry;

/**
 * FlutterForAmaplocationPlugin
 */
public class FlutterForAmaplocationPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler,
        ActivityAware, PluginRegistry.RequestPermissionsResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity

    private final int RequestPermissionCode = 1;

    //上下文
    private Context context;

    //当前activity
    private Activity activity;

    //设置binding
    private ActivityPluginBinding activityPluginBinding;

    //监听
    private PermissionListener permissionListener;

    //方法channel
    private MethodChannel channel;

    //时间channel
    private EventChannel eventChannel;

    //事件发送
    private EventChannel.EventSink eventSink;

    //声明AMapLocationClient类对象
    public AMapLocationClient locationClient = null;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        //上下文
        context = flutterPluginBinding.getApplicationContext();
        //创建方法
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_for_amaplocation");
        //创建事件
        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_for_amaplocation_event");
        //设置handler
        eventChannel.setStreamHandler(this);
        //设置
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        context = null;
        activity = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        addBinding(binding);
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        addBinding(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onDetachedFromActivity() {
        removeBinding();
    }

    //绑定关系
    private void addBinding(ActivityPluginBinding binding) {
        if (activityPluginBinding != null) {
            activityPluginBinding.removeRequestPermissionsResultListener(this);
        }
        activity = binding.getActivity();
        activityPluginBinding = binding;
        activityPluginBinding.addRequestPermissionsResultListener(this);
    }

    //移除关系
    private void removeBinding() {
        if (activityPluginBinding != null) {
            activityPluginBinding.removeRequestPermissionsResultListener(this);
        }
        activity = null;
        activityPluginBinding = null;
    }

    @Override
    public void onMethodCall(@NonNull final MethodCall call, @NonNull final Result result) {
        //初始化location
        if (call.method.equals("initLocation")) {
            //设置Apikey
            String apiKey = call.argument("apiKey");
            //设置Apikey
            AMapLocationClient.setApiKey(apiKey);
            //创建locationClient
            locationClient = new AMapLocationClient(context);
            //成功
            result.success("1");
            //成功
            return;
        }
        //初始化
        if (locationClient == null) {
            result.error("-100", "not init location", "not init location");
            return;
        }
        //获取location
        if (call.method.equals("getLocation")) {
            //检查permission
            checkPermission(new PermissionListener() {
                @Override
                public void result(boolean flag) {
                    if (!flag) {
                        result.error("-200", "没有获取到定位权限", "没有获取到定位权限");
                        return;
                    }
                    //距离设置
                    int locationTimeout = call.argument("locationTimeout");
                    //时间间隔
                    int reGeocodeTimeout = call.argument("reGeocodeTimeout");
                    //定位的模式
                    int locationType = call.argument("locationType");
                    //开始定位
                    locationOnece(locationTimeout, reGeocodeTimeout, locationType, result);
                }
            });
        }
        //获取location
        else if (call.method.equals("startLocation")) {
            checkPermission(new PermissionListener() {
                @Override
                public void result(boolean flag) {
                    if (!flag) {
                        result.error("-200", "没有获取到定位权限", "没有获取到定位权限");
                        return;
                    }
                    //距离设置
                    int distanceFilter = call.argument("distanceFilter");
                    //时间间隔
                    int interval = call.argument("interval");
                    //定位的模式
                    int locationMode = call.argument("locationMode");
                    //开始定位
                    locationStart(distanceFilter, interval, locationMode, result);
                }
            });
        }
        //获取location
        else if (call.method.equals("stopLocation")) {
            //停止定位
            locationClient.stopLocation();
            result.success("1");
        }
        //获取location
        else if (call.method.equals("searchKeyword")) {
            //搜索关键字
            String keywords = call.argument("keywords");
            //搜索城市名称
            String types = call.argument("types");
            //搜索城市名称
            String city = call.argument("city");
            //搜索城市名称
            int page = call.argument("page");
            //搜索城市名称
            int size = call.argument("size");
            //最小
            int cityLimitInt=call.argument("cityLimit");
            //搜索城市名称
            boolean cityLimit = cityLimitInt ==1 ?true:false;
            //搜索poi数据
            PoiSearch.Query query = new PoiSearch.Query(keywords, types, city);
            //设置每页大小
            query.setPageSize(size);
            //设置当前页码
            query.setPageNum(page);
            //限制城市
            query.setCityLimit(cityLimit);
            //创建搜索
            PoiSearch poiSearch = new PoiSearch(context, query);
            //设置监听
            poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
                @Override
                public void onPoiSearched(PoiResult poiResult, int i) {
                    //数据
                    ArrayList<PoiItem> poiItems = poiResult.getPois();
                    List<AmapPoi> pois = new ArrayList<>();
                    if (poiItems != null)
                        for (PoiItem item : poiItems) {
                            AmapPoi poi = new AmapPoi();
                            poi.setLat(item.getLatLonPoint().getLatitude());
                            poi.setLng(item.getLatLonPoint().getLongitude());
                            poi.setUid(item.getPoiId());
                            poi.setName(item.getTitle());
                            poi.setType(item.getTypeDes());
                            poi.setTypecode(item.getTypeCode());
                            poi.setAddress(item.getSnippet());
                            poi.setTel(item.getTel());
                            poi.setDistance(item.getDistance() + "");
                            poi.setPostcode(item.getPostcode());
                            poi.setWebsite(item.getWebsite());
                            poi.setEmail(item.getEmail());
                            poi.setProvince(item.getProvinceName());
                            poi.setPcode(item.getProvinceCode());
                            poi.setCity(item.getCityName());
                            poi.setCitycode(item.getCityCode());
                            poi.setDistrict(item.getAdName());
                            poi.setAdcode(item.getAdCode());
                            pois.add(poi);
                        }
                    result.success(modelToString(pois, AmapPoi.class));
                }

                @Override
                public void onPoiItemSearched(PoiItem poiItem, int i) {

                }
            });
            poiSearch.searchPOIAsyn();
        } else if (call.method.equals("searchAround")) {
            //搜索关键字
            String lat = call.argument("lat");
            //搜索城市名称
            String lng = call.argument("lng");
            //搜索城市名称
            String distance = call.argument("distance");
            //搜索关键字
            String keywords = call.argument("keywords");
            //搜索城市名称
            String types = call.argument("types");
            //搜索城市名称
            String city = call.argument("city");
            //搜索城市名称
            int page = call.argument("page");
            //搜索城市名称
            int size = call.argument("size");
            //搜索poi数据
            PoiSearch.Query query = new PoiSearch.Query(keywords, types, city);
            //设置每页大小
            query.setPageSize(size);
            //设置当前页码
            query.setPageNum(page);
            //创建搜索
            PoiSearch poiSearch = new PoiSearch(context, query);
            //设置搜索附近周边
            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(Double.parseDouble(lat),
                    Double.parseDouble(lng)),
                    Integer.parseInt(distance)));
            //设置监听
            poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
                @Override
                public void onPoiSearched(PoiResult poiResult, int i) {
                    //数据
                    ArrayList<PoiItem> poiItems = poiResult.getPois();
                    List<AmapPoi> pois = new ArrayList<>();
                    if (poiItems != null)
                        for (PoiItem item : poiItems) {
                            AmapPoi poi = new AmapPoi();
                            poi.setLat(item.getLatLonPoint().getLatitude());
                            poi.setLng(item.getLatLonPoint().getLongitude());
                            poi.setUid(item.getPoiId());
                            poi.setName(item.getTitle());
                            poi.setType(item.getTypeDes());
                            poi.setTypecode(item.getTypeCode());
                            poi.setAddress(item.getSnippet());
                            poi.setTel(item.getTel());
                            poi.setDistance(item.getDistance() + "");
                            poi.setPostcode(item.getPostcode());
                            poi.setWebsite(item.getWebsite());
                            poi.setEmail(item.getEmail());
                            poi.setProvince(item.getProvinceName());
                            poi.setPcode(item.getProvinceCode());
                            poi.setCity(item.getCityName());
                            poi.setCitycode(item.getCityCode());
                            poi.setDistrict(item.getAdName());
                            poi.setAdcode(item.getAdCode());
                            pois.add(poi);
                        }
                    result.success(modelToString(pois, AmapPoi.class));
                }

                @Override
                public void onPoiItemSearched(PoiItem poiItem, int i) {
                }
            });
            poiSearch.searchPOIAsyn();
        } else {
            result.notImplemented();
        }
    }


    //对象装json
    private <T> String modelToString(List<T> t, Class<T> cls) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        JSONArray array = new JSONArray();
        for (int s = 0; s < t.size(); s++) {
            try {
                array.put(new JSONObject(gson.toJson(t.get(s), cls)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array.toString();
    }


    //开始定位
    private void locationStart(int distanceFilter,
                               int interval,
                               int locationMode,
                               final Result result) {
        //声明定位回调监听器
        AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation.getErrorCode() == 0) {
                    //创建定位
                    AmapLocation location = new AmapLocation();
                    location.setLatitude(aMapLocation.getLatitude() + "");
                    location.setLongitude(aMapLocation.getLongitude() + "");
                    location.setCity(aMapLocation.getCity());
                    location.setAdcode(aMapLocation.getAdCode());
                    location.setAOIName(aMapLocation.getAoiName());
                    location.setPOIName(aMapLocation.getPoiName());
                    location.setDistrict(aMapLocation.getDistrict());
                    location.setCountry(aMapLocation.getCountry());
                    location.setNumber(aMapLocation.getStreetNum());
                    location.setStreet(aMapLocation.getStreet());
                    location.setFormattedAddress(aMapLocation.getAddress());
                    location.setProvince(aMapLocation.getProvince());
                    location.setCitycode(aMapLocation.getCityCode());
                    Gson gson = new Gson();
                    //成功
                    String json = gson.toJson(location);
                    //成功
                    if (eventSink != null) {
                        eventSink.success(json);
                    }
                } else {
                    result.error(aMapLocation.getErrorCode() + "",
                            aMapLocation.getErrorInfo(),
                            aMapLocation.getLocationDetail());

                }
            }
        };
        //设置定位回调监听
        locationClient.setLocationListener(mLocationListener);
        //定位设置
        AMapLocationClientOption option = new AMapLocationClientOption();
        //设置定位模式
        if (locationMode == 1) {
            //省电模式
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        } else if (locationMode == 2) {
            //仅设备
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
        } else if (locationMode == 3) {
            //高精度定位
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        }
        //设置定位的精度
        option.setDeviceModeDistanceFilter(distanceFilter);
        //时间间隔
        option.setInterval(interval * 1000);
        //获取一次定位结果：
        //该方法默认为false。
        option.setOnceLocation(false);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，
        //setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        option.setOnceLocationLatest(false);
        //设置
        locationClient.setLocationOption(option);
        //开始定位
        locationClient.startLocation();
    }

    //单次定位
    private void locationOnece(int locationTimeout, int reGeocodeTimeout, int locationType, final Result result) {
        //初始化定位
        AMapLocationClient client = new AMapLocationClient(context);
        //声明定位回调监听器
        AMapLocationListener mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    if (aMapLocation.getErrorCode() == 0) {
                        //创建定位
                        AmapLocation location = new AmapLocation();
                        location.setLatitude(aMapLocation.getLatitude() + "");
                        location.setLongitude(aMapLocation.getLongitude() + "");
                        location.setCity(aMapLocation.getCity());
                        location.setAdcode(aMapLocation.getAdCode());
                        location.setAOIName(aMapLocation.getAoiName());
                        location.setPOIName(aMapLocation.getPoiName());
                        location.setDistrict(aMapLocation.getDistrict());
                        location.setCountry(aMapLocation.getCountry());
                        location.setNumber(aMapLocation.getStreetNum());
                        location.setStreet(aMapLocation.getStreet());
                        location.setFormattedAddress(aMapLocation.getAddress());
                        location.setProvince(aMapLocation.getProvince());
                        location.setCitycode(aMapLocation.getCityCode());
                        Gson gson = new Gson();
                        //成功
                        String json = gson.toJson(location);
                        //成功
                        result.success(json);
                    } else {
                        result.error(aMapLocation.getErrorCode() + "",
                                aMapLocation.getErrorInfo(),
                                aMapLocation.getLocationDetail());
                    }
                }
            }
        };
        //设置定位回调监听
        client.setLocationListener(mLocationListener);
        //定位设置
        AMapLocationClientOption option = new AMapLocationClientOption();
        //设置定位精度
        if (locationType == 1) {
            option.setDeviceModeDistanceFilter(100);
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        } else if (locationType == 2) {
            option.setDeviceModeDistanceFilter(10);
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        } else if (locationType == 3) {
            option.setDeviceModeDistanceFilter(10);
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        } else if (locationType == 4) {
            option.setDeviceModeDistanceFilter(100);
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        } else if (locationType == 5) {
            option.setDeviceModeDistanceFilter(1000);
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        } else if (locationType == 6) {
            option.setDeviceModeDistanceFilter(3000);
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        }
        //定位超时时间
        option.setGpsFirstTimeout(locationTimeout * 1000);
        //逆地理编码
        option.setHttpTimeOut(reGeocodeTimeout * 1000);
        //获取一次定位结果：
        //该方法默认为false。
        option.setOnceLocation(true);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，
        //setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        option.setOnceLocationLatest(true);
        //设置
        client.setLocationOption(option);
        //开始定位
        client.startLocation();
    }

    //检查权限
    private void checkPermission(PermissionListener listener) {
        int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        //已经获取了权限
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            listener.result(true);
        }
        //监听权限结果
        else {
            permissionListener = listener;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, RequestPermissionCode);
        }
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        this.eventSink = eventSink;
    }

    @Override
    public void onCancel(Object o) {
        this.eventSink = null;
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RequestPermissionCode) {
            //获取权限结果
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意了权限申请
                if (permissionListener != null) {
                    permissionListener.result(true);
                    permissionListener = null;
                }
            } else {
                //用户拒绝了权限申请
                if (permissionListener != null) {
                    permissionListener.result(false);
                    permissionListener = null;
                }
            }
        }
        return false;
    }
}
