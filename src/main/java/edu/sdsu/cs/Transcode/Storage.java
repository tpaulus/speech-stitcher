package edu.sdsu.cs.Transcode;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Getter;

import java.io.File;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
public class Storage {
    public static File getVideoByARN(final String arn) {
        return S3.getInstance().getFileByARN(arn);
    }

    private static class S3 {
        @Getter
        private static S3 instance = new S3();
        private AmazonS3 s3;

        private S3() {
            s3 = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(Credentials.getCredentials())
                            .build();
        }

        public File getFileByARN(final String arn) {
            // TODO
            return null;
        }
    }
}
