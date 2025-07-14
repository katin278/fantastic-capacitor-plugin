# Fantastic Capacitor Plugin

## 配置文件格式

配置文件应为JSON格式，包含以下字段：

```json
{
  "WiFiName": "您的WiFi名称",
  "WiFiPassword": "WiFi密码",
  "WiFiType": "WPA2",
  "AutoConnect": true,
  "Timeout": 30000,
  "RetryCount": 3,
  "LastUpdated": "2024-01-01T00:00:00Z"
}
```

## 字段说明

- `WiFiName`: WiFi网络的SSID（必填）
- `WiFiPassword`: WiFi密码（可选，开放网络可不填）
- `WiFiType`: WiFi加密类型（可选，默认WPA2）
- `AutoConnect`: 是否自动连接（可选，默认true）
- `Timeout`: 连接超时时间（可选，单位毫秒，默认30000）
- `RetryCount`: 重试次数（可选，默认3）
- `LastUpdated`: 最后更新时间（可选）

## 使用方法

1. 将配置文件复制到TF卡根目录或以下子目录之一：

   - /Download/
   - /Documents/
   - /Android/data/[应用包名]/files/

2. 在代码中调用：

```java
JSONObject result = tools.getWifiNameFromConfig(context, "config.json");
if (result.optBoolean("success", false)) {
    String wifiName = result.getString("wifiName");
    // 使用获取到的WiFiName
} else {
    String error = result.optString("error", "未知错误");
    // 处理错误情况
}
```

## 错误处理

该API会返回详细的错误信息，包括：

- 文件权限问题
- 文件不存在
- JSON格式错误
- 必要字段缺失
- TF卡访问错误

## 调试信息

成功时返回的JSON中包含：

- `success`: true
- `wifiName`: 获取到的WiFi名称
- `configFilePath`: 配置文件的完整路径

失败时返回的JSON中包含：

- `success`: false
- `error`: 详细的错误信息

## API

### getDeviceMacAddress

获取设备的MAC地址。

**注意：** 在Android 10及以上版本，需要以下权限：

- `ACCESS_FINE_LOCATION`
- `ACCESS_WIFI_STATE`
- `LOCAL_MAC_ADDRESS`（部分设备可能需要）

```typescript
import { tools } from 'fantastic-capacitor-plugin';

const getMacAddress = async () => {
  try {
    const result = await tools.getDeviceMacAddress();
    if (result.success) {
      console.log('MAC地址:', result.macAddress);
      console.log('Android版本:', result.androidVersion);
    } else {
      console.error('获取MAC地址失败:', result.message);
      if (result.requiredPermissions) {
        console.log('需要的权限:', result.requiredPermissions);
      }
    }
  } catch (error) {
    console.error('发生错误:', error);
  }
};
```

**返回值：**

```typescript
{
  success: boolean;        // 是否成功
  macAddress?: string;     // MAC地址（如果成功）
  androidVersion?: number; // Android系统版本
  message?: string;        // 错误信息（如果失败）
  requiredPermissions?: string[]; // 缺少的权限列表（如果需要）
}
```

**注意事项：**

1. 在Android 10及以上版本，由于系统限制，可能会返回默认MAC地址"02:00:00:00:00:00"
2. 使用前请确保已经获取了必要的权限
3. 建议在应用启动时检查并请求所需权限
