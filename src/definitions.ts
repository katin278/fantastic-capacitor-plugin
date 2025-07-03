export interface WifiNetwork {
  ssid: string;
  bssid: string;
  level: number;
  signalStrength: number;
  frequency: number;
  isConnected: boolean;
  securityType: string;
  isEnterprise: boolean;
}

export interface WifiConnectionResult {
  success: boolean;
  message?: string;
}

export interface toolsPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  
  /**
   * 通过DevicePolicyManager直接授予权限
   * @param options.permissions 需要授予的权限数组
   * @returns 授权结果对象
   * @example
   * const result = await tools.grantPermissions({
   *   permissions: [
   *     'android.permission.CAMERA',
   *     'android.permission.ACCESS_FINE_LOCATION'
   *   ]
   * });
   * // 返回结果示例：
   * // {
   * //   "success": true,
   * //   "results": {
   * //     "android.permission.CAMERA": true,
   * //     "android.permission.ACCESS_FINE_LOCATION": true
   * //   }
   * // }
   */
  grantPermissions(options: {
    permissions: string[]
  }): Promise<{
    success: boolean;
    error?: string;
    results?: {
      [permission: string]: boolean;
    }
  }>;
  
  /**
   * 检查指定的Android权限是否已被授予
   * @param options.permissions 要检查的权限数组
   * @returns 包含每个权限及其授予状态的对象
   * @example
   * const result = await tools.checkPermissions({
   *   permissions: [
   *     'android.permission.CAMERA',
   *     'android.permission.ACCESS_FINE_LOCATION'
   *   ]
   * });
   * // 返回结果示例：
   * // {
   * //   "android.permission.CAMERA": true,
   * //   "android.permission.ACCESS_FINE_LOCATION": false
   * // }
   */
  checkPermissions(options: { 
    permissions: string[] 
  }): Promise<{ [permission: string]: boolean }>;
  
  /**
   * 检查是否具有Wi-Fi相关权限
   */
  checkWifiPermissions(): Promise<{ hasPermissions: boolean }>;
  
  /**
   * 请求Wi-Fi相关权限
   */
  requestWifiPermissions(): Promise<{
    granted: boolean;
    locationPermission: boolean;
    wifiPermission: boolean;
  }>;
  
  /**
   * 获取附近Wi-Fi网络详细信息列表
   * 
   * 返回每个Wi-Fi的SSID、BSSID、安全协议类型、信号强度和是否为企业级Wi-Fi等信息
   */
  getWifiList(): Promise<{ wifiList: WifiNetwork[] }>;

  /**
   * 连接到开放Wi-Fi网络（无密码）
   * 
   * @param options.ssid Wi-Fi网络名称
   */
  connectToOpenWifi(options: { ssid: string }): Promise<WifiConnectionResult>;

  /**
   * 连接到个人Wi-Fi网络（需要密码）
   * 
   * @param options.ssid Wi-Fi网络名称
   * @param options.password Wi-Fi密码
   */
  connectToPersonalWifi(options: { 
    ssid: string; 
    password: string 
  }): Promise<WifiConnectionResult>;

  /**
   * 连接到企业Wi-Fi网络（需要密码和身份认证）
   * 
   * @param options.ssid Wi-Fi网络名称
   * @param options.password Wi-Fi密码
   * @param options.identity 企业Wi-Fi身份（通常是用户名/邮箱）
   */
  connectToEnterpriseWifi(options: { 
    ssid: string; 
    password: string;
    identity: string;
  }): Promise<WifiConnectionResult>;
}
