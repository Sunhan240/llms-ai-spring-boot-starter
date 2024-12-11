package com.eastrobot.arch.llms.chat.openai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Log probability information for the choice.
 *
 * @param content A list of message content tokens with log probability information.
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 15:41
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogProbs {

    @JsonProperty("content") List<Content> content;
    /**
     * Message content tokens with log probability information.
     *
     * @param token       The token.
     * @param logprob     The log probability of the token.
     * @param probBytes   A list of integers representing the UTF-8 bytes representation
     *                    of the token. Useful in instances where characters are represented by multiple
     *                    tokens and their byte representations must be combined to generate the correct
     *                    text representation. Can be null if there is no bytes representation for the token.
     * @param topLogprobs List of the most likely tokens and their log probability,
     *                    at this token position. In rare cases, there may be fewer than the number of
     *                    requested top_logprobs returned.
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content {
        @JsonProperty("token") String token;
        @JsonProperty("logprob") Float logprob;
        @JsonProperty("bytes") List<Integer> probBytes;
        @JsonProperty("top_logprobs") List<TopLogProbs> topLogprobs;

        /**
         * The most likely tokens and their log probability, at this token position.
         *
         * @param token     The token.
         * @param logprob   The log probability of the token.
         * @param probBytes A list of integers representing the UTF-8 bytes representation
         *                  of the token. Useful in instances where characters are represented by multiple
         *                  tokens and their byte representations must be combined to generate the correct
         *                  text representation. Can be null if there is no bytes representation for the token.
         */
        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class TopLogProbs {
            @JsonProperty("token") String token;
            @JsonProperty("logprob") Float logprob;
            @JsonProperty("bytes") List<Integer> probBytes;
        }
    }
}

