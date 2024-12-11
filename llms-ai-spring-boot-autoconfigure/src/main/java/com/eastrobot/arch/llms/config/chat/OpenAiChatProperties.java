package com.eastrobot.arch.llms.config.chat;

import com.eastrobot.arch.llms.chat.openai.OpenAiChatOptions;
import com.eastrobot.arch.llms.config.LlmsAiParentProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>OpenAI properties</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 13:46
 */
@Data
@ConfigurationProperties(prefix = OpenAiChatProperties.CONFIG_PREFIX)
public class OpenAiChatProperties {
    public static final String CONFIG_PREFIX = "llms.ai.chat";
    public static final String DEFAULT_CHAT_MODEL = "gpt-4o";

    /**
     * 模型列表
     */
    private List<ChatProperties> models;
    private static final Double DEFAULT_TEMPERATURE = 0.3;

    @Getter
    @Setter
    public static class ChatProperties extends LlmsAiParentProperties {
        /**
         * Enable chat model.
         */
        private boolean enabled = true;
        /**
         * Follow the OpenAI api standard
         */
        private boolean standard = true;
        /**
         * custom header
         */
        private Map<String, List<String>> headers = new HashMap<>();
        /**
         * model option args
         */
        @NestedConfigurationProperty
        private OpenAiChatOptions options = OpenAiChatOptions.builder()
//                .withModel(DEFAULT_CHAT_MODEL)
                .withTemperature(DEFAULT_TEMPERATURE.floatValue())
                .build();
    }


}
