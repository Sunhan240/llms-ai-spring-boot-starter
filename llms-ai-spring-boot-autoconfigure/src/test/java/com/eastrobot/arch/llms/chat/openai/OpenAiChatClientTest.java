package com.eastrobot.arch.llms.chat.openai;

import com.eastrobot.arch.llms.chat.api.LlmsAiApi;
import com.eastrobot.arch.llms.chat.client.ChatClient;
import com.eastrobot.arch.llms.chat.messages.AssistantMessage;
import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.messages.SystemMessage;
import com.eastrobot.arch.llms.chat.messages.UserMessage;
import com.eastrobot.arch.llms.chat.model.ChatResponse;
import com.eastrobot.arch.llms.chat.prompt.Prompt;
import com.eastrobot.arch.llms.config.chat.OpenAiChatProperties;
import com.eastrobot.arch.llms.model.ModelOptionsUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

class OpenAiChatClientTest {

    @Test
    void call() {
        String str = "{\"models\":[{\"apiKey\":\"sunhan\",\"baseUrl\":\"https://api.openai.com\",\"name\":\"gpt-4o\",\"desc\":\"openai\",\"enabled\":true,\"standard\":true,\"uri\":\"/v1/chat/completions\",\"options\":{\"streamUsage\":false,\"model\":\"gpt-4o\",\"presence_penalty\":2.0,\"response_format\":{\"type\":\"text\"},\"temperature\":0.2,\"custom\":{\"dims\":256,\"modelName\":\"gpt\"}}},{\"apiKey\":\"sunhan\",\"baseUrl\":\"https://api.openai.com\",\"name\":\"gpt3.5\",\"desc\":\"openai\",\"enabled\":true,\"standard\":true,\"uri\":\"/v1/chat/completions\",\"options\":{\"streamUsage\":false,\"model\":\"\",\"top_logprobs\":1,\"presence_penalty\":2.0,\"response_format\":{\"type\":\"text\"},\"temperature\":0.7,\"custom\":{\"key\":\"hello\"}}},{\"apiKey\":\"sk-Ou7qoeUx3ddwivmV4YPrURizkMMOG0SGsH1ARIJy0tLMB9bE\",\"baseUrl\":\"https://api.moonshot.cn\",\"uri\":\"v1/chat/completions\",\"name\":\"moonshot-v1-8k\",\"enabled\":true,\"standard\":true,\"options\":{\"streamUsage\":false,\"model\":\"moonshot-v1-8k\",\"temperature\":0.3,\"custom\":{\"dims\":256,\"modelName\":\"gpt\"}}}]}";
        OpenAiChatProperties chatProperties = ModelOptionsUtils.jsonToObject(str, OpenAiChatProperties.class);
        assert chatProperties != null;
        Map<String, OpenAiChatProperties.ChatProperties> map = chatProperties.getModels().stream().collect(Collectors.toMap(OpenAiChatProperties.ChatProperties::getName, Function.identity()));
        OpenAiChatProperties.ChatProperties properties = map.get("moonshot-v1-8k");
        OpenAiChatOptions chatOptions = properties.getOptions();
        String modelName = properties.getName();
        String apiKey = properties.getApiKey();
        String baseUrl = properties.getBaseUrl();
        LlmsAiApi llmsAiApi = new LlmsAiApi(modelName, baseUrl, apiKey);
        if (StringUtils.isNotBlank(properties.getUri()))
            llmsAiApi.setUri(properties.getUri());
        OpenAiChatModel chatModel = new OpenAiChatModel(llmsAiApi, chatOptions);

        /*String content = ChatClient.create(chatModel).prompt()
                .user(u -> u.text("请回答我最近的{country}大选目前的情况是怎样的")
                        .param("country", "美国"))
                .system(s -> s.text("假设你目前是个非常专业的问答机器人，能够理解{systemPrompt}")
                        .param("systemPrompt", "知识库的问题和答案")).call().content();
        System.out.println(content);*/

        ChatClient chatClient = ChatClient.create(chatModel);
        Flux<String> flux = chatClient.prompt()
                .user(u -> u.text("请回答我最近的{country}大选目前的情况是怎样的")
                        .param("country", "美国"))
                .system(s -> s.text("假设你目前是个非常专业的问答机器人，能够理解{systemPrompt}")
                        .param("systemPrompt", "知识库的问题和答案")).stream().content();
        String join = String.join("", Objects.requireNonNull(flux.collectList().block()));
        System.out.println(join);

        System.out.println("------");

        flux = chatClient.prompt()
                .user(u -> u.text("请回答我通用大模型{model}的优点")
                        .param("model", "月之暗面kimi"))
                .system(s -> s.text("假设你目前是个非常擅长分析{method}的人工智能模型")
                        .param("method", "技术方案")).stream().content();
        join = String.join("", Objects.requireNonNull(flux.collectList().block()));
        System.out.println(join);
//        String call = chatModel.call("美国大选目前的情况是怎样的");
//        System.out.println(call);
    }


    @Test
    void call2() {
        String modelName = "hz30_v1";
        String apiKey = null;
        String baseUrl = "http://122.226.240.96:10099/v1/chat/completions";
        LlmsAiApi llmsAiApi = new LlmsAiApi(modelName, baseUrl, apiKey);
//        if (StringUtils.isNotBlank(properties.getUri()))
//            llmsAiApi.setUri(properties.getUri());
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .withModel("hz30_v1")
                .withTemperature(0.1f)
                .withStreamUsage(false)
                .build();
        OpenAiChatModel chatModel = new OpenAiChatModel(llmsAiApi, chatOptions);
        List<Message> messages = new ArrayList<>();
        UserMessage userMessage = new UserMessage("信用卡的年费制度是怎么样的，什么样的情况下可以免年费");
        messages.add(userMessage);
        SystemMessage systemMessage = new SystemMessage("假设你目前是个非常专业的问答机器人，能够理解知识库的问题和答案");
        messages.add(systemMessage);
        Prompt prompt = new Prompt(messages, chatOptions);
        ChatResponse chatResponse = chatModel.call(prompt);
        System.out.println(chatResponse.getResult().getOutput().getContent());
//        String call = chatModel.call("");
//        System.out.println(call);
    }

    @Test
    void call3() {
        String modelName = "hz30_v1";
        String apiKey = null;
        String baseUrl = "http://122.226.240.96:10099/v1/chat/completions";
        LlmsAiApi llmsAiApi = new LlmsAiApi(modelName, baseUrl, apiKey);
//        if (StringUtils.isNotBlank(properties.getUri()))
//            llmsAiApi.setUri(properties.getUri());
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .withModel("hz30_v1")
                .withTemperature(0.1f)
                .withStreamUsage(false)
                .build();
        OpenAiChatModel chatModel = new OpenAiChatModel(llmsAiApi, chatOptions);
        List<Message> messages = new ArrayList<>();
        UserMessage userMessage = new UserMessage("总结上述对话的内容");
        messages.add(userMessage);
        SystemMessage systemMessage = new SystemMessage("假设你目前是个非常专业的问答机器人，能够理解知识库的问题和答案");
        messages.add(systemMessage);

        String a1 = "信用卡的年费制度是指信用卡公司向持卡人收取的年度费用。通常情况下，信用卡年费是信用卡公司为了维持信用卡业务运营而收取的费用。年费的金额因信用卡种类和银行而异，通常在几百元到几千元之间。\n" +
                "\n" +
                "信用卡年费的收取方式也因银行而异。有些银行会在持卡人首次使用信用卡时收取年费，有些银行则会在持卡人使用信用卡满一年后收取年费。此外，有些银行还会提供免年费的优惠，例如在信用卡激活后的前几个月内免年费，或者在持卡人达到一定消费额度后免年费。\n" +
                "\n" +
                "如果你想知道信用卡年费的具体情况，建议你咨询你的信用卡发卡银行。";
        AssistantMessage assistantMessage = new AssistantMessage(a1);
        messages.add(assistantMessage);
        Prompt prompt = new Prompt(messages, chatOptions);
        ChatResponse chatResponse = chatModel.call(prompt);
        System.out.println(chatResponse.getResult().getOutput().getContent());
    }


}