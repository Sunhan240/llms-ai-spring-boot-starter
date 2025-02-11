package com.eastrobot.arch.llms.chat.openai.model;

import com.eastrobot.arch.llms.chat.openai.tool.FunctionTool;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Creates a model response for the given chat conversation.
 *
 * @param messages         A list of messages comprising the conversation so far.
 * @param model            ID of the model to use.
 * @param frequencyPenalty Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing
 *                         frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.
 * @param logitBias        Modify the likelihood of specified tokens appearing in the completion. Accepts a JSON object
 *                         that maps tokens (specified by their token ID in the tokenizer) to an associated bias value from -100 to 100.
 *                         Mathematically, the bias is added to the logits generated by the model prior to sampling. The exact effect will
 *                         vary per model, but values between -1 and 1 should decrease or increase likelihood of selection; values like -100
 *                         or 100 should result in a ban or exclusive selection of the relevant token.
 * @param logprobs         Whether to return log probabilities of the output tokens or not. If true, returns the log
 *                         probabilities of each output token returned in the 'content' of 'message'.
 * @param topLogprobs      An integer between 0 and 5 specifying the number of most likely tokens to return at each token
 *                         position, each with an associated log probability. 'logprobs' must be set to 'true' if this parameter is used.
 * @param maxTokens        The maximum number of tokens to generate in the chat completion. The total length of input
 *                         tokens and generated tokens is limited by the model's context length.
 * @param n                How many chat completion choices to generate for each input message. Note that you will be charged based
 *                         on the number of generated tokens across all the choices. Keep n as 1 to minimize costs.
 * @param presencePenalty  Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they
 *                         appear in the text so far, increasing the model's likelihood to talk about new topics.
 * @param responseFormat   An object specifying the format that the model must output. Setting to { "type":
 *                         "json_object" } enables JSON mode, which guarantees the message the model generates is valid JSON.
 * @param seed             This feature is in Beta. If specified, our system will make a best effort to sample
 *                         deterministically, such that repeated requests with the same seed and parameters should return the same result.
 *                         Determinism is not guaranteed, and you should refer to the system_fingerprint response parameter to monitor
 *                         changes in the backend.
 * @param stop             Up to 4 sequences where the API will stop generating further tokens.
 * @param stream           If set, partial message deltas will be sent.Tokens will be sent as data-only server-sent events as
 *                         they become available, with the stream terminated by a data: [DONE] message.
 * @param streamOptions    Options for streaming response. Only set this when you set.
 * @param temperature      What sampling temperature to use, between 0 and 1. Higher values like 0.8 will make the output
 *                         more random, while lower values like 0.2 will make it more focused and deterministic. We generally recommend
 *                         altering this or top_p but not both.
 * @param topP             An alternative to sampling with temperature, called nucleus sampling, where the model considers the
 *                         results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10%
 *                         probability mass are considered. We generally recommend altering this or temperature but not both.
 * @param topK             Only sample from the top K options for each subsequent token. Used to
 *                         remove "long tail" low probability responses. Learn more technical details here.
 *                         Recommended for advanced use cases only. You usually only need to use temperature.
 * @param tools            A list of tools the model may call. Currently, only functions are supported as a tool. Use this to
 *                         provide a list of functions the model may generate JSON inputs for.
 * @param toolChoice       Controls which (if any) function is called by the model. none means the model will not call a
 *                         function and instead generates a message. auto means the model can pick between generating a message or calling a
 *                         function. Specifying a particular function via {"type: "function", "function": {"name": "my_function"}} forces
 *                         the model to call that function. none is the default when no functions are present. auto is the default if
 *                         functions are present. Use the {@link ToolChoiceBuilder} to create the tool choice value.
 * @param user             A unique identifier representing your end-user, which can help OpenAI to monitor and detect abuse.
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 14:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {

    @JsonProperty("messages") List<ChatCompletionMessage> messages;
    @JsonProperty("model") String model;
    @JsonProperty("frequency_penalty") Float frequencyPenalty;
    @JsonProperty("logit_bias") Map<String, Integer> logitBias;
    @JsonProperty("logprobs") Boolean logprobs;
    @JsonProperty("top_logprobs") Integer topLogprobs;
    @JsonProperty("max_tokens") Integer maxTokens;
    @JsonProperty("n") Integer n;
    @JsonProperty("presence_penalty") Float presencePenalty;
    @JsonProperty("response_format") ResponseFormat responseFormat;
    @JsonProperty("seed") Integer seed;
    @JsonProperty("stop") List<String> stop;
    @JsonProperty("stream") Boolean stream;
    @JsonProperty("stream_options") StreamOptions streamOptions;
    @JsonProperty("temperature") Float temperature;
    @JsonProperty("top_p") Float topP;
    @JsonProperty("top_k") Integer topK;
    @JsonProperty("tools") List<FunctionTool> tools;
    @JsonProperty("tool_choice") Object toolChoice;
    @JsonProperty("user") String user;
    /**
     * Shortcut constructor for a chat completion request with the given messages for streaming.
     *
     * @param messages A list of messages comprising the conversation so far.
     * @param stream   If set, partial message deltas will be sent.Tokens will be sent as data-only server-sent events
     *                 as they become available, with the stream terminated by a data: [DONE] message.
     */
    public ChatCompletionRequest(List<ChatCompletionMessage> messages, Boolean stream) {
        this(messages, null, null, null, null, null, null, null, null,
                null, null, null, stream, null, null, null, null,
                null, null, null);
    }

    /**
     * Shortcut constructor for a chat completion request with the given messages, model and temperature.
     *
     * @param messages    A list of messages comprising the conversation so far.
     * @param model       ID of the model to use.
     * @param temperature What sampling temperature to use, between 0 and 1.
     */
    public ChatCompletionRequest(List<ChatCompletionMessage> messages, String model, Float temperature) {
        this(messages, model, null, null, null, null, null, null, null,
                null, null, null, false, null, temperature, null, null,
                null, null, null);
    }
    public ChatCompletionRequest withStreamOptions(StreamOptions streamOptions) {
        return new ChatCompletionRequest(messages, model, frequencyPenalty, logitBias, logprobs, topLogprobs, maxTokens, n, presencePenalty,
                responseFormat, seed, stop, stream, streamOptions, temperature, topP, topK,
                tools, toolChoice, user);
    }
}
