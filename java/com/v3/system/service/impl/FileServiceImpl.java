package com.v3.system.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.v3.common.utils.OssProperties;
import com.v3.common.utils.OssUtil;
import com.v3.system.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/8/27 22:41
 */
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private OssProperties ossProperties;

    public String upload(MultipartFile urlFile,String randomName) throws IOException {
        // 文件名称
        Calendar calendar = Calendar.getInstance();
        String originalFileName = urlFile.getOriginalFilename();
        String fileExtension = "";

        // 获取文件扩展名
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // 生成随机文件名
        String randomFileName = randomName+ fileExtension;

        // 文件存储的路径
        String path = "upload/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String filePath = ossProperties.getLocPath() + path;
        File file = new File(filePath + randomFileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();                //不存在就全部创建
        }
        String storeUrlPath = path + randomFileName;

        // 检查文件是否已经在临时目录中不可访问
        try {
            urlFile.transferTo(file);
        } catch (IllegalStateException e) {
            // 如果文件已经被移动，则创建一个副本再进行操作
            if (e.getMessage().contains("has already been moved")) {
                // 创建一个新的临时文件用于存储副本
                File tempFile = File.createTempFile("upload-", fileExtension);
                // 将原始文件内容复制到临时文件
                urlFile.transferTo(tempFile);
                // 将临时文件移动到目标位置
                tempFile.renameTo(file);
            } else {
                throw e;
            }
        }

        System.out.println("上传成功");
        return "/img/"+storeUrlPath;
    }

    public String uploadToOss(MultipartFile urlFile,String randName) throws IOException {
        String url = ossUtil.uploadFile(urlFile, "img/upload/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date()),randName);
        if (url != null) {
            /*System.out.println("OSS上传成功: " + url);*/
            return url;
        } else {
           /* System.out.println("OSS上传失败");*/
            throw new IOException("OSS上传失败");
        }
    }

    public String uploadToOssFromFile(File file, String randName) throws IOException {
        String originalFileName = file.getName();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String folder = "img/upload/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String newFileName = randName + fileExtension;
        String objectName = folder + "/" + newFileName;

        OSS ossClient = null;
        InputStream inputStream = null;
        try {
            String endpoint = ossProperties.getEndpoint();
            String accessKeyId = ossProperties.getAccessKeyId();
            String accessKeySecret = ossProperties.getAccessKeySecret();
            String bucketName = ossProperties.getBucketName();

            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            inputStream = new FileInputStream(file);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            ossClient.putObject(putObjectRequest);

            String url = "/" + objectName;
            System.out.println("OSS从文件上传成功: " + url);
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("OSS从文件上传失败", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    public String[] uploadBoth(MultipartFile urlFile, String randName) throws IOException {
        String originalFileName = urlFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String randomFileName = randName + fileExtension;

        byte[] fileBytes = urlFile.getBytes();

        String path = "upload/"+new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"/";
        String filePath = ossProperties.getLocPath() + path;
        File localFile = new File(filePath + randomFileName);
        if (!localFile.getParentFile().exists()) {
            localFile.getParentFile().mkdirs();
        }
        java.nio.file.Files.write(localFile.toPath(), fileBytes);
        String localUrl = "/img/" + path + randomFileName;

        File tempFile = File.createTempFile("upload-", fileExtension);
        java.nio.file.Files.write(tempFile.toPath(), fileBytes);
        String ossUrl = uploadToOssFromFile(tempFile, randName);
        tempFile.delete();

        return new String[]{localUrl, ossUrl};
    }

}
