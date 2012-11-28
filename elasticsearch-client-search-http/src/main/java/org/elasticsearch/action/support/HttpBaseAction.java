package org.elasticsearch.action.support;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.common.xcontent.XContentParser;

public abstract class HttpBaseAction <Request extends ActionRequest, Response extends ActionResponse> 
    extends HttpAction<Request,Response> {
    
    
    
    protected List<ShardOperationFailedException> parseShardFailures(XContentParser parser) throws IOException {
        List<ShardOperationFailedException> list = Lists.newArrayList();
        XContentParser.Token token;
        String currentFieldName = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
            String index = null;
            int shardId = 0;
            String reason = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else if (token.isValue()) {
                    if ("index".equals(currentFieldName)) {
                        index = parser.text();
                    } else if ("shardId".equals(currentFieldName)) {
                        shardId = parser.intValue();
                    } else if ("reason".equals(currentFieldName)) {
                        reason = parser.text();
                    }
                }
            }
            list.add(new DefaultShardOperationFailedException(index, shardId, reason));
        }
        return list;
    }

}
