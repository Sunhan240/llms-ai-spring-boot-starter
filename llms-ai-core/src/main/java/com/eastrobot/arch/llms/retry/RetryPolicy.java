package com.eastrobot.arch.llms.retry;

import com.eastrobot.arch.llms.chat.model.ChatResponse;
import com.eastrobot.arch.llms.model.ModelOptionsUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

/**
 * <p>重试机制</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/8/14 16:08
 */
@Data
public class RetryPolicy {
    private String id;
    /**
     * 最大重试次数
     */
    private int maxAttempts = 3;
    /**
     * 退避策略，重试的时间间隔
     */
    private Backoff backoff;
    /**
     * 重试断言，True: 中断、False: 继续
     */
    private Predicate<ChatResponse> predicate;
    /**
     * 指定哪些异常会被捕获重试
     */
    private List<Class<? extends Throwable>> throwables;
    /**
     * 如果重试失败，是否使用最后一次回调结果
     */
    private boolean useLastCallback = true;
    /**
     * 如果重试失败，使用默认兜底话术
     */
    private String recoveryCallback;


    @Override
    public String toString() {
        return "RetryPolicy{" +
                "id='" + id + '\'' +
                ", maxAttempts=" + maxAttempts +
                ", backoff=" + backoff +
//                ", predicate=" + predicate +
                ", throwables=" + throwables +
                ", useLastCallback=" + useLastCallback +
                ", recoveryCallback='" + recoveryCallback + '\'' +
                '}';
    }

    public static void main(String[] args) {
        RetryPolicy retryPolicy = new RetryPolicy();
        retryPolicy.setPredicate(precheck());
        retryPolicy.setBackoff(new Backoff());
        retryPolicy.setId("");
        System.out.println(ModelOptionsUtils.toJsonString(retryPolicy));
        System.out.println(retryPolicy);

        RetryPolicy retryPolicy2 = new RetryPolicy();
        retryPolicy2.setPredicate(precheck());
        retryPolicy2.setId("");
        retryPolicy2.setBackoff(Backoff.fixed(Duration.ofMillis(1000)));
        System.out.println(retryPolicy2);
        System.out.println(retryPolicy2.equals(retryPolicy));
    }

    static Predicate<ChatResponse> precheck() {
        return chatResponse -> {
            try {
                String content = chatResponse.getResult().getOutput().getContent();
                new ObjectMapper().readTree(content);
                return true;// 如果满足预想结果，中断重试操作
            } catch (JsonProcessingException e) {
                return false;
            }
        };
    }
}
