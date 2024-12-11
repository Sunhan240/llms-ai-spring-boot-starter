package com.eastrobot.arch.llms.chat.openai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An object specifying the format that the model must output.
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/9 11:28
 */
@Data
@NoArgsConstructor
public class ResponseFormat {
    /**
     * Must be one of 'text' or 'json_object'.
     */
    @JsonProperty("type")
    String type;

    public ResponseFormat(String type){
        this.type = type;
    }
}
