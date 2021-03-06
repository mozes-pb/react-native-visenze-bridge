package com.rbzlib;

import android.util.Log;
import android.net.Uri;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import com.visenze.visearch.android.BaseSearchParams;
import com.visenze.visearch.android.ColorSearchParams;
import com.visenze.visearch.android.IdSearchParams;
import com.visenze.visearch.android.ResultList;
import com.visenze.visearch.android.TrackParams;
import com.visenze.visearch.android.UploadSearchParams;
import com.visenze.visearch.android.ViSearch;
import com.visenze.visearch.android.model.Image;
import com.visenze.visearch.android.model.ImageResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

public class RNVisenzeBridgeModule extends ReactContextBaseJavaModule {

    private static final String VISENZE_RESULT_EVENT = "VisenzeResultEvent";
    private final ReactApplicationContext reactContext;
    private ViSearch viSearch;
    private ViSearch.ResultListener visearchListener;
    final static String ModuleName = "RNVisenzeBridge";

    public RNVisenzeBridgeModule(ReactApplicationContext reactContext) {
      super(reactContext);
      this.reactContext = reactContext;
    }

    @Override
    public String getName() {
      return ModuleName;
    }

    @ReactMethod
    public void start(final String appkey) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("RNVisenzeBridge", "start is called");
                viSearch = new ViSearch.Builder(appkey).build(reactContext);
                visearchListener = new ViSearch.ResultListener() {
                    @Override
                    public void onSearchResult(ResultList resultList) {
                        JSONArray data = new JSONArray();
                        for (ImageResult imageResult : resultList.getImageList()) {
                            JSONObject jsonImage = new JSONObject(imageResult.getMetaData());
                            data.put(jsonImage);
                        }
                        try {
                            RCTNativeAppEventEmitter eventEmitter = getReactApplicationContext().getJSModule(RCTNativeAppEventEmitter.class);
                            eventEmitter.emit(VISENZE_RESULT_EVENT, JsonConvert.jsonToReact(data));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSearchError(String errorMessage) {
                        Log.i(ModuleName, "Search Error");
                    }

                    @Override
                    public void onSearchCanceled() {
                        Log.i(ModuleName, "Search Canceled");
                    }
                };
                viSearch.setListener(visearchListener);
            }
        });
    }

    @ReactMethod
    public void searchById(final String id){
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BaseSearchParams baseSearchParams = new BaseSearchParams();
                baseSearchParams.setGetAllFl(true);
                IdSearchParams idSearchParams = new IdSearchParams(id);
                idSearchParams.setBaseSearchParams(baseSearchParams);
                viSearch.idSearch(idSearchParams);
            }
        });
    }

    @ReactMethod
    public void searchByUrl(final String url, final String limitDetection){
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BaseSearchParams baseSearchParams = new BaseSearchParams();
                baseSearchParams.setGetAllFl(true);
                UploadSearchParams  uploadSearchParams = new UploadSearchParams(url);
                uploadSearchParams.setDetection(limitDetection);
                uploadSearchParams.setBaseSearchParams(baseSearchParams);
                viSearch.uploadSearch(uploadSearchParams);
            }
        });
    }

    @ReactMethod
    public void searchByPath(final String path, final String limitDetection){
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Image image = new Image(path, Image.ResizeSettings.STANDARD);
                BaseSearchParams baseSearchParams = new BaseSearchParams();
                baseSearchParams.setGetAllFl(true);
                UploadSearchParams  uploadSearchParams = new UploadSearchParams();
                uploadSearchParams.setImage(image);
                uploadSearchParams.setDetection(limitDetection);
                uploadSearchParams.setBaseSearchParams(baseSearchParams);
                viSearch.uploadSearch(uploadSearchParams);
            }
        });
    }

    @ReactMethod
    public void searchByUri(final String uri, final String limitDetection){
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Image image = new Image(reactContext, Uri.parse(uri), Image.ResizeSettings.STANDARD);
                BaseSearchParams baseSearchParams = new BaseSearchParams();
                baseSearchParams.setGetAllFl(true);
                UploadSearchParams  uploadSearchParams = new UploadSearchParams();
                uploadSearchParams.setImage(image);
                uploadSearchParams.setDetection(limitDetection);
                uploadSearchParams.setBaseSearchParams(baseSearchParams);
                viSearch.uploadSearch(uploadSearchParams);
            }
        });
    }

    @ReactMethod
    public void searchByColor(final String hexString){
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BaseSearchParams baseSearchParams = new BaseSearchParams();
                baseSearchParams.setGetAllFl(true);
                ColorSearchParams colorSearchParams = new ColorSearchParams(hexString);
                colorSearchParams.setBaseSearchParams(baseSearchParams);
                viSearch.colorSearch(colorSearchParams);
            }
        });
    }

    @ReactMethod
    public void trackSearchResultClickEvent(final String imageName, final String requestID){
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viSearch.track(new TrackParams()
                    .setAction("click").setImName(imageName)
                    .setReqid(requestID));
            }
        });
    }
}