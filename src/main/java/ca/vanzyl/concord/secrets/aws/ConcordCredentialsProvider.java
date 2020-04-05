package ca.vanzyl.concord.secrets.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSSessionCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use Concords and its secrets manager as a way to provide AWS credentials for the AWS SDK Java client.
 */
public class ConcordCredentialsProvider implements AWSCredentialsProvider {

    private static Logger logger = LoggerFactory.getLogger(ConcordCredentialsProvider.class);

    private String awsAccessKey;
    private String awsSecretKey;
    private String awsSessionToken;

    @Override
    public AWSCredentials getCredentials() {

        if (awsSessionToken != null && awsSessionToken != "") {
            return new AWSSessionCredentials() {
                @Override
                public String getSessionToken() {
                    return awsSessionToken;
                }

                @Override
                public String getAWSAccessKeyId() {
                    return awsAccessKey;
                }

                @Override
                public String getAWSSecretKey() {
                    return awsAccessKey;
                }
            };
        } else {
            return new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return awsAccessKey;
                }

                @Override
                public String getAWSSecretKey() {
                    return awsAccessKey;
                }
            };
        }
    }

    @Override
    public void refresh() {
    }
}
