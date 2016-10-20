# OPenLocalMapDemo
Android应用中打开本地地图进行导航


#1.需求
在Android应用中打开百度地图或者高德地图进行路线规划，如果没有安装则打开网页百度地图进行路线规划。


#2.API

##2.1 打开百度地图应用

[地址：http://lbsyun.baidu.com/index.php?title=uri/api/android](http://lbsyun.baidu.com/index.php?title=uri/api/android)

打开文档可以看到功能还是很多的，这里只介绍 **公交、驾车、导航、步行和骑行导航**

![app_route_api](https://github.com/LineChen/OPenLocalMapDemo/blob/master/screenshot/app_route_api.png)


**注：必选项一定要填**

##2.2 打开浏览器并跳转到网页百度地图

> 参数

![web_route_api](https://github.com/LineChen/OPenLocalMapDemo/blob/master/screenshot/web_route_api.png)


#2.3.打开高德地图应用

[地址：http://lbs.amap.com/api/uri-api/guide/android-uri-explain/info/](http://lbs.amap.com/api/uri-api/guide/android-uri-explain/info/)

>参数

![gd_app_route_api](https://github.com/LineChen/OPenLocalMapDemo/blob/master/screenshot/gd_app_route_api.png)


#3.使用

> 封装了一个工具类，具体请看代码

```java
public class OpenLocalMapUtil {

    /**
     * 地图应用是否安装
     * @return
     */
    public static boolean isGdMapInstalled(){
        return isInstallPackage("com.autonavi.minimap");
    }

    public static boolean isBaiduMapInstalled(){
        return isInstallPackage("com.baidu.BaiduMap");
    }

    private static boolean isInstallPackage(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    /**
     * 获取打开百度地图应用uri [http://lbsyun.baidu.com/index.php?title=uri/api/android]
     * @param originLat
     * @param originLon
     * @param desLat
     * @param desLon
     * @return
     */
    public static String getBaiduMapUri(String originLat, String originLon, String originName, String desLat, String desLon, String destination, String region, String src){
        String uri = "intent://map/direction?origin=latlng:%1$s,%2$s|name:%3$s" +
                "&destination=latlng:%4$s,%5$s|name:%6$s&mode=driving&region=%7$s&src=%8$s#Intent;" +
                "scheme=bdapp;package=com.baidu.BaiduMap;end";

        return String.format(uri, originLat, originLon, originName, desLat, desLon, destination, region, src);
    }

    /**
     * 获取打开高德地图应用uri
     */
    public static String getGdMapUri(String appName, String slat, String slon, String sname, String dlat, String dlon, String dname){
        String uri = "androidamap://route?sourceApplication=%1$s&slat=%2$s&slon=%3$s&sname=%4$s&dlat=%5$s&dlon=%6$s&dname=%7$s&dev=0&m=0&t=2";
        return String.format(uri, appName, slat, slon, sname, dlat, dlon, dname);
    }


    /**
     * 网页版百度地图 有经纬度
     * @param originLat
     * @param originLon
     * @param originName ->注：必填
     * @param desLat
     * @param desLon
     * @param destination
     * @param region : 当给定region时，认为起点和终点都在同一城市，除非单独给定起点或终点的城市。-->注：必填，不填不会显示导航路线
     * @param appName
     * @return
     */
    public static String getWebBaiduMapUri(String originLat, String originLon, String originName, String desLat, String desLon, String destination, String region, String appName) {
        String uri = "http://api.map.baidu.com/direction?origin=latlng:%1$s,%2$s|name:%3$s" +
                "&destination=latlng:%4$s,%5$s|name:%6$s&mode=driving&region=%7$s&output=html" +
                "&src=%8$s";
        return String.format(uri, originLat, originLon, originName, desLat, desLon, destination, region, appName);
    }



    /**
     * 百度地图定位经纬度转高德经纬度
     * @param bd_lat
     * @param bd_lon
     * @return
     */
    public static double[] bdToGaoDe(double bd_lat, double bd_lon) {
        double[] gd_lat_lon = new double[2];
        double PI = 3.14159265358979324 * 3000.0 / 180.0;
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI);
        gd_lat_lon[0] = z * Math.cos(theta);
        gd_lat_lon[1] = z * Math.sin(theta);
        return gd_lat_lon;
    }

    /**
     * 高德地图定位经纬度转百度经纬度
     * @param gd_lon
     * @param gd_lat
     * @return
     */
    public static double[] gaoDeToBaidu(double gd_lon, double gd_lat) {
        double[] bd_lat_lon = new double[2];
        double PI = 3.14159265358979324 * 3000.0 / 180.0;
        double x = gd_lon, y = gd_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * PI);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * PI);
        bd_lat_lon[0] = z * Math.cos(theta) + 0.0065;
        bd_lat_lon[1] = z * Math.sin(theta) + 0.006;
        return bd_lat_lon;
    }
}

```

**注：百度地图和高德地图使用的坐标系不同，但是用上面两个方法转换的坐标还是有问题。测试时把百度地图定位的坐标用上面的方法转换成高德地图的坐标，然后在高德地图网站对转换后的坐标进行查找，能找到正确的位置，然而打开高德地图app后提示位置不在支持范围内。幸运的是，高德地图sdk中有相应的工具类CoordinateConverter对坐标进行转换。**

>打开百度地图

```java
    /**
     *  打开百度地图
     */
    private void openBaiduMap(double slat, double slon, String sname, double dlat, double dlon, String dname, String city) {
        if(OpenLocalMapUtil.isBaiduMapInstalled()){
            try {
                String uri = OpenLocalMapUtil.getBaiduMapUri(String.valueOf(slat), String.valueOf(slon), sname,
                        String.valueOf(dlat), String.valueOf(dlon), dname, city, SRC);
                Intent intent = Intent.parseUri(uri, 0);
                startActivity(intent); //启动调用

                isOpened = true;
            } catch (Exception e) {
                isOpened = false;
                e.printStackTrace();
            }
        } else{
            isOpened = false;
        }
    }

```


>打开浏览器进行百度地图导航

```java
 /**
     * 打开浏览器进行百度地图导航
     */
    private void openWebMap(double slat, double slon, String sname, double dlat, double dlon, String dname, String city){
        Uri mapUri = Uri.parse(OpenLocalMapUtil.getWebBaiduMapUri(String.valueOf(slat), String.valueOf(slon), sname,
                String.valueOf(dlat), String.valueOf(dlon),
                dname, city, APP_NAME));
        Intent loction = new Intent(Intent.ACTION_VIEW, mapUri);
        startActivity(loction);
    }

```

>打开高德地图

```java
/**
     * 打开高德地图
     */
    private void openGaoDeMap(double slat, double slon, String sname, double dlat, double dlon, String dname) {
        if(OpenLocalMapUtil.isGdMapInstalled()){
            try {
            //百度地图定位坐标转换成高德地图可识别坐标
                CoordinateConverter converter= new CoordinateConverter(this);
                converter.from(CoordinateConverter.CoordType.BAIDU);
                DPoint sPoint = null, dPoint = null;
                try {
                    converter.coord(new DPoint(slat, slon));
                    sPoint = converter.convert();
                    converter.coord(new DPoint(dlat, dlon));
                    dPoint = converter.convert();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (sPoint != null && dPoint != null) {
                    String uri = OpenLocalMapUtil.getGdMapUri(APP_NAME, String.valueOf(sPoint.getLatitude()), String.valueOf(sPoint.getLongitude()),
                            sname, String.valueOf(dPoint.getLatitude()), String.valueOf(dPoint.getLongitude()), dname);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.autonavi.minimap");
                    intent.setData(Uri.parse(uri));
                    startActivity(intent); //启动调用

                    isOpened = true;
                }
            } catch (Exception e) {
                isOpened = false;
                e.printStackTrace();
            }
        } else{
            isOpened = false;
        }
    }

```

效果图：

![这里写图片描述](https://github.com/LineChen/OPenLocalMapDemo/blob/master/screenshot/Screenshot_2016-10-20-09-57-22-148_com.baidu.Baid.png)