package com.leyou.upload;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FdfsTest {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;

    // 注入客户端调用方法即可
    @Test
    public void testUpload() throws FileNotFoundException {
        File file = new File("C:\\Users\\Liuzhong-T580\\Desktop\\leyou\\vue\\vue插件.png");
        // 上传并且生成缩略图
        StorePath storePath = this.fastFileStorageClient.uploadFile(new FileInputStream(file), file.length(), "png",null);
        // 带分组的路径
        System.out.println(storePath.getFullPath());
        // 不带分组的路径
        System.out.println(storePath.getPath());
    }

    // 注入客户端调用方法即可
    @Test
    public void testUploadCreateThumb() throws FileNotFoundException {
        File file = new File("C:\\Users\\Liuzhong-T580\\Pictures\\Java学习路线.jpg");       // 传个大点的图片
        // 上传并且生成缩略图
        StorePath storePath = this.fastFileStorageClient.uploadImageAndCrtThumbImage(new FileInputStream(file), file.length(), "jpg",null);
        // 带分组的路径
        System.out.println(storePath.getFullPath());
        // 不带分组的路径
        System.out.println(storePath.getPath());
        // 获取缩略图路径
        String path = thumbImageConfig.getThumbImagePath(storePath.getPath());
        System.out.println(path);
    }



}
