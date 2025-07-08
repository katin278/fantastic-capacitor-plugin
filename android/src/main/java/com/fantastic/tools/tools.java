package com.fantastic.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.NetworkRequest;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbConstants;
import android.os.Environment;
import android.os.StatFs;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.storage.StorageManager;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import android.content.BroadcastReceiver;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataOutputStream;

import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.ByteArrayInputStream;
import java.util.Formatter;

import android.webkit.WebView;
import android.webkit.WebSettings;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.app.ActivityManager;

public class tools {
    private static final String TAG = "FantasticWifiTools";
    private BroadcastReceiver sdCardReceiver;
    private boolean isListeningToSdCard = false;
    private SDCardStateCallback sdCardCallback;

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    /**
     * 检查Wi-Fi相关权限
     * @param context Android上下文
     * @return 权限检查结果
     */
    public boolean checkWifiPermissions(Context context) {
        // 检查ACCESS_FINE_LOCATION权限（Android 6.0+需要位置权限才能获取Wi-Fi信息）
        boolean hasFineLocationPermission = context.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        
        // 检查ACCESS_WIFI_STATE权限
        boolean hasWifiStatePermission = context.checkSelfPermission(
                Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
                
        return hasFineLocationPermission && hasWifiStatePermission;
    }

    /**
     * 获取需要请求的Wi-Fi权限列表
     * @return 需要请求的权限数组
     */
    public String[] getRequiredWifiPermissions() {
        // Android 6.0+需要位置权限才能获取Wi-Fi信息，Android 10+还需要FINE_LOCATION
        List<String> permissions = new ArrayList<>();
        
        // 基础Wi-Fi权限
        permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        permissions.add(Manifest.permission.CHANGE_WIFI_STATE);
        
        // 位置权限（Android 6.0+要求获取Wi-Fi信息）
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        
        // Android 10+ 需要精确位置获取完整的Wi-Fi信息
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        
        return permissions.toArray(new String[0]);
    }

    /**
     * 获取周围Wi-Fi网络的详细信息
     * @param context Android上下文
     * @return Wi-Fi信息列表
     */
    public List<JSONObject> getWifiList(Context context) {
        List<JSONObject> wifiDetailList = new ArrayList<>();
        
        // 检查权限
        if (!checkWifiPermissions(context)) {
            Log.e(TAG, "缺少获取Wi-Fi信息的权限");
            return wifiDetailList;
        }
        
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            
            if (wifiManager != null) {
                // 启动扫描
                boolean scanStarted = wifiManager.startScan();
                Log.d(TAG, "Wi-Fi扫描启动: " + scanStarted);
            
                // 获取当前连接的Wi-Fi信息
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                String connectedSSID = "";
                
                if (connectionInfo != null && connectionInfo.getSSID() != null) {
                    connectedSSID = connectionInfo.getSSID();
                    // 去除SSID两端的引号（如果有）
                    if (connectedSSID.startsWith("\"") && connectedSSID.endsWith("\"")) {
                        connectedSSID = connectedSSID.substring(1, connectedSSID.length() - 1);
                    }
                }
                
                // 获取扫描结果
                List<ScanResult> scanResults = wifiManager.getScanResults();
                if (scanResults != null) {
                    Log.d(TAG, "扫描到 " + scanResults.size() + " 个Wi-Fi网络");
                    
                    for (ScanResult scanResult : scanResults) {
                        if (scanResult.SSID != null && !scanResult.SSID.isEmpty()) {
                            try {
                                JSONObject wifiInfo = new JSONObject();
                                
                                // 基本信息
                                wifiInfo.put("ssid", scanResult.SSID);
                                wifiInfo.put("bssid", scanResult.BSSID);
                                
                                // 信号强度（dBm，通常是负值，越接近0信号越强）
                                wifiInfo.put("level", scanResult.level);
                                
                                // 计算信号百分比（范围转换为0-100%）
                                int signalLevel = WifiManager.calculateSignalLevel(scanResult.level, 101);
                                wifiInfo.put("signalStrength", signalLevel);
                                
                                // 频率（MHz）
                                wifiInfo.put("frequency", scanResult.frequency);
                                
                                // 连接状态
                                boolean isConnected = !connectedSSID.isEmpty() && 
                                                     connectedSSID.equals(scanResult.SSID);
                                wifiInfo.put("isConnected", isConnected);
                                
                                // 安全协议
                                String securityType = getSecurityType(scanResult);
                                wifiInfo.put("securityType", securityType);
                                
                                // 是否为企业级Wi-Fi（通常为WPA2-Enterprise/WPA3-Enterprise）
                                boolean isEnterprise = isEnterpriseNetwork(securityType);
                                wifiInfo.put("isEnterprise", isEnterprise);
                                
                                wifiDetailList.add(wifiInfo);
                            } catch (JSONException e) {
                                Log.e(TAG, "创建Wi-Fi信息JSON出错: " + e.getMessage());
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Wi-Fi扫描结果为空");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取Wi-Fi列表出错: " + e.getMessage());
        }
        
        return wifiDetailList;
    }
    
    /**
     * 获取Wi-Fi安全协议类型
     * @param scanResult Wi-Fi扫描结果
     * @return 安全协议类型
     */
    private String getSecurityType(ScanResult scanResult) {
        // 从scanResult获取capabilities字符串
        String capabilities = scanResult.capabilities;
        Log.d(TAG, "Wi-Fi capabilities: " + capabilities);
             
        // 更精确的安全类型判断
        if (capabilities.contains("WEP")) {
            return "WEP";
        } 
        else if (capabilities.contains("RSN-OWE_TRANSITION-CCMP")) {
            return "Open";
        }
        else if (capabilities.contains("WPA3-Enterprise")) {
            return "WPA3-Enterprise";
        } else if (capabilities.contains("WPA3") || capabilities.contains("SAE")) {
            return "WPA3-Personal";
        } else if (capabilities.contains("WPA2-Enterprise") || 
                  (capabilities.contains("WPA2") && capabilities.contains("EAP"))) {
            return "WPA2-Enterprise";
        } else if (capabilities.contains("WPA-EAP") || 
                  (capabilities.contains("WPA") && capabilities.contains("EAP"))) {
            return "WPA-Enterprise";
        } else if (capabilities.contains("RSN") || capabilities.contains("WPA2")) {
            return "WPA2-Personal";
        } else if (capabilities.contains("WPA-PSK") || capabilities.contains("WPA")) {
            return "WPA-Personal";
        } else if (capabilities.isEmpty() || capabilities.equals("[ESS]")) {
            return "Open";
        } else {
            return "Unknown";
        }
    }
    
    /**
     * 判断是否为企业级Wi-Fi
     * @param securityType 安全协议类型
     * @return 是否为企业级Wi-Fi
     */
    private boolean isEnterpriseNetwork(String securityType) {
        return securityType.contains("Enterprise");
    }

    /**
     * 连接到Wi-Fi网络
     * @param context Android上下文
     * @param ssid 网络SSID（名称）
     * @param password 密码（如果需要）
     * @param isEnterpriseNetwork 是否是企业网络
     * @param identity 企业网络身份（如果适用）
     * @return 连接结果，true表示成功连接或正在连接，false表示连接失败
     */
    public boolean connectToWifi(Context context, String ssid, String password, 
                                boolean isEnterpriseNetwork, String identity) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            
            // 确保Wi-Fi已开启
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                // 等待Wi-Fi开启
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "等待Wi-Fi开启被中断: " + e.getMessage());
                }
            }
            
            // 根据Android版本使用不同的连接方式
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //     // Android 10+使用新的NetworkRequest API
            //     return connectToWifiAndroid10Plus(context, ssid, password, isEnterpriseNetwork, identity);
            // } else {
                // Android 9及以下使用传统的WifiConfiguration API
                return connectToWifiLegacy(context, wifiManager, ssid, password, isEnterpriseNetwork, identity);
            // }
        } catch (Exception e) {
            Log.e(TAG, "连接Wi-Fi出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Android 10+的Wi-Fi连接方法
     */
    private boolean connectToWifiAndroid10Plus(Context context, String ssid, String password, 
                                             boolean isEnterpriseNetwork, String identity) {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                
            WifiNetworkSpecifier.Builder specifierBuilder = new WifiNetworkSpecifier.Builder();
            specifierBuilder.setSsid(ssid);
            
            // 配置认证方式
            if (password == null || password.isEmpty()) {
                // 开放网络
                // 不需要设置密码
            } else if (isEnterpriseNetwork) {
                // 企业网络需要通过其他API处理，此方法不完全支持
                Log.w(TAG, "Android 10+上通过此API无法完全支持企业网络连接");
                // 企业网络需要特殊处理，这里简化处理
                specifierBuilder.setWpa2EnterpriseConfig(
                    createEnterpriseConfig(identity, password)
                );
            } else {
                // 个人网络
                specifierBuilder.setWpa2Passphrase(password);
            }
            
            NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();
            networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            networkRequestBuilder.setNetworkSpecifier(specifierBuilder.build());
            
            final CountDownLatch connectLatch = new CountDownLatch(1);
            final boolean[] connectionSuccess = {false};
            
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    // 已连接到指定网络
                    connectionSuccess[0] = true;
                    connectLatch.countDown();
                }
                
                @Override
                public void onUnavailable() {
                    // 无法连接到指定网络
                    connectionSuccess[0] = false;
                    connectLatch.countDown();
                }
            };
            
            // 请求连接
            connectivityManager.requestNetwork(networkRequestBuilder.build(), networkCallback);
            
            // 等待连接结果，最多等待10秒
            try {
                connectLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "等待Wi-Fi连接被中断: " + e.getMessage());
            }
            
            return connectionSuccess[0];
        } catch (Exception e) {
            Log.e(TAG, "Android 10+连接Wi-Fi出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 为企业Wi-Fi创建配置
     */
    private WifiEnterpriseConfig createEnterpriseConfig(String identity, String password) {
        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        enterpriseConfig.setIdentity(identity);
        enterpriseConfig.setPassword(password);
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
        enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
        return enterpriseConfig;
    }
    
    /**
     * Android 9及以下的Wi-Fi连接方法
     */
    private boolean connectToWifiLegacy(Context context, WifiManager wifiManager, 
                                      String ssid, String password, 
                                      boolean isEnterpriseNetwork, String identity) {
        try {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "\"" + ssid + "\"";  // 需要加引号
            
            if (password == null || password.isEmpty()) {
                // 开放网络
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            } else if (isEnterpriseNetwork) {
                // 企业网络配置
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                
                wifiConfig.enterpriseConfig.setIdentity(identity);
                wifiConfig.enterpriseConfig.setPassword(password);
                wifiConfig.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
                wifiConfig.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
            } else {
                // 个人网络
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfig.preSharedKey = "\"" + password + "\"";  // 需要加引号
            }
            
            // 添加网络配置
            int networkId = wifiManager.addNetwork(wifiConfig);
            
            if (networkId == -1) {
                Log.e(TAG, "无法添加网络配置");
                return false;
            }
            
            // 断开当前连接并连接到新的网络
            wifiManager.disconnect();
            boolean enableSuccess = wifiManager.enableNetwork(networkId, true);
            boolean reconnectSuccess = wifiManager.reconnect();
            
            Log.d(TAG, "enableNetwork结果: " + enableSuccess + ", reconnect结果: " + reconnectSuccess);
            
            return enableSuccess && reconnectSuccess;
        } catch (Exception e) {
            Log.e(TAG, "Legacy方式连接Wi-Fi出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 连接到开放Wi-Fi网络
     * @param context Android上下文
     * @param ssid 网络SSID（名称）
     * @return 连接结果
     */
    public boolean connectToOpenWifi(Context context, String ssid) {
        return connectToWifi(context, ssid, null, false, null);
    }
    
    /**
     * 连接到个人Wi-Fi网络（需要密码）
     * @param context Android上下文
     * @param ssid 网络SSID（名称）
     * @param password 密码
     * @return 连接结果
     */
    public boolean connectToPersonalWifi(Context context, String ssid, String password) {
        return connectToWifi(context, ssid, password, false, null);
    }
    
    /**
     * 连接到企业Wi-Fi网络
     * @param context Android上下文
     * @param ssid 网络SSID（名称）
     * @param password 密码
     * @param identity 身份（通常是用户名/邮箱）
     * @return 连接结果
     */
    public boolean connectToEnterpriseWifi(Context context, String ssid, String password, String identity) {
        return connectToWifi(context, ssid, password, true, identity);
    }

    /**
     * 检查多个应用权限是否被授予
     * @param context Android上下文
     * @param permissions 需要检查的权限数组
     * @return 包含每个权限检查结果的JSONObject
     */
    public JSONObject checkPermissions(Context context, String[] permissions) {
        JSONObject result = new JSONObject();
        
        try {
            for (String permission : permissions) {
                boolean isGranted = context.checkSelfPermission(permission) 
                                  == PackageManager.PERMISSION_GRANTED;
                result.put(permission, isGranted);
            }
        } catch (Exception e) {
            Log.e(TAG, "检查权限时出错: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 通过DevicePolicyManager直接授予权限
     * @param context Android上下文
     * @param permissions 需要授予的权限数组
     * @return 授权结果
     */
    public JSONObject grantPermissions(Context context, String[] permissions) {
        JSONObject result = new JSONObject();
        
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            // 使用主应用的DeviceAdminReceiver，注意包名和类名的正确组合
            ComponentName adminComponent = new ComponentName(
                "com.syncsign.family.tablet",  // 主应用包名
                "de.kolbasa.apkupdater.tools.DAReceiver"  // 主应用的DeviceAdminReceiver完整路径
            );
            
            if (dpm == null) {
                result.put("success", false);
                result.put("error", "无法获取DevicePolicyManager");
                return result;
            }

            // 验证设备管理员是否有效
            if (!dpm.isAdminActive(adminComponent)) {
                result.put("success", false);
                result.put("error", "设备管理员未激活: " + adminComponent.flattenToString());
                return result;
            }

            JSONObject permissionResults = new JSONObject();
            boolean allGranted = true;
            
            for (String permission : permissions) {
                try {
                    Log.d(TAG, "正在授予权限: " + permission + " 给应用: " + context.getPackageName());
                    // 先尝试直接授予权限
                    dpm.setPermissionGrantState(
                        adminComponent,
                        context.getPackageName(),  // 为当前应用授权
                        permission,
                        DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                    );
                    
                    // 等待一下让系统处理权限变更
                    Thread.sleep(100);
                    
                    // 检查权限是否真的被授予
                    int permissionState = dpm.getPermissionGrantState(
                        adminComponent,
                        context.getPackageName(),
                        permission
                    );
                    
                    boolean isGranted = permissionState == DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;
                    
                    // 双重检查：通过常规方式也确认一下
                    boolean isGrantedCheck = context.checkSelfPermission(permission) 
                                          == PackageManager.PERMISSION_GRANTED;
                    
                    // 只有两种检查都通过才认为真的授予了
                    boolean finalGranted = isGranted && isGrantedCheck;
                    
                    permissionResults.put(permission, finalGranted);
                    if (!finalGranted) {
                        allGranted = false;
                        Log.w(TAG, "权限 " + permission + " 可能未被成功授予: " + 
                              "DPM状态=" + isGranted + ", 实际状态=" + isGrantedCheck);
                    } else {
                        Log.d(TAG, "权限 " + permission + " 已成功授予");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "授予权限时出错: " + permission + ", " + e.getMessage());
                    permissionResults.put(permission, false);
                    allGranted = false;
                }
            }
            
            result.put("results", permissionResults);
            result.put("success", allGranted);
        } catch (Exception e) {
            Log.e(TAG, "授予权限过程出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 检测设备外接端口状态
     * @param context Android上下文
     * @return 端口状态信息的JSON对象
     */
    public JSONObject checkExternalPorts(Context context) {
        JSONObject result = new JSONObject();
        
        try {
            // 检测USB端口状态
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            
            JSONArray usbPorts = new JSONArray();
            for (Map.Entry<String, UsbDevice> entry : deviceList.entrySet()) {
                UsbDevice device = entry.getValue();
                JSONObject portInfo = new JSONObject();
                portInfo.put("deviceId", device.getDeviceId());
                portInfo.put("deviceName", device.getDeviceName());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    portInfo.put("manufacturerName", device.getManufacturerName());
                    portInfo.put("productName", device.getProductName());
                }
                portInfo.put("interfaceCount", device.getInterfaceCount());
                portInfo.put("vendorId", device.getVendorId());
                portInfo.put("productId", device.getProductId());
                portInfo.put("deviceClass", getUsbDeviceClass(device.getDeviceClass()));
                portInfo.put("isConnected", true);
                usbPorts.put(portInfo);
            }
            result.put("usbPorts", usbPorts);
            
            // 检测Type-C端口状态（Android 6.0及以上）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                JSONObject typeCInfo = new JSONObject();
                try {
                    typeCInfo.put("isAvailable", true);
                    typeCInfo.put("isCharging", isUsbCharging(context));
                    // 在Android 10及以上版本中，使用UsbManager.FUNCTION_MTP已被弃用
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        typeCInfo.put("isDataTransferEnabled", true); // 简化处理
                    } else {
                        typeCInfo.put("isDataTransferEnabled", true); // 默认支持数据传输
                    }
                } catch (Exception e) {
                    typeCInfo.put("isAvailable", false);
                    typeCInfo.put("error", e.getMessage());
                }
                result.put("typeC", typeCInfo);
            }
            
            // 检测TF卡槽状态
            JSONObject tfCardInfo = new JSONObject();
            
            try {
                StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                boolean hasCardSlot = false;  // 是否有TF卡槽
                boolean cardInserted = false; // 是否插入了卡
                
                // 方法1：通过StorageVolume检测
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    List<android.os.storage.StorageVolume> volumes = storageManager.getStorageVolumes();
                    for (android.os.storage.StorageVolume volume : volumes) {
                        if (volume.isRemovable()) {
                            hasCardSlot = true;
                            break;
                        }
                    }
                }
                
                // 方法2：通过Environment检测外部存储路径
                File[] externalStorageDirs = context.getExternalFilesDirs(null);
                if (!hasCardSlot && externalStorageDirs != null) {
                    for (File dir : externalStorageDirs) {
                        if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                            hasCardSlot = true;
                            break;
                        }
                    }
                }
                
                // 方法3：检查常见的SD卡路径
                String[] sdCardPaths = {
                    "/storage/sdcard1",
                    "/storage/extSdCard",
                    "/storage/external_SD",
                    "/storage/SD",
                    "/mnt/sdcard1",
                    "/mnt/extSdCard",
                    "/mnt/external_SD",
                    "/mnt/media_rw/sdcard1"
                };
                
                for (String path : sdCardPaths) {
                    File potentialPath = new File(path);
                    if (potentialPath.exists() || potentialPath.canRead()) {
                        hasCardSlot = true;
                        break;
                    }
                }
                
                // 设置基本状态
                tfCardInfo.put("isAvailable", hasCardSlot);
                
                // 如果有卡槽，检查卡的状态
                if (hasCardSlot) {
                    String state = "unknown";
                    boolean isMounted = false;
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        List<android.os.storage.StorageVolume> volumes = storageManager.getStorageVolumes();
                        for (android.os.storage.StorageVolume volume : volumes) {
                            if (volume.isRemovable()) {
                                state = volume.getState();
                                cardInserted = !Environment.MEDIA_UNMOUNTED.equals(state) && 
                                             !Environment.MEDIA_REMOVED.equals(state);
                                isMounted = Environment.MEDIA_MOUNTED.equals(state);
                                
                                if (isMounted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    File path = volume.getDirectory();
                                    if (path != null && path.exists()) {
                                        android.os.StatFs stat = new android.os.StatFs(path.getPath());
                                        long blockSize = stat.getBlockSizeLong();
                                        long totalBlocks = stat.getBlockCountLong();
                                        long availableBlocks = stat.getAvailableBlocksLong();
                                        
                                        tfCardInfo.put("totalSpace", totalBlocks * blockSize);
                                        tfCardInfo.put("availableSpace", availableBlocks * blockSize);
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        // 对于旧版本Android，检查是否可以访问SD卡路径
                        for (String path : sdCardPaths) {
                            File sdCard = new File(path);
                            if (sdCard.exists()) {
                                cardInserted = true;
                                isMounted = sdCard.canRead() && sdCard.canWrite();
                                state = isMounted ? "mounted" : "unmounted";
                                
                                if (isMounted) {
                                    android.os.StatFs stat = new android.os.StatFs(path);
                                    long blockSize = stat.getBlockSizeLong();
                                    long totalBlocks = stat.getBlockCountLong();
                                    long availableBlocks = stat.getAvailableBlocksLong();
                                    
                                    tfCardInfo.put("totalSpace", totalBlocks * blockSize);
                                    tfCardInfo.put("availableSpace", availableBlocks * blockSize);
                                }
                                break;
                            }
                        }
                    }
                    
                    tfCardInfo.put("hasCardInserted", cardInserted);
                    tfCardInfo.put("isMounted", isMounted);
                    tfCardInfo.put("state", state);
                } else {
                    tfCardInfo.put("hasCardInserted", false);
                    tfCardInfo.put("isMounted", false);
                    tfCardInfo.put("state", "no_card_slot");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "检测TF卡时出错: " + e.getMessage());
                tfCardInfo.put("isAvailable", false);
                tfCardInfo.put("hasCardInserted", false);
                tfCardInfo.put("isMounted", false);
                tfCardInfo.put("state", "error");
                tfCardInfo.put("error", e.getMessage());
            }
            
            result.put("tfCard", tfCardInfo);
            result.put("success", true);
            
        } catch (Exception e) {
            Log.e(TAG, "检测外接端口状态时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 获取USB设备类型的描述
     */
    private String getUsbDeviceClass(int deviceClass) {
        switch (deviceClass) {
            case UsbConstants.USB_CLASS_APP_SPEC:
                return "应用程序特定";
            case UsbConstants.USB_CLASS_AUDIO:
                return "音频设备";
            case UsbConstants.USB_CLASS_CDC_DATA:
                return "CDC数据设备";
            case UsbConstants.USB_CLASS_COMM:
                return "通信设备";
            case UsbConstants.USB_CLASS_CONTENT_SEC:
                return "内容安全设备";
            case UsbConstants.USB_CLASS_HID:
                return "人机接口设备";
            case UsbConstants.USB_CLASS_HUB:
                return "USB集线器";
            case UsbConstants.USB_CLASS_MASS_STORAGE:
                return "大容量存储设备";
            case UsbConstants.USB_CLASS_MISC:
                return "其他设备";
            case UsbConstants.USB_CLASS_PER_INTERFACE:
                return "每个接口";
            case UsbConstants.USB_CLASS_PHYSICA:
                return "物理设备";
            case UsbConstants.USB_CLASS_PRINTER:
                return "打印机";
            case UsbConstants.USB_CLASS_STILL_IMAGE:
                return "图像设备";
            case UsbConstants.USB_CLASS_VENDOR_SPEC:
                return "厂商特定设备";
            case UsbConstants.USB_CLASS_VIDEO:
                return "视频设备";
            case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
                return "无线控制器";
            default:
                return "未知设备类型";
        }
    }
    
    /**
     * 检查设备是否正在通过USB充电
     */
    private boolean isUsbCharging(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);
        
        if (batteryStatus != null) {
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            return chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        }
        
        return false;
    }

    /**
     * TF卡状态变化的回调接口
     */
    public interface SDCardStateCallback {
        void onSDCardStateChanged(JSONObject state);
    }

    /**
     * 开始监听TF卡槽状态
     * @param context Android上下文
     * @param callback 状态变化回调
     * @return 是否成功开始监听
     */
    public boolean startMonitoringSDCard(Context context, SDCardStateCallback callback) {
        if (isListeningToSdCard) {
            Log.w(TAG, "已经在监听TF卡状态");
            return false;
        }

        try {
            sdCardCallback = callback;
            sdCardReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    Log.d(TAG, "收到存储设备广播: " + action);

                    if (action == null) return;

                    try {
                        JSONObject state = new JSONObject();
                        
                        switch (action) {
                            case Intent.ACTION_MEDIA_MOUNTED:
                                // TF卡被插入并挂载
                                state.put("event", "mounted");
                                state.put("path", intent.getData().getPath());
                                state.put("isAvailable", true);
                                state.put("hasCardInserted", true);
                                state.put("isMounted", true);
                                state.put("state", "mounted");
                                // 获取存储空间信息
                                try {
                                    File path = new File(intent.getData().getPath());
                                    if (path.exists()) {
                                        android.os.StatFs stat = new android.os.StatFs(path.getPath());
                                        long blockSize = stat.getBlockSizeLong();
                                        long totalBlocks = stat.getBlockCountLong();
                                        long availableBlocks = stat.getAvailableBlocksLong();
                                        state.put("totalSpace", totalBlocks * blockSize);
                                        state.put("availableSpace", availableBlocks * blockSize);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "获取TF卡空间信息失败: " + e.getMessage());
                                }
                                break;

                            case Intent.ACTION_MEDIA_UNMOUNTED:
                                // TF卡被卸载（仍在插着但未挂载）
                                state.put("event", "unmounted");
                                state.put("path", intent.getData().getPath());
                                state.put("isAvailable", true);
                                state.put("hasCardInserted", true);
                                state.put("isMounted", false);
                                state.put("state", "unmounted");
                                break;

                            case Intent.ACTION_MEDIA_REMOVED:
                            case Intent.ACTION_MEDIA_BAD_REMOVAL:
                                // TF卡被移除
                                state.put("event", "removed");
                                state.put("isAvailable", true);
                                state.put("hasCardInserted", false);
                                state.put("isMounted", false);
                                state.put("state", "removed");
                                break;

                            case Intent.ACTION_MEDIA_SHARED:
                                // TF卡被共享（通过USB共享给电脑）
                                state.put("event", "shared");
                                state.put("path", intent.getData().getPath());
                                state.put("isAvailable", true);
                                state.put("hasCardInserted", true);
                                state.put("isMounted", false);
                                state.put("state", "shared");
                                break;

                            case Intent.ACTION_MEDIA_CHECKING:
                                // TF卡正在检查
                                state.put("event", "checking");
                                state.put("path", intent.getData().getPath());
                                state.put("isAvailable", true);
                                state.put("hasCardInserted", true);
                                state.put("isMounted", false);
                                state.put("state", "checking");
                                break;
                        }

                        if (sdCardCallback != null) {
                            sdCardCallback.onSDCardStateChanged(state);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "处理TF卡状态变化时出错: " + e.getMessage());
                    }
                }
            };

            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_SHARED);
            filter.addAction(Intent.ACTION_MEDIA_CHECKING);
            filter.addDataScheme("file");
            
            context.registerReceiver(sdCardReceiver, filter);
            isListeningToSdCard = true;
            
            // 立即检查当前状态并回调
            if (sdCardCallback != null) {
                JSONObject currentState = checkExternalPorts(context);
                if (currentState.has("tfCard")) {
                    sdCardCallback.onSDCardStateChanged(currentState.getJSONObject("tfCard"));
                }
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "启动TF卡状态监听时出错: " + e.getMessage());
            return false;
        }
    }

    /**
     * 停止监听TF卡槽状态
     * @param context Android上下文
     */
    public void stopMonitoringSDCard(Context context) {
        if (!isListeningToSdCard || sdCardReceiver == null) {
            return;
        }

        try {
            context.unregisterReceiver(sdCardReceiver);
            sdCardReceiver = null;
            sdCardCallback = null;
            isListeningToSdCard = false;
        } catch (Exception e) {
            Log.e(TAG, "停止TF卡状态监听时出错: " + e.getMessage());
        }
    }

    /**
     * 直接授予文件管理权限
     * @param context Android上下文
     * @return 是否成功获取所有权限
     */
    private boolean requestFilePermissions(Context context) {
        try {
            // Android 11 (API 30)及以上版本需要特殊处理
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // 检查是否已经有所有文件访问权限
                if (!Environment.isExternalStorageManager()) {
                    try {
                        // 1. 首先尝试通过DevicePolicyManager授予权限
                        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        ComponentName adminComponent = new ComponentName(context, "de.kolbasa.apkupdater.tools.DAReceiver");
                        
                        if (dpm != null && dpm.isAdminActive(adminComponent)) {
                            // 使用DPM授予权限
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                dpm.setPermissionGrantState(
                                    adminComponent,
                                    context.getPackageName(),
                                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                                );
                                
                                // 等待权限生效
                                Thread.sleep(1000);
                            }
                        }
                        
                        // 2. 如果DPM方式失败，尝试使用命令行方式
                        if (!Environment.isExternalStorageManager()) {
                            try {
                                String[] commands = {
                                    "pm grant " + context.getPackageName() + " android.permission.MANAGE_EXTERNAL_STORAGE",
                                    "appops set " + context.getPackageName() + " MANAGE_EXTERNAL_STORAGE allow",
                                    "settings put global " + context.getPackageName() + "_external_storage_access 1"
                                };
                                
                                for (String command : commands) {
                                    Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
                                    int result = process.waitFor();
                                    
                                    if (result == 0) {
                                        Log.d(TAG, "命令执行成功: " + command);
                                    } else {
                                        // 如果普通shell失败，尝试使用su
                                        process = Runtime.getRuntime().exec("su");
                                        DataOutputStream os = new DataOutputStream(process.getOutputStream());
                                        os.writeBytes(command + "\n");
                                        os.writeBytes("exit\n");
                                        os.flush();
                                        result = process.waitFor();
                                        
                                        if (result == 0) {
                                            Log.d(TAG, "使用root权限执行命令成功: " + command);
                                        } else {
                                            Log.e(TAG, "命令执行失败: " + command);
                                        }
                                    }
                                }
                                
                                // 等待权限生效
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                Log.e(TAG, "执行命令失败: " + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "请求MANAGE_EXTERNAL_STORAGE权限失败: " + e.getMessage());
                    }
                }
                return Environment.isExternalStorageManager();
            }
            // Android 10 (API 29)及以下版本
            else {
                String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
                
                // 检查是否已经有权限
                boolean hasAllPermissions = true;
                for (String permission : permissions) {
                    if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        hasAllPermissions = false;
                        break;
                    }
                }
                
                if (!hasAllPermissions) {
                    try {
                        // 1. 首先尝试通过DevicePolicyManager授予权限
                        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        ComponentName adminComponent = new ComponentName(context, "de.kolbasa.apkupdater.tools.DAReceiver");
                        
                        if (dpm != null && dpm.isAdminActive(adminComponent)) {
                            for (String permission : permissions) {
                                dpm.setPermissionGrantState(
                                    adminComponent,
                                    context.getPackageName(),
                                    permission,
                                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                                );
                            }
                            // 等待权限生效
                            Thread.sleep(1000);
                        }
                        
                        // 2. 如果DPM方式失败，尝试使用命令行方式
                        hasAllPermissions = true;
                        for (String permission : permissions) {
                            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                                hasAllPermissions = false;
                                try {
                                    String command = "pm grant " + context.getPackageName() + " " + permission;
                                    
                                    // 先尝试普通shell
                                    Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
                                    int result = process.waitFor();
                                    
                                    if (result != 0) {
                                        // 如果普通shell失败，尝试使用su
                                        process = Runtime.getRuntime().exec("su");
                                        DataOutputStream os = new DataOutputStream(process.getOutputStream());
                                        os.writeBytes(command + "\n");
                                        os.writeBytes("exit\n");
                                        os.flush();
                                        result = process.waitFor();
                                        
                                        if (result == 0) {
                                            Log.d(TAG, "使用root权限授予权限成功: " + permission);
                                        } else {
                                            Log.e(TAG, "授予权限失败: " + permission);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "执行命令失败: " + e.getMessage());
                                }
                            }
                        }
                        
                        // 最后检查权限是否都已授予
                        hasAllPermissions = true;
                        for (String permission : permissions) {
                            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                                hasAllPermissions = false;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "通过DPM授予权限失败: " + e.getMessage());
                    }
                }
                return hasAllPermissions;
            }
        } catch (Exception e) {
            Log.e(TAG, "请求文件权限时出错: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从TF卡中读取CSV文件并获取第一个可用的license
     * @param context Android上下文
     * @param csvFileName CSV文件名（相对于TF卡根目录）
     * @return 包含结果的JSON对象
     */
    public JSONObject getAvailableLicenseFromSD(Context context, String csvFileName) {
        JSONObject result = new JSONObject();
        
        try {
            // 请求必要的文件权限
            if (!requestFilePermissions(context)) {
                result.put("success", false);
                result.put("error", "无法获取必要的文件访问权限");
                return result;
            }

            // 首先检查TF卡状态
            JSONObject portStatus = checkExternalPorts(context);
            JSONObject tfCardInfo = portStatus.getJSONObject("tfCard");
            
            if (!tfCardInfo.optBoolean("isAvailable", false) || 
                !tfCardInfo.optBoolean("isMounted", false)) {
                result.put("success", false);
                result.put("error", "TF卡不可用或未挂载");
                return result;
            }

            // 获取TF卡路径
            String sdCardPath = null;
            
            // 1. 首先尝试获取外部存储目录
            File[] externalStorageDirs = context.getExternalFilesDirs(null);
            if (externalStorageDirs != null && externalStorageDirs.length > 1 && externalStorageDirs[1] != null) {
                String path = externalStorageDirs[1].getAbsolutePath();
                // 移除Android/data/包名部分
                int index = path.indexOf("/Android/data/");
                if (index > 0) {
                    sdCardPath = path.substring(0, index);
                    Log.d(TAG, "通过getExternalFilesDirs找到TF卡路径: " + sdCardPath);
                }
            }
            
            // 2. 如果上述方法失败，尝试Environment.getExternalStorageDirectory()
            if (sdCardPath == null) {
                File externalStorage = Environment.getExternalStorageDirectory();
                if (externalStorage != null && externalStorage.exists()) {
                    sdCardPath = externalStorage.getAbsolutePath();
                    Log.d(TAG, "通过Environment找到存储路径: " + sdCardPath);
                }
            }
            
            // 3. 如果还是找不到，尝试常见路径
            if (sdCardPath == null) {
                String[] commonPaths = {
                    "/storage/sdcard1",
                    "/storage/extSdCard",
                    "/storage/external_SD",
                    "/storage/SD",
                    "/mnt/sdcard1",
                    "/mnt/extSdCard",
                    "/storage/0000-0000",
                    "/storage/emulated/0/external_sd",
                    "/storage/self/primary",
                    "/storage/emulated/0"
                };
                
                for (String path : commonPaths) {
                    File potentialPath = new File(path);
                    if (potentialPath.exists()) {
                        sdCardPath = path;
                        Log.d(TAG, "通过常见路径找到存储: " + sdCardPath);
                        break;
                    }
                }
            }
            
            if (sdCardPath == null) {
                result.put("success", false);
                result.put("error", "无法获取存储路径");
                return result;
            }

            // 构建CSV文件完整路径并尝试多个可能的位置
            File csvFile = null;
            String[] possibleLocations = {
                sdCardPath + "/" + csvFileName,
                sdCardPath + "/Download/" + csvFileName,
                sdCardPath + "/Documents/" + csvFileName,
                sdCardPath + "/Android/data/" + context.getPackageName() + "/files/" + csvFileName
            };

            for (String location : possibleLocations) {
                File testFile = new File(location);
                if (testFile.exists()) {
                    csvFile = testFile;
                    Log.d(TAG, "找到CSV文件: " + location);
                    break;
                }
            }

            if (csvFile == null) {
                result.put("success", false);
                result.put("error", "找不到CSV文件，已尝试以下路径：\n" + String.join("\n", possibleLocations));
                return result;
            }

            // 检查文件权限
            if (!csvFile.canRead()) {
                // 尝试修改文件权限
                try {
                    csvFile.setReadable(true, false);
                    csvFile.setExecutable(true, false);
                    
                    // 如果还是不能读取，尝试使用命令行修改权限
                    if (!csvFile.canRead()) {
                        Process process = Runtime.getRuntime().exec(new String[]{
                            "chmod",
                            "644",
                            csvFile.getAbsolutePath()
                        });
                        process.waitFor();
                        
                        // 检查命令执行结果
                        BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream())
                        );
                        String errorLine;
                        StringBuilder errorOutput = new StringBuilder();
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorOutput.append(errorLine).append("\n");
                        }
                        
                        if (errorOutput.length() > 0) {
                            Log.e(TAG, "修改文件权限时出错: " + errorOutput.toString());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "修改文件权限失败: " + e.getMessage());
                }
                
                // 再次检查权限
                if (!csvFile.canRead()) {
                    result.put("success", false);
                    result.put("error", "无法读取CSV文件（权限不足）\n" +
                              "文件路径: " + csvFile.getAbsolutePath() + "\n" +
                              "当前权限: " + getFilePermissions(csvFile) + "\n" +
                              "请确保应用有足够的权限访问外部存储");
                    return result;
                }
            }

            // 读取CSV文件
            String availableLicense = null;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(csvFile));
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    Log.d(TAG, "读取第 " + lineNumber + " 行: " + line);
                    
                    if (line.trim().isEmpty()) continue;
                    
                    String[] parts = line.split(",");
                    if (parts.length > 0) {
                        String license = parts[0].trim();
                        if (!license.isEmpty() && license.length() >= 4) {
                            if (parts.length == 1 || parts[1].trim().isEmpty()) {
                                availableLicense = license;
                                Log.d(TAG, "找到可用的license: " + license);
                                break;
                            }
                        }
                    }
                }
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "关闭CSV文件时出错: " + e.getMessage());
                    }
                }
            }

            if (availableLicense != null) {
                result.put("success", true);
                result.put("license", availableLicense);
            } else {
                result.put("success", false);
                result.put("error", "未找到可用的license");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "读取CSV文件时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", "读取CSV文件时出错: " + e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 获取文件权限信息的详细描述
     */
    private String getFilePermissions(File file) {
        StringBuilder permissions = new StringBuilder();
        permissions.append("文件信息:\n");
        permissions.append("- 存在: ").append(file.exists()).append("\n");
        permissions.append("- 可读: ").append(file.canRead()).append("\n");
        permissions.append("- 可写: ").append(file.canWrite()).append("\n");
        permissions.append("- 可执行: ").append(file.canExecute()).append("\n");
        permissions.append("- 是文件: ").append(file.isFile()).append("\n");
        permissions.append("- 是目录: ").append(file.isDirectory()).append("\n");
        permissions.append("- 是隐藏: ").append(file.isHidden()).append("\n");
        permissions.append("- 绝对路径: ").append(file.getAbsolutePath()).append("\n");
        
        try {
            // 获取文件的详细权限信息
            Process process = Runtime.getRuntime().exec("ls -l " + file.getAbsolutePath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                permissions.append("- 系统权限: ").append(line).append("\n");
            }
            reader.close();
            
            // 获取文件所有者信息
            process = Runtime.getRuntime().exec("stat " + file.getAbsolutePath());
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = reader.readLine()) != null) {
                permissions.append("- ").append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            permissions.append("获取系统权限信息失败: ").append(e.getMessage());
        }
        
        return permissions.toString();
    }

    /**
     * 检查应用是否被重新签名
     * @param context Android上下文
     * @return 包含检查结果的JSON对象
     */
    public JSONObject checkAppSignature(Context context) {
        JSONObject result = new JSONObject();
        
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            
            // 获取当前应用的签名信息
            PackageInfo packageInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
            } else {
                packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            }
            
            // 原始签名的SHA-256指纹（原始十六进制格式，不带冒号）
            String originalSignature = "80abf06c4d842440dc23028816604536302c452b6de4499e2609f86677141ddf";
            
            // 获取当前签名
            String currentSignature = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (packageInfo.signingInfo != null) {
                    Signature[] signatures = packageInfo.signingInfo.getApkContentsSigners();
                    if (signatures != null && signatures.length > 0) {
                        currentSignature = bytesToHex(getSHA256(signatures[0].toByteArray()));
                    }
                }
            } else {
                if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                    currentSignature = bytesToHex(getSHA256(packageInfo.signatures[0].toByteArray()));
                }
            }
            
            // 记录详细信息
            result.put("success", true);
            result.put("packageName", packageName);
            result.put("currentSignature", currentSignature);
            result.put("isOriginalSignature", originalSignature.equalsIgnoreCase(currentSignature));
            
            // 添加更多签名信息
            JSONObject signatureDetails = new JSONObject();
            Signature[] signatures;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                signatures = packageInfo.signingInfo.getApkContentsSigners();
            } else {
                signatures = packageInfo.signatures;
            }
            
            if (signatures != null && signatures.length > 0) {
                byte[] signatureBytes = signatures[0].toByteArray();
                signatureDetails.put("md5", bytesToHex(getMD5(signatureBytes)));
                signatureDetails.put("sha1", bytesToHex(getSHA1(signatureBytes)));
                signatureDetails.put("sha256", bytesToHex(getSHA256(signatureBytes)));
                
                // 同时提供冒号分隔的格式
                signatureDetails.put("md5_formatted", formatSignature(getMD5(signatureBytes)));
                signatureDetails.put("sha1_formatted", formatSignature(getSHA1(signatureBytes)));
                signatureDetails.put("sha256_formatted", formatSignature(getSHA256(signatureBytes)));
                
                // 获取证书信息
                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(
                        new ByteArrayInputStream(signatureBytes)
                    );
                    signatureDetails.put("issuer", cert.getIssuerDN().toString());
                    signatureDetails.put("subject", cert.getSubjectDN().toString());
                    signatureDetails.put("serialNumber", cert.getSerialNumber().toString());
                    signatureDetails.put("validFrom", cert.getNotBefore().toString());
                    signatureDetails.put("validUntil", cert.getNotAfter().toString());
                } catch (Exception e) {
                    Log.e(TAG, "获取证书详细信息失败: " + e.getMessage());
                }
            }
            result.put("signatureDetails", signatureDetails);
            
        } catch (Exception e) {
            Log.e(TAG, "检查应用签名时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 将字节数组转换为十六进制字符串（不带冒号分隔）
     */
    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * 格式化签名字节数组为冒号分隔的十六进制字符串
     */
    private String formatSignature(byte[] signature) {
        if (signature == null) return "";
        Formatter formatter = new Formatter();
        for (byte b : signature) {
            formatter.format("%02X:", b);
        }
        String result = formatter.toString();
        formatter.close();
        // 移除最后一个冒号
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
    
    /**
     * 获取字节数组的MD5值
     */
    private byte[] getMD5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(bytes);
        } catch (Exception e) {
            Log.e(TAG, "计算MD5失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取字节数组的SHA-1值
     */
    private byte[] getSHA1(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(bytes);
        } catch (Exception e) {
            Log.e(TAG, "计算SHA-1失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取字节数组的SHA-256值
     */
    private byte[] getSHA256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(bytes);
        } catch (Exception e) {
            Log.e(TAG, "计算SHA-256失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 验证设备日期和时间
     * @param context Android上下文
     * @return 包含验证结果的JSON对象
     */
    public JSONObject checkDeviceDateTime(Context context) {
        JSONObject result = new JSONObject();
        
        try {
            // 获取系统当前时间
            long currentTimeMillis = System.currentTimeMillis();
            java.util.Date currentDate = new java.util.Date(currentTimeMillis);
            
            // 获取系统时区信息
            java.util.TimeZone timeZone = java.util.TimeZone.getDefault();
            
            // 格式化日期和时间
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = dateFormat.format(currentDate);
            
            // 检查是否为24小时制
            android.text.format.DateFormat df = new android.text.format.DateFormat();
            boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(context);
            
            // 检查自动时间设置是否启用
            boolean autoTimeEnabled = android.provider.Settings.Global.getInt(
                context.getContentResolver(),
                android.provider.Settings.Global.AUTO_TIME,
                0
            ) == 1;
            
            // 检查自动时区设置是否启用
            boolean autoTimeZoneEnabled = android.provider.Settings.Global.getInt(
                context.getContentResolver(),
                android.provider.Settings.Global.AUTO_TIME_ZONE,
                0
            ) == 1;

            // 检查是否使用网络提供的时间
            boolean useNetworkTime = android.provider.Settings.Global.getInt(
                context.getContentResolver(),
                android.provider.Settings.Global.AUTO_TIME,
                0
            ) == 1 && isNetworkTimeAvailable(context);
            
            // 获取ISO 8601格式时间
            java.text.SimpleDateFormat iso8601Format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            String iso8601DateTime = iso8601Format.format(currentDate);
            
            // 构建返回结果
            result.put("success", true);
            result.put("currentDateTime", formattedDateTime);
            result.put("iso8601DateTime", iso8601DateTime);
            result.put("timestamp", currentTimeMillis);
            result.put("unixTimestamp", currentTimeMillis / 1000);
            result.put("timeZoneId", timeZone.getID());
            result.put("timeZoneName", timeZone.getDisplayName());
            result.put("timeZoneOffset", timeZone.getRawOffset() / 3600000.0); // 转换为小时
            result.put("isDaylightTime", timeZone.inDaylightTime(currentDate));
            result.put("is24HourFormat", is24HourFormat);
            result.put("autoTimeEnabled", autoTimeEnabled);
            result.put("autoTimeZoneEnabled", autoTimeZoneEnabled);
            result.put("useNetworkTime", useNetworkTime);
            
            // 检查时间是否准确（基于自动时间设置状态和网络时间可用性）
            result.put("isTimeAccurate", useNetworkTime);
            result.put("timeOffsetFromNTP", 0); // 由于无法使用SntpClient，默认为0
            
        } catch (Exception e) {
            Log.e(TAG, "验证设备日期和时间时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 检查网络时间是否可用
     * @param context Android上下文
     * @return 网络时间是否可用
     */
    private boolean isNetworkTimeAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 使用新的NetworkCapabilities API
                    Network network = cm.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                        return capabilities != null && (
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        );
                    }
                } else {
                    // 对于旧版本Android，直接返回true，因为我们已经确认了自动时间同步是开启的
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查网络时间可用性时出错: " + e.getMessage());
        }
        return false;
    }

    /**
     * 检查WebView版本信息
     * @param context Android上下文
     * @return 包含WebView信息的JSON对象
     */
    public JSONObject checkWebViewInfo(Context context) {
        JSONObject result = new JSONObject();
        final WebView[] webViewHolder = new WebView[1];
        
        try {
            // 获取WebView包信息
            PackageInfo webViewPackageInfo = null;
            String webViewPackageName = null;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0及以上使用WebView.getCurrentWebViewPackage()
                try {
                    webViewPackageInfo = WebView.getCurrentWebViewPackage();
                    if (webViewPackageInfo != null) {
                        webViewPackageName = webViewPackageInfo.packageName;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取WebView包信息失败: " + e.getMessage());
                }
            }
            
            // 如果上述方法失败，尝试获取默认WebView包信息
            if (webViewPackageInfo == null) {
                String[] webViewPackages = {
                    "com.google.android.webview",
                    "com.android.webview",
                    "com.android.chrome",
                    "com.chrome.beta",
                    "com.chrome.dev",
                    "com.chrome.canary"
                };
                
                PackageManager pm = context.getPackageManager();
                for (String packageName : webViewPackages) {
                    try {
                        webViewPackageInfo = pm.getPackageInfo(packageName, 0);
                        webViewPackageName = packageName;
                        break;
                    } catch (PackageManager.NameNotFoundException e) {
                        // 继续检查下一个包
                        continue;
                    }
                }
            }
            
            // 在主线程中创建WebView实例
            final String[] userAgent = {null};
            final WebSettings[] webSettings = {null};
            
            if (context instanceof android.app.Activity) {
                android.app.Activity activity = (android.app.Activity) context;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            webViewHolder[0] = new WebView(context);
                            webSettings[0] = webViewHolder[0].getSettings();
                            userAgent[0] = webSettings[0].getUserAgentString();
                        } catch (Exception e) {
                            Log.e(TAG, "创建WebView失败: " + e.getMessage());
                        }
                    }
                });
                
                // 等待主线程操作完成
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e(TAG, "等待WebView创建被中断: " + e.getMessage());
                }
            }
            
            // 构建返回结果
            result.put("success", true);
            
            if (webViewPackageInfo != null) {
                result.put("packageName", webViewPackageName);
                result.put("versionName", webViewPackageInfo.versionName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    result.put("versionCode", webViewPackageInfo.getLongVersionCode());
                } else {
                    result.put("versionCode", (long) webViewPackageInfo.versionCode);
                }
                
                // 获取更多包信息
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    result.put("firstInstallTime", webViewPackageInfo.firstInstallTime);
                    result.put("lastUpdateTime", webViewPackageInfo.lastUpdateTime);
                }
            }
            
            // 添加WebView设置信息
            if (webSettings[0] != null) {
                JSONObject settings = new JSONObject();
                settings.put("userAgent", userAgent[0]);
                settings.put("javaScriptEnabled", webSettings[0].getJavaScriptEnabled());
                settings.put("databaseEnabled", webSettings[0].getDatabaseEnabled());
                settings.put("domStorageEnabled", webSettings[0].getDomStorageEnabled());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    settings.put("safeBrowsingEnabled", webSettings[0].getSafeBrowsingEnabled());
                }
                result.put("settings", settings);
            }
            
            // 添加系统信息
            result.put("androidVersion", Build.VERSION.RELEASE);
            result.put("androidSDK", Build.VERSION.SDK_INT);
            
            // 检查WebView是否可用
            boolean isWebViewEnabled = isWebViewEnabled(context);
            result.put("isEnabled", isWebViewEnabled);
            
            // 获取WebView数据目录
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                String dataDir = context.getDataDir().getAbsolutePath() + "/app_webview";
                result.put("dataDirectory", dataDir);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "检查WebView信息时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        } finally {
            // 确保在主线程中销毁WebView
            if (webViewHolder[0] != null && context instanceof android.app.Activity) {
                final WebView finalWebView = webViewHolder[0];
                ((android.app.Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            finalWebView.destroy();
                        } catch (Exception e) {
                            Log.e(TAG, "销毁WebView失败: " + e.getMessage());
                        }
                    }
                });
            }
        }
        
        return result;
    }
    
    /**
     * 检查WebView是否可用
     * @param context Android上下文
     * @return WebView是否可用
     */
    private boolean isWebViewEnabled(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PackageInfo webViewPackage = WebView.getCurrentWebViewPackage();
                return webViewPackage != null;
            } else {
                PackageManager pm = context.getPackageManager();
                try {
                    pm.getPackageInfo("com.google.android.webview", 0);
                    return true;
                } catch (PackageManager.NameNotFoundException e1) {
                    try {
                        pm.getPackageInfo("com.android.webview", 0);
                        return true;
                    } catch (PackageManager.NameNotFoundException e2) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查WebView可用性时出错: " + e.getMessage());
            return false;
        }
    }

    /**
     * 将字节转换为GB
     */
    private double bytesToGB(long bytes) {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * 获取设备硬件信息
     */
    public JSONObject getHardwareInfo(Context context) {
        JSONObject result = new JSONObject();
        
        try {
            // 获取存储信息
            JSONObject storage = new JSONObject();
            File internalStorage = context.getFilesDir().getParentFile();
            StatFs stat = new StatFs(internalStorage.getPath());
            
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            long freeBlocks = stat.getFreeBlocksLong();
            
            long totalBytes = totalBlocks * blockSize;
            long availableBytes = availableBlocks * blockSize;
            long freeBytes = freeBlocks * blockSize;
            
            // 转换为GB
            double totalGB = bytesToGB(totalBytes);
            double availableGB = bytesToGB(availableBytes);
            double freeGB = bytesToGB(freeBytes);
            
            // 检查存储健康状态
            boolean isStorageHealthy = true;
            StringBuilder storageHealthDetails = new StringBuilder();
            
            // 1. 检查剩余空间比例
            double freeSpaceRatio = (double) freeBytes / totalBytes;
            if (freeSpaceRatio < 0.1) { // 剩余空间小于10%
                isStorageHealthy = false;
                storageHealthDetails.append("存储空间严重不足（<10%）; ");
            } else if (freeSpaceRatio < 0.2) { // 剩余空间小于20%
                storageHealthDetails.append("存储空间偏低（<20%）; ");
            }
            
            // 2. 检查存储设备读写速度
            try {
                File testFile = new File(context.getCacheDir(), "storage_speed_test");
                long startTime = System.nanoTime();
                
                // 写入测试
                FileOutputStream fos = new FileOutputStream(testFile);
                byte[] testData = new byte[1024 * 1024]; // 1MB数据
                fos.write(testData);
                fos.close();
                
                // 读取测试
                FileInputStream fis = new FileInputStream(testFile);
                fis.read(new byte[1024 * 1024]);
                fis.close();
                
                // 计算读写时间
                long endTime = System.nanoTime();
                double timeMs = (endTime - startTime) / 1_000_000.0; // 转换为毫秒
                
                testFile.delete();
                
                if (timeMs > 1000) { // 如果读写1MB数据超过1秒
                    isStorageHealthy = false;
                    storageHealthDetails.append("存储设备读写速度异常; ");
                }
            } catch (Exception e) {
                isStorageHealthy = false;
                storageHealthDetails.append("存储设备读写测试失败; ");
            }
            
            storage.put("totalSpace", totalGB);
            storage.put("availableSpace", availableGB);
            storage.put("freeSpace", freeGB);
            storage.put("isHealthy", isStorageHealthy);
            storage.put("healthDetails", storageHealthDetails.length() > 0 ? 
                       storageHealthDetails.toString() : "存储设备状态正常");
            storage.put("details", String.format(
                "总存储空间: %.2f GB, 可用空间: %.2f GB, 剩余空间: %.2f GB",
                totalGB, availableGB, freeGB
            ));
            
            // 获取内存信息
            JSONObject memory = new JSONObject();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memInfo);
            
            // 转换为GB
            double totalMemGB = bytesToGB(memInfo.totalMem);
            double availableMemGB = bytesToGB(memInfo.availMem);
            
            // 检查内存健康状态
            boolean isMemoryHealthy = true;
            StringBuilder memoryHealthDetails = new StringBuilder();
            
            // 1. 检查可用内存比例
            double availableMemRatio = (double) memInfo.availMem / memInfo.totalMem;
            if (availableMemRatio < 0.1) { // 可用内存小于10%
                isMemoryHealthy = false;
                memoryHealthDetails.append("可用内存严重不足（<10%）; ");
            } else if (availableMemRatio < 0.2) { // 可用内存小于20%
                memoryHealthDetails.append("可用内存偏低（<20%）; ");
            }
            
            // 2. 检查内存分配速度
            try {
                long startTime = System.nanoTime();
                byte[] testData = new byte[50 * 1024 * 1024]; // 分配50MB内存
                long endTime = System.nanoTime();
                double allocTimeMs = (endTime - startTime) / 1_000_000.0;
                
                if (allocTimeMs > 100) { // 如果分配50MB内存超过100ms
                    isMemoryHealthy = false;
                    memoryHealthDetails.append("内存分配速度异常; ");
                }
            } catch (OutOfMemoryError e) {
                isMemoryHealthy = false;
                memoryHealthDetails.append("内存分配测试失败; ");
            }
            
            memory.put("totalMemory", totalMemGB);
            memory.put("availableMemory", availableMemGB);
            memory.put("lowMemory", memInfo.lowMemory);
            memory.put("isHealthy", isMemoryHealthy);
            memory.put("healthDetails", memoryHealthDetails.length() > 0 ? 
                      memoryHealthDetails.toString() : "内存状态正常");
            memory.put("details", String.format(
                "总内存: %.2f GB, 可用内存: %.2f GB, 低内存状态: %s",
                totalMemGB, availableMemGB,
                memInfo.lowMemory ? "是" : "否"
            ));
            
            // 获取CPU信息
            JSONObject cpu = new JSONObject();
            int cores = Runtime.getRuntime().availableProcessors();
            
            // 读取CPU频率
            double maxFreq = 0;
            try {
                File[] cpuFiles = new File("/sys/devices/system/cpu/").listFiles(
                    file -> file.getName().matches("cpu[0-9]+")
                );
                
                if (cpuFiles != null) {
                    for (File cpuFile : cpuFiles) {
                        File freqFile = new File(cpuFile, "cpufreq/scaling_cur_freq");
                        if (freqFile.exists()) {
                            BufferedReader reader = new BufferedReader(new FileReader(freqFile));
                            String line = reader.readLine();
                            reader.close();
                            if (line != null) {
                                double freq = Double.parseDouble(line) / 1_000_000.0; // 转换为GHz
                                maxFreq = Math.max(maxFreq, freq);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "读取CPU频率失败: " + e.getMessage());
            }
            
            // 读取CPU温度
            double temperature = 0;
            try {
                File tempFile = new File("/sys/class/thermal/thermal_zone0/temp");
                if (tempFile.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(tempFile));
                    String line = reader.readLine();
                    reader.close();
                    if (line != null) {
                        temperature = Double.parseDouble(line) / 1000.0; // 转换为摄氏度
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "读取CPU温度失败: " + e.getMessage());
            }
            
            // 获取CPU使用率
            double cpuUsage = 0;
            try {
                BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"));
                String line = reader.readLine();
                reader.close();
                if (line != null) {
                    String[] values = line.split("\\s+");
                    if (values.length >= 5) {
                        long user = Long.parseLong(values[1]);
                        long nice = Long.parseLong(values[2]);
                        long system = Long.parseLong(values[3]);
                        long idle = Long.parseLong(values[4]);
                        long total = user + nice + system + idle;
                        cpuUsage = 100.0 * (1 - (double)idle / total);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "读取CPU使用率失败: " + e.getMessage());
            }
            
            // 检查CPU健康状态
            boolean isCpuHealthy = true;
            StringBuilder cpuHealthDetails = new StringBuilder();
            
            // 1. 检查温度
            if (temperature > 80) { // 温度超过80度
                isCpuHealthy = false;
                cpuHealthDetails.append("CPU温度过高; ");
            } else if (temperature > 70) { // 温度超过70度
                cpuHealthDetails.append("CPU温度偏高; ");
            }
            
            // 2. 检查使用率
            if (cpuUsage > 90) { // CPU使用率超过90%
                isCpuHealthy = false;
                cpuHealthDetails.append("CPU使用率过高; ");
            } else if (cpuUsage > 80) { // CPU使用率超过80%
                cpuHealthDetails.append("CPU负载偏高; ");
            }
            
            cpu.put("cores", cores);
            cpu.put("frequency", maxFreq);
            cpu.put("isHealthy", isCpuHealthy);
            cpu.put("temperature", temperature);
            cpu.put("usage", cpuUsage);
            cpu.put("details", String.format(
                "CPU核心数: %d, 频率: %.2f GHz, 温度: %.1f℃, 使用率: %.1f%%",
                cores, maxFreq, temperature, cpuUsage
            ));
            
            // 获取传感器信息
            JSONArray sensors = new JSONArray();
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            
            for (Sensor sensor : sensorList) {
                JSONObject sensorInfo = new JSONObject();
                sensorInfo.put("name", sensor.getName());
                sensorInfo.put("type", getSensorTypeName(sensor.getType()));
                sensorInfo.put("vendor", sensor.getVendor());
                
                // 检查传感器是否正常工作
                boolean isWorking = sensor.getMinDelay() > 0;
                sensorInfo.put("isWorking", isWorking);
                
                StringBuilder details = new StringBuilder();
                details.append(String.format("类型: %s, ", getSensorTypeName(sensor.getType())));
                details.append(String.format("制造商: %s, ", sensor.getVendor()));
                details.append(String.format("版本: %d, ", sensor.getVersion()));
                details.append(String.format("功耗: %.2f mA, ", sensor.getPower()));
                details.append(String.format("最大量程: %.2f, ", sensor.getMaximumRange()));
                details.append(String.format("分辨率: %.6f, ", sensor.getResolution()));
                details.append(String.format("最小延迟: %d μs", sensor.getMinDelay()));
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    details.append(String.format(", 最大延迟: %d μs", sensor.getMaxDelay()));
                    details.append(String.format(", 报告模式: %s", getReportingMode(sensor.getReportingMode())));
                }
                
                sensorInfo.put("details", details.toString());
                sensors.put(sensorInfo);
            }
            
            result.put("success", true);
            result.put("storage", storage);
            result.put("memory", memory);
            result.put("cpu", cpu);
            result.put("sensors", sensors);
            
        } catch (Exception e) {
            Log.e(TAG, "获取硬件信息时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 获取传感器报告模式名称
     */
    private String getReportingMode(int mode) {
        switch (mode) {
            case Sensor.REPORTING_MODE_CONTINUOUS:
                return "连续";
            case Sensor.REPORTING_MODE_ON_CHANGE:
                return "变化时";
            case Sensor.REPORTING_MODE_ONE_SHOT:
                return "单次";
            case Sensor.REPORTING_MODE_SPECIAL_TRIGGER:
                return "特殊触发";
            default:
                return "未知";
        }
    }

    /**
     * 获取传感器类型名称
     */
    private String getSensorTypeName(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                return "加速度传感器";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "环境温度传感器";
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                return "游戏旋转矢量传感器";
            case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                return "地磁旋转矢量传感器";
            case Sensor.TYPE_GRAVITY:
                return "重力传感器";
            case Sensor.TYPE_GYROSCOPE:
                return "陀螺仪传感器";
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                return "未校准陀螺仪传感器";
            case Sensor.TYPE_HEART_RATE:
                return "心率传感器";
            case Sensor.TYPE_LIGHT:
                return "光线传感器";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "线性加速度传感器";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "磁场传感器";
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                return "未校准磁场传感器";
            case Sensor.TYPE_ORIENTATION:
                return "方向传感器";
            case Sensor.TYPE_PRESSURE:
                return "压力传感器";
            case Sensor.TYPE_PROXIMITY:
                return "距离传感器";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "相对湿度传感器";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "旋转矢量传感器";
            case Sensor.TYPE_SIGNIFICANT_MOTION:
                return "显著运动传感器";
            case Sensor.TYPE_STEP_COUNTER:
                return "计步传感器";
            case Sensor.TYPE_STEP_DETECTOR:
                return "步伐检测传感器";
            default:
                return "未知传感器(类型" + type + ")";
        }
    }
}
