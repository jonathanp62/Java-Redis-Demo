package net.jmp.demo.redis;

/*
 * (#)Config.java   0.2.0   05/02/2024
 * (#)Config.java   0.1.0   05/01/2024
 *
 * @author    Jonathan Parker
 * @version   0.2.0
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

/**
 * The Redis configuration class.
 */
final class Redis {
    /** The host name. */
    @SerializedName("hostname")
    private String hostName;

    /** The port. */
    @SerializedName("port")
    private int port;

    /** The protocol. */
    @SerializedName("protocol")
    private String protocol;

    /**
     * Get the host name.
     *
     * @return  java.lang.String
     */
    String getHostName() {
        return this.hostName;
    }

    /**
     * Set the host name.
     *
     * @param   hostName    java.lang.String
     */
    void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    /**
     * Get the port.
     *
     * @return  int
     */
    int getPort() {
        return this.port;
    }

    /**
     * Set the port.
     *
     * @param   port    int
     */
    void setPort(final int port) {
        this.port = port;
    }

    /**
     * Get the protocol.
     *
     * @return  java.lang.String
     */
    String getProtocol() {
        return this.protocol;
    }

    /**
     * Set the protocol.
     *
     * @param   protocol    java.lang.String
     */
    void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    /**
     * The to-string method.
     *
     * @return  java.lang.String
     */
    @Override
    public String toString() {
        return "Redis{" +
                "hostName='" + hostName + '\'' +
                ", port=" + port +
                ", protocol=" + protocol +
                '}';
    }
}
