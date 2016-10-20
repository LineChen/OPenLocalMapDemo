package com.beiing.openlocalmapdemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.cocosw.bottomsheet.BottomSheet;

/**
 * 注意：起点位置需要通过定位来获取当前位置，否则查不到结果
 */
public class MainActivity extends AppCompatActivity {

    /**
     * 当前位置
     */
    private static double[] START_LATLON = {120.11649,30.272873};
    /**
     * 目的地
     */
    private static double[] DESTINATION_TA_LATLON = {120.156132,30.237626};

    private String SNAME = "起点";

    private String DNAME = "终点";

    private String CITY = "杭州";

    private static String APP_NAME = "OPenLocalMapDemo";

    private static String SRC = "thirdapp.navi.beiing.openlocalmapdemo";

    private boolean isOpened;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOpened = false;
    }

    public void openLocalMap(View view) {
        openLocalMap(START_LATLON[0], START_LATLON[1], SNAME,  CITY);
    }

    public void openBaiduMap(View view) {
        openBaiduMap(START_LATLON[0], START_LATLON[1], SNAME, DESTINATION_TA_LATLON[0], DESTINATION_TA_LATLON[1], DNAME, CITY);
    }

    public void openWebBaiduMap(View view) {
        openWebMap(START_LATLON[0], START_LATLON[1], SNAME, DESTINATION_TA_LATLON[0], DESTINATION_TA_LATLON[1], DNAME, CITY);
    }

    public void openGaodeMap(View view) {
        openGaoDeMap(START_LATLON[0], START_LATLON[1], SNAME, DESTINATION_TA_LATLON[0], DESTINATION_TA_LATLON[1], DNAME);
    }

    /**
     *
     * @param slat
     * @param slon
     * @param address 当前位置
     * @param city 所在城市
     */
    private void openLocalMap(double slat, double slon, String address, String city) {
        if(OpenLocalMapUtil.isBaiduMapInstalled() && OpenLocalMapUtil.isGdMapInstalled()){
            chooseOpenMap(slat, slon, address, city);
        } else {
            openBaiduMap(slat, slon, address, DESTINATION_TA_LATLON[0], DESTINATION_TA_LATLON[1], DNAME, city);

            if(!isOpened){
                openGaoDeMap(slat, slon, address, DESTINATION_TA_LATLON[0], DESTINATION_TA_LATLON[1], DNAME);
            }

            if(!isOpened){
                //打开网页地图
                openWebMap(slat, slon, address, DESTINATION_TA_LATLON[0], DESTINATION_TA_LATLON[1], DNAME, city);
            }
        }

    }

    /**
     * 如果两个地图都安装，提示选择
     * @param slat
     * @param slon
     * @param address
     * @param city
     */
    private void chooseOpenMap(final double slat, final double slon, final String address, final String city) {
        BottomSheet.Builder builder = new BottomSheet
                .Builder(this, com.cocosw.bottomsheet.R.style.BottomSheet_Dialog)
                .title("请选择");
        builder.sheet(0, "百度地图").sheet(1, "高德地图")
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            openBaiduMap(slat, slon, address, DESTINATION_TA_LATLON[0], DESTINATION_TA_LATLON[1], DNAME, city);
                        } else if(which == 1){
                            openGaoDeMap(slat, slon, address, DESTINATION_TA_LATLON[0], DESTINATION_TA_LATLON[1], DNAME);
                        }
                    }
                }).build().show();
    }

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

    /**
     * 打开高德地图
     */
    private void openGaoDeMap(double slat, double slon, String sname, double dlat, double dlon, String dname) {
        if(OpenLocalMapUtil.isGdMapInstalled()){
            try {
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


}
