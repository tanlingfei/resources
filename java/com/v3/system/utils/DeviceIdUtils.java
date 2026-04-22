package com.v3.system.utils;

import com.v3.common.utils.BeanUtil;
import com.v3.common.utils.OssProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DeviceIdUtils {
    private static final String KEY_PREFIX = "ip:record:";
    private static final long EXPIRE_MINUTES = 10;
    private static final int TOLERANCE_SECONDS = 2; // 误差容错秒

    /**
     * 生成设备唯一标识（同一手机换浏览器基本不变）
     */
    public static String getDeviceId(HttpServletRequest request, String screenSign) {
        return md5(getOrDeviceId(request, screenSign));
    }

    private static String getOrDeviceId(HttpServletRequest request, String screenSign) {
        // 真实客户端IP（兼容Nginx代理）
        String ip = getClientRealIp(request);

        // 设备硬件信息（屏蔽浏览器差异）
        String deviceInfo = StringUtils.hasText(request.getHeader("User-Agent")) ? request.getHeader("User-Agent") : "";
        /*     String deviceInfo = extractDeviceInfo(ua);*/

        // 屏幕特征（区分同款手机）
        screenSign = StringUtils.hasText(screenSign) ? screenSign : "default-screen";

        // 拼接
        String raw = ip + "|" + deviceInfo + "|" + screenSign;
        System.out.println("设备信息===" + raw);
        return raw;
    }

    /**
     * 提取设备信息：系统+版本
     */
    private static String extractDeviceInfo(String ua) {
        StringBuilder sb = new StringBuilder();
        // 将ua转换为小写
        String lowerUa = ua.toLowerCase();

        // 系统类型
        if (lowerUa.contains("iphone")) {
            sb.append("iOS");
        } else if (lowerUa.contains("android")) {
            sb.append("Android");
        } else if (lowerUa.contains("windows")) {
            sb.append("Windows");
        } else if (lowerUa.contains("macintosh")) {
            sb.append("MacOS");
        } else if (lowerUa.contains("linux")) {
            sb.append("Linux");
        } else {
            sb.append("UnknownOS");
        }

        // Android 版本（使用正则表达式匹配）
        java.util.regex.Pattern androidPattern = java.util.regex.Pattern.compile("android\\s*([0-9]+(\\.[0-9]+)?)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher androidMatcher = androidPattern.matcher(ua);
        if (androidMatcher.find()) {
            String version = androidMatcher.group(1);
            sb.append("-").append(version);
        }

        // iOS 版本（使用正则表达式匹配）
        java.util.regex.Pattern iosPattern = java.util.regex.Pattern.compile("ios\\s*([0-9]+(\\.[0-9]+)?)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher iosMatcher = iosPattern.matcher(ua);
        if (iosMatcher.find()) {
            String version = iosMatcher.group(1);
            sb.append("-").append(version);
        }

        return sb.length() > 0 ? sb.toString() : "unknown-device";
    }

    /**
     * 获取真实IP，兼容Nginx代理
     */
    public static String getClientRealIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip == null ? "" : ip;
    }

    /**
     * MD5
     */
    public static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * 生成设备ID并保存到本地文件
     */
    public static String generateAndSaveDeviceId(HttpServletRequest request, String visId, String dir) {
        // 获取当前时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = dateFormat.format(new Date());

        // 生成设备ID
        String deviceId = getOrDeviceId(request, currentTime + "|" + visId);

        // 保存到本地文件
        saveDeviceIdToFile(deviceId, dir);

        return deviceId;
    }

    /**
     * 保存设备ID到本地文件
     */
    private static void saveDeviceIdToFile(String deviceId, String dir) {
        try {
            // 获取OssProperties实例
            OssProperties ossProperties = (OssProperties) BeanUtil.getBean("ossProperties");
            if (ossProperties == null) {
                System.err.println("OssProperties not found");
                return;
            }

            // 构建保存路径
            String locPath = ossProperties.getLocPath();
            if (StringUtils.isEmpty(locPath)) {
                System.err.println("locPath not configured");
                return;
            }

            String logDirPath = locPath + "/" + dir;
            File logDir = new File(logDirPath);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // 构建文件名
            SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");
            String fileName = "log-" + fileDateFormat.format(new Date()) + ".txt";
            File logFile = new File(logDir, fileName);

            // 追加记录到文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(deviceId);
                writer.newLine();
            }

            /*System.out.println("Device ID saved to file: " + logFile.getAbsolutePath());*/
        } catch (IOException e) {
            System.err.println("Failed to save device ID to file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error saving device ID: " + e.getMessage());
        }
    }

    // ====================== 保存 ======================
    public static void save(RedisTemplate<String, String> redisTemplate, HttpServletRequest request, String data, String deviceId) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        if (deviceId != null && !deviceId.equals("13621215240")) {
            String key = "clipboard:" + deviceId;
            System.out.println("存储口令信息唯一key=" + key);
            ops.set(key, data, EXPIRE_MINUTES, TimeUnit.MINUTES);
        } else {
            long createTime = System.currentTimeMillis();
            String key = "clipboard:record:" + createTime;
            String zsetKey = "clipboard:zset";

            System.out.println("存储口令信息通用key=" + key);

            ops.set(key, data, EXPIRE_MINUTES, TimeUnit.MINUTES);

            redisTemplate.opsForZSet().add(zsetKey, key, createTime);
            redisTemplate.expire(zsetKey, EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }

    // ====================== 取出最近保存的数据 ======================
    public static String getMyData(RedisTemplate<String, String> redisTemplate, HttpServletRequest request) {
        String zsetKey = "clipboard:zset";
      /*  System.out.println("获取口令信息通用key=" + zsetKey);*/
        Set<String> keys = redisTemplate.opsForZSet().reverseRange(zsetKey, 0, 0);

        if (keys != null && !keys.isEmpty()) {
            String latestKey = keys.iterator().next();
            return redisTemplate.opsForValue().get(latestKey);
        }

        return null;
    }

    // ====================== 取出【根据deviceId】 ======================
    public static String getMyDataByDeviceId(RedisTemplate<String, String> redisTemplate, String deviceId) {
            String key = "clipboard:" + deviceId;
            System.out.println("获取口令信息唯一key=" + key);
            return redisTemplate.opsForValue().get(key);
    }
}
