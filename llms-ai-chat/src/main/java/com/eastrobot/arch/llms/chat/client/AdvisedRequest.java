package com.eastrobot.arch.llms.chat.client;

import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.model.ChatModel;
import com.eastrobot.arch.llms.chat.prompt.ChatOptions;
import com.eastrobot.arch.llms.model.Media;
import com.eastrobot.arch.llms.retry.RetryPolicy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/23 14:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvisedRequest {
    private ChatModel chatModel;
    private String userText;
    private String systemText;
    private ChatOptions chatOptions;
    private List<Media> media;
    //    private List<String> functionNames;
    //    List<FunctionCallback> functionCallbacks;
    private List<Message> messages;
    private Map<String, Object> userParams;
    private Map<String, Object> systemParams;
    private List<RequestResponseAdvisor> advisors;
    private Map<String, Object> advisorParams;
    private RetryPolicy retryPolicy;

    public static Builder from(AdvisedRequest from) {
        Builder builder = new Builder();
        builder.chatModel = from.chatModel;
        builder.userText = from.userText;
        builder.systemText = from.systemText;
        builder.chatOptions = from.chatOptions;
        builder.media = from.media;
//        builder.functionNames = from.functionNames;
//        builder.functionCallbacks = from.functionCallbacks;
        builder.messages = from.messages;
        builder.userParams = from.userParams;
        builder.systemParams = from.systemParams;
        builder.advisors = from.advisors;
        builder.advisorParams = from.advisorParams;
        builder.retryPolicy = from.retryPolicy;
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {

        private ChatModel chatModel;

        private String userText = "";

        private String systemText = "";

        private ChatOptions chatOptions = null;

        private List<Media> media = Collections.emptyList();

//        private List<String> functionNames = List.of();

//        private List<FunctionCallback> functionCallbacks = List.of();

        private List<Message> messages = Collections.emptyList();

        private Map<String, Object> userParams = Collections.emptyMap();

        private Map<String, Object> systemParams = Collections.emptyMap();

        private List<RequestResponseAdvisor> advisors = Collections.emptyList();

        private Map<String, Object> advisorParams = Collections.emptyMap();
        /**
         * register retry action
         */
        private RetryPolicy retryPolicy;

        public Builder withChatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public Builder withUserText(String userText) {
            this.userText = userText;
            return this;
        }

        public Builder withSystemText(String systemText) {
            this.systemText = systemText;
            return this;
        }

        public Builder withChatOptions(ChatOptions chatOptions) {
            this.chatOptions = chatOptions;
            return this;
        }

        public Builder withMedia(List<Media> media) {
            this.media = media;
            return this;
        }

        /*public Builder withFunctionNames(List<String> functionNames) {
            this.functionNames = functionNames;
            return this;
        }

        public Builder withFunctionCallbacks(List<FunctionCallback> functionCallbacks) {
            this.functionCallbacks = functionCallbacks;
            return this;
        }*/

        public Builder withMessages(List<Message> messages) {
            this.messages = messages;
            return this;
        }

        public Builder withUserParams(Map<String, Object> userParams) {
            this.userParams = userParams;
            return this;
        }

        public Builder withSystemParams(Map<String, Object> systemParams) {
            this.systemParams = systemParams;
            return this;
        }

        public Builder withAdvisors(List<RequestResponseAdvisor> advisors) {
            this.advisors = advisors;
            return this;
        }

        public Builder withAdvisorParams(Map<String, Object> advisorParams) {
            this.advisorParams = advisorParams;
            return this;
        }

        public Builder withRetryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }


        public AdvisedRequest build() {
            return new AdvisedRequest(chatModel, this.userText, this.systemText, this.chatOptions, this.media,
                    /*this.functionNames, this.functionCallbacks,*/ this.messages, this.userParams, this.systemParams,
                    this.advisors, this.advisorParams, this.retryPolicy);
        }

    }
}
