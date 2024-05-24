package net.jmp.demo.redis.impl;

/*
 * (#)LiveObjects.java 0.7.0   05/24/2024
 * (#)LiveObjects.java 0.6.0   05/23/2024
 *
 * @author   Jonathan Parker
 * @version  0.7.0
 * @since    0.6.0
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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.redisson.api.RedissonClient;
import org.redisson.api.RLiveObjectService;

import org.slf4j.LoggerFactory;

import org.slf4j.ext.XLogger;

import net.jmp.demo.redis.api.Demo;

import net.jmp.demo.redis.config.Config;

import net.jmp.demo.redis.objects.Recording;

/*
 * The class that demonstrates live objects.
 */
public class LiveObjects extends Demo  {
    /** The logger. */
    private final XLogger logger = new XLogger(LoggerFactory.getLogger(this.getClass().getName()));

    /** The date formatter */
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

    /**
     * The constructor that takes
     * the application configuration.
     *
     * @param   config  net.jmp.demo.redis.Config
     * @param   client  org.redisson.api.RedissonClient
     */
    public LiveObjects(final Config config, final RedissonClient client) {
        super(config, client);
    }

    /**
     * The go method.
     */
    @Override
    public void go() {
        this.logger.entry();

        this.persist();

        final String id = this.attach();

        this.merge(id);

        this.logger.exit();
    }

    /**
     * Persist a live object. Persist returns proxied attached object
     * for the detached object. Transfers all the non-null field values
     * to the redis server. Only when the object does not already exist.
     */
    private void persist() {
        this.logger.entry();

        final RLiveObjectService service = this.client.getLiveObjectService();
        final String id = UUID.randomUUID().toString();

        final Recording sourceRecording = new Recording();  // This is a detached object

        service.registerClass(Recording.class);

        sourceRecording.setId(id);
        sourceRecording.setLabel("Deutsche Grammophon");
        sourceRecording.setTitle("Rachmaninoff: The Piano Concertos and Paganini Rhapsody");
        sourceRecording.setArtists(
                List.of(
                        "Yuja Wang",
                        "Gustavo Dudamel",
                        "Los Angeles Philharmonic Orchestra"
                )
        );
        sourceRecording.setTimeInMinutes(148);

        Date whenRecorded = null;

        try {
            whenRecorded = this.dateFormatter.parse("1-Jan-2023");
        } catch (final ParseException pe) {
            this.logger.catching(pe);
        }

        if (whenRecorded != null) {
            sourceRecording.setWhenRecorded(whenRecorded);

            this.logger.info("Source recording: {}", sourceRecording);

            // Logging the to-string methods of the proxied objects always show all fields as null

            final Recording attachedProxyRecording = service.persist(sourceRecording);

            if (this.logger.isInfoEnabled()) {
                this.logger.info("Attached proxy title: {}", attachedProxyRecording.getTitle());
                this.logger.info("Attached proxy label: {}", attachedProxyRecording.getLabel());
                this.logger.info("Attached proxy time : {}", attachedProxyRecording.getTimeInMinutes());
            }

            final Recording proxiedRecording = service.get(Recording.class, id);

            if (this.logger.isInfoEnabled()) {
                this.logger.info("Proxied title: {}", proxiedRecording.getTitle());
                this.logger.info("Proxied label: {}", attachedProxyRecording.getLabel());
                this.logger.info("Proxied time : {}", attachedProxyRecording.getTimeInMinutes());
            }
        }

        service.unregisterClass(Recording.class);

        this.logger.exit();
    }

    /**
     * Attach a live object.
     * Return its identifier.
     *
     * @return  java.lang.String
     */
    private String attach() {
        this.logger.entry();

        final RLiveObjectService service = this.client.getLiveObjectService();
        final String id = UUID.randomUUID().toString();

        final Recording sourceRecording = new Recording();  // This is a detached object

        service.registerClass(Recording.class);

        sourceRecording.setId(id);
        sourceRecording.setLabel("Deutsche Grammophon");
        sourceRecording.setTitle("Beethoven and Beyond");
        sourceRecording.setArtists(
                List.of(
                        "Maria Duenas",
                        "Manfred Hancock",
                        "Vienna Symphony Orchestra"
                )
        );
        //sourceRecording.setTimeInMinutes(100);

        Date whenRecorded = null;

        try {
            whenRecorded = this.dateFormatter.parse("1-Jan-2023");
        } catch (final ParseException pe) {
            this.logger.catching(pe);
        }

        if (whenRecorded != null) {

        }

        service.unregisterClass(Recording.class);

        this.logger.exit(id);

        return id;
    }

    /**
     * Merge a live object.
     *
     * @param   id  java.lang.String
     */
    private void merge(final String id) {
        this.logger.entry(id);
        
        this.logger.exit();
    }

    /**
     * Search for live objects.
     */
    private void search() {
        this.logger.entry();

        this.logger.exit();
    }
}
