/*
 * Copyright 2023 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eastrobot.arch.llms.model;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 表示与AI模型响应相关联的元数据的接口.
 * 该接口旨在提供有关AI模型生成响应的附加信息，包括处理细节和特定于模型的数据.
 * 它作为核心领域内的值对象，增强了对各种应用中AI模型响应的理解和管理。
 *
 * @author han.sun
 */
public interface ResponseMetadata {
    /**
     * Gets an entry from the context. Returns {@code null} when entry is not present.
     *
     * @param key key
     * @param <T> value type
     * @return entry or {@code null} if not present
     */
    <T> T get(String key);

    /**
     * Gets an entry from the context. Throws exception when entry is not present.
     *
     * @param key key
     * @param <T> value type
     * @return entry
     * @throws IllegalArgumentException if not present
     */
    <T> T getRequired(Object key);

    /**
     * Checks if context contains a key.
     *
     * @param key key
     * @return {@code true} when the context contains the entry with the given key
     */
    boolean containsKey(Object key);

    /**
     * Returns an element or default if not present.
     *
     * @param key           key
     * @param defaultObject default object to return
     * @param <T>           value type
     * @return object or default if not present
     */
    <T> T getOrDefault(Object key, T defaultObject);

    /**
     * Returns an element or default if not present.
     *
     * @param key                   key
     * @param defaultObjectSupplier supplier for default object to return
     * @param <T>                   value type
     * @return object or default if not present
     * @since 1.11.0
     */
    default <T> T getOrDefault(String key, Supplier<T> defaultObjectSupplier) {
        T value = get(key);
        return value != null ? value : defaultObjectSupplier.get();
    }

    Set<Map.Entry<String, Object>> entrySet();

    Set<String> keySet();

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map contains no key-value mappings
     */
    boolean isEmpty();
}
