package edu.sdsu.cs.Workers;

import edu.sdsu.cs.IntakeQueue;
import edu.sdsu.cs.JobStore;
import edu.sdsu.cs.Models.StitchJob;
import edu.sdsu.cs.Transcode.ETJob;
import edu.sdsu.cs.Transcode.Utterances;
import lombok.extern.log4j.Log4j;
import org.quartz.*;

import java.util.Arrays;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
public class IntakeQueueWorker implements Job {
    /**
     * Schedule the Queue Worker Job
     *
     * @param scheduler         {@link Scheduler} Quartz Scheduler Instance
     * @param intervalInSeconds How often the job should run in Seconds
     * @throws SchedulerException Something went wrong scheduling the job
     */
    public static void schedule(Scheduler scheduler, int intervalInSeconds) throws SchedulerException {
        JobDetail job = newJob(IntakeQueueWorker.class)
                .withIdentity("IntakeQueueWorker", "QueueWorkers")
                .build();

        // Trigger the job to run now, and then repeat every X Seconds
        Trigger trigger = newTrigger()
                .withIdentity("IntakeQueueWorker", "QueueWorkers")
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(intervalInSeconds)
                        .repeatForever())
                .startNow()
                .build();

        // Tell quartz to schedule the job using our trigger
        scheduler.scheduleJob(job, trigger);
    }

    private static void updateJobStatus(StitchJob job, StitchJob.TranscodeStatus newStatus) {
        StitchJob.Status status = job.getStatus();
        status.setStatus(newStatus);
        job.updateStatus(status);
    }

    @SuppressWarnings("RedundantThrows")
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (IntakeQueue.getInstance().isEmpty())
            log.debug("There are currently no jobs on the Intake Queue");

        while (!IntakeQueue.getInstance().isEmpty()) {
            StitchJob job = IntakeQueue.getInstance().getNextJob();
            log.info(String.format("Pulled Job %s off the Queue for Intake Processing", job.getJobID()));
            log.debug(job.toString());
            updateJobStatus(job, StitchJob.TranscodeStatus.PROCESSING);

            final String tableName = "ss-" + job.getSpeaker().toLowerCase();
            List<String> words = Arrays.asList(job.getBody().split("\\s"));  // Split on White Space

            while (!words.isEmpty()) {
                try {
                    Utterances.FindResult result = Utterances.getBestWordSequence(tableName, words);
                    log.debug(String.format("Matched Words - [%s]", result.getWordsMatched()));
                    log.debug(String.format("Added Clip to Job %s - %s", job.getJobID(), result.getClip().toString()));

                    job.getClips().add(result.getClip());
                    // Remove Matched words from List
                    for (String matchedWord : result.getWordsMatched()) {
                        if (matchedWord.equals(words.get(0)))
                            words.remove(0);
                    }
                } catch (Utterances.UtteranceNotFoundException e) {
                    log.warn(String.format("Could not find \"%s\" in utterance table", words.get(0)), e);
                    updateJobStatus(job, StitchJob.TranscodeStatus.ERROR);
                    break;
                }
            }

            ETJob etJob = new ETJob(job);
            log.info("Pushing job to ETS");
            job.setETSJobID(etJob.pushJob());
            log.debug("ETS job has id - " + job.getETSJobID());

            updateJobStatus(job, StitchJob.TranscodeStatus.SUBMITTED);
            log.info(String.format("Finished Intake Processing for Job %s", job.getJobID()));
            log.debug(job.toString());

            // Update Job in JobStore
            JobStore.getInstance().putJob(job);
        }
    }
}
