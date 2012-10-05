package org.elasticsearch.threadpool;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.unit.SizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilderString;

public interface ThreadPoolInfoElement extends Streamable, ToXContent {
    
    String name();

    String getName();

    String type();

    String getType();
    
    int min();

    int getMin();

    int max();
    
    int getMax();
    
    @Nullable
    TimeValue keepAlive();

    @Nullable
    TimeValue getKeepAlive();
    
    @Nullable
    SizeValue capacity();

    @Nullable
    SizeValue getCapacity();
    
    static final class Fields {

        public static final XContentBuilderString TYPE = new XContentBuilderString("type");
        public static final XContentBuilderString MIN = new XContentBuilderString("min");
        public static final XContentBuilderString MAX = new XContentBuilderString("max");
        public static final XContentBuilderString KEEP_ALIVE = new XContentBuilderString("keep_alive");
        public static final XContentBuilderString CAPACITY = new XContentBuilderString("capacity");
    }    
}
