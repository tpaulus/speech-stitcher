package edu.sdsu.cs;

import edu.sdsu.cs.Models.StitchJob;
import lombok.Getter;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Store Stitch Jobs in a Memory Backed MapDB.
 * Docs for MapDB can be found at: https://jankotek.gitbooks.io/mapdb/content/
 *
 * @author Tom Paulus
 * Created on 2/28/18.
 */

@SuppressWarnings("unused")
public class JobStore {
    @Getter
    private static final int JOB_TTL = 36;
    private static JobStore instance = new JobStore();

    private DB db;
    private HTreeMap<String, StitchJob> map;

    @SuppressWarnings("unchecked")
    private JobStore() {
        ScheduledExecutorService executor =
                Executors.newScheduledThreadPool(1);

        db = DBMaker
                .memoryDB()
                .make();
        map = (HTreeMap<String, StitchJob>) db
                .hashMap("jobs")
                .expireAfterUpdate(JOB_TTL, TimeUnit.HOURS)
                .expireExecutor(executor)
                .expireExecutorPeriod(300000)  // Run every 5 minutes
                .createOrOpen();
    }

    public static JobStore getInstance() {
        return instance;
    }

    public StitchJob getJob(final String id) {
        return map.get(id);
    }

    public void putJob(final StitchJob job) {
        map.put(job.getJobID(), job);
        db.commit();
    }

    public StitchJob removeJob(final String id) {
        StitchJob stitchJob = map.remove(id);
        db.commit();
        return stitchJob;
    }
}
