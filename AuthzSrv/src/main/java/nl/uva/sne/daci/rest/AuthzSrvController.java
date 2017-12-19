package nl.uva.sne.daci.rest;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;


import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ExceptionHandler; 
import org.springframework.web.bind.annotation.RequestBody;


import nl.uva.sne.daci.authzsvc.AuthzRequest;
import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvc.AuthzSvc.DecisionType;
import nl.uva.sne.daci.authzsvcimp.AuthzSvcImpl;
import nl.uva.sne.daci.authzsvcimp.Configuration;
import nl.uva.sne.daci.authzsvcpdp.PDPSvc;
import nl.uva.sne.daci.authzsvcpdp.PDPSvcPoolImpl;
import nl.uva.sne.daci.authzsvcpolicy.PolicyManager;

@RestController
@EnableAutoConfiguration
public class AuthzSrvController{


	
	
  /*Creation of a PDP Instance --> This may be useful later!!!*/
  @RequestMapping(
  			value = "/pdps/{tenantId}/pdp",
	    	method = RequestMethod.GET,
	    	consumes = { "application/json",  "application/xml"},
	    	produces = { "application/json",  "application/xml"}
  			 )
  public PDPSvc pdpInstance(@PathVariable String tenantId) {
	  	
	  String authzPolicyKeyPrefix = String.format(Configuration.REDIS_KEYPREFIX_FORMAT, Configuration.DOMAIN);
	  
	  PolicyManager policyMgr = new PolicyManager(authzPolicyKeyPrefix, Configuration.REDIS_SERVER_ADDRESS);
	  
	  try {
		  PDPSvcPoolImpl pool = new PDPSvcPoolImpl(policyMgr);
		  PDPSvc newPDP = pool.getService(tenantId + "");
		  return newPDP;
		}catch(Exception e) {
			throw new RuntimeException("Couldn't get pdp", e);
		}
  }

  

  

  /*Policy Evaluation */
  @RequestMapping(
  			value = "/pdps/{tenantId}/decision",
	    	method = RequestMethod.POST,
	    	consumes = { "application/json",  "application/xml"},
	    	produces = { "application/json",  "application/xml"}
  			 )
  public AuthzResponse pdp(@PathVariable String tenantId,
						   @RequestBody AuthzRequest request) {

	  AuthzSvcImpl authzsvc = new AuthzSvcImpl();
	  authzsvc.init();
	  try {
			String encodedTenantId = URLDecoder.decode(tenantId, "UTF-8");			
			AuthzResponse res = authzsvc.authorize(/*authzPolicyKeyPrefix + ":" + */encodedTenantId, request);
			return res;
		} catch (UnsupportedEncodingException e1) {
			return new AuthzResponse(DecisionType.ERROR);
		}catch(Exception e) {
			throw new RuntimeException("Couldn't evaluate the policy", e);
		}
  }
  
  
  
 
  @RequestMapping(
  			value = "/pdps/{tenantId}/hello",
	    	method = RequestMethod.GET
  			 )
  public String hello(//@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,
								 @RequestParam(value="domain", defaultValue="demo-uva") String domain,
								 @PathVariable String tenantId/*,
								 @RequestParam(value="request") AuthzRequest request*/) {
	  try {
		  return "Hello: Authorization Service-> Domain:" + domain + " tenantId:"+ tenantId;
	  }catch(Exception e) {
			throw new RuntimeException("Couldn't get the message", e);
	  }

  }

  
    
}
