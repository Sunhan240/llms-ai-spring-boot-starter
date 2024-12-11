package com.eastrobot.arch.llms.config.rest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>WebClient config</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/12 13:59
 */
@Data
@ConfigurationProperties(prefix = LlmsRestProperties.CONFIG_PREFIX)
public class LlmsRestProperties {
    public static final String CONFIG_PREFIX = "llms.ai.rest";
    /**
     * 连接超时
     */
    private Integer connectTimeout = 3000;
    /**
     * 响应超时
     */
    private Integer readTimeout = 60000;
    /**
     * 请求超时
     */
    private Integer writeTimeout = 60000;

}
