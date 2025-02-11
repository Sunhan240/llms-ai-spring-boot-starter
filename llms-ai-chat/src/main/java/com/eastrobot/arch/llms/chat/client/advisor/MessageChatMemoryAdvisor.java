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

package com.eastrobot.arch.llms.chat.client.advisor;

import com.eastrobot.arch.llms.chat.client.AdvisedRequest;
import com.eastrobot.arch.llms.chat.memory.ChatMemory;
import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.messages.UserMessage;
import com.eastrobot.arch.llms.chat.model.ChatResponse;
import com.eastrobot.arch.llms.chat.model.MessageAggregator;
import com.eastrobot.arch.llms.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Memory is retrieved added as a collection of messages to the prompt
 *
 * @author Christian Tzolov
 * @since 1.0.0 M1
 */
public class MessageChatMemoryAdvisor extends AbstractChatMemoryAdvisor<ChatMemory> {

    public MessageChatMemoryAdvisor(ChatMemory chatMemory) {
        super(chatMemory);
    }

    public MessageChatMemoryAdvisor(ChatMemory chatMemory, String defaultConversationId, int chatHistoryWindowSize) {
        super(chatMemory, defaultConversationId, chatHistoryWindowSize);
    }

    @Override
    public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
        String conversationId = this.doGetConversationId(context);

        int chatMemoryRetrieveSize = this.doGetChatMemoryRetrieveSize(context);

        // 1. Retrieve the chat memory for the current conversation.
        List<Message> memoryMessages = this.getChatMemoryStore().get(conversationId, chatMemoryRetrieveSize);

        // 2. Advise the request messages list.
        List<Message> advisedMessages = new ArrayList<>(request.getMessages());
        advisedMessages.addAll(memoryMessages);

        // 3. Create a new request with the advised messages.
        AdvisedRequest advisedRequest = AdvisedRequest.from(request).withMessages(advisedMessages).build();

        // 4. Add the new user input to the conversation memory.
        UserMessage userMessage = new UserMessage(new PromptTemplate(request.getUserText(), request.getUserParams()).render(), request.getMedia());
        this.getChatMemoryStore().add(this.doGetConversationId(context), userMessage);

        return advisedRequest;
    }

    @Override
    public ChatResponse adviseResponse(ChatResponse chatResponse, Map<String, Object> context) {
        List<Message> assistantMessages = chatResponse.getResults().stream().map(g -> (Message) g.getOutput()).collect(Collectors.toList());

        this.getChatMemoryStore().add(this.doGetConversationId(context), assistantMessages);

        return chatResponse;
    }

    @Override
    public Flux<ChatResponse> adviseResponse(Flux<ChatResponse> fluxChatResponse, Map<String, Object> context) {
        return new MessageAggregator().aggregate(fluxChatResponse, chatResponse -> {
            List<Message> assistantMessages = chatResponse.getResults()
                    .stream()
                    .map(g -> (Message) g.getOutput())
                    .collect(Collectors.toList());

            this.getChatMemoryStore().add(this.doGetConversationId(context), assistantMessages);
        });
    }

}