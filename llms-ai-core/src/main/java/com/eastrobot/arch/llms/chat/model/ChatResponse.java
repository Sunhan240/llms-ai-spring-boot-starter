package com.eastrobot.arch.llms.chat.model;

import com.eastrobot.arch.llms.chat.metadata.ChatResponseMetadata;
import com.eastrobot.arch.llms.model.ModelResponse;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p>由AI提供者返回的聊天完成(例如generation)响应</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/12 17:01
 */
public class ChatResponse implements ModelResponse<Generation> {

    private final ChatResponseMetadata chatResponseMetadata;

    /**
     * List of generated messages returned by the AI provider.
     */
    private final List<Generation> generations;

    /**
     * Construct a new {@link ChatResponse} instance without metadata.
     *
     * @param generations the {@link List} of {@link Generation} returned by the AI provider.
     */
    public ChatResponse(List<Generation> generations) {
        this(generations, new ChatResponseMetadata());
    }

    /**
     * Construct a new {@link ChatResponse} instance.
     *
     * @param generations          the {@link List} of {@link Generation} returned by the AI provider.
     * @param chatResponseMetadata {@link ChatResponseMetadata} containing information
     *                             about the use of the AI provider's API.
     */
    public ChatResponse(List<Generation> generations, ChatResponseMetadata chatResponseMetadata) {
        this.chatResponseMetadata = chatResponseMetadata;
        this.generations = generations;
    }

    /**
     * The {@link List} of {@link Generation generated outputs}.
     * <p>
     * It is a {@link List} of {@link List lists} because the Prompt could request
     * multiple output {@link Generation generations}.
     *
     * @return the {@link List} of {@link Generation generated outputs}.
     */

    @Override
    public List<Generation> getResults() {
        return this.generations;
    }

    /**
     * @return Returns the first {@link Generation} in the generations list.
     */
    public Generation getResult() {
        if (CollectionUtils.isEmpty(this.generations)) {
            return null;
        }
        return this.generations.get(0);
    }

    /**
     * @return Returns {@link ChatResponseMetadata} containing information about the use
     * of the AI provider's API.
     */
    @Override
    public ChatResponseMetadata getMetadata() {
        return this.chatResponseMetadata;
    }

    @Override
    public String toString() {
        return "ChatResponse [metadata=" + chatResponseMetadata + ", generations=" + generations + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChatResponse))
            return false;
        ChatResponse that = (ChatResponse) o;
        return Objects.equals(chatResponseMetadata, that.chatResponseMetadata)
                && Objects.equals(generations, that.generations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatResponseMetadata, generations);
    }

    public static ChatResponse.Builder builder() {
        return new ChatResponse.Builder();
    }

    public static class Builder {

        private List<Generation> generations;

        private final ChatResponseMetadata.Builder chatResponseMetadataBuilder;

        private Builder() {
            this.chatResponseMetadataBuilder = ChatResponseMetadata.builder();
        }

        public Builder from(ChatResponse other) {
            this.generations = other.generations;
            Set<Map.Entry<String, Object>> entries = other.chatResponseMetadata.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                this.chatResponseMetadataBuilder.withKeyValue(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder withMetadata(String key, Object value) {
            this.chatResponseMetadataBuilder.withKeyValue(key, value);
            return this;
        }

        public Builder withGenerations(List<Generation> generations) {
            this.generations = generations;
            return this;

        }

        public ChatResponse build() {
            return new ChatResponse(generations, chatResponseMetadataBuilder.build());
        }

    }
}
