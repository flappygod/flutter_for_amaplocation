#import "FlutterForAmaplocationPlugin.h"
#import "JsonTool.h"


@interface FlutterForAmaplocationPlugin ()<FlutterStreamHandler>
@property(nonatomic,strong) CLLocationManager * cllManager;
//管理
@property(nonatomic,strong) AMapLocationManager* manager;
//方法的channel
@property(nonatomic,weak) FlutterMethodChannel* channel;
//事件的channel
@property(nonatomic,weak) FlutterEventChannel* eventChannel;

@end


@implementation FlutterForAmaplocationPlugin
{
    FlutterEventSink locationEvent;
}

//注册
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    
    //创建eventChannel
    FlutterEventChannel* eventChannel=[FlutterEventChannel eventChannelWithName:@"flutter_for_amaplocation_event"
                                                                binaryMessenger:[registrar messenger]];
    
    //方法
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"flutter_for_amaplocation"
                                     binaryMessenger:[registrar messenger]];
    //创建插件并注册
    FlutterForAmaplocationPlugin* instance = [[FlutterForAmaplocationPlugin alloc] init];
    //保留引用
    instance.eventChannel=eventChannel;
    //设置handler
    [instance.eventChannel setStreamHandler:instance];
    //保留引用
    instance.channel=channel;
    //添加
    [registrar addMethodCallDelegate:instance channel:channel];
    
}

//调用
- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    
    //初始化location
    if ([@"initLocation" isEqualToString:call.method]) {
        //获取地址
        NSString* apiKey=(NSString*)call.arguments[@"apiKey"];
        
        //设置key
        [AMapServices sharedServices].apiKey =apiKey;
        
        //创建
        _cllManager = [CLLocationManager new];
        
        //管理器
        _manager=[[AMapLocationManager alloc]init];
        
        //设置代理
        _manager.delegate=self;
        
        //设置成功
        result(@"1");
        
        //返回
        return;
    }
    
    //没有初始化
    if(_cllManager==nil){
        result([FlutterError errorWithCode:[NSString stringWithFormat:@"%ld",(long)-100] message:@"location not init" details:@"location not init"]);
        return;
    }
    
    //单次定位获取位置信息
    if ([@"getLocation" isEqualToString:call.method]) {
        //请求“使用期间”使用定位服务
        [_cllManager requestWhenInUseAuthorization];
        //管理器
        NSInteger locationType=((NSString*)call.arguments[@"locationType"]).integerValue;
        CLLocationAccuracy type=0;
        if(locationType==1)
        {
            type=kCLLocationAccuracyBestForNavigation;
        }
        if(locationType==2)
        {
            type=kCLLocationAccuracyBest;
        }
        if(locationType==3)
        {
            type=kCLLocationAccuracyNearestTenMeters;
        }
        if(locationType==4)
        {
            type=kCLLocationAccuracyHundredMeters;
        }
        if(locationType==5)
        {
            type=kCLLocationAccuracyKilometer;
        }
        if(locationType==6)
        {
            type=kCLLocationAccuracyThreeKilometers;
        }
        // 带逆地理信息的一次定位（返回坐标和地址信息）
        [_manager setDesiredAccuracy:type];
        //   定位超时时间，最低2s，此处设置为2s
        _manager.locationTimeout =((NSString*)call.arguments[@"locationTimeout"]).integerValue;;
        //   逆地理请求超时时间，最低2s，此处设置为2s
        _manager.reGeocodeTimeout = ((NSString*)call.arguments[@"reGeocodeTimeout"]).integerValue;;
        //防止循环引用
        //定位
        [_manager  requestLocationWithReGeocode:YES completionBlock:^(CLLocation *location, AMapLocationReGeocode *regeocode, NSError *error) {
            if (error)
            {
                //抛出异常处理
                result([FlutterError errorWithCode:[NSString stringWithFormat:@"%ld",(long)error.code]
                                           message:error.description
                                           details:error.localizedDescription]);
                return;
            }
            //设置
            NSMutableDictionary* dic=[[NSMutableDictionary alloc]init];
            //设置经纬度
            if(location!=nil){
                dic[@"latitude"]=[NSString stringWithFormat:@"%f",location.coordinate.latitude];
                dic[@"longitude"]=[NSString stringWithFormat:@"%f",location.coordinate.longitude];
            }
            //设置
            if(regeocode!=nil){
                ///格式化地址
                if(regeocode.formattedAddress!=nil)
                    dic[@"formattedAddress"]=regeocode.formattedAddress;
                ///国家
                if(regeocode.country!=nil)
                    dic[@"country"]=regeocode.country;
                ///省/直辖市
                if(regeocode.province!=nil)
                    dic[@"province"]=regeocode.province;
                ///市
                if(regeocode.city!=nil)
                    dic[@"city"]=regeocode.city;
                ///区
                if(regeocode.district!=nil)
                    dic[@"district"]=regeocode.district;
                ///城市编码
                if(regeocode.citycode!=nil)
                    dic[@"citycode"]=regeocode.citycode;
                ///城市编码
                if(regeocode.adcode!=nil)
                    dic[@"adcode"]=regeocode.adcode;
                ///街道名称
                if(regeocode.street!=nil)
                    dic[@"street"]=regeocode.street;
                ///门牌号
                if(regeocode.number!=nil)
                    dic[@"number"]=regeocode.number;
                ///兴趣点名称
                if(regeocode.POIName!=nil)
                    dic[@"POIName"]=regeocode.POIName;
                ///门牌号
                if(regeocode.AOIName!=nil)
                    dic[@"AOIName"]=regeocode.AOIName;
            }
            //转换为字符串
            NSString* json=[JsonTool DicToJSONString:dic];
            //成功
            result(json);
        }];
        
    }//单次定位获取位置信息
    else if ([@"startLocation" isEqualToString:call.method]) {
        //请求“使用期间”使用定位服务
        [_cllManager requestWhenInUseAuthorization];
        //开始定位
        //设置
        NSInteger  distanceFilter=((NSString*)call.arguments[@"distanceFilter"]).integerValue;
        //设置
        NSString*  locatingWithReGeocode=call.arguments[@"locatingWithReGeocode"];
        //后台定位
        NSString*  allowsBackgroundLocationUpdates=call.arguments[@"allowsBackgroundLocationUpdates"];
        //设置代理
        _manager.delegate = self;
        //定位精度
        _manager.distanceFilter = distanceFilter;
        //是否你地理编码
        _manager.locatingWithReGeocode = locatingWithReGeocode.boolValue;
        //是否允许后台定位
        _manager.allowsBackgroundLocationUpdates=allowsBackgroundLocationUpdates.boolValue;
        //开始定位
        [_manager startUpdatingLocation];
    }
    //停止定位获取信息
    else if ([@"stopLocation" isEqualToString:call.method]) {
        //停止定位
        [_manager stopUpdatingLocation];
        //成功
        result(@"1");
    }
    //没有实现该方法
    else{
        result(FlutterMethodNotImplemented);
    }
}



#pragma mark - <FlutterStreamHandler>
// // 这个onListen是Flutter端开始监听这个channel时的回调，第二个参数 EventSink是用来传数据的载体。
- (FlutterError* _Nullable)onListenWithArguments:(id _Nullable)arguments
                                       eventSink:(FlutterEventSink)events {
    // arguments flutter给native的参数
    // 回调给flutter， 建议使用实例指向，因为该block可以使用多次
    locationEvent=events;
    return nil;
}

/// flutter不再接收
- (FlutterError* _Nullable)onCancelWithArguments:(id _Nullable)arguments {
    // arguments flutter给native的参数
    //清空参数
    locationEvent=nil;
    return nil;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    
}


#pragma mark - AMapLocationManagerDelegate
/**
 *  @brief 当plist配置NSLocationAlwaysUsageDescription或者NSLocationAlwaysAndWhenInUseUsageDescription，并且[CLLocationManager authorizationStatus] == kCLAuthorizationStatusNotDetermined，会调用代理的此方法。
 此方法实现调用申请后台权限API即可：[locationManager requestAlwaysAuthorization](必须调用,不然无法正常获取定位权限)
 *  @param manager 定位 AMapLocationManager 类。
 *  @param locationManager  需要申请后台定位权限的locationManager。
 *  @since 2.6.2
 */
- (void)amapLocationManager:(AMapLocationManager *)manager doRequireLocationAuth:(CLLocationManager*)locationManager{
    
}

/**
 *  @brief 当定位发生错误时，会调用代理的此方法。
 *  @param manager 定位 AMapLocationManager 类。
 *  @param error 返回的错误，参考 CLError 。
 */
- (void)amapLocationManager:(AMapLocationManager *)manager didFailWithError:(NSError *)error{
    
}

/**
 *  @brief 连续定位回调函数.注意：本方法已被废弃，如果实现了amapLocationManager:didUpdateLocation:reGeocode:方法，则本方法将不会回调。
 *  @param manager 定位 AMapLocationManager 类。
 *  @param location 定位结果。
 */
- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateLocation:(CLLocation *)location{
    
}

/**
 *  @brief 连续定位回调函数.注意：如果实现了本方法，则定位信息不会通过amapLocationManager:didUpdateLocation:方法回调。
 *  @param manager 定位 AMapLocationManager 类。
 *  @param location 定位结果。
 *  @param regeocode 逆地理信息。
 */
- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateLocation:(CLLocation *)location reGeocode:(AMapLocationReGeocode *)regeocode{
    //返回了数据
    //设置
    NSMutableDictionary* dic=[[NSMutableDictionary alloc]init];
    //设置经纬度
    if(location!=nil){
        dic[@"latitude"]=[NSString stringWithFormat:@"%f",location.coordinate.latitude];
        dic[@"longitude"]=[NSString stringWithFormat:@"%f",location.coordinate.longitude];
    }
    //设置
    if(regeocode!=nil){
        ///格式化地址
        if(regeocode.formattedAddress!=nil)
            dic[@"formattedAddress"]=regeocode.formattedAddress;
        ///国家
        if(regeocode.country!=nil)
            dic[@"country"]=regeocode.country;
        ///省/直辖市
        if(regeocode.province!=nil)
            dic[@"province"]=regeocode.province;
        ///市
        if(regeocode.city!=nil)
            dic[@"city"]=regeocode.city;
        ///区
        if(regeocode.district!=nil)
            dic[@"district"]=regeocode.district;
        ///城市编码
        if(regeocode.citycode!=nil)
            dic[@"citycode"]=regeocode.citycode;
        ///城市编码
        if(regeocode.adcode!=nil)
            dic[@"adcode"]=regeocode.adcode;
        ///街道名称
        if(regeocode.street!=nil)
            dic[@"street"]=regeocode.street;
        ///门牌号
        if(regeocode.number!=nil)
            dic[@"number"]=regeocode.number;
        ///兴趣点名称
        if(regeocode.POIName!=nil)
            dic[@"POIName"]=regeocode.POIName;
        ///门牌号
        if(regeocode.AOIName!=nil)
            dic[@"AOIName"]=regeocode.AOIName;
        //转换为字符串
        NSString* json=[JsonTool DicToJSONString:dic];
        //传递持续定位的数据
        if(locationEvent!=nil){
            locationEvent(json);
        }
    }
    
}

/**
 *  @brief 定位权限状态改变时回调函数
 *  @param manager 定位 AMapLocationManager 类。
 *  @param status 定位权限状态。
 */
- (void)amapLocationManager:(AMapLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status{
    
}


/**
 *  @brief 设备方向改变时回调函数
 *  @param manager 定位 AMapLocationManager 类。
 *  @param newHeading 设备朝向。
 */
- (void)amapLocationManager:(AMapLocationManager *)manager didUpdateHeading:(CLHeading *)newHeading{
    
}



@end
