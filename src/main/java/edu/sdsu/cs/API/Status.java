package edu.sdsu.cs.API;

import edu.sdsu.cs.JobStore;
import edu.sdsu.cs.Models.SimpleMessage;
import edu.sdsu.cs.Models.StitchJob;
import lombok.extern.log4j.Log4j;
import org.glassfish.jersey.media.sse.SseFeature;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
@Path("status/{id}")
public class Status {
    /**
     * Get the status of a job. This endpoint should not be used for polling, use the stream endpoint instead.
     *
     * @param jobId {@link String} Job ID
     * @return Job Status as JSON
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobStatus(@PathParam("id") final String jobId) {
        log.debug("Getting Job Status for Job: " + jobId);
        StitchJob job = JobStore.getInstance().getJob(jobId);
        if (job == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new SimpleMessage("Error", String.format("No job exists with that id. Check the ID" +
                                    "and try the request again. Requests expire %d hours after they are completed.",
                            JobStore.getJOB_TTL())).asJson())
                    .build();
        }

        return Response
                .status(Response.Status.OK)
                .entity(job.getStatus().asJson())
                .build();
    }

    /**
     * Get SSE Broadcast Stream of Status Updates. Ideal for updating a Client Interface as the job is being processed
     *
     * @param jobId {@link String} Job ID
     * @return SSE Event Stream
     */
    @Path("stream")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public Response getJobSSEBroadcast(@PathParam("id") final String jobId) {
        log.debug("Getting Job Status for Job: " + jobId);
        StitchJob job = JobStore.getInstance().getJob(jobId);
        if (job == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new SimpleMessage("Error", String.format("No job exists with that id. Check the ID" +
                                    "and try the request again. Requests expire %d hours after they are completed.",
                            JobStore.getJOB_TTL())).asJson())
                    .build();
        }

        return Response
                .status(Response.Status.OK)
                .entity(job.getBroadcaster())
                .build();
    }
}
