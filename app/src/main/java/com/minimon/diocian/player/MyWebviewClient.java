package com.minimon.diocian.player;

import android.content.Context;
import android.content.Intent;
import android.net.UrlQuerySanitizer;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ICARUSUD on 2018. 3. 9..
 */

public class MyWebviewClient extends WebViewClient {

    private Context mContext;
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url.startsWith("minimon://goToWeb")){
//            view.loadUrl("http://dev.api.minimon.com/Test/view/channel");
            url = url.substring(18);
            String params[] = url.split("&");
            Map<String, String> map = new HashMap<>();
            for(String param : params){
                String name = param.split("=")[0];
                String value = param.split("=")[1];
                map.put(name,value);
            }
            if("channel".equals(map.get("page"))){ //페이지일 경우
                view.loadUrl("http://dev.api.minimon.com/Test/view/channel");
            }else if("episode".equals(map.get("page"))){                                  //에피소드일 경우
                EpisodeInfo.getInsatnace().setIdx(map.get("value"));
                Log.d("LoadingUrl_ep_idx","value : "+map.get("value"));
                Intent intent = new Intent(mContext,DramaPlayActivity.class);
                mContext.startActivity(intent);
            }
            Log.d("LoadingUrl",url);
           return true;
        }else {
            Log.d("LoadingUrl",url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
    public MyWebviewClient(Context context){
        mContext = context;
    }
}