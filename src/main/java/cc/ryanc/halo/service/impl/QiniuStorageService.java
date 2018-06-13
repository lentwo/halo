package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.service.CloudStorageService;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Service
@Slf4j
public class QiniuStorageService extends CloudStorageService {

    @Value("${qiniu.accessKey}")
    private String accessKey;

    @Value("${qiniu.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucket}")
    private String bucket;

    @Value("${qiniu.domain}")
    private String domain;

    private UploadManager uploadManager;

    private Auth auth;

    @PostConstruct
    public void init() {
        Configuration configuration = new Configuration(Zone.autoZone());
        uploadManager = new UploadManager(configuration);
        auth = Auth.create(accessKey, secretKey);
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fsize\":$(fsize)}");
    }

    @Override
    public String upload(byte[] data, String path) {
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(data, path, upToken);
            if (!response.isOK()) {
                log.error("upload file to qiniu failed", response.toString());
                throw new RuntimeException("upload file to qiniu failed：" + response.toString());
            }
        } catch (QiniuException e) {
            log.error("upload file to qiniu failed", e);
            throw new RuntimeException("upload file failed：", e);
        }
        return domain + "/" + path;
    }


}
