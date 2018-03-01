package edu.sdsu.cs;

import edu.sdsu.cs.Models.StitchJob;
import lombok.extern.log4j.Log4j;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
public class IntakeQueue {
    private static IntakeQueue queueInstance = new IntakeQueue();
    private Queue<String> jobQueue;

    public static IntakeQueue getInstance() {
        return queueInstance;
    }

    private IntakeQueue() {
        jobQueue = new LinkedList<>();
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
}
