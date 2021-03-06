package com.banutech.collectiontreasure.common.impl;

import com.banutech.collectiontreasure.common.IHttpClient;
import com.banutech.collectiontreasure.common.IRequest;
import com.banutech.collectiontreasure.common.IResponse;
import com.banutech.collectiontreasure.util.LogUtil;
import com.blankj.utilcode.util.NetworkUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpClientImpl implements IHttpClient {

    private final OkHttpClient client;
    String urlAddress = "https://bmws.banutech.com/cxf/receivablesWebService?wsdl";

    public OkHttpClientImpl() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .readTimeout(20000, TimeUnit.MILLISECONDS)
                .writeTimeout(20000, TimeUnit.MILLISECONDS).build();
    }

    @Override
    public IResponse post(IRequest request) {
        IResponse baseResponse = null;
        Call call = null;
        try {
            baseResponse = new BaseResponse();
            String requestBody = request.createNetSoapText();
            LogUtil.logI("wak", "请求体：" + requestBody);
            RequestBody body = RequestBody.create(MediaType.parse("text/xml; charset=utf-8"), requestBody);
            Request re = new Request.Builder().url(urlAddress).post(body).build();
            call = client.newCall(re);
            if (!NetworkUtils.isConnected() && call != null) {
                baseResponse.setCode(BaseResponse.NET_EXCEPTION);
                baseResponse.setData("网络异常");
                baseResponse.setException(new IllegalArgumentException("联网错误"));
                call.cancel();
                return baseResponse;
            }

            Response response = call.execute();
            //LogUtil.logI("wak", "当前联网返回码:" + response.code());
            if (response.code() == BaseResponse.CODE_SUCCESS) {
                baseResponse.setCode(response.code());
                baseResponse.setData(response.body().string());
            } else {
                baseResponse.setCode(BaseResponse.NET_EXCEPTION);
                baseResponse.setData("网络异常");
                baseResponse.setException(new IllegalArgumentException("联网错误"));
            }

        } catch (IOException e) {
            e.printStackTrace();
            baseResponse = new BaseResponse();
            baseResponse.setCode(BaseResponse.NET_EXCEPTION);
            baseResponse.setData("Io异常");
            baseResponse.setException(e);
        }
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        return baseResponse;
    }
}
