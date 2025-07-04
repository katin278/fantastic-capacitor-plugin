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

</docgen-api>
