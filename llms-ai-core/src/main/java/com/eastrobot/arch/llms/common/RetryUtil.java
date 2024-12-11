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

import com.eastrobot.arch.llms.retry.Backoff;
import com.eastrobot.arch.llms.retry.RetryPolicy;
import com.eastrobot.arch.llms.retry.TransientAiException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

import java.util.concurrent.TimeUnit;

/**
 * RetryUtils is a utility class for configuring and handling retry operations. It
 * provides a default RetryTemplate and a default ResponseErrorHandler.
 *
 * @author han.sun
 * @since 1.0.0
 */
@Slf4j
public abstract class RetryUtil {

    private static final Cache<String, RetryTemplate> CACHE =
            Caffeine.newBuilder().maximumSize(300).expireAfterAccess(300, TimeUnit.SECONDS).build();

    public static RetryTemplate build(RetryPolicy retryPolicy) {
        if (retryPolicy == null) retryPolicy = new RetryPolicy();
        String key = retryPolicy.toString();
        RetryTemplate retryTemplate = CACHE.getIfPresent(key);
        if (retryTemplate == null) {
            if (log.isDebugEnabled())
                log.debug("Retry instance Cache invalidation，rebuild...");
            RetryTemplateBuilder builder = RetryTemplate.builder();
            Backoff backoff = retryPolicy.getBackoff();
            if (backoff == null)
                backoff = new Backoff();
            if (backoff.getType() == 0)
                builder.fixedBackoff(backoff.getFixedInterval().toMillis());
            else if (backoff.getType() == 1)
                builder.uniformRandomBackoff(backoff.getMinInterval().toMillis(), backoff.getMaxInterval().toMillis());
            else
                builder.exponentialBackoff(backoff.getInitialInterval().toMillis(),
                        backoff.getMultiplier(), backoff.getMaxInterval().toMillis());

            if (retryPolicy.getThrowables() != null && !retryPolicy.getThrowables().isEmpty())
                builder.retryOn(retryPolicy.getThrowables());
            else
                builder.retryOn(TransientAiException.class);
            builder.maxAttempts(retryPolicy.getMaxAttempts() > 0 ? retryPolicy.getMaxAttempts()
                    : SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS);
            retryTemplate = builder.build();
            retryTemplate.registerListener(new RetryListener() {
                @Override
                public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                    return true;
                }

                @Override
                public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                }

                @Override
                public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    log.warn("{} Retry error. Retry count: {}", context.getAttribute("conversation_id"), context.getRetryCount(), throwable);
                }
            });
            CACHE.put(key, retryTemplate);
        }
        return retryTemplate;
    }

}
