# fantastic-capacitor-plugin

a fantastic capacitor plugin

## Install

```bash
npm install fantastic-capacitor-plugin
npx cap sync
```

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
