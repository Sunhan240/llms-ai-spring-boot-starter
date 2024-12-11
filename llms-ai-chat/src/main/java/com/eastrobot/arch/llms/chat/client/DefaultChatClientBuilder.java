package com.eastrobot.arch.llms.chat.client;

import com.eastrobot.arch.llms.chat.client.ChatClient.Builder;
import com.eastrobot.arch.llms.chat.client.ChatClient.PromptSystemSpec;
import com.eastrobot.arch.llms.chat.client.ChatClient.PromptUserSpec;
import com.eastrobot.arch.llms.chat.client.DefaultChatClient.DefaultChatClientRequestSpec;
import com.eastrobot.arch.llms.chat.model.ChatModel;
import com.eastrobot.arch.llms.chat.prompt.ChatOptions;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * DefaultChatClientBuilder is a builder class for creating a ChatClient.
 * It provides methods to set default values for various properties of the ChatClient.
 *
 * @author Mark Pollack
 * @author Christian Tzolov
 * @author Josh Long
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class DefaultChatClientBuilder implements Builder {

    protected final DefaultChatClientRequestSpec defaultRequest;

    private final ChatModel chatModel;

    public DefaultChatClientBuilder(ChatModel chatModel) {
        Assert.notNull(chatModel, "the " + ChatModel.class.getName() + " must be non-null");
        this.chatModel = chatModel;
        this.defaultRequest = new DefaultChatClientRequestSpec(chatModel,
                "", Collections.emptyMap(),
                "", Collections.emptyMap(),
                Collections.emptyList(), Collections.emptyList(),
                null, Collections.emptyList(), Collections.emptyMap(),null);
    }

    public ChatClient build() {
        return new DefaultChatClient(this.chatModel, this.defaultRequest);
    }

    public Builder defaultAdvisors(RequestResponseAdvisor... advisor) {
        this.defaultRequest.advisors(advisor);
        return this;
    }

    public Builder defaultAdvisors(Consumer<ChatClient.AdvisorSpec> advisorSpecConsumer) {
        this.defaultRequest.advisors(advisorSpecConsumer);
        return this;
    }

    public Builder defaultAdvisors(List<RequestResponseAdvisor> advisors) {
        this.defaultRequest.advisors(advisors);
        return this;
    }

    public Builder defaultOptions(ChatOptions chatOptions) {
        this.defaultRequest.options(chatOptions);
        return this;
    }

    public Builder defaultUser(String text) {
        this.defaultRequest.user(text);
        return this;
    }

    public Builder defaultUser(Resource text, Charset charset) {
        try (InputStream inputStream = text.getInputStream()) {
            this.defaultRequest.user(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Builder defaultUser(Resource text) {
        return this.defaultUser(text, Charset.defaultCharset());
    }

    public Builder defaultUser(Consumer<PromptUserSpec> userSpecConsumer) {
        this.defaultRequest.user(userSpecConsumer);
        return this;
    }

    public Builder defaultSystem(String text) {
        this.defaultRequest.system(text);
        return this;
    }

    public Builder defaultSystem(Resource text, Charset charset) {
        try (InputStream inputStream = text.getInputStream()) {
            this.defaultRequest.system(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Builder defaultSystem(Resource text) {
        return this.defaultSystem(text, Charset.defaultCharset());
    }

    public Builder defaultSystem(Consumer<PromptSystemSpec> systemSpecConsumer) {
        this.defaultRequest.system(systemSpecConsumer);
        return this;
    }

}
