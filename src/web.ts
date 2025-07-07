import { WebPlugin } from '@capacitor/core';

import type { 
  WifiNetwork, 
  WifiConnectionResult, 
  ExternalPortsStatus, 
  SDCardMonitoringResult,
  LicenseResult,
  AppSignatureResult,
  DeviceDateTimeResult,
  WebViewInfoResult,
  toolsPlugin
} from './definitions';

export class toolsWeb extends WebPlugin implements toolsPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
  
  async grantPermissions(options: {
    permissions: string[]
  }): Promise<{
    success: boolean;
    error?: string;
    results?: {
      [permission: string]: boolean;
    }
  }> {
    console.log('Web平台不支持直接授予Android权限', options);
    return {
      success: false,
      error: 'Web平台不支持此操作',
      results: options.permissions.reduce((acc, permission) => {
        acc[permission] = false;
        return acc;
      }, {} as { [key: string]: boolean })
    };
  }
  
  async checkPermissions(options: { 
    permissions: string[] 
  }): Promise<{ [permission: string]: boolean }> {
    console.log('Web平台不支持检查Android权限', options);
    return options.permissions.reduce((result, permission) => {
      result[permission] = false;
      return result;
    }, {} as { [permission: string]: boolean });
  }
  
  async checkWifiPermissions(): Promise<{ hasPermissions: boolean }> {
    console.log('Web平台不支持检查Wi-Fi权限');
    return { hasPermissions: false };
  }
  
  async requestWifiPermissions(): Promise<{
    granted: boolean;
    locationPermission: boolean;
    wifiPermission: boolean;
  }> {
    console.log('Web平台不支持请求Wi-Fi权限');
    return {
      granted: false,
      locationPermission: false,
      wifiPermission: false,
    };
  }
  
  async getWifiList(): Promise<{ wifiList: WifiNetwork[] }> {
    console.log('Web平台不支持获取Wi-Fi列表');
    return { wifiList: [] };
  }

  async connectToOpenWifi(options: { ssid: string }): Promise<WifiConnectionResult> {
    console.log('Web平台不支持连接到开放Wi-Fi网络', options);
    return {
      success: false,
      message: 'Web平台不支持Wi-Fi连接操作'
    };
  }

  async connectToPersonalWifi(options: { 
    ssid: string; 
    password: string 
  }): Promise<WifiConnectionResult> {
    console.log('Web平台不支持连接到个人Wi-Fi网络', options);
    return {
      success: false,
      message: 'Web平台不支持Wi-Fi连接操作'
    };
  }

  async connectToEnterpriseWifi(options: { 
    ssid: string; 
    password: string;
    identity: string;
  }): Promise<WifiConnectionResult> {
    console.log('Web平台不支持连接到企业Wi-Fi网络', options);
    return {
      success: false,
      message: 'Web平台不支持Wi-Fi连接操作'
    };
  }

  async checkExternalPorts(): Promise<ExternalPortsStatus> {
    console.log('Web平台不支持检测外接端口状态');
    return {
      success: false,
      error: 'Web平台不支持检测外接端口状态',
      usbPorts: [],
      typeC: {
        isAvailable: false,
        isCharging: false,
        isDataTransferEnabled: false
      },
      tfCard: {
        isAvailable: false,
        isMounted: false,
        state: 'unavailable'
      }
    };
  }

  async startMonitoringSDCard(): Promise<SDCardMonitoringResult> {
    throw this.unimplemented('在Web环境中不支持TF卡监听功能');
  }

  async stopMonitoringSDCard(): Promise<SDCardMonitoringResult> {
    throw this.unimplemented('在Web环境中不支持TF卡监听功能');
  }

  async getAvailableLicenseFromSD(_options: { fileName: string; }): Promise<LicenseResult> {
    throw this.unimplemented('在Web环境中不支持读取TF卡文件');
  }

  async checkAppSignature(): Promise<AppSignatureResult> {
    throw this.unimplemented('在Web平台上不支持此功能');
  }

  async checkDeviceDateTime(): Promise<DeviceDateTimeResult> {
    try {
      // 获取当前时间
      const now = new Date();
      const timestamp = now.getTime();
      
      // 格式化日期时间
      const currentDateTime = now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).replace(/\//g, '-');
      
      // 获取ISO 8601格式时间
      const iso8601DateTime = now.toISOString();
      
      // 获取时区信息
      const timeZoneId = Intl.DateTimeFormat().resolvedOptions().timeZone;
      const timeZoneName = new Intl.DateTimeFormat('zh-CN', {
        timeZoneName: 'long'
      }).formatToParts(now).find(part => part.type === 'timeZoneName')?.value || '';
      
      // 计算时区偏移量（小时）
      const timeZoneOffset = -now.getTimezoneOffset() / 60;
      
      // 检查是否为夏令时
      const isDaylightTime = now.getTimezoneOffset() < this.getStandardOffset(now);
      
      // Web平台的结果
      const result: DeviceDateTimeResult = {
        success: true,
        currentDateTime,
        iso8601DateTime,
        timestamp,
        unixTimestamp: Math.floor(timestamp / 1000),
        timeZoneId,
        timeZoneName,
        timeZoneOffset,
        isDaylightTime,
        is24HourFormat: true, // Web平台默认使用24小时制
        autoTimeEnabled: true, // Web平台时间总是自动的
        autoTimeZoneEnabled: true, // Web平台时区总是自动的
        isTimeAccurate: true, // Web平台时间总是准确的
        timeOffsetFromNTP: 0 // Web平台无法获取NTP偏差
      };
      
      return result;
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : '获取时间信息失败'
      };
    }
  }

  // 辅助方法：获取标准时区偏移（用于判断夏令时）
  private getStandardOffset(date: Date): number {
    const jan = new Date(date.getFullYear(), 0, 1);
    const jul = new Date(date.getFullYear(), 6, 1);
    return Math.max(jan.getTimezoneOffset(), jul.getTimezoneOffset());
  }

  async checkWebViewInfo(): Promise<WebViewInfoResult> {
    // Web平台获取浏览器信息
    try {
      const userAgent = navigator.userAgent;
      const isChrome = /Chrome/.test(userAgent);
      const chromeVersion = userAgent.match(/Chrome\/(\d+\.\d+\.\d+\.\d+)/);
      
      return {
        success: true,
        packageName: isChrome ? 'com.google.chrome' : 'unknown',
        versionName: chromeVersion ? chromeVersion[1] : 'unknown',
        settings: {
          userAgent: userAgent,
          javaScriptEnabled: true, // 浏览器中JavaScript总是启用的
          databaseEnabled: 'indexedDB' in window,
          domStorageEnabled: 'localStorage' in window,
          safeBrowsingEnabled: true // 现代浏览器默认启用安全浏览
        },
        isEnabled: true,
        androidVersion: 'web',
        androidSDK: 0
      };
    } catch (error) {
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error occurred'
      };
    }
  }
}
