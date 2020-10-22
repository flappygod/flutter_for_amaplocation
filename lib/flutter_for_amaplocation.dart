import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';

class FlutterForAmaplocation {
  //方法
  static const MethodChannel _channel =
  const MethodChannel('flutter_for_amaplocation');

  //事件
  static const _locationEventChannel =
  EventChannel('flutter_for_amaplocation_event');

  //初始化定位
  static Future<bool> initLocation(String apiKey) async {
    final String flag =
    await _channel.invokeMethod('initLocation', {"apiKey": apiKey});
    if (flag == null || flag == '' || int.parse(flag) == 0) {
      return false;
    }
    return true;
  }

  //获取定位
  static Future<Location> getLocation(LocationOneceOption option) async {
    final String jsonData =
    await _channel.invokeMethod('getLocation', option.toJson());
    //解析出数据
    Location location = Location.fromJson(jsonDecode(jsonData));
    //返回数据
    return location;
  }

  //开始定位
  static Stream<Location> startLocation(LocationAlwaysOption option) {
    //调用持续定位
    _channel.invokeMethod('startLocation', option.toJson());
    //开始定位
    return _locationEventChannel
        .receiveBroadcastStream()
        .map((result) => result as String)
        .map((resultJson) => Location.fromJson(jsonDecode(resultJson)));
  }

  //停止更新位置信息
  static Future<bool> stopLocation() async {
    final String flag = await _channel.invokeMethod('stopLocation');
    if (flag == null || flag == '' || int.parse(flag) == 0) {
      return false;
    }
    return true;
  }
}

//定位的位置
class Location {
  String longitude;
  String province;
  String latitude;
  String street;
  String aOIName;
  String formattedAddress;
  String city;
  String citycode;
  String district;
  String adcode;
  String number;
  String country;
  String pOIName;

  Location(
      {this.longitude,
        this.province,
        this.latitude,
        this.street,
        this.aOIName,
        this.formattedAddress,
        this.city,
        this.citycode,
        this.district,
        this.adcode,
        this.number,
        this.country,
        this.pOIName});

  Location.fromJson(Map<String, dynamic> json) {
    longitude = json['longitude'];
    province = json['province'];
    latitude = json['latitude'];
    street = json['street'];
    aOIName = json['AOIName'];
    formattedAddress = json['formattedAddress'];
    city = json['city'];
    citycode = json['citycode'];
    district = json['district'];
    adcode = json['adcode'];
    number = json['number'];
    country = json['country'];
    pOIName = json['POIName'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['longitude'] = this.longitude;
    data['province'] = this.province;
    data['latitude'] = this.latitude;
    data['street'] = this.street;
    data['AOIName'] = this.aOIName;
    data['formattedAddress'] = this.formattedAddress;
    data['city'] = this.city;
    data['citycode'] = this.citycode;
    data['district'] = this.district;
    data['adcode'] = this.adcode;
    data['number'] = this.number;
    data['country'] = this.country;
    data['POIName'] = this.pOIName;
    return data;
  }
}

//一直定位的设置
class LocationAlwaysOption {
  //设置模式
  static const int Battery_Saving = 1;
  static const int Device_Sensors = 2;
  static const int Hight_Accuracy = 3;

  //android/ios
  int distanceFilter;

  //android
  int locationMode;

  //android
  int interval;

  //ios
  bool locatingWithReGeocode;

  //ios
  bool allowsBackgroundLocationUpdates;

  LocationAlwaysOption(
      {this.distanceFilter: 200,
        this.locationMode: Battery_Saving,
        this.interval: 30,
        this.locatingWithReGeocode: true,
        this.allowsBackgroundLocationUpdates: false});

  LocationAlwaysOption.fromJson(Map<String, dynamic> json) {
    distanceFilter = json['distanceFilter'];
    locationMode = json['androidlocationMode'];
    interval = json['interval'];
    locatingWithReGeocode = json['locatingWithReGeocode'];
    allowsBackgroundLocationUpdates = json['allowsBackgroundLocationUpdates'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['distanceFilter'] = this.distanceFilter;
    data['locationMode'] = this.locationMode;
    data['interval'] = this.interval;
    data['locatingWithReGeocode'] = this.locatingWithReGeocode;
    data['allowsBackgroundLocationUpdates'] =
        this.allowsBackgroundLocationUpdates;
    return data;
  }
}

//单次定位的设置
class LocationOneceOption {
  //kCLLocationAccuracyHundredMeters，一次还不错的定位，偏差在百米左右，超时时间设置在2s-3s左右即可。
  static const kCLLocationAccuracyBestForNavigation = 1;

  //高精度：kCLLocationAccuracyBest，可以获取精度很高的一次定位，偏差在十米左右，超时时间请设置到10s，如果到达10s时没有获取到足够精度的定位结果，会回调当前精度最高的结果。
  static const kCLLocationAccuracyBest = 2;
  static const kCLLocationAccuracyNearestTenMeters = 3;
  static const kCLLocationAccuracyHundredMeters = 4;
  static const kCLLocationAccuracyKilometer = 5;
  static const kCLLocationAccuracyThreeKilometers = 6;

  int locationType;

  //定位的超时时间
  int locationTimeout;

  //逆地理定位超时时间
  int reGeocodeTimeout;

  LocationOneceOption(
      {this.locationType: 4,
        this.locationTimeout: 2,
        this.reGeocodeTimeout: 2});

  LocationOneceOption.fromJson(Map<String, dynamic> json) {
    locationType = json['locationType'];
    locationTimeout = json['locationTimeout'];
    reGeocodeTimeout = json['reGeocodeTimeout'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['locationType'] = this.locationType;
    data['locationTimeout'] = this.locationTimeout;
    data['reGeocodeTimeout'] = this.reGeocodeTimeout;
    return data;
  }
}
