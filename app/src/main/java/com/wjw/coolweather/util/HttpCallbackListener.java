package com.wjw.coolweather.util;

/**
 * Created by wangjianwei on 2016/6/1.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
