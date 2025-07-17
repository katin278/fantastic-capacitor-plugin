import type { PluginListenerHandle } from '@capacitor/core';

export interface NetworkSiteStatus {
  url: string;
  isAvailable: boolean;
  responseTime?: number;    // 响应时间（毫秒）
  error?: string;          // 如果访问失败，错误信息
  statusCode?: number;     // HTTP状态码
  debugInfo?: {            // 调试信息
    type?: string;         // 响应类型
    contentType?: string;  // 内容类型
    headers?: Record<string, string>; // 响应头
    message?: string;      // 错误消息
    stack?: string;        // 错误堆栈
    [key: string]: any;    // 其他调试信息
  };
}

export interface NetworkStatusResult {
  success: boolean;
  error?: string;
  isConnected: boolean;        // 是否已连接到网络
  isInternetAvailable: boolean; // 是否可以访问互联网（任一网站可访问即为true）
  networkType?: string;        // 网络类型（WIFI/MOBILE/ETHERNET等）
  siteStatus?: NetworkSiteStatus[]; // 各网站的访问状态
  details?: {
    signalStrength?: number;   // 信号强度（仅WIFI）
    ssid?: string;            // WIFI名称（仅WIFI）
    ipAddress?: string;       // IP地址
    linkSpeed?: number;       // 连接速度（仅WIFI，Mbps）
    dns?: string[];          // DNS服务器
    gateway?: string;        // 网关地址
    latency?: number;        // 网络延迟（毫秒）
  };
}

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

export interface WebViewSettings {
  userAgent: string;
  javaScriptEnabled: boolean;
  databaseEnabled: boolean;
  domStorageEnabled: boolean;
  safeBrowsingEnabled?: boolean;
}

export interface WebViewInfoResult {
  success: boolean;
  error?: string;
  packageName?: string;
  versionName?: string;
  versionCode?: number;
  firstInstallTime?: number;
  lastUpdateTime?: number;
  settings?: WebViewSettings;
  androidVersion?: string;
  androidSDK?: number;
  isEnabled?: boolean;
  dataDirectory?: string;
}

export interface HardwareCheckResult {
  success: boolean;
  error?: string;
  
  // 存储检查结果
  storage: {
    passed: boolean;
    available: number;
    required: number;
    details: string;
  };
  
  // 内存检查结果
  memory: {
    passed: boolean;
    available: number;
    required: number;
    details: string;
  };
  
  // CPU检查结果
  cpu: {
    passed: boolean;
    cores: {
      available: number;
      required: number;
      passed: boolean;
    };
    frequency: {
      available: number;
      required: number;
      passed: boolean;
    };
    details: string;
  };
  
  // 传感器检查结果
  sensors: {
    passed: boolean;
    available: string[];
    required: string[];
    missing: string[];
    details: string;
  };
}

export interface StorageInfo {
  totalSpace: number;      // 总存储空间（GB）
  availableSpace: number;  // 可用存储空间（GB）
  freeSpace: number;       // 剩余存储空间（GB）
  details: string;         // 人类可读的详细信息
  isHealthy: boolean;      // 存储设备是否健康
  healthDetails: string;   // 健康状态详细信息
}

export interface MemoryInfo {
  totalMemory: number;     // 总内存（GB）
  availableMemory: number; // 可用内存（GB）
  lowMemory: boolean;      // 是否处于低内存状态
  details: string;         // 人类可读的详细信息
  isHealthy: boolean;      // 内存是否健康
  healthDetails: string;   // 健康状态详细信息
}

export interface CpuInfo {
  cores: number;           // CPU核心数
  frequency: number;       // CPU频率（GHz）
  isHealthy: boolean;      // CPU是否健康
  temperature: number;     // CPU温度（摄氏度）
  usage: number;          // CPU使用率（百分比）
  details: string;        // 详细信息
}

export interface SensorInfo {
  name: string;           // 传感器名称
  type: string;          // 传感器类型
  vendor: string;        // 制造商
  isWorking: boolean;    // 是否正常工作
  details: string;       // 详细信息
}

export interface HardwareInfoResult {
  success: boolean;
  error?: string;
  storage: StorageInfo;
  memory: MemoryInfo;
  cpu: CpuInfo;           // 添加CPU信息
  sensors: SensorInfo[];
}

export interface WifiConfigResult {
  success: boolean;
  wifiName?: string;
  wifiPassword?: string;
  wifiType?: string;
  autoConnect?: boolean;
  timeout?: number;
  retryCount?: number;
  lastUpdated?: string;
  configFilePath?: string;
  error?: string;
  // 添加完整的配置对象
  config?: {
    [key: string]: any;
  };
}

export interface DeviceInfoResult {
  success: boolean;
  error?: string;
  deviceInfo?: {
    [key: string]: any;
  };
  filePath?: string;
}

export interface WriteDeviceInfoResult {
  success: boolean;
  error?: string;
  filePath?: string;
  isNewFile?: boolean;  // 是否是新创建的文件
}

export interface UpdateLicenseResult {
  success: boolean;
  error?: string;
  license?: {
    license: string;
    status: string;
    // 其他可能的字段
    [key: string]: any;
  };
}

export interface toolsPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  
  /**
   * 检查网络是否实际可用
   * 
   * 该方法会：
   * 1. 检查网络连接状态
   * 2. 验证是否可以实际访问互联网
   * 3. 获取详细的网络信息
   * 
   * @param options.sites 要检查的网站列表，如果不提供则使用默认网站
   * @returns 网络状态检查结果
   * @example
   * // 使用默认网站列表
   * const result1 = await tools.checkNetworkStatus();
   * 
   * // 使用自定义网站列表
   * const result2 = await tools.checkNetworkStatus({
   *   sites: [
   *     'https://www.baidu.com',
   *     'https://www.qq.com',
   *     'https://www.taobao.com'
   *   ]
   * });
   * 
   * if (result1.success) {
   *   console.log('网络连接状态:', result1.isConnected);
   *   console.log('互联网可用:', result1.isInternetAvailable);
   *   console.log('网络类型:', result1.networkType);
   *   if (result1.siteStatus) {
   *     result1.siteStatus.forEach(site => {
   *       console.log(`${site.url}: ${site.isAvailable ? '可访问' : '不可访问'}`);
   *       if (site.responseTime) console.log(`响应时间: ${site.responseTime}ms`);
   *       if (!site.isAvailable) console.log(`错误: ${site.error}`);
   *     });
   *   }
   * }
   */
  checkNetworkStatus(options?: { sites?: string[] }): Promise<NetworkStatusResult>;
  
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

  checkWebViewInfo(): Promise<WebViewInfoResult>;

  /**
   * 检查设备硬件是否满足要求
   * @param options.minStorageSpace 最小存储空间要求（字节）
   * @param options.minMemory 最小内存要求（字节）
   * @param options.minCpuCores 最小CPU核心数
   * @param options.minCpuFrequency 最小CPU频率（Hz）
   * @param options.requiredSensors 必需的传感器类型列表
   * @returns Promise<HardwareCheckResult> 硬件检查结果
   */
  checkHardwareRequirements(options: {
    minStorageSpace: number;
    minMemory: number;
    minCpuCores: number;
    minCpuFrequency: number;
    requiredSensors: string[];
  }): Promise<HardwareCheckResult>;

  /**
   * 获取设备硬件信息
   * 包括存储空间、内存和传感器状态
   * @returns Promise<HardwareInfoResult> 硬件信息
   */
  getHardwareInfo(): Promise<HardwareInfoResult>;

  /**
   * 从TF卡中读取配置文件并获取所有配置信息
   * 
   * 配置文件格式要求：
   * - JSON格式
   * - 标准字段：
   *   - WiFiName: WiFi名称（必需）
   *   - WiFiPassword: WiFi密码（必需）
   *   - WiFiType: WiFi类型（可选，如WPA2/WPA3等）
   *   - AutoConnect: 是否自动连接（可选）
   *   - Timeout: 连接超时时间（可选，毫秒）
   *   - RetryCount: 重试次数（可选）
   *   - LastUpdated: 最后更新时间（可选）
   * - 支持自定义字段
   * 
   * @param options.fileName 配置文件名（相对于TF卡根目录）
   * @returns 包含所有配置信息的结果对象
   * @example
   * const result = await tools.getWifiNameFromConfig({
   *   fileName: 'config.json'
   * });
   * // 成功返回：
   * // {
   * //   "success": true,
   * //   "wifiName": "YourWiFiName",
   * //   "wifiPassword": "YourWiFiPassword",
   * //   "wifiType": "WPA2",
   * //   "autoConnect": true,
   * //   "timeout": 30000,
   * //   "retryCount": 3,
   * //   "lastUpdated": "2024-01-01T00:00:00Z",
   * //   "configFilePath": "/storage/sdcard1/config.json",
   * //   "config": {
   * //     "WiFiName": "YourWiFiName",
   * //     "WiFiPassword": "YourWiFiPassword",
   * //     "WiFiType": "WPA2",
   * //     "AutoConnect": true,
   * //     "Timeout": 30000,
   * //     "RetryCount": 3,
   * //     "LastUpdated": "2024-01-01T00:00:00Z",
   * //     "CustomField1": "自定义值1",
   * //     "CustomField2": "自定义值2"
   * //   }
   * // }
   * // 失败返回：
   * // {
   * //   "success": false,
   * //   "error": "错误信息"
   * // }
   */
  getWifiNameFromConfig(options: {
    fileName: string;
  }): Promise<WifiConfigResult>;

  /**
   * 读取 /vendor/device_info.json 文件内容
   * 
   * 该方法会尝试以下步骤：
   * 1. 直接读取 /vendor/device_info.json
   * 2. 如果无法直接读取，尝试将文件拷贝到应用可访问的位置
   * 3. 读取拷贝后的文件内容
   * 
   * @returns 包含设备信息的结果对象
   * @example
   * const result = await tools.getDeviceInfo();
   * if (result.success) {
   *   console.log('设备信息:', result.deviceInfo);
   * } else {
   *   console.error('读取失败:', result.error);
   * }
   */
  getDeviceInfo(): Promise<DeviceInfoResult>;

  /**
   * 在vendor目录下创建或更新device_info.json文件
   * 
   * 如果文件已存在，则只更新内容；如果不存在，则创建新文件。
   * 该方法需要root权限。
   * 
   * @param options.deviceInfo 要写入的设备信息对象
   * @returns 操作结果
   * @example
   * const result = await tools.writeDeviceInfo({
   *   deviceInfo: {
   *     model: "设备型号",
   *     serialNumber: "序列号",
   *     manufacturer: "制造商",
   *     // ... 其他设备信息
   *   }
   * });
   * if (result.success) {
   *   console.log('写入成功');
   *   console.log('文件路径:', result.filePath);
   *   console.log('是否新文件:', result.isNewFile);
   * } else {
   *   console.error('写入失败:', result.error);
   * }
   */
  writeDeviceInfo(options: {
    deviceInfo: {
      [key: string]: any;
    };
  }): Promise<WriteDeviceInfoResult>;

  /**
   * 修改SD卡中license.csv文件中指定license的状态
   * 
   * @param options.fileName CSV文件名（相对于TF卡根目录）
   * @param options.license 要修改的license
   * @param options.status 新的状态值
   * @returns 修改结果，成功时返回更新后的license信息
   * @example
   * const result = await tools.updateLicenseStatus({
   *   fileName: "license.csv",
   *   license: "XXXX-XXXX-XXXX-XXXX",
   *   status: "used"
   * });
   * if (result.success) {
   *   console.log('License信息:', result.license);
   * } else {
   *   console.error('修改失败:', result.error);
   * }
   */
  updateLicenseStatus(options: {
    fileName: string;
    license: string;
    status: string;
  }): Promise<UpdateLicenseResult>;

  /**
   * 获取设备MAC地址
   * 注意：在Android 10及以上版本，需要特定权限
   * @returns 包含MAC地址信息的对象
   */
  getDeviceMacAddress(): Promise<{
    success: boolean;
    macAddress?: string;
    androidVersion?: number;
    message?: string;
    requiredPermissions?: string[];
  }>;

  /**
   * 断开并清除当前Wi-Fi连接
   * 
   * 该方法会：
   * 1. 断开当前Wi-Fi连接
   * 2. 删除已保存的网络配置
   * 
   * 注意：
   * - 需要Wi-Fi和位置权限
   * - 删除配置后，再次连接需要重新输入密码
   * - Android 10及以上版本使用新的API实现
   * 
   * @returns 包含操作结果的Promise
   * @example
   * const result = await tools.disconnectAndForgetWifi();
   * if (result.success) {
   *   console.log('已断开并清除Wi-Fi连接');
   * } else {
   *   console.error('操作失败:', result.message);
   * }
   */
  disconnectAndForgetWifi(): Promise<{
    success: boolean;
    message?: string;
  }>;
}
