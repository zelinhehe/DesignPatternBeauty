package C09_interface;

public class ProgramToInterface {
}

class Image {}

interface ImageStore {
    String upload(Image image, String bucketName);
    Image download(String url);
}

class AliyunImageStore implements ImageStore {

    public String upload(Image image, String bucketName) {
        createBucketIfNotExisting(bucketName);
        String accessToken = generateAccessToken();
        return "image_url";
    }

    public Image download(String url) {
        String accessToken = generateAccessToken();
        return new Image();
    }

    private void createBucketIfNotExisting(String bucketName) { }

    private String generateAccessToken() {
        return "token";
    }
}

// 上传下载流程改变：私有云不需要支持access token
class PrivateImageStore implements ImageStore  {
    public String upload(Image image, String bucketName) {
        createBucketIfNotExisting(bucketName);
        return "image_url";
    }

    public Image download(String url) { return new Image(); }

    private void createBucketIfNotExisting(String bucketName) { }
}

// ImageStore的使用举例
class ImageProcessingJob {
    private static final String BUCKET_NAME = "ai_images_bucket";

    public void process() {
        Image image = new Image();//处理图片，并封装为Image对象
        ImageStore imageStore = new PrivateImageStore();

        imageStore.upload(image, BUCKET_NAME);
        imageStore.download("image_url");
    }
}
