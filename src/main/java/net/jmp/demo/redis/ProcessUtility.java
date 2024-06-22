package net.jmp.demo.redis;

/*
 * (#)ProcessUtility.java   0.9.0   06/22/2024
 *
 * @author   Jonathan Parker
 * @version  0.9.0
 * @since    0.9.0
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

import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

public final class ProcessUtility {
    private static final XLogger logger;

    static {
        logger = new XLogger(LoggerFactory.getLogger(ProcessUtility.class.getName()));
    }

    /**
     * A hidden constructor.
     */
    private ProcessUtility() {
        throw new IllegalStateException("ProcessUtility contains only static methods");
    }

    /**
     * Return true if the Redis server is running.
     *
     * @return  boolean
     */
    public static boolean isRedisServerRunning() {
        logger.entry();

        final boolean result = isProcessRunning("redis-server");

        logger.exit(result);

        return result;
    }

    /**
     * Return true if the Redis stack server is running.
     *
     * @return  boolean
     */
    public static boolean isRedisStackServerRunning() {
        logger.entry();

        final boolean result = isProcessRunning("redis-stack-server");

        logger.exit(result);

        return result;
    }

    /**
     * Return true if the Redis server is running.
     *
     * @return  boolean
     */
    private static boolean isProcessRunning(final String processString) {
        logger.entry(processString);

        boolean result = false;

        // To issue a command with pipes the following syntax is required

        final List<String> arguments = List.of(
                "/bin/sh",
                "-c",
                "ps -ef|grep " + processString + "|grep -v grep"
        );

        final ProcessBuilder processBuilder = new ProcessBuilder(arguments);

        try {
            final Process process = processBuilder.start();
            final String output = new String(process.getInputStream().readAllBytes());

            final int exitValue = process.waitFor();

            if (exitValue == 0 && !output.isEmpty()) {
                if (logger.isDebugEnabled())
                    logger.debug("Output from '{}' process status: {}", processString, output.trim());

                result = true;
            } else {
                logger.warn("No output from '{}' process status", processString);
            }
        } catch (final IOException ioe) {
            logger.catching(ioe);
        } catch (final InterruptedException ie) {
            logger.catching(ie);
            Thread.currentThread().interrupt();
        }

        logger.exit(result);

        return result;
    }
}
