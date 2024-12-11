package com.eastrobot.arch.llms.chat.model;

import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.messages.UserMessage;
import com.eastrobot.arch.llms.model.Model;
import com.eastrobot.arch.llms.chat.prompt.ChatOptions;
import com.eastrobot.arch.llms.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.Arrays;

/**
 * <p></p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/12 17:00
 */
public interface ChatModel extends Model<Prompt, ChatResponse>, StreamingChatModel {

    String model();

    default String call(String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        Generation generation = call(prompt).getResult();
        return (generation != null) ? generation.getOutput().getContent() : "";
    }

    default String call(Message... messages) {
        Prompt prompt = new Prompt(Arrays.asList(messages));
        Generation generation = call(prompt).getResult();
        return (generation != null) ? generation.getOutput().getContent() : "";
    }

    @Override
    ChatResponse call(Prompt prompt);

    ChatOptions getDefaultOptions();

    default Flux<ChatResponse> stream(Prompt prompt) {
        throw new UnsupportedOperationException("streaming is not supported");
    }

}
