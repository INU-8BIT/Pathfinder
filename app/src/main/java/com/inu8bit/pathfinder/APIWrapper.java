package com.inu8bit.pathfinder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Wrapper Class for API Call via GET, POST and so on.
 */


public class APIWrapper {
    protected StringBuilder url;
    protected String method;
    protected Map<String, String> params = new HashMap<>();

    protected String send() throws InterruptedException, ExecutionException {
        url.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey());
            url.append("=");
            url.append(entry.getValue());
            url.append("&");
        }
        HTTPRequest httpRequest = new HTTPRequest();
        return httpRequest.execute(url.toString()).get();
    }
}
