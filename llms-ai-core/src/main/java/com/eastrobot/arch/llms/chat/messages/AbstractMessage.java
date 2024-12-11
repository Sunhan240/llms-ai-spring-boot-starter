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
package com.eastrobot.arch.llms.chat.messages;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The AbstractMessage class is an abstract implementation of the Message interface. It
 * provides a base implementation for message content, media attachments, metadata, and
 * message type.
 *
 * @author han.sun
 * @see Message
 */
public abstract class AbstractMessage implements Message {
    public static final String MESSAGE_TYPE = "messageType";

    protected final MessageType messageType;

    protected String textContent;

//    protected final List<Media> media;

    /**
     * Additional options for the message to influence the response, not a generative map.
     */
    protected final Map<String, Object> metadata;

    public AbstractMessage(MessageType messageType, String content) {
        this(messageType, content, new HashMap<String, Object>() {{
            put(MESSAGE_TYPE, messageType);
        }});
    }

//    protected AbstractMessage(MessageType messageType, String content, Map<String, Object> metadata) {
//        Assert.notNull(messageType, "Message type must not be null");
//        this.messageType = messageType;
//        this.textContent = content;
////        this.media = new ArrayList<>();
//        this.metadata = new HashMap<>(metadata);
//        this.metadata.put(MESSAGE_TYPE, messageType);
//    }

//    protected AbstractMessage(MessageType messageType, String textContent) {
//        this(messageType, textContent, Map.of(MESSAGE_TYPE, messageType));
//    }

    public AbstractMessage(MessageType messageType, String textContent,
                              Map<String, Object> metadata) {

        Assert.notNull(messageType, "Message type must not be null");
        if (messageType == MessageType.SYSTEM || messageType == MessageType.USER) {
            Assert.notNull(textContent, "Content must not be null for SYSTEM or USER messages");
        }
//        Assert.notNull(textContent, "Content must not be null");
//        Assert.notNull(media, "media data must not be null");

        this.messageType = messageType;
        this.textContent = textContent;
//        this.media = new ArrayList<>(media);
        this.metadata = new HashMap<>(metadata);
        this.metadata.put(MESSAGE_TYPE, messageType);
    }

    public AbstractMessage(MessageType messageType, Resource resource) {
        this(messageType, resource, Collections.emptyMap());
    }

    @SuppressWarnings("null")
    public AbstractMessage(MessageType messageType, Resource resource, Map<String, Object> metadata) {
        Assert.notNull(messageType, "Message type must not be null");
        Assert.notNull(resource, "Resource must not be null");

        this.messageType = messageType;
        this.metadata = new HashMap<>(metadata);
        this.metadata.put(MESSAGE_TYPE, messageType);
//        this.media = new ArrayList<>();

        try (InputStream inputStream = resource.getInputStream()) {
            this.textContent = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read resource", ex);
        }
    }

    @Override
    public String getContent() {
        return this.textContent;
    }

//    @Override
    public void setContent(String textContent) {
         this.textContent = textContent;
    }

    /* @Override
     public List<Media> getMedia(String... dummy) {
         return this.media;
     }
 */
    @Override
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    @Override
    public MessageType getMessageType() {
        return this.messageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.messageType, this.textContent, this.metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractMessage other = (AbstractMessage) obj;
        return Objects.equals(this.messageType, other.messageType)
                && Objects.equals(this.textContent, other.textContent)
                && Objects.equals(this.metadata, other.metadata);
    }


}
