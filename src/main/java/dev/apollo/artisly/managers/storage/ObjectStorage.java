package dev.apollo.artisly.managers.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class ObjectStorage {

  private AmazonS3 s3Client;

  public ObjectStorage(String endpoint, String accessKey, String secretKey) {

    BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, "");

    s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(endpointConfiguration)
            .withPathStyleAccessEnabled(true)
            .build();
  }

  public AmazonS3 getS3Client() {
    return s3Client;
  }

}