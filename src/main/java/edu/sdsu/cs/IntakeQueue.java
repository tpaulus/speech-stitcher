package edu.sdsu.cs;

import edu.sdsu.cs.Models.StitchJob;
import lombok.extern.log4j.Log4j;

import java.util.LinkedList;
import java.util.Queue;

/**
 * FIFO Queue to manage the processing of jobs submitted by users. When a user posts a new job to the Generate API
 * Endpoint, a new StitchJob is created and pushed to the JobStore. The ID of the job to process is then added to
 * the processing queue (this class). From there, it is processed by the IntakeQueueWorker.
 *
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
public class IntakeQueue {
    private static IntakeQueue queueInstance = new IntakeQueue();
    private Queue<String> jobQueue;

    private IntakeQueue() {
        jobQueue = new LinkedList<>();
    }

    public static IntakeQueue getInstance() {
        return queueInstance;
    }

    public void addJob(StitchJob job) {
        JobStore.getInstance().putJob(job);
        jobQueue.add(job.getJobID());
        log.debug(String.format("Added Job %s to the Intake Queue", job.getJobID()));
    }

    public StitchJob getNextJob() {
        String jobId = jobQueue.poll();
        if (jobId == null) {
            // Queue is Empty
            return null;
        } else {
            log.debug(String.format("Popped Job %s off the Queue for Intake Processing", jobId));
            return JobStore.getInstance().getJob(jobId);
        }
    }

    public boolean isEmpty() {
        return jobQueue.isEmpty();
    }

    public int size() {
        return jobQueue.size();
    }
}
