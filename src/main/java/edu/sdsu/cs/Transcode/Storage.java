package edu.sdsu.cs.Transcode;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import edu.sdsu.cs.Models.StitchJob;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static edu.sdsu.cs.Transcode.ETJob.FILE_EXTENSION;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
public class Storage {
    public static void publishVideo(StitchJob job) {
        final String bucketName = ETPipeline.getInstance().getPipeline().getOutputBucket();
        final String key = job.getJobID() + FILE_EXTENSION;

        // Set Video as Public in Bucket
        S3.getInstance().setFileAsPublic(bucketName, key);

        // Set Job ID as a Tag in the S3 Object
        S3.getInstance().setFileTags(bucketName, key, new ArrayList<Tag>() {{
            add(new Tag("internal_job_id", job.getJobID()));
            add(new Tag("ets_job_id", job.getETSJobID()));
            add(new Tag("speaker", job.getSpeaker()));
        }});

        // Update outputURL in Job Status
        StitchJob.Status jobStatus = job.getStatus();
        jobStatus.setOutputUrl(S3.getInstance().getObjectURL(bucketName, key).toString());
        job.setStatus(jobStatus);

    }

    @SuppressWarnings("WeakerAccess")
    private static class S3 {
        @Getter
        private static S3 instance = new S3();
        private AmazonS3 s3;

        private S3() {
            s3 = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(Credentials.getCredentials())
                    .withRegion(Credentials.getRegion())
                    .build();
        }

        public void setFileAsPublic(final String bucket, final String key) {
            SetObjectAclRequest setObjectAclRequest = new SetObjectAclRequest(bucket, key, CannedAccessControlList.PublicRead);
            s3.setObjectAcl(setObjectAclRequest);
            log.debug("SetObjectAclRequest - " + s3.getCachedResponseMetadata(setObjectAclRequest));
        }

        public void setFileTags(final String bucket, final String key, List<Tag> tags) {
            SetObjectTaggingRequest setObjectTaggingRequest = new SetObjectTaggingRequest(
                    bucket,
                    key,
                    new ObjectTagging(tags));

            s3.setObjectTagging(setObjectTaggingRequest);
            log.debug("SetObjectTaggingRequest - " + s3.getCachedResponseMetadata(setObjectTaggingRequest));
        }

        public URL getObjectURL(final String bucket, final String key) {
            return s3.getUrl(bucket, key);
        }
    }
}
