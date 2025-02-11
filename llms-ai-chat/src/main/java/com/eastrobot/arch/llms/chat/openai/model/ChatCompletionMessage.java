package com.eastrobot.arch.llms.chat.openai.model;

import com.eastrobot.arch.llms.chat.openai.tool.ToolCall;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>Message comprising the conversation.</p>
 *
 * @param rawContent The contents of the message. Can be either a {@link MediaContent} or a {@link String}.
 *                   The response message content is always a {@link String}.
 * @param role       The role of the messages author. Could be one of the {@link Role} types.
 * @param name       An optional name for the participant. Provides the model information to differentiate between
 *                   participants of the same role. In case of Function calling, the name is the function name that the message is
 *                   responding to.
 * @param toolCallId Tool call that this message is responding to. Only applicable for the {@link Role#TOOL} role
 *                   and null otherwise.
 * @param toolCalls  The tool calls generated by the model, such as function calls. Applicable only for
 *                   {@link Role#ASSISTANT} role and null otherwise.
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 15:12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionMessage {
    @JsonProperty("content") Object rawContent;
    @JsonProperty("role") Role role;
    @JsonProperty("name") String name;
    @JsonProperty("tool_call_id") String toolCallId;
    @JsonProperty("tool_calls") List<ToolCall> toolCalls;

    /**
     * Create a chat completion message with the given content and role. All other fields are null.
     *
     * @param content The contents of the message.
     * @param role    The role of the author of this message.
     */
    public ChatCompletionMessage(Object content, Role role) {
        this(content, role, null, null, null);
    }

    /**
     * Get message content as String.
     */
    public String content() {
        if (this.rawContent == null) {
            return null;
        }
        if (this.rawContent instanceof String) {
            return ((String) rawContent);
        }
        throw new IllegalStateException("The content is not a string!");
    }
}
