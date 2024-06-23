package net.jmp.demo.redis.objects;

/*
 * (#)Recording.java    0.7.0   05/24/2024
 * (#)Recording.java    0.6.0   05/23/2024
 *
 * @author    Jonathan Parker
 * @version   0.7.0
 * @since     0.6.0
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

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.redisson.api.annotation.REntity;
import org.redisson.api.annotation.RId;
import org.redisson.api.annotation.RIndex;

/**
 * The recording class. Live objects
 * cannot be final nor can they
 * contain primitives.
 */
@REntity
public class Recording {
    /** The live object identifier. */
    @RId
    private String id;

    /** The title. */
    private String title;

    /** The label. */
    @RIndex
    private String label;

    /** The list of artists. */
    private List<String> artists;

    /** The time in minutes. */
    private Integer timeInMinutes;

    /** The date when recorded. */
    private Date whenRecorded;

    /**
     * The default constructor.
     */
    public Recording() {
        super();
    }

    /**
     * A constructor that takes an identifier.
     *
     * @param   id  java.lang.String
     */
    public Recording(final String id) {
        super();

        this.id = id;
    }

    /**
     * Get the live object identifier.
     *
     * @return  java.lang.String
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the live object identifier.
     *
     * @param   id  java.lang.String
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the title.
     *
     * @return  java.lang.String
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title.
     *
     * @param   title   java.lang.String
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Get the label.
     *
     * @return  java.lang.String
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Set the label.
     *
     * @param   label   java.lang.String
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * Get the list of artists.
     *
     * @return  java.util.List&lt;java.lang.String&gt;
     */
    public List<String> getArtists() {
        return this.artists;
    }

    /**
     * Set the list of artists.
     *
     * @param   artists java.util.List&lt;java.lang.String&gt;
     */
    public void setArtists(final List<String> artists) {
        this.artists = artists;
    }

    /**
     * Get the time in minutes.
     *
     * @return  java.lang.Integer
     */
    public Integer getTimeInMinutes() {
        return this.timeInMinutes;
    }

    /**
     * Set the time in minutes.
     *
     * @param   timeInMinutes   java.lang.Integer
     */
    public void setTimeInMinutes(final Integer timeInMinutes) {
        this.timeInMinutes = timeInMinutes;
    }

    /**
     * Get the date when recorded.
     *
     * @return  java.util.Date
     */
    public Date getWhenRecorded() {
        return this.whenRecorded;
    }

    /**
     * Set the date when recorded.
     *
     * @param   whenRecorded    java.util.Date
     */
    public void setWhenRecorded(final Date whenRecorded) {
        this.whenRecorded = whenRecorded;
    }

    /**
     * The equals method.
     *
     * @param   o   java.lang.Object
     * @return      boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Recording recording = (Recording) o;

        return Objects.equals(this.timeInMinutes, recording.timeInMinutes) && Objects.equals(this.id, recording.id) && Objects.equals(this.title, recording.title) && Objects.equals(this.label, recording.label) && Objects.equals(this.artists, recording.artists) && Objects.equals(this.whenRecorded, recording.whenRecorded);
    }

    /**
     * The hash-code method.
     *
     * @return  int
     */
    @Override
    public int hashCode() {
        int result = Objects.hashCode(this.id);

        result = 31 * result + Objects.hashCode(this.title);
        result = 31 * result + Objects.hashCode(this.label);
        result = 31 * result + Objects.hashCode(this.artists);
        result = 31 * result + Objects.hashCode(this.whenRecorded);
        result = 31 * result + Objects.hashCode(this.timeInMinutes);

        return result;
    }

    /**
     * The to-string method.
     *
     * @return  java.lang.String
     */
    @Override
    public String toString() {
        return "Recording{" +
                "id='" + this.id + '\'' +
                ", title='" + this.title + '\'' +
                ", label='" + this.label + '\'' +
                ", artists=" + this.artists +
                ", timeInMinutes=" + this.timeInMinutes +
                ", whenRecorded=" + this.whenRecorded +
                '}';
    }
}
