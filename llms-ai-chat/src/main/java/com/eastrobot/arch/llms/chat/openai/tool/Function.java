package com.eastrobot.arch.llms.chat.openai.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Function definition.
 *
 * @param description A description of what the function does, used by the model to choose when and how to call
 * the function.
 * @param name The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes,
 * with a maximum length of 64.
 * @param parameters The parameters the functions accepts, described as a JSON Schema object. To describe a
 * function that accepts no parameters, provide the value {"type": "object", "properties": {}}.
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 15:20
 */
@Data
public class Function {
    @JsonProperty("description") String description;
    @JsonProperty("name") String name;
    @JsonProperty("parameters") Map<String, Object> parameters;
}
