package com.eastrobot.arch.llms.chat.client;

import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.model.ChatResponse;
import com.eastrobot.arch.llms.chat.prompt.ChatOptions;
import com.eastrobot.arch.llms.chat.prompt.Prompt;
import com.eastrobot.arch.llms.converter.StructuredOutputConverter;
import com.eastrobot.arch.llms.model.Media;
import com.eastrobot.arch.llms.chat.model.ChatModel;
import com.eastrobot.arch.llms.retry.Backoff;
import com.eastrobot.arch.llms.retry.RetryPolicy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * <p>客户端使用流畅的API对AI模型执行无状态请求</p>
 * Use {@link ChatClient#builder(ChatModel)} to prepare an instance.
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/23 16:30
 */
public interface ChatClient {

    static ChatClient create(ChatModel chatModel) {
        return builder(chatModel).build();
    }

    static Builder builder(ChatModel chatModel) {
        return new DefaultChatClientBuilder(chatModel);
    }

    ChatClientRequestSpec prompt();

    ChatClientPromptRequestSpec prompt(Prompt prompt);

    /**
     * Return a {@link ChatClient.Builder} to create a new {@link ChatClient} whose
     * settings are replicated from the default {@link ChatClientRequestSpec} of this
     * client.
     */
    Builder mutate();

    interface PromptUserSpec {

        PromptUserSpec text(String text);

        PromptUserSpec text(Resource text, Charset charset);

        PromptUserSpec text(Resource text);

        PromptUserSpec params(Map<String, Object> p);

        PromptUserSpec param(String k, Object v);

        PromptUserSpec media(Media... media);

        PromptUserSpec media(MimeType mimeType, URL url);

        PromptUserSpec media(MimeType mimeType, Resource resource);

    }

    interface PromptSystemSpec {

        PromptSystemSpec text(String text);

        PromptSystemSpec text(Resource text, Charset charset);

        PromptSystemSpec text(Resource text);

        PromptSystemSpec params(Map<String, Object> p);

        PromptSystemSpec param(String k, Object v);

    }

    interface RetrySpec {
        RetrySpec maxAttempts(int maxAttempts);
        RetrySpec backoff(Backoff backoff);
        RetrySpec predicate(Predicate<ChatResponse> predicate);
        RetrySpec throwables(List<Class<? extends Throwable>> throwables);
        RetrySpec useLastCallback(boolean useLastCallback);
        RetrySpec recoveryCallback(String recoveryCallback);
    }

    interface AdvisorSpec {

        AdvisorSpec param(String k, Object v);

        AdvisorSpec params(Map<String, Object> p);

        AdvisorSpec advisors(RequestResponseAdvisor... advisors);

        AdvisorSpec advisors(List<RequestResponseAdvisor> advisors);

    }

    interface CallResponseSpec {

        <T> T entity(ParameterizedTypeReference<T> type);

        <T> T entity(StructuredOutputConverter<T> structuredOutputConverter);

        <T> T entity(Class<T> type);

        ChatResponse chatResponse();

        String content();

        <T> ResponseEntity<ChatResponse, T> responseEntity(Class<T> type);

        <T> ResponseEntity<ChatResponse, T> responseEntity(ParameterizedTypeReference<T> type);

        <T> ResponseEntity<ChatResponse, T> responseEntity(StructuredOutputConverter<T> structuredOutputConverter);

    }

    interface StreamResponseSpec {

        Flux<ChatResponse> chatResponse();

        Flux<String> content();

    }

    interface ChatClientPromptRequestSpec {

        CallPromptResponseSpec call();

        StreamPromptResponseSpec stream();

    }

    interface CallPromptResponseSpec {

        String content();

        List<String> contents();

        ChatResponse chatResponse();

    }

    interface StreamPromptResponseSpec {

        Flux<ChatResponse> chatResponse();

        Flux<String> content();

    }


    interface ChatClientRequestSpec {

        /**
         * Return a {@code ChatClient.Builder} to create a new {@code ChatClient} whose
         * settings are replicated from this {@code ChatClientRequest}.
         */
        Builder mutate();

        ChatClientRequestSpec retry(RetryPolicy retryPolicy);

        ChatClientRequestSpec retry(Consumer<RetrySpec >retryPolicy);

        ChatClientRequestSpec advisors(Consumer<AdvisorSpec> consumer);

        ChatClientRequestSpec advisors(RequestResponseAdvisor... advisors);

        ChatClientRequestSpec advisors(List<RequestResponseAdvisor> advisors);

        ChatClientRequestSpec messages(Message... messages);

        ChatClientRequestSpec messages(List<Message> messages);

        <T extends ChatOptions> ChatClientRequestSpec options(T options);

//        <I, O> ChatClientRequestSpec function(String name, String description,
//                                              java.util.function.Function<I, O> function);

//        ChatClientRequestSpec functions(String... functionBeanNames);

        ChatClientRequestSpec system(String text);

        ChatClientRequestSpec system(Resource textResource, Charset charset);

        ChatClientRequestSpec system(Resource text);

        ChatClientRequestSpec system(Consumer<PromptSystemSpec> consumer);

        ChatClientRequestSpec user(String text);

        ChatClientRequestSpec user(Resource text, Charset charset);

        ChatClientRequestSpec user(Resource text);

        ChatClientRequestSpec user(Consumer<PromptUserSpec> consumer);

        CallResponseSpec call();

        StreamResponseSpec stream();

    }


    interface Builder {

        Builder defaultAdvisors(RequestResponseAdvisor... advisor);

        Builder defaultAdvisors(Consumer<AdvisorSpec> advisorSpecConsumer);

        Builder defaultAdvisors(List<RequestResponseAdvisor> advisors);

        Builder defaultOptions(ChatOptions chatOptions);

        Builder defaultUser(String text);

        Builder defaultUser(Resource text, Charset charset);

        Builder defaultUser(Resource text);

        Builder defaultUser(Consumer<PromptUserSpec> userSpecConsumer);

        Builder defaultSystem(String text);

        Builder defaultSystem(Resource text, Charset charset);

        Builder defaultSystem(Resource text);

        Builder defaultSystem(Consumer<PromptSystemSpec> systemSpecConsumer);

        /*<I, O> Builder defaultFunction(String name, String description, java.util.function.Function<I, O> function);

        Builder defaultFunctions(String... functionNames);*/

        ChatClient build();

    }

}
