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


import com.eastrobot.arch.llms.chat.messages.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 如果当前的实现和它们的角色不能满足您的需要，则允许您将角色指定为字符串的PromptTemplate
 *
 * @author han.sun
 */
public class ChatPromptTemplate implements PromptTemplateChatActions, PromptTemplateActions {

    private final List<PromptTemplate> promptTemplates;

    public ChatPromptTemplate(List<PromptTemplate> promptTemplates) {
        this.promptTemplates = promptTemplates;
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        for (PromptTemplate promptTemplate : promptTemplates) {
            sb.append(promptTemplate.render());
        }
        return sb.toString();
    }

    @Override
    public String render(Map<String, Object> model) {
        StringBuilder sb = new StringBuilder();
        for (PromptTemplate promptTemplate : promptTemplates) {
            sb.append(promptTemplate.render(model));
        }
        return sb.toString();
    }

    @Override
    public List<Message> createMessages() {
        List<Message> messages = new ArrayList<>();
        for (PromptTemplate promptTemplate : promptTemplates) {
            messages.add(promptTemplate.createMessage());
        }
        return messages;
    }

    @Override
    public List<Message> createMessages(Map<String, Object> model) {
        List<Message> messages = new ArrayList<>();
        for (PromptTemplate promptTemplate : promptTemplates) {
            messages.add(promptTemplate.createMessage(model));
        }
        return messages;
    }

    @Override
    public Prompt create() {
        List<Message> messages = createMessages();
        return new Prompt(messages);
    }

    @Override
    public Prompt create(Map<String, Object> model) {
        List<Message> messages = createMessages(model);
        return new Prompt(messages);
    }

}
