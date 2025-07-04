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

export interface UsbPortInfo {
  deviceId: number;
  deviceName: string;
  manufacturerName?: string;
  productName?: string;
  interfaceCount: number;
  vendorId: number;
  productId: number;
  deviceClass: string;
  isConnected: boolean;
}

export interface TypeCPortInfo {
  isAvailable: boolean;
  isCharging?: boolean;
  isDataTransferEnabled?: boolean;
  error?: string;
}

export interface TFCardInfo {
  isAvailable: boolean;
  isMounted: boolean;
  state: string;
  totalSpace?: number;
  availableSpace?: number;
}

export interface ExternalPortsStatus {
  success: boolean;
  error?: string;
  usbPorts?: UsbPortInfo[];
  typeC?: TypeCPortInfo;
  tfCard?: TFCardInfo;
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

  /**
   * 检测设备外接端口状态
   * 
   * 检测设备的USB端口（包括Type-C）和TF卡槽的状态，返回：
   * - USB端口列表及其详细信息
   * - Type-C端口状态（是否可用、是否充电、是否支持数据传输）
   * - TF卡槽状态（是否可用、是否已挂载、存储空间信息）
   * 
   * @returns 外接端口状态信息
   * @example
   * const result = await tools.checkExternalPorts();
   * // 返回结果示例：
   * // {
   * //   "success": true,
   * //   "usbPorts": [{
   * //     "deviceId": 1,
   * //     "deviceName": "USB Device",
   * //     "manufacturerName": "Company",
   * //     "productName": "Product",
   * //     "interfaceCount": 1,
   * //     "vendorId": 1234,
   * //     "productId": 5678,
   * //     "deviceClass": "大容量存储设备",
   * //     "isConnected": true
   * //   }],
   * //   "typeC": {
   * //     "isAvailable": true,
   * //     "isCharging": true,
   * //     "isDataTransferEnabled": true
   * //   },
   * //   "tfCard": {
   * //     "isAvailable": true,
   * //     "isMounted": true,
   * //     "state": "mounted",
   * //     "totalSpace": 32000000000,
   * //     "availableSpace": 16000000000
   * //   }
   * // }
   */
  checkExternalPorts(): Promise<ExternalPortsStatus>;
}
