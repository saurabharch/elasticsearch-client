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

package org.elasticsearch.common.bytes;

import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.Bytes;
import org.elasticsearch.common.io.stream.BytesStreamInput;
import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class BytesArray implements BytesReference {

    public static final BytesArray EMPTY = new BytesArray(Bytes.EMPTY_ARRAY, 0, 0);

    protected byte[] bytes;
    protected int offset;
    protected int length;

    public BytesArray(String bytes) {
        this(toBytes(bytes));
    }
    
    private static byte[] toBytes(String bytes) {
        try {
        return bytes.getBytes("UTF-8");
        } catch(UnsupportedEncodingException e) {
            return null;
        }
    }

    public BytesArray(byte[] bytes) {
        this.bytes = bytes;
        this.offset = 0;
        this.length = bytes.length;
    }

    public BytesArray(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    
    public byte get(int index) {
        return bytes[offset + index];
    }

    
    public int length() {
        return length;
    }

    
    public BytesReference slice(int from, int length) {
        if (from < 0 || (from + length) > this.length) {
            throw new ElasticSearchIllegalArgumentException("can't slice a buffer with length [" + this.length + "], with slice parameters from [" + from + "], length [" + length + "]");
        }
        return new BytesArray(bytes, offset + from, length);
    }

    
    public StreamInput streamInput() {
        return new BytesStreamInput(bytes, offset, length, false);
    }

    
    public void writeTo(OutputStream os) throws IOException {
        os.write(bytes, offset, length);
    }

    
    public byte[] toBytes() {
        if (offset == 0 && bytes.length == length) {
            return bytes;
        }
        return Arrays.copyOfRange(bytes, offset, offset + length);
    }

    
    public BytesArray toBytesArray() {
        return this;
    }

    
    public BytesArray copyBytesArray() {
        return new BytesArray(Arrays.copyOfRange(bytes, offset, offset + length));
    }

    
    public boolean hasArray() {
        return true;
    }

    
    public byte[] array() {
        return bytes;
    }

    
    public int arrayOffset() {
        return offset;
    }

    
    public String toUtf8() {
        if (length == 0) {
            return "";
        }
        try {
            return new String(bytes, offset, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    
    public boolean equals(Object obj) {
        return bytesEquals((BytesArray) obj);
    }

    public boolean bytesEquals(BytesArray other) {
        if (length == other.length) {
            int otherUpto = other.offset;
            final byte[] otherBytes = other.bytes;
            final int end = offset + length;
            for (int upto = offset; upto < end; upto++, otherUpto++) {
                if (bytes[upto] != otherBytes[otherUpto]) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    
    public int hashCode() {
        int result = 0;
        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            result = 31 * result + bytes[i];
        }
        return result;
    }
}