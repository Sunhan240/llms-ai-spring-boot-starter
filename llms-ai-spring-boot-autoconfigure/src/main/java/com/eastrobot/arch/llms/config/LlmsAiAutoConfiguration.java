package com.eastrobot.arch.llms.config;

import com.eastrobot.arch.llms.config.chat.OpenAiChatProperties;
import com.eastrobot.arch.llms.config.rest.LlmsRestProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p></p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/19 15:09
 */
@ComponentScan
@AutoConfiguration
@EnableConfigurationProperties({
        OpenAiChatProperties.class, LlmsRestProperties.class
})
public class LlmsAiAutoConfiguration {
}
