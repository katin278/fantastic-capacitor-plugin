import type { PluginListenerHandle } from '@capacitor/core';

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

export interface SDCardState {
  event?: 'mounted' | 'unmounted' | 'removed' | 'shared' | 'checking';
  path?: string;
  isAvailable: boolean;
  hasCardInserted: boolean;
  isMounted: boolean;
  state: string;
  totalSpace?: number;
  availableSpace?: number;
}

export interface SDCardMonitoringResult {
  success: boolean;
  error?: string;
}

export interface LicenseResult {
  success: boolean;
  license?: string;
  error?: string;
}

export interface SignatureDetails {
  md5: string;
  sha1: string;
  sha256: string;
  md5_formatted: string;
  sha1_formatted: string;
  sha256_formatted: string;
  issuer: string;
  subject: string;
  serialNumber: string;
  validFrom: string;
  validUntil: string;
}

export interface AppSignatureResult {
  success: boolean;
  packageName: string;
  currentSignature: string;
  isOriginalSignature: boolean;
  signatureDetails?: SignatureDetails;
  error?: string;
}

export interface DeviceDateTimeResult {
  /** 操作是否成功 */
  success: boolean;
  
  /** 如果操作失败，包含错误信息 */
  error?: string;
  
  /** 当前日期时间，格式：YYYY-MM-DD HH:mm:ss
   * 例如：2024-03-15 14:30:45
   */
  currentDateTime?: string;
  
  /** ISO 8601格式的日期时间，包含时区信息
   * 例如：2024-03-15T14:30:45.123+08:00
   */
  iso8601DateTime?: string;
  
  /** 当前时间戳（毫秒）
   * 从1970年1月1日UTC零点开始的毫秒数
   */
  timestamp?: number;
  
  /** Unix时间戳（秒）
   * 从1970年1月1日UTC零点开始的秒数
   */
  unixTimestamp?: number;
  
  /** 时区ID
   * 例如：'Asia/Shanghai', 'America/New_York'
   */
  timeZoneId?: string;
  
  /** 时区名称，本地化显示
   * 例如：'中国标准时间', '美国东部时间'
   */
  timeZoneName?: string;
  
  /** 时区偏移量（小时）
   * 正数表示超前UTC，负数表示落后UTC
   * 例如：+8.0表示北京时间，-5.0表示纽约时间
   */
  timeZoneOffset?: number;
  
  /** 是否处于夏令时 
   * true: 当前处于夏令时
   * false: 当前处于标准时间
   */
  isDaylightTime?: boolean;
  
  /** 系统是否使用24小时制
   * true: 使用24小时制（例如：14:30）
   * false: 使用12小时制（例如：2:30 PM）
   */
  is24HourFormat?: boolean;
  
  /** 系统是否启用了自动时间设置
   * true: 系统会自动从网络获取和更新时间
   * false: 用户手动设置时间
   */
  autoTimeEnabled?: boolean;
  
  /** 系统是否启用了自动时区设置
   * true: 系统会根据位置自动设置时区
   * false: 用户手动设置时区
   */
  autoTimeZoneEnabled?: boolean;
  
  /** 设备时间是否准确
   * true: 时间被认为是准确的
   * false: 时间可能不准确
   * Android: 基于自动时间设置状态
   * Web: 总是返回true
   */
  isTimeAccurate?: boolean;
  
  /** 与网络时间服务器的时间偏差（毫秒）
   * 0: 表示时间准确或无法获取偏差
   * 正数: 表示设备时间快于标准时间
   * 负数: 表示设备时间慢于标准时间
   * Web平台始终返回0
   */
  timeOffsetFromNTP?: number;
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

  /**
   * 开始监听TF卡槽状态变化
   * @returns 监听是否成功启动
   */
  startMonitoringSDCard(): Promise<SDCardMonitoringResult>;

  /**
   * 停止监听TF卡槽状态
   * @returns 是否成功停止监听
   */
  stopMonitoringSDCard(): Promise<SDCardMonitoringResult>;

  /**
   * 添加TF卡状态变化的监听器
   * @param eventName 事件名称 'sdCardStateChanged'
   * @param callback 回调函数，接收状态变化信息
   */
  addListener(
    eventName: 'sdCardStateChanged',
    callback: (state: SDCardState) => void,
  ): Promise<PluginListenerHandle>;

  /**
   * 从TF卡中读取CSV文件并获取第一个可用的license
   * 
   * CSV文件格式要求：
   * - 第一列：license
   * - 第二列：status（为空表示可用）
   * 
   * @param options.fileName CSV文件名（相对于TF卡根目录）
   * @returns 包含可用license的结果对象
   * @example
   * const result = await tools.getAvailableLicenseFromSD({
   *   fileName: 'licenses.csv'
   * });
   * // 成功返回：
   * // {
   * //   "success": true,
   * //   "license": "XXXX-XXXX-XXXX-XXXX"
   * // }
   * // 失败返回：
   * // {
   * //   "success": false,
   * //   "error": "错误信息"
   * // }
   */
  getAvailableLicenseFromSD(options: {
    fileName: string;
  }): Promise<LicenseResult>;

  /**
   * 检查应用是否被重新签名
   * 
   * 用于检查应用是否被重新签名，可以用来验证应用的完整性。
   * 返回签名的详细信息，包括MD5、SHA-1、SHA-256等多种格式的签名值，
   * 以及证书的详细信息（发行者、有效期等）。
   * 
   * @returns 包含签名检查结果的Promise
   * @example
   * const result = await tools.checkAppSignature();
   * // 返回结果示例：
   * // {
   * //   "success": true,
   * //   "packageName": "com.example.app",
   * //   "currentSignature": "80abf06c4d842440dc...",
   * //   "isOriginalSignature": true,
   * //   "signatureDetails": {
   * //     "md5": "1234567890abcdef...",
   * //     "sha1": "1234567890abcdef...",
   * //     "sha256": "80abf06c4d842440dc...",
   * //     "issuer": "CN=Example",
   * //     "validFrom": "2023-01-01",
   * //     "validUntil": "2024-01-01"
   * //   }
   * // }
   */
  checkAppSignature(): Promise<AppSignatureResult>;

  /**
   * 验证设备日期和时间
   * @returns Promise<DeviceDateTimeResult> 包含设备日期时间信息的Promise
   */
  checkDeviceDateTime(): Promise<DeviceDateTimeResult>;
}
