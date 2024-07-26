package net.jmp.demo.redis.impl;

/*
 * (#)Pipelining.java   0.10.0  07/26/2024
 *
 * @author   Jonathan Parker
 * @version  0.10.0
 * @since    0.10.0
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

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import net.jmp.demo.redis.api.Demo;

import net.jmp.demo.redis.config.Config;

import org.redisson.api.*;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

/*
 * The class that demonstrates using Redis pipelining.
 */
public final class Pipelining extends Demo {
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
    public Pipelining(final Config config, final RedissonClient client) {
        super(config, client);
    }

    /**
     * The go method.
     */
    @Override
    public void go() {
        this.logger.entry();

        final int numItems = 100_567;

        final RSet<String> identifiers = this.client.getSet("identifiers");

        this.loadData(identifiers, numItems);
        this.removeData(identifiers);

        // Sets are deleted if they no longer have items in them

        if (identifiers.delete())
            this.logger.debug("Set '{}' deleted", "identifiers");

        this.logger.exit();
    }

    /**
     * Load the data.
     *
     * @param   identifiers org.redisson.api.RSet
     * @param   count       int
     */
    private void loadData(final RSet<String> identifiers, final int count) {
        this.logger.entry(identifiers, count);

        assert identifiers != null;
        assert identifiers.isEmpty();

        this.logger.debug("Creating buckets and loading the identifiers set");

        for (int i = 0; i < count; i++) {
            final String id = UUID.randomUUID().toString();
            final RBucket<String> bucket = this.client.getBucket(id);

            bucket.set("Value: " + id);

            identifiers.add(id);
        }

        this.logger.debug("There are {} identifiers", identifiers.size());

        this.logger.exit();
    }

    /**
     * Remove the data.
     *
     * @param   identifiers org.redisson.api.RSet
     */
    private void removeData(final RSet<String> identifiers) {
        this.logger.entry();

        assert identifiers != null;
        assert !identifiers.isEmpty();

        final int batchSize = 1_000;

        while (!identifiers.isEmpty()) {
            final Set<String> subset = identifiers.random(batchSize);
            final RBatch batch = this.client.createBatch(BatchOptions.defaults());

            subset.forEach(item -> {
                batch.getBucket(item).getAndDeleteAsync();
                batch.getSet("identifiers").removeAsync(item);
            });

            final RFuture<Long> future = batch.getAtomicLong("counter").incrementAndGetAsync();

            future.whenComplete((result, exception) -> {
                this.logger.debug("Done batch {}", String.valueOf(result));
            });

            batch.execute();

            try {
                future.get();
            } catch (final InterruptedException ie) {
                this.logger.catching(ie);
                Thread.currentThread().interrupt();
            } catch (final ExecutionException ee) {
                this.logger.catching(ee);
            }
        }

        final RAtomicLong counter = this.client.getAtomicLong("counter");

        if (counter.delete())
            this.logger.debug("Counter '{}' deleted", "counter");

        this.logger.exit();
    }
}
