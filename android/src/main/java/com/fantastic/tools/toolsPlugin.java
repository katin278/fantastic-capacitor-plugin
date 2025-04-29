package com.fantastic.tools;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

@CapacitorPlugin(
    name = "tools",
    permissions = {
        @Permission(
            alias = "location",
            strings = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            }
        ),
        @Permission(
            alias = "wifi",
            strings = {
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            }
        )
    }
)
public class toolsPlugin extends Plugin {

    private tools implementation = new tools();
    private static final String TAG = "FantasticWifiTools";

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }
    
    /**
     * 检查是否有Wi-Fi相关权限
     */
    @PluginMethod
    public void checkWifiPermissions(PluginCall call) {
        JSObject result = new JSObject();
        
        boolean hasPermissions = implementation.checkWifiPermissions(getContext());
        result.put("hasPermissions", hasPermissions);
        
        call.resolve(result);
    }
    
    /**
     * 请求Wi-Fi相关权限
     */
    @PluginMethod
    public void requestWifiPermissions(PluginCall call) {
        // 保存call以在权限回调中使用
        saveCall(call);
        
        // 请求位置权限（必需）
        requestPermissionForAlias("location", call, "wifiPermissionsCallback");
    }
    
    /**
     * 位置权限回调
     */
    @PermissionCallback
    private void wifiPermissionsCallback(PluginCall call) {
        // 检查位置权限结果
        if (getPermissionState("location") == PermissionState.GRANTED) {
            // 位置权限已授予，请求Wi-Fi权限
            requestPermissionForAlias("wifi", call, "wifiStatePermissionsCallback");
        } else {
            // 位置权限被拒绝
            JSObject result = new JSObject();
            result.put("granted", false);
            result.put("locationPermission", false);
            result.put("wifiPermission", false);
            
            call.resolve(result);
        }
    }
    
    /**
     * Wi-Fi权限回调
     */
    @PermissionCallback
    private void wifiStatePermissionsCallback(PluginCall call) {
        JSObject result = new JSObject();
        boolean locationGranted = getPermissionState("location") == PermissionState.GRANTED;
        boolean wifiGranted = getPermissionState("wifi") == PermissionState.GRANTED;
        
        result.put("granted", locationGranted && wifiGranted);
        result.put("locationPermission", locationGranted);
        result.put("wifiPermission", wifiGranted);
        
        call.resolve(result);
    }
    
    /**
     * 获取附近Wi-Fi网络详细信息列表
     */
    @PluginMethod
    public void getWifiList(PluginCall call) {
        if (!implementation.checkWifiPermissions(getContext())) {
            call.reject("需要Wi-Fi和位置权限才能获取Wi-Fi列表");
            return;
        }
        
        try {
            List<JSONObject> wifiDetailList = implementation.getWifiList(getContext());
            
            JSArray wifiArray = new JSArray();
            for (JSONObject wifiDetail : wifiDetailList) {
                JSObject wifiObject = new JSObject();
                
                // 使用迭代器获取JSONObject中的所有键
                Iterator<String> keys = wifiDetail.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
                        // 根据值的类型进行处理
                        Object value = wifiDetail.get(key);
                        if (value instanceof String) {
                            wifiObject.put(key, (String) value);
                        } else if (value instanceof Integer) {
                            wifiObject.put(key, (Integer) value);
                        } else if (value instanceof Boolean) {
                            wifiObject.put(key, (Boolean) value);
                        } else if (value instanceof Double) {
                            wifiObject.put(key, (Double) value);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "处理Wi-Fi信息时出错: " + e.getMessage());
                    }
                }
                
                wifiArray.put(wifiObject);
            }
            
            JSObject result = new JSObject();
            result.put("wifiList", wifiArray);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("获取Wi-Fi列表失败: " + e.getMessage());
        }
    }

    /**
     * 连接到开放Wi-Fi网络（无密码）
     */
    @PluginMethod
    public void connectToOpenWifi(PluginCall call) {
        if (!implementation.checkWifiPermissions(getContext())) {
            call.reject("需要Wi-Fi和位置权限才能连接Wi-Fi");
            return;
        }
        
        String ssid = call.getString("ssid");
        if (ssid == null || ssid.isEmpty()) {
            call.reject("SSID不能为空");
            return;
        }
        
        try {
            boolean success = implementation.connectToOpenWifi(getContext(), ssid);
            
            JSObject result = new JSObject();
            result.put("success", success);
            if (!success) {
                result.put("message", "连接失败，请检查Wi-Fi是否可用");
            }
            
            call.resolve(result);
        } catch (Exception e) {
            call.reject("连接Wi-Fi失败: " + e.getMessage());
        }
    }
    
    /**
     * 连接到个人Wi-Fi网络（需要密码）
     */
    @PluginMethod
    public void connectToPersonalWifi(PluginCall call) {
        if (!implementation.checkWifiPermissions(getContext())) {
            call.reject("需要Wi-Fi和位置权限才能连接Wi-Fi");
            return;
        }
        
        String ssid = call.getString("ssid");
        String password = call.getString("password");
        
        if (ssid == null || ssid.isEmpty()) {
            call.reject("SSID不能为空");
            return;
        }
        
        if (password == null || password.isEmpty()) {
            call.reject("密码不能为空");
            return;
        }
        
        try {
            boolean success = implementation.connectToPersonalWifi(getContext(), ssid, password);
            
            JSObject result = new JSObject();
            result.put("success", success);
            if (!success) {
                result.put("message", "连接失败，请检查Wi-Fi密码是否正确");
            }
            
            call.resolve(result);
        } catch (Exception e) {
            call.reject("连接Wi-Fi失败: " + e.getMessage());
        }
    }
    
    /**
     * 连接到企业Wi-Fi网络（需要密码和身份）
     */
    @PluginMethod
    public void connectToEnterpriseWifi(PluginCall call) {
        if (!implementation.checkWifiPermissions(getContext())) {
            call.reject("需要Wi-Fi和位置权限才能连接Wi-Fi");
            return;
        }
        
        String ssid = call.getString("ssid");
        String password = call.getString("password");
        String identity = call.getString("identity");
        
        if (ssid == null || ssid.isEmpty()) {
            call.reject("SSID不能为空");
            return;
        }
        
        if (password == null || password.isEmpty()) {
            call.reject("密码不能为空");
            return;
        }
        
        if (identity == null || identity.isEmpty()) {
            call.reject("身份不能为空");
            return;
        }
        
        try {
            boolean success = implementation.connectToEnterpriseWifi(getContext(), ssid, password, identity);
            
            JSObject result = new JSObject();
            result.put("success", success);
            if (!success) {
                result.put("message", "连接失败，请检查Wi-Fi凭据是否正确");
            }
            
            call.resolve(result);
        } catch (Exception e) {
            call.reject("连接Wi-Fi失败: " + e.getMessage());
        }
    }
}
