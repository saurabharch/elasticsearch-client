package org.elasticsearch.cluster.routing.allocation.command;

import java.io.IOException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;

public interface AllocationCommandFactory<T extends AllocationCommand> {

    T readFrom(StreamInput in) throws IOException;

    void writeTo(T command, StreamOutput out) throws IOException;

    T fromXContent(XContentParser parser) throws IOException;

    void toXContent(T command, XContentBuilder builder, ToXContent.Params params) throws IOException;
}
