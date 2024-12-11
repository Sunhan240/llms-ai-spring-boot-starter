package com.eastrobot.arch.llms.chat.openai;

import com.eastrobot.arch.llms.chat.api.LlmsAiApi;
import com.eastrobot.arch.llms.chat.messages.AssistantMessage;
import com.eastrobot.arch.llms.chat.messages.MessageType;
import com.eastrobot.arch.llms.chat.messages.UserMessage;
import com.eastrobot.arch.llms.chat.metadata.ChatGenerationMetadata;
import com.eastrobot.arch.llms.chat.metadata.ChatResponseMetadata;
import com.eastrobot.arch.llms.chat.metadata.EmptyUsage;
import com.eastrobot.arch.llms.chat.metadata.RateLimit;
import com.eastrobot.arch.llms.chat.model.ChatModel;
import com.eastrobot.arch.llms.chat.model.ChatResponse;
import com.eastrobot.arch.llms.chat.model.Generation;
import com.eastrobot.arch.llms.chat.openai.metadata.OpenAiUsage;
import com.eastrobot.arch.llms.chat.openai.model.*;
import com.eastrobot.arch.llms.chat.prompt.ChatOptions;
import com.eastrobot.arch.llms.chat.prompt.Prompt;
import com.eastrobot.arch.llms.model.ModelOptionsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <p>openai chat model</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/18 16:45
 */
@Slf4j
public class OpenAiChatModel implements ChatModel {

    private final LlmsAiApi llmsAiApi;
    private final OpenAiChatOptions defaultOptions;

    /**
     * Creates an instance of the OpenAiChatModel.
     *
     * @param openAiApi The OpenAiApi instance to be used for interacting with the OpenAI
     *                  Chat API.
     * @throws IllegalArgumentException if openAiApi is null
     */
    public OpenAiChatModel(LlmsAiApi openAiApi) {
        this(openAiApi,
                OpenAiChatOptions.builder().withModel(openAiApi.getModelName()).withTemperature(0.3f).build());
    }

    /**
     * Initializes an instance of the OpenAiChatModel.
     *
     * @param openAiApi The OpenAiApi instance to be used for interacting with the OpenAI Chat API.
     * @param options   The OpenAiChatOptions to configure the chat model.
     */
    public OpenAiChatModel(LlmsAiApi openAiApi, OpenAiChatOptions options) {
        Assert.notNull(openAiApi, "AiApi must not be null");
        Assert.notNull(options, "Options must not be null");
        this.llmsAiApi = openAiApi;
        this.defaultOptions = options;
    }

    @Override
    public String model() {
        return this.defaultOptions.getModel();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        ChatCompletionRequest request = createRequest(prompt, false);
        if (log.isDebugEnabled())
            log.debug("openai request:{}", ModelOptionsUtils.toJsonString(request));
        ChatCompletion chatCompletion = this.llmsAiApi.chatCompletionEntity(request, ChatCompletion.class);
        if (log.isDebugEnabled())
            log.debug("openai completion:{}", ModelOptionsUtils.toJsonString(chatCompletion));
        if (chatCompletion == null) {
            log.warn("No chat completion returned for prompt: {}", prompt);
            return new ChatResponse(new ArrayList<>());
        }

       /* if (isToolFunctionCall(chatCompletion)) {
            List<Message> toolCallMessageConversation = this.handleToolCallRequests(prompt.getInstructions(),
                    chatCompletion);
            // Recursively call the call method with the tool call message
            // conversation that contains the call responses.

            return this.call(new Prompt(toolCallMessageConversation, prompt.getOptions()));
        }*/

        // Non function calling.
//        RateLimit rateLimits = OpenAiResponseHeaderExtractor.extractAiResponseHeaders(completionEntity);

        List<ChatCompletion.Choice> choices = chatCompletion.getChoices();
        if (choices == null) {
            log.warn("No choices returned for prompt: {}", prompt);
            return new ChatResponse(new ArrayList<>());
        }
        List<Generation> generations = choices.stream().map(choice -> {
            Map<String, Object> metadata = new HashMap<String, Object>() {{
                put("id", chatCompletion.getId());
                put("role", choice.getMessage().getRole() != null ? choice.getMessage().getRole().name() : "");
                put("index", choice.getIndex());
                put("finishReason", choice.getFinishReason() != null ? choice.getFinishReason().name() : "");
            }};
            /*Map<String, Object> metadata = Map.of("id", chatCompletion.getId(), "role",
                    choice.getMessage().getRole() != null ? choice.getMessage().getRole().name() : "", "finishReason",
                    choice.getFinishReason() != null ? choice.getFinishReason().name() : "");*/
            Generation generation = new Generation(choice.getMessage().content(), metadata);
            if (choice.getFinishReason() != null) {
                generation = generation
                        .withGenerationMetadata(ChatGenerationMetadata.from(choice.getFinishReason().name(), null));
            }
            return generation;

        }).collect(Collectors.toList());

        return new ChatResponse(generations, from(chatCompletion, null));
        /*return new ChatResponse(generations,
                OpenAiChatResponseMetadata.from(chatCompletion).withRateLimit(rateLimits));*/

//        return null;
    }

    ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {

//        Set<String> functionsForThisRequest = new HashSet<>();
        List<ChatCompletionMessage> chatCompletionMessages = prompt.getInstructions().stream().map(message -> {
            if (message.getMessageType() == MessageType.USER || message.getMessageType() == MessageType.SYSTEM) {
                Object content = message.getContent();
                if (message instanceof UserMessage) {
                    UserMessage userMessage = (UserMessage) message;
                    if (!CollectionUtils.isEmpty(userMessage.getMedia())) {
                        List<MediaContent> contentList = new ArrayList<>(
                                Collections.singletonList(new MediaContent(message.getContent())));
                        contentList.addAll(userMessage.getMedia()
                                .stream()
                                .map(media -> new MediaContent(new MediaContent.ImageUrl(
                                        this.fromMediaData(media.getMimeType(), media.getData()))))
                                .collect(Collectors.toList()));
                        content = contentList;
                    }
                }
                return Collections.singletonList(new ChatCompletionMessage(content, Role.valueOf(message.getMessageType().name())));
            } else if (message.getMessageType() == MessageType.ASSISTANT) {
                AssistantMessage assistantMessage = (AssistantMessage) message;
                /*List<ToolCall> toolCalls = null;
                if (!CollectionUtils.isEmpty(assistantMessage.getToolCalls())) {
                    toolCalls = assistantMessage.getToolCalls().stream().map(toolCall -> {
                        var function = new ChatCompletionFunction(toolCall.name(), toolCall.arguments());
                        return new ToolCall(toolCall.id(), toolCall.type(), function);
                    }).toList();
                }*/
                return Collections.singletonList(new ChatCompletionMessage(assistantMessage.getContent(),
                        Role.ASSISTANT, null, null, null));
            }
            /*else if (message.getMessageType() == MessageType.FUNCTION) {
                ToolResponseMessage toolMessage = (ToolResponseMessage) message;

                toolMessage.getResponses().forEach(response -> {
                    Assert.isTrue(response.id() != null, "ToolResponseMessage must have an id");
                    Assert.isTrue(response.name() != null, "ToolResponseMessage must have a name");
                });

                return toolMessage.getResponses()
                        .stream()
                        .map(tr -> new ChatCompletionMessage(tr.responseData(), ChatCompletionMessage.Role.TOOL, tr.name(),
                                tr.id(), null))
                        .toList();
            }*/
            else {
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
            }
        }).flatMap(List::stream).collect(Collectors.toList());

        ChatCompletionRequest request = new ChatCompletionRequest(chatCompletionMessages, stream);

        if (prompt.getOptions() != null) {
            OpenAiChatOptions updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(),
                    ChatOptions.class, OpenAiChatOptions.class);

            /*Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions,
                    IS_RUNTIME_CALL);
            functionsForThisRequest.addAll(promptEnabledFunctions);*/

            request = ModelOptionsUtils.merge(updatedRuntimeOptions, request, ChatCompletionRequest.class);
        }

        if (this.defaultOptions != null) {

            /*Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions,
                    !IS_RUNTIME_CALL);

            functionsForThisRequest.addAll(defaultEnabledFunctions);*/

            request = ModelOptionsUtils.merge(request, this.defaultOptions, ChatCompletionRequest.class);
        }

        // Add the enabled functions definitions to the request's tools parameter.
        /*if (!CollectionUtils.isEmpty(functionsForThisRequest)) {

            request = ModelOptionsUtils.merge(
                    OpenAiChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(),
                    request, ChatCompletionRequest.class);
        }*/

        // Remove `streamOptions` from the request if it is not a streaming request
        if (request.getStreamOptions() != null && !stream) {
            log.warn("Removing streamOptions from the request as it is not a streaming request!");
            request = request.withStreamOptions(null);
        }
        return request;
    }

    private String fromMediaData(MimeType mimeType, Object mediaContentData) {
        if (mediaContentData instanceof byte[]) {
            byte[] bytes = (byte[]) mediaContentData;
            // Assume the bytes are an image. So, convert the bytes to a base64 encoded
            // following the prefix pattern.
            return String.format("data:%s;base64,%s", mimeType.toString(), Base64.getEncoder().encodeToString(bytes));
        } else if (mediaContentData instanceof String) {

            // Assume the text is a URLs or a base64 encoded image prefixed by the user.
            return (String) mediaContentData;
        } else {
            throw new IllegalArgumentException(
                    "Unsupported media data type: " + mediaContentData.getClass().getSimpleName());
        }
    }

    private ChatResponseMetadata from(ChatCompletion result, RateLimit rateLimit) {
        Assert.notNull(result, "OpenAI ChatCompletionResult must not be null");
        ChatResponseMetadata.Builder builder = ChatResponseMetadata.builder()
                .withId(result.getId() != null ? result.getId() : "")
                .withUsage(result.getUsage() != null ? OpenAiUsage.from(result.getUsage()) : new EmptyUsage())
                .withModel(result.getModel() != null ? result.getModel() : "")
                .withKeyValue("created", result.getCreated() != null ? result.getCreated() : 0L)
                .withKeyValue("system-fingerprint", result.getSystemFingerprint() != null ? result.getSystemFingerprint() : "");
        if (rateLimit != null) {
            builder.withRateLimit(rateLimit);
        }
        return builder.build();
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return OpenAiChatOptions.fromOptions(this.defaultOptions);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        ChatCompletionRequest request = createRequest(prompt, true);
        log.info("openai stream:{}", ModelOptionsUtils.toJsonString(request));
        Flux<ChatCompletionChunk> completionChunks = this.llmsAiApi.chatCompletionStream(request, ChatCompletionChunk.class);
        ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

        // Convert the ChatCompletionChunk into a ChatCompletion to be able to reuse
        // the function call handling logic.
        Flux<ChatResponse> chatResponse = completionChunks.map(this::chunkToChatCompletion)
                .switchMap(chatCompletion -> Mono.just(chatCompletion).map(chatCompletion2 -> {
                    try {
                        @SuppressWarnings("null")
                        String id = chatCompletion2.getId();
                        // @formatter:off
                        List<Generation> generations = chatCompletion2.getChoices().stream().map(choice -> {
                            if (choice.getMessage().getRole() != null) {
                                roleMap.putIfAbsent(id, choice.getMessage().getRole().name());
                            }
                            Map<String, Object> metadata = new HashMap<String, Object>() {{
                                put("id", chatCompletion2.getId());
                                put("role", roleMap.getOrDefault(id, ""));
                                put("finishReason", choice.getFinishReason() != null ? choice.getFinishReason().name() : "");
                            }};
                            Generation generation = new Generation(choice.getMessage().content(), metadata);
                            if (choice.getFinishReason() != null) {
                                generation = generation
                                        .withGenerationMetadata(ChatGenerationMetadata.from(choice.getFinishReason().name(), null));
                            }
                            return generation;
                        }).collect(Collectors.toList());
                        // @formatter:on

                        if (chatCompletion2.getUsage() != null) {
                            return new ChatResponse(generations, from(chatCompletion2, null));
                        } else {
                            return new ChatResponse(generations);
                        }
                    } catch (Exception e) {
                        log.error("Error processing chat completion", e);
                        return new ChatResponse(Collections.emptyList());
                    }
                }));

        /*if (isToolCall(response, OpenAiApi.ChatCompletionFinishReason.TOOL_CALLS.name())) {
                var toolCallConversation = handleToolCalls(prompt, response);
                // Recursively call the stream method with the tool call message
                // conversation that contains the call responses.
                return this.stream(new Prompt(toolCallConversation, prompt.getOptions()));
            }
            else {*/
        //            }
        return chatResponse.flatMap(Flux::just);


//        return ChatModel.super.stream(prompt);
    }

    /*private ChatResponseMetadata from(ChatCompletion result) {
        Assert.notNull(result, "OpenAI ChatCompletionResult must not be null");
        return ChatResponseMetadata.builder()
                .withId(result.id())
                .withUsage(OpenAiUsage.from(result.usage()))
                .withModel(result.model())
                .withKeyValue("created", result.created())
                .withKeyValue("system-fingerprint", result.systemFingerprint())
                .build();
    }*/

    private ChatCompletion chunkToChatCompletion(ChatCompletionChunk chunk) {
        List<ChatCompletion.Choice> choices = chunk.getChoices()
                .stream()
                .map(chunkChoice -> new ChatCompletion.Choice(chunkChoice.getFinishReason(), chunkChoice.getIndex(), chunkChoice.getDelta(),
                        chunkChoice.getLogprobs()))
                .collect(Collectors.toList());

        return new ChatCompletion(chunk.getId(), choices, chunk.getCreated(), chunk.getModel(),
                chunk.getSystemFingerprint(), "chat.completion", chunk.getUsage());
    }
}
