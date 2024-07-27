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

    /** The map of key sets. */
    private RMap<String, RSet<String>> keySetMap;

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

        final String mapName = "key-set-map";

        this.keySetMap = this.client.getMap(mapName);

        final String setName = UUID.randomUUID() + "-key-set";
        final RSet<String> keySet = this.client.getSet(setName);

        this.keySetMap.put(setName, keySet);

        this.loadData(setName);
        this.removeData(setName);

        // Sets are deleted if they no longer have items in them

        if (keySet.isExists()) {
            if (keySet.delete()) {
                this.logger.debug("Set '{}' deleted", setName);
            }
        } else {
            this.logger.debug("Set '{}' no longer exists", setName);
        }

        if (this.keySetMap.isExists()) {
            if (this.keySetMap.delete()) {
                this.logger.debug("Map '{}' deleted", mapName);
            }
        } else {
            this.logger.debug("Map '{}' no longer exists", mapName);
        }

        this.logger.exit();
    }

    /**
     * Load the data.
     *
     * @param   setName java.lang.String
     */
    private void loadData(final String setName) {
        this.logger.entry(setName);

        assert setName != null;

        final String batchNumber = "loadBatchNumber";
        final int numItems = 100_789;

        this.logger.debug("Creating buckets and loading the key set");

        final int batchSize = 5_000;

        int itemsLeft = numItems;

        final long startTime = System.currentTimeMillis();

        while (itemsLeft > 0) {
            final RBatch batch = this.client.createBatch(BatchOptions.defaults());
            final int limit = Math.min(batchSize, itemsLeft);

            for (int i = 0; i < limit; i++) {
                final String id = UUID.randomUUID().toString();

                batch.getBucket(id).setAsync("Value: " + id);
                batch.getSet(setName).addAsync(id);

                itemsLeft--;
            }

            final RFuture<Long> future = batch.getAtomicLong(batchNumber).incrementAndGetAsync();

            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    this.logger.error(exception.getMessage());
                } else {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Done loading batch {}", String.valueOf(result));
                    }
                }
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

        this.logger.info("Loading data took {} ms", System.currentTimeMillis() - startTime);

        this.deleteCounter(this.client.getAtomicLong(batchNumber), batchNumber);

        final RSet<String> keySet = this.keySetMap.get(setName);

        if (keySet.size() == numItems) {
            this.logger.debug("There are {} keys", keySet.size());
        } else {
            this.logger.debug("There should be {} keys, not {}", numItems, keySet.size());
        }

        this.logger.exit();
    }

    /**
     * Remove the data.
     *
     * @param   setName java.lang.String
     */
    private void removeData(final String setName) {
        this.logger.entry(setName);

        assert setName != null;

        final String batchNumber = "removeBatchNumber";

        this.logger.debug("Removing buckets and items from the identifiers set");

        final int batchSize = 5_000;
        final RSet<String> keySet = this.keySetMap.get(setName);
        final long startTime = System.currentTimeMillis();

        while (!keySet.isEmpty()) {
            final Set<String> subset = keySet.random(batchSize);
            final RBatch batch = this.client.createBatch(BatchOptions.defaults());

            subset.forEach(item -> {
                batch.getBucket(item).getAndDeleteAsync();
                batch.getSet(setName).removeAsync(item);
            });

            final RFuture<Long> future = batch.getAtomicLong(batchNumber).incrementAndGetAsync();

            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    this.logger.catching(exception);
                } else {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Done removing batch {}", String.valueOf(result));
                    }
                }
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

        this.logger.info("Removing data took {} ms", System.currentTimeMillis() - startTime);

        this.deleteCounter(this.client.getAtomicLong(batchNumber), batchNumber);

        if (keySet.isEmpty()) {
            this.logger.debug("Key set '{}' is empty", setName);
        } else {
            this.logger.debug("There should be 0 keys, not {}", keySet.size());
        }

        this.logger.exit();
    }

    /**
     * Delete the specified counter.
     *
     * @param   counter org.redisson.api.RAtomicLong
     */
    private void deleteCounter(final RAtomicLong counter, final String name) {
        this.logger.entry(counter, name);

        assert counter != null;
        assert name != null;

        if (counter.delete())
            this.logger.debug("Counter '{}' deleted", name);

        this.logger.exit();
    }
}
