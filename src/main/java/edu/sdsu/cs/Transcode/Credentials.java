package edu.sdsu.cs.Transcode;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
public class Credentials {
    private static final String AWS_ACCESS_ENV_NAME = "AWS_ACCESS";
    private static final String AWS_SECRET_ENV_NAME = "AWS_SECRET";

    static AWSCredentialsProvider getCredentials() {
        return new AWSStaticCredentialsProvider(new AWSCredentials() {
            @Override public String getAWSAccessKeyId() {
                return System.getenv(AWS_ACCESS_ENV_NAME);
            }

            @Override public String getAWSSecretKey() {
                return System.getenv(AWS_SECRET_ENV_NAME);
            }
        });
    }
}
