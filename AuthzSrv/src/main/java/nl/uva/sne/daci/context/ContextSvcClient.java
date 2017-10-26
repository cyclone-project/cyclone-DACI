package nl.uva.sne.daci.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.uva.sne.daci.context.ContextRequestImpl;
import nl.uva.sne.daci.context.ContextBaseResponse;


/*
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.HttpClient;
*/

public class ContextSvcClient {

	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContextSvcClient.class);
	//private static final String RESOURCE_CONTEXT_DECISION = "http://localhost:8080/contexts";
	
	
	private String serviceBaseURL;
	//private HttpClient httpClient;
	
	public ContextSvcClient(String address){
		this.serviceBaseURL = address;
		
		//this.methodVerifyGrantTokenURL = this.serviceURL + METHOD_VERIFY_GRANT_TOKEN;
	
		//httpClient = HttpClients.createDefault();
	}
	
		
	
	/*This will make REST calls to ContextSrv for validation of the context...
	 * What you need to do:
	 * - create the relevant JSON objects for the response...
	 * - ??*/
	 
	/*ContextResponse validate(ContextRequest request){
		  
		 RestTemplate restTemplate = new RestTemplate();  
		 HttpEntity<String> tenantId = new HttpEntity<String>(tenantId); 
		 ContextResponse result = restTemplate.postForObject(serviceBaseURL, request, responseType)//getForObject(serviceBaseURL, ContextResponse.class);
		 System.out.println(result);
		 return result;
	}*/
	

	 
	 /* This is teh alternative implementation with httpclient....*/
	ContextBaseResponse validate(ContextRequest request, String tenantId) throws Exception {
	
		HttpClient client = HttpClientBuilder.create().build();
	    ObjectMapper mapper = new ObjectMapper();
	    try{
	        HttpPost mPost = new HttpPost(serviceBaseURL);

	        mPost.setHeader("Content-Type", "application/json");
	        mPost.setHeader("accept", "application/json");
	            
	        mPost.setEntity(new StringEntity(tenantId));
	        mPost.setEntity(new StringEntity(mapper.writeValueAsString(request)));         
	            
	        
	        HttpResponse response = client.execute(mPost); 
	            
	           
	        mPost.releaseConnection( );

	        return mapper.readValue(response.getEntity().getContent(),ContextBaseResponse.class);
	    }catch(Exception e){
	       	throw new Exception("Exception in adding bucket : " + e.getMessage());     	
	    }
	 }
	
}
