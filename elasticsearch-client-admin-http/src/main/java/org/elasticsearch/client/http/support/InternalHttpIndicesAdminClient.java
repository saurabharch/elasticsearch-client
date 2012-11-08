package org.elasticsearch.client.http.support;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.HttpIndicesAdminActionModule;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.client.GenericClient;
import org.elasticsearch.common.settings.Settings;

public class InternalHttpIndicesAdminClient extends HttpClient {

    private final HttpIndicesAdminActionModule actions;
    
    public InternalHttpIndicesAdminClient(Settings settings, HttpIndicesAdminActionModule actions) {
        super(settings);
        this.actions = actions;
    }
    
    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, Client extends GenericClient> 
            ActionFuture<Response> execute(Action<Request, Response, RequestBuilder, Client> action, Request request) {
        HttpAction<Request,Response> httpAction = actions.getAction(action.name());
        return httpAction.execute(this, request);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, Client extends GenericClient> 
            void execute(Action<Request, Response, RequestBuilder, Client> action, Request request, ActionListener<Response> listener) {
        HttpAction<Request, Response> httpAction = actions.getAction(action.name());
        httpAction.execute(this, request, listener);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, Client extends GenericClient> 
            RequestBuilder prepareExecute(Action<Request, Response, RequestBuilder, Client> action) {
        return action.newRequestBuilder((Client)this);
    }
    
}
