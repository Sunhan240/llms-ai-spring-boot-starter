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
package com.eastrobot.arch.llms.chat.model;

import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.prompt.Prompt;
import com.eastrobot.arch.llms.model.StreamingModel;
import reactor.core.publisher.Flux;

import java.util.Arrays;

/**
 * <p>流式聊天接口</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/12 17:00
 */
@FunctionalInterface
public interface StreamingChatModel extends StreamingModel<Prompt, ChatResponse> {

    default Flux<String> stream(String message) {
        Prompt prompt = new Prompt(message);
        return stream(prompt).map(response -> (
                response.getResult() == null
                        || response.getResult().getOutput() == null
                        || response.getResult().getOutput().getContent() == null)
                ? "" : response.getResult().getOutput().getContent());
    }

    default Flux<String> stream(Message... messages) {
        Prompt prompt = new Prompt(Arrays.asList(messages));
        return stream(prompt).map(response -> (
                response.getResult() == null
                        || response.getResult().getOutput() == null
                        || response.getResult().getOutput().getContent() == null)
                ? "" : response.getResult().getOutput().getContent());
    }

    @Override
    Flux<ChatResponse> stream(Prompt prompt);

}
