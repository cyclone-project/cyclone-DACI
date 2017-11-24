package nl.uva.sne.daci.appsec;

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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.uva.sne.daci.appsec.AuthzRequest;
import nl.uva.sne.daci.appsec.AuthzResponse;
import nl.uva.sne.daci.appsec.AuthzSvc;

import org.apache.http.entity.mime.MultipartEntityBuilder;


/**
 * @author fturkmen
 * This code is just for testing the Authorization service...
 */
public class DemoRestClient {
	

	static String providerPolicy = "policies/EnergyCyclone.EUC_ProviderPolicySet.xml";
	static String intertenantPolicy = "policies/EnergyCyclone.EUC_inter-tenant-policies.xml";
	static String intratenantPolicy = "policies/EnergyCyclone.API_Resources_Tenant.xml";
	
	/**
	 * First create a tenant, then upload policies and send a request.
	 * 
	 * @param args[0] the address of the authorization service
	 * @param args[1] the port number of the authorization service
	 * @param args[2] the address of the tenant management service
	 * @param args[3] the port number of the tenant management service
	 * @param args[4] the address of the redis server
	 * @param args[5] the domain name used in DACI services
	 * @param args[6] the tenant ID
	 * @param args[7] the subject to be checked for authorization
	 * @param args[8] the resource to be accessed
	 */
	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
        	restClient.createTenant(args[6], args[2], args[3], "tenants", args[4], args[5]);
        	restClient.setPolicy(args[6], providerPolicy, args[2], args[3],"providerPolicy", args[4], args[5]);
        	restClient.setPolicy(args[6], intertenantPolicy, args[2], args[3], "intertenantPolicy", args[4], args[5]);
        	restClient.setPolicy(args[6], intratenantPolicy, args[2], args[3], "tenantUserPolicy", args[4], args[5]);
        	
        	AuthzRequest ar = createRequest(args[7], args[8], "execute");	
        	if (restClient.readPrivateData_Integrated(ar, args[6], args[0], args[1])) 
        		System.out.println("SUCCESS!");
    		else System.out.println("NOT AUTHORIZED!!!");
        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
        }
	}

	
	
	/*This will be direct check, call the service...*/
	public boolean readPrivateData_Integrated(AuthzRequest ar, String tenantId,
											  String authzSrvAddress, String authzSrvPort) throws Exception{
			
			
			AuthzSvc.DecisionType res = authorize(ar, tenantId, authzSrvAddress, authzSrvPort,
													  "/pdps/" + tenantId+"/decision").getDecision();
			if (res.equals(AuthzSvc.DecisionType.PERMIT))
				return readPrivateData();
			else return false;
	}
	
	
	/*This is for testing the aspect oriented DACI integration...*/
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
	
	

	private void  setPolicy(String tenantId, String policyFile, String tenantSrvAddress, String tenantSrvPort, 
																String endPoint, String redisAddress, 
																String domain) throws Exception {
		
		String output = null;
        String url = tenantSrvAddress + ":"+ tenantSrvPort + "/" + endPoint;
        HttpClient client = HttpClientBuilder.create().build();
        //ObjectMapper mapper = new ObjectMapper();
        try{
        	
        	
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("redisAddress", redisAddress));
            nameValuePairs.add(new BasicNameValuePair("domain", domain));
            nameValuePairs.add(new BasicNameValuePair("tenantId",tenantId));
            
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




	private void  createTenant(String tenantId, String tenantSrvAddress, String tenantSrvPort, 
												String endPoint, String redisAddress, 
												String domain) throws Exception {
		
		String output = null;
        String url = tenantSrvAddress + ":" + tenantSrvPort + "/"+ endPoint;
        HttpClient client = HttpClients.createDefault();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
        	    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try{
        	HttpPost mPost = new HttpPost(url);
        	 Map<String, String> params = new HashMap<String, String>();
            params.put("redisAddress", redisAddress);
	        params.put("domain", domain);
	        params.put("tenantId",tenantId);
	        mPost.setEntity(new StringEntity(mapper.writeValueAsString(params)));
	        mPost.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(mPost); 
            output = response.toString();
            mPost.releaseConnection( );
            System.out.println("Response... : " + output);           
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }	     	
	}
	
	/*TODO : Add removeTenant call as well*/

	 
	
	
	public static AuthzResponse authorize(AuthzRequest req, String tenantId, 
										  String authzSrvAddress, String authzSrvPort, 
										  String endPoint) throws Exception {
		
		String output = null;
        String url = authzSrvAddress + ":"+ authzSrvPort + endPoint;
        HttpClient client = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
        	    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
