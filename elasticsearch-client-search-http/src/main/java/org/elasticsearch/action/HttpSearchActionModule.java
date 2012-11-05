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

package org.elasticsearch.action;

import com.google.common.collect.Maps;
import java.util.Map;
import org.elasticsearch.action.count.CountAction;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.http.action.count.HttpCountAction;

public class HttpSearchActionModule {

    private final static Map<String, ActionEntry> actions = Maps.newHashMap();

    /**
     * Register our actions.
     */
    static {
        registerAction(CountAction.INSTANCE, new HttpCountAction());
    }
        
    /**
     * Registers an action.
     *
     * @param action                  The action type.
     * @param htppAction         The HTTP action implementing the actual action.
     * @param supportHttpActions Any support actions that are needed by the HTTP action.
     * @param <Request>               The request type.
     * @param <Response>              The response type.
     */
    private static <Request extends ActionRequest, Response extends ActionResponse> 
            void registerAction(GenericAction<Request, Response> action, HttpAction<Request, Response> httpAction, Class... supportHttpActions) {
        actions.put(action.name(), new ActionEntry(action, httpAction, supportHttpActions));
    }
        
    public <Request extends ActionRequest, Response extends ActionResponse> HttpAction<Request, Response> getAction(String action) {
        return actions.get(action).httpAction;
    }

    static class ActionEntry<Request extends ActionRequest, Response extends ActionResponse> {
        public final GenericAction<Request, Response> action;
        public final HttpAction<Request, Response> httpAction;
        public final Class[] supportHttpActions;

        ActionEntry(GenericAction<Request, Response> action, HttpAction<Request, Response> httpAction, Class... supportHttpActions) {
            this.action = action;
            this.httpAction = httpAction;
            this.supportHttpActions = supportHttpActions;
        }
    }
    
}
