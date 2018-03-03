package edu.sdsu.cs.Transcode;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
public class Credentials {
    public static final String CONFIG_FILE_NAME = "ets_pipeline.properties";
    private static final String AWS_ACCESS_ENV_NAME = "AWS_ACCESS";
    private static final String AWS_SECRET_ENV_NAME = "AWS_SECRET";

    public static AWSCredentialsProvider getCredentials() {
        return new AWSStaticCredentialsProvider(new AWSCredentials() {
            @Override public String getAWSAccessKeyId() {
                return System.getenv(AWS_ACCESS_ENV_NAME);
            }

            @Override public String getAWSSecretKey() {
                return System.getenv(AWS_SECRET_ENV_NAME);
            }
        });
    }

    public static String getRegion() {
        try {
            Properties properties = new Properties();
            properties.load(Credentials.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME));
            return properties.getProperty("region");
        } catch (IOException e) {
            log.error("Could not open Pipeline Config - unable to create pipeline");
            throw new RuntimeException("Could not open Pipeline Config", e);
        }
    }
}
