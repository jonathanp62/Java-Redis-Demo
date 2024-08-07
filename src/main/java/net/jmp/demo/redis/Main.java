package net.jmp.demo.redis;

/*
 * (#)Main.java 0.10.0  07/26/2024
 * (#)Main.java 0.9.0   06/22/2024
 * (#)Main.java 0.8.0   06/14/2024
 * (#)Main.java 0.6.0   05/23/2024
 * (#)Main.java 0.5.0   05/18/2024
 * (#)Main.java 0.4.0   05/17/2024
 * (#)Main.java 0.3.0   05/03/2024
 * (#)Main.java 0.2.0   05/01/2024
 * (#)Main.java 0.1.0   05/01/2024
 *
 * @author   Jonathan Parker
 * @version  0.10.0
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

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;
import java.util.Optional;

import java.util.regex.Pattern;

import org.redisson.api.RedissonClient;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

import net.jmp.demo.redis.api.Demo;

import net.jmp.demo.redis.config.Architecture;
import net.jmp.demo.redis.config.Config;

import net.jmp.demo.redis.impl.*;

/*
 * The application's main class.
 */
public final class Main {
    /** The default configuration file name. */
    private static final String DEFAULT_APP_CONFIG_FILE = "config/config.json";

    /** The logger. */
    private final XLogger logger = new XLogger(LoggerFactory.getLogger(this.getClass().getName()));

    /** A regular expression pattern to get the version from the 'redis-server --version' command. */
    private final Pattern versionPattern = Pattern.compile("(?i)\\.*v=(?<version>.+?)\\s(?-i)");

    /**
     * The default constructor.
     */
    private Main() {
        super();
    }

    /**
     * The go method.
     */
    private void go() {
        this.logger.entry();

        this.logger.info("Redis-Demo {}", Version.VERSION_STRING);

        this.getAppConfig().ifPresentOrElse(appConfig -> {
            RedissonClient client = null;

            if (ProcessUtility.isRedisServerRunning(appConfig.getProcessUtility().getRedisServer())) {
                try {
                    client = this.getClient(appConfig);

                    this.logServerVersion(appConfig);

                    final List<Demo> demos = List.of(
                            new Caching(appConfig, client),
                            new Publishing(appConfig, client),
                            new Collections(appConfig, client),
                            new Locking(appConfig, client),
                            new Json(appConfig, client),
                            new LiveObjects(appConfig, client),
                            new Pipelining(appConfig, client)
                    );

                    demos.forEach(Demo::go);
                } catch (final IOException ioe) {
                    this.logger.catching(ioe);
                } finally {
                    if (client != null) {
                        Connector.disconnect(client);

                        if (client.isShutdown())
                            this.logger.info("Redisson client has shut down");
                    }
                }
            } else {
                this.logger.error("The '{}' is not running", appConfig.getProcessUtility().getRedisServer());
            }
        }, () -> this.logger.error("No configuration found for Redis-Demo"));

        this.logger.exit();
    }

    /**
     * Get the application configuration.
     *
     * @return  java.lang.Optional&lt;net.jmp.demo.redis.Config&gt;
     */
    private Optional<Config> getAppConfig() {
        this.logger.entry();

        final String configFileName = System.getProperty("app.configurationFile", DEFAULT_APP_CONFIG_FILE);

        this.logger.info("Reading the configuration from: {}", configFileName);

        Config appConfig = null;

        try {
            appConfig = new Gson().fromJson(Files.readString(Paths.get(configFileName)), Config.class);
        } catch (final IOException ioe) {
            this.logger.catching(ioe);
        }

        this.logger.exit(appConfig);

        return Optional.ofNullable(appConfig);
    }

    /**
     * Get the Redisson client.
     *
     * @param   config  net.jmp.demo.redis.Config
     * @return          org.redisson.api.RedissonClient
     */
    private RedissonClient getClient(final Config config) {
        this.logger.entry(config);

        assert config != null;

        final var connector = new Connector(
                config.getRedis().getHostName(),
                config.getRedis().getPort(),
                config.getRedis().getProtocol()
        );

        final var client = connector.connect();

        this.logger.exit(client);

        return client;
    }

    /**
     * Log the server version.
     *
     * @param   config  net.jmp.demo.redis.Config
     * @throws          java.io.IOException
     */
    private void logServerVersion(final Config config) throws IOException {
        this.logger.entry(config);

        assert config != null;

        final var architecture = this.getArchitecture();

        String command;

        if (architecture == Architecture.INTEL)
            command = config.getRedis().getServerCLI().getCommandIntel();
        else if (architecture == Architecture.APPLE_SILICON)
            command = config.getRedis().getServerCLI().getCommandSilicon();
        else
            throw new IllegalStateException("Unsupported architecture: " + architecture);

        final StringBuilder sb = new StringBuilder();
        final Process process = new ProcessBuilder(
                command,
                config.getRedis().getServerCLI().getArgument()
            )
            .redirectErrorStream(true)
            .start();

        try (final var processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = processOutputReader.readLine()) != null) {
                sb.append(line);
            }

            process.waitFor();

            if (process.exitValue() == 0) {
                final var matcher = this.versionPattern.matcher(sb.toString());

                if (matcher.find()) {
                    final var version = matcher.group("version");

                    if (version != null)
                        this.logger.info("Redis server {}", version);
                    else {
                        if (this.logger.isWarnEnabled())
                            this.logger.warn("Group 'version' not found in {}", sb.toString());
                    }
                } else {
                    if (this.logger.isWarnEnabled())
                        this.logger.warn("No match on {}", sb.toString());
                }
            } else {
                if (this.logger.isWarnEnabled())
                    this.logger.warn(
                            "Process failed: {}",
                            process.info().commandLine().orElse(
                                    command +
                                            ' ' +
                                            config.getRedis().getServerCLI().getArgument()
                            )
                    );
            }
        } catch (final InterruptedException ie) {
            this.logger.catching(ie);
            Thread.currentThread().interrupt();     // Restore the interrupt status
        }

        this.logger.exit();
    }

    /**
     * Return the architecture.
     *
     * @return  net.jmp.demo.redis.config.Architecture
     * @throws  java.io.IOException
     */
    private Architecture getArchitecture() throws IOException {
        this.logger.entry();

        Architecture result = Architecture.INTEL;

        final StringBuilder sb = new StringBuilder();
        final Process process = new ProcessBuilder(
                "/usr/sbin/sysctl",
                "-n",
                "machdep.cpu.brand_string"
            )
            .redirectErrorStream(true)
            .start();

        try (final var processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = processOutputReader.readLine()) != null) {
                sb.append(line);
            }

            process.waitFor();

            if (process.exitValue() == 0) {
                if (sb.toString().equals("Apple M2 Max")) {
                    result = Architecture.APPLE_SILICON;
                }
            } else {
                if (this.logger.isWarnEnabled()) {
                    this.logger.warn("Process failed: {}", process.info().commandLine().orElse("/usr/sbin/sysctl -n machdep.cpu.brand_string"));
                }
            }
        } catch (final InterruptedException ie) {
            this.logger.catching(ie);
            Thread.currentThread().interrupt();     // Restore the interrupt status
        }

        this.logger.exit(result);

        return result;
    }

    /**
     * The main method.
     *
     * @param   args    java.lang.String[]
     */
    public static void main(final String[] args) {
        new Main().go();
    }
}
