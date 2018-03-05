package edu.sdsu.cs.Workers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.gson.Gson;
import edu.sdsu.cs.JobStore;
import edu.sdsu.cs.Models.JobStatusNotification;
import edu.sdsu.cs.Models.StitchJob;
import edu.sdsu.cs.Transcode.Credentials;
import lombok.extern.log4j.Log4j;
import org.quartz.*;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static edu.sdsu.cs.Transcode.ETJob.METADATA_INTERNAL_KEY_NAME;
import static edu.sdsu.cs.Transcode.Credentials.CONFIG_FILE_NAME;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Pull notifications from the SQS Queue which are sent by the Elastic Transcoder Service whenever a change in status
 * for a Job Occurs.
 *
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Log4j
public class ETUpdateQueueWorker implements Job {
    private final String queueUrl;
    private static final int MAX_NUMBER_OF_MESSAGES = 5;
    private static final int VISIBILITY_TIMEOUT = 15;

    private AmazonSQS amazonSqs;

    public ETUpdateQueueWorker() {
        try {
            Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME));
            queueUrl = properties.getProperty("sqs.url");
        } catch (IOException e) {
            log.error("Could not open Pipeline Config - unable to create pipeline");
            throw new RuntimeException("Could not open Pipeline Config", e);
        }

        amazonSqs = AmazonSQSClientBuilder
                .standard()
                .withCredentials(Credentials.getCredentials())
                .build();
    }

    /**
     * Schedule the Queue Worker Job
     *
     * @param scheduler         {@link Scheduler} Quartz Scheduler Instance
     * @param intervalInSeconds How often the job should run in Seconds
     * @throws SchedulerException Something went wrong scheduling the job
     */
    public static void schedule(Scheduler scheduler, int intervalInSeconds) throws SchedulerException {
        JobDetail job = newJob(ETUpdateQueueWorker.class)
                .withIdentity("ETUpdateQueueWorker", "QueueWorkers")
                .build();

        // Trigger the job to run now, and then repeat every X Seconds
        Trigger trigger = newTrigger()
                .withIdentity("ETUpdateQueueWorker", "QueueWorkers")
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
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
                .withVisibilityTimeout(VISIBILITY_TIMEOUT)
                .withWaitTimeSeconds(0);
        List<Message> messages = amazonSqs.receiveMessage(receiveMessageRequest).getMessages();
        log.debug("ReceiveMessageRequest - " + amazonSqs.getCachedResponseMetadata(receiveMessageRequest));

        if (messages == null || messages.isEmpty()) {
            log.debug("There are currently no messages in the ET Notification Queue");
            return;
        }

        Gson gson = new Gson();
        for (Message message : messages) {
            // Parse the message
            JobStatusNotification notification = gson.fromJson(message.getBody(), JobStatusNotification.class);

            // Find and update the job in the Job Store
            String notification_job_id = notification.getUserMetadata().get(METADATA_INTERNAL_KEY_NAME);
            log.debug("Received ETS Status Update for Job - " + notification_job_id);
            log.debug(notification.getMessageDetails());
            StitchJob stitchJob = JobStore.getInstance().getJob(notification_job_id);
            if (stitchJob == null) {
                log.warn(String.format("No Job with id %s was found even though an SNS notification was sent. This is only a problem" +
                        "if the server hasn't been restarted in a while, as it means that jobs have been lost", notification_job_id));
                throw new RuntimeException("Job could not be found");
            }

            switch (notification.getState()){
                case PROGRESSING:
                    updateJobStatus(stitchJob, StitchJob.TranscodeStatus.IN_PROGRESS);
                    break;
                case COMPLETED:
                    updateJobStatus(stitchJob, StitchJob.TranscodeStatus.COMPLETED);
                    break;
                case ERROR:
                    updateJobStatus(stitchJob, StitchJob.TranscodeStatus.ERROR);
                    break;
            }

            // Update Job in JobStore
            JobStore.getInstance().putJob(stitchJob);

            // Delete the message from the queue.
            DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withReceiptHandle(message.getReceiptHandle());
            amazonSqs.deleteMessage(deleteMessageRequest);
            log.debug("Deleted Message from Queue - " + message);
            log.debug("DeleteMessageRequest - " + amazonSqs.getCachedResponseMetadata(receiveMessageRequest));
        }
    }
}
