package com.eastrobot.arch.llms.chat.api;

import com.eastrobot.arch.llms.common.ApiUtils;
import com.eastrobot.arch.llms.model.ModelOptionsUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>model chat api</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/11 14:21
 */
@Data
@Slf4j
public class LlmsAiApi {

    public static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

    protected String modelName;
    protected WebClient webClient;
    protected String baseUrl;
    protected String apiKey;
    protected String uri;

    public LlmsAiApi(String modelName, String baseUrl) {
        this(modelName, baseUrl, null);
    }

    public LlmsAiApi(String modelName, String baseUrl, String apiKey) {
        this(modelName, baseUrl, apiKey, null);
    }

    public LlmsAiApi(String modelName, String baseUrl, String apiKey, Function<String, Integer> timeoutProperties) {
        this(modelName, baseUrl, apiKey, timeoutProperties, null);
    }

    public LlmsAiApi(String modelName, String baseUrl, String apiKey, Function<String, Integer> timeoutProperties, Consumer<HttpHeaders> consumer) {
        this(modelName, baseUrl, "", apiKey, timeoutProperties, consumer);

    }

    public LlmsAiApi(String modelName, String baseUrl, String uri, String apiKey, Function<String, Integer> timeoutProperties, Consumer<HttpHeaders> consumer) {
        Assert.state(StringUtils.isNotBlank(modelName), "The model name cannot be empty!");
        Assert.state(StringUtils.isNotBlank(baseUrl), "The model baseUrl cannot be empty!");
        this.baseUrl = baseUrl;
        this.uri = uri;
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.webClient = ApiUtils.builder(baseUrl, apiKey, timeoutProperties, consumer);
    }

    public <TReq, TRes> TRes chatCompletionEntity(TReq chatRequest, Class<TRes> clazz) {
        return this.webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(Mono.just(chatRequest), chatRequest.getClass())
                .retrieve()
                .bodyToMono(clazz)
                .block()
                ;
    }

    public <TReq, TRes> TRes chatCompletionEntity(TReq chatRequest, Function<String, TRes> parseFunc) {
        return this.webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(Mono.just(chatRequest), chatRequest.getClass())
                .retrieve()
                .bodyToMono(String.class)
                .map(parseFunc)
                .block()
                ;
    }

    public String chatCompletion(String callJson) {
        return this.webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .bodyValue(callJson)
                .retrieve()
                .bodyToMono(String.class)
                .block()
                ;
    }

    public <TReq, TRes> Flux<TRes> chatCompletionStream(TReq chatRequest, Class<TRes> clazz) {
        return this.webClient
                .post()
//                .uri("/v1/chat/completions")
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(Mono.just(chatRequest), chatRequest.getClass())
                .retrieve()
                .bodyToFlux(String.class)
                // cancels the flux stream after the "[DONE]" is received.
                .takeUntil(SSE_DONE_PREDICATE)
                // filters out the "[DONE]" message.
                .filter(SSE_DONE_PREDICATE.negate())
                .mapNotNull(content -> {
                    if (log.isDebugEnabled())
                        log.debug("chat api stream:{}", content);
                    return ModelOptionsUtils.jsonToObject(content, clazz);
                })
                ;
    }

    public <TReq, TRes> Flux<TRes> chatCompletionStream(TReq chatRequest, Function<String, TRes> parseFunc) {
        return this.webClient
                .post()
//                .uri("/v1/chat/completions")
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .body(Mono.just(chatRequest), chatRequest.getClass())
                .retrieve()
                .bodyToFlux(String.class)
                // cancels the flux stream after the "[DONE]" is received.
                .takeUntil(SSE_DONE_PREDICATE)
                // filters out the "[DONE]" message.
                .filter(SSE_DONE_PREDICATE.negate())
                .map(parseFunc)
                ;
    }


}
