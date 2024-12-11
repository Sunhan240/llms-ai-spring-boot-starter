package com.eastrobot.arch.llms.chat.openai;

import com.eastrobot.arch.llms.chat.messages.Message;
import com.eastrobot.arch.llms.chat.prompt.Prompt;
import com.eastrobot.arch.llms.chat.prompt.PromptTemplate;
import com.eastrobot.arch.llms.chat.prompt.SystemPromptTemplate;
import com.eastrobot.arch.llms.model.ModelOptionsUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * <p></p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/8/19 10:56
 */
//@SpringBootTest(classes = ChatApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class StringTemplateTest {
    protected ST st;

    private Map<String, Object> dynamicModel = new HashMap<>();

    protected String template;

    @Value("classpath:/prompts/system-message.st")
    private Resource systemResource;

    private void add(String name, Object value) {
        this.st.add(name, value);
        this.dynamicModel.put(name, value);
    }

    @Test
    void test1() {
        try {
//            this.template = renderResource(systemResource);
            this.template = "\"You are a helpful AI assistant. Your name is {name}.\n" +
                    "You are an AI assistant that helps people find information.\n" +
                    "Your name is {name}\n" +
                    "You should reply to the user's request with your name and also in the style of a {voice}.";
            Map<String, Object> model = new HashMap<>();
            model.put("name", "孙寒");
            model.put("voice", "嘻嘻");
            model.put("platform", "web");

            this.st = new ST(this.template, '{', '}');
            for (Map.Entry<String, Object> entry : model.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
            System.out.println(this.st.render());
            System.out.println(this.dynamicModel);
        } catch (Exception ex) {
            throw new IllegalArgumentException("The template string is not valid.", ex);
        }
    }

    @Test
    void test2() {
        Map<String, String> variables = new HashMap<String, String>() {{
            put("姓名", "你的名字");
            put("日期", "订票时间");
            put("地址", "预定机构的地址");
        }};
        String jsonSchema = ModelOptionsUtils.toJsonString(variables);

        this.template = "###输出结果### 1、输出格式:{json_schema}；2、确保输出格式可以被Python的json.loads方法解析.";
        Map<String, Object> model = new HashMap<>();
        model.put("json_schema", jsonSchema);
        this.st = new ST(this.template, '{', '}');
        for (Map.Entry<String, Object> entry : model.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        System.out.println(this.st.render());
        System.out.println(this.dynamicModel);
    }

    @Test
    void test3() {
        this.template = "#人物设定#\\n你是一个饮品客服{name_sunhan}，需要与用户进行对话，引导用户完成饮品的下单。\\n#具体要求#\\" +
                "n1.引导>客户依次确认要喝的{name} ，以及{speciality}和需要的{sugar}\\n饮品名称和饮品特性有固定关系：\\" +
                "n1）饮品特性是只能加冰或者不加冰的{name}包括：美式>咖啡，经典拿铁，卡布奇诺，香草拿铁，榛果拿铁\\" +
                "n2）饮品特性是只能做热饮的{name}包括：燕麦拿铁，生椰拿铁，摩卡拿铁，玛奇朵拿铁，爆汁甜玉米 ，爆汁\n" +
                "甜玉米奶昔 ，燕麦牛奶 ，可可奶， 港式奶茶\\n3）饮品特性是只能做冷饮的{name}包括：冰芭乐果茸果汁，冰青蜜柚果汁，" +
                "芭乐冰美式，青蜜柚冰美式，冰黄皮\n" +
                "油柑茶 ，黄皮冰美式\\n2.{name}都需要确认{sugar}，{sugar}包括：有糖，无糖。" +
                "\\n3.确认上述信息后，需要跟用户进行一次最终确认，最终确认成功后完成下单>。";


        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("name", "饮品名称");
            put("speciality", "饮品特性");
//            put("sugar", "糖度");
            put("name_sunhan", "孙寒");
        }};
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(template);
        Message message = systemPromptTemplate.createMessage(variables);
        System.out.println(message.getContent());
    }

    @Test
    void createMessage() {
        try {
//            this.template = renderResource(systemResource);
            this.template = "\"You are a helpful AI assistant. Your name is {name}.\n" +
                    "You are an AI assistant that helps people find information.\n" +
                    "Your name is {name}\n" +
                    "You should reply to the user's request {brand} with your name and also in the style of a {voice}.";
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(template);
            Map<String, Object> model = new HashMap<>();
            model.put("name", "孙寒");
            model.put("voice", "嘻嘻");
            model.put("platform", "web");
            model.put("brand", "孙寒");
            Message message = systemPromptTemplate.createMessage(model);
            System.out.println(message + "\n");

            Prompt prompt = systemPromptTemplate.create(model);
            System.out.println(prompt + "\n");

            String render = systemPromptTemplate.render(model);
            System.out.println(render);

            String s = "\"You are a helpful AI assistant. Your name is 哔哩哔哩";
            SystemPromptTemplate systemPromptTemplate2 = new SystemPromptTemplate(s);
            System.out.println(systemPromptTemplate2.createMessage());
            System.out.println(systemPromptTemplate2.create());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String renderResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            System.out.println("getInputStream:" + inputStream);
            return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read resource", ex);
        }
    }

    @Test
    void test() {
        this.template = "#人物设定#\\n你是一个饮品客服<name_sunhan>，需要与用户进行对话，引导用户完成饮品的下单。\n#具体要求#\n" +
                "1.引导>客户依次确认要喝的<name>，以及<speciality>和需要的<sugar>\\n饮品名称和饮品特性有固定关系：\n" +
                "1）饮品特性是只能加冰或者不加冰的<name>包括：美式>咖啡，经典拿铁，卡布奇诺，香草拿铁，榛果拿铁\n" +
                "2）饮品特性是只能做热饮的<name>包括：燕麦拿铁，生椰拿铁，摩卡拿铁，玛奇朵拿铁，爆汁甜玉米 ，爆汁" +
                "甜玉米奶昔 ，燕麦牛奶 ，可可奶， 港式奶茶\n" +
                "3）饮品特性是只能做冷饮的<name>包括：冰芭乐果茸果汁，冰青蜜柚果汁，" +
                "芭乐冰美式，青蜜柚冰美式，冰黄皮\n" +
                "油柑茶 ，黄皮冰美式\n" +
                "2.<name>都需要确认<糖度>，<sugar>包括：有糖，无糖。\n" +
                "3.确认上述信息后，需要跟用户进行一次最终确认，最终确认成功后完成下单>。";

        this.st = new ST(this.template, '<', '>');

        st.add("name", "饮品名称");
        st.add("speciality", "饮品特性");
        st.add("sugar", "糖度");
        st.add("糖度", "糖度");
        st.add("name_sunhan", "孙寒");
//        Map<String, Object> variables = new HashMap<String, Object>() {{
//            put("name", "饮品名称");
//            put("speciality", "饮品特性");
////            put("sugar", "糖度");
//            put("name_sunhan", "孙寒");
//        }};
//        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(template);
//        Message message = systemPromptTemplate.createMessage(variables);
//        System.out.println(message.getContent());

        System.out.println(st.render(Locale.CHINA));

    }

}
