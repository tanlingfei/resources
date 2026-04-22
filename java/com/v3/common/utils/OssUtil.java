package com.v3.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OssUtil {

    @Autowired
    private OssProperties ossProperties;

    public String uploadFile(File file, String folder) {
        OSS ossClient = null;
        InputStream inputStream = null;
        try {
            String endpoint = ossProperties.getEndpoint();
            String accessKeyId = ossProperties.getAccessKeyId();
            String accessKeySecret = ossProperties.getAccessKeySecret();
            String bucketName = ossProperties.getBucketName();

            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            String fileName = file.getName();
            String objectName = folder + "/" + fileName;

            // 检查文件是否已存在
            if (ossClient.doesObjectExist(bucketName, objectName)) {
                System.out.println("文件已存在，跳过上传: " + fileName);
                return "https://" + bucketName + "." + endpoint + "/" + objectName;
            }

            inputStream = new FileInputStream(file);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);

            String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
            return url;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    public String uploadFile(MultipartFile file, String folder,String randomName) {
        OSS ossClient = null;
        InputStream inputStream = null;
        try {
            String endpoint = ossProperties.getEndpoint();
            String accessKeyId = ossProperties.getAccessKeyId();
            String accessKeySecret = ossProperties.getAccessKeySecret();
            String bucketName = ossProperties.getBucketName();

            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            String fileName = file.getOriginalFilename();
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String newFileName = randomName + extension;

            String objectName = folder + "/" + newFileName;

            inputStream = file.getInputStream();

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);

            return "/" + objectName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    public List<String> listFiles(String folder) {
        OSS ossClient = null;
        List<String> fileList = new ArrayList<>();
        try {
            String endpoint = ossProperties.getEndpoint();
            String accessKeyId = ossProperties.getAccessKeyId();
            String accessKeySecret = ossProperties.getAccessKeySecret();
            String bucketName = ossProperties.getBucketName();

            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            String nextMarker = null;
            do {
                ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName)
                        .withPrefix(folder + "/")
                        .withDelimiter("/")
                        .withMarker(nextMarker)
                        .withMaxKeys(1000); // 每次最多获取1000个文件

                ObjectListing objectListing = ossClient.listObjects(listObjectsRequest);
                for (OSSObjectSummary summary : objectListing.getObjectSummaries()) {
                    String key = summary.getKey();
                    if (!key.endsWith("/")) {
                        fileList.add(key.substring(key.lastIndexOf("/") + 1));
                    }
                }
                nextMarker = objectListing.getNextMarker();
            } while (nextMarker != null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return fileList;
    }
}
