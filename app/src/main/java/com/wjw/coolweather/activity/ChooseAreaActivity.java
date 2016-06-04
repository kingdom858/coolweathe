package com.wjw.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wjw.coolweather.R;
import com.wjw.coolweather.db.CoolWeatherDB;
import com.wjw.coolweather.model.City;
import com.wjw.coolweather.model.County;
import com.wjw.coolweather.model.Province;
import com.wjw.coolweather.util.HttpCallbackListener;
import com.wjw.coolweather.util.HttpUtil;
import com.wjw.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangjianwei on 2016/6/2.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();
    /**
     * 省列表
     */
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;
    private City selectedCity;

    /**
     * 当前选中级别
     */
    private int currentLevel;

    /**
     * 是否从天气页面跳转过来
     */
    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("city_selected",false)&&!isFromWeatherActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题
        setContentView(R.layout.choose_area);
        listView = (ListView)findViewById(R.id.list_view);
        titleText = (TextView)findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);//默认适配器
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);//获取数据库工具实例
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//列表项目点击监听
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){ //如果当前级别是省级
                    selectedProvince = provinceList.get(position); //保存当前点击的省级名称
                    queryCities();//查询市级数据,设置到datalist中展现
                }else if(currentLevel == LEVEL_CITY){//如果当前级别是市级
                    selectedCity = cityList.get(position);//保存当前点击的市级名称
                    queryCounties();//查询县级数据,设置到datalist中展现
                }else if (currentLevel == LEVEL_COUNTY){
                    String countyCode = countyList.get(position).getCountyCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();//查询省级数据,设置到datalist中展现
    }

    /**
     * 查询全国省级,优先从数据库查询,如果没有再去服务器查询
     */
    private void queryProvinces(){
        provinceList = coolWeatherDB.loadProvinces();
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(null,"province");
        }
    }

    /**
     * 查询所选省内的所有市,优先从数据库差,没有再从服务器查
     */
    private void queryCities(){
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }

    /**
     * 查询选中市内的所有县
     */
    private void queryCounties(){
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }

    /**
     * 从服务器查询数据
     */
    private void queryFromServer(final String code,final String type){
        String address;
        if(!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result=false;
                if("province".equals(type)){
                    result = Utility.handleProvincesResponse(coolWeatherDB,response);
                }else if("city".equals(type)){
                    result = Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void  closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    /**
     * 捕获back键
     */
    public void onBackPressed(){
        if(currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
        }else {
            if(isFromWeatherActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
