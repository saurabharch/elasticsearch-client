package org.elasticsearch.action.support;

import com.ning.http.client.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.ElasticSearchInterruptedException;
import org.elasticsearch.ElasticSearchTimeoutException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.common.unit.TimeValue;

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
    /*
    public T actionGet() throws ElasticSearchException {
        try {
            return get();
        } catch (InterruptedException e) {
            throw new ElasticSearchInterruptedException(e.getMessage());
        } catch (ExecutionException e) {
            throw rethrowExecutionException(e);
        }
    }
    
    public T actionGet(String timeout) throws ElasticSearchException {
        return actionGet(TimeValue.parseTimeValue(timeout, null));
    }

    
    public T actionGet(long timeoutMillis) throws ElasticSearchException {
        return actionGet(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    
    public T actionGet(TimeValue timeout) throws ElasticSearchException {
        return actionGet(timeout.millis(), TimeUnit.MILLISECONDS);
    }

    
    public T actionGet(long timeout, TimeUnit unit) throws ElasticSearchException {
        try {
            return get(timeout, unit);
        } catch (TimeoutException e) {
            throw new ElasticSearchTimeoutException(e.getMessage());
        } catch (InterruptedException e) {
            throw new ElasticSearchInterruptedException(e.getMessage());
        } catch (ExecutionException e) {
            throw rethrowExecutionException(e);
        }
    }
    */
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
