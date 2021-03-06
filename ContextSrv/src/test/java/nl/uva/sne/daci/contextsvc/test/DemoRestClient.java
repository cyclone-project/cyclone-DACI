package nl.uva.sne.daci.contextsvc.test;


import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
//import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.contextimpl.ContextRequestWrapper;
import nl.uva.sne.daci.context.ContextResponse;
import nl.uva.sne.daci.contextimpl.ContextBaseResponse;
import nl.uva.sne.daci.contextimpl.ContextRequestImpl;
import nl.uva.sne.daci.contextsvcimpl.Configuration;
import nl.uva.sne.daci.contextsvcimpl.ContextSvcImpl;




public class DemoRestClient {
	
	

	
	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
        	
  			ContextSrvImplTester c = new ContextSrvImplTester();
  			List<String> tenants = c.setupPolicies(ContextSrvImplTester.redisAddress,ContextSrvImplTester.domain);
  			c.setupTenantIdentifiers(tenants, ContextSrvImplTester.redisAddress, ContextSrvImplTester.domain);	
  			
        	/*Build the context request here ...*/
        	
    		String tenantId = "Energy_Tenant1";
    		ContextRequest req = c.buildContextRequest(tenantId);
    		
    		ContextResponse res = restClient.validate((ContextRequestImpl)req, tenantId, "http://localhost", "8090", "contexts");
    		System.out.println("Response : " + res.getDecision().toString());
        } catch (Exception e) {
            e.printStackTrace(); 
        }

	}
	

	private ContextBaseResponse  validate(ContextRequestImpl req, String tenantId, 
																  String ctxSrvAddress, String ctxSrvPort, 
																  String endPoint) throws Exception {
	

        String url = ctxSrvAddress + ":" + ctxSrvPort + "/" + endPoint;
        HttpClient client = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        HttpPost mPost = null;
        try{
            mPost = new HttpPost(url);

            /*GrantTokenType gt = convertGrantToken(URLDecoder.decode(token, "UTF-8"));
            System.out.println("Here we are..");
            String tokennew = convertGrantToken(gt);
            System.out.println("Here we are.." + tokennew);*/
	        
            mPost.setHeader("Content-Type", "application/json");
            mPost.setHeader("accept", "application/json");
            
            ContextRequestWrapper crw = new ContextRequestWrapper();
	        crw.setRequest(req);
	        crw.setTenantId(tenantId);
	        mPost.setEntity(new StringEntity(mapper.writeValueAsString(crw)));       
            
           /* RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(url, mPost, mPost.getEntity(), ContextRequestImpl.class);*/
            HttpResponse response = client.execute(mPost); 
            
            return mapper.readValue(response.getEntity().getContent(),ContextBaseResponse.class);
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        }finally{
            mPost.releaseConnection( );
        }
	}
	
	 
}
