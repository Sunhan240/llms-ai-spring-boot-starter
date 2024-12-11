package com.eastrobot.arch.llms.chat.openai.tool;

import com.eastrobot.arch.llms.chat.openai.model.ChatCompletionFunction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * The relevant tool call.
 *
 * @param id       The ID of the tool call. This ID must be referenced when you submit the tool outputs in using the
 *                 Submit tool outputs to run endpoint.
 * @param type     The type of tool call the output is required for. For now, this is always function.
 * @param function The function definition.
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 15:18
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolCall {
    @JsonProperty("id")
    String id;
    @JsonProperty("type")
    String type;
    @JsonProperty("function")
    ChatCompletionFunction function;
}
