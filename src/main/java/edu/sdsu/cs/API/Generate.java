package edu.sdsu.cs.API;

import edu.sdsu.cs.IntakeQueue;
import edu.sdsu.cs.Models.SimpleMessage;
import edu.sdsu.cs.Models.StitchJob;
import lombok.extern.log4j.Log4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Tom Paulus
 * Created on 1/24/18.
 */
@Path("make")
@Log4j
public class Generate {
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateVideo(@FormParam("speaker") final String speaker,
                                  @FormParam("text") final String text) {

        if (speaker.isEmpty() || text.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new SimpleMessage("Error", "Incomplete Request. Please specify both a " +
                            "speaker and the text payload").asJson())
                    .build();
        }

        StitchJob job = StitchJob.builder()
                .speaker(speaker)
                .body(text)
                .build();

        log.info("Created new Job - " + job.getJobID());
        log.debug(job.toString());

        log.debug("Adding Job to Intake Queue");
        IntakeQueue.getInstance().addJob(job);
        log.info(String.format("Job %s has been queued for processing", job.getJobID()));

        return Response
                .status(Response.Status.CREATED)
                .entity(job.asJson())
                .build();
    }
}
