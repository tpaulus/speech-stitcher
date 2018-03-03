package edu.sdsu.cs.Models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import edu.sdsu.cs.Transcode.Storage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Data
@Log4j
public class StitchJob implements Serializable {
    @Expose
    private String jobID;
    @Expose
    private Status status;

    private String speaker;
    private String body;
    private List<SourceClip> clips;

    private String ETSJobID;
    private EventOutput broadcaster;

    @Builder
    public StitchJob(String speaker, String body) {
        this.jobID = UUID.randomUUID().toString();
        this.speaker = speaker;
        this.body = body;
        this.clips = new ArrayList<>();
        this.broadcaster = new EventOutput();
        this.status = new Status(TranscodeStatus.RECEIVED);
    }

    public void updateStatus(final Status updatedStatus) {
        setStatus(updatedStatus);
        if (updatedStatus.status == TranscodeStatus.COMPLETED)
            // Prep Video for Distribution
            Storage.publishVideo(this);

        try {
            getBroadcaster().write(new OutboundEvent.Builder()
                    .name("stitch-status")
                    .data(String.class, updatedStatus.asJson())
                    .build()
            );
        } catch (IOException e) {
            log.warn("Could not broadcast status update change; Job ID: " + getJobID());
        }
    }

    public String asJson() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }

    @SuppressWarnings("unused")
    @AllArgsConstructor
    public enum TranscodeStatus {
        RECEIVED("Received", 0),
        PROCESSING("Processing Text", 1),
        SUBMITTED("Stitching Queued", 2),
        IN_PROGRESS("Stitching In Progress", 3),
        COMPLETED("Completed", 4),
        ERROR("Error", -1);

        private String display_text;
        private int step;
    }

    @Data
    public static class Status {
        private TranscodeStatus status;
        private String outputUrl;

        Status(TranscodeStatus status) {
            this.status = status;
            this.outputUrl = "";
        }

        public String asJson() {
            return new Gson().toJson(this);
        }
    }
}
