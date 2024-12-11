package com.eastrobot.arch.llms.config.chat;

import com.eastrobot.arch.llms.chat.client.ChatClient;
import com.eastrobot.arch.llms.chat.model.ChatModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;

/**
 * <p></p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/22 16:25
 */
@Slf4j
@Data
public class ChatClientFactoryBean implements FactoryBean<ChatClient> {

    private String modelName;
    private ChatModel chatModel;

    @Override
    public ChatClient getObject() throws Exception {
        log.info("FactoryBean obtain ChatClient.....{}", modelName);
        return ChatClient.create(chatModel);
    }

    @Override
    public Class<?> getObjectType() {
        return ChatClient.class;
    }

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }
}
