package com.eastrobot.arch.llms.retry;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * <p>Exponential Backoff properties.</p>
 *
 * @author han.sun
 * @version 6.0.0
 * @since 2024/8/14 16:12
 */
@Data
@NoArgsConstructor
public class Backoff {
    /**
     * 0:固定时间、1:范围内随机间隔、2:指数退避策略
     */
    private int type;
    private Duration fixedInterval = Duration.ofMillis(1000);

    private Duration minInterval;
    /**
     * Initial sleep duration.
     */
    private Duration initialInterval;
    /**
     * Backoff interval multiplier.
     */
    private int multiplier;
    /**
     * Maximum backoff duration.
     */
    private Duration maxInterval;

    public Backoff(Duration fixedInterval) {
        this.type = 0;
        this.fixedInterval = fixedInterval;
    }

    public Backoff(Duration minInterval, Duration maxInterval) {
        this.type = 1;
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
    }

    public Backoff(Duration initialInterval, int multiplier, Duration maxInterval) {
        this.type = 2;
        this.initialInterval = initialInterval;
        this.multiplier = multiplier;
        this.maxInterval = maxInterval;
    }

    public static Backoff fixed(Duration interval) {
        return new Backoff(interval);
    }

    public static Backoff random(Duration minInterval, Duration maxInterval) {
        return new Backoff(minInterval, maxInterval);
    }

    // exponential
    public static Backoff exponential(Duration initialInterval, int multiplier, Duration maxInterval) {
        return new Backoff(initialInterval, multiplier, maxInterval);
    }


}
