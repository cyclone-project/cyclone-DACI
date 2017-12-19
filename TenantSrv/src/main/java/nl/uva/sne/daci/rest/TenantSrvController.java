package nl.uva.sne.daci.rest;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.validation.BindingResult;
import com.fasterxml.jackson.core.JsonParser;
import nl.uva.sne.daci.tenant.authzadmin.PAP;
import nl.uva.sne.daci.tenant.tenantadmin.TenantSvc;
import nl.uva.sne.daci.tenant.tenantadmin.TenantSvcImpl;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.multipart.MultipartFile;




/*** TODO : Add logging here ...*/
@RestController
@EnableAutoConfiguration
public class TenantSrvController{	
	
    /***********    POLICY MANAGEMENT *********/
	    
    /*Upload Provider policies ...*/
	@PostMapping("/providerPolicy")
    public /*List<String>*/void providerPolicy(@RequestParam(value="redisAddress") String redisAddress,
    									  	   @RequestParam(value="domain") String domain,
    									  	   @RequestParam(value="policy") MultipartFile policyFile) {
    	try {
    		/*Providers are identified by the "domain" name*/
    		PAP pap =  new PAP(domain);    		
  	      	pap.setProviderPolicy(redisAddress, new ByteArrayInputStream(policyFile.getBytes()));
    	}catch(Exception e) {
			throw new RuntimeException("Couldn't set the Provider policies", e);
		}
    }
    
    
 
    /*Upload intertenant policies ...*/
	@PostMapping("/intertenantPolicy")
    public /*List<String>*/void intertenantPolicy(//@RequestParam(value="tenantId") String tenantId,
    											  @RequestParam(value="redisAddress") String redisAddress,
	  		  									  @RequestParam(value="domain") String domain,
	  		  									  @RequestParam(value="policy") MultipartFile policyFile) {
    	try {
    		PAP pap =  new PAP(domain);    		
  	      	pap.setIntertenantPolicy(/*tenantId,*/ redisAddress, new ByteArrayInputStream(policyFile.getBytes()));
    	}catch(Exception e) {
			throw new RuntimeException("Couldn't set the Trust policies", e);
		}
    }
    
    
    /*Upload intratenant policies ...*/
	@PostMapping("/tenantUserPolicy")
    public /*List<String>*/void tenantUserPolicy(//@RequestParam(value="tenantId") String tenantId,
    											 @RequestParam(value="redisAddress") String redisAddress,
	  		  									 @RequestParam(value="domain") String domain,
	  		  									 @RequestParam(value="policy") MultipartFile policyFile) {
    	try {
    		/*Here there should be a way of checking if the tenant is stored registered already (i.e. stored in redis)*/
    		TenantSvcImpl tsc = new TenantSvcImpl(redisAddress, domain);
    		/*if (!tsc.checkTenant(tenantId)){
    			System.out.println("Couldn't find the tenant");
    			return;
    		};*/
    		PAP pap =  new PAP(domain);    		
  	      	pap.setIntratenantPolicy(redisAddress, new ByteArrayInputStream(policyFile.getBytes()));
    	}catch(Exception e) {
			throw new RuntimeException("Couldn't set the User policies", e);
		}
    }
    
    
    
   
    
    /***********    TENANT MANAGEMENT *********/
	  /*Tenant Creation*/
	  @RequestMapping(
				value = "/tenants",
		    	method = RequestMethod.POST,
		    	consumes = { "application/json",  "application/xml"},
				produces = { "application/json",  "application/xml"}
				 )
	  //@PostMapping("/tenants")
	  public boolean tenantCreation(@RequestBody Map<String,String> body) {
		  try {
			  TenantSvc tsc = new TenantSvcImpl(body.get("redisAddress"), body.get("domain"));
			  return tsc.createTenant(body.get("tenantId"));
			}catch(Exception e) {
				throw new RuntimeException("Couldn't add the tenant", e);
			}
	  }
  
	  
	  /*Tenant Removal; just for fun, the parameters are in the header */
	  @RequestMapping(
				value = "/tenants",
		    	method = RequestMethod.DELETE
				 )
	  //@DeleteMapping("/tenants")
	  public boolean tenantDeletion(@RequestParam(value="tenantId") String tenantId,
				 					@RequestParam(value="redisAddress") String redisAddress,
				 					@RequestParam(value="domain") String domain)
	  {
		 
		  try {
			  TenantSvc tsc = new TenantSvcImpl(redisAddress, domain);
			  return tsc.removeTenant(tenantId);
			}catch(Exception e) {
				throw new RuntimeException("Couldn't remove the tenant", e);
			}
	}  

     
}
