package net.jmp.demo.redis.config;

/*
 * (#)Config.java   0.9.0   06/22/2024
 * (#)Config.java   0.5.0   05/18/2024
 * (#)Config.java   0.1.0   05/01/2024
 *
 * @author    Jonathan Parker
 * @version   0.9.0
 * @since     0.1.0
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

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * The configuration class.
 */
public final class Config {
    /** The Redis component. */
    @SerializedName("redis")
    private Redis redis;

    /** The process utility component. */
    @SerializedName("process-utility")
    private ProcessUtility processUtility;

    /**
     * Get the Redis component
     *
     * @return  net.jmp.demo.redis.Redis
     */
    public Redis getRedis() {
        return this.redis;
    }

    /**
     * Set the Redis component.
     *
     * @param   redis   net.jmp.demo.redis.Redis
     */
    public void setRedis(final Redis redis) {
        this.redis = redis;
    }

    /**
     * Set the process utility component.
     *
     * @param   processUtility  net.jmp.demo.redis.ProcessUtility
     */
    public void setProcessUtility(final ProcessUtility processUtility) {
        this.processUtility = processUtility;
    }

    /**
     * Get the process utility component
     *
     * @return  net.jmp.demo.redis.ProcessUtility
     */
    public ProcessUtility getProcessUtility() {
        return this.processUtility;
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

        final Config config = (Config) o;

        return Objects.equals(this.redis, config.redis) && Objects.equals(this.processUtility, config.processUtility);
    }

    /**
     * The hash-code method.
     *
     * @return  int
     */
    @Override
    public int hashCode() {
        int result = Objects.hashCode(this.redis);

        result = 31 * result + Objects.hashCode(this.processUtility);

        return result;
    }

    /**
     * The to-string method.
     *
     * @return  java.lang.String
     */
    @Override
    public String toString() {
        return "Config{" +
                "processUtility=" + this.processUtility +
                ", redis=" + this.redis +
                '}';
    }
}
