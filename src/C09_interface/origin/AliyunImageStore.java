package C09_interface.origin;

class Image {}

public class AliyunImageStore {

    public void createBucketIfNotExisting(String bucketName) { }

    public String generateAccessToken() { return ""; }

    public String uploadToAliyun(Image image, String bucketName, String accessToken) { return ""; }

    public Image downloadFromAliyun(String url, String accessToken) { return new Image(); }
}

// AliyunImageStore类的使用举例
class ImageProcessingJob {
    private static final String BUCKET_NAME = "ai_images_bucket";

    public void process() {
        Image image = new Image(); //处理图片，并封装为Image对象

        AliyunImageStore imageStore = new AliyunImageStore(/*省略参数*/);
        imageStore.createBucketIfNotExisting(BUCKET_NAME);
        String accessToken = imageStore.generateAccessToken();

        imageStore.uploadToAliyun(image, BUCKET_NAME, accessToken);
        imageStore.downloadFromAliyun("image_url", accessToken);
    }
}
