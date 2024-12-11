package com.eastrobot.arch.llms.chat.openai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Usage statistics for the completion request.
 *
 * @param completionTokens Number of tokens in the generated completion. Only applicable for completion requests.
 * @param promptTokens     Number of tokens in the prompt.
 * @param totalTokens      Total number of tokens used in the request (prompt + completion).
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 15:39
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Usage {
    @JsonProperty("completion_tokens") Integer completionTokens;
    @JsonProperty("prompt_tokens") Integer promptTokens;
    @JsonProperty("total_tokens") Integer totalTokens;
}
