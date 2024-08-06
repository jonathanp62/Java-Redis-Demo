package net.jmp.demo.redis.impl;

/*
 * (#)Publishing.java   0.12.0  08/06/2024
 * (#)Publishing.java   0.11.0  08/05/2024
 * (#)Publishing.java   0.3.0   05/03/2024
 * (#)Publishing.java   0.2.0   05/03/2024
 *
 * @author   Jonathan Parker
 * @version  0.12.0
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

import java.util.List;

import java.util.stream.IntStream;

import net.jmp.demo.redis.Synchronizer;

import net.jmp.demo.redis.api.Demo;

import net.jmp.demo.redis.config.Config;

import org.redisson.api.*;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

/**
 * A class that demonstrates using Redis for publishing and subscribing to a topic.
 */
public final class Publishing extends Demo {
    /** The logger. */
    private final XLogger logger = new XLogger(LoggerFactory.getLogger(this.getClass().getName()));

    /** The synchronizer. */
    private final Synchronizer synchronizer = new Synchronizer();

    /**
     * The constructor that takes
     * the application configuration.
     *
     * @param   config  net.jmp.demo.redis.Config
     * @param   client  org.redisson.api.RedissonClient
     *
     */
    public Publishing(final Config config, final RedissonClient client) {
        super(config, client);
    }

    /**
     * The go method.
     */
    @Override
    public void go() {
        this.logger.entry();

        this.topics();
        this.queues();

        this.logger.exit();
    }

    /**
     * Topics.
     */
    private void topics() {
        this.logger.entry();

        final String topicName = "jonathans-topic";
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
            this.logger.debug("Semaphore '{}' deleted", semaphoreName);

        this.logger.exit();
    }

    /**
     * Queues.
     */
    private void queues() {
        this.logger.entry();

        final String queueName = "jonathans-queue";
        final RQueue<String> producer = this.client.getQueue(queueName);
        final List<Thread> listeners = createAndStartListenerThreads(queueName);
        final int max = 30;

        final List<String> elements =
                IntStream.rangeClosed(1, max)
                        .mapToObj(String::valueOf)
                        .toList();

        // Add elements to the queue

        elements.forEach(e -> {
            producer.offer(e);

            if (String.valueOf(max).equals(e)) {
                synchronized (this.synchronizer) {
                    this.synchronizer.setNotified(true);
                    this.synchronizer.notifyAll();
                }
            }
        });

        // Wait for the listeners to complete

        listeners.forEach(listener -> {
            try {
                listener.join();
            } catch (final InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        producer.delete();

        this.logger.exit();
    }

    /**
     * Create and start the listener threads.
     *
     * @param   queueName   java.lang.String
     * @return              java.util.List&lt;java.lang.Thread&gt;
     */
    private List<Thread> createAndStartListenerThreads(final String queueName) {
        this.logger.entry(queueName);

        assert queueName != null;

        final Runnable runnable = () -> {
            final RQueue<String> consumer = client.getQueue(queueName);

            while (true) {
                if (consumer.isEmpty()) {
                    waitForNotify();
                } else {
                    consumeQueue(consumer);
                    break;
                }
            }
        };

        final Thread listener1 = new Thread(runnable);
        final Thread listener2 = new Thread(runnable);
        final Thread listener3 = new Thread(runnable);

        listener1.setName("listener-thread-1");
        listener2.setName("listener-thread-2");
        listener3.setName("listener-thread-3");

        listener1.start();
        listener2.start();
        listener3.start();

        this.logger.exit();

        return List.of(listener1, listener2, listener3);
    }

    /**
     * Wait for lock notification.
     */
    private void waitForNotify() {
        this.logger.entry();

        while (true) {
            synchronized (this.synchronizer) {
                if (!this.synchronizer.isNotified()) {
                    try {
                        this.synchronizer.wait();
                    } catch (final InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }

                this.synchronizer.setNotified(false);

                break;
            }
        }

        this.logger.exit();
    }

    /**
     * Consume the elements on the queue.
     *
     * @param   queue   org.redisson.api.RQueue&lt;java.lang.String&gt;
     */
    private void consumeQueue(final RQueue<String> queue) {
        this.logger.entry(queue);

        assert queue != null;

        while (queue.peek() != null) {
            final String message = queue.poll();

            if (message != null) {
                logger.info("Received message from {}: {}", Thread.currentThread().getName(), message);
            }
        }

        this.logger.exit();
    }
}
