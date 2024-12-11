package com.eastrobot.arch.llms.chat.openai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An array of content parts with a defined type.
 * Each MediaContent can be of either "text" or "image_url" type. Not both.
 *
 * @param type     Content  type, each can be of type text or image_url.
 * @param text     The text content of the message.
 * @param imageUrl The image content of the message. You can pass multiple
 *                 images by adding multiple image_url content parts. Image input is only
 *                 supported when using the gpt-4-visual-preview model.
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/24 10:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaContent {

    @JsonProperty("type") String type;
    @JsonProperty("text") String text;
    @JsonProperty("image_url") ImageUrl imageUrl;

    /**
     * Shortcut constructor for a text content.
     * @param text The text content of the message.
     */
    public MediaContent(String text) {
        this("text", text, null);
    }

    /**
     * Shortcut constructor for an image content.
     * @param imageUrl The image content of the message.
     */
    public MediaContent(ImageUrl imageUrl) {
        this("image_url", null, imageUrl);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImageUrl {

        @JsonProperty("url") String url;
        @JsonProperty("detail") String detail;

        public ImageUrl(String url) {
            this(url, null);
        }
    }


}
