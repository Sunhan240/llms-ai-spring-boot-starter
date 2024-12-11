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
package com.eastrobot.arch.llms.chat.prompt;

import lombok.Data;

import java.util.Map;

public class ChatOptionsBuilder {

    @Data
    private static class DefaultChatOptions implements ChatOptions {

        private String model;
        private Float frequencyPenalty;
        private Integer maxTokens;
        private Float presencePenalty;
        private Float temperature;
        private Float topP;
        private Integer topK;
        private Map<String, Object> custom;

        @Override
        public ChatOptions copy() {
            return builder().withModel(this.model)
                    .withFrequencyPenalty(this.frequencyPenalty)
                    .withMaxTokens(this.maxTokens)
                    .withPresencePenalty(this.presencePenalty)
                    .withTemperature(this.temperature)
                    .withTopK(this.topK)
                    .withTopP(this.topP)
                    .withCustom(this.custom)
                    .build();
        }
    }

    private final DefaultChatOptions options = new DefaultChatOptions();

    private ChatOptionsBuilder() {
    }

    public static ChatOptionsBuilder builder() {
        return new ChatOptionsBuilder();
    }

    public ChatOptionsBuilder withModel(String model) {
        options.setModel(model);
        return this;
    }

    public ChatOptionsBuilder withFrequencyPenalty(Float frequencyPenalty) {
        options.setFrequencyPenalty(frequencyPenalty);
        return this;
    }

    public ChatOptionsBuilder withMaxTokens(Integer maxTokens) {
        options.setMaxTokens(maxTokens);
        return this;
    }

    public ChatOptionsBuilder withPresencePenalty(Float presencePenalty) {
        options.setPresencePenalty(presencePenalty);
        return this;
    }

    public ChatOptionsBuilder withTemperature(Float temperature) {
        options.setTemperature(temperature);
        return this;
    }

    public ChatOptionsBuilder withTopP(Float topP) {
        options.setTopP(topP);
        return this;
    }

    public ChatOptionsBuilder withTopK(Integer topK) {
        options.setTopK(topK);
        return this;
    }

    public ChatOptionsBuilder withCustom(Map<String, Object> custom) {
        options.setCustom(custom);
        return this;
    }

    public ChatOptions build() {
        return options;
    }

}