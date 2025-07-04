package com.fantastic.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import java.lang.reflect.Method;
import android.content.BroadcastReceiver;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
     * 从TF卡中读取CSV文件并获取第一个可用的license
     * @param context Android上下文
     * @param csvFileName CSV文件名（相对于TF卡根目录）
     * @return 包含结果的JSON对象
     */
    public JSONObject getAvailableLicenseFromSD(Context context, String csvFileName) {
        JSONObject result = new JSONObject();
        
        try {
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
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11及以上使用StorageManager的新API
                List<android.os.storage.StorageVolume> volumes = storageManager.getStorageVolumes();
                for (android.os.storage.StorageVolume volume : volumes) {
                    if (volume.isRemovable()) {
                        File path = volume.getDirectory();
                        if (path != null) {
                            sdCardPath = path.getAbsolutePath();
                            Log.d(TAG, "Android 11+ 找到TF卡路径: " + sdCardPath);
                            break;
                        }
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7-10 使用StorageVolume
                List<android.os.storage.StorageVolume> volumes = storageManager.getStorageVolumes();
                for (android.os.storage.StorageVolume volume : volumes) {
                    if (volume.isRemovable()) {
                        try {
                            // 使用反射获取路径
                            Method getPath = volume.getClass().getMethod("getPath");
                            sdCardPath = (String) getPath.invoke(volume);
                            Log.d(TAG, "Android 7-10 找到TF卡路径: " + sdCardPath);
                            break;
                        } catch (Exception e) {
                            Log.e(TAG, "获取StorageVolume路径失败: " + e.getMessage());
                        }
                    }
                }
            }
            
            // 如果上述方法都失败，尝试使用Environment
            if (sdCardPath == null) {
                File[] externalDirs = context.getExternalFilesDirs(null);
                for (File dir : externalDirs) {
                    if (dir != null && Environment.isExternalStorageRemovable(dir)) {
                        // 获取外部存储根目录
                        String path = dir.getAbsolutePath();
                        // 移除Android/data/包名部分，获取根目录
                        int index = path.indexOf("/Android/data/");
                        if (index > 0) {
                            sdCardPath = path.substring(0, index);
                            Log.d(TAG, "通过Environment找到TF卡路径: " + sdCardPath);
                            break;
                        }
                    }
                }
            }
            
            // 如果还是找不到，尝试常见路径
            if (sdCardPath == null) {
                String[] commonPaths = {
                    "/storage/sdcard1",
                    "/storage/extSdCard",
                    "/storage/external_SD",
                    "/storage/SD",
                    "/mnt/sdcard1",
                    "/mnt/extSdCard",
                    "/storage/0000-0000", // 通用格式的TF卡路径
                    "/storage/emulated/0/external_sd"
                };
                
                for (String path : commonPaths) {
                    File potentialPath = new File(path);
                    if (potentialPath.exists() && potentialPath.canRead()) {
                        sdCardPath = path;
                        Log.d(TAG, "通过常见路径找到TF卡: " + sdCardPath);
                        break;
                    }
                }
            }
            
            if (sdCardPath == null) {
                result.put("success", false);
                result.put("error", "无法获取TF卡路径");
                return result;
            }

            // 构建CSV文件完整路径
            File csvFile = new File(sdCardPath, csvFileName);
            Log.d(TAG, "尝试访问CSV文件: " + csvFile.getAbsolutePath());
            Log.d(TAG, "文件是否存在: " + csvFile.exists());
            Log.d(TAG, "文件是否可读: " + csvFile.canRead());
            
            if (!csvFile.exists()) {
                // 如果在根目录找不到，尝试在Download文件夹中查找
                csvFile = new File(sdCardPath + "/Download", csvFileName);
                Log.d(TAG, "尝试在Download文件夹中查找: " + csvFile.getAbsolutePath());
                Log.d(TAG, "文件是否存在: " + csvFile.exists());
                Log.d(TAG, "文件是否可读: " + csvFile.canRead());
            }

            // 分别检查文件存在性和读取权限
            if (!csvFile.exists()) {
                result.put("success", false);
                result.put("error", "CSV文件不存在，已尝试以下路径：\n" + 
                          "1. " + new File(sdCardPath, csvFileName).getAbsolutePath() + "\n" +
                          "2. " + new File(sdCardPath + "/Download", csvFileName).getAbsolutePath());
                return result;
            }

            if (!csvFile.canRead()) {
                result.put("success", false);
                result.put("error", "无法读取CSV文件（权限不足）: " + csvFile.getAbsolutePath() + 
                          "\n文件权限: " + getFilePermissions(csvFile));
                return result;
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
                    
                    // 跳过空行
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    
                    // 分割CSV行
                    String[] parts = line.split(",");
                    if (parts.length > 0) {
                        String license = parts[0].trim();
                        // 检查license是否有效（不为空且长度合理）
                        if (!license.isEmpty() && license.length() >= 4) {
                            // 如果只有一列，或第二列为空，则该license可用
                            if (parts.length == 1 || parts[1].trim().isEmpty()) {
                                availableLicense = license;
                                Log.d(TAG, "找到可用的license: " + license);
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "读取CSV文件时出错: " + e.getMessage());
                result.put("success", false);
                result.put("error", "读取CSV文件时出错: " + e.getMessage());
                return result;
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
     * 获取文件权限信息
     * @param file 要检查的文件
     * @return 权限信息字符串
     */
    private String getFilePermissions(File file) {
        StringBuilder permissions = new StringBuilder();
        permissions.append("可读: ").append(file.canRead());
        permissions.append(", 可写: ").append(file.canWrite());
        permissions.append(", 可执行: ").append(file.canExecute());
        
        try {
            String[] command = {"ls", "-l", file.getAbsolutePath()};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                permissions.append("\n系统权限: ").append(line);
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "获取文件权限详情失败: " + e.getMessage());
        }
        
        return permissions.toString();
    }
}
