package com.eastrobot.arch.llms.chat.client;

import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.messages.SystemMessage;
import com.eastrobot.arch.llms.chat.messages.UserMessage;
import com.eastrobot.arch.llms.chat.model.ChatModel;
import com.eastrobot.arch.llms.chat.model.ChatResponse;
import com.eastrobot.arch.llms.chat.model.Generation;
import com.eastrobot.arch.llms.chat.model.StreamingChatModel;
import com.eastrobot.arch.llms.chat.prompt.ChatOptions;
import com.eastrobot.arch.llms.chat.prompt.Prompt;
import com.eastrobot.arch.llms.chat.prompt.PromptTemplate;
import com.eastrobot.arch.llms.common.RetryUtil;
import com.eastrobot.arch.llms.converter.BeanOutputConverter;
import com.eastrobot.arch.llms.converter.StructuredOutputConverter;
import com.eastrobot.arch.llms.model.Media;
import com.eastrobot.arch.llms.retry.Backoff;
import com.eastrobot.arch.llms.retry.RetryPolicy;
import com.eastrobot.arch.llms.retry.TransientAiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The default implementation of {@link ChatClient} as created by the
 * {@link Builder#build()} } method.
 *
 * @author Mark Pollack
 * @author Christian Tzolov
 * @author Josh Long
 * @author Arjen Poutsma
 * @since 1.0.0
 */
@Slf4j
public class DefaultChatClient implements ChatClient {

    private final ChatModel chatModel;

    private final DefaultChatClientRequestSpec defaultChatClientRequest;

    public DefaultChatClient(ChatModel chatModel, DefaultChatClientRequestSpec defaultChatClientRequest) {
        this.chatModel = chatModel;
        this.defaultChatClientRequest = defaultChatClientRequest;
    }

    @Override
    public ChatClientRequestSpec prompt() {
        return new DefaultChatClientRequestSpec(this.defaultChatClientRequest);
    }

    @Override
    public ChatClientPromptRequestSpec prompt(Prompt prompt) {
        return new DefaultChatClientPromptRequestSpec(this.chatModel, prompt);
    }

    /**
     * Return a {@code ChatClient2Builder} to create a new {@code ChatClient} whose
     * settings are replicated from this {@code ChatClientRequest}.
     */
    @Override
    public Builder mutate() {
        return this.defaultChatClientRequest.mutate();
    }

    public static class DefaultPromptUserSpec implements PromptUserSpec {

        private String text = "";

        private final Map<String, Object> params = new HashMap<>();

        private final List<Media> media = new ArrayList<>();

        @Override
        public PromptUserSpec media(Media... media) {
            this.media.addAll(Arrays.asList(media));
            return this;
        }

        @Override
        public PromptUserSpec media(MimeType mimeType, URL url) {
            this.media.add(new Media(mimeType, url));
            return this;
        }

        @Override
        public PromptUserSpec media(MimeType mimeType, Resource resource) {
            this.media.add(new Media(mimeType, resource));
            return this;
        }

        @Override
        public PromptUserSpec text(String text) {
            this.text = text;
            return this;
        }

        @Override
        public PromptUserSpec text(Resource text, Charset charset) {
            try (InputStream inputStream = text.getInputStream()) {
                this.text(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public PromptUserSpec text(Resource text) {
            this.text(text, Charset.defaultCharset());
            return this;
        }

        @Override
        public PromptUserSpec param(String k, Object v) {
            this.params.put(k, v);
            return this;
        }

        @Override
        public PromptUserSpec params(Map<String, Object> p) {
            this.params.putAll(p);
            return this;
        }

        protected String text() {
            return this.text;
        }

        protected Map<String, Object> params() {
            return this.params;
        }

        protected List<Media> media() {
            return this.media;
        }

    }


    public static class DefaultPromptSystemSpec implements PromptSystemSpec {

        private String text = "";

        private final Map<String, Object> params = new HashMap<>();

        @Override
        public PromptSystemSpec text(String text) {
            this.text = text;
            return this;
        }

        @Override
        public PromptSystemSpec text(Resource text, Charset charset) {
            try (InputStream inputStream = text.getInputStream()) {
                this.text(StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
        public PromptSystemSpec text(Resource text) {
            this.text(text, Charset.defaultCharset());
            return this;
        }

        @Override
        public PromptSystemSpec param(String k, Object v) {
            this.params.put(k, v);
            return this;
        }

        @Override
        public PromptSystemSpec params(Map<String, Object> p) {
            this.params.putAll(p);
            return this;
        }

        protected String text() {
            return this.text;
        }

        protected Map<String, Object> params() {
            return this.params;
        }

    }

    public static class DefaultRetrySpec implements RetrySpec {

        private final RetryPolicy retryPolicy = new RetryPolicy();

        @Override
        public RetrySpec maxAttempts(int maxAttempts) {
            this.retryPolicy.setMaxAttempts(maxAttempts);
            return this;
        }

        @Override
        public RetrySpec backoff(Backoff backoff) {
            if (backoff == null) backoff = new Backoff();
            this.retryPolicy.setBackoff(backoff);
            return this;
        }

        @Override
        public RetrySpec predicate(Predicate<ChatResponse> predicate) {
            this.retryPolicy.setPredicate(predicate);
            return this;
        }

        @Override
        public RetrySpec throwables(List<Class<? extends Throwable>> throwables) {
            if (throwables != null && !throwables.isEmpty())
                this.retryPolicy.setThrowables(throwables);
            return this;
        }

        @Override
        public RetrySpec useLastCallback(boolean useLastCallback) {
            this.retryPolicy.setUseLastCallback(useLastCallback);
            return this;
        }

        @Override
        public RetrySpec recoveryCallback(String recoveryCallback) {
            this.retryPolicy.setRecoveryCallback(recoveryCallback);
            return this;
        }

        public RetryPolicy getRetryPolicy() {
            return this.retryPolicy;
        }
    }

    public static class DefaultAdvisorSpec implements AdvisorSpec {

        private final List<RequestResponseAdvisor> advisors = new ArrayList<>();

        private final Map<String, Object> params = new HashMap<>();

        public AdvisorSpec param(String k, Object v) {
            this.params.put(k, v);
            return this;
        }

        public AdvisorSpec params(Map<String, Object> p) {
            this.params.putAll(p);
            return this;
        }

        public AdvisorSpec advisors(RequestResponseAdvisor... advisors) {
            this.advisors.addAll(Arrays.asList(advisors));
            return this;
        }

        public AdvisorSpec advisors(List<RequestResponseAdvisor> advisors) {
            this.advisors.addAll(advisors);
            return this;
        }

        public List<RequestResponseAdvisor> getAdvisors() {
            return advisors;
        }

        public Map<String, Object> getParams() {
            return params;
        }

    }

    public static class DefaultCallResponseSpec implements CallResponseSpec {

        private final DefaultChatClientRequestSpec request;

        private final ChatModel chatModel;

        public DefaultCallResponseSpec(ChatModel chatModel, DefaultChatClientRequestSpec request) {
            this.chatModel = chatModel;
            this.request = request;
        }

        public <T> ResponseEntity<ChatResponse, T> responseEntity(Class<T> type) {
            Assert.notNull(type, "the class must be non-null");
            return doResponseEntity(new BeanOutputConverter<>(type));
        }

        public <T> ResponseEntity<ChatResponse, T> responseEntity(ParameterizedTypeReference<T> type) {
            return doResponseEntity(new BeanOutputConverter<T>(type));
        }

        public <T> ResponseEntity<ChatResponse, T> responseEntity(
                StructuredOutputConverter<T> structuredOutputConverter) {
            return doResponseEntity(structuredOutputConverter);
        }

        protected <T> ResponseEntity<ChatResponse, T> doResponseEntity(StructuredOutputConverter<T> boc) {
            ChatResponse chatResponse = doGetChatResponse(this.request, boc.getFormat());
            String responseContent = chatResponse.getResult().getOutput().getContent();
            T entity = boc.convert(responseContent);

            return new ResponseEntity<>(chatResponse, entity);
        }

        public <T> T entity(ParameterizedTypeReference<T> type) {
            return doSingleWithBeanOutputConverter(new BeanOutputConverter<>(type));
        }

        public <T> T entity(StructuredOutputConverter<T> structuredOutputConverter) {
            return doSingleWithBeanOutputConverter(structuredOutputConverter);
        }

        private <T> T doSingleWithBeanOutputConverter(StructuredOutputConverter<T> boc) {
            ChatResponse chatResponse = doGetChatResponse(this.request, boc.getFormat());
            String stringResponse = chatResponse.getResult().getOutput().getContent();
            return boc.convert(stringResponse);
        }

        public <T> T entity(Class<T> type) {
            Assert.notNull(type, "the class must be non-null");
            StructuredOutputConverter<T> boc = new BeanOutputConverter<T>(type);
            return doSingleWithBeanOutputConverter(boc);
        }

        private ChatResponse doGetChatResponse() {
            return this.doGetChatResponse(this.request, "");
        }

        private ChatResponse doGetChatResponse(DefaultChatClientRequestSpec inputRequest, String formatParam) {

            Map<String, Object> context = new ConcurrentHashMap<>(inputRequest.getAdvisorParams());
            DefaultChatClientRequestSpec advisedRequest = DefaultChatClientRequestSpec.adviseOnRequest(inputRequest,
                    context);

            String processedUserText = StringUtils.hasText(formatParam)
                    ? advisedRequest.getUserText() + System.lineSeparator() + "{spring_ai_soc_format}"
                    : advisedRequest.getUserText();

            Map<String, Object> userParams = new HashMap<>(advisedRequest.getUserParams());
            if (StringUtils.hasText(formatParam)) {
                userParams.put("spring_ai_soc_format", formatParam);
            }

//            List<Message> messages = new ArrayList<>(advisedRequest.getMessages());
            List<Message> messages = new ArrayList<>();
            boolean textsAreValid = (StringUtils.hasText(processedUserText)
                    || StringUtils.hasText(advisedRequest.getSystemText()));
            if (textsAreValid) {
                if (StringUtils.hasText(advisedRequest.getSystemText())
                        || !advisedRequest.getSystemParams().isEmpty()) {
                    Message systemMessage = new SystemMessage(
                            new PromptTemplate(advisedRequest.getSystemText(), advisedRequest.getSystemParams())
                                    .render());
                    messages.add(systemMessage);
                }
                // chat memory
                messages.addAll(advisedRequest.getMessages());
                UserMessage userMessage;
                if (!CollectionUtils.isEmpty(userParams)) {
                    userMessage = new UserMessage(new PromptTemplate(processedUserText, userParams).render(),
                            advisedRequest.getMedia());
                } else {
                    userMessage = new UserMessage(processedUserText, advisedRequest.getMedia());
                }
                messages.add(userMessage);
            }

            Prompt prompt = new Prompt(messages, advisedRequest.getChatOptions());

            ChatResponse advisedResponse;
            if (advisedRequest.getRetryPolicy() != null) {
                RetryPolicy retryPolicy = advisedRequest.getRetryPolicy();
                RetryTemplate retryTemplate = RetryUtil.build(retryPolicy);
                advisedResponse = retryTemplate.execute(retryContext -> {
                            ChatResponse chatResponse = this.chatModel.call(prompt);
                            if (retryPolicy.getPredicate() == null) return chatResponse;
                            else {
                                retryContext.setAttribute("conversation_id", userParams.get("conversation_id"));
                                if (chatResponse != null && chatResponse.getResult() != null) {
                                    boolean test = retryPolicy.getPredicate().test(chatResponse);
                                    if (retryPolicy.isUseLastCallback())
                                        retryContext.setAttribute("result", chatResponse);
                                    if (!test) throw new TransientAiException("pre check failed，assert retry continues");
                                    else {
                                        retryContext.setExhaustedOnly();
                                        return chatResponse;
                                    }
                                } else
                                    throw new TransientAiException("the model: 【" + this.chatModel.model() + "】 response is null!");
                            }
                        }, retryContext -> {
                            log.error("{} recovery callback", retryContext.getAttribute("conversation_id"));
                            if (retryPolicy.isUseLastCallback())
                                return (ChatResponse) retryContext.getAttribute("result");
                            else
                                return new ChatResponse(Collections.singletonList(new Generation(retryPolicy.getRecoveryCallback())));
                        }
                );
            } else {
                advisedResponse = this.chatModel.call(prompt);
            }
            // apply the advisors on response
            if (!CollectionUtils.isEmpty(inputRequest.getAdvisors())) {
                List<RequestResponseAdvisor> currentAdvisors = new ArrayList<>(inputRequest.getAdvisors());
                for (RequestResponseAdvisor advisor : currentAdvisors) {
                    advisedResponse = advisor.adviseResponse(advisedResponse, context);
                }
            }
            return advisedResponse;
        }

        public ChatResponse chatResponse() {
            return doGetChatResponse();
        }

        public String content() {
            return doGetChatResponse().getResult().getOutput().getContent();
        }

    }

    public static class DefaultStreamResponseSpec implements StreamResponseSpec {

        private final DefaultChatClientRequestSpec request;

        private final ChatModel chatModel;

        public DefaultStreamResponseSpec(ChatModel chatModel, DefaultChatClientRequestSpec request) {
            this.chatModel = chatModel;
            this.request = request;
        }

        private Flux<ChatResponse> doGetFluxChatResponse(DefaultChatClientRequestSpec inputRequest) {

            Map<String, Object> context = new ConcurrentHashMap<>(inputRequest.getAdvisorParams());
            DefaultChatClientRequestSpec advisedRequest = DefaultChatClientRequestSpec.adviseOnRequest(inputRequest,
                    context);

            String processedUserText = advisedRequest.getUserText();
            Map<String, Object> userParams = new HashMap<>(advisedRequest.getUserParams());

            List<Message> messages = new ArrayList<>(advisedRequest.getMessages());
            boolean textsAreValid = (StringUtils.hasText(processedUserText)
                    || StringUtils.hasText(advisedRequest.getSystemText()));
            if (textsAreValid) {
                UserMessage userMessage = null;
                if (!CollectionUtils.isEmpty(userParams)) {
                    System.out.println("userParams:" + userParams);
                    userMessage = new UserMessage(new PromptTemplate(processedUserText, userParams).render(),
                            advisedRequest.getMedia());
                } else {
                    userMessage = new UserMessage(processedUserText, advisedRequest.getMedia());
                }
                if (StringUtils.hasText(advisedRequest.getSystemText())
                        || !advisedRequest.getSystemParams().isEmpty()) {
                    System.out.println("systemParams:" + advisedRequest.getSystemParams());
                    Message systemMessage = new SystemMessage(
                            new PromptTemplate(advisedRequest.getSystemText(), advisedRequest.getSystemParams())
                                    .render());
                    messages.add(systemMessage);
                }
                messages.add(userMessage);
            }

            Prompt prompt = new Prompt(messages, advisedRequest.getChatOptions());

            Flux<ChatResponse> advisedResponse = this.chatModel.stream(prompt);
            // apply the advisors on response
            if (!CollectionUtils.isEmpty(inputRequest.getAdvisors())) {
                List<RequestResponseAdvisor> currentAdvisors = new ArrayList<>(inputRequest.getAdvisors());
                for (RequestResponseAdvisor advisor : currentAdvisors) {
                    advisedResponse = advisor.adviseResponse(advisedResponse, context);
                }
            }

            return advisedResponse;
        }

        public Flux<ChatResponse> chatResponse() {
            return doGetFluxChatResponse(this.request);
        }

        public Flux<String> content() {
            return doGetFluxChatResponse(this.request).map(r -> {
                if (r.getResult() == null || r.getResult().getOutput() == null
                        || r.getResult().getOutput().getContent() == null) {
                    return "";
                }
                return r.getResult().getOutput().getContent();
            }).filter(StringUtils::hasLength);
        }

    }

    public static class DefaultChatClientRequestSpec implements ChatClientRequestSpec {

        private final ChatModel chatModel;

        private String userText = "";

        private String systemText = "";

        private ChatOptions chatOptions;

        private final List<Media> media = new ArrayList<>();

        private final List<Message> messages = new ArrayList<>();

        private final Map<String, Object> userParams = new HashMap<>();

        private final Map<String, Object> systemParams = new HashMap<>();

        private RetryPolicy retryPolicy;

        private final List<RequestResponseAdvisor> advisors = new ArrayList<>();

        private final Map<String, Object> advisorParams = new HashMap<>();

        public String getUserText() {
            return userText;
        }

        public Map<String, Object> getUserParams() {
            return userParams;
        }

        public String getSystemText() {
            return systemText;
        }

        public Map<String, Object> getSystemParams() {
            return systemParams;
        }

        public ChatOptions getChatOptions() {
            return chatOptions;
        }

        public RetryPolicy getRetryPolicy() {
            return retryPolicy;
        }

        public List<RequestResponseAdvisor> getAdvisors() {
            return advisors;
        }

        public Map<String, Object> getAdvisorParams() {
            return advisorParams;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public List<Media> getMedia() {
            return media;
        }

        /* copy constructor */
        DefaultChatClientRequestSpec(DefaultChatClientRequestSpec ccr) {
            this(ccr.chatModel, ccr.userText, ccr.userParams, ccr.systemText, ccr.systemParams, /*ccr.functionCallbacks,*/
                    ccr.messages, /*ccr.functionNames,*/ ccr.media, ccr.chatOptions, ccr.advisors, ccr.advisorParams, ccr.retryPolicy);
        }

        public DefaultChatClientRequestSpec(ChatModel chatModel, String userText, Map<String, Object> userParams,
                                            String systemText, Map<String, Object> systemParams, /*List<FunctionCallback> functionCallbacks,*/
                                            List<Message> messages, /*List<String> functionNames,*/ List<Media> media, ChatOptions chatOptions,
                                            List<RequestResponseAdvisor> advisors, Map<String, Object> advisorParams, RetryPolicy retryPolicy) {

            this.chatModel = chatModel;
            this.chatOptions = chatOptions != null ? chatOptions.copy()
                    : (chatModel.getDefaultOptions() != null) ? chatModel.getDefaultOptions().copy() : null;

            this.userText = userText;
            this.userParams.putAll(userParams);
            this.systemText = systemText;
            this.systemParams.putAll(systemParams);
            this.messages.addAll(messages);
            this.media.addAll(media);
            this.advisors.addAll(advisors);
            this.advisorParams.putAll(advisorParams);
            this.retryPolicy = retryPolicy;
        }

        /**
         * Return a {@code ChatClient2Builder} to create a new {@code ChatClient2} whose
         * settings are replicated from this {@code ChatClientRequest}.
         */
        public Builder mutate() {
            DefaultChatClientBuilder builder = (DefaultChatClientBuilder) ChatClient.builder(chatModel)
                    .defaultSystem(s -> s.text(this.systemText).params(this.systemParams))
                    .defaultUser(u -> u.text(this.userText)
                            .params(this.userParams)
                            .media(this.media.toArray(new Media[0])))
                    .defaultOptions(this.chatOptions)
//                    .defaultFunctions(StringUtils.toStringArray(this.functionNames))
                    ;
            // workaround to set the missing fields.
            builder.defaultRequest.getMessages().addAll(this.messages);
//            builder.defaultRequest.getFunctionCallbacks().addAll(this.functionCallbacks);
            return builder;
        }

        @Override
        public ChatClientRequestSpec retry(RetryPolicy retryPolicy) {
            Assert.notNull(retryPolicy, "the retryPolicy must be non-null");
            this.retryPolicy = retryPolicy;
            return this;
        }

        @Override
        public ChatClientRequestSpec retry(Consumer<RetrySpec> consumer) {
            Assert.notNull(consumer, "the retryPolicy consumer must be non-null");
            DefaultRetrySpec rs = new DefaultRetrySpec();
            consumer.accept(rs);
            this.retryPolicy = rs.getRetryPolicy();
            return this;
        }

        public ChatClientRequestSpec advisors(Consumer<ChatClient.AdvisorSpec> consumer) {
            Assert.notNull(consumer, "the consumer must be non-null");
            DefaultAdvisorSpec as = new DefaultAdvisorSpec();
            consumer.accept(as);
            this.advisorParams.putAll(as.getParams());
            this.advisors.addAll(as.getAdvisors());
            return this;
        }

        public ChatClientRequestSpec advisors(RequestResponseAdvisor... advisors) {
            Assert.notNull(advisors, "the advisors must be non-null");
            this.advisors.addAll(Arrays.asList(advisors));
            return this;
        }

        public ChatClientRequestSpec advisors(List<RequestResponseAdvisor> advisors) {
            Assert.notNull(advisors, "the advisors must be non-null");
            this.advisors.addAll(advisors);
            return this;
        }

        public ChatClientRequestSpec messages(Message... messages) {
            Assert.notNull(messages, "the messages must be non-null");
            this.messages.addAll(Arrays.asList(messages));
            return this;
        }

        public ChatClientRequestSpec messages(List<Message> messages) {
            Assert.notNull(messages, "the messages must be non-null");
            this.messages.addAll(messages);
            return this;
        }

        public <T extends ChatOptions> ChatClientRequestSpec options(T options) {
//            Assert.notNull(options, "the options must be non-null");
            this.chatOptions = options;
            return this;
        }

        public ChatClientRequestSpec system(String text) {
            Assert.notNull(text, "the text must be non-null");
            this.systemText = text;
            return this;
        }

        public ChatClientRequestSpec system(Resource textResource, Charset charset) {

            Assert.notNull(textResource, "the text resource must be non-null");
            Assert.notNull(charset, "the charset must be non-null");

            try (InputStream inputStream = textResource.getInputStream()) {
                this.systemText = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public ChatClientRequestSpec system(Resource text) {
            Assert.notNull(text, "the text resource must be non-null");
            return this.system(text, Charset.defaultCharset());
        }

        public ChatClientRequestSpec system(Consumer<PromptSystemSpec> consumer) {
            Assert.notNull(consumer, "the consumer must be non-null");
            DefaultPromptSystemSpec ss = new DefaultPromptSystemSpec();
            consumer.accept(ss);
            this.systemText = StringUtils.hasText(ss.text()) ? ss.text() : this.systemText;
            this.systemParams.putAll(ss.params());
            return this;
        }

        public ChatClientRequestSpec user(String text) {
            Assert.notNull(text, "the text must be non-null");
            this.userText = text;
            return this;
        }

        public ChatClientRequestSpec user(Resource text, Charset charset) {
            Assert.notNull(text, "the text resource must be non-null");
            Assert.notNull(charset, "the charset must be non-null");
            try (InputStream inputStream = text.getInputStream()) {
                this.userText = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public ChatClientRequestSpec user(Resource text) {
            Assert.notNull(text, "the text resource must be non-null");
            return this.user(text, Charset.defaultCharset());
        }

        public ChatClientRequestSpec user(Consumer<PromptUserSpec> consumer) {
            Assert.notNull(consumer, "the consumer must be non-null");
            DefaultPromptUserSpec us = new DefaultPromptUserSpec();
            consumer.accept(us);
            this.userText = StringUtils.hasText(us.text()) ? us.text() : this.userText;
            this.userParams.putAll(us.params());
            this.media.addAll(us.media());
            return this;
        }

        public CallResponseSpec call() {
            return new DefaultCallResponseSpec(chatModel, this);
        }

        public StreamResponseSpec stream() {
            return new DefaultStreamResponseSpec(chatModel, this);
        }

        public static DefaultChatClientRequestSpec adviseOnRequest(DefaultChatClientRequestSpec inputRequest,
                                                                   Map<String, Object> context) {

            DefaultChatClientRequestSpec advisedRequest = inputRequest;

            if (!CollectionUtils.isEmpty(inputRequest.advisors)) {
                AdvisedRequest adviseRequest = new AdvisedRequest(inputRequest.chatModel, inputRequest.userText,
                        inputRequest.systemText, inputRequest.chatOptions, inputRequest.media,
                        /*inputRequest.functionNames, inputRequest.functionCallbacks,*/ inputRequest.messages,
                        inputRequest.userParams, inputRequest.systemParams, inputRequest.advisors,
                        inputRequest.advisorParams, inputRequest.getRetryPolicy());

                // apply the advisors onRequest
                List<RequestResponseAdvisor> currentAdvisors = new ArrayList<>(inputRequest.advisors);
                for (RequestResponseAdvisor advisor : currentAdvisors) {
                    adviseRequest = advisor.adviseRequest(adviseRequest, context);
                }

                advisedRequest = new DefaultChatClientRequestSpec(adviseRequest.getChatModel(), adviseRequest.getUserText(),
                        adviseRequest.getUserParams(), adviseRequest.getSystemText(), adviseRequest.getSystemParams(),
                        /*adviseRequest.functionCallbacks(),*/ adviseRequest.getMessages(),/* adviseRequest.functionNames(),*/
                        adviseRequest.getMedia(), adviseRequest.getChatOptions(), adviseRequest.getAdvisors(),
                        adviseRequest.getAdvisorParams(), adviseRequest.getRetryPolicy());
            }

            return advisedRequest;
        }

    }

    // Prompt

    public static class DefaultCallPromptResponseSpec implements CallPromptResponseSpec {

        private final ChatModel chatModel;

        private final Prompt prompt;

        public DefaultCallPromptResponseSpec(ChatModel chatModel, Prompt prompt) {
            this.chatModel = chatModel;
            this.prompt = prompt;
        }

        public String content() {
            return doGetChatResponse(this.prompt).getResult().getOutput().getContent();
        }

        public List<String> contents() {
            return doGetChatResponse(this.prompt).getResults().stream().map(r -> r.getOutput().getContent()).collect(Collectors.toList());
        }

        public ChatResponse chatResponse() {
            return doGetChatResponse(this.prompt);
        }

        private ChatResponse doGetChatResponse(Prompt prompt) {
            return chatModel.call(prompt);
        }

    }

    public static class DefaultStreamPromptResponseSpec implements StreamPromptResponseSpec {

        private final Prompt prompt;

        private final StreamingChatModel chatModel;

        public DefaultStreamPromptResponseSpec(StreamingChatModel streamingChatModel, Prompt prompt) {
            this.chatModel = streamingChatModel;
            this.prompt = prompt;
        }

        public Flux<ChatResponse> chatResponse() {
            return doGetFluxChatResponse(this.prompt);
        }

        private Flux<ChatResponse> doGetFluxChatResponse(Prompt prompt) {
            return this.chatModel.stream(prompt);
        }

        public Flux<String> content() {
            return doGetFluxChatResponse(this.prompt).map(r -> {
                if (r.getResult() == null || r.getResult().getOutput() == null
                        || r.getResult().getOutput().getContent() == null) {
                    return "";
                }
                return r.getResult().getOutput().getContent();
            }).filter(StringUtils::hasText);
        }

    }

    public static class DefaultChatClientPromptRequestSpec implements ChatClientPromptRequestSpec {

        private final ChatModel chatModel;

        private final Prompt prompt;

        public DefaultChatClientPromptRequestSpec(ChatModel chatModel, Prompt prompt) {
            this.chatModel = chatModel;
            this.prompt = prompt;
        }

        public CallPromptResponseSpec call() {
            return new DefaultCallPromptResponseSpec(this.chatModel, this.prompt);
        }

        public StreamPromptResponseSpec stream() {
            return new DefaultStreamPromptResponseSpec(this.chatModel, this.prompt);
        }

    }

}
