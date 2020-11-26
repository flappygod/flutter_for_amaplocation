package com.yimeiduo.flutter_for_amaplocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;

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

    //上下文
    private Context context;

    //当前activity
    private Activity activity;

    //方法channel
    private MethodChannel channel;

    //时间channel
    private EventChannel eventChannel;

    //事件发送
    private EventChannel.EventSink eventSink;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;

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
        context = null;
        activity = null;
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addRequestPermissionsResultListener(this);
        context = binding.getActivity().getApplicationContext();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addRequestPermissionsResultListener(this);
        context = binding.getActivity().getApplicationContext();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
        //初始化location
        if (call.method.equals("initLocation")) {
            //这里以ACCESS_COARSE_LOCATION为例
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }
            //设置Apikey
            String apiKey = call.argument("apiKey");
            //设置Apikey
            AMapLocationClient.setApiKey(apiKey);
            //初始化定位
            mLocationClient = new AMapLocationClient(context);
            //成功
            result.success("1");
        }
        //获取location
        else if (call.method.equals("getLocation")) {
            if (mLocationClient != null) {
                //声明定位回调监听器
                AMapLocationListener mLocationListener = new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation aMapLocation) {
                        //创建定位
                        Location location = new Location();
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
                    }
                };
                //设置定位回调监听
                mLocationClient.setLocationListener(mLocationListener);
                //定位设置
                AMapLocationClientOption option = new AMapLocationClientOption();
                //高精度定位
                option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                //获取一次定位结果：
                //该方法默认为false。
                option.setOnceLocation(true);
                //获取最近3s内精度最高的一次定位结果：
                //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，
                //setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
                option.setOnceLocationLatest(true);
                //设置
                mLocationClient.setLocationOption(option);
                //开始定位
                mLocationClient.startLocation();
            } else {
                result.error("-100", "not init location", "not init location");
            }

        }
        //获取location
        else if (call.method.equals("startLocation")) {

            if (mLocationClient != null) {
                //声明定位回调监听器
                AMapLocationListener mLocationListener = new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation aMapLocation) {
                        //创建定位
                        Location location = new Location();
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
                    }
                };
                //设置定位回调监听
                mLocationClient.setLocationListener(mLocationListener);
                //定位设置
                AMapLocationClientOption option = new AMapLocationClientOption();

                //距离设置
                int distanceFilter = call.argument("distanceFilter");
                //时间间隔
                int interval = call.argument("interval");
                //定位的模式
                int locationMode = call.argument("locationMode");
                //设置定位模式
                if (locationMode == 1) {
                    //高精度定位
                    option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
                } else if (locationMode == 2) {
                    //高精度定位
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
                mLocationClient.setLocationOption(option);
                //开始定位
                mLocationClient.startLocation();
            } else {

                result.error("-100", "not init location", "not init location");
            }

        }
        //获取location
        else if (call.method.equals("stopLocation")) {

            if (mLocationClient != null) {
                //开始定位
                mLocationClient.stopLocation();
                //成功
                result.success("1");

            } else {
                result.error("-100", "not init location", "not init location");

            }
        } else {
            result.notImplemented();
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
        return false;
    }
}
