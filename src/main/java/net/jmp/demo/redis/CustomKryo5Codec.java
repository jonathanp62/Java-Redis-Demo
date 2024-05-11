package net.jmp.demo.redis;

/*
 * (#)CustomKryo5Codec.java 0.3.0   05/11/2024
 *
 * @author   Jonathan Parker
 * @version  0.3.0
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

import com.esotericsoftware.kryo.Kryo;

import org.redisson.codec.Kryo5Codec;

/**
 * A class that extends the Kryo5 codec in
 * order to set the references to true.
 */
final class CustomKryo5Codec extends Kryo5Codec {
    /**
     * Create and return an instance of Kryo.
     *
     * @param   classLoader java.lang.ClassLoader
     * @return              com.esotericsoftware.kryo.Kryo
     */
    @Override
    protected Kryo createKryo(final ClassLoader classLoader) {
        final Kryo kryo = super.createKryo(classLoader);

        kryo.setReferences(true);

        return kryo;
    }
}
