package com.eastrobot.arch.llms.config.demo;

import com.eastrobot.arch.llms.chat.ChatModelProvider;
import com.eastrobot.arch.llms.chat.client.RequestResponseAdvisor;
import com.eastrobot.arch.llms.chat.client.advisor.MessageChatMemoryAdvisor;
import com.eastrobot.arch.llms.chat.memory.ChatMemory;
import com.eastrobot.arch.llms.chat.memory.InMemoryChatMemory;
import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.model.ChatModel;
import com.eastrobot.arch.llms.chat.model.ChatResponse;
import com.eastrobot.arch.llms.chat.openai.OpenAiChatOptions;
import com.eastrobot.arch.llms.chat.openai.model.ResponseFormat;
import com.eastrobot.arch.llms.converter.BeanOutputConverter;
import com.eastrobot.arch.llms.converter.ListOutputConverter;
import com.eastrobot.arch.llms.converter.MapOutputConverter;
import com.eastrobot.arch.llms.converter.StructuredOutputConverter;
import com.eastrobot.arch.llms.model.ModelOptionsUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Predicate;

/**
 * <p>demo</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/7/22 18:03
 */
@Slf4j
//@RestController
public class ChatController implements InitializingBean {

    @javax.annotation.Resource
    private ApplicationContext context;

    /**
     * yaml配置完后，看看容器中目前有些什么聊天模型
     */
    @GetMapping("/getBean2")
    public Set<String> getBean() {
        Map<String, ChatModel> beansOfType = context.getBeansOfType(ChatModel.class);
        //  ["robotChatModelImpl", "hz30_v1ChatModelImpl", "moonshotv18kChatModelImpl"]
        return beansOfType.keySet();
    }

    /**
     * 直接注入具体的ChatModel
     */
//    @Resource(name = "moonshotv18kChatModelImpl")
//    private OpenAiChatModel moonshotv1ChatModel;
//
//    @GetMapping("easy")
//    public String easy(String msg) {
//        // req 你好啊
//        // resp:  你好！很高兴见到你。请问有什么我可以帮助您的吗？如果您有任何问题或需要建议，请随时告诉我。
//        return moonshotv1ChatModel.call(msg);
//    }

    // 同步阻塞式
    @GetMapping("huazang")
    public String huazang(String msg) {
        // 本质与上述一致，获取基础聊天组件进行问答
        String content = ChatModelProvider.model("hz30_v1").call(msg);
        System.out.println(content);

        // 使用ChatClient高级API进行问答（推荐）
        content = ChatModelProvider.client("hz30_v1")
                .prompt()
                .user(msg)
                .call()
                .content();
        System.out.println(content);
        return content;
    }


    // #############使用增强功能#############

    // Build a store component with a memory message
    protected ChatMemory chatMemory = null;
    protected RequestResponseAdvisor memoryAdvisor = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        // in cluster mode you can choose to use the redis list data structure as this component
        this.chatMemory = new InMemoryChatMemory();
        this.memoryAdvisor = new MessageChatMemoryAdvisor(this.chatMemory);
    }

    @Value("classpath:/prompts/system-message.st")
    private Resource systemMessage;

    // 同步阻塞式
    @GetMapping("call")
    public String call(String type, String msg, String user) {
        String result = ChatModelProvider.client("hz30_v1").prompt()
                // 提词，PromptTemplate变量替换
                .user(u -> u.text("{msg}")
                        .param("msg", msg).param("conversation_id", user))
                .system(s -> s.text(systemMessage)
                        .param("ability", "善于分析以及推理能力")
                )
//                .options(StringUtils.isBlank(type) ? null :
//                        new OpenAiChatOptions.Builder().withResponseFormat(new ResponseFormat(type)).build())
                // 注册一个对话记忆的增强功能
                .advisors(advisorSpec -> advisorSpec.advisors(memoryAdvisor).params(new HashMap<String, Object>() {{
                    // 可以使用用户ID，对消息队列进行Clipping，避免内存膨胀
                    put("chat_memory_conversation_id", user);
                    // 每次从仓库中获取最近对话记录条目
                    put("chat_memory_response_size", 6);
                }}))
                // 注册重试机制，一般针对希望模型返回特殊响应报文如（Json、xml等）；如果只是普通聊天无需做重试机制
                .retry(rc -> rc
                        .maxAttempts(2) // 最大重试次数
                        .predicate(precheck()) // 预检，断言是否需要继续重试
                        // True: 最大次数重试后，还是失败使用最后一次响应输出；False: 放弃使用最后一次重试的接口响应，并使用兜底话术；
                        .useLastCallback(true)
                )
                .call()
                .content();

        List<Message> messages = this.chatMemory.get(user, 4);
        log.info("messages:{}", ModelOptionsUtils.toJsonString(messages));
        return result;
    }

    Predicate<ChatResponse> precheck() {
        return chatResponse -> {
            try {
                String content = chatResponse.getResult().getOutput().getContent();
                return !StringUtils.isBlank(content) && !content.contains("不知道");// 如果满足预想结果，中断重试操作
            } catch (Exception e) {
                return false;
            }
        };
    }

    // 响应式
    @GetMapping(value = "stream")
    public Flux<String> stream(String msg) {
        Flux<String> content = ChatModelProvider.client("moonshot-v1-8k").prompt()
                // 提词，PromptTemplate变量替换
                .user(u -> u.text("{msg}")
                        .param("msg", msg))
                .system(s -> s.text("假设你目前是个非常专业的问答机器人，能够理解{systemPrompt}.")
                        .param("systemPrompt", "知识库的问题和答案"))
                // 对基础问答功能进行增强，记录历史对话，每次请求大模型前获取历史对话。响应后对当前对话存储
                .advisors(advisorSpec -> {
                    advisorSpec.advisors(memoryAdvisor).params(new HashMap<String, Object>() {{
                        // 可以使用用户ID，对消息队列进行Clipping，避免内存膨胀
                        put("chat_memory_conversation_id", "haha");
                        // 每次从仓库中获取最近对话记录条目
                        put("chat_memory_response_size", 4);
                    }});
                }).stream().content();
        List<Message> messages = this.chatMemory.get("haha", 4);
//        log.info("messages:{}", JsonUtil.toJson(messages));
        log.info("messages:{}", ModelOptionsUtils.toJsonString(messages));
        return content;
    }


    /**
     * 应用端自我实现的扩展聊天模型，调用时API写法与标准的一致
     */
    @GetMapping("robot/call2")
    public String robot2(String msg) {
        String message = ChatModelProvider.model("robot10").call(msg);
        System.out.println(message);
        return message;
    }

    @GetMapping("robot/call3")
    public String robot3(String msg) {
        String message = ChatModelProvider.client("robot10").prompt()
                .user(msg)
//                .user(u -> u.text("{msg}").param("msg", msg))
//                .advisors(advisorSpec ->
//                        advisorSpec.advisors(memoryAdvisor).params(advisorMap))
                .call().content();
        System.out.println(message);
        return message;
    }


    // #########################  结构化输出模型返回不一定会成功  #########################

    // 结构化输出JSON列表
    @GetMapping("structured/list")
    public List<String> structuredList(String msg) {
        StructuredOutputConverter<List<String>> converter = new ListOutputConverter(new DefaultConversionService());

        String content = ChatModelProvider.client("moonshot-v1-8k")
                .prompt()
                .user(u -> u.text(msg + "\n {format} ").param("format", converter.getFormat()))
                .call()
                .content();
        System.out.println(content);
        return converter.convert(content);
    }

    // 结构化输出JSON对象
    @GetMapping("structured/obj")
    public Intention structuredObj(String msg) {
        StructuredOutputConverter<Intention> converter = new BeanOutputConverter<>(new ParameterizedTypeReference<Intention>() {
        });
        String content = ChatModelProvider.client("moonshot-v1-8k")
                .prompt()
                .system(s -> s.text("请基于上述信息提取关键要素并返回json，行为要素设置为action字段；意图目的设置为target字段；意图目的设置为target字段"))
                .user(u -> u.text(msg + "\n {format} ").param("format", converter.getFormat()))
                .call()
                .content();
        System.out.println(content);
        return converter.convert(content);
    }

    @GetMapping("structured/map")
    public Map<String, Object> structuredMap(String msg) {
        StructuredOutputConverter<Map<String, Object>> converter = new MapOutputConverter();
        String content = ChatModelProvider.client("moonshot-v1-8k")
                .prompt()
                .system(s -> s.text("请基于上述信息提取关键要素并返回json"))
                .user(u -> u.text(msg + "\n {format} ").param("format", converter.getFormat()))
                .call()
                .content();
        System.out.println(content);
        return converter.convert(content);
    }

    @Getter
    @Setter
    public static class Intention {
        private String name;
        private String from;
        private String to;
        private String desc;
        private String action;
        private String target;
        private String value;
    }

}
