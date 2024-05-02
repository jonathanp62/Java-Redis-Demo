package net.jmp.demo.redis;

/*
 * (#)Caching.java  0.2.0   05/02/2024
 * (#)Caching.java  0.1.0   05/01/2024
 *
 * @author   Jonathan Parker
 * @version  0.2.0
 * @since    0.1.0
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

import org.redisson.api.RedissonClient;

import org.redisson.client.codec.StringCodec;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

/*
 * The class that demonstrates using Redis for caching.
 */
final class Caching {
    /** The logger. */
    private final XLogger logger = new XLogger(LoggerFactory.getLogger(this.getClass().getName()));

    /** The application configuration. */
    private final Config config;

    /**
     * The constructor that takes
     * the application configuration.
     *
     * @param   config  net.jmp.demo.redis.Config
     */
    Caching(final Config config) {
        super();

        this.config = config;
    }

    /**
     * The go method.
     */
    void go() {
        this.logger.entry();

        final var connector = new Connector(
                this.config.getRedis().getHostName(),
                this.config.getRedis().getPort(),
                this.config.getRedis().getProtocol()
        );

        final var client = connector.connect();

        this.listBucketValues(client);

        connector.disconnect(client);

        if (client.isShutdown())
            this.logger.info("Redisson client has shut down");

        this.logger.exit();
    }

    /**
     * List the string values in all the buckets.
     *
     * @param   client  org.redisson.api.RedissonClient
     */
    void listBucketValues(final RedissonClient client) {
        this.logger.entry(client);

        if (this.logger.isInfoEnabled()) {
            for (final var key : client.getKeys().getKeys()) {
                final var bucket = client.getBucket(key, StringCodec.INSTANCE);

                this.logger.info("Value of key '{}': {}", key, bucket.get());
            }
        }

        this.logger.exit();
    }
}

