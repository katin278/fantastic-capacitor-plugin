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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class tools {
    private static final String TAG = "FantasticWifiTools";

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
             
        // if (capabilities.contains("WEP")) {
        //     return "WEP";
        // } else if (capabilities.contains("WPA3-Enterprise")) {
        //     return "WPA3-Enterprise";
        // } else if (capabilities.contains("WPA3-SAE")) {
        //     return "WPA3-Personal";
        // } else if (capabilities.contains("WPA2-Enterprise")) {
        //     return "WPA2-Enterprise";
        // } else if (capabilities.contains("RSN") || capabilities.contains("WPA2")) {
        //     return "WPA2-Personal";
        // } else if (capabilities.contains("WPA-PSK")) {
        //     return "WPA-Personal";
        // } else if (capabilities.contains("WPA-EAP")) {
        //     return "WPA-Enterprise";
        // } else if (capabilities.contains("[ESS]")) {
        //     return "Open";
        // } else {
        //     return "Unknown";
        // }
        return scanResult.capabilities;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+使用新的NetworkRequest API
                return connectToWifiAndroid10Plus(context, ssid, password, isEnterpriseNetwork, identity);
            } else {
                // Android 9及以下使用传统的WifiConfiguration API
                return connectToWifiLegacy(context, wifiManager, ssid, password, isEnterpriseNetwork, identity);
            }
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
}
