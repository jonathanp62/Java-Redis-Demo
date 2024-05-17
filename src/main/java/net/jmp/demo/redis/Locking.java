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
import org.redisson.api.RReadWriteLock;
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

        final RLock myLock = this.client.getLock("lock");

        try {
            if (!myLock.isLocked()) {
                myLock.lock();

                this.logger.info("Acquired 'my-lock'");

                this.spin();

                myLock.unlock();

                this.logger.info("Released 'my-lock'");
            }
        } catch (final Exception e) {
            this.logger.catching(e);

            myLock.unlock();

            this.logger.warn("Released 'my-lock'");
        }

        this.logger.exit();
    }

    /**
     * Work with multi-lock.
     */
    private void multiLock() {
        this.logger.entry();

        final RLock myLock1 = this.client.getLock("lock-1");
        final RLock myLock2 = this.client.getLock("lock-2");
        final RLock myLock3 = this.client.getLock("lock-3");

        final RedissonMultiLock myLock = new RedissonMultiLock(myLock1, myLock2, myLock3);

        try {
            if (!myLock1.isLocked() && !myLock2.isLocked() && !myLock3.isLocked()) {
                myLock.lock();

                this.logger.info("Acquired 'multi-lock'");

                this.spin();

                myLock.unlock();

                this.logger.info("Released 'multi-lock'");
            }
        } catch (final Exception e) {
            this.logger.catching(e);

            myLock.unlock();

            this.logger.warn("Released 'multi-lock'");
        }

        this.logger.exit();
    }

    /**
     * Work with the read-write lock.
     */
    private void readWriteLock() {
        this.logger.entry();

        this.readOnlyLock();
        this.writeOnlyLock();

        this.logger.exit();
    }

    /**
     * Work with the read-only lock.
     */
    private void readOnlyLock() {
        this.logger.entry();

        final RReadWriteLock rwLock = this.client.getReadWriteLock("read-write-lock-1");
        final RLock myLock = rwLock.readLock();

        try {
            if (!myLock.isLocked()) {
                myLock.lock();

                this.logger.info("Acquired 'read-lock'");

                this.spin();

                myLock.unlock();

                this.logger.info("Released 'read-lock'");
            }
        } catch (final Exception e) {
            this.logger.catching(e);

            myLock.unlock();

            this.logger.warn("Released 'read-lock'");
        }

        this.logger.exit();
    }

    /**
     * Work with the write-only lock. The write
     * lock is not obtainable if the read lock
     * is already held so one does not obtain the
     * read lock and then escalalate to the write
     * lock.
     */
    private void writeOnlyLock() {
        this.logger.entry();

        final RReadWriteLock rwLock = this.client.getReadWriteLock("read-write-lock-2");
        final RLock myLock = rwLock.writeLock();

        try {
            if (!myLock.isLocked()) {
                myLock.lock();

                this.logger.info("Acquired 'write-lock'");

                this.spin();

                myLock.unlock();

                this.logger.info("Released 'write-lock'");
            }
        } catch (final Exception e) {
            this.logger.catching(e);

            myLock.unlock();

            this.logger.warn("Released 'write-lock'");
        }

        this.logger.exit();
    }

    /**
     * Perform a 'long' running operation.
     */
    private void spin() {
        this.logger.entry();

        int x = 10_000;

        while (x > 0)
            x--;

        this.logger.exit();
    }
}
