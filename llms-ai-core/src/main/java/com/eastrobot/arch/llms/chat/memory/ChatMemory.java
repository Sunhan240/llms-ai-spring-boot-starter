/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eastrobot.arch.llms.chat.memory;

import com.eastrobot.arch.llms.chat.messages.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * The ChatMemory interface represents a storage for chat conversation history. It
 * provides methods to add messages to a conversation, retrieve messages from a
 * conversation, and clear the conversation history.
 *
 * @author sunhan
 */
public interface ChatMemory {

    default void add(String conversationId, Message message) {
        this.add(conversationId, new ArrayList<Message>() {{
            add(message);
        }});
    }

    void add(String conversationId, List<Message> messages);

    List<Message> get(String conversationId, int lastN);

    void clear(String conversationId);

    default void remove(String conversationId, int lastN) {
        List<Message> all = get(conversationId, -1);
        if (all != null && !all.isEmpty()) {
            int size = all.size();
            if (size > lastN) {
                all = all.subList(0, size - lastN);
                add(conversationId, all);
            } else {
                clear(conversationId);
            }
        }
    }
}