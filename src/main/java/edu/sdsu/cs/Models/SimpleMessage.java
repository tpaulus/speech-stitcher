package edu.sdsu.cs.Models;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;

/**
 * @author Tom Paulus
 * Created on 1/22/18.
 */
@SuppressWarnings("unused")
@AllArgsConstructor
public class SimpleMessage {
    private String type;
    private String message;

    public String asJson() {
        return new Gson().toJson(this);
    }
}