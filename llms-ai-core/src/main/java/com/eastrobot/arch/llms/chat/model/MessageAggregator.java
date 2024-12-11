/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eastrobot.arch.llms.chat.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Helper that for streaming chat responses, aggregate the chat response messages into a
 * single AssistantMessage. Job is performed in parallel to the chat response processing.
 *
 * @author Christian Tzolov
 * @since 1.0.0
 */
public class MessageAggregator {

	private static final Logger logger = LoggerFactory.getLogger(MessageAggregator.class);

	public Flux<ChatResponse> aggregate(Flux<ChatResponse> fluxChatResponse,
			Consumer<ChatResponse> onAggregationComplete) {

		AtomicReference<StringBuilder> stringBufferRef = new AtomicReference<>(new StringBuilder());
		AtomicReference<Map<String, Object>> mapRef = new AtomicReference<>();

		return fluxChatResponse.doOnSubscribe(subscription -> {
			// logger.info("Aggregation Subscribe:" + subscription);
			stringBufferRef.set(new StringBuilder());
			mapRef.set(new HashMap<>());
		}).doOnNext(chatResponse -> {
			// logger.info("Aggregation Next:" + chatResponse);
			if (chatResponse.getResult() != null) {
				if (chatResponse.getResult().getOutput().getContent() != null) {
					stringBufferRef.get().append(chatResponse.getResult().getOutput().getContent());
				}
				if (chatResponse.getResult().getOutput().getMetadata() != null) {
					mapRef.get().putAll(chatResponse.getResult().getOutput().getMetadata());
				}
			}
		}).doOnComplete(() -> {
			// logger.debug("Aggregation Complete");
			onAggregationComplete
				.accept(new ChatResponse(Collections.singletonList(new Generation(stringBufferRef.get().toString(), mapRef.get()))));
			stringBufferRef.set(new StringBuilder());
			mapRef.set(new HashMap<>());
		}).doOnError(e -> logger.error("Aggregation Error", e));
	}

	/*public Flux<ChatResponse> aggregate(Flux<ChatResponse> fluxChatResponse,
										Consumer<ChatResponse> onAggregationComplete) {

		// Assistant Message
		AtomicReference<StringBuilder> messageTextContentRef = new AtomicReference<>(new StringBuilder());
		AtomicReference<Map<String, Object>> messageMetadataMapRef = new AtomicReference<>();

		// ChatGeneration Metadata
		AtomicReference<ChatGenerationMetadata> generationMetadataRef = new AtomicReference<>(
				ChatGenerationMetadata.NULL);

		// Usage
		AtomicReference<Long> metadataUsagePromptTokensRef = new AtomicReference<>(0L);
		AtomicReference<Long> metadataUsageGenerationTokensRef = new AtomicReference<>(0L);
		AtomicReference<Long> metadataUsageTotalTokensRef = new AtomicReference<>(0L);

		AtomicReference<PromptMetadata> metadataPromptMetadataRef = new AtomicReference<>(PromptMetadata.empty());
		AtomicReference<RateLimit> metadataRateLimitRef = new AtomicReference<>(new EmptyRateLimit());

		AtomicReference<String> metadataIdRef = new AtomicReference<>("");
		AtomicReference<String> metadataModelRef = new AtomicReference<>("");

		return fluxChatResponse.doOnSubscribe(subscription -> {
			messageTextContentRef.set(new StringBuilder());
			messageMetadataMapRef.set(new HashMap<>());
			metadataIdRef.set("");
			metadataModelRef.set("");
			metadataUsagePromptTokensRef.set(0L);
			metadataUsageGenerationTokensRef.set(0L);
			metadataUsageTotalTokensRef.set(0L);
			metadataPromptMetadataRef.set(PromptMetadata.empty());
			metadataRateLimitRef.set(new EmptyRateLimit());

		}).doOnNext(chatResponse -> {

			if (chatResponse.getResult() != null) {
				if (chatResponse.getResult().getMetadata() != null
						&& chatResponse.getResult().getMetadata() != ChatGenerationMetadata.NULL) {
					generationMetadataRef.set(chatResponse.getResult().getMetadata());
				}
				if (chatResponse.getResult().getOutput().getContent() != null) {
					messageTextContentRef.get().append(chatResponse.getResult().getOutput().getContent());
				}
				if (chatResponse.getResult().getOutput().getMetadata() != null) {
					messageMetadataMapRef.get().putAll(chatResponse.getResult().getOutput().getMetadata());
				}
			}
			if (chatResponse.getMetadata() != null) {
				if (chatResponse.getMetadata().getUsage() != null) {
					Usage usage = chatResponse.getMetadata().getUsage();
					metadataUsagePromptTokensRef.set(
							usage.getPromptTokens() > 0 ? usage.getPromptTokens() : metadataUsagePromptTokensRef.get());
					metadataUsageGenerationTokensRef.set(usage.getGenerationTokens() > 0 ? usage.getGenerationTokens()
							: metadataUsageGenerationTokensRef.get());
					metadataUsageTotalTokensRef
							.set(usage.getTotalTokens() > 0 ? usage.getTotalTokens() : metadataUsageTotalTokensRef.get());
				}
				if (chatResponse.getMetadata().getPromptMetadata() != null
						&& chatResponse.getMetadata().getPromptMetadata().iterator().hasNext()) {
					metadataPromptMetadataRef.set(chatResponse.getMetadata().getPromptMetadata());
				}
				if (chatResponse.getMetadata().getRateLimit() != null
						&& !(metadataRateLimitRef.get() instanceof EmptyRateLimit)) {
					metadataRateLimitRef.set(chatResponse.getMetadata().getRateLimit());
				}
				if (StringUtils.hasText(chatResponse.getMetadata().getId())) {
					metadataIdRef.set(chatResponse.getMetadata().getId());
				}
				if (StringUtils.hasText(chatResponse.getMetadata().getModel())) {
					metadataModelRef.set(chatResponse.getMetadata().getModel());
				}
			}
		}).doOnComplete(() -> {

			DefaultUsage usage = new DefaultUsage(metadataUsagePromptTokensRef.get(), metadataUsageGenerationTokensRef.get(),
					metadataUsageTotalTokensRef.get());

			ChatResponseMetadata chatResponseMetadata = ChatResponseMetadata.builder()
					.withId(metadataIdRef.get())
					.withModel(metadataModelRef.get())
					.withRateLimit(metadataRateLimitRef.get())
					.withUsage(usage)
					.withPromptMetadata(metadataPromptMetadataRef.get())
					.build();

			onAggregationComplete.accept(new ChatResponse(Collections.singletonList(new Generation(
					new AssistantMessage(messageTextContentRef.get().toString(), messageMetadataMapRef.get()),
					generationMetadataRef.get())), chatResponseMetadata));

			messageTextContentRef.set(new StringBuilder());
			messageMetadataMapRef.set(new HashMap<>());
			metadataIdRef.set("");
			metadataModelRef.set("");
			metadataUsagePromptTokensRef.set(0L);
			metadataUsageGenerationTokensRef.set(0L);
			metadataUsageTotalTokensRef.set(0L);
			metadataPromptMetadataRef.set(PromptMetadata.empty());
			metadataRateLimitRef.set(new EmptyRateLimit());

		}).doOnError(e -> {
			logger.error("Aggregation Error", e);
		});
	}

	@NoArgsConstructor
	@AllArgsConstructor
	public class DefaultUsage implements Usage {
		long promptTokens;long generationTokens; long totalTokens;
		@Override
		public Long getPromptTokens() {
			return promptTokens;
		}

		@Override
		public Long getGenerationTokens() {
			return generationTokens;
		}

		@Override
		public Long getTotalTokens() {
			return totalTokens;
		}
	}
*/
}