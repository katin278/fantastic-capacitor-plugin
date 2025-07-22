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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import android.content.BroadcastReceiver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
import android.net.NetworkInfo;
import android.net.DhcpInfo;

import java.util.Locale;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.NetworkInterface;
import java.util.Collections;
import android.content.pm.ApplicationInfo;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

            // 获取存储设备信息
            String storageModel = "未知";
            String storageName = "未知";
            try {
                Process process = Runtime.getRuntime().exec("mount");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(internalStorage.getPath())) {
                        String[] parts = line.split(" ");
                        if (parts.length > 0) {
                            File deviceFile = new File("/sys/block/" + parts[0].substring(parts[0].lastIndexOf("/") + 1));
                            if (deviceFile.exists()) {
                                File modelFile = new File(deviceFile, "device/model");
                                File vendorFile = new File(deviceFile, "device/vendor");
                                if (modelFile.exists() && vendorFile.exists()) {
                                    BufferedReader modelReader = new BufferedReader(new FileReader(modelFile));
                                    BufferedReader vendorReader = new BufferedReader(new FileReader(vendorFile));
                                    storageModel = modelReader.readLine();
                                    storageName = vendorReader.readLine() + " " + storageModel;
                                    modelReader.close();
                                    vendorReader.close();
                                }
                            }
                        }
                    }
                }
                reader.close();
            } catch (Exception e) {
                Log.e(TAG, "读取存储设备信息失败: " + e.getMessage());
            }
            
            storage.put("totalSpace", totalGB);
            storage.put("availableSpace", availableGB);
            storage.put("freeSpace", freeGB);
            storage.put("model", storageModel);
            storage.put("name", storageName);
            storage.put("details", String.format(
                "存储设备: %s\n型号: %s\n总空间: %.2f GB, 可用空间: %.2f GB, 剩余空间: %.2f GB",
                storageName, storageModel, totalGB, availableGB, freeGB
            ));
            
            // 获取内存信息
            JSONObject memory = new JSONObject();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memInfo);
            
            double totalMemGB = bytesToGB(memInfo.totalMem);
            double availableMemGB = bytesToGB(memInfo.availMem);

            // 获取内存型号信息
            String memoryModel = "未知";
            String memoryName = "未知";
            try {
                BufferedReader reader = new BufferedReader(new FileReader("/proc/meminfo"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Hardware")) {
                        String[] parts = line.split(":");
                        if (parts.length > 1) {
                            memoryModel = parts[1].trim();
                            memoryName = memoryModel;
                        }
                        break;
                    }
                }
                reader.close();
            } catch (Exception e) {
                Log.e(TAG, "读取内存型号失败: " + e.getMessage());
            }
            
            memory.put("totalMemory", totalMemGB);
            memory.put("availableMemory", availableMemGB);
            memory.put("lowMemory", memInfo.lowMemory);
            memory.put("model", memoryModel);
            memory.put("name", memoryName);
            memory.put("details", String.format(
                "内存设备: %s\n型号: %s\n总内存: %.2f GB, 可用内存: %.2f GB, 低内存状态: %s",
                memoryName, memoryModel, totalMemGB, availableMemGB,
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

            // 读取CPU型号信息
            String cpuModel = "未知";
            String cpuName = "未知";
            try {
                BufferedReader reader = new BufferedReader(new FileReader("/proc/cpuinfo"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Hardware") || line.startsWith("model name")) {
                        String[] parts = line.split(":");
                        if (parts.length > 1) {
                            cpuModel = parts[1].trim();
                            cpuName = cpuModel;
                        }
                        break;
                    }
                }
                reader.close();
            } catch (Exception e) {
                Log.e(TAG, "读取CPU型号失败: " + e.getMessage());
            }
            
            cpu.put("cores", cores);
            cpu.put("frequency", maxFreq);
            cpu.put("model", cpuModel);
            cpu.put("name", cpuName);
            cpu.put("details", String.format(
                "处理器: %s\n型号: %s\nCPU核心数: %d, 频率: %.2f GHz",
                cpuName, cpuModel, cores, maxFreq
            ));
            
            // 获取光线传感器信息
            JSONArray sensors = new JSONArray();
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            
            if (lightSensor != null) {
                JSONObject sensorInfo = new JSONObject();
                sensorInfo.put("name", lightSensor.getName());
                sensorInfo.put("type", "光线传感器");
                sensorInfo.put("vendor", lightSensor.getVendor());
                sensorInfo.put("model", lightSensor.getName());
                
                // 检查传感器是否正常工作
                boolean isWorking = false;
                String workingStatus = "";
                
                try {
                    // 检查传感器基本参数
                    boolean hasValidRange = lightSensor.getMaximumRange() > 0;
                    boolean hasValidPower = lightSensor.getPower() >= 0;
                    boolean hasValidResolution = lightSensor.getResolution() > 0;
                    
                    // 尝试注册监听器来验证传感器
                    boolean canRegister = sensorManager.registerListener(
                        new android.hardware.SensorEventListener() {
                            @Override
                            public void onSensorChanged(android.hardware.SensorEvent event) {}
                            
                            @Override
                            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
                        },
                        lightSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    );
                    
                    // 立即取消注册
                    if (canRegister) {
                        sensorManager.unregisterListener(new android.hardware.SensorEventListener() {
                            @Override
                            public void onSensorChanged(android.hardware.SensorEvent event) {}
                            
                            @Override
                            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
                        });
                    }
                    
                    // 综合判断传感器状态
                    isWorking = hasValidRange && hasValidPower && hasValidResolution && canRegister;
                    
                    // 构建状态描述
                    StringBuilder status = new StringBuilder();
                    if (!hasValidRange) status.append("量程异常; ");
                    if (!hasValidPower) status.append("功耗异常; ");
                    if (!hasValidResolution) status.append("分辨率异常; ");
                    if (!canRegister) status.append("无法注册监听器; ");
                    
                    workingStatus = status.length() > 0 ? status.toString() : "正常";
                    
                } catch (Exception e) {
                    isWorking = false;
                    workingStatus = "检测出错: " + e.getMessage();
                }
                
                sensorInfo.put("isWorking", isWorking);
                
                StringBuilder details = new StringBuilder();
                details.append(String.format("传感器名称: %s\n", lightSensor.getName()));
                details.append(String.format("型号: %s\n", lightSensor.getName()));
                details.append(String.format("制造商: %s\n", lightSensor.getVendor()));
                details.append(String.format("版本: %d\n", lightSensor.getVersion()));
                details.append(String.format("功耗: %.2f mA\n", lightSensor.getPower()));
                details.append(String.format("最大量程: %.2f\n", lightSensor.getMaximumRange()));
                details.append(String.format("分辨率: %.6f\n", lightSensor.getResolution()));
                details.append(String.format("最小延迟: %d μs\n", lightSensor.getMinDelay()));
                details.append(String.format("工作状态: %s", workingStatus));
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    details.append(String.format("\n最大延迟: %d μs", lightSensor.getMaxDelay()));
                    details.append(String.format("\n报告模式: %s", getReportingMode(lightSensor.getReportingMode())));
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
     * 从TF卡中读取配置文件并获取所有配置信息
     * @param context Android上下文
     * @param configFileName 配置文件名（相对于TF卡根目录）
     * @return 包含所有配置信息的JSON对象
     */
    public JSONObject getWifiNameFromConfig(Context context, String configFileName) {
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

            // 构建配置文件完整路径并尝试多个可能的位置
            File configFile = null;
            String[] possibleLocations = {
                sdCardPath + "/" + configFileName,
                sdCardPath + "/Download/" + configFileName,
                sdCardPath + "/Documents/" + configFileName,
                sdCardPath + "/Android/data/" + context.getPackageName() + "/files/" + configFileName
            };

            for (String location : possibleLocations) {
                File testFile = new File(location);
                if (testFile.exists()) {
                    configFile = testFile;
                    Log.d(TAG, "找到配置文件: " + location);
                    break;
                }
            }

            if (configFile == null) {
                result.put("success", false);
                result.put("error", "找不到配置文件，已尝试以下路径：\n" + String.join("\n", possibleLocations));
                return result;
            }

            // 检查文件权限
            if (!configFile.canRead()) {
                // 尝试修改文件权限
                try {
                    configFile.setReadable(true, false);
                    configFile.setExecutable(true, false);
                    
                    // 如果还是不能读取，尝试使用命令行修改权限
                    if (!configFile.canRead()) {
                        Process process = Runtime.getRuntime().exec(new String[]{
                            "chmod",
                            "644",
                            configFile.getAbsolutePath()
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
                if (!configFile.canRead()) {
                    result.put("success", false);
                    result.put("error", "无法读取配置文件（权限不足）\n" +
                              "文件路径: " + configFile.getAbsolutePath() + "\n" +
                              "当前权限: " + getFilePermissions(configFile) + "\n" +
                              "请确保应用有足够的权限访问外部存储");
                    return result;
                }
            }

            // 读取配置文件
            StringBuilder jsonContent = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(configFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
                
                // 解析JSON内容
                JSONObject config = new JSONObject(jsonContent.toString());
                
                // 检查必需字段
                String wifiName = config.optString("WiFiName", "");
                String wifiPassword = config.optString("WiFiPassword", "");
                
                if (wifiName.isEmpty() || wifiPassword.isEmpty()) {
                    result.put("success", false);
                    result.put("error", "配置文件中缺少必需的WiFiName或WiFiPassword字段");
                    return result;
                }
                
                // 设置基本字段
                result.put("success", true);
                result.put("wifiName", wifiName);
                result.put("wifiPassword", wifiPassword);
                result.put("configFilePath", configFile.getAbsolutePath());
                
                // 设置可选字段
                if (config.has("WiFiType")) {
                    result.put("wifiType", config.getString("WiFiType"));
                }
                if (config.has("AutoConnect")) {
                    result.put("autoConnect", config.getBoolean("AutoConnect"));
                }
                if (config.has("Timeout")) {
                    result.put("timeout", config.getInt("Timeout"));
                }
                if (config.has("RetryCount")) {
                    result.put("retryCount", config.getInt("RetryCount"));
                }
                if (config.has("LastUpdated")) {
                    result.put("lastUpdated", config.getString("LastUpdated"));
                }
                
                // 添加完整的配置对象
                result.put("config", config);
                
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "关闭配置文件时出错: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "读取配置文件时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", "读取配置文件时出错: " + e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 执行Shell命令
     * @param cmd 要执行的命令
     * @return 命令执行结果
     */
    private String execCommand(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;
        Process p = null;

        try {
            p = Runtime.getRuntime().exec("sh");
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            dos.writeBytes(cmd);
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;

            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            while ((line = br.readLine()) != null) {
                result += line;
            }
            p.waitFor();
        } catch (Exception e) {
            Log.e(TAG, "执行命令时出错: " + e.getMessage());
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    Log.e(TAG, "关闭输出流时出错: " + e.getMessage());
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    Log.e(TAG, "关闭输入流时出错: " + e.getMessage());
                }
            }
        }
        return result;
    }

    /**
     * 检查命令执行是否成功
     * @param result 命令执行结果
     * @return 是否成功
     */
    private boolean isCommandSuccessful(String result) {
        return result != null && !result.contains("Error") && !result.contains("Permission denied");
    }

    /**
     * 检查vendor目录是否可读
     * @return 是否可读
     */
    private boolean isVendorReadable() {
        return new File("/vendor/device_info.json").canRead();
    }

    /**
     * 将设备信息文件拷贝到应用可访问的位置
     * @param context Android上下文
     * @return 拷贝结果，成功返回目标文件路径，失败返回null
     */
    private String copyDeviceInfoToAccessibleLocation(Context context) {
        String destPath = context.getFilesDir() + "/device_info.json";
        Log.d(TAG, "正在将 /vendor/device_info.json 拷贝到 " + destPath);

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // 确保目标目录存在
            outputStream.writeBytes("mkdir -p " + new File(destPath).getParent() + "\n");
            outputStream.flush();
            
            // 拷贝文件
            outputStream.writeBytes("cp /vendor/device_info.json " + destPath + "\n");
            outputStream.flush();
            
            // 修改权限确保应用可以读取
            outputStream.writeBytes("chmod 644 " + destPath + "\n");
            outputStream.flush();
            
            outputStream.writeBytes("exit\n");
            outputStream.flush();

            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            process.waitFor();
            
            // 检查文件是否成功拷贝
            if (new File(destPath).exists() && new File(destPath).canRead()) {
                Log.d(TAG, "文件成功拷贝到 " + destPath);
                return destPath;
            } else {
                Log.e(TAG, "文件拷贝失败: " + result.toString());
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "拷贝过程出错", e);
            return null;
        }
    }

    /**
     * 读取设备信息文件
     * @param context Android上下文
     * @return 包含设备信息的JSON对象
     */
    public JSONObject getDeviceInfo(Context context) {
        JSONObject result = new JSONObject();
        
        try {
            // 首先尝试直接读取
            File deviceInfoFile = new File("/vendor/device_info.json");
            String filePath = "/vendor/device_info.json";
            
            // 如果无法直接读取，尝试拷贝到应用目录
            if (!deviceInfoFile.canRead()) {
                Log.d(TAG, "无法直接读取设备信息文件，尝试拷贝到应用目录");
                String newPath = copyDeviceInfoToAccessibleLocation(context);
                if (newPath != null) {
                    deviceInfoFile = new File(newPath);
                    filePath = newPath;
                } else {
                    result.put("success", false);
                    result.put("error", "无法访问设备信息文件");
                    return result;
                }
            }

            // 读取文件内容
            StringBuilder jsonContent = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(deviceInfoFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
                
                // 解析JSON内容
                JSONObject deviceInfo = new JSONObject(jsonContent.toString());
                
                result.put("success", true);
                result.put("deviceInfo", deviceInfo);
                result.put("filePath", filePath);
                
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "关闭文件时出错: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "读取设备信息时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", "读取设备信息时出错: " + e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 在vendor目录下创建或更新device_info.json文件
     * @param context Android上下文
     * @param deviceInfo 要写入的设备信息
     * @return 操作结果
     */
    public JSONObject writeDeviceInfo(Context context, JSONObject deviceInfo) {
        JSONObject result = new JSONObject();
        final String VENDOR_FILE_PATH = "/vendor/device_info.json";
        
        try {
            // 检查文件是否已存在
            File deviceInfoFile = new File(VENDOR_FILE_PATH);
            boolean isNewFile = !deviceInfoFile.exists();
            
            // 首先将JSON内容写入临时文件
            File tempFile = new File(context.getCacheDir(), "temp_device_info.json");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(deviceInfo.toString(4)); // 使用4个空格缩进
            writer.close();
            
            // 准备使用root权限执行命令
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // 重新挂载vendor分区为可写
            outputStream.writeBytes("mount -o remount,rw /vendor\n");
            outputStream.flush();
            
            // 如果是新文件，先创建
            if (isNewFile) {
                outputStream.writeBytes("touch " + VENDOR_FILE_PATH + "\n");
                outputStream.flush();
            }
            
            // 拷贝临时文件到vendor目录
            outputStream.writeBytes("cat " + tempFile.getAbsolutePath() + " > " + VENDOR_FILE_PATH + "\n");
            outputStream.flush();
            
            // 设置适当的权限
            outputStream.writeBytes("chmod 644 " + VENDOR_FILE_PATH + "\n");
            outputStream.flush();
            
            // 重新挂载vendor分区为只读（安全考虑）
            outputStream.writeBytes("mount -o remount,ro /vendor\n");
            outputStream.flush();
            
            outputStream.writeBytes("exit\n");
            outputStream.flush();

            // 读取命令输出
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            // 读取错误输出
            StringBuilder error = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }

            // 等待命令执行完成
            int exitValue = process.waitFor();
            
            // 删除临时文件
            tempFile.delete();

            // 验证文件是否成功创建/更新
            if (exitValue == 0 && new File(VENDOR_FILE_PATH).exists()) {
                result.put("success", true);
                result.put("filePath", VENDOR_FILE_PATH);
                result.put("isNewFile", isNewFile);
                Log.d(TAG, "设备信息" + (isNewFile ? "创建" : "更新") + "成功");
            } else {
                result.put("success", false);
                result.put("error", "命令执行失败。错误信息：" + error.toString());
                Log.e(TAG, "写入设备信息失败：" + error.toString());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "写入设备信息时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", "写入设备信息时出错: " + e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 修改SD卡中license.csv文件中指定license的状态
     * @param context Android上下文
     * @param csvFileName CSV文件名
     * @param licenseKey 要修改的license
     * @param newStatus 新的状态值
     * @return 修改结果
     */
    public JSONObject updateLicenseStatus(Context context, String csvFileName, String licenseKey, String newStatus) {
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

            // 创建临时文件
            File tempFile = new File(context.getCacheDir(), "temp_" + csvFileName);
            boolean found = false;
            JSONObject licenseInfo = new JSONObject();
            
            // 读取CSV文件并修改指定license的状态
            BufferedReader reader = null;
            FileWriter writer = null;
            try {
                reader = new BufferedReader(new FileReader(csvFile));
                writer = new FileWriter(tempFile);
                
                String line;
                String header = null;
                while ((line = reader.readLine()) != null) {
                    // 保存标题行
                    if (header == null) {
                        header = line;
                        writer.write(line + "\n");
                        continue;
                    }
                    
                    // 解析CSV行
                    String[] columns = line.split(",", -1);
                    if (columns.length >= 2) {
                        String currentLicense = columns[0].trim();
                        
                        // 找到匹配的license
                        if (currentLicense.equals(licenseKey)) {
                            found = true;
                            // 更新状态
                            columns[1] = newStatus;
                            
                            // 保存license信息
                            String[] headerColumns = header.split(",", -1);
                            for (int i = 0; i < Math.min(headerColumns.length, columns.length); i++) {
                                licenseInfo.put(headerColumns[i].trim(), columns[i].trim());
                            }
                        }
                        
                        // 写入新行（更新后的或原始的）
                        writer.write(String.join(",", columns) + "\n");
                    } else {
                        // 保持原行不变
                        writer.write(line + "\n");
                    }
                }
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "关闭读取流时出错: " + e.getMessage());
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        Log.e(TAG, "关闭写入流时出错: " + e.getMessage());
                    }
                }
            }
            
            if (!found) {
                tempFile.delete();
                result.put("success", false);
                result.put("error", "未找到指定的license: " + licenseKey);
                return result;
            }
            
            // 将临时文件复制回原位置
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                // 复制文件
                outputStream.writeBytes("cat " + tempFile.getAbsolutePath() + " > " + csvFile.getAbsolutePath() + "\n");
                outputStream.flush();
                
                // 设置适当的权限
                outputStream.writeBytes("chmod 644 " + csvFile.getAbsolutePath() + "\n");
                outputStream.flush();
                
                outputStream.writeBytes("exit\n");
                outputStream.flush();

                // 读取错误输出
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    error.append(line).append("\n");
                }

                // 等待命令执行完成
                int exitValue = process.waitFor();
                
                // 删除临时文件
                tempFile.delete();

                if (exitValue != 0) {
                    result.put("success", false);
                    result.put("error", "更新文件失败: " + error.toString());
                    return result;
                }
            } catch (Exception e) {
                tempFile.delete();
                result.put("success", false);
                result.put("error", "更新文件时出错: " + e.getMessage());
                return result;
            }

            // 返回成功结果
            result.put("success", true);
            result.put("license", licenseInfo);
            
        } catch (Exception e) {
            Log.e(TAG, "修改license状态时出错: " + e.getMessage());
            try {
                result.put("success", false);
                result.put("error", "修改license状态时出错: " + e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建错误JSON时出错: " + je.getMessage());
            }
        }
        
        return result;
    }

    private JSONArray checkMultiSiteConnectivity(String[] sites) {
        JSONArray siteStatus = new JSONArray();
        
        // 如果没有提供网站列表，使用默认列表
        if (sites == null || sites.length == 0) {
            sites = new String[] {
                "https://www.baidu.com",
                "https://www.qq.com",
                "https://www.taobao.com"
            };
        }
        
        for (String site : sites) {
            HttpURLConnection connection = null;
            long startTime = System.currentTimeMillis();
            
            try {
                URL url = new URL(site);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                // 根据不同网站设置不同的超时时间
                if (site.equals("https://api.calendarloop.com")) {
                    connection.setConnectTimeout(20000);    // 连接超时：20秒
                    connection.setReadTimeout(20000);       // 读取超时：20秒
                } else {
                    connection.setConnectTimeout(5000);     // 连接超时：5秒
                    connection.setReadTimeout(5000);        // 读取超时：5秒
                }
                
                connection.setInstanceFollowRedirects(false); // 不跟随重定向
                
                // 发送请求
                int responseCode = connection.getResponseCode();
                long endTime = System.currentTimeMillis();
                JSONObject status = new JSONObject();
                status.put("url", site);
                status.put("isAvailable", responseCode >= 200 && responseCode < 400);
                status.put("responseTime", endTime - startTime);
                status.put("statusCode", responseCode);
                
                siteStatus.put(status);
                
            } catch (Exception e) {
                try {
                    JSONObject status = new JSONObject();
                    status.put("url", site);
                    status.put("isAvailable", false);
                    status.put("error", e.getMessage());
                    siteStatus.put(status);
                } catch (JSONException je) {
                    Log.e(TAG, "Error creating JSON for site status", je);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        
        return siteStatus;
    }

    // 保持原有的无参方法
    public JSONObject checkNetworkStatus(Context context) {
        return checkNetworkStatus(context, null);
    }

    // 新增带参数的方法
    public JSONObject checkNetworkStatus(Context context, String[] sites) {
        JSONObject result = new JSONObject();
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            
            boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
            String networkType = "none";
            JSONObject details = new JSONObject();
            
            if (isConnected) {
                // 获取网络类型
                switch (activeNetwork.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        networkType = "WIFI";
                        // 获取WIFI详细信息
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        if (wifiInfo != null) {
                            details.put("ssid", wifiInfo.getSSID().replace("\"", ""));
                            details.put("signalStrength", WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5));
                            details.put("linkSpeed", wifiInfo.getLinkSpeed());
                            details.put("ipAddress", formatIpAddress(wifiInfo.getIpAddress()));
                        }
                        break;
                    case ConnectivityManager.TYPE_MOBILE:
                        networkType = "MOBILE";
                        break;
                    case ConnectivityManager.TYPE_ETHERNET:
                        networkType = "ETHERNET";
                        break;
                    default:
                        networkType = "OTHER";
                }
                
                // 获取网络详细信息
                DhcpInfo dhcpInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getDhcpInfo();
                if (dhcpInfo != null) {
                    details.put("gateway", formatIpAddress(dhcpInfo.gateway));
                    details.put("dns", new JSONArray()
                        .put(formatIpAddress(dhcpInfo.dns1))
                        .put(formatIpAddress(dhcpInfo.dns2)));
                }
                
                // 检查多个网站的连通性
                JSONArray siteStatus = checkMultiSiteConnectivity(sites);
                boolean isInternetAvailable = false;
                
                // 如果任一网站可访问，则认为互联网可用
                for (int i = 0; i < siteStatus.length(); i++) {
                    JSONObject site = siteStatus.getJSONObject(i);
                    if (site.getBoolean("isAvailable")) {
                        isInternetAvailable = true;
                        break;
                    }
                }
                
                result.put("success", true);
                result.put("isConnected", true);
                result.put("isInternetAvailable", isInternetAvailable);
                result.put("networkType", networkType);
                result.put("siteStatus", siteStatus);
                result.put("details", details);
            } else {
                result.put("success", true);
                result.put("isConnected", false);
                result.put("isInternetAvailable", false);
                result.put("networkType", "none");
            }
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("isConnected", false);
                result.put("isInternetAvailable", false);
                result.put("error", e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "Error creating JSON response", je);
            }
        }
        return result;
    }

    private String formatIpAddress(int ipAddress) {
        return String.format(Locale.US, "%d.%d.%d.%d",
            (ipAddress & 0xff),
            (ipAddress >> 8 & 0xff),
            (ipAddress >> 16 & 0xff),
            (ipAddress >> 24 & 0xff));
    }

    /**
     * 获取设备MAC地址
     * 需要以下权限：
     * - ACCESS_FINE_LOCATION (Android 10+)
     * - ACCESS_WIFI_STATE
     * - LOCAL_MAC_ADDRESS (Android 10+, 部分机型可能需要)
     *
     * @param context 上下文
     * @return 包含MAC地址信息的JSONObject
     */
    public JSONObject getDeviceMacAddress(Context context) {
        JSONObject result = new JSONObject();
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            
            if (wifiManager == null) {
                result.put("success", false);
                result.put("message", "无法获取WifiManager服务");
                return result;
            }

            String macAddress = "02:00:00:00:00:00"; // 默认值
            String method = "default";
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10及以上版本
                if (!checkWifiPermissions(context)) {
                    result.put("success", false);
                    result.put("message", "缺少必要权限");
                    result.put("requiredPermissions", new JSONArray(getRequiredWifiPermissions()));
                    return result;
                }

                // 方法1：通过NetworkInterface获取（最可靠）
                String networkMac = getMacAddressFromNetworkInterface();
                if (networkMac != null && !networkMac.equals("02:00:00:00:00:00")) {
                    macAddress = networkMac;
                    method = "NetworkInterface";
                } else {
                    // 方法2：通过文件系统获取
                    String fileMac = getMacAddressFromFile();
                    if (fileMac != null && !fileMac.equals("02:00:00:00:00:00")) {
                        macAddress = fileMac;
                        method = "FileSystem";
                    } else {
                        // 方法3：通过命令行获取
                        String cmdMac = getMacAddressFromCommand();
                        if (cmdMac != null && !cmdMac.equals("02:00:00:00:00:00")) {
                            macAddress = cmdMac;
                            method = "Command";
                        } else {
                            // 方法4：尝试WifiInfo（通常返回随机MAC）
                            try {
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                if (wifiInfo != null) {
                                    String wifiMac = wifiInfo.getMacAddress();
                                    if (wifiMac != null) {
                                        macAddress = wifiMac;
                                        method = "WifiInfo(可能是随机MAC)";
                                    }
                                }
                            } catch (SecurityException e) {
                                Log.e(TAG, "获取MAC地址时发生安全异常: " + e.getMessage());
                            }
                        }
                    }
                }
            } else {
                // Android 10以下版本：直接通过WifiInfo获取
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    String wifiMac = wifiInfo.getMacAddress();
                    if (wifiMac != null) {
                        macAddress = wifiMac;
                        method = "WifiInfo";
                    }
                }
                
                // 如果WifiInfo获取失败，尝试其他方法
                if ("02:00:00:00:00:00".equals(macAddress)) {
                    String networkMac = getMacAddressFromNetworkInterface();
                    if (networkMac != null && !networkMac.equals("02:00:00:00:00:00")) {
                        macAddress = networkMac;
                        method = "NetworkInterface";
                    }
                }
            }

            result.put("success", true);
            result.put("macAddress", macAddress);
            result.put("method", method);
            result.put("androidVersion", Build.VERSION.SDK_INT);
            result.put("isRandomizedMac", "02:00:00:00:00:00".equals(macAddress) || 
                      macAddress.startsWith("02:"));
            
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("message", "获取MAC地址时发生错误: " + e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "创建JSON响应时发生错误", je);
            }
        }
        
        return result;
    }

    /**
     * 通过NetworkInterface获取MAC地址
     */
    private String getMacAddressFromNetworkInterface() {
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                String name = networkInterface.getName();
                // 尝试多种可能的网络接口名称
                if (name.equalsIgnoreCase("wlan0") || 
                    name.equalsIgnoreCase("eth0") || 
                    name.toLowerCase().startsWith("wlan") ||
                    name.toLowerCase().startsWith("wifi")) {
                    
                    byte[] hardwareAddress = networkInterface.getHardwareAddress();
                    if (hardwareAddress != null && hardwareAddress.length == 6) {
                        StringBuilder sb = new StringBuilder();
                        for (byte b : hardwareAddress) {
                            sb.append(String.format("%02X:", b));
                        }
                        if (sb.length() > 0) {
                            sb.deleteCharAt(sb.length() - 1);
                            String mac = sb.toString();
                            // 避免返回无效或随机化的MAC
                            if (!mac.equals("00:00:00:00:00:00") && 
                                !mac.startsWith("02:")) {
                                return mac;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "通过NetworkInterface获取MAC地址失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 通过文件系统获取MAC地址
     */
    private String getMacAddressFromFile() {
        String[] macFiles = {
            "/sys/class/net/wlan0/address",
            "/sys/class/net/eth0/address",
            "/sys/class/net/wifi/address"
        };
        
        for (String filePath : macFiles) {
            try {
                File file = new File(filePath);
                if (file.exists() && file.canRead()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String mac = reader.readLine();
                    reader.close();
                    
                    if (mac != null && mac.length() >= 17) {
                        mac = mac.toUpperCase().trim();
                        // 验证MAC地址格式
                        if (mac.matches("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$")) {
                            // 避免返回无效或随机化的MAC
                            if (!mac.equals("00:00:00:00:00:00") && 
                                !mac.startsWith("02:")) {
                                return mac;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "无法从文件读取MAC地址: " + filePath + ", " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * 通过命令行获取MAC地址
     */
    private String getMacAddressFromCommand() {
        try {
            // 尝试多种命令
            String[] commands = {
                "cat /sys/class/net/wlan0/address",
                "ip link show wlan0",
                "busybox ifconfig wlan0",
                "ifconfig wlan0"
            };
            
            for (String command : commands) {
                try {
                    Process process = Runtime.getRuntime().exec(command);
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 查找MAC地址模式
                        String mac = extractMacFromLine(line);
                        if (mac != null && !mac.equals("00:00:00:00:00:00") && 
                            !mac.startsWith("02:")) {
                            reader.close();
                            process.destroy();
                            return mac;
                        }
                    }
                    reader.close();
                    process.destroy();
                } catch (Exception e) {
                    Log.d(TAG, "命令执行失败: " + command + ", " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "通过命令行获取MAC地址失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 从文本行中提取MAC地址
     */
    private String extractMacFromLine(String line) {
        if (line == null) return null;
        
        // MAC地址的正则表达式模式
        String macPattern = "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(macPattern);
        java.util.regex.Matcher matcher = pattern.matcher(line);
        
        if (matcher.find()) {
            return matcher.group().toUpperCase().replace("-", ":");
        }
        return null;
    }

    public JSONObject disconnectAndForgetWifi(Context context) {
        JSONObject result = new JSONObject();
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                result.put("success", false);
                result.put("message", "无法获取WifiManager服务");
                return result;
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10及以上版本
                ConnectivityManager connectivityManager = 
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                
                if (connectivityManager == null) {
                    result.put("success", false);
                    result.put("message", "无法获取ConnectivityManager服务");
                    return result;
                }

                // 断开当前Wi-Fi连接
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                
                connectivityManager.requestNetwork(builder.build(), 
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            connectivityManager.bindProcessToNetwork(null);
                            connectivityManager.unregisterNetworkCallback(this);
                        }
                    });

                // 移除所有已保存的网络配置
                List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
                if (configuredNetworks != null) {
                    for (WifiConfiguration wifiConfig : configuredNetworks) {
                        wifiManager.removeNetwork(wifiConfig.networkId);
                    }
                    wifiManager.saveConfiguration();
                }

                result.put("success", true);
                result.put("message", "已成功断开并清除所有Wi-Fi连接");
            } else {
                result.put("success", false);
                result.put("message", "此功能仅支持Android 10及以上版本");
            }
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("message", "断开Wi-Fi时发生错误: " + e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "JSON错误: " + je.getMessage());
            }
        }
        return result;
    }

    /**
     * 清除应用数据和缓存
     * @param context Android上下文
     * @param packageName 要清除的应用包名，如果为空则清除当前应用
     * @return 操作结果
     */
    public JSONObject clearAppData(Context context, String packageName) {
        JSONObject result = new JSONObject();
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                try {
                    boolean hasRoot = checkRootAccess();
                    if (hasRoot || hasRequiredPermissions(context)) {
                        boolean cacheCleared = clearApplicationCache(context);
                        boolean dataCleared = clearApplicationData(context);
                        boolean dbCleared = clearDatabases(context);
                        boolean prefsCleared = clearSharedPreferences(context);
                        
                        result.put("success", true);
                        result.put("message", "已成功清除应用数据和缓存");
                        
                        JSONObject details = new JSONObject();
                        details.put("clearedCache", cacheCleared);
                        details.put("clearedData", dataCleared);
                        details.put("clearedDatabases", dbCleared);
                        details.put("clearedPreferences", prefsCleared);
                        details.put("packageName", packageName);
                        details.put("hasRootAccess", hasRoot);
                        result.put("details", details);
                    } else {
                        result.put("success", false);
                        result.put("message", "清除应用数据失败，可能需要系统权限");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "清除应用数据时出错", e);
                    result.put("success", false);
                    result.put("message", "清除应用数据时出错: " + getDetailedErrorMessage(e));
                }
            } else {
                result.put("success", false);
                result.put("message", "此功能仅支持Android 10及以上版本");
            }
        } catch (Exception e) {
            try {
                Log.e(TAG, "清除应用数据时发生异常", e);
                result.put("success", false);
                result.put("message", "操作失败: " + getDetailedErrorMessage(e));
            } catch (JSONException je) {
                Log.e(TAG, "JSON错误", je);
            }
        }
        
        return result;
    }

    private boolean hasRequiredPermissions(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否有root权限
     */
    private boolean checkRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.destroy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 清除应用缓存
     */
    private boolean clearApplicationCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            File externalCacheDir = context.getExternalCacheDir();
            
            boolean success = true;
            if (cacheDir != null) {
                success &= deleteDirectory(cacheDir);
            }
            if (externalCacheDir != null) {
                success &= deleteDirectory(externalCacheDir);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "清除缓存时出错", e);
            return false;
        }
    }

    /**
     * 清除应用数据
     */
    private boolean clearApplicationData(Context context) {
        try {
            File dataDir = new File(context.getApplicationInfo().dataDir);
            File[] externalFilesDirs = context.getExternalFilesDirs(null);
            
            boolean success = true;
            if (dataDir.exists()) {
                success &= deleteDirectory(dataDir);
            }
            if (externalFilesDirs != null) {
                for (File dir : externalFilesDirs) {
                    if (dir != null && dir.exists()) {
                        success &= deleteDirectory(dir);
                    }
                }
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "清除应用数据时出错", e);
            return false;
        }
    }

    /**
     * 清除数据库
     */
    private boolean clearDatabases(Context context) {
        try {
            for (String database : context.databaseList()) {
                context.deleteDatabase(database);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "清除数据库时出错", e);
            return false;
        }
    }

    /**
     * 清除SharedPreferences
     */
    private boolean clearSharedPreferences(Context context) {
        try {
            File prefsDir = new File(context.getApplicationInfo().dataDir, "shared_prefs");
            return !prefsDir.exists() || deleteDirectory(prefsDir);
        } catch (Exception e) {
            Log.e(TAG, "清除SharedPreferences时出错", e);
            return false;
        }
    }

    /**
     * 获取详细的错误信息
     */
    private String getDetailedErrorMessage(Exception e) {
        StringBuilder message = new StringBuilder(e.getMessage() != null ? e.getMessage() : "未知错误");
        
        if (e instanceof SecurityException) {
            message.append(" (权限不足，请确保应用具有系统权限)");
        } else if (e instanceof IllegalArgumentException) {
            message.append(" (参数错误，请检查包名是否正确)");
        } else if (e instanceof RuntimeException) {
            message.append(" (运行时错误，可能需要系统权限)");
        }
        
        return message.toString();
    }

    /**
     * 递归删除目录及其内容
     * @param directory 要删除的目录
     * @return 是否删除成功
     */
    private boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return false;
        }

        // 如果是文件，直接删除
        if (directory.isFile()) {
            return directory.delete();
        }

        // 如果是目录，递归删除内容
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteDirectory(file);
            }
        }

        // 删除空目录
        return directory.delete();
    }

    /**
     * 获取当前连接的Wi-Fi信息
     * @param context Android上下文
     * @return 包含Wi-Fi信息的JSON对象
     */
    public JSONObject getCurrentWifiInfo(Context context) {
        JSONObject result = new JSONObject();
        
        try {
            if (!checkWifiPermissions(context)) {
                result.put("success", false);
                result.put("message", "缺少必要权限");
                JSONArray permissions = new JSONArray()
                    .put(Manifest.permission.ACCESS_FINE_LOCATION)
                    .put(Manifest.permission.ACCESS_WIFI_STATE);
                result.put("requiredPermissions", permissions);
                return result;
            }

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                result.put("success", false);
                result.put("message", "无法获取WifiManager服务");
                return result;
            }

            if (!wifiManager.isWifiEnabled()) {
                result.put("success", true);
                result.put("isWifiEnabled", false);
                result.put("message", "Wi-Fi未启用");
                return result;
            }

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo == null) {
                result.put("success", true);
                result.put("isConnected", false);
                result.put("message", "未连接到任何Wi-Fi网络");
                return result;
            }

            // 获取基本连接信息
            String ssid = wifiInfo.getSSID();
            if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }

            result.put("success", true);
            result.put("isWifiEnabled", true);
            result.put("isConnected", true);

            // Wi-Fi基本信息
            JSONObject wifiDetails = new JSONObject();
            wifiDetails.put("ssid", ssid);
            wifiDetails.put("bssid", wifiInfo.getBSSID());
            wifiDetails.put("macAddress", wifiInfo.getMacAddress());
            wifiDetails.put("ipAddress", formatIpAddress(wifiInfo.getIpAddress()));
            wifiDetails.put("networkId", wifiInfo.getNetworkId());
            wifiDetails.put("rssi", wifiInfo.getRssi());
            wifiDetails.put("linkSpeed", wifiInfo.getLinkSpeed());
            wifiDetails.put("frequency", wifiInfo.getFrequency());
            wifiDetails.put("signalStrength", WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5));

            // 获取网络配置信息
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10及以上版本
                NetworkCapabilities networkCapabilities = null;
                ConnectivityManager connectivityManager = 
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                
                if (connectivityManager != null) {
                    Network network = connectivityManager.getActiveNetwork();
                    if (network != null) {
                        networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    }
                }

                if (networkCapabilities != null) {
                    JSONObject capabilities = new JSONObject();
                    capabilities.put("hasInternet", networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET));
                    capabilities.put("isValidated", networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED));
                    capabilities.put("maxDownloadSpeed", networkCapabilities.getLinkDownstreamBandwidthKbps());
                    capabilities.put("maxUploadSpeed", networkCapabilities.getLinkUpstreamBandwidthKbps());
                    wifiDetails.put("capabilities", capabilities);
                }
            }

            // 获取DHCP信息
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            if (dhcpInfo != null) {
                JSONObject dhcpDetails = new JSONObject();
                dhcpDetails.put("gateway", formatIpAddress(dhcpInfo.gateway));
                dhcpDetails.put("netmask", formatIpAddress(dhcpInfo.netmask));
                dhcpDetails.put("dns1", formatIpAddress(dhcpInfo.dns1));
                dhcpDetails.put("dns2", formatIpAddress(dhcpInfo.dns2));
                dhcpDetails.put("serverAddress", formatIpAddress(dhcpInfo.serverAddress));
                dhcpDetails.put("leaseDuration", dhcpInfo.leaseDuration);
                wifiDetails.put("dhcp", dhcpDetails);
            }

            // 获取安全类型
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            if (configuredNetworks != null) {
                for (WifiConfiguration config : configuredNetworks) {
                    if (config.networkId == wifiInfo.getNetworkId()) {
                        wifiDetails.put("securityType", getSecurityType(config));
                        break;
                    }
                }
            }

            result.put("wifiInfo", wifiDetails);
            
            // 添加连接状态
            result.put("connectionState", getWifiConnectionState(context));
            
        } catch (Exception e) {
            try {
                result.put("success", false);
                result.put("message", "获取Wi-Fi信息时出错: " + e.getMessage());
            } catch (JSONException je) {
                Log.e(TAG, "JSON错误", je);
            }
        }
        
        return result;
    }

    /**
     * 获取Wi-Fi连接状态
     */
    private String getWifiConnectionState(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = 
                    connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                            return "CONNECTED_VALIDATED";
                        }
                        return "CONNECTED";
                    }
                }
            }
        }
        return "DISCONNECTED";
    }

    /**
     * 获取Wi-Fi安全类型
     */
    private String getSecurityType(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return "WPA-PSK";
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
            config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return "WPA-EAP";
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
            if (config.wepKeys[0] != null) {
                return "WEP";
            }
            return "OPEN";
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.SAE)) {
                return "WPA3-SAE";
            }
            if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.OWE)) {
                return "OWE";
            }
            if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.SUITE_B_192)) {
                return "WPA3-SUITE-B";
            }
        }
        return "UNKNOWN";
    }
}
