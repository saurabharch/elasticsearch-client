package org.elasticsearch.threadpool;

import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilderString;

public interface ThreadPoolStatsElement extends Streamable, ToXContent {

    String name();

    String getName();

    int threads();

    int getThreads();

    int queue();

    int getQueue();

    int active();

    int getActive();

    long rejected();

    long getRejected();

    static final class Fields {

        public static final XContentBuilderString THREAD_POOL = new XContentBuilderString("thread_pool");
        public static final XContentBuilderString THREADS = new XContentBuilderString("threads");
        public static final XContentBuilderString QUEUE = new XContentBuilderString("queue");
        public static final XContentBuilderString ACTIVE = new XContentBuilderString("active");
        public static final XContentBuilderString REJECTED = new XContentBuilderString("rejected");
    }
}
