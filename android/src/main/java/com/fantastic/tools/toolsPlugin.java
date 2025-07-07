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
import org.json.JSONArray;

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
        ),
        @Permission(
            alias = "storage",
            strings = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
        )
    }
)
public class toolsPlugin extends Plugin {

    private tools implementation = new tools();
    private static final String TAG = "FantasticWifiTools";
    private PluginCall sdCardCallbackCall;

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSArray permissionsArray = call.getArray("permissions");
        if (permissionsArray == null) {
            call.reject("permissions参数不能为空");
            return;
        }

        try {
            String[] permissions = new String[permissionsArray.length()];
            for (int i = 0; i < permissionsArray.length(); i++) {
                permissions[i] = permissionsArray.getString(i);
            }

            JSONObject permissionResults = implementation.checkPermissions(getContext(), permissions);
            
            // 直接创建权限结果对象
            JSObject result = new JSObject();
            Iterator<String> keys = permissionResults.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                result.put(key, permissionResults.getBoolean(key));
            }
            
            call.resolve(result);
        } catch (Exception e) {
            call.reject("检查权限失败: " + e.getMessage());
        }
    }

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

    /**
     * 通过DevicePolicyManager直接授予权限
     */
    @PluginMethod
    public void grantPermissions(PluginCall call) {
        JSArray permissionsArray = call.getArray("permissions");
        if (permissionsArray == null) {
            call.reject("permissions参数不能为空");
            return;
        }

        try {
            String[] permissions = new String[permissionsArray.length()];
            for (int i = 0; i < permissionsArray.length(); i++) {
                permissions[i] = permissionsArray.getString(i);
            }

            JSONObject grantResults = implementation.grantPermissions(getContext(), permissions);
            
            // 将JSONObject转换为JSObject
            JSObject result = new JSObject();
            Iterator<String> keys = grantResults.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = grantResults.get(key);
                result.put(key, value);
            }
            
            call.resolve(result);
        } catch (Exception e) {
            call.reject("授予权限失败: " + e.getMessage());
        }
    }

    /**
     * 检测设备外接端口状态
     */
    @PluginMethod
    public void checkExternalPorts(PluginCall call) {
        try {
            JSONObject portStatus = implementation.checkExternalPorts(getContext());
            
            // 将JSONObject转换为JSObject
            JSObject result = new JSObject();
            result.put("success", portStatus.optBoolean("success", false));
            
            if (portStatus.has("error")) {
                result.put("error", portStatus.optString("error"));
            }
            
            // 处理USB端口信息
            if (portStatus.has("usbPorts")) {
                JSONArray usbPorts = portStatus.getJSONArray("usbPorts");
                JSArray usbPortsArray = new JSArray();
                
                for (int i = 0; i < usbPorts.length(); i++) {
                    JSONObject portInfo = usbPorts.getJSONObject(i);
                    JSObject port = new JSObject();
                    
                    // 复制所有端口信息
                    Iterator<String> keys = portInfo.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object value = portInfo.get(key);
                        if (value instanceof String) {
                            port.put(key, (String) value);
                        } else if (value instanceof Integer) {
                            port.put(key, (Integer) value);
                        } else if (value instanceof Boolean) {
                            port.put(key, (Boolean) value);
                        }
                    }
                    
                    usbPortsArray.put(port);
                }
                
                result.put("usbPorts", usbPortsArray);
            }
            
            // 处理Type-C端口信息
            if (portStatus.has("typeC")) {
                JSONObject typeCInfo = portStatus.getJSONObject("typeC");
                JSObject typeC = new JSObject();
                
                Iterator<String> keys = typeCInfo.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = typeCInfo.get(key);
                    if (value instanceof String) {
                        typeC.put(key, (String) value);
                    } else if (value instanceof Boolean) {
                        typeC.put(key, (Boolean) value);
                    }
                }
                
                result.put("typeC", typeC);
            }
            
            // 处理TF卡信息
            if (portStatus.has("tfCard")) {
                JSONObject tfCardInfo = portStatus.getJSONObject("tfCard");
                JSObject tfCard = new JSObject();
                
                Iterator<String> keys = tfCardInfo.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = tfCardInfo.get(key);
                    if (value instanceof String) {
                        tfCard.put(key, (String) value);
                    } else if (value instanceof Boolean) {
                        tfCard.put(key, (Boolean) value);
                    } else if (value instanceof Long) {
                        tfCard.put(key, (Long) value);
                    }
                }
                
                result.put("tfCard", tfCard);
            }
            
            call.resolve(result);
        } catch (Exception e) {
            call.reject("检测外接端口状态失败: " + e.getMessage());
        }
    }

    /**
     * 开始监听TF卡槽状态
     */
    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void startMonitoringSDCard(PluginCall call) {
        try {
            // 保存回调，用于后续事件通知
            this.sdCardCallbackCall = call;
            call.setKeepAlive(true);

            boolean success = implementation.startMonitoringSDCard(getContext(), new tools.SDCardStateCallback() {
                @Override
                public void onSDCardStateChanged(JSONObject state) {
                    if (sdCardCallbackCall != null) {
                        try {
                            JSObject result = new JSObject();
                            Iterator<String> keys = state.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                Object value = state.get(key);
                                if (value instanceof String) {
                                    result.put(key, (String) value);
                                } else if (value instanceof Boolean) {
                                    result.put(key, (Boolean) value);
                                } else if (value instanceof Integer) {
                                    result.put(key, (Integer) value);
                                } else if (value instanceof Long) {
                                    result.put(key, (Long) value);
                                }
                            }
                            notifyListeners("sdCardStateChanged", result);
                        } catch (Exception e) {
                            Log.e(TAG, "发送TF卡状态变化通知时出错: " + e.getMessage());
                        }
                    }
                }
            });

            JSObject result = new JSObject();
            result.put("success", success);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("启动TF卡状态监听失败: " + e.getMessage());
        }
    }

    /**
     * 停止监听TF卡槽状态
     */
    @PluginMethod
    public void stopMonitoringSDCard(PluginCall call) {
        try {
            implementation.stopMonitoringSDCard(getContext());
            
            // 清理回调
            if (sdCardCallbackCall != null) {
                sdCardCallbackCall.release(bridge);
                sdCardCallbackCall = null;
            }
            
            JSObject result = new JSObject();
            result.put("success", true);
            call.resolve(result);
        } catch (Exception e) {
            call.reject("停止TF卡状态监听失败: " + e.getMessage());
        }
    }

    /**
     * 从TF卡中读取CSV文件并获取第一个可用的license
     */
    @PluginMethod
    public void getAvailableLicenseFromSD(PluginCall call) {
        String csvFileName = call.getString("fileName");
        if (csvFileName == null || csvFileName.isEmpty()) {
            call.reject("文件名不能为空");
            return;
        }

        try {
            JSONObject result = implementation.getAvailableLicenseFromSD(getContext(), csvFileName);
            
            // 将JSONObject转换为JSObject
            JSObject jsResult = new JSObject();
            jsResult.put("success", result.optBoolean("success", false));
            
            if (result.has("error")) {
                jsResult.put("error", result.optString("error"));
            }
            
            if (result.has("license")) {
                jsResult.put("license", result.optString("license"));
            }
            
            call.resolve(jsResult);
        } catch (Exception e) {
            call.reject("读取license失败: " + e.getMessage());
        }
    }

    /**
     * 检查应用是否被重新签名
     */
    @PluginMethod
    public void checkAppSignature(PluginCall call) {
        try {
            JSONObject result = implementation.checkAppSignature(getContext());
            
            // 将JSONObject转换为JSObject
            JSObject ret = new JSObject();
            
            // 基本信息
            ret.put("success", result.optBoolean("success", false));
            ret.put("packageName", result.optString("packageName", ""));
            ret.put("currentSignature", result.optString("currentSignature", ""));
            ret.put("isOriginalSignature", result.optBoolean("isOriginalSignature", false));
            
            // 错误信息（如果有）
            if (result.has("error")) {
                ret.put("error", result.getString("error"));
            }
            
            // 处理签名详情
            if (result.has("signatureDetails")) {
                JSONObject details = result.getJSONObject("signatureDetails");
                JSObject signatureDetails = new JSObject();
                
                // 签名值（原始格式）
                signatureDetails.put("md5", details.optString("md5", ""));
                signatureDetails.put("sha1", details.optString("sha1", ""));
                signatureDetails.put("sha256", details.optString("sha256", ""));
                
                // 签名值（冒号分隔格式）
                signatureDetails.put("md5_formatted", details.optString("md5_formatted", ""));
                signatureDetails.put("sha1_formatted", details.optString("sha1_formatted", ""));
                signatureDetails.put("sha256_formatted", details.optString("sha256_formatted", ""));
                
                // 证书信息
                signatureDetails.put("issuer", details.optString("issuer", ""));
                signatureDetails.put("subject", details.optString("subject", ""));
                signatureDetails.put("serialNumber", details.optString("serialNumber", ""));
                signatureDetails.put("validFrom", details.optString("validFrom", ""));
                signatureDetails.put("validUntil", details.optString("validUntil", ""));
                
                ret.put("signatureDetails", signatureDetails);
            }
            
            call.resolve(ret);
        } catch (Exception e) {
            Log.e(TAG, "检查应用签名失败: " + e.getMessage());
            call.reject("检查应用签名失败: " + e.getMessage());
        }
    }

    /**
     * 验证设备日期和时间
     */
    @PluginMethod
    public void checkDeviceDateTime(PluginCall call) {
        try {
            tools tools = new tools();
            JSONObject result = tools.checkDeviceDateTime(getContext());
            JSObject ret = JSObject.fromJSONObject(result);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("检查设备日期时间失败: " + e.getMessage());
        }
    }

    @PluginMethod()
    public void checkWebViewInfo(PluginCall call) {
        try {
            tools tools = new tools();
            JSONObject result = tools.checkWebViewInfo(getContext());
            JSObject ret = JSObject.fromJSONObject(result);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("获取WebView信息失败: " + e.getMessage());
        }
    }

    @PluginMethod()
    public void getHardwareInfo(PluginCall call) {
        try {
            tools tools = new tools();
            JSONObject result = tools.getHardwareInfo(getContext());
            JSObject ret = JSObject.fromJSONObject(result);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("获取硬件信息失败: " + e.getMessage());
        }
    }
}
