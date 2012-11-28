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

package org.elasticsearch.action.support;

import com.ning.http.client.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.elasticsearch.action.ActionListener;

public class HttpActionFuture<T> extends AdapterActionFuture<T, T> {
    
    private final ListenableFuture<T> future;
    private ActionListener<T> listener;
    
    public static <T> HttpActionFuture<T> newFuture(ListenableFuture<T> listener) {
        return new HttpActionFuture<T>(listener);
    }
    
    public HttpActionFuture(ListenableFuture<T> future) {
        this.future = future;
    }
    
    public HttpActionFuture<T> listener(ActionListener<T> listener) {
        this.listener = listener;
        return this;
    }
    
    protected T convert(T listenerResponse) {
        return listenerResponse;
    }
    
    public T get(long timeout, TimeUnit unit) throws InterruptedException,
            TimeoutException, ExecutionException {
        return future.get(timeout, unit);
    }
    
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }
    
    public void onResponse(T result) {
        super.onResponse(result);
        if (listener != null) {
            listener.onResponse(result);
        }
    }
    
    public void onFailure(Throwable e) {
        super.onFailure(e);
        if (listener != null) {
            listener.onFailure(e);
        }
    }
}
