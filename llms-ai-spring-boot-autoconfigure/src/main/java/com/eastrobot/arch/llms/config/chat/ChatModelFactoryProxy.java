package com.eastrobot.arch.llms.config.chat;

import com.eastrobot.arch.llms.chat.ChatModelProvider;
import com.eastrobot.arch.llms.chat.ChatProperties;
import com.eastrobot.arch.llms.chat.api.LlmsAiApi;
import com.eastrobot.arch.llms.chat.openai.OpenAiChatModel;
import com.eastrobot.arch.llms.chat.openai.OpenAiChatOptions;
import com.eastrobot.arch.llms.config.rest.LlmsRestProperties;
import com.eastrobot.arch.llms.model.ModelOptionsUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * <p>工厂Bean生成代理类</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/22 17:22
 */
@Slf4j
@Component
public class ChatModelFactoryProxy implements EnvironmentAware, BeanDefinitionRegistryPostProcessor {

    private OpenAiChatProperties chatProperties;
    private LlmsRestProperties restProperties;

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
        if (this.chatProperties != null) {
            List<OpenAiChatProperties.ChatProperties> models = chatProperties.getModels();
            if (!CollectionUtils.isEmpty(models)) {
                for (OpenAiChatProperties.ChatProperties model : models) {
                    if (model.isEnabled()) {
                        String modelName = model.getName();
                        OpenAiChatOptions options = model.getOptions();
                        String optionModelName = options.getModel();
                        if (StringUtils.isAllBlank(optionModelName, modelName))
                            throw new IllegalArgumentException("The model name must not be empty !");
                        modelName = StringUtils.isNotBlank(optionModelName) ? optionModelName : modelName;
                        options.setModel(modelName);
                        LlmsAiApi llmsAiApi = new LlmsAiApi(modelName, model.getBaseUrl(), model.getUri(),
                                model.getApiKey(), restProperties == null ? null : this::getTimeout,
                                headers -> headers.addAll(CollectionUtils.toMultiValueMap(model.getHeaders())));
                        //  follow the OpenAI api standard
                        if (model.isStandard()) {
                            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(OpenAiChatModel.class);
                            GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                            definition.getPropertyValues().add("modelName", modelName);
                            definition.getPropertyValues().add("llmsAiApi", llmsAiApi);
                            definition.getPropertyValues().add("chatOptions", options);
                            definition.setBeanClass(ChatModelFactoryBean.class);
                            definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);

                            registry.registerBeanDefinition(modelName.replace("-", "").replace(" ", "") + "ChatModelImpl", definition);
                        } else
                            log.info("The chat model :【{}】 requires a custom implementation on the application side!", model.getName());
                        // customizer
                        ChatModelProvider.putApi(modelName, llmsAiApi);
                        ChatModelProvider.putOptions(modelName, options);

                        ChatProperties copyProps = ModelOptionsUtils.jsonToObject(ModelOptionsUtils.toJsonString(model), ChatProperties.class);
                        ChatModelProvider.putProps(modelName, copyProps);

                    } else log.error("the chat model:【{}】 is disabled !", model.getName());
                }
            } else
                log.warn("The chat model is not configured!");
        }
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        ConfigurationProperties openaiChatProps = OpenAiChatProperties.class.getAnnotation(ConfigurationProperties.class);
        ConfigurationProperties llmsRestProps = LlmsRestProperties.class.getAnnotation(ConfigurationProperties.class);
        try {
            this.chatProperties = Binder.get(environment).bind(openaiChatProps.prefix(), OpenAiChatProperties.class).get();
            try {
                this.restProperties = Binder.get(environment).bind(llmsRestProps.prefix(), LlmsRestProperties.class).get();
            } catch (NoSuchElementException e) {
                log.warn("The properties : [{}] is not configured, use the default policy!", LlmsRestProperties.CONFIG_PREFIX);
                this.restProperties = new LlmsRestProperties();
            }
            if (log.isDebugEnabled()) {
                log.debug("chatProperties: {}", ModelOptionsUtils.toJsonString(chatProperties));
                log.debug("restProperties: {}", ModelOptionsUtils.toJsonString(restProperties));
            }
        } catch (NoSuchElementException e) {
            log.warn("The properties : [{}] is not configured !", OpenAiChatProperties.CONFIG_PREFIX);
        }
    }

    private Integer getTimeout(String param) {
        switch (param) {
            case "connectTimeout":
                return restProperties.getConnectTimeout();
            case "readTimeout":
                return restProperties.getReadTimeout();
            case "writeTimeout":
                return restProperties.getWriteTimeout();
            default:
                return 30000;
        }
    }

}
