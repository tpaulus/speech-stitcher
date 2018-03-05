package edu.sdsu.cs.Models;

import lombok.Data;

/**
 * ETS Input Source Clip
 * Times should be stored with the following format:
 * HH:mm:ss.sss
 *
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Data
public class SourceClip {
    private String sourceArn;
    private String startTime;
    private String duration;
}
