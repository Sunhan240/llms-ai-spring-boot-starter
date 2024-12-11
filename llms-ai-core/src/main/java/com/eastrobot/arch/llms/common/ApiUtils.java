/*
 * Copyright 2023 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eastrobot.arch.llms.common;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The ApiUtils class provides utility methods for working with API requests and responses.
 *
 * @author han.sun
 */
public class ApiUtils {

    private ApiUtils() {
    }

    public static Consumer<HttpHeaders> getJsonContentHeaders(String apiKey) {
        return (headers) -> {
            if (StringUtils.isNotBlank(apiKey))
                headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
        };
    }

    public static WebClient builder(String baseUrl) {
        return builder(baseUrl, null);
    }

    public static WebClient builder(String baseUrl, String openApiKey) {
        return builder(baseUrl, openApiKey, null, null);
    }

    public static WebClient builder(String baseUrl, String openApiKey, Function<String, Integer> restProperties) {
        return builder(baseUrl, openApiKey, restProperties, null);
    }

    public static WebClient builder(String baseUrl, String token, Function<String, Integer> restProperties, Consumer<HttpHeaders> consumer) {
        if (StringUtils.isBlank(baseUrl)) {
            throw new NullPointerException("The AI model call address cannot be empty!");
        }
        int connectTimeout = restProperties == null ? 3000 : restProperties.apply("connectTimeout");
        int readTimeout = restProperties == null ? 60000 : restProperties.apply("readTimeout");
        int writeTimeout = restProperties == null ? 60000 : restProperties.apply("writeTimeout");
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout > 0 ? connectTimeout : 3000)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(
                            new ReadTimeoutHandler(readTimeout >= 3000 ? readTimeout : 60000, TimeUnit.MILLISECONDS)
                    );
                    connection.addHandlerLast(
                            new WriteTimeoutHandler(writeTimeout >= 3000 ? writeTimeout : 60000, TimeUnit.MILLISECONDS)
                    );
                });
        WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);
        if (consumer == null)
            consumer = getJsonContentHeaders(token);
        else
            consumer = consumer.andThen(getJsonContentHeaders(token));
        builder.defaultHeaders(consumer);
        builder.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)));
        return builder.build();
    }


}
