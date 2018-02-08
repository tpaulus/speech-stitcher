package edu.sdsu.cs.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * @author Tom Paulus
 * Created on 1/22/18.
 */
@Data
public class Word {
    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("start")
    @Expose
    public String start;

    @SerializedName("end")
    @Expose
    public String end;

    @SerializedName("word")
    @Expose
    public String word;

    @SerializedName("source")
    @Expose
    public String source;

    @SerializedName("next")
    @Expose
    public Word nextWord;
}
