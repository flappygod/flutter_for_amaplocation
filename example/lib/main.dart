import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_for_amaplocation/flutter_for_amaplocation.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initLocation();
  }

  //初始化定位系统
  void initLocation() async{


    //b96df7301ed1e6cc6732b00f264ebe4f
    //进行初始化
    FlutterForAmaplocation.initLocation("88b9df0e69bcc4c743d8dadccc60efe6");
    //FlutterForAmaplocation.initLocation("b96df7301ed1e6cc6732b00f264ebe4f");
    //获取单次的定位信息
    // Stream<Location> future= FlutterForAmaplocation.startLocation(new LocationAlwaysOption(interval: 10));
    // //获取到数据之后打印
    // future.listen((location){
    //   print(location.toJson().toString());
    // });

    // Location str =await FlutterForAmaplocation.getLocation(new LocationOneceOption());
    // print(str.toJson());


    List<AmapPoi> str =await FlutterForAmaplocation.searchKeyword(keywords: "金融大厦",types: "",city: "贵阳市");
    print(str.toString());
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: new GestureDetector(
          onTap: (){
            FlutterForAmaplocation.stopLocation();
          },
          child: Center(
            child: Text('Running on: $_platformVersion\n'),
          ),
        ),
      ),
    );
  }
}
