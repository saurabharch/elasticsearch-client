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

package org.elasticsearch.common.xcontent.json;

import com.fasterxml.jackson.core.JsonGenerator;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.xcontent.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class JsonXContentGenerator implements XContentGenerator {

    protected final JsonGenerator generator;

    public JsonXContentGenerator(JsonGenerator generator) {
        this.generator = generator;
    }

    
    public XContentType contentType() {
        return XContentType.JSON;
    }

    
    public void usePrettyPrint() {
        generator.useDefaultPrettyPrinter();
    }

    
    public void writeStartArray() throws IOException {
        generator.writeStartArray();
    }

    
    public void writeEndArray() throws IOException {
        generator.writeEndArray();
    }

    
    public void writeStartObject() throws IOException {
        generator.writeStartObject();
    }

    
    public void writeEndObject() throws IOException {
        generator.writeEndObject();
    }

    
    public void writeFieldName(String name) throws IOException {
        generator.writeFieldName(name);
    }

    
    public void writeFieldName(XContentString name) throws IOException {
        generator.writeFieldName(name);
    }

    
    public void writeString(String text) throws IOException {
        generator.writeString(text);
    }

    
    public void writeString(char[] text, int offset, int len) throws IOException {
        generator.writeString(text, offset, len);
    }

    
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        generator.writeUTF8String(text, offset, length);
    }

    
    public void writeBinary(byte[] data, int offset, int len) throws IOException {
        generator.writeBinary(data, offset, len);
    }

    
    public void writeBinary(byte[] data) throws IOException {
        generator.writeBinary(data);
    }

    
    public void writeNumber(int v) throws IOException {
        generator.writeNumber(v);
    }

    
    public void writeNumber(long v) throws IOException {
        generator.writeNumber(v);
    }

    
    public void writeNumber(double d) throws IOException {
        generator.writeNumber(d);
    }

    
    public void writeNumber(float f) throws IOException {
        generator.writeNumber(f);
    }

    
    public void writeBoolean(boolean state) throws IOException {
        generator.writeBoolean(state);
    }

    
    public void writeNull() throws IOException {
        generator.writeNull();
    }

    
    public void writeStringField(String fieldName, String value) throws IOException {
        generator.writeStringField(fieldName, value);
    }

    
    public void writeStringField(XContentString fieldName, String value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeString(value);
    }

    
    public void writeBooleanField(String fieldName, boolean value) throws IOException {
        generator.writeBooleanField(fieldName, value);
    }

    
    public void writeBooleanField(XContentString fieldName, boolean value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeBoolean(value);
    }

    
    public void writeNullField(String fieldName) throws IOException {
        generator.writeNullField(fieldName);
    }

    
    public void writeNullField(XContentString fieldName) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNull();
    }

    
    public void writeNumberField(String fieldName, int value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    
    public void writeNumberField(XContentString fieldName, int value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    
    public void writeNumberField(String fieldName, long value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    
    public void writeNumberField(XContentString fieldName, long value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    
    public void writeNumberField(String fieldName, double value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    
    public void writeNumberField(XContentString fieldName, double value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    
    public void writeNumberField(String fieldName, float value) throws IOException {
        generator.writeNumberField(fieldName, value);
    }

    
    public void writeNumberField(XContentString fieldName, float value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeNumber(value);
    }

    
    public void writeBinaryField(String fieldName, byte[] data) throws IOException {
        generator.writeBinaryField(fieldName, data);
    }

    
    public void writeBinaryField(XContentString fieldName, byte[] value) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeBinary(value);
    }

    
    public void writeArrayFieldStart(String fieldName) throws IOException {
        generator.writeArrayFieldStart(fieldName);
    }

    
    public void writeArrayFieldStart(XContentString fieldName) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeStartArray();
    }

    
    public void writeObjectFieldStart(String fieldName) throws IOException {
        generator.writeObjectFieldStart(fieldName);
    }

    
    public void writeObjectFieldStart(XContentString fieldName) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeStartObject();
    }

    
    public void writeRawField(String fieldName, byte[] content, OutputStream bos) throws IOException {
        generator.writeRaw(", \"");
        generator.writeRaw(fieldName);
        generator.writeRaw("\" : ");
        flush();
        bos.write(content);
    }

    
    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException {
        generator.writeRaw(", \"");
        generator.writeRaw(fieldName);
        generator.writeRaw("\" : ");
        flush();
        bos.write(content, offset, length);
    }

    
    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        generator.writeRaw(", \"");
        generator.writeRaw(fieldName);
        generator.writeRaw("\" : ");
        flush();
        Streams.copy(content, bos);
    }

    
    public void writeRawField(String fieldName, BytesReference content, OutputStream bos) throws IOException {
        generator.writeRaw(", \"");
        generator.writeRaw(fieldName);
        generator.writeRaw("\" : ");
        flush();
        content.writeTo(bos);
    }

    
    public void copyCurrentStructure(XContentParser parser) throws IOException {
        // the start of the parser
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser instanceof JsonXContentParser) {
            generator.copyCurrentStructure(((JsonXContentParser) parser).parser);
        } else {
            XContentHelper.copyCurrentStructure(this, parser);
        }
    }

    
    public void flush() throws IOException {
        generator.flush();
    }

    
    public void close() throws IOException {
        generator.close();
    }
}
