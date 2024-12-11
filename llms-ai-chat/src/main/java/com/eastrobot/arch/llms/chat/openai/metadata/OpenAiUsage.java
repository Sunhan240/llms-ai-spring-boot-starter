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
package com.eastrobot.arch.llms.chat.openai.metadata;

import com.eastrobot.arch.llms.chat.metadata.Usage;
import org.springframework.util.Assert;

/**
 * {@link Usage} implementation for {@literal OpenAI}.
 *
 * @author John Blum
 * @see <a href=
 * "https://platform.openai.com/docs/api-reference/completions/object">Completion
 * Object</a>
 * @since 0.7.0
 */
public class OpenAiUsage implements Usage {

    public static OpenAiUsage from(com.eastrobot.arch.llms.chat.openai.model.Usage usage) {
        return new OpenAiUsage(usage);
    }

    private final com.eastrobot.arch.llms.chat.openai.model.Usage usage;

    protected OpenAiUsage(com.eastrobot.arch.llms.chat.openai.model.Usage usage) {
        Assert.notNull(usage, "OpenAI Usage must not be null");
        this.usage = usage;
    }

    protected com.eastrobot.arch.llms.chat.openai.model.Usage getUsage() {
        return this.usage;
    }

    @Override
    public Long getPromptTokens() {
        Integer promptTokens = getUsage().getPromptTokens();
        return promptTokens != null ? promptTokens.longValue() : 0;
    }

    @Override
    public Long getGenerationTokens() {
        Integer generationTokens = getUsage().getCompletionTokens();
        return generationTokens != null ? generationTokens.longValue() : 0;
    }

    @Override
    public Long getTotalTokens() {
        Integer totalTokens = getUsage().getTotalTokens();
        if (totalTokens != null) {
            return totalTokens.longValue();
        } else {
            return getPromptTokens() + getGenerationTokens();
        }
    }

    @Override
    public String toString() {
        return getUsage().toString();
    }
}
