Public with sharing virtual class RallyBoundRestClient {

	private class RallyBoundRestClientException extends Exception {}
	
	private Map<String,String> headers;
	private String actionUrl;
	private String method;
	private String body;
	private Http http;
	private HttpRequest request;
	private Boolean useBasicAuth;
	private String accessTokenAuth;
	private Boolean doRefreshToken = false;
	Public HttpResponse response;
	Public String responseBody;
	
	private Static String ENDPOINT = 'https://rest.rallybound.com/sprocket/';
	private Static String BASIC_HEADER = 'Basic [client_id & client_secret base64]'; //See RFC 2617 https://www.ietf.org/rfc/rfc2617.txt
	private Static String REFRESH_TOKEN = null; // keep REFRESH_TOKEN null if Basic Auth is enough, ie open endpoints that do not require impersonating User or Admin
	private Static Integer TIMEOUT = 60000; // Default HTTP Timeout in milliseconds 
	
	
	Public RallyBoundRestClient(String actionUrl, String method, Map<String,String> headers, String body, Boolean useBasicAuth) {
		this.actionUrl = actionUrl;
		this.method = method;
		this.headers = headers;
		this.body = body;
		this.useBasicAuth = useBasicAuth;
		
		this.http = new Http();
		this.request = buildRequest();
		makeRequest();
		
		this.responseBody = handleResponse();
	}
	
	Public RallyBoundRestClient(String actionUrl, String method, String body, Boolean useBasicAuth) {
		this(actionUrl, method, null, body, useBasicAuth);
	}
	
	Public RallyBoundRestClient(String actionUrl, String method, Map<String,String> headers, String body) {
		this(actionUrl, method, headers, body, false);
	}
	
	Public RallyBoundRestClient(String actionUrl, String method, String body) {
		this(actionUrl, method, new Map<String,String>(), body);
	}
	
	Public RallyBoundRestClient(String actionUrl, String method, Map<String,String> headers) {
		this(actionUrl, method, headers, null);
	}
	
	Public RallyBoundRestClient(String actionUrl, String method) {
		this(actionUrl, method, new Map<String,String>(), null);
	}
	
	private Static String refreshAccessToken(){
		RallyBoundRestClient request = new RallyBoundRestClient(
			'auth/oauth2/refresh',
			'POST',
			'grant_type=refresh_token&refresh_token=' + REFRESH_TOKEN,
			true
		);
		Map<String,Object> responseInfo = (Map<String,Object>)JSON.deserializeUntyped(request.responseBody);
		return 'Bearer ' + (String)responseInfo.get('access_token');
	}

	private HttpRequest buildRequest(){
		HttpRequest request = new HttpRequest();
		request.setTimeout(TIMEOUT);
		String authToUse = BASIC_HEADER;
		if(!this.useBasicAuth && REFRESH_TOKEN != null) {
			if(this.accessTokenAuth == null){
				this.accessTokenAuth = refreshAccessToken();
				authToUse = this.accessTokenAuth;
			}	
		}
		request.setHeader('Authorization', authToUse); 
		if (this.headers != null) {
			for(String hkey : this.headers.keySet()){
				request.setHeader(hkey, this.headers.get(hkey)); 
			}
		}
		request.setEndpoint(ENDPOINT + this.actionUrl);
		request.setMethod(this.method);
		if (this.body != null && this.body.length() > 0) {
			request.setBody(this.body);
		}
		return request;
	}

	private void makeRequest(){
		HttpResponse response = this.http.send(this.request);
		if (response.getStatusCode() > 299) {
			String responseBody = response.getBody();
			if(!this.useBasicAuth && REFRESH_TOKEN != null && !this.doRefreshToken) { //doRefreshToken should be false -- we don't want to try refreshing more than once.
				try {
					Map<String,Object> responseInfo = (Map<String,Object>)JSON.deserializeUntyped(responseBody);
					if ((Integer)responseInfo.get('errorCode') == 10001) {
						this.doRefreshToken = true;
					}
				}
				catch (Exception e) { }
				if(this.doRefreshToken){
					this.http = new Http();
					this.request = buildRequest();
					this.accessTokenAuth = refreshAccessToken();
					this.request.setHeader('Authorization', this.accessTokenAuth); 
					makeRequest();
					return;
				}
			}
			throw new RallyBoundRestClientException('Failed to recieve a success code from remote. Code was: ' + response.getStatusCode() + '. Request was ' + this.request + '. Response Body is: ' + responseBody);
		}
		this.response = response;
	}

	private string handleResponse(){
		return this.response.getBody();
	}
	
}