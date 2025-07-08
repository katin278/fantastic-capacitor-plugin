# Fantastic Capacitor Plugin 6

一个功能强大的Capacitor插件，提供了丰富的设备硬件检测和系统功能。

## 功能特性

- 📱 硬件信息检测

  - 存储空间（总量/可用/剩余）
  - 内存状态（总量/可用/是否低内存）
  - 传感器状态（支持多种传感器类型）
  - CPU信息（核心数/频率）

- 📶 Wi-Fi管理

  - 扫描周围Wi-Fi网络
  - 连接Wi-Fi（支持开放/个人/企业网络）
  - 获取当前连接状态

- 💾 外部存储

  - TF卡状态监控
  - 读取TF卡文件
  - 存储空间分析

- 🔒 安全性

  - 应用签名验证
  - 权限管理
  - WebView信息检查

- ⚡ 系统功能
  - 日期时间验证
  - 设备管理
  - 端口状态检测

## 安装

```bash
npm install fantastic-capacitor-plugin
npx cap sync
```

## API文档

### 1. 硬件信息检测

#### 获取硬件信息

```typescript
import { Plugins } from '@capacitor/core';
const { tools } = Plugins;

const hardwareInfo = await tools.getHardwareInfo();
console.log('存储空间:', hardwareInfo.storage.details);
console.log('内存状态:', hardwareInfo.memory.details);
console.log('可用传感器:', hardwareInfo.sensors.length);
```

#### 检查硬件要求

```typescript
const requirements = await tools.checkHardwareRequirements({
  minStorageSpace: 1024 * 1024 * 1024, // 1GB
  minMemory: 2 * 1024 * 1024 * 1024, // 2GB
  minCpuCores: 4,
  minCpuFrequency: 1.5 * 1000 * 1000 * 1000, // 1.5GHz
  requiredSensors: ['加速度传感器', '陀螺仪传感器'],
});
```

### 2. Wi-Fi管理

#### 扫描Wi-Fi

```typescript
const wifiList = await tools.getWifiList();
wifiList.forEach((wifi) => {
  console.log(`${wifi.ssid}: 信号强度 ${wifi.signalStrength}%`);
});
```

#### 连接Wi-Fi

```typescript
// 连接开放网络
await tools.connectToOpenWifi({ ssid: 'OpenWifi' });

// 连接个人网络
await tools.connectToPersonalWifi({
  ssid: 'HomeWifi',
  password: 'password123',
});

// 连接企业网络
await tools.connectToEnterpriseWifi({
  ssid: 'CompanyWifi',
  password: 'password123',
  identity: 'user@company.com',
});
```

### 3. 外部存储

#### 监控TF卡状态

```typescript
// 开始监听
await tools.startMonitoringSDCard();

// 添加状态变化监听器
tools.addListener('sdCardStateChanged', (state) => {
  console.log('TF卡状态:', state.state);
  console.log('可用空间:', state.availableSpace);
});

// 停止监听
await tools.stopMonitoringSDCard();
```

### 4. 安全性

#### 验证应用签名

```typescript
const signatureInfo = await tools.checkAppSignature();
if (signatureInfo.isOriginalSignature) {
  console.log('应用签名验证通过');
} else {
  console.warn('应用可能被篡改');
}
```

### 5. 系统功能

#### 检查设备时间

```typescript
const dateTimeInfo = await tools.checkDeviceDateTime();
console.log('系统时间:', dateTimeInfo.currentDateTime);
console.log('时区:', dateTimeInfo.timeZoneName);
```

## 平台支持

| 功能         | Android | iOS | Web |
| ------------ | ------- | --- | --- |
| 硬件信息检测 | ✅      | 🚧  | ⚠️  |
| Wi-Fi管理    | ✅      | 🚧  | ❌  |
| 外部存储     | ✅      | 🚧  | ⚠️  |
| 安全性       | ✅      | 🚧  | ⚠️  |
| 系统功能     | ✅      | 🚧  | ⚠️  |

✅ 完全支持
🚧 开发中
⚠️ 部分支持
❌ 不支持

## 注意事项

1. Android权限

   - 需要在AndroidManifest.xml中添加相应权限
   - 部分功能需要运行时权限

2. 存储空间

   - 容量单位统一使用GB
   - 精确到小数点后2位

3. 传感器检测

   - 不同设备支持的传感器类型可能不同
   - 建议先检查传感器可用性

4. Wi-Fi连接
   - Android 10及以上版本需要特殊处理
   - 企业网络连接可能需要额外配置

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

MIT License

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`grantPermissions(...)`](#grantpermissions)
* [`checkPermissions(...)`](#checkpermissions)
* [`checkWifiPermissions()`](#checkwifipermissions)
* [`requestWifiPermissions()`](#requestwifipermissions)
* [`getWifiList()`](#getwifilist)
* [`connectToOpenWifi(...)`](#connecttoopenwifi)
* [`connectToPersonalWifi(...)`](#connecttopersonalwifi)
* [`connectToEnterpriseWifi(...)`](#connecttoenterprisewifi)
* [`checkExternalPorts()`](#checkexternalports)
* [`startMonitoringSDCard()`](#startmonitoringsdcard)
* [`stopMonitoringSDCard()`](#stopmonitoringsdcard)
* [`addListener('sdCardStateChanged', ...)`](#addlistenersdcardstatechanged-)
* [`getAvailableLicenseFromSD(...)`](#getavailablelicensefromsd)
* [`checkAppSignature()`](#checkappsignature)
* [`checkDeviceDateTime()`](#checkdevicedatetime)
* [`checkWebViewInfo()`](#checkwebviewinfo)
* [`checkHardwareRequirements(...)`](#checkhardwarerequirements)
* [`getHardwareInfo()`](#gethardwareinfo)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### grantPermissions(...)

```typescript
grantPermissions(options: { permissions: string[]; }) => Promise<{ success: boolean; error?: string | undefined; results?: { [permission: string]: boolean; } | undefined; }>
```

通过DevicePolicyManager直接授予权限

| Param         | Type                                    |
| ------------- | --------------------------------------- |
| **`options`** | <code>{ permissions: string[]; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; error?: string; results?: { [permission: string]: boolean; }; }&gt;</code>

--------------------


### checkPermissions(...)

```typescript
checkPermissions(options: { permissions: string[]; }) => Promise<{ [permission: string]: boolean; }>
```

检查指定的Android权限是否已被授予

| Param         | Type                                    |
| ------------- | --------------------------------------- |
| **`options`** | <code>{ permissions: string[]; }</code> |

**Returns:** <code>Promise&lt;{ [permission: string]: boolean; }&gt;</code>

--------------------


### checkWifiPermissions()

```typescript
checkWifiPermissions() => Promise<{ hasPermissions: boolean; }>
```

检查是否具有Wi-Fi相关权限

**Returns:** <code>Promise&lt;{ hasPermissions: boolean; }&gt;</code>

--------------------


### requestWifiPermissions()

```typescript
requestWifiPermissions() => Promise<{ granted: boolean; locationPermission: boolean; wifiPermission: boolean; }>
```

请求Wi-Fi相关权限

**Returns:** <code>Promise&lt;{ granted: boolean; locationPermission: boolean; wifiPermission: boolean; }&gt;</code>

--------------------


### getWifiList()

```typescript
getWifiList() => Promise<{ wifiList: WifiNetwork[]; }>
```

获取附近Wi-Fi网络详细信息列表

返回每个Wi-Fi的SSID、BSSID、安全协议类型、信号强度和是否为企业级Wi-Fi等信息

**Returns:** <code>Promise&lt;{ wifiList: WifiNetwork[]; }&gt;</code>

--------------------


### connectToOpenWifi(...)

```typescript
connectToOpenWifi(options: { ssid: string; }) => Promise<WifiConnectionResult>
```

连接到开放Wi-Fi网络（无密码）

| Param         | Type                           |
| ------------- | ------------------------------ |
| **`options`** | <code>{ ssid: string; }</code> |

**Returns:** <code>Promise&lt;<a href="#wificonnectionresult">WifiConnectionResult</a>&gt;</code>

--------------------


### connectToPersonalWifi(...)

```typescript
connectToPersonalWifi(options: { ssid: string; password: string; }) => Promise<WifiConnectionResult>
```

连接到个人Wi-Fi网络（需要密码）

| Param         | Type                                             |
| ------------- | ------------------------------------------------ |
| **`options`** | <code>{ ssid: string; password: string; }</code> |

**Returns:** <code>Promise&lt;<a href="#wificonnectionresult">WifiConnectionResult</a>&gt;</code>

--------------------


### connectToEnterpriseWifi(...)

```typescript
connectToEnterpriseWifi(options: { ssid: string; password: string; identity: string; }) => Promise<WifiConnectionResult>
```

连接到企业Wi-Fi网络（需要密码和身份认证）

| Param         | Type                                                               |
| ------------- | ------------------------------------------------------------------ |
| **`options`** | <code>{ ssid: string; password: string; identity: string; }</code> |

**Returns:** <code>Promise&lt;<a href="#wificonnectionresult">WifiConnectionResult</a>&gt;</code>

--------------------


### checkExternalPorts()

```typescript
checkExternalPorts() => Promise<ExternalPortsStatus>
```

检测设备外接端口状态

检测设备的USB端口（包括Type-C）和TF卡槽的状态，返回：
- USB端口列表及其详细信息
- Type-C端口状态（是否可用、是否充电、是否支持数据传输）
- TF卡槽状态（是否可用、是否已挂载、存储空间信息）

**Returns:** <code>Promise&lt;<a href="#externalportsstatus">ExternalPortsStatus</a>&gt;</code>

--------------------


### startMonitoringSDCard()

```typescript
startMonitoringSDCard() => Promise<SDCardMonitoringResult>
```

开始监听TF卡槽状态变化

**Returns:** <code>Promise&lt;<a href="#sdcardmonitoringresult">SDCardMonitoringResult</a>&gt;</code>

--------------------


### stopMonitoringSDCard()

```typescript
stopMonitoringSDCard() => Promise<SDCardMonitoringResult>
```

停止监听TF卡槽状态

**Returns:** <code>Promise&lt;<a href="#sdcardmonitoringresult">SDCardMonitoringResult</a>&gt;</code>

--------------------


### addListener('sdCardStateChanged', ...)

```typescript
addListener(eventName: 'sdCardStateChanged', callback: (state: SDCardState) => void) => Promise<PluginListenerHandle>
```

添加TF卡状态变化的监听器

| Param           | Type                                                                    | Description               |
| --------------- | ----------------------------------------------------------------------- | ------------------------- |
| **`eventName`** | <code>'sdCardStateChanged'</code>                                       | 事件名称 'sdCardStateChanged' |
| **`callback`**  | <code>(state: <a href="#sdcardstate">SDCardState</a>) =&gt; void</code> | 回调函数，接收状态变化信息             |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### getAvailableLicenseFromSD(...)

```typescript
getAvailableLicenseFromSD(options: { fileName: string; }) => Promise<LicenseResult>
```

从TF卡中读取CSV文件并获取第一个可用的license

CSV文件格式要求：
- 第一列：license
- 第二列：status（为空表示可用）

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ fileName: string; }</code> |

**Returns:** <code>Promise&lt;<a href="#licenseresult">LicenseResult</a>&gt;</code>

--------------------


### checkAppSignature()

```typescript
checkAppSignature() => Promise<AppSignatureResult>
```

检查应用是否被重新签名

用于检查应用是否被重新签名，可以用来验证应用的完整性。
返回签名的详细信息，包括MD5、SHA-1、SHA-256等多种格式的签名值，
以及证书的详细信息（发行者、有效期等）。

**Returns:** <code>Promise&lt;<a href="#appsignatureresult">AppSignatureResult</a>&gt;</code>

--------------------


### checkDeviceDateTime()

```typescript
checkDeviceDateTime() => Promise<DeviceDateTimeResult>
```

验证设备日期和时间

**Returns:** <code>Promise&lt;<a href="#devicedatetimeresult">DeviceDateTimeResult</a>&gt;</code>

--------------------


### checkWebViewInfo()

```typescript
checkWebViewInfo() => Promise<WebViewInfoResult>
```

**Returns:** <code>Promise&lt;<a href="#webviewinforesult">WebViewInfoResult</a>&gt;</code>

--------------------


### checkHardwareRequirements(...)

```typescript
checkHardwareRequirements(options: { minStorageSpace: number; minMemory: number; minCpuCores: number; minCpuFrequency: number; requiredSensors: string[]; }) => Promise<HardwareCheckResult>
```

检查设备硬件是否满足要求

| Param         | Type                                                                                                                                  |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ minStorageSpace: number; minMemory: number; minCpuCores: number; minCpuFrequency: number; requiredSensors: string[]; }</code> |

**Returns:** <code>Promise&lt;<a href="#hardwarecheckresult">HardwareCheckResult</a>&gt;</code>

--------------------


### getHardwareInfo()

```typescript
getHardwareInfo() => Promise<HardwareInfoResult>
```

获取设备硬件信息
包括存储空间、内存和传感器状态

**Returns:** <code>Promise&lt;<a href="#hardwareinforesult">HardwareInfoResult</a>&gt;</code>

--------------------


### Interfaces


#### WifiNetwork

| Prop                 | Type                 |
| -------------------- | -------------------- |
| **`ssid`**           | <code>string</code>  |
| **`bssid`**          | <code>string</code>  |
| **`level`**          | <code>number</code>  |
| **`signalStrength`** | <code>number</code>  |
| **`frequency`**      | <code>number</code>  |
| **`isConnected`**    | <code>boolean</code> |
| **`securityType`**   | <code>string</code>  |
| **`isEnterprise`**   | <code>boolean</code> |


#### WifiConnectionResult

| Prop          | Type                 |
| ------------- | -------------------- |
| **`success`** | <code>boolean</code> |
| **`message`** | <code>string</code>  |


#### ExternalPortsStatus

| Prop           | Type                                                    |
| -------------- | ------------------------------------------------------- |
| **`success`**  | <code>boolean</code>                                    |
| **`error`**    | <code>string</code>                                     |
| **`usbPorts`** | <code>UsbPortInfo[]</code>                              |
| **`typeC`**    | <code><a href="#typecportinfo">TypeCPortInfo</a></code> |
| **`tfCard`**   | <code><a href="#tfcardinfo">TFCardInfo</a></code>       |


#### UsbPortInfo

| Prop                   | Type                 |
| ---------------------- | -------------------- |
| **`deviceId`**         | <code>number</code>  |
| **`deviceName`**       | <code>string</code>  |
| **`manufacturerName`** | <code>string</code>  |
| **`productName`**      | <code>string</code>  |
| **`interfaceCount`**   | <code>number</code>  |
| **`vendorId`**         | <code>number</code>  |
| **`productId`**        | <code>number</code>  |
| **`deviceClass`**      | <code>string</code>  |
| **`isConnected`**      | <code>boolean</code> |


#### TypeCPortInfo

| Prop                        | Type                 |
| --------------------------- | -------------------- |
| **`isAvailable`**           | <code>boolean</code> |
| **`isCharging`**            | <code>boolean</code> |
| **`isDataTransferEnabled`** | <code>boolean</code> |
| **`error`**                 | <code>string</code>  |


#### TFCardInfo

| Prop                 | Type                 |
| -------------------- | -------------------- |
| **`isAvailable`**    | <code>boolean</code> |
| **`isMounted`**      | <code>boolean</code> |
| **`state`**          | <code>string</code>  |
| **`totalSpace`**     | <code>number</code>  |
| **`availableSpace`** | <code>number</code>  |


#### SDCardMonitoringResult

| Prop          | Type                 |
| ------------- | -------------------- |
| **`success`** | <code>boolean</code> |
| **`error`**   | <code>string</code>  |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### SDCardState

| Prop                  | Type                                                                         |
| --------------------- | ---------------------------------------------------------------------------- |
| **`event`**           | <code>'mounted' \| 'unmounted' \| 'removed' \| 'shared' \| 'checking'</code> |
| **`path`**            | <code>string</code>                                                          |
| **`isAvailable`**     | <code>boolean</code>                                                         |
| **`hasCardInserted`** | <code>boolean</code>                                                         |
| **`isMounted`**       | <code>boolean</code>                                                         |
| **`state`**           | <code>string</code>                                                          |
| **`totalSpace`**      | <code>number</code>                                                          |
| **`availableSpace`**  | <code>number</code>                                                          |


#### LicenseResult

| Prop          | Type                 |
| ------------- | -------------------- |
| **`success`** | <code>boolean</code> |
| **`license`** | <code>string</code>  |
| **`error`**   | <code>string</code>  |


#### AppSignatureResult

| Prop                      | Type                                                          |
| ------------------------- | ------------------------------------------------------------- |
| **`success`**             | <code>boolean</code>                                          |
| **`packageName`**         | <code>string</code>                                           |
| **`currentSignature`**    | <code>string</code>                                           |
| **`isOriginalSignature`** | <code>boolean</code>                                          |
| **`signatureDetails`**    | <code><a href="#signaturedetails">SignatureDetails</a></code> |
| **`error`**               | <code>string</code>                                           |


#### SignatureDetails

| Prop                   | Type                |
| ---------------------- | ------------------- |
| **`md5`**              | <code>string</code> |
| **`sha1`**             | <code>string</code> |
| **`sha256`**           | <code>string</code> |
| **`md5_formatted`**    | <code>string</code> |
| **`sha1_formatted`**   | <code>string</code> |
| **`sha256_formatted`** | <code>string</code> |
| **`issuer`**           | <code>string</code> |
| **`subject`**          | <code>string</code> |
| **`serialNumber`**     | <code>string</code> |
| **`validFrom`**        | <code>string</code> |
| **`validUntil`**       | <code>string</code> |


#### DeviceDateTimeResult

| Prop                      | Type                 | Description                                                                     |
| ------------------------- | -------------------- | ------------------------------------------------------------------------------- |
| **`success`**             | <code>boolean</code> | 操作是否成功                                                                          |
| **`error`**               | <code>string</code>  | 如果操作失败，包含错误信息                                                                   |
| **`currentDateTime`**     | <code>string</code>  | 当前日期时间，格式：YYYY-MM-DD HH:mm:ss 例如：2024-03-15 14:30:45                            |
| **`iso8601DateTime`**     | <code>string</code>  | ISO 8601格式的日期时间，包含时区信息 例如：2024-03-15T14:30:45.123+08:00                         |
| **`timestamp`**           | <code>number</code>  | 当前时间戳（毫秒） 从1970年1月1日UTC零点开始的毫秒数                                                 |
| **`unixTimestamp`**       | <code>number</code>  | Unix时间戳（秒） 从1970年1月1日UTC零点开始的秒数                                                 |
| **`timeZoneId`**          | <code>string</code>  | 时区ID 例如：'Asia/Shanghai', 'America/New_York'                                     |
| **`timeZoneName`**        | <code>string</code>  | 时区名称，本地化显示 例如：'中国标准时间', '美国东部时间'                                                |
| **`timeZoneOffset`**      | <code>number</code>  | 时区偏移量（小时） 正数表示超前UTC，负数表示落后UTC 例如：+8.0表示北京时间，-5.0表示纽约时间                          |
| **`isDaylightTime`**      | <code>boolean</code> | 是否处于夏令时 true: 当前处于夏令时 false: 当前处于标准时间                                           |
| **`is24HourFormat`**      | <code>boolean</code> | 系统是否使用24小时制 true: 使用24小时制（例如：14:30） false: 使用12小时制（例如：2:30 PM）                  |
| **`autoTimeEnabled`**     | <code>boolean</code> | 系统是否启用了自动时间设置 true: 系统会自动从网络获取和更新时间 false: 用户手动设置时间                             |
| **`autoTimeZoneEnabled`** | <code>boolean</code> | 系统是否启用了自动时区设置 true: 系统会根据位置自动设置时区 false: 用户手动设置时区                               |
| **`isTimeAccurate`**      | <code>boolean</code> | 设备时间是否准确 true: 时间被认为是准确的 false: 时间可能不准确 Android: 基于自动时间设置状态 Web: 总是返回true       |
| **`timeOffsetFromNTP`**   | <code>number</code>  | 与网络时间服务器的时间偏差（毫秒） 0: 表示时间准确或无法获取偏差 正数: 表示设备时间快于标准时间 负数: 表示设备时间慢于标准时间 Web平台始终返回0 |


#### WebViewInfoResult

| Prop                   | Type                                                        |
| ---------------------- | ----------------------------------------------------------- |
| **`success`**          | <code>boolean</code>                                        |
| **`error`**            | <code>string</code>                                         |
| **`packageName`**      | <code>string</code>                                         |
| **`versionName`**      | <code>string</code>                                         |
| **`versionCode`**      | <code>number</code>                                         |
| **`firstInstallTime`** | <code>number</code>                                         |
| **`lastUpdateTime`**   | <code>number</code>                                         |
| **`settings`**         | <code><a href="#webviewsettings">WebViewSettings</a></code> |
| **`androidVersion`**   | <code>string</code>                                         |
| **`androidSDK`**       | <code>number</code>                                         |
| **`isEnabled`**        | <code>boolean</code>                                        |
| **`dataDirectory`**    | <code>string</code>                                         |


#### WebViewSettings

| Prop                      | Type                 |
| ------------------------- | -------------------- |
| **`userAgent`**           | <code>string</code>  |
| **`javaScriptEnabled`**   | <code>boolean</code> |
| **`databaseEnabled`**     | <code>boolean</code> |
| **`domStorageEnabled`**   | <code>boolean</code> |
| **`safeBrowsingEnabled`** | <code>boolean</code> |


#### HardwareCheckResult

| Prop          | Type                                                                                                                                                                                       |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`success`** | <code>boolean</code>                                                                                                                                                                       |
| **`error`**   | <code>string</code>                                                                                                                                                                        |
| **`storage`** | <code>{ passed: boolean; available: number; required: number; details: string; }</code>                                                                                                    |
| **`memory`**  | <code>{ passed: boolean; available: number; required: number; details: string; }</code>                                                                                                    |
| **`cpu`**     | <code>{ passed: boolean; cores: { available: number; required: number; passed: boolean; }; frequency: { available: number; required: number; passed: boolean; }; details: string; }</code> |
| **`sensors`** | <code>{ passed: boolean; available: string[]; required: string[]; missing: string[]; details: string; }</code>                                                                             |


#### HardwareInfoResult

| Prop          | Type                                                |
| ------------- | --------------------------------------------------- |
| **`success`** | <code>boolean</code>                                |
| **`error`**   | <code>string</code>                                 |
| **`storage`** | <code><a href="#storageinfo">StorageInfo</a></code> |
| **`memory`**  | <code><a href="#memoryinfo">MemoryInfo</a></code>   |
| **`cpu`**     | <code><a href="#cpuinfo">CpuInfo</a></code>         |
| **`sensors`** | <code>SensorInfo[]</code>                           |


#### StorageInfo

| Prop                 | Type                 |
| -------------------- | -------------------- |
| **`totalSpace`**     | <code>number</code>  |
| **`availableSpace`** | <code>number</code>  |
| **`freeSpace`**      | <code>number</code>  |
| **`details`**        | <code>string</code>  |
| **`isHealthy`**      | <code>boolean</code> |
| **`healthDetails`**  | <code>string</code>  |


#### MemoryInfo

| Prop                  | Type                 |
| --------------------- | -------------------- |
| **`totalMemory`**     | <code>number</code>  |
| **`availableMemory`** | <code>number</code>  |
| **`lowMemory`**       | <code>boolean</code> |
| **`details`**         | <code>string</code>  |
| **`isHealthy`**       | <code>boolean</code> |
| **`healthDetails`**   | <code>string</code>  |


#### CpuInfo

| Prop              | Type                 |
| ----------------- | -------------------- |
| **`cores`**       | <code>number</code>  |
| **`frequency`**   | <code>number</code>  |
| **`isHealthy`**   | <code>boolean</code> |
| **`temperature`** | <code>number</code>  |
| **`usage`**       | <code>number</code>  |
| **`details`**     | <code>string</code>  |


#### SensorInfo

| Prop            | Type                 |
| --------------- | -------------------- |
| **`name`**      | <code>string</code>  |
| **`type`**      | <code>string</code>  |
| **`vendor`**    | <code>string</code>  |
| **`isWorking`** | <code>boolean</code> |
| **`details`**   | <code>string</code>  |

</docgen-api>

## 检查应用签名

用于检查应用是否被重新签名，可以用来验证应用的完整性。

### 使用方法

```typescript
import { tools } from 'fantastic-capacitor-plugin';

async function checkAppSignature() {
  try {
    const result = await tools.checkAppSignature();
    console.log('应用包名:', result.packageName);
    console.log('当前签名:', result.currentSignature);
    console.log('是否为原始签名:', result.isOriginalSignature);

    if (result.signatureDetails) {
      console.log('MD5:', result.signatureDetails.md5);
      console.log('SHA-1:', result.signatureDetails.sha1);
      console.log('SHA-256:', result.signatureDetails.sha256);
      console.log('证书发行者:', result.signatureDetails.issuer);
      console.log('证书有效期:', result.signatureDetails.validFrom, '至', result.signatureDetails.validUntil);
    }
  } catch (error) {
    console.error('检查签名失败:', error);
  }
}
```

### 返回值类型

```typescript
interface AppSignatureResult {
  success: boolean;
  packageName: string;
  currentSignature: string;
  isOriginalSignature: boolean;
  signatureDetails?: {
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
  };
  error?: string;
}
```

### 注意事项

1. 该功能仅在 Android 平台可用
2. 需要在 `tools.java` 中设置正确的原始签名值
3. 签名值提供两种格式：
   - 原始十六进制字符串（例如：80abf06c4d842440dc...）
   - 冒号分隔格式（例如：80:AB:F0:6C:4D:84...）

### 验证设备日期和时间

```typescript
checkDeviceDateTime(): Promise<DeviceDateTimeResult>
```

验证设备的日期和时间设置，包括：

- 当前时间（多种格式）
- 时区信息
- 系统时间设置
- NTP时间验证

**返回值示例：**

```typescript
{
  success: true,
  currentDateTime: "2024-03-15 14:30:45",
  iso8601DateTime: "2024-03-15T14:30:45.123+08:00",
  timestamp: 1710486645123,
  unixTimestamp: 1710486645,
  timeZoneId: "Asia/Shanghai",
  timeZoneName: "中国标准时间",
  timeZoneOffset: 8.0,
  isDaylightTime: false,
  is24HourFormat: true,
  autoTimeEnabled: true,
  autoTimeZoneEnabled: true,
  isTimeAccurate: true,
  timeOffsetFromNTP: 123
}
```

**使用示例：**

```typescript
import { Plugins } from '@capacitor/core';
const { tools } = Plugins;

async function checkDeviceTime() {
  try {
    const result = await tools.checkDeviceDateTime();
    if (result.success) {
      console.log('当前时间：', result.currentDateTime);
      console.log('时区：', result.timeZoneName);
      console.log('时间是否准确：', result.isTimeAccurate);
    } else {
      console.error('验证失败：', result.error);
    }
  } catch (error) {
    console.error('验证出错：', error);
  }
}
```

**返回值说明：**

- `success`: 是否成功获取时间信息
- `currentDateTime`: 当前日期时间（标准格式）
- `iso8601DateTime`: ISO 8601格式的日期时间
- `timestamp`: 时间戳（毫秒）
- `unixTimestamp`: Unix时间戳（秒）
- `timeZoneId`: 时区ID
- `timeZoneName`: 时区名称
- `timeZoneOffset`: 时区偏移量（小时）
- `isDaylightTime`: 是否处于夏令时
- `is24HourFormat`: 是否使用24小时制
- `autoTimeEnabled`: 是否启用自动时间设置
- `autoTimeZoneEnabled`: 是否启用自动时区设置
- `isTimeAccurate`: 时间是否准确（与NTP服务器对比）
- `timeOffsetFromNTP`: 与NTP服务器的时间偏差（毫秒）

**注意事项：**

1. NTP时间验证需要网络连接
2. 时间偏差在30秒内被认为是准确的
3. 某些设备可能禁用了自动时间设置功能
