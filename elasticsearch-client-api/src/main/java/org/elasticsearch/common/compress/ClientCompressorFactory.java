/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.common.compress;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.compress.lzf.ClientLZFCompressor;
import org.elasticsearch.common.compress.snappy.UnavailableSnappyCompressor;
import org.elasticsearch.common.compress.snappy.xerial.XerialSnappy;
import org.elasticsearch.common.compress.snappy.xerial.ClientXerialSnappyCompressor;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.io.stream.CachedStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 */
public class ClientCompressorFactory {

    private static final ClientLZFCompressor LZF = new ClientLZFCompressor();

    private static final ClientCompressor[] compressors;
    private static final ImmutableMap<String, ClientCompressor> compressorsByType;
    private static ClientCompressor defaultCompressor;

    static {
        List<ClientCompressor> compressorsX = Lists.newArrayList();
        compressorsX.add(LZF);
        boolean addedSnappy = false;
        if (XerialSnappy.available) {
            compressorsX.add(new ClientXerialSnappyCompressor());
            addedSnappy = true;
        } else {
            Loggers.getLogger(ClientCompressorFactory.class).debug("failed to load xerial snappy-java", XerialSnappy.failure);
        }
        if (!addedSnappy) {
            compressorsX.add(new UnavailableSnappyCompressor());
        }

        compressors = compressorsX.toArray(new ClientCompressor[compressorsX.size()]);

        MapBuilder<String, ClientCompressor> compressorsByTypeX = MapBuilder.newMapBuilder();
        for (ClientCompressor compressor : compressors) {
            compressorsByTypeX.put(compressor.type(), compressor);
        }
        compressorsByType = compressorsByTypeX.immutableMap();

        defaultCompressor = LZF;
    }

    public static synchronized void configure(Settings settings) {
        for (ClientCompressor compressor : compressors) {
            compressor.configure(settings);
        }
        String defaultType = settings.get("compress.default.type", "lzf").toLowerCase(Locale.ENGLISH);
        boolean found = false;
        for (ClientCompressor compressor : compressors) {
            if (defaultType.equalsIgnoreCase(compressor.type())) {
                defaultCompressor = compressor;
                found = true;
                break;
            }
        }
        if (!found) {
            Loggers.getLogger(ClientCompressorFactory.class).warn("failed to find default type [{}]", defaultType);
        }
    }

    public static synchronized void setDefaultCompressor(ClientCompressor defaultCompressor) {
        ClientCompressorFactory.defaultCompressor = defaultCompressor;
    }

    public static ClientCompressor defaultCompressor() {
        return defaultCompressor;
    }

    public static boolean isCompressed(BytesReference bytes) {
        return compressor(bytes) != null;
    }

    public static boolean isCompressed(byte[] data) {
        return compressor(data, 0, data.length) != null;
    }

    public static boolean isCompressed(byte[] data, int offset, int length) {
        return compressor(data, offset, length) != null;
    }

    @Nullable
    public static ClientCompressor compressor(BytesReference bytes) {
        for (ClientCompressor compressor : compressors) {
            if (compressor.isCompressed(bytes)) {
                return compressor;
            }
        }
        return null;
    }

    @Nullable
    public static ClientCompressor compressor(byte[] data) {
        return compressor(data, 0, data.length);
    }

    @Nullable
    public static ClientCompressor compressor(byte[] data, int offset, int length) {
        for (ClientCompressor compressor : compressors) {
            if (compressor.isCompressed(data, offset, length)) {
                return compressor;
            }
        }
        return null;
    }

    public static ClientCompressor compressor(String type) {
        return compressorsByType.get(type);
    }

    /**
     * Uncompress the provided data, data can be detected as compressed using {@link #isCompressed(byte[], int, int)}.
     */
    public static BytesReference uncompressIfNeeded(BytesReference bytes) throws IOException {
        ClientCompressor compressor = compressor(bytes);
        if (compressor != null) {
            if (bytes.hasArray()) {
                return new BytesArray(compressor.uncompress(bytes.array(), bytes.arrayOffset(), bytes.length()));
            }
            StreamInput compressed = compressor.streamInput(bytes.streamInput());
            CachedStreamOutput.Entry entry = CachedStreamOutput.popEntry();
            try {
                Streams.copy(compressed, entry.bytes());
                compressed.close();
                return new BytesArray(entry.bytes().bytes().toBytes());
            } finally {
                CachedStreamOutput.pushEntry(entry);
            }
        }
        return bytes;
    }
}
