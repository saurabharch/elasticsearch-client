package org.elasticsearch.threadpool;

import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.xcontent.ToXContent;

public interface ThreadPoolStats extends Streamable, ToXContent, Iterable<ThreadPoolStatsElement> {
    
}
