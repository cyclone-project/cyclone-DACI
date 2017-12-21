package nl.uva.sne.daci.rest;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.w3._2000._09.xmldsig.KeyInfoType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import nl.uva.sne.daci._1_0.schema.GrantTokenType;
import nl.uva.sne.daci._1_0.schema.ObjectFactory;
import nl.uva.sne.daci._1_0.schema.RequestType;
import nl.uva.sne.daci.tokensvc.TokenSvc;
import nl.uva.sne.daci.tokensvc.utils.XMLUtil;
import nl.uva.sne.daci.tokensvcimpl.TokenSvcImpl;

import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ExceptionHandler; 

@RestController
@EnableAutoConfiguration
public class TokenSrvController{

  
  private static String baseDir = "tokenSrvFiles/";
  private static String keyAlias = "tokensvc";
  private static String keyPasswd = "tokensvc-cloud";
  private static String keyStoreFile = "tokensvc-keystore.jks";
  private static String storePasswd = "cloudsecurity";
  private static String trustedKeyStore = "trusted-keystore.jks";
  private static String trustedKeyStorePasswd = "trusted";
  
  
  
  @RequestMapping(
			value = "tokens",
	    	method = RequestMethod.POST,
	    	consumes = { "application/xml","application/json"},
	    	produces = { "application/xml","application/json"}
			 )
  //@ExceptionHandler(IOException.class)
  //@ExceptionHandler(Exception.class)
  public String validation(@RequestBody String token) {
	  try {
		  TokenSvcImpl tsc = new TokenSvcImpl();
		  tsc.setBaseDir(baseDir);
		  tsc.setKeyAlias(keyAlias);
		  tsc.setKeyPassword(keyPasswd);
		  tsc.setKeyStore(keyStoreFile);
		  tsc.setKeyStorePassword(storePasswd);
		  tsc.setTrustedKeyStore(trustedKeyStore);
		  tsc.setTrustedKeyStorePassword(trustedKeyStorePasswd);
			
		  tsc.init();
		  
		  //return "true";
		  return Boolean.toString(tsc.verifyGrantToken(token));
		  
		}catch(Exception e) {
			throw new RuntimeException("Error validating the token...", e);
		}
}
 
  

  @RequestMapping(
			value = "tokens/{tenantId}",
	    	method = RequestMethod.POST,
	    	consumes = {"application/xml","application/json"/*,"application/x-www-form-urlencoded"*/},
	    	produces = {"application/xml","application/json"}
			 )
  //@ExceptionHandler(IOException.class)
  //@ExceptionHandler(Exception.class)
  public String token(/*@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
								 @RequestParam(value="domain", defaultValue="demo-uva") String domain,*/
		  						 @PathVariable String tenantId,
								 /*@RequestParam(value="request") AuthzRequest request*/
		  						 /*@RequestParam(value="request") */RequestType request,
		  						 /*@RequestParam(value="key")  */ KeyInfoType userKeyInfo) {
	  	
	  
	  try {
		  TokenSvcImpl tsc = new TokenSvcImpl();
		  tsc.setBaseDir(baseDir);
		  tsc.setKeyAlias(keyAlias);
		  tsc.setKeyPassword(keyPasswd);
		  tsc.setKeyStore(keyStoreFile);
		  tsc.setKeyStorePassword(storePasswd);
		  tsc.setTrustedKeyStore(trustedKeyStore);
		  tsc.setTrustedKeyStorePassword(trustedKeyStorePasswd);
			
		  tsc.init();
		  String token = tsc.issueGrantToken(tenantId, request, userKeyInfo);
		 
		  return token;
		}catch(Exception e) {
			throw new RuntimeException("Error issuing the token...", e);
		}

}
  
  
  
  @RequestMapping(
			value = "tokens/{tenantId}/hello",
	    	method = RequestMethod.GET
			 )
//@ExceptionHandler(IOException.class)
//@ExceptionHandler(Exception.class)
public String hello(@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
								 @RequestParam(value="domain", defaultValue="demo-uva") String domain,
								 @PathVariable String tenantId/*,
								 @RequestParam(value="request") AuthzRequest request*/) {
	  try {
		  return "Hello: Authorization Service --> Address:" + redisAddress + " Domain:" + domain + " tenantId:"+ tenantId;
		}catch(Exception e) {
			throw new RuntimeException("Error hello...", e);
		}

}
    
}
