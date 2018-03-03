package edu.sdsu.cs.Transcode;

import com.amazonaws.services.elastictranscoder.model.*;
import edu.sdsu.cs.Models.SourceClip;
import edu.sdsu.cs.Models.StitchJob;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * A list of system presets can be found on the AWS ETS Documentation:
 * https://docs.aws.amazon.com/elastictranscoder/latest/developerguide/system-presets.html
 *
 * @author Tom Paulus
 * Created on 3/2/18.
 */
@Log4j
public class ETJob {
    public static final String METADATA_INTERNAL_KEY_NAME = "METADATA_INTERNAL_KEY_NAME";
    public static final String FILE_EXTENSION = ".mp4";
    private static final String OUTPUT_PRESET_ID = "1351620000001-000020"; // Generic 480p 16:9
    private CreateJobRequest createJobRequest;

    public ETJob(StitchJob stitchJob) {
        List<JobInput> inputList = new ArrayList<>();
        for (SourceClip clip : stitchJob.getClips()) {
            inputList.add(new JobInput()
                    .withKey(clip.getSourceArn())
                    .withTimeSpan(new TimeSpan()
                            .withStartTime(clip.getStartTime())
                            .withDuration(clip.getDuration())
                    )
            );
        }

        CreateJobOutput output = new CreateJobOutput()
                .withPresetId(OUTPUT_PRESET_ID)
                .withKey(stitchJob.getJobID() + FILE_EXTENSION);

        createJobRequest = new CreateJobRequest()
                .withPipelineId(ETPipeline.getInstance().getPipeline().getId())
                .withInputs(inputList)
                .withOutput(output)
                .withUserMetadata(new TreeMap<String, String>() {{
                    put(METADATA_INTERNAL_KEY_NAME, stitchJob.getJobID());
                }});
    }

    public String pushJob() {
        CreateJobResult result = ETPipeline.getInstance().elasticTranscoder.createJob(createJobRequest);
        log.debug("CreatePipelineRequest - " + ETPipeline.getInstance().elasticTranscoder.getCachedResponseMetadata(createJobRequest));

        return result.getJob().getId();
    }
}
