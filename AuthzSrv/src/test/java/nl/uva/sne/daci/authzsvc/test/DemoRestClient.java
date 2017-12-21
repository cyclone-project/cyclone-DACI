package nl.uva.sne.daci.authzsvc.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.uva.sne.daci.authzsvc.AuthzRequest;
import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvc.AuthzSvc;

import org.apache.http.entity.mime.MultipartEntityBuilder;


public class DemoRestClient {
	

	static String providerPolicy = "policies/EnergyCyclone.EUC_ProviderPolicySet.xml";
	static String intertenantPolicy = "policies/EnergyCyclone.EUC_inter-tenant-policies.xml";
	static String intratenantPolicy = "policies/EnergyCyclone.API_Resources_Tenant.xml";
	
	
	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
        	restClient.createTenant("Energy_Tenant1", "http://localhost", "8092", "tenants", "localhost", "demo-uva");
        	restClient.setPolicy(providerPolicy, "providerPolicy", "http://localhost", "8092", "localhost", "demo-uva");
        	restClient.setPolicy(intertenantPolicy, "intertenantPolicy", "http://localhost", "8092", "localhost", "demo-uva");
        	restClient.setPolicy(intratenantPolicy, "tenantUserPolicy", "http://localhost", "8092", "localhost", "demo-uva");
        	
        	
        	
        	AuthzRequest ar = AuthzSrvImplTester.createRequest("fisfeps", "listPowerPlants", "execute");	
        	if (restClient.readPrivateData_Integrated(ar, "Energy_Tenant1","http://localhost", "8089")) 
        		System.out.println("SUCCESS!");
    		else System.out.println("NOT AUTHORIZED!!!");
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }
	}

	
	
	/*This will be direct check, call the service...*/
	public boolean readPrivateData_Integrated(AuthzRequest ar, String tenantId,
											  String authzSrvAddress, String authzSrvPort) throws Exception{
			
			
			AuthzSvc.DecisionType res = authorize(ar/*, tenantId*/, authzSrvAddress, authzSrvPort,
					  								"/pdps/" + tenantId+"/decision").getDecision();
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
	

	

	private void  setPolicy(/*String tenantId, */String policyFile, String endPoint, 
											 String tenantSrvAddress, String tenantSrvPort, 
											 String redisAddress, String domain) throws Exception {
		
		String output = null;
        String url = tenantSrvAddress + ":"+ tenantSrvPort + "/" + endPoint;//"http://localhost:8092/" + endPoint;
        HttpClient client = HttpClientBuilder.create().build();
        try{
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("redisAddress", redisAddress));
            nameValuePairs.add(new BasicNameValuePair("domain", domain));
            //nameValuePairs.add(new BasicNameValuePair("tenantId",tenantId));
            
            URIBuilder uri = new URIBuilder(url);
            uri.setParameters(nameValuePairs);
            HttpPost mPost = new HttpPost(uri.toString());

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
            
            HttpResponse response = client.execute(mPost); 
            
            output = response.toString();
            mPost.releaseConnection( );
            //System.out.println("Response : " + output);
            
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	
	}
	


	

	private void  createTenant(String tenantId, String tenantSrvAddress, String tenantSrvPort, 
												String endPoint, String redisAddress, 
												String domain) throws Exception {
		
		String output = null;
        String url = tenantSrvAddress + ":" + tenantSrvPort + "/"+ endPoint;//"http://localhost:8092/tenants";
        HttpClient client = HttpClients.createDefault();
        ObjectMapper mapper = new ObjectMapper();
 
        HttpPost mPost = new HttpPost(url);

        try {
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("redisAddress", redisAddress);
	        params.put("domain", domain);
	        params.put("tenantId",tenantId);
	        mPost.setEntity(new StringEntity(mapper.writeValueAsString(params)));
	        mPost.setHeader("Content-type", "application/json");
	        HttpResponse response = client.execute(mPost); 
	        output = response.toString();
	        mPost.releaseConnection( );
        }catch (Exception e) {
                e.printStackTrace();
        }

	}
	


	 
	
	
	public static AuthzResponse authorize(AuthzRequest req/*, String tenantId*/,
															String authzSrvAddress, String authzSrvPort, 
															String endPoint) throws Exception {
		
        String url = authzSrvAddress + ":"+ authzSrvPort + endPoint;//"http://localhost:8089/pdps/" + tenantId+"/decision";
        HttpClient client = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        try{
            HttpPost mPost = new HttpPost(url);
            
            mPost.setHeader("Content-Type", "application/json");
            mPost.setHeader("Accept", "application/json");
            
            System.out.println("STRING : " + mapper.writeValueAsString(req));
            //mPost.setEntity(new StringEntity(tenantId));
            mPost.setEntity(new StringEntity(mapper.writeValueAsString(req))); 
            
            
           /* RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, mPost, mPost.getEntity(), ContextRequestImpl.class);*/
            HttpResponse response = client.execute(mPost); 
            
            mPost.releaseConnection();

            return mapper.readValue(response.getEntity().getContent(),AuthzResponse.class);
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	
	}
}
