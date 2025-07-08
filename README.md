# WiFi配置文件说明

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
