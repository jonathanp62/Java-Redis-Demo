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

    /** The date formatter. */
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

    /** The Redisson live object service. */
    private final RLiveObjectService service;

    /**
     * The constructor that takes
     * the application configuration.
     *
     * @param   config  net.jmp.demo.redis.Config
     * @param   client  org.redisson.api.RedissonClient
     */
    public LiveObjects(final Config config, final RedissonClient client) {
        super(config, client);

        this.service = client.getLiveObjectService();
    }

    /**
     * The go method.
     */
    @Override
    public void go() {
        this.logger.entry();

        this.service.registerClass(Recording.class);

        final Recording recording1 = this.persist();
        final Recording recording2 = this.attach();

        this.merge(recording2);

        this.delete(recording1, recording2);

        this.service.unregisterClass(Recording.class);

        this.logger.exit();
    }

    /**
     * Persist a live object. Persist returns proxied attached object
     * for the detached object. Transfers all the non-null field values
     * to the redis server. Only when the object does not already exist.
     * Return the attached proxy object for later cleanup.
     *
     * @return  net.jmp.demo.redis.objects.Recording
     */
    private Recording persist() {
        this.logger.entry();

        final String id = UUID.randomUUID().toString();
        final Recording detachedRecording = new Recording();  // This is a detached object

        detachedRecording.setId(id);
        detachedRecording.setLabel("Deutsche Grammophon");
        detachedRecording.setTitle("Rachmaninoff: The Piano Concertos and Paganini Rhapsody");
        detachedRecording.setArtists(
                List.of(
                        "Yuja Wang",
                        "Gustavo Dudamel",
                        "Los Angeles Philharmonic Orchestra"
                )
        );
        detachedRecording.setTimeInMinutes(148);

        Date whenRecorded = null;

        try {
            whenRecorded = this.dateFormatter.parse("1-Jan-2023");
        } catch (final ParseException pe) {
            this.logger.catching(pe);
        }

        Recording attachedProxyRecording = null;

        if (whenRecorded != null) {
            detachedRecording.setWhenRecorded(whenRecorded);

            this.logger.info("Detached recording: {}", detachedRecording);

            // Logging the to-string methods of the proxied objects always show all fields as null

            attachedProxyRecording = this.service.persist(detachedRecording);

            if (this.logger.isInfoEnabled()) {
                this.logger.info("Attached proxy title: {}", attachedProxyRecording.getTitle());
                this.logger.info("Attached proxy label: {}", attachedProxyRecording.getLabel());
                this.logger.info("Attached proxy time : {}", attachedProxyRecording.getTimeInMinutes());
            }

            final Recording proxiedRecording = this.service.get(Recording.class, id);

            if (this.logger.isInfoEnabled()) {
                this.logger.info("Proxied title: {}", proxiedRecording.getTitle());
                this.logger.info("Proxied label: {}", attachedProxyRecording.getLabel());
                this.logger.info("Proxied time : {}", attachedProxyRecording.getTimeInMinutes());
            }
        }

        this.logger.exit(attachedProxyRecording);

        return attachedProxyRecording;
    }

    /**
     * Attach a live object.
     * Return the detached object.
     *
     * @return  net.jmp.demo.redis.objects.Recording
     */
    private Recording attach() {
        this.logger.entry();

        final String id = UUID.randomUUID().toString();
        final Recording detachedRecording = new Recording();  // This is a detached object

        detachedRecording.setId(id);
        detachedRecording.setLabel("Deutsche Grammophon");
        detachedRecording.setTitle("Beethoven and Beyond");
        detachedRecording.setArtists(
                List.of(
                        "Maria Duenas",
                        "Manfred Hancock",
                        "Vienna Symphony Orchestra"
                )
        );

        final Recording attachedProxyRecording = this.service.attach(detachedRecording);

        // The attachedProxyRecording instance has a length of zero bytes
        // Each field will be null

        if (this.logger.isInfoEnabled()) {
            this.logger.info("Attached proxy title: {}", attachedProxyRecording.getTitle());
            this.logger.info("Attached proxy label: {}", attachedProxyRecording.getLabel());
            this.logger.info("Attached proxy time : {}", attachedProxyRecording.getTimeInMinutes());
        }

        this.logger.exit(detachedRecording);

        return detachedRecording;
    }

    /**
     * Merge a live object. The input is the detached
     * object from the previous attach operation.
     * Return the attached proxy object for later cleanup.
     *
     * @param   detachedRecording   net.jmp.demo.redis.objects.Recording
     * @return                      net.jmp.demo.redis.objects.Recording
     */
    private Recording merge(final Recording detachedRecording) {
        this.logger.entry(detachedRecording);

        detachedRecording.setTimeInMinutes(100);

        Date whenRecorded = null;

        try {
            whenRecorded = this.dateFormatter.parse("1-Jan-2023");
        } catch (final ParseException pe) {
            this.logger.catching(pe);
        }

        if (whenRecorded != null) {
            detachedRecording.setWhenRecorded(whenRecorded);
        }

        final Recording attachedProxyRecording = this.service.merge(detachedRecording);

        if (this.logger.isInfoEnabled()) {
            this.logger.info("Attached proxy title: {}", attachedProxyRecording.getTitle());
            this.logger.info("Attached proxy label: {}", attachedProxyRecording.getLabel());
            this.logger.info("Attached proxy time : {}", attachedProxyRecording.getTimeInMinutes());
            this.logger.info("Attached proxy date : {}", attachedProxyRecording.getWhenRecorded());
        }

        this.logger.exit(attachedProxyRecording);

        return attachedProxyRecording;
    }

    /**
     * Search for live objects.
     */
    private void search() {
        this.logger.entry();

        this.logger.exit();
    }

    /**
     * Delete the specified live objects.
     *
     * @param   recordings  net.jmp.demo.redis.objects.Recording[]
     */
    private void delete(final Recording... recordings) {
        this.logger.entry((Object) recordings);

        for (final var recording : recordings) {
            final long deletes = service.delete(Recording.class, recording.getId());

            this.logger.info("{} live object(s) were deleted", deletes);
        }
        this.logger.exit();
    }
}
