package org.elasticsearch.common.compress;

import java.io.IOException;
import java.util.Arrays;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.Settings;

public class NullCompressor implements BasicCompressor {

    public static final String TYPE = "null";
    
    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void configure(Settings settings) {
    }

    @Override
    public boolean isCompressed(BytesReference bytes) {
        return false;
    }

    @Override
    public boolean isCompressed(byte[] data, int offset, int length) {
        return false;
    }

    @Override
    public byte[] uncompress(byte[] data, int offset, int length) throws IOException {
        return Arrays.copyOfRange(data, offset, length);
    }

    @Override
    public byte[] compress(byte[] data, int offset, int length) throws IOException {
        return Arrays.copyOfRange(data, offset, length);
    }

    @Override
    public CompressedStreamInput streamInput(StreamInput in) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CompressedStreamOutput streamOutput(StreamOutput out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
