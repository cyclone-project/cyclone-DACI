package nl.uva.sne.daci.contextsvcimpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3._2000._09.xmldsig.KeyInfoType;

import nl.uva.sne.daci._1_0.schema.AttributeType;
import nl.uva.sne.daci._1_0.schema.AttributeValueType;
import nl.uva.sne.daci._1_0.schema.AttributesType;
import nl.uva.sne.daci._1_0.schema.ObjectFactory;
import nl.uva.sne.daci._1_0.schema.RequestType;
import nl.uva.sne.daci.context.Context;
import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.context.ContextResponse;
import nl.uva.sne.daci.context.ContextStore;
import nl.uva.sne.daci.contextsvc.ContextSvc;
import nl.uva.sne.daci.context.ContextResponse.ContextDecision;
import nl.uva.sne.daci.contextimpl.ContextBaseResponse;
import nl.uva.sne.daci.contextimpl.ContextManager;
import nl.uva.sne.daci.contextimpl.ContextStoreImpl;
import nl.uva.sne.daci.context.tenant.TenantManager;
//FT:03.02.2017
//import nl.uva.sne.daci.tokensvc.TokenSvc;
import nl.uva.sne.daci.utils.XMLUtil;

public class ContextSvcImpl implements ContextSvc {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContextSvcImpl.class);

	private static final int NUM_ADDITIONAL_ATTR = 0;
	
	private String domain;
	private String redisServerAddress;
	
	private ContextStore ctxStore;

	//FT:03.02.2017
	//private TokenSvc tokensvc;
	private TokenSvcClient tsc;
	
	public ContextSvcImpl() throws Exception {
		this(Configuration.DOMAIN, Configuration.REDIS_SERVER_ADDRESS);
		
		this.tsc = new TokenSvcClient(Configuration.TOKEN_SVC_URL); 
		/*if (tokensvc == null)
			throw new RuntimeException("Cannot connect to the DACI tokenservice");		
		this.tokensvc = tokensvc;*/		
	}

	public ContextSvcImpl(String domain, String redisServerAddr){
		this.domain = domain;
		this.redisServerAddress = redisServerAddr;	
		this.tsc = new TokenSvcClient(Configuration.TOKEN_SVC_URL); 
	}
		
	
	public void init() {
		try {
			log.info("Initializing DACI ContextService");
			long startTime = System.currentTimeMillis();
			
			loadContexts();
			
			long currentTime = System.currentTimeMillis();
			log.info("DACI ContextService initialization done: {} ms", (currentTime-startTime));
		} catch (Exception e) {
			log.error("Failed to load contexts from Redis server:" + this.redisServerAddress);
			e.printStackTrace();
			throw new RuntimeException("Error connecting to Redis server", e);
		}
	}
	/**
	 * Load contexts from provider's delegation policy & tenant sharing policy.
	 * @throws Exception 
	 */
	private void loadContexts() throws Exception {
		
		// Load tenant identifiers from configuration
		TenantManager tenantMgr = new TenantManager(domain, redisServerAddress);
		log.info("Load tenant identifiers from {}", redisServerAddress);
		tenantMgr.loadTenantIds();
		
		// Load contexts from redis configuration
		ContextManager ctxMan = new ContextManager(domain, redisServerAddress);
		
		List<String> tenants = tenantMgr.getTenantIdentifiers();
		ctxMan.setTenantIdentifiers(tenants);
		
		log.info("Load provider contexts from {}", redisServerAddress);
		ctxMan.loadProviderContexts();
		
		log.info("Load inter-tenant contexts from {}", redisServerAddress);
		ctxMan.loadInterTenantContexts();
		
		ctxStore = ctxMan.getContextStore();
		log.info("# of contexts: {}", ctxStore.getContexts().size());
	}
	
//	public void setContexts(ContextStore ctxStore) {
//		this.ctxStore = ctxStore;
//	}

	@Override
	public ContextResponse validate(ContextRequest request,String tenantId){
		log.debug("Receive request:{}", request.toString());
		try {
			return validate(request.getSubjectAttributes(), request.getPermissionAttributes(), tenantId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ContextBaseResponse(ContextDecision.ERROR);
		}		

	}

	
	
	private ContextResponse validate(Map<String, String> subject, Map<String, String> permission,
																String tenantId) throws Exception {
		
		Map<String, String> attrs = new HashMap<String, String>(subject.size() + permission.size());
		attrs.putAll(subject);
		attrs.putAll(permission);
		
		for(Context ctx: ctxStore.getContexts()) {

			if (ctx.validate(attrs)) {
				if (this.ctxStore.isRootContext(ctx)) {
					// if this is the ctx for local resource
					if (isLocalContext(ctx))						
						return new ContextBaseResponse(ContextDecision.PERMIT);
					else{ 
					// otherwise, create a grant-token and relay to user for the purpose to get access token
					// at the remote domain.
						log.info("Will ask the remote domain .... for " + tenantId);
						return createGrantTokenResponse(ctx,tenantId);
					}
				}					
				else {
					// if it's not the root contexts, find the ctx that can validate <ctx_issuer, permission> request
					return validate(ctx.getIssuerAttributes(), permission, tenantId);
				}
			}
		}
		
		// return an error response
		return new ContextBaseResponse();
	}

	private ContextResponse createGrantTokenResponse(Context ctx, String tenantId) throws Exception {
		
		/*if (this.tokensvc == null)
			throw new Exception("Cannot connect to tokensvc");*/
		
//		String grantToken = "temp-grant-token";
//		ContextResponse resp = new ContextBaseResponse(grantToken);
//		
//		return resp;
		// create a dummy request
		RequestType request = createDummyRequest();
		KeyInfoType userKeyInfo = getDummyKeyInfo();
				
		//String token = this.tokensvc.issueGrantToken("http://demo3.sne.uva.nl/VI/750", request, userKeyInfo);
		String token = tsc.issueGrantToken(tenantId, request, userKeyInfo);
		
		ContextResponse resp = new ContextBaseResponse(token);
		
		return resp;
	}

	private KeyInfoType getDummyKeyInfo() {
		return DummyRequestGenerator.generateKeyInfo();
	}

	private RequestType createDummyRequest() {
		return DummyRequestGenerator.generate(NUM_ADDITIONAL_ATTR);
	}

	/** FT: This needs to be fully implemented but in CYCLONE it was not needed...*/
	private boolean isLocalContext(Context ctx) {
		// temporary: always for local resource first.
		//return true;
		
		// for testing performance with tokenservice, return false;
		return false;
	}
}
