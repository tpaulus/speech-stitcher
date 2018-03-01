package edu.sdsu.cs.Models;

import lombok.Data;

/**
 * @author Tom Paulus
 * Created on 2/28/18.
 */
@Data
public class SourceClip {
    private String sourceArn;
    private String startTime;
    private String duration;
}
