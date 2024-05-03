package net.jmp.demo.redis;

/*
 * (#)Publishing.java   0.2.0   05/03/2024
 *
 * @author   Jonathan Parker
 * @version  0.2.0
 * @since    0.2.0
 *
 * MIT License
 *
 * Copyright (c) 2024 Jonathan M. Parker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.time.Duration;

import org.redisson.api.RSemaphore;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

/**
 * A class that demonstrates using Redis for publishing and subscribing to a topic.
 */
final class Publishing {
    /** The logger. */
    private final XLogger logger = new XLogger(LoggerFactory.getLogger(this.getClass().getName()));

    /** The application configuration. */
    private final Config config;

    /** The Redisson client. */
    private final RedissonClient client;

    /**
     * The constructor that takes
     * the application configuration.
     *
     * @param   config  net.jmp.demo.redis.Config
     * @param   client  org.redisson.api.RedissonClient
     *
     */
    Publishing(final Config config, final RedissonClient client) {
        super();

        this.config = config;
        this.client = client;
    }

    void go() {
        this.logger.entry();

        final String topicName = "jonathanp62";
        final String semaphoreName = "topic-semaphore";
        final RTopic subscribedTopic = this.client.getTopic(topicName);
        final RSemaphore semaphore = this.client.getSemaphore(semaphoreName);

        if (semaphore.trySetPermits(1)) {
            if (semaphore.tryAcquire()) {
                subscribedTopic.addListener(String.class, (channel, string) -> {
                    this.logger.info("Published message: {}", string);
                    semaphore.release();
                });

                final RTopic publishedTopic = this.client.getTopic(topicName);

                publishedTopic.publish("This is my message to publish");

                try {
                    semaphore.tryAcquire(Duration.ofSeconds(5));
                } catch (final InterruptedException ie) {
                    this.logger.catching(ie);
                    Thread.currentThread().interrupt();
                }

                subscribedTopic.removeAllListeners();
            } else
                this.logger.warn("Unable to acquire a permit for semaphore '{}'", semaphoreName);
        } else
            this.logger.warn("Unable to set one permit for semaphore '{}'", semaphoreName);

        if (semaphore.delete())
            this.logger.info("Semaphore '{}' deleted", semaphoreName);

        this.logger.exit();
    }
}
