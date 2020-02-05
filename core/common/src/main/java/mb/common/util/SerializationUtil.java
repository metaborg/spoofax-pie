/*
 * Modifications copyright (C) 2019 Delft University of Technology
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mb.common.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Copied from https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/SerializationUtils.java
public class SerializationUtil {
    /**
     * <p>Serializes an {@code Object} to the specified stream.</p>
     *
     * <p>The stream will be closed once the object is written.
     * This avoids the need for a finally clause, and maybe also exception handling, in the application code.</p>
     *
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param obj          the object to serialize to bytes, may be null
     * @param outputStream the stream to write to, must not be null
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static void serialize(final Serializable obj, final OutputStream outputStream) {
        try(final ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
            out.writeObject(obj);
            out.flush();
        } catch(final IOException e) {
            throw new SerializationException(e);
        }
    }

    /**
     * Serializes an {@code Object} to a byte array for storage/serialization.
     *
     * @param obj the object to serialize to bytes
     * @return a byte[] with the converted Serializable
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static byte[] serialize(final Serializable obj) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(obj, baos);
        return baos.toByteArray();
    }

    /**
     * Serializes an {@code Object} to a string for storage/serialization.
     *
     * @param obj the object to serialize to bytes
     * @return a String with the converted Serializable
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static String serializeToString(final Serializable obj) {
        return new String(Base64.getEncoder().encode(serialize(obj)), StandardCharsets.ISO_8859_1);
    }


    /**
     * <p>
     * Deserializes an {@code Object} from the specified stream.
     * </p>
     *
     * <p>
     * The stream will be closed once the object is written. This avoids the need for a finally clause, and maybe also
     * exception handling, in the application code.
     * </p>
     *
     * <p>
     * The stream passed in is not buffered internally within this method. This is the responsibility of your
     * application if desired.
     * </p>
     *
     * <p>
     * If the call site incorrectly types the return value, a {@link ClassCastException} is thrown from the call site.
     * Without Generics in this declaration, the call site must type cast and can cause the same ClassCastException.
     * Note that in both cases, the ClassCastException is in the call site, not in this method.
     * </p>
     *
     * @param <T>         the object type to be deserialized
     * @param inputStream the serialized object input stream, must not be null
     * @return the deserialized object
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static <T> T deserialize(final InputStream inputStream, final ClassLoader classLoader) {
        try(final ObjectInputStream in = new ClassLoaderObjectInputStream(classLoader, inputStream)) {
            @SuppressWarnings("unchecked") final T obj = (T) in.readObject();
            return obj;
        } catch(final ClassCastException | ClassNotFoundException | IOException e) {
            throw new SerializationException(e);
        }
    }

    /**
     * <p>
     * Deserializes a single {@code Object} from an array of bytes.
     * </p>
     *
     * <p>
     * If the call site incorrectly types the return value, a {@link ClassCastException} is thrown from the call site.
     * Without Generics in this declaration, the call site must type cast and can cause the same ClassCastException.
     * Note that in both cases, the ClassCastException is in the call site, not in this method.
     * </p>
     *
     * @param <T> the object type to be deserialized
     * @return the deserialized object
     * @throws IllegalArgumentException if {@code objectData} is {@code null}
     * @throws SerializationException   (runtime) if the serialization fails
     */
    public static <T> T deserialize(final byte[] objectData, final ClassLoader classLoader) {
        return SerializationUtil.deserialize(new ByteArrayInputStream(objectData), classLoader);
    }

    /**
     * <p>
     * Deserializes a single {@code Object} from a String that was serialized with {@link
     * #serializeToString(Serializable)}.
     * </p>
     *
     * <p>
     * If the call site incorrectly types the return value, a {@link ClassCastException} is thrown from the call site.
     * Without Generics in this declaration, the call site must type cast and can cause the same ClassCastException.
     * Note that in both cases, the ClassCastException is in the call site, not in this method.
     * </p>
     *
     * @param <T> the object type to be deserialized
     * @return the deserialized object
     * @throws IllegalArgumentException if {@code objectData} is {@code null}
     * @throws SerializationException   (runtime) if the serialization fails
     */
    public static <T> T deserialize(final String objectData, final ClassLoader classLoader) {
        return SerializationUtil.deserialize(new ByteArrayInputStream(Base64.getDecoder().decode(objectData)), classLoader);
    }
}

