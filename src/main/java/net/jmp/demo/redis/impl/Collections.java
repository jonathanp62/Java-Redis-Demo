package net.jmp.demo.redis.impl;

/*
 * (#)Collections.java  0.8.0   06/14/2024
 * (#)Collections.java  0.3.0   05/03/2024
 *
 * @author   Jonathan Parker
 * @version  0.8.0
 * @since    0.3.0
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

import java.util.Map;

import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

import net.jmp.demo.redis.api.Demo;

import net.jmp.demo.redis.config.Config;

/*
 * The class that demonstrates using Redis for
 * collections, in this case map, set, and list.
 */
public final class Collections extends Demo {
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
    public Collections(final Config config, final RedissonClient client) {
        super(config, client);
    }

    /**
     * The go method.
     */
    @Override
    public void go() {
        this.logger.entry();

        this.map();
        this.set();
        this.list();

        this.logger.exit();
    }

    /**
     * Work with map.
     */
    private void map() {
        this.logger.entry();

        final RMap<String, String> myFamily = this.client.getMap("my-family");

        String prevWifeEntry = myFamily.put("wife", "Wendy");

        if (prevWifeEntry != null)
            this.logger.debug("Previous value for 'wife' was {}", prevWifeEntry);

        final String prevDaughterEntry = myFamily.put("daughter", "Lauren");

        if (prevDaughterEntry != null)
            this.logger.debug("Previous value for 'daughter' was {}", prevDaughterEntry);

        final String prevSonEntry = myFamily.put("son", "Michael");

        if (prevSonEntry != null)
            this.logger.debug("Previous value for 'son' was {}", prevSonEntry);

        this.logMap(myFamily);

        prevWifeEntry = myFamily.put("wife", "Dena");

        if (prevWifeEntry != null)
            this.logger.debug("Previous value for 'wife' was {}", prevWifeEntry);

        this.logMap(myFamily);

        myFamily.remove("son");
        myFamily.remove("daughter");
        myFamily.remove("wife");

        if (myFamily.delete())
            this.logger.debug("Map '{}' deleted", "my-family");

        this.logger.exit();
    }

    /**
     * Log the specified map.
     *
     * @param   map java.util.Map&lt;java.lang.String, java.lang.String&gt;
     */
    private void logMap(final Map<String, String> map) {
        this.logger.entry(map);

        assert map != null;
        assert !map.isEmpty();

        for (final var entry : map.entrySet()) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Entry {}: {}", entry.getKey(), entry.getValue());
            }
        }

        this.logger.exit();
    }

    /**
     * Work with set.
     */
    private void set() {
        this.logger.entry();

        final RSet<String> composers = this.client.getSet("composers");

        composers.add("Bach");
        composers.add("Mozart");
        composers.add("Mahler");
        composers.add("Beethoven");
        composers.add("Bruckner");
        composers.add("Bach");      // Will not be added to the set

        this.logger.info("There are {} items in the 'composers' set", composers.size());

        composers.forEach(composer -> this.logger.info("Composer: {}", composer));

        if (composers.delete())
            this.logger.debug("Set '{}' deleted", "composers");

        this.logger.exit();
    }

    /**
     * Work with list.
     */
    private void list() {
        this.logger.entry();

        final RList<Integer> ages = this.client.getList("ages");

        ages.add(62);
        ages.add(60);
        ages.add(69);
        ages.add(35);
        ages.add(31);

        this.logger.info("There are {} items in the 'ages' list", ages.size());

        ages.forEach(age -> this.logger.info("Age: {}", age));

        if (ages.delete())
            this.logger.debug("List '{}' deleted", "ages");

        this.logger.exit();
    }
}
