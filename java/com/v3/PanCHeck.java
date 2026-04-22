package com.v3;
/*import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;*/

import java.util.concurrent.TimeUnit;

/**
 * 夸克网盘链接检测程序
 * 完全兼容 JDK 1.8
 */
public class PanCHeck {

    // WebDriver实例（复用同一个，避免频繁启动关闭）
  /*  private static WebDriver driver = null;*/

    /**
     * 初始化WebDriver（只执行一次）
     */
   /* private static void initDriver() {
        if (driver == null) {
            System.out.println("正在初始化Chrome驱动...");

            // 使用 useMirror() 自动选择国内镜像
            WebDriverManager.chromedriver()
                    .useMirror()              // 自动使用国内镜像加速
                    .cachePath("./chromedriver_cache") // 缓存到项目目录
                    .setup();

            // Chrome配置
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");              // 无头模式
            options.addArguments("--disable-gpu");           // 禁用GPU加速
            options.addArguments("--no-sandbox");            // 禁用沙箱
            options.addArguments("--disable-dev-shm-usage"); // 解决资源限制问题
            options.addArguments("--window-size=1920,1080"); // 设置窗口大小
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            // 启动浏览器
            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

            System.out.println("Chrome驱动初始化完成");
        }
    }*/

    /**
     * 关闭WebDriver（程序结束时调用）
     */
 /*   public static void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("关闭驱动时发生异常: " + e.getMessage());
            } finally {
                driver = null;
                System.out.println("Chrome驱动已关闭");
            }
        }
    }*/

    /**
     * 检测夸克链接状态
     * @param quarkLink 夸克分享链接
     * @return 检测结果
     */
/*    public static String checkQuarkLinkStatus(String quarkLink) {
        // 确保驱动已初始化
        initDriver();

        // 清理链接（去掉?后面的参数）
        String cleanUrl = quarkLink.split("\\?")[0];

        try {
            System.out.println("正在检测: " + cleanUrl);

            // 访问链接
            driver.get(cleanUrl);

            // 【JDK 8 兼容】使用整数秒，不用 Duration
            WebDriverWait wait = new WebDriverWait(driver, 15);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            // 给JS一点渲染时间
            Thread.sleep(2000);

            // 获取页面文本
            WebElement body = driver.findElement(By.tagName("body"));
            String pageText = body.getText();
            String pageHtml = driver.getPageSource().toLowerCase();

            // 精确判断页面状态
            if (pageText.contains("文件已被分享者删除") ||
                    pageText.contains("分享已被取消") ||
                    pageText.contains("已删除")) {
                return "链接失效：文件已被分享者删除";
            }

            if (pageText.contains("分享内容违规") ||
                    pageText.contains("已被和谐") ||
                    pageText.contains("违规内容")) {
                return "链接失效：内容违规已被删除";
            }

            if (pageText.contains("需要提取码") ||
                    pageText.contains("请输入提取码") ||
                    pageText.contains("输入密码") ||
                    pageText.contains("提取码")) {
                return "需要密码";
            }

            if (pageText.contains("保存到网盘") ||
                    pageText.contains("保存到我的网盘") ||
                    pageHtml.contains("save to pan")) {
                return "有效";
            }

            // 检查是否存在分享不存在的情况
            if (pageHtml.contains("404") ||
                    pageHtml.contains("not found") ||
                    pageText.contains("不存在")) {
                return "链接失效：分享不存在";
            }

            // 如果以上都没匹配到，返回页面内容预览
            String preview = pageText.length() > 100 ? pageText.substring(0, 100) : pageText;
            return "未知状态，页面内容: " + preview;

        } catch (Exception e) {
            return "检测失败: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }*/

    /**
     * 简化的布尔方法：判断链接是否可以用于转存
     * @param quarkLink 夸克链接
     * @return true=可以尝试转存，false=彻底失效
     */
 /*   public static boolean isQuarkLinkSaveable(String quarkLink) {
        String status = checkQuarkLinkStatus(quarkLink);
        boolean saveable = !status.startsWith("链接失效") &&
                !status.startsWith("检测失败") &&
                !status.equals("未知状态，页面内容: ");

        System.out.println("结果: " + status + " → " + (saveable ? "✅ 可转存" : "❌ 不可转存"));
        return saveable;
    }*/

    /**
     * 从文件读取链接（每行一个）- JDK 8 兼容版
     * @param filePath 文件路径
     * @return 链接列表
     */
    public static java.util.List<String> readLinksFromFile(String filePath) {
        java.util.List<String> links = new java.util.ArrayList<>();
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            links = java.nio.file.Files.readAllLines(path);

            // 【JDK 8 兼容】使用迭代器去除空行和空格
            java.util.Iterator<String> iterator = links.iterator();
            while (iterator.hasNext()) {
                String link = iterator.next();
                if (link == null || link.trim().isEmpty()) {
                    iterator.remove();
                } else {
                    // 原地替换为trim后的值
                    int index = links.indexOf(link);
                    links.set(index, link.trim());
                }
            }

            System.out.println("从文件读取到 " + links.size() + " 个链接");
        } catch (Exception e) {
            System.err.println("读取文件失败: " + e.getMessage());
        }
        return links;
    }

    public static void main(String[] args) {
/*        try {
            // 测试单个链接
            System.out.println("=== 单个链接测试 ===");
            String testLink = "https://pan.quark.cn/s/0186e9cfeee4";
            boolean saveable = isQuarkLinkSaveable(testLink);
            System.out.println("最终判断: " + (saveable ? "可转存" : "不可转存"));

        } catch (Exception e) {
            System.err.println("程序运行异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 程序结束时关闭驱动
            quitDriver();
        }*/
    }
}
