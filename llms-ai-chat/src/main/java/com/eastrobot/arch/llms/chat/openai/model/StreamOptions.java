package com.eastrobot.arch.llms.chat.openai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * If set, an additional chunk will be streamed
 * before the data: [DONE] message. The usage field on this chunk
 * shows the token usage statistics for the entire request, and
 * the choices field will always be an empty array. All other chunks
 * will also include a usage field, but with a null value.
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/9 11:33
 */
@Data
public class StreamOptions {
    @JsonProperty("include_usage")
    Boolean includeUsage;
    public static StreamOptions INCLUDE_USAGE = new StreamOptions(true);

    public StreamOptions(boolean includeUsage) {
        this.includeUsage = includeUsage;
    }
}
