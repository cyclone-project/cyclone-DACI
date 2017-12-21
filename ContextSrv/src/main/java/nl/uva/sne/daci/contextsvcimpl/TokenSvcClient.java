package nl.uva.sne.daci.contextsvcimpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci._1_0.schema.GrantTokenType;
import nl.uva.sne.daci._1_0.schema.ObjectFactory;
import nl.uva.sne.daci._1_0.schema.RequestType;
import nl.uva.sne.daci.utils.XMLUtil;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
//import org.apache.commons.httpclient.Header;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TokenSvcClient {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenSvcClient.class);

	
/*	public static final String METHOD_TOKEN = "/tokens";
	
	public static final String TENANT_ID = "daci_tenant_id";
	
	public static final String REQUEST = "daci_request";
	
	public static final String KEYINFO = "daci_keyinfo";
	
	public static final String TOKEN = "daci_token";
	
	
	public static final String SAMPLE_REQUEST_FILE = "src/test/resources/sample-request.xml";
	
	public static final String SAMPLE_KEYINFO_FILE = "src/test/resources/sample-keyinfo.xml";
	
	public static void main(String[] args) throws Exception {
		
        TokenSvcClient client = new TokenSvcClient(Configuration.TOKEN_SVC_URL);
        
        Document docRequest = XMLUtil.readXML(SAMPLE_REQUEST_FILE);
        RequestType request = XMLUtil.unmarshal(RequestType.class, docRequest.getDocumentElement());
        
        Document docKeyInfo = XMLUtil.readXML(SAMPLE_KEYINFO_FILE);
        KeyInfoType keyInfo = XMLUtil.unmarshal(KeyInfoType.class, docKeyInfo.getDocumentElement());
        
//        GrantTokenType t = client.issueGrantToken("abc123", request, keyInfo);
        
        String s = client.issueGrantToken("abc123", request, keyInfo);
        System.out.println("Received token:" + s);
        
//        print(t, System.out);
//        String sToken = convertGrantToken(t);
//        System.out.println("Received token:" + sToken);
        GrantTokenType t = convertGrantToken(s);
        System.out.println("Converted token:");print(t, System.out);
        
        System.out.println("2nd converted token:" + convertGrantToken(t));
        
    	client.verifyGrantToken(s);                        
	}*/
	
	/*private static String convertGrantToken(GrantTokenType t) throws JAXBException {
		

		OutputStream os = new ByteArrayOutputStream();
		
		JAXBElement<GrantTokenType> jaxb = (new ObjectFactory()).createGrantToken(t);
		
		JAXBContext jc = JAXBContext.newInstance(GrantTokenType.class);
		Marshaller m = jc.createMarshaller();
		m.marshal(jaxb, os);
				
		return os.toString();
	}

	private static void print(GrantTokenType t, OutputStream os) {
				
		JAXBElement<GrantTokenType> jaxb = (new ObjectFactory()).createGrantToken(t); 
		XMLUtil.print(jaxb, GrantTokenType.class, os);		
	}

*/
	private String serviceURL;
	private HttpClient httpClient;
	
	public TokenSvcClient(String address){
		this.serviceURL = address;
		
		httpClient = HttpClients.createDefault();
	}
	
	

	public boolean verifyGrantToken(String token) throws Exception {
        String url = this.serviceURL;
        HttpClient client = HttpClientBuilder.create().build();
        try{
            HttpPost mPost = new HttpPost(url);  
            mPost.setHeader("Content-Type", "application/xml");
            mPost.setHeader("accept", "application/xml");
            
            mPost.setEntity(new StringEntity(token));            
            
            HttpResponse response = client.execute(mPost);
            
            String responseString = new BasicResponseHandler().handleResponse(response);
            mPost.releaseConnection( );
            return new Boolean(responseString);
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        	
        }
		
	}
	
	public String issueGrantToken(String tenantId, RequestType request, KeyInfoType keyinfo) throws Exception {
		
        String url = this.serviceURL + "/" + tenantId;
        HttpClient client = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        try{
            HttpPost mPost = new HttpPost(url);
    		
            mPost.setEntity(new StringEntity(mapper.writeValueAsString(request)));
            mPost.setEntity(new StringEntity(mapper.writeValueAsString(keyinfo)));
    		mPost.setHeader("content-type", "application/json");
    		mPost.setHeader("accept", "application/xml");
 
            HttpResponse response = client.execute(mPost);

        	ResponseHandler<String> handler = new BasicResponseHandler();
    		String responseString = handler.handleResponse(response);
    		mPost.releaseConnection( );
    		return responseString;
        }catch(Exception e){
        	throw new Exception("Exception in adding bucket : " + e.getMessage());
        }
		
	}
	
}
