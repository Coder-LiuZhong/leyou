package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Store;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

    // 允许上传的文件类型；  先写在此，后续可搞到yml配置文件里； 数组工具类，可以接收可变参数 String... a
    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/png","image/bmp");        // 字符数组转化为list

    /**
     * 上传图片到本地
     * */
    public String uploadImage(MultipartFile file) {
        try {
            // 检验文件类型
            String contentType = file.getContentType();     // 浏览器F12可以看到传了这个参数进来
            if ( !ALLOW_TYPES.contains(contentType) ) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());  // ImageIO工具类可以读取一个输入流，如果读取的不是图片就会返回空或者抛出一个异常
            if (image == null) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 准备目标路径
            //String classpath = this.getClass().getClassLoader().getResource("").getFile();     // 可能出错。   保存到classpath下挺麻烦的
            //File dest = new File(classpath);
            File dest2 = new File("D:/WorkSpace/learning/IDEA2018/leyou/leyou", file.getOriginalFilename());

            // 1.保存文件到本地
            file.transferTo(dest2);

            // 2.返回一个路径
            return "http://image.leyou.com/" + file.getOriginalFilename();

            // 图片有一个另外的地址：  因为图片不能保存在服务器内部，会对服务器产生额外的加载负担；  一般静态资源都应该采用独立域名，这样访问静态资源时不会携带一些不必要的cookie，减少请求的数据量

        } catch (IOException e) {       // 上传失败要去记录日志，所以这里不抛
            log.error("上传文件失败!", e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }

    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    /**
     * 优化
     * 上传图片到FastDFS。
     * */
    public String uploadImageFstDFS(MultipartFile file) {
        try {
            // 检验文件类型
            String contentType = file.getContentType();     // 浏览器F12可以看到传了这个参数进来
            if ( !ALLOW_TYPES.contains(contentType) ) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());  // ImageIO工具类可以读取一个输入流，如果读取的不是图片就会返回空或者抛出一个异常
            if (image == null) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 上传到FastDFS
            //String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");         // 在最后一个点的后面进行截取  底层截取的效率比上面要高很多
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

            // 返回一个路径
            return "http://image.leyou.com/" + storePath.getFullPath();

        } catch (IOException e) {       // 上传失败要去记录日志，所以这里不抛
            log.error("上传文件失败!", e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }

    @Autowired
    private UploadProperties prop;

    /**
     * 进一步优化
     * 抽取fastDFS固定的部分作为属性放到yml配置文件里，通过读取属性文件来获取。
     * */
    public String uploadImageFstDFSYml(MultipartFile file) {
        try {
            // 检验文件类型
            String contentType = file.getContentType();     // 浏览器F12可以看到传了这个参数进来
            if ( !prop.getAllowTypes().contains(contentType) ) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());  // ImageIO工具类可以读取一个输入流，如果读取的不是图片就会返回空或者抛出一个异常
            if (image == null) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 上传到FastDFS
            //String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");         // 在最后一个点的后面进行截取  底层截取的效率比上面要高很多
            StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);

            // 返回一个路径
            return prop.getBaseUrl() + storePath.getFullPath();

        } catch (IOException e) {       // 上传失败要去记录日志，所以这里不抛
            log.error("[文件上传] 上传文件失败!", e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }

}
