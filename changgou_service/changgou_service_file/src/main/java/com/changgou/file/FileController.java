package com.changgou.file;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
public class FileController {


    @PostMapping("/upload")
    public String upload( @RequestParam("file") MultipartFile file){
        try {
            //1.获取文件名
            String fileName = file.getOriginalFilename();
            //2.获取文件内容
            byte[] bytes = file.getBytes();
            //3.获取文件扩展名 .
            String ext = fileName.substring( fileName.lastIndexOf( "." ) );
            //4.封装文件实体
            FastDFSFile fastDFSFile=new FastDFSFile( fileName,bytes,ext );
            //5.文件上传
            String[] result = FastDFSClient.upload( fastDFSFile );
            //6.返回结果
            String path = FastDFSClient.getTrackerUrl()+result[0]+"/"+result[1];
            return path;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
