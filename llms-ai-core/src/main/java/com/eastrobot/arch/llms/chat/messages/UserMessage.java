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

import com.eastrobot.arch.llms.model.Media;
import com.eastrobot.arch.llms.model.MediaContent;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.util.*;

/**
 * A message of the type 'user' passed as input Messages with the user role are from the
 * end-user or developer. They represent questions, prompts, or any input that you want
 * the generative to respond to.
 *
 * @author han.sun
 */
public class UserMessage extends AbstractMessage implements MediaContent {

    protected List<Media> media;

    public UserMessage(String message) {
        super(MessageType.USER, message);
    }

    public UserMessage(Resource resource) {
        super(MessageType.USER, resource);
        this.media = new ArrayList<>();
    }

    public UserMessage(String textContent, List<Media> media) {
        this(MessageType.USER, textContent, media, Collections.emptyMap());
    }

    public UserMessage(String textContent, Media... media) {
        this(textContent, Arrays.asList(media));
    }

    public UserMessage(String textContent, Collection<Media> mediaList, Map<String, Object> metadata) {
        this(MessageType.USER, textContent, mediaList, metadata);
    }


    public UserMessage(MessageType messageType, String textContent, Collection<Media> media,
                       Map<String, Object> metadata) {
        super(messageType, textContent, metadata);
        Assert.notNull(media, "media data must not be null");
        this.media = new ArrayList<>(media);
    }

    public UserMessage(String textContent, Map<String, Object> metadata) {
        super(MessageType.USER, textContent, metadata);
    }

    @Override
    public String toString() {
        return "UserMessage{" + "content='" + getContent() + '\'' + ", properties=" + metadata + ", messageType="
                + messageType + '}';
    }

    @Override
    public Collection<Media> getMedia() {
        return this.media;
    }

    /*public List<Media> getMedia(String... dummy) {
        return this.media;
    }*/

}
