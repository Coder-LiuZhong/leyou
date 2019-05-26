package com.leyou.upload.web;

import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 图片上传
     *      http://api.leyou.com/api/upload/image
     *      图片上传肯定是POST
     *      请求参数文件file被SpringMVC封装为了一个接口来接收：MultipartFile
     *      上传后返回文件的url路径
     * */
    @PostMapping("image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file ){
        //return ResponseEntity.ok( uploadService.uploadImage(file) );                  //上传到本地
        //return ResponseEntity.ok( uploadService.uploadImageFstDFS(file) );
        return ResponseEntity.ok( uploadService.uploadImageFstDFSYml(file) );           //上传到fastDFS
    }

}
