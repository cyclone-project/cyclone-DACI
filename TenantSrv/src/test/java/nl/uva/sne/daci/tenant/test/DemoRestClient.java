package nl.uva.sne.daci.tenant.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.entity.mime.MultipartEntityBuilder;


public class DemoRestClient {
	

	static String providerPolicy = "policies/EnergyCyclone.EUC_ProviderPolicySet.xml";
	static String intertenantPolicy = "policies/EnergyCyclone.EUC_inter-tenant-policies.xml";
	static String intratenantPolicy = "policies/EnergyCyclone.API_Resources_Tenant.xml";
	
	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
        
        	restClient.createTenant("EnergyUC_Tenant1", "localhost", "demo-uva");
        	//restClient.deleteTenant("EnergyUC_Tenant2", "localhost", "demo-uva");
        	restClient.setPolicy(providerPolicy, "providerPolicy", "localhost", "demo-uva");
        	restClient.setPolicy(intertenantPolicy, "intertenantPolicy","localhost","demo-uva");
        	restClient.setPolicy(intratenantPolicy, "tenantUserPolicy","localhost","demo-uva");
        	
        } catch (Exception e) {
            e.printStackTrace(); 
        }
	}

	

	private void  setPolicy(/*String tenantId, */String policyFile, String endPoint, 
											 String redisAddress, String domain) throws Exception {
		
		String output = null;
        String url = "http://localhost:8092/" + endPoint;
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

	
	private void  createTenant(String tenantId, String redisAddress, String domain) throws Exception {
		
		String output = null;
        String url = "http://localhost:8092/tenants";
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
            //System.out.println("Response from Tenant Srv... : " + output);
        }catch (Exception e) {
                e.printStackTrace();
        }

	}
	
	private void  deleteTenant(String tenantId, String redisAddress, String domain) throws Exception {
		
		String output = null;
        String url = "http://localhost:8092/tenants";
        HttpClient client = HttpClients.createDefault();
 
        
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("redisAddress", redisAddress));
            nameValuePairs.add(new BasicNameValuePair("domain", domain));
            nameValuePairs.add(new BasicNameValuePair("tenantId",tenantId));
            URIBuilder uri = new URIBuilder(url);
            uri.setParameters(nameValuePairs);
            HttpDelete mDelete = new HttpDelete(uri.toString());
            HttpResponse response = client.execute(mDelete); 
            output = response.toString();
            mDelete.releaseConnection( );
            //System.out.println("Response from Tenant Srv... : " + output);
        }catch (Exception e) {
                e.printStackTrace();
        }

	}
	 
}
