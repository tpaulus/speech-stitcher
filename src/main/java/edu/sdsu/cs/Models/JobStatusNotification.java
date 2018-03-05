package edu.sdsu.cs.Models;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * POJO Model for JSON Object that is sent by ETS to SNS
 */
@Data
public class JobStatusNotification {
    private JobState state;
    private int errorCode;
    private String messageDetails;
    private String version;
    private String jobId;
    private String pipelineId;
    private JobInput input;
    private String outputKeyPrefix;
    private List<JobOutput> outputs;
    private Map<String, String> userMetadata;

    public enum JobState {
        PROGRESSING,
        COMPLETED,
        ERROR;

        public boolean isTerminalState() {
            return this.equals(JobState.COMPLETED) || this.equals(JobState.ERROR);
        }
    }

    @Data
    public static class JobInput {
        private String key;
    }

    @Data
    public static class JobOutput {
        private String id;
        private String presetId;
        private String key;
        private String status;

        private String statusDetail;
        private int errorCode;
    }
}