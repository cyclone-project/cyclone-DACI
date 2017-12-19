package nl.uva.sne.daci.tokensvc.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.uva.sne.daci._1_0.schema.AttributeType;
import nl.uva.sne.daci._1_0.schema.AttributeValueType;
import nl.uva.sne.daci._1_0.schema.AttributesType;
import nl.uva.sne.daci._1_0.schema.GrantTokenType;
import nl.uva.sne.daci._1_0.schema.ObjectFactory;
import nl.uva.sne.daci._1_0.schema.RequestType;
import nl.uva.sne.daci.tokensvc.utils.XMLUtil; 

public class DemoRestClient {
	
	

	
	
	String TENANT_ID = "daci_tenant_id";
	String REQUEST = "daci_request";
	String KEYINFO = "daci_keyinfo";
	
	public static void main(String[] args) {
        DemoRestClient restClient = new DemoRestClient();
        try {
        	RequestType req = TokenSvcImplTest.generateRequest(0);
        	KeyInfoType kinfo = TokenSvcImplTest.generateKeyInfo();
            String token = restClient.issueGrantToken("EnergyUC_Tenant1", req, kinfo);
                 
            boolean res = restClient.verifyGrantToken(token);
        	if (res) System.out.println("Valid");
        	else System.out.println("Not valid");
        } catch (Exception e) {
            e.printStackTrace(); 
        }

	}


	private boolean verifyGrantToken(String token) throws Exception {
        String url = "http://localhost:8091/tokens";
        HttpClient client = HttpClientBuilder.create().build();
        try{
            HttpPost mPost = new HttpPost(url);  
            mPost.setHeader("Content-Type", "application/xml");
            mPost.setHeader("accept", "application/xml");
            
            mPost.setEntity(new StringEntity(token));            
            
            HttpResponse response = client.execute(mPost);
            mPost.releaseConnection( );
            String responseString = new BasicResponseHandler().handleResponse(response);
            
            return new Boolean(responseString);
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }
		
	}
	
	private String issueGrantToken(String tenantId, RequestType request, KeyInfoType keyinfo) throws Exception {
		
        String url = "http://localhost:8091/tokens/"+tenantId;
        HttpClient client = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        try{
            HttpPost mPost = new HttpPost(url);
    		
            mPost.setEntity(new StringEntity(mapper.writeValueAsString(request)));
            mPost.setEntity(new StringEntity(mapper.writeValueAsString(keyinfo)));
    		mPost.setHeader("content-type", "application/json");
    		mPost.setHeader("accept", "application/xml");
 
            HttpResponse response = client.execute(mPost);
            
            mPost.releaseConnection( );

        	ResponseHandler<String> handler = new BasicResponseHandler();
    		String responseString = handler.handleResponse(response);
    		
    		return responseString;
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        }
		
	}



	/**
	 * 
	 * @param token A grant token in pure string. 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static GrantTokenType convertGrantToken(String token) throws ParserConfigurationException, SAXException, IOException {

		InputStream istream = new ByteArrayInputStream(token.getBytes("UTF-8"));

		// From IS -> DOM -> JAXB -> Object	
		Document doc = XMLUtil.readXML(istream);

		return XMLUtil.unmarshal(GrantTokenType.class, doc.getDocumentElement());		
	}
	
	
	private static String print(RequestType request) {		
		OutputStream os = new ByteArrayOutputStream();				
		XMLUtil.print((new ObjectFactory()).createRequest(request), RequestType.class, os);		
		return os.toString();
	}


	private static String print(KeyInfoType keyInfo) {
		OutputStream os = new ByteArrayOutputStream();				
		XMLUtil.print((new ObjectFactory()).createKeyInfo(keyInfo), KeyInfoType.class, os);		
		return os.toString();
	}
	

	 
}
