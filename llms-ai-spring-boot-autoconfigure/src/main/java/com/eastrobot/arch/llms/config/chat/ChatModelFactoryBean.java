package com.eastrobot.arch.llms.config.chat;

import com.eastrobot.arch.llms.chat.api.LlmsAiApi;
import com.eastrobot.arch.llms.chat.model.ChatModel;
import com.eastrobot.arch.llms.chat.openai.OpenAiChatModel;
import com.eastrobot.arch.llms.chat.openai.OpenAiChatOptions;
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
public class ChatModelFactoryBean implements FactoryBean<ChatModel> {
    private String modelName;
    private LlmsAiApi llmsAiApi;
    private OpenAiChatOptions chatOptions;

    @Override
    public OpenAiChatModel getObject() throws Exception {
        if (log.isDebugEnabled())
            log.debug("FactoryBean obtain ChatModel .....{}", modelName);
        return new OpenAiChatModel(llmsAiApi, chatOptions);
    }

    @Override
    public Class<?> getObjectType() {
        return OpenAiChatModel.class;
    }

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }

}
