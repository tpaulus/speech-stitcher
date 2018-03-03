package edu.sdsu.cs.Transcode;

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClientBuilder;
import com.amazonaws.services.elastictranscoder.model.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
public class ETPipeline {

    private static ETPipeline instance = new ETPipeline();
    AmazonElasticTranscoder elasticTranscoder;
    private Properties pipelineConfig = new Properties();

    @Getter
    private Pipeline pipeline;

    private ETPipeline() {
        try {
            pipelineConfig.load(this.getClass().getClassLoader().getResourceAsStream(Credentials.CONFIG_FILE_NAME));
        } catch (IOException e) {
            log.error("Could not open Pipeline Config - unable to create pipeline");
            throw new RuntimeException("Could not open Pipeline Config", e);
        }

        elasticTranscoder = AmazonElasticTranscoderClientBuilder
                .standard()
                .withCredentials(Credentials.getCredentials())
                .withRegion(Credentials.getRegion())
                .build();
    }

    public static ETPipeline getInstance() {
        return instance;
    }

    /**
     * Make a new Pipeline based off of the Config File
     * Buckets and SNS topics should exist before this method is called.
     */
    public void makePipeline() {
        CreatePipelineRequest createPipelineRequest = new CreatePipelineRequest();
        Notifications notifications = new Notifications();

        createPipelineRequest.setName(pipelineConfig.getProperty("name"));
        createPipelineRequest.setInputBucket(pipelineConfig.getProperty("s3.source_bucket"));
        createPipelineRequest.setOutputBucket(pipelineConfig.getProperty("s3.destination_bucket"));
        notifications.setProgressing(pipelineConfig.getProperty("sns.progressing_topic"));
        notifications.setCompleted(pipelineConfig.getProperty("sns.completion_topic"));
        createPipelineRequest.setNotifications(notifications);

        CreatePipelineResult result = elasticTranscoder.createPipeline(createPipelineRequest);

        log.debug("Pipeline ARN - " + result.getPipeline().getArn());
        log.debug("CreatePipelineRequest - " + elasticTranscoder.getCachedResponseMetadata(createPipelineRequest));

        pipeline = result.getPipeline();
    }

    /**
     * Delete the Pipeline - This will fail if there are active jobs in the pipeline
     */
    public void deletePipeline() {
        DeletePipelineRequest deletePipelineRequest = new DeletePipelineRequest();
        DeletePipelineResult result = elasticTranscoder.deletePipeline(deletePipelineRequest);
        log.debug("DeletePipelineRequest - " + elasticTranscoder.getCachedResponseMetadata(deletePipelineRequest));
    }

    /**
     * Find a pipeline that matches the saved configuration
     *
     * @return True if a matching pipeline was found; else false
     */
    public boolean findPipeline() {
        ListPipelinesRequest listPipelinesRequest = new ListPipelinesRequest();
        ListPipelinesResult result = elasticTranscoder.listPipelines(listPipelinesRequest);
        log.debug("ListPipelinesRequest - " + elasticTranscoder.getCachedResponseMetadata(listPipelinesRequest));

        for (Pipeline pipeline : result.getPipelines()) {
            if (pipelineConfig.getProperty("name").equals(pipeline.getName()) &&
                    pipelineConfig.getProperty("s3.source_bucket").equals(pipeline.getInputBucket()) &&
                    pipelineConfig.getProperty("s3.destination_bucket").equals(pipeline.getOutputBucket())) {
                this.pipeline = pipeline;
                return true;
            }
        }

        return false;
    }

    /**
     * Activate the Pipeline
     */
    public void activate() {
        if (pipeline == null) throw new RuntimeException("No active Pipeline - Search for or Create a pipeline first");
        log.debug("Activating Pipeline - " + pipeline.getName());

        UpdatePipelineStatusRequest updatePipelineStatusRequest = new UpdatePipelineStatusRequest();
        updatePipelineStatusRequest.setId(pipeline.getId());
        updatePipelineStatusRequest.setStatus("Active");

        UpdatePipelineStatusResult result = elasticTranscoder.updatePipelineStatus(updatePipelineStatusRequest);
        log.debug("UpdatePipelineStatusRequest - " + elasticTranscoder.getCachedResponseMetadata(updatePipelineStatusRequest));

        pipeline = result.getPipeline();
        log.debug("Pipeline Active - " + pipeline.getName());
    }
}
