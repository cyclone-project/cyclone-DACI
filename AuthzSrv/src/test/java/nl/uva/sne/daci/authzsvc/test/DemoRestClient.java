package nl.uva.sne.daci.appsec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.uva.sne.daci.appsec.AuthzRequest;
import nl.uva.sne.daci.appsec.AuthzResponse;
import nl.uva.sne.daci.appsec.AuthzSvc;

import org.apache.http.entity.mime.MultipartEntityBuilder;


public class DemoRestClient {
	

	static String providerPolicy = "policies/EnergyCyclone.EUC_ProviderPolicySet.xml";
	static String intertenantPolicy = "policies/EnergyCyclone.EUC_inter-tenant-policies.xml";
	static String intratenantPolicy = "policies/EnergyCyclone.API_Resources_Tenant.xml";
	
	
	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
        	restClient.createTenant("Energy_Tenant1", "localhost", "demo-uva");
        	restClient.setPolicy("Energy_Tenant1", providerPolicy, "providerPolicy", "localhost", "demo-uva");
        	restClient.setPolicy("Energy_Tenant1", intertenantPolicy, "intertenantPolicy", "localhost", "demo-uva");
        	restClient.setPolicy("Energy_Tenant1", intratenantPolicy, "tenantUserPolicy", "localhost", "demo-uva");
        	
        	AuthzRequest ar = createRequest("a", "listPowerPlants", "execute");	
        	if (restClient.readPrivateData_Integrated(ar, "Energy_Tenant1")) 
        		System.out.println("SUCCESS!");
    		else System.out.println("NOT AUTHORIZED!!!");
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }
	}

	
	
	/*This will be direct check, call the service...*/
	public boolean readPrivateData_Integrated(AuthzRequest ar, String tenantId) throws Exception{
			
			
			AuthzSvc.DecisionType res = authorize(ar, tenantId).getDecision();
			if (res.equals(AuthzSvc.DecisionType.PERMIT))
				return readPrivateData();
			else return false;
	}
	
	
	
	private boolean readPrivateData(){
		try (InputStream in = Files.newInputStream(Paths.get("sensitiveFile.txt"));
			    BufferedReader reader =
			      new BufferedReader(new InputStreamReader(in))) {
			    String line = null;
			    while ((line = reader.readLine()) != null) {
			        System.out.println(line);
			    }
			    return true;
			} catch (IOException x) {
			    System.err.println(x);
			}
		
		return false;
	}
	
	
	private static final String SUBJECT_ID = //"urn:oasis:names:tc:xacml:1.0:subject:subject-id";
			"urn:oasis:names:tc:xacml:1.0:subject:subject-role";
	private static final String RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	private static final String ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	/*Create request/s*/	
	public static AuthzRequest createRequest(String subjectRole, String resourceId, String actionId) {
		AuthzRequest request = new AuthzRequest();
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(SUBJECT_ID, subjectRole);
		attrs.put(RESOURCE_ID, resourceId);
		attrs.put(ACTION_ID, actionId);
		request.setAttributes(attrs);
		return request;
	}
	
	

	private void  setPolicy(String tenantId, String policyFile, String endPoint, String redisAddress, String domain) throws Exception {
		
		String output = null;
        String url = "http://localhost:8092/" + endPoint;
        HttpClient client = HttpClientBuilder.create().build();
        //ObjectMapper mapper = new ObjectMapper();
        try{
            HttpPost mPost = new HttpPost(url);
  
            //mPost.setHeader("Content-Type", "application/xml");
            //mPost.setHeader("accept", "application/xml");
            
            mPost.setEntity(new StringEntity(tenantId));
            mPost.setEntity(new StringEntity(redisAddress));
            mPost.setEntity(new StringEntity(domain));
            
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            File f = new File(policyFile);
            builder.addBinaryBody(
            	    "policy",
            	    new FileInputStream(f),
            	    ContentType.APPLICATION_OCTET_STREAM,
            	    f.getName()
            	);

            HttpEntity multipart = builder.build();
            mPost.setEntity(multipart);
            	
            //FileEntity entity = new FileEntity(new File(policyFile));
            //entity.setContentType(ContentType.APPLICATION_XML.getMimeType());
            //mPost.setEntity(entity);      

            HttpResponse response = client.execute(mPost); 
            
            output = response.toString();
            mPost.releaseConnection( );
            System.out.println("Response : " + output);
            
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	
	}




	private void  createTenant(String tenantId, String redisAddress, String domain) throws Exception {
		
		String output = null;
        String url = "http://localhost:8092/tenants";
        HttpClient client = HttpClientBuilder.create().build();
        //ObjectMapper mapper = new ObjectMapper();
       
        try{
        	HttpPost mPost = new HttpPost(url);
        	//mPost.setHeader("Content-Type", "application/json");
            //mPut.setHeader("accept", "application/json");
            
        	mPost.setEntity(new StringEntity(redisAddress));
        	mPost.setEntity(new StringEntity(domain));
        	mPost.setEntity(new StringEntity(tenantId));
            //FileEntity entity = new FileEntity(new File(policyFile));
            //entity.setContentType(ContentType.APPLICATION_XML.getMimeType());
            //mPost.setEntity(entity);      

            HttpResponse response = client.execute(mPost); 
            output = response.toString();
            mPost.releaseConnection( );
            System.out.println("Response... : " + output);
            
        	/*HttpPut mPut = new HttpPut(url);
            mPut.setHeader("Content-Type", "application/json");
            mPut.setEntity(new StringEntity("localhost"));
            mPut.setEntity(new StringEntity("demo-uva"));
            mPut.setEntity(new StringEntity(tenantId));
            HttpResponse response = client.execute(mPut); 
            output = response.toString();
            mPut.releaseConnection( );
            System.out.println("Response... : " + output);*/
            
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	     	
	}
	
	/*TODO : Add removeTenant call as well*/

	 
	
	
	public static AuthzResponse authorize(AuthzRequest req, String tenantId) throws Exception {
		
		String output = null;
        String url = "http://localhost:8089/pdps/" + tenantId+"/decision";
        HttpClient client = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        try{
            HttpPost mPost = new HttpPost(url);
            
            mPost.setHeader("Content-Type", "application/json");
            mPost.setHeader("accept", "application/json");
            
            mPost.setEntity(new StringEntity(tenantId));
            mPost.setEntity(new StringEntity(mapper.writeValueAsString(req)));         
            
           /* RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, mPost, mPost.getEntity(), ContextRequestImpl.class);*/
            HttpResponse response = client.execute(mPost); 
            
            output = response.toString();
            mPost.releaseConnection( );

            return mapper.readValue(response.getEntity().getContent(),AuthzResponse.class);
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	
	}
}
