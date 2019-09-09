package com.atguigu.gmall0401.gmall0401manageweb.controller;

import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author xtsky
 * @create 2019-09-08 21:07
 */
@RestController
@CrossOrigin
public class FileController {

    @Value("${fileServer.url}")
    String fileServerUrl;

    @PostMapping("fileUpload")
    public String fileUpload(@RequestParam("file")MultipartFile file) throws IOException, MyException {
        String ConfPath = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(ConfPath);
        TrackerClient trackerClient=new TrackerClient();
        TrackerServer trackerServer=trackerClient.getConnection();
        StorageClient storageClient=new StorageClient(trackerServer,null);
        String originalFilename = file.getOriginalFilename(); // 文件名
        String s1 = StringUtils.substringAfterLast(originalFilename, "."); //扩展名
        String[] upload_file = storageClient.upload_file(file.getBytes(), s1, null);
        String fileUrl = fileServerUrl;
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            fileUrl += "/" + s;
        }

        return fileUrl;

    }

}
