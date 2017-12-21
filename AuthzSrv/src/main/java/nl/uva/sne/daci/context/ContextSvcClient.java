package nl.uva.sne.daci.context;


import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.uva.sne.daci.context.ContextBaseResponse;



public class ContextSvcClient {

	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContextSvcClient.class);
	
	private String serviceBaseURL;
	
	public ContextSvcClient(String address){
		this.serviceBaseURL = address;
		
	}
	
	 
	 /** This is teh alternative implementation with httpclient....*/
	ContextBaseResponse validate(String tenantId, ContextRequestImpl ctxRequest) throws Exception {
	
		HttpClient client = HttpClientBuilder.create().build();
	    ObjectMapper mapper = new ObjectMapper();
	    HttpPost mPost = null;
	    try{
	        mPost = new HttpPost(serviceBaseURL);

	        mPost.setHeader("Content-Type", "application/json");
	        mPost.setHeader("accept", "application/json");
	        
	        ContextRequestWrapper crw = new ContextRequestWrapper();
	        crw.setRequest(ctxRequest);
	        crw.setTenantId(tenantId);
	        mPost.setEntity(new StringEntity(mapper.writeValueAsString(crw)));
	        
	        HttpResponse response = client.execute(mPost); 
	            
	        return mapper.readValue(response.getEntity().getContent(),ContextBaseResponse.class);
	    }catch(Exception e){
	       	throw new Exception("Exception in adding bucket : " + e.getMessage());     	
	    }finally{
	    	 mPost.releaseConnection();
	    }
	 }
	
}
