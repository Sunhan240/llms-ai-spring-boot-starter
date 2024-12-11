package com.eastrobot.arch.llms.chat;

import com.eastrobot.arch.llms.chat.api.LlmsAiApi;
import com.eastrobot.arch.llms.chat.client.ChatClient;
import com.eastrobot.arch.llms.chat.model.ChatModel;
import com.eastrobot.arch.llms.chat.openai.OpenAiChatOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p></p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/19 17:26
 */
@Slf4j
@Component
public class ChatModelProvider implements ApplicationRunner {

    @Resource
    private ApplicationContext applicationContext;

    private static final Map<String, ChatProperties> CHAT_METAS = new HashMap<>();
    private static final Map<String, OpenAiChatOptions> CHAT_OPTIONS = new HashMap<>();
    private static final Map<String, LlmsAiApi> CHAT_AI_API = new HashMap<>();
    private static final Map<String, ChatModel> CHAT_MODELS = new HashMap<>();
    private static final Map<String, ChatClient> CHAT_CLIENTS = new HashMap<>();


    public static ChatProperties getProps(String model) {
        return CHAT_METAS.get(model);
    }

    public static void putProps(String model, ChatProperties options) {
        Assert.state(model != null && options != null, "The model name and options cannot be null!");
        CHAT_METAS.put(model, options);
    }

    public static OpenAiChatOptions getOptions(String model) {
        return CHAT_OPTIONS.get(model);
    }

    public static void putOptions(String model, OpenAiChatOptions options) {
        Assert.state(model != null && options != null, "The model name and options cannot be null!");
        CHAT_OPTIONS.put(model, options);
    }

    public static LlmsAiApi getApi(String model) {
        return CHAT_AI_API.get(model);
    }

    public static void putApi(String model, LlmsAiApi api) {
        CHAT_AI_API.put(model, api);
    }


    public static ChatModel model(String model) {
        Assert.state(model != null, "The model name and options cannot be null!");
        return CHAT_MODELS.get(model);
    }

    public static ChatClient client(String model) {
        Assert.state(model != null, "The model name and options cannot be null!");
        return CHAT_CLIENTS.get(model);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, ChatModel> map = applicationContext.getBeansOfType(ChatModel.class);
        for (Map.Entry<String, ChatModel> entry : map.entrySet()) {
            ChatModel chatModel = entry.getValue();
            String model = chatModel.model();
            CHAT_MODELS.put(model, chatModel);
            CHAT_CLIENTS.put(model, ChatClient.create(chatModel));
        }
        if (log.isDebugEnabled()) {
            CHAT_MODELS.forEach((k, v) -> log.debug("chat model:{} | {}", k, v));
            CHAT_CLIENTS.forEach((k, v) -> log.debug("chat client:{} | {}", k, v));
        }
    }
}
