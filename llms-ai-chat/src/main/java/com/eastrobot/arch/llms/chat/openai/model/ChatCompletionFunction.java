package com.eastrobot.arch.llms.chat.openai.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * The function definition.
 *
 * @param name      The name of the function.
 * @param arguments The arguments that the model expects you to pass to the function.
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 15:16
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionFunction {
    @JsonProperty("name")
    String name;
    @JsonProperty("arguments")
    String arguments;
}
