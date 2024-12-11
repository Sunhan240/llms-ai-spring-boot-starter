package com.eastrobot.arch.llms.chat.openai.tool;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a tool the model may call. Currently, only functions are supported as a tool.
 *
 * @param type     The type of the tool. Currently, only 'function' is supported.
 * @param function The function definition.
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 15:21
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FunctionTool {
    @JsonProperty("type")
    Type type;
    @JsonProperty("function")
    Function function;

    /**
     * Create a tool of type 'function' and the given function definition.
     */
    public enum Type {
        /**
         * Function tool type.
         */
        @JsonProperty("function") FUNCTION
    }
}
