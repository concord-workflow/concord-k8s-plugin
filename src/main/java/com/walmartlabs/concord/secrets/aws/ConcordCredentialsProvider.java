package com.walmartlabs.concord.secrets.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

/**
 * Use Concords and its secrets manager as a way to provide AWS credentials for the AWS SDK Java client.
 */
public class ConcordCredentialsProvider implements AWSCredentialsProvider {

    private String accessKeyId;
    private String secretAccessKey;

    @Override
    public AWSCredentials getCredentials() {

        return new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return accessKeyId;
            }

            @Override
            public String getAWSSecretKey() {
                return secretAccessKey;
            }
        };
    }

    @Override
    public void refresh() {
    }
}
