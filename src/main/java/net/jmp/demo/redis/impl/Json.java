package net.jmp.demo.redis.impl;

/*
 * (#)Json.java 0.5.0   05/18/2024
 *
 * @author   Jonathan Parker
 * @version  0.5.0
 * @since    0.5.0
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

import com.fasterxml.jackson.core.type.TypeReference;

import org.redisson.api.RJsonBucket;
import org.redisson.api.RedissonClient;

import org.redisson.client.RedisException;

import org.redisson.codec.JacksonCodec;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

import net.jmp.demo.redis.api.Demo;

import net.jmp.demo.redis.config.*;

/*
 * The class that demonstrates using Redis
 * to serialize objects as JSON.
 */
public final class Json extends Demo {
    /** The logger. */
    private final XLogger logger = new XLogger(LoggerFactory.getLogger(this.getClass().getName()));

    /**
     * The constructor that takes
     * the application configuration.
     *
     * @param   config  net.jmp.demo.redis.Config
     * @param   client  org.redisson.api.RedissonClient
     */
    public Json(final Config config, final RedissonClient client) {
        super(config, client);
    }

    /**
     * The go method.
     */
    @Override
    public void go() {
        this.logger.entry();

        final RJsonBucket<Config> bucket = this.client.getJsonBucket("my-config", new JacksonCodec<>(Config.class));
        final String path = ".";

        try {
            if (bucket.setIfAbsent(path, this.config))
                this.logger.info("Set the 'my-config' JSON bucket");
            else {
                this.logger.info("Did not set the 'my-config' JSON bucket");

                final Config myConfig = bucket.get();

                if (myConfig.equals(this.config))
                    this.logger.info("Serialized/deserialized config matches config object");
                else
                    this.logger.info("Serialized/deserialized config does not match config object");

                this.logger.info("There are {} keys in the root", bucket.countKeys());
                this.logger.info("There are {} keys in the redis section", bucket.countKeys("redis"));
                this.logger.info("There are {} keys in the serverCLI section", bucket.countKeys("redis.serverCLI"));

                final var port = bucket.get(new JacksonCodec<>(new TypeReference<Integer>() {
                }), "redis.port");

                final var hostName = bucket.get(new JacksonCodec<>(new TypeReference<String>() {
                }), "redis.hostName");

                final var serverCli = bucket.get(new JacksonCodec<>(new TypeReference<ServerCLI>() {
                }), "redis.serverCLI");

                final var redis = bucket.get(new JacksonCodec<>(new TypeReference<Redis>() {
                }), "redis");

                this.logger.info("The port              : {}", port);
                this.logger.info("The host name         : {}", hostName);
                this.logger.info("The server-cli section: {}", serverCli);
                this.logger.info("The redis section     : {}", redis);

                // Note: This fails if the path does not refer to a string

                this.logger.info("The string size of the redis.serverCLI.command is {}", bucket.stringSize("redis.serverCLI.command"));
            }
        } catch (final RedisException re) {
            this.logger.catching(re);
        }

        this.logger.exit();
    }
}
