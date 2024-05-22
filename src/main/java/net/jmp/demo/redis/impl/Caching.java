package net.jmp.demo.redis.impl;

/*
 * (#)Caching.java  0.3.0   05/03/2024
 * (#)Caching.java  0.2.0   05/02/2024
 * (#)Caching.java  0.1.0   05/01/2024
 *
 * @author   Jonathan Parker
 * @version  0.3.0
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

import com.esotericsoftware.kryo.KryoException;

import java.util.List;

import org.redisson.api.*;

import org.redisson.client.codec.StringCodec;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

import net.jmp.demo.redis.api.Demo;

import net.jmp.demo.redis.config.Config;

/*
 * The class that demonstrates using Redis for caching.
 */
public final class Caching extends Demo {
    /** The logger. */
    private final XLogger logger = new XLogger(LoggerFactory.getLogger(this.getClass().getName()));

    /**
     * The constructor that takes
     * the application configuration.
     *
     * @param   config  net.jmp.demo.redis.Config
     * @param   client  org.redisson.api.RedissonClient
     *
     */
    public Caching(final Config config, final RedissonClient client) {
        super(config, client);
    }

    /**
     * The go method.
     */
    @Override
    public void go() {
        this.logger.entry();

        this.listBucketValues();
        this.setAndGetStrings();
        this.setAndGetObject();
        this.setAndGetAtomicLong();

        this.logger.exit();
    }

    /**
     * List the string values in all the buckets.
     */
    private void listBucketValues() {
        this.logger.entry();

        if (this.logger.isInfoEnabled()) {
            for (final var key : this.client.getKeys().getKeys()) {
                try {
                    final var bucket = this.client.getBucket(key, StringCodec.INSTANCE);

                    this.logger.info("Value of key '{}': {}", key, bucket.get());
                } catch (final KryoException ke) {
                    this.logger.catching(ke);
                }
            }
        }

        this.logger.exit();
    }

    /**
     * Set some strings and then get them.
     */
    private void setAndGetStrings() {
        this.logger.entry();

        final List<RBucket<String>> stringBuckets = List.of(
            this.client.getBucket("one"),
            this.client.getBucket("two"),
            this.client.getBucket("three")
        );

        stringBuckets.forEach(bucket -> bucket.set(bucket.getName().toUpperCase()));
        stringBuckets.forEach(bucket -> this.logger.info("Key: {}; Value: {}", bucket.getName(), bucket.get()));
        stringBuckets.forEach(RObject::delete);

        final var one = this.client.getBucket("one");

        if (one.get() == null)
            this.logger.info("Key 'one' was not found");

        this.logger.exit();
    }

    /**
     * Set a more complex object and then get it.
     */
    private void setAndGetObject() {
        this.logger.entry();

        final String bucketName = "config";
        final RBucket<Config> bucket = this.client.getBucket(bucketName);

        bucket.set(this.config);

        final var myConfig = bucket.get();

        if (this.logger.isInfoEnabled())
            this.logger.info("Bucket '{}': {}", bucketName, myConfig.toString());

        if (bucket.delete())
            this.logger.debug("Bucket '{}' deleted", bucketName);

        final var config = this.client.getBucket(bucketName);

        if (config.get() == null)
            this.logger.info("Bucket '{}' was not found", bucketName);

        this.logger.exit();
    }

    /**
     * Set an atomic long and do some operations.
     */
    private void setAndGetAtomicLong() {
        this.logger.entry();

        final String atomicLongName = "my-atomic-long";
        final RAtomicLong atomicLong = this.client.getAtomicLong(atomicLongName);

        atomicLong.set(34567);

        if (atomicLong.compareAndSet(34567, 45678))
            this.logger.info("{}: {}", atomicLongName, atomicLong.incrementAndGet());

        if (atomicLong.delete())
            this.logger.debug("AtomicLong '{}' deleted", atomicLongName);

        this.logger.exit();
    }
}

