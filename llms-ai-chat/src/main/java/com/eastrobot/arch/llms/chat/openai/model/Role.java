package com.eastrobot.arch.llms.chat.openai.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The role of the author of this message.
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 15:15
 */
public enum Role {
    /**
     * System message.
     */
    @JsonProperty("system") SYSTEM,
    /**
     * User message.
     */
    @JsonProperty("user") USER,
    /**
     * Assistant message.
     */
    @JsonProperty("assistant") ASSISTANT,
    /**
     * Tool message.
     */
    @JsonProperty("tool") TOOL
}

