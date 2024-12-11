/*
 * Copyright 2023 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eastrobot.arch.llms.chat.openai;

import com.eastrobot.arch.llms.chat.openai.model.ResponseFormat;
import com.eastrobot.arch.llms.chat.openai.model.StreamOptions;
import com.eastrobot.arch.llms.chat.prompt.ChatOptions;
import com.eastrobot.arch.llms.model.ModelOptionsUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Christian Tzolov
 * @author Mariusz Bernacki
 * @author Thomas Vitale
 * @since 0.8.0
 */
@Data
@JsonInclude(Include.NON_NULL)
public class OpenAiChatOptions implements ChatOptions {

    // @formatter:off
    /**
     * ID of the model to use.
     */
    private @JsonProperty("model") String model;
    /**
     * Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing
     * frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.
     */
    private @JsonProperty("frequency_penalty") Float frequencyPenalty;
    /**
     * Modify the likelihood of specified tokens appearing in the completion. Accepts a JSON object
     * that maps tokens (specified by their token ID in the tokenizer) to an associated bias value from -100 to 100.
     * Mathematically, the bias is added to the logits generated by the model prior to sampling. The exact effect will
     * vary per model, but values between -1 and 1 should decrease or increase likelihood of selection; values like -100
     * or 100 should result in a ban or exclusive selection of the relevant token.
     */
    private @JsonProperty("logit_bias") Map<String, Integer> logitBias;
    /**
     * Whether to return log probabilities of the output tokens or not. If true, returns the log probabilities
     * of each output token returned in the 'content' of 'message'.
     */
    private @JsonProperty("logprobs") Boolean logprobs;
    /**
     * An integer between 0 and 5 specifying the number of most likely tokens to return at each token position,
     * each with an associated log probability. 'logprobs' must be set to 'true' if this parameter is used.
     */
    private @JsonProperty("top_logprobs") Integer topLogprobs;
    /**
     * The maximum number of tokens to generate in the chat completion. The total length of input
     * tokens and generated tokens is limited by the model's context length.
     */
    private @JsonProperty("max_tokens") Integer maxTokens;
    /**
     * How many chat completion choices to generate for each input message. Note that you will be charged based
     * on the number of generated tokens across all of the choices. Keep n as 1 to minimize costs.
     */
    private @JsonProperty("n") Integer n;
    /**
     * Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they
     * appear in the text so far, increasing the model's likelihood to talk about new topics.
     */
    private @JsonProperty("presence_penalty") Float presencePenalty;
    /**
     * An object specifying the format that the model must output. Setting to { "type":
     * "json_object" } enables JSON mode, which guarantees the message the model generates is valid JSON.
     */
    private @JsonProperty("response_format") ResponseFormat responseFormat;
    /**
     * Options for streaming response. Included in the API only if streaming-mode completion is requested.
     */
    private @JsonProperty("stream_options") StreamOptions streamOptions;
    /**
     * This feature is in Beta. If specified, our system will make a best effort to sample
     * deterministically, such that repeated requests with the same seed and parameters should return the same result.
     * Determinism is not guaranteed, and you should refer to the system_fingerprint response parameter to monitor
     * changes in the backend.
     */
    private @JsonProperty("seed") Integer seed;
    /**
     * Up to 4 sequences where the API will stop generating further tokens.
     */
    @NestedConfigurationProperty
    private @JsonProperty("stop") List<String> stop;
    /**
     * What sampling temperature to use, between 0 and 1. Higher values like 0.8 will make the output
     * more random, while lower values like 0.2 will make it more focused and deterministic. We generally recommend
     * altering this or top_p but not both.
     */
    private @JsonProperty("temperature") Float temperature;
    /**
     * An alternative to sampling with temperature, called nucleus sampling, where the model considers the
     * results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10%
     * probability mass are considered. We generally recommend altering this or temperature but not both.
     */
    private @JsonProperty("top_p") Float topP;
    /**
     * Only sample from the top K options for each subsequent token. Used to
     * remove "long tail" low probability responses. Learn more technical details here.
     * Recommended for advanced use cases only. You usually only need to use temperature.
     */
    private @JsonProperty("top_k") Integer topK;
    /**
     * A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
     */
    private @JsonProperty("user") String user;

    private @JsonProperty("custom") Map<String, Object> custom;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected OpenAiChatOptions options;

        public Builder() {
            this.options = new OpenAiChatOptions();
        }

        public Builder withModel(String model) {
            this.options.model = model;
            return this;
        }

        public Builder withFrequencyPenalty(Float frequencyPenalty) {
            this.options.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder withLogitBias(Map<String, Integer> logitBias) {
            this.options.logitBias = logitBias;
            return this;
        }

        public Builder withLogprobs(Boolean logprobs) {
            this.options.logprobs = logprobs;
            return this;
        }

        public Builder withTopLogprobs(Integer topLogprobs) {
            this.options.topLogprobs = topLogprobs;
            return this;
        }

        public Builder withMaxTokens(Integer maxTokens) {
            this.options.maxTokens = maxTokens;
            return this;
        }

        public Builder withN(Integer n) {
            this.options.n = n;
            return this;
        }

        public Builder withPresencePenalty(Float presencePenalty) {
            this.options.presencePenalty = presencePenalty;
            return this;
        }

        public Builder withResponseFormat(ResponseFormat responseFormat) {
            this.options.responseFormat = responseFormat;
            return this;
        }

        public Builder withStreamUsage(boolean enableStreamUsage) {
            if (enableStreamUsage) {
                this.options.streamOptions = StreamOptions.INCLUDE_USAGE;
            }
            return this;
        }

        public Builder withSeed(Integer seed) {
            this.options.seed = seed;
            return this;
        }

        public Builder withStop(List<String> stop) {
            this.options.stop = stop;
            return this;
        }

        public Builder withTemperature(Float temperature) {
            this.options.temperature = temperature;
            return this;
        }

        public Builder withTopP(Float topP) {
            this.options.topP = topP;
            return this;
        }

        public Builder withTopK(Integer topK) {
            this.options.topK = topK;
            return this;
        }

        public Builder withUser(String user) {
            this.options.user = user;
            return this;
        }

        public Builder withCustom(Map<String, Object> custom) {
            this.options.custom = custom;
            return this;
        }

        public OpenAiChatOptions build() {
            return this.options;
        }

    }

    public Boolean getStreamUsage() {
        return this.streamOptions != null;
    }

    @JsonProperty("stream_usage")
    public void setStreamUsage(Boolean enableStreamUsage) {
        this.streamOptions = (enableStreamUsage) ? StreamOptions.INCLUDE_USAGE : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.model, this.frequencyPenalty, this.logitBias, this.logprobs, this.topLogprobs,
                this.maxTokens, this.n, this.presencePenalty, this.responseFormat, this.streamOptions, this.seed,
                this.stop, this.temperature, this.topP, this.topK, this.user, this.custom);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OpenAiChatOptions other = (OpenAiChatOptions) o;
        return Objects.equals(this.model, other.model) && Objects.equals(this.frequencyPenalty, other.frequencyPenalty)
                && Objects.equals(this.logitBias, other.logitBias) && Objects.equals(this.logprobs, other.logprobs)
                && Objects.equals(this.topLogprobs, other.topLogprobs)
                && Objects.equals(this.maxTokens, other.maxTokens) && Objects.equals(this.n, other.n)
                && Objects.equals(this.presencePenalty, other.presencePenalty)
                && Objects.equals(this.responseFormat, other.responseFormat)
                && Objects.equals(this.streamOptions, other.streamOptions) && Objects.equals(this.seed, other.seed)
                && Objects.equals(this.stop, other.stop) && Objects.equals(this.temperature, other.temperature)
                && Objects.equals(this.topP, other.topP) && Objects.equals(this.topK, other.topK)
                && Objects.equals(this.user, other.user) && Objects.equals(this.custom, other.custom);
    }

    @Override
    public OpenAiChatOptions copy() {
        return OpenAiChatOptions.fromOptions(this);
    }

    public static OpenAiChatOptions fromOptions(OpenAiChatOptions fromOptions) {
        return OpenAiChatOptions.builder()
                .withModel(fromOptions.getModel())
                .withFrequencyPenalty(fromOptions.getFrequencyPenalty())
                .withLogitBias(fromOptions.getLogitBias())
                .withLogprobs(fromOptions.getLogprobs())
                .withTopLogprobs(fromOptions.getTopLogprobs())
                .withMaxTokens(fromOptions.getMaxTokens())
                .withN(fromOptions.getN())
                .withPresencePenalty(fromOptions.getPresencePenalty())
                .withResponseFormat(fromOptions.getResponseFormat())
                .withStreamUsage(fromOptions.getStreamUsage())
                .withSeed(fromOptions.getSeed())
                .withStop(fromOptions.getStop())
                .withTemperature(fromOptions.getTemperature())
                .withTopP(fromOptions.getTopP())
                .withTopK(fromOptions.getTopK())
                .withUser(fromOptions.getUser())
                .withCustom(fromOptions.getCustom())
                .build();
    }

    @Override
    public String toString() {
        return "OpenAiChatOptions: " + ModelOptionsUtils.toJsonString(this);
    }

}
