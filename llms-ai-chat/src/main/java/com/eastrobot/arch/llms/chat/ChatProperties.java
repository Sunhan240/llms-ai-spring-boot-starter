package com.eastrobot.arch.llms.chat;

import com.eastrobot.arch.llms.chat.openai.OpenAiChatOptions;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>chat props</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/25 17:51
 */
@Data
public class ChatProperties {
    /**
     * authorization
     */
    private String apiKey;
    /**
     * model base url
     */
    private String baseUrl;
    private String uri;
    /**
     * model name
     */
    private String name;
    /**
     * model brief description
     */
    private String desc;

    private boolean enabled = true;
    /*
     * Follow the OpenAI api standard
     */
    private boolean standard = true;
    /**
     * custom header
     */
    private Map<String, List<String>> headers = new HashMap<>();

    private OpenAiChatOptions options;
}
