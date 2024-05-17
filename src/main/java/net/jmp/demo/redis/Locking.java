package net.jmp.demo.redis;

/*
 * (#)Locking.java  0.4.0   05/17/2024
 * (#)Locking.java  0.3.0   05/03/2024
 * (#)Locking.java  0.2.0   05/02/2024
 * (#)Locking.java  0.1.0   05/01/2024
 *
 * @author   Jonathan Parker
 * @version  0.4.0
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

import org.redisson.RedissonMultiLock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

/*
 * The class that demonstrates using Redis for locking.
 */
final class Locking extends Demo {
    /** The logger. */
    private final XLogger logger = new XLogger(LoggerFactory.getLogger(this.getClass().getName()));

    /**
     * The constructor that takes
     * the application configuration.
     *
     * @param   config  net.jmp.demo.redis.Config
     * @param   client  org.redisson.api.RedissonClient
     */
    Locking(final Config config, final RedissonClient client) {
        super(config, client);
    }

    /**
     * The go method.
     */
    @Override
    public void go() {
        this.logger.entry();

        this.lock();
        this.multiLock();
        this.readWriteLock();

        this.logger.exit();
    }

    /**
     * Work with lock.
     */
    private void lock() {
        this.logger.entry();

        final RLock myLock = this.client.getLock("my-lock");

        try {
            myLock.lock();

            this.logger.info("Acquired 'my-lock'");

            for (int i = 0; i < 10_000; i++) {
                ;
            }
        } catch (final Throwable t) {
            this.logger.catching(t);
        } finally {
            myLock.unlock();

            this.logger.info("Released 'my-lock'");
        }

        this.logger.exit();
    }

    /**
     * Work with multi-lock.
     */
    private void multiLock() {
        this.logger.entry();

        final RLock myLock1 = this.client.getLock("my-lock-1");
        final RLock myLock2 = this.client.getLock("my-lock-2");
        final RLock myLock3 = this.client.getLock("my-lock-3");

        final RedissonMultiLock myLock = new RedissonMultiLock(myLock1, myLock2, myLock3);

        try {
            myLock.lock();

            this.logger.info("Acquired 'multi-lock'");

            for (int i = 0; i < 10_000; i++) {
                ;
            }
        } catch (final Throwable t) {
            this.logger.catching(t);
        } finally {
            myLock.unlock();

            this.logger.info("Released 'multi-lock'");
        }

        this.logger.exit();
    }

    /**
     * Work with the read-write lock.
     */
    private void readWriteLock() {
        this.logger.entry();
        this.logger.exit();
    }
}
