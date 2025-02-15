package dev.apollo.artisly.services;


import com.amazonaws.services.s3.model.*;
import dev.apollo.artisly.Artisly;
import dev.apollo.artisly.models.ProductImage;
import org.apache.tika.Tika;
import redis.clients.jedis.Jedis;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MediaService {

    private Artisly artisly;
    private String bucketName = "artisly";
    private String profilePictureDirectory = "profile-pictures";
    private String storePictureDirectory = "store-pictures";
    private String productPictureDirectory = "product-pictures";
    private String immutablePictureDirectory = "immutable-product-pictures";


    public MediaService(Artisly artisly)
    {
        this.artisly = artisly;
    }


    public String uploadProfilePicture(UUID userId, InputStream is, String contentType)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, profilePictureDirectory + "/" + userId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                artisly.getObjectStorage().getS3Client().deleteObject(bucketName, os.getKey());
            }
        }

        String fileName = UUID.randomUUID().toString();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        PutObjectRequest request = new PutObjectRequest(bucketName, profilePictureDirectory + "/" + userId.toString() + "/" + fileName, is, metadata);
        request.setCannedAcl(CannedAccessControlList.PublicRead);
        artisly.getObjectStorage().getS3Client().putObject(request);

        String URL = artisly.getObjectStorage().getS3Client().getUrl(bucketName, profilePictureDirectory + "/" + userId.toString() + "/" + fileName).toString();

        try(Jedis jedis = artisly.getRedis().getJedis().getResource())
        {
            jedis.hdel("profile-picture-url", userId.toString());
            jedis.hset("profile-picture-url", userId.toString(), URL);
        }

        return URL;

    }

    public String getProfilePictureUrl(UUID userId)
    {
        try(Jedis jedis = artisly.getRedis().getJedis().getResource())
        {
            if (jedis.hexists("profile-picture-url", userId.toString()))
            {
                String URL = jedis.hget("profile-picture-url", userId.toString());
                if (!URL.equalsIgnoreCase("nil"))
                {
                    return URL;
                }
            }
        }
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, profilePictureDirectory + "/" + userId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                String url = artisly.getObjectStorage().getS3Client().getUrl(bucketName, os.getKey()).toString();
                try(Jedis jedis = artisly.getRedis().getJedis().getResource())
                {
                    jedis.hset("profile-picture-url", userId.toString(), url);
                }
                return url;
            }
        }

        String URL = artisly.getObjectStorage().getS3Client().getUrl(bucketName, profilePictureDirectory + "/default/default").toString();
        try(Jedis jedis = artisly.getRedis().getJedis().getResource())
        {
            jedis.hset("profile-picture-url", userId.toString(), URL);
        }
        return URL;
    }

    public boolean deleteProfilePicture(UUID userId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, profilePictureDirectory + "/" + userId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                artisly.getObjectStorage().getS3Client().deleteObject(bucketName, os.getKey());
                try(Jedis jedis = artisly.getRedis().getJedis().getResource())
                {
                    jedis.hdel("profile-picture-url", userId.toString());
                }
            }

            artisly.getObjectStorage().getS3Client().deleteObject(bucketName, profilePictureDirectory + "/" + userId.toString());
            return true;
        }
        return false;
    }

    public boolean hasProfilePicture(UUID userId)
    {
        try(Jedis jedis = artisly.getRedis().getJedis().getResource())
        {
            if (jedis.hexists("profile-picture-url", userId.toString()))
            {
                String URL = jedis.hget("profile-picture-url", userId.toString());
                if (!URL.equalsIgnoreCase("nil"))
                {
                    return true;
                }
            }
        }
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, profilePictureDirectory + "/" + userId.toString()).getObjectSummaries();
        return objects.size() > 0;
    }

    public String uploadStoreLogo(UUID storeId, InputStream is, String contentType)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, storePictureDirectory + "/" + storeId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("logo"))
                {
                    artisly.getObjectStorage().getS3Client().deleteObject(bucketName, os.getKey());
                    try(Jedis jedis = artisly.getRedis().getJedis().getResource())
                    {
                        jedis.hdel("store-logo-url", storeId.toString());
                    }
                }
            }
        }

        String fileName = "logo";
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        PutObjectRequest request = new PutObjectRequest(bucketName, storePictureDirectory + "/" + storeId.toString() + "/" + fileName, is, metadata);
        request.setCannedAcl(CannedAccessControlList.PublicRead);

        artisly.getObjectStorage().getS3Client().putObject(request);

        String URL = artisly.getObjectStorage().getS3Client().getUrl(bucketName, storePictureDirectory + "/" + storeId.toString() + "/" + fileName).toString();
        try(Jedis jedis = artisly.getRedis().getJedis().getResource())
        {
            jedis.hset("store-logo-url", storeId.toString(), URL);
        }
        return URL;

    }

    public String uploadStoreBanner(UUID storeId, InputStream is, String contentType)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, storePictureDirectory + "/" + storeId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("banner"))
                {
                    artisly.getObjectStorage().getS3Client().deleteObject(bucketName, os.getKey());
                    try(Jedis jedis = artisly.getRedis().getJedis().getResource())
                    {
                        jedis.hdel("store-banner-url", storeId.toString());
                    }
                }
            }
        }

        String fileName = "banner";
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        PutObjectRequest request = new PutObjectRequest(bucketName, storePictureDirectory + "/" + storeId.toString() + "/" + fileName, is, metadata);
        request.setCannedAcl(CannedAccessControlList.PublicRead);

        artisly.getObjectStorage().getS3Client().putObject(request);

        String URL = artisly.getObjectStorage().getS3Client().getUrl(bucketName, storePictureDirectory + "/" + storeId.toString() + "/" + fileName).toString();
        try(Jedis jedis = artisly.getRedis().getJedis().getResource())
        {
            jedis.hset("store-banner-url", storeId.toString(), URL);
        }

        return URL;
    }

    public String getStoreLogoUrl(UUID storeId)
    {
        try(Jedis jedis = artisly.getRedis().getJedis().getResource())
        {
            if (jedis.hexists("store-logo-url", storeId.toString()))
            {
                String URL = jedis.hget("store-logo-url", storeId.toString());
                if (!URL.equalsIgnoreCase("nil"))
                {
                    return URL;
                }
            }
        }
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, storePictureDirectory + "/" + storeId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("logo"))
                {
                    String URL = artisly.getObjectStorage().getS3Client().getUrl(bucketName, os.getKey()).toString();
                    try(Jedis jedis = artisly.getRedis().getJedis().getResource())
                    {
                        jedis.hset("store-logo-url", storeId.toString(), URL);
                    }
                    return URL;
                }
            }
        }

        String URL = artisly.getObjectStorage().getS3Client().getUrl(bucketName, storePictureDirectory + "/default/logo.png").toString();
        try(Jedis jedis = artisly.getRedis().getJedis().getResource())
        {
            jedis.hset("store-logo-url", storeId.toString(), URL);
        }

        return URL;
    }

    public String getStoreBannerUrl(UUID storeId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, storePictureDirectory + "/" + storeId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("banner"))
                {
                    String URL = artisly.getObjectStorage().getS3Client().getUrl(bucketName, os.getKey()).toString();
                    try(Jedis jedis = artisly.getRedis().getJedis().getResource())
                    {
                        jedis.hset("store-banner-url", storeId.toString(), URL);
                    }
                    return URL;
                }
            }
        }

        String URL = artisly.getObjectStorage().getS3Client().getUrl(bucketName, storePictureDirectory + "/default/banner.png").toString();
        try(Jedis jedis = artisly.getRedis().getJedis().getResource())
        {
            jedis.hset("store-banner-url", storeId.toString(), URL);
        }
        return URL;
    }

    public boolean deleteStoreLogo(UUID storeId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, storePictureDirectory + "/" + storeId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("logo"))
                {
                    artisly.getObjectStorage().getS3Client().deleteObject(bucketName, os.getKey());

                }
            }

            artisly.getObjectStorage().getS3Client().deleteObject(bucketName, storePictureDirectory + "/" + storeId.toString());
            return true;
        }
        return false;
    }

    public boolean deleteStoreBanner(UUID storeId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, storePictureDirectory + "/" + storeId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("banner"))
                {
                    artisly.getObjectStorage().getS3Client().deleteObject(bucketName, os.getKey());
                }
            }

            artisly.getObjectStorage().getS3Client().deleteObject(bucketName, storePictureDirectory + "/" + storeId.toString());
            return true;
        }
        return false;
    }

    public boolean hasStoreLogo(UUID storeId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, storePictureDirectory + "/" + storeId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("logo"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasStoreBanner(UUID storeId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, storePictureDirectory + "/" + storeId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("banner"))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public int getProductImageCount(UUID productId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, productPictureDirectory + "/" + productId.toString()).getObjectSummaries();
        return objects.size();
    }

    public ProductImage uploadProductImage(UUID productId, InputStream is, String contentType, boolean isThumbnail)
    {

        String fileName = UUID.randomUUID().toString();
        if (isThumbnail)
        {
            fileName = "thumbnail";
        }

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);

        PutObjectRequest request = new PutObjectRequest(bucketName, productPictureDirectory + "/" + productId.toString() + "/" + fileName, is, metadata);
        request.setCannedAcl(CannedAccessControlList.PublicRead);

        artisly.getObjectStorage().getS3Client().putObject(request);

        ProductImage productImage = new ProductImage(
                fileName,
                artisly.getObjectStorage().getS3Client().getUrl(bucketName, productPictureDirectory + "/" + productId.toString() + "/" + fileName).toString(),
                isThumbnail);

        return productImage;

    }

    public List<ProductImage> getProductImages(UUID productId)
    {
        List<ProductImage> images = new ArrayList<>();
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, productPictureDirectory + "/" + productId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                images.add(new ProductImage(
                        os.getKey().substring(os.getKey().lastIndexOf("/") + 1),
                        artisly.getObjectStorage().getS3Client().getUrl(bucketName, os.getKey()).toString(),
                        os.getKey().contains("thumbnail")));
            }
        }
        else
        {
                images.add(new ProductImage(
                    "default",
                    artisly.getObjectStorage().getS3Client().getUrl(bucketName, productPictureDirectory + "/default/thumbnail").toString(),
                    true
            ));
        }
        return images;
    }

    public ProductImage getProductThumbnail(UUID productId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, productPictureDirectory + "/" + productId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("thumbnail"))
                {
                    return new ProductImage(
                            os.getKey().substring(os.getKey().lastIndexOf("/") + 1),
                            artisly.getObjectStorage().getS3Client().getUrl(bucketName, os.getKey()).toString(),
                            true);
                }
            }
        }

        return new ProductImage(
                "default",
                artisly.getObjectStorage().getS3Client().getUrl(bucketName, productPictureDirectory + "/default").toString(),
                true
        );
    }

    public boolean deleteProductImage(UUID productId, String fileName)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, productPictureDirectory + "/" + productId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains(fileName))
                {
                    artisly.getObjectStorage().getS3Client().deleteObject(bucketName, os.getKey());
                }
            }

            artisly.getObjectStorage().getS3Client().deleteObject(bucketName, productPictureDirectory + "/" + productId.toString());
            return true;
        }
        return false;
    }

    public boolean hasProductImages(UUID productId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, productPictureDirectory + "/" + productId.toString()).getObjectSummaries();
        return objects.size() > 0;
    }

    public boolean deleteAllProductImages(UUID productId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, productPictureDirectory + "/" + productId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                artisly.getObjectStorage().getS3Client().deleteObject(bucketName, os.getKey());
            }

            artisly.getObjectStorage().getS3Client().deleteObject(bucketName, productPictureDirectory + "/" + productId.toString());
            return true;
        }
        return false;
    }

    public List<ProductImage> copyProductImagesToImmutable(UUID productId, UUID immutableProductId)
    {
        List<ProductImage> images = new ArrayList<>();
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, productPictureDirectory + "/" + productId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                String fileName = os.getKey().substring(os.getKey().lastIndexOf("/") + 1);
                CopyObjectRequest request = new CopyObjectRequest(bucketName, os.getKey(), bucketName, immutablePictureDirectory + "/" + immutableProductId.toString() + "/" + fileName);
                request.setCannedAccessControlList(CannedAccessControlList.PublicRead);
                artisly.getObjectStorage().getS3Client().copyObject(request);
                images.add(new ProductImage(
                        fileName,
                        artisly.getObjectStorage().getS3Client().getUrl(bucketName, immutablePictureDirectory + "/" + immutableProductId.toString() + "/" + fileName).toString(),
                        os.getKey().contains("thumbnail")));
                //images.add(artisly.getObjectStorage().getS3Client().getUrl(bucketName, immutablePictureDirectory + "/" + immutableProductId.toString() + "/" + fileName).toString());
            }
        }
        return images;
    }

    public List<ProductImage> getImmutableProductImages(UUID immutableProductId)
    {
        List<ProductImage> images = new ArrayList<>();
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, immutablePictureDirectory + "/" + immutableProductId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                images.add(new ProductImage(
                        os.getKey().substring(os.getKey().lastIndexOf("/") + 1),
                        artisly.getObjectStorage().getS3Client().getUrl(bucketName, os.getKey()).toString(),
                        os.getKey().contains("thumbnail")));
            }
        }
        else
        {
            images.add(new ProductImage(
                    "default",
                    artisly.getObjectStorage().getS3Client().getUrl(bucketName, productPictureDirectory + "/default/thumbnail").toString(),
                    true
            ));
        }
        return images;
    }

    public ProductImage getImmutableProductThumbnail(UUID immutableProductId)
    {
        List<S3ObjectSummary> objects = artisly.getObjectStorage().getS3Client().listObjects(bucketName, immutablePictureDirectory + "/" + immutableProductId.toString()).getObjectSummaries();
        if (objects.size() > 0)
        {
            for (S3ObjectSummary os : objects)
            {
                if (os.getKey().contains("thumbnail"))
                {
                    return new ProductImage(
                            os.getKey().substring(os.getKey().lastIndexOf("/") + 1),
                            artisly.getObjectStorage().getS3Client().getUrl(bucketName, os.getKey()).toString(),
                            true);
                }
            }
        }

        return new ProductImage(
                "default",
                artisly.getObjectStorage().getS3Client().getUrl(bucketName, immutablePictureDirectory + "/default").toString(),
                true
        );
    }






}
