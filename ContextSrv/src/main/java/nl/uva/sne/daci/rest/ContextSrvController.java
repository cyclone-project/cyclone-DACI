package nl.uva.sne.daci.rest;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.contextimpl.ContextRequestImpl;
import nl.uva.sne.daci.context.ContextResponse;
import nl.uva.sne.daci.contextimpl.ContextBaseResponse;
import nl.uva.sne.daci.contextsvcimpl.Configuration;
import nl.uva.sne.daci.contextsvcimpl.ContextSvcImpl;

import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ExceptionHandler; 

@RestController
@EnableAutoConfiguration
public class ContextSrvController{

	
	
  /*Context checking*/
  @RequestMapping(
			value = "/contexts",
	    	method = RequestMethod.POST,
	    	consumes = { "application/json",  "application/xml"},
	    	produces = { "application/json",  "application/xml"}
			 )
  //@ExceptionHandler(IOException.class)
  //@ExceptionHandler(Exception.class)
  public ContextBaseResponse context(/*@RequestParam(value="redisAddress", defaultValue="localhost") String redisAddress,*/
								 /*@RequestParam(value="domain", defaultValue="demo-uva") String domain,*/
								 /*@RequestParam(value="tenantId")*/ String tenantId,
								 /*@RequestParam(value="request") AuthzRequest request*/
		  						 /*@RequestParam(value="ctxRequest")*/ @RequestBody ContextRequestImpl ctxRequest) {
	  	
	  
	  try {
		  
		    
		  	ContextSvcImpl contextSvc;
		    contextSvc = new ContextSvcImpl(Configuration.DOMAIN, Configuration.REDIS_SERVER_ADDRESS);
		    contextSvc.init();
		    
		    
		    ContextBaseResponse res = (ContextBaseResponse) contextSvc.validate(ctxRequest, tenantId);
		    return res;
		}catch(Exception e) {
			throw new RuntimeException("Couldn't get the Context Server ...", e);
		}
	  //ev = new Evaluator(redisAddress, domain);
	  //return ev.checkAuthorization(tenantId, request);
}
  

  
  @RequestMapping(
  			value = "/contexts/{clientId}/hello",
	    	method = RequestMethod.GET
  			 )
  //@ExceptionHandler(IOException.class)
  //@ExceptionHandler(Exception.class)
  public String hello(@PathVariable String clientId/*,
								 @RequestParam(value="request") AuthzRequest request*/) {
	  try {
		  return "Hello: Context Service --> clientId:"+ clientId;
		}catch(Exception e) {
			throw new RuntimeException("Couldn't get the message", e);
		}

  }
  
    
}
