package ca.vanzyl.concord.plugins;

import org.ini4j.Wini;

import java.io.File;

public class AwsCredentials {

    private static final String AWS_ACCESS_KEY_ID = "aws_access_key_id";
    private static final String AWS_SECRET_ACCESS_KEY = "aws_secret_access_key";
    private static final File AWS_CREDENTIALS_DIRECTORY = new File(System.getProperty("user.home"), ".aws");
    private static final File AWS_CREDENTIALS_FILE = new File(AWS_CREDENTIALS_DIRECTORY, "credentials");
    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;

    public AwsCredentials() throws Exception {
        Wini awsCredentialsFile = new Wini(AWS_CREDENTIALS_FILE);
        awsAccessKeyId = awsCredentialsFile.get("default", AWS_ACCESS_KEY_ID, String.class);
        awsSecretAccessKey = awsCredentialsFile.get("default", AWS_SECRET_ACCESS_KEY, String.class);
    }

    public String awsAccessKeyId() {
        return awsAccessKeyId;
    }

    public String awsSecretAccessKey() {
        return awsSecretAccessKey;
    }
}
