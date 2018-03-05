package edu.sdsu.cs.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import edu.sdsu.cs.Transcode.Utterances;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * POJO Model for a Word Utterance as it is saved in the DynamoDB Table
 *
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

    @Getter(AccessLevel.NONE)
    @SerializedName("next")
    @Expose
    public Word nextWord;

    public Word getNextWord(final String tableName) {
        if (nextWord != null)
            return Utterances.getWordByID(tableName, nextWord.id);
        else
            return null;
    }
}
