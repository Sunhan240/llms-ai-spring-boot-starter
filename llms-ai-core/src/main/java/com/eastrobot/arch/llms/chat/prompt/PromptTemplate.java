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
package com.eastrobot.arch.llms.chat.prompt;

import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.messages.UserMessage;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.compiler.STLexer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author sunhan
 */
public class PromptTemplate implements PromptTemplateActions, PromptTemplateMessageActions {

    private final ST st;

    private final Map<String, Object> dynamicModel = new HashMap<>();

    protected String template;

    protected TemplateFormat templateFormat = TemplateFormat.ST;

    public PromptTemplate(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            this.template = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read resource", ex);
        }
        try {
            this.st = new ST(this.template, '{', '}');
        } catch (Exception ex) {
            throw new IllegalArgumentException("The template string is not valid.", ex);
        }
    }

    public PromptTemplate(String template) {
        this.template = template;
        // If the template string is not valid, an exception will be thrown
        try {
            this.st = new ST(this.template, '{', '}');
        } catch (Exception ex) {
            throw new IllegalArgumentException("The template string is not valid.", ex);
        }
    }

    public PromptTemplate(String template, Map<String, Object> model) {
        this.template = template;
        // If the template string is not valid, an exception will be thrown
        try {
            this.st = new ST(this.template, '{', '}');
            for (Entry<String, Object> entry : model.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("The template string is not valid.", ex);
        }
    }

    public PromptTemplate(Resource resource, Map<String, Object> model) {
        try (InputStream inputStream = resource.getInputStream()) {
            this.template = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read resource", ex);
        }
        // If the template string is not valid, an exception will be thrown
        try {
            this.st = new ST(this.template, '{', '}');
            for (Entry<String, Object> entry : model.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("The template string is not valid.", ex);
        }
    }

    public void add(String name, Object value) {
        this.st.add(name, value);
        this.dynamicModel.put(name, value);
    }

    public String getTemplate() {
        return this.template;
    }

    public TemplateFormat getTemplateFormat() {
        return this.templateFormat;
    }

    /**
     * Render Methods
     */
    @Override
    public String render() {
        validate(this.dynamicModel);
        return st.render();
    }

    @Override
    public String render(Map<String, Object> model) {
        validate(model);
        for (Entry<String, Object> entry : model.entrySet()) {
            if (this.st.getAttribute(entry.getKey()) != null) {
                this.st.remove(entry.getKey());
            }
            if (entry.getValue() instanceof Resource) {
                this.st.add(entry.getKey(), renderResource((Resource) entry.getValue()));
            } else {
                this.st.add(entry.getKey(), entry.getValue());
            }

        }
        return this.st.render();
    }

    private String renderResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read resource", ex);
        }
    }

    @Override
    public Message createMessage() {
        return new UserMessage(render());
    }

	/*@Override
	public Message createMessage(List<Media> mediaList) {
		return new UserMessage(render(), mediaList);
	}*/

    @Override
    public Message createMessage(Map<String, Object> model) {
        return new UserMessage(render(model));
    }

    @Override
    public Prompt create() {
        return new Prompt(render(new HashMap<>()));
    }

    @Override
    public Prompt create(Map<String, Object> model) {
        return new Prompt(render(model));
    }

    public Set<String> getInputVariables() {
        TokenStream tokens = this.st.impl.tokens;
        Set<String> inputVariables = new HashSet<>();
        boolean isInsideList = false;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getType() == STLexer.LDELIM && i + 1 < tokens.size()
                    && tokens.get(i + 1).getType() == STLexer.ID) {
                if (i + 2 < tokens.size() && tokens.get(i + 2).getType() == STLexer.COLON) {
                    inputVariables.add(tokens.get(i + 1).getText());
                    isInsideList = true;
                }
            } else if (token.getType() == STLexer.RDELIM) {
                isInsideList = false;
            } else if (!isInsideList && token.getType() == STLexer.ID) {
                inputVariables.add(token.getText());
            }
        }

        return inputVariables;
    }

    private Set<String> getModelKeys(Map<String, Object> model) {
        Set<String> dynamicVariableNames = new HashSet<>(this.dynamicModel.keySet());
        Set<String> modelVariables = new HashSet<>(model.keySet());
        modelVariables.addAll(dynamicVariableNames);
        return modelVariables;
    }

    protected void validate(Map<String, Object> model) {

        Set<String> templateTokens = getInputVariables();
        Set<String> modelKeys = getModelKeys(model);
        // 修复主动传入的变量校验缺失报错
        templateTokens.forEach(key -> {
            if (!modelKeys.contains(key)) model.put(key, key);
        });
        // Check if model provides all keys required by the template
        if (!modelKeys.containsAll(templateTokens)) {
            templateTokens.removeAll(modelKeys);
            throw new IllegalStateException(
                    "Not all template variables were replaced. Missing variable names are " + templateTokens);
        }
    }

}
