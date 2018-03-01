package edu.sdsu.cs;

import edu.sdsu.cs.Transcode.ETPipeline;
import edu.sdsu.cs.Workers.IntakeQueueWorker;
import lombok.extern.log4j.Log4j;
import org.quartz.SchedulerException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Tom Paulus
 * Created on 1/22/18.
 */
@Log4j
public class Init implements ServletContextListener {
    private static final int QUEUE_WORKER_FREQUENCY = 5;

    @Override public void contextInitialized(ServletContextEvent sce) {
        log.info("Initializing Elastic Transcoder Pipeline");
        if (!ETPipeline.getInstance().findPipeline()) {
            log.info("No Pipeline Exists - Creating new Pipeline from Config");
            ETPipeline.getInstance().makePipeline();
        } else {
            log.info("Found exiting pipeline in ETS that matches specifications");
            log.info("Activating Pipeline");
            ETPipeline.getInstance().activate();
        }

        try {
            Schedule.getScheduler().clear();
            Schedule.getScheduler().standby();
        } catch (SchedulerException e) {
            log.error("Problem Starting Scheduler", e);
        }

        try {
            IntakeQueueWorker.schedule(Schedule.getScheduler(), QUEUE_WORKER_FREQUENCY);
        } catch (SchedulerException e) {
            log.error("Problem scheduling Intake Queue Worker", e);
        }
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        try {
            Schedule.getScheduler().shutdown(false);
        } catch (SchedulerException e) {
            log.error("Could not shutdown Scheduler!", e);
        }
    }
}
