package com.eastrobot.arch.llms.chat.openai.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The reason the model stopped generating tokens.
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 15:32
 */
public enum ChatCompletionFinishReason {
    /**
     * The model hit a natural stop point or a provided stop sequence.
     */
    @JsonProperty("stop") STOP,
    /**
     * The maximum number of tokens specified in the request was reached.
     */
    @JsonProperty("length") LENGTH,
    /**
     * The content was omitted due to a flag from our content filters.
     */
    @JsonProperty("content_filter") CONTENT_FILTER,
    /**
     * The model called a tool.
     */
    @JsonProperty("tool_calls") TOOL_CALLS,
    /**
     * (deprecated) The model called a function.
     */
    @JsonProperty("function_call") FUNCTION_CALL,
    /**
     * Only for compatibility with Mistral AI API.
     */
    @JsonProperty("tool_call") TOOL_CALL
}
