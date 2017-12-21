package nl.uva.sne.daci.contextsvc.test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.uva.sne.daci.context.ContextRequest;
import nl.uva.sne.daci.context.ContextRequestBuilder;
import nl.uva.sne.daci.context.ContextResponse;
import nl.uva.sne.daci.contextimpl.ContextManager;
import nl.uva.sne.daci.contextsvcimpl.Configuration;
import nl.uva.sne.daci.contextsvcimpl.ContextSvcImpl;
import nl.uva.sne.daci.context.tenant.TenantManager;
import redis.clients.jedis.Jedis;

public class ContextSrvImplTester {

	
	private static final String INTRATENANT_POLICY1 = "policies/EnergyCyclone.API_Resources_Tenant.xml";
	private static final String PROVIDER_POLICY1 = "policies/EnergyCyclone.EUC_ProviderPolicySet.xml";
	private static final String INTERTENANT_POLICY1 = "policies/EnergyCyclone.EUC_inter-tenant-policies.xml";
	
	
	
	public static String[][] attributes = new String[][]{
		{"urn:oasis:names:tc:xacml:1.0:action:action-id", /*"SLI:Operate-VR:Stop"*/"execute"},
		//{"urn:oasis:names:tc:xacml:1.0:subject:subject-role", /*"admin"*/ "Bioinformatics_IFB_Tenant2"},
		{"urn:oasis:names:tc:xacml:1.0:subject:subject-id", /*"admin"*/ "Energy_Tenant1"},
		//{"urn:oasis:names:tc:xacml:1.0:subject:subject-id", /*"admin"*/ "Bioinformatics_IFB_Tenant1"},
		{"urn:oasis:names:tc:xacml:1.0:resource:resource-id", /*"http://demo3.uva.nl/vi/745/ComputingNode"*/"listPowerPlants"}
	};
	
	
	public static String redisAddress = "localhost";
	public static String domain = "demo-uva";
	
	public static void main(String[] args){
		try{
			ContextSrvImplTester c = new ContextSrvImplTester();
			List<String> tenants = c.setupPolicies(redisAddress,domain);
			/////////////////////
			System.out.println("TENANT IDs ...");
			for (String str : tenants)
				System.out.println("Tenant Id : " + str);
			/////////////////////
			c.setupTenantIdentifiers(tenants, redisAddress, domain);
		}catch(Exception e){
			System.err.println("Exception in Constructor");
		}
		
		ContextSvcImpl ctxsvc = new ContextSvcImpl(domain, redisAddress);
		ctxsvc.init();
		
	
		String tenantId = "";//"Bioinformatics_IFB_Tenant1";
		ContextResponse res = ctxsvc.validate(buildContextRequest(tenantId), /*"Bioinformatics_IFB_Tenant1"*/"");
		System.out.println("Response : " + res.getDecision().toString());
	}
	
	
	
	static ContextRequest buildContextRequest(String tenantId){
		Map<String, String> request = new HashMap<String, String>();
		for (int i = 0; i < attributes.length; i++) {
			request.put(attributes[i][0], attributes[i][1]);
		}
		
		ContextRequestBuilder builder = new ContextRequestBuilder ();
		ContextRequest ctxRequest = null;
		if (tenantId == "") ctxRequest = builder.create(request); //With any subject attribute
		else ctxRequest = builder.create(tenantId, request); //With subject-id = tenantId
		return ctxRequest;
	}
	
	void setupTenantIdentifiers(List<String> tenants, String redisAddress, String domain) {
		Jedis jedis = new Jedis(redisAddress);
		
		try {
			TenantManager tenantMgr = new TenantManager(domain, redisAddress);
			StringBuilder builder = new StringBuilder();
			
			int index = 0;
			builder.append(tenants.get(index++));		
			for(; index < tenants.size(); index++) {
				builder.append(Configuration.TENANTID_DELIMITER + tenants.get(index));
			}
			
			jedis.set(tenantMgr.getTenantConfigKey(), builder.toString());
			
		}finally{		
			jedis.disconnect();
		}
	}
	
	
	/*Setup policies ...*/
	List<String> setupPolicies(String redisAddress, String domain) throws Exception {
		
		ContextManager ctxMan = new ContextManager(domain, redisAddress);		
		
		Jedis jedis = new Jedis(redisAddress);
		try {	
			// Load provider policies to redis
			jedis.set(ctxMan.getProviderPolicyKey(), PolicySetupUtil.loadPolicySet(PROVIDER_POLICY1));
			
			//load Intertenant policies to redis...
			loadIntertenantPolicies(INTERTENANT_POLICY1, ctxMan, jedis);

			//load Intratenant policies to redis...
			Map<String, String> tenantPolicies = loadIntratenantPolicies(INTRATENANT_POLICY1, ctxMan, jedis,domain);
			
			return new ArrayList<String>(tenantPolicies.keySet());
		}finally{
			jedis.disconnect();
		}
	}
	
	
	
	private Map<String, String> loadIntertenantPolicies(String policyName, ContextManager ctxMan, Jedis jedis){
		try {
			// load inter-tenant policies to redis
			Map<String, String> tenantPolicies = PolicySetupUtil.loadPolicies(policyName);
			for(String pId : tenantPolicies.keySet()) {				
				String pKey = ctxMan.getInterTenantPolicyKey(pId);
				jedis.set(pKey, tenantPolicies.get(pId));			
			}
			return tenantPolicies;
		}catch(Exception e){
			System.err.println("Exception in loadIntertenantPolicies" + e.getMessage());
			return null;
		}/*finally{
			jedis.disconnect();
		}*/
	}
	
	
	
	
	private Map<String, String> loadIntratenantPolicies(String policyName, ContextManager ctxMan, Jedis jedis, String domain){
		try{
			Map<String, String> intraTenantPolicies = /*PolicySetupUtil.loadPolicyorPolicySets(policyName);//*/PolicySetupUtil.loadPolicies(policyName);
			String authzPolicyKeyPrefix = String.format(Configuration.REDIS_KEYPREFIX_FORMAT, domain);			
			for(String pId : intraTenantPolicies.keySet()) {
				String pKey = authzPolicyKeyPrefix + ":" + pId;		
				jedis.set(pKey, intraTenantPolicies.get(pId));
				System.out.println("Put intra-tenant policy:" + pKey);
			}
			return intraTenantPolicies;
		}catch(Exception e){
			System.err.println("Exception in loadIntratenantPolicies"+ e.getMessage());
			return null;
		}
		/*finally{
			jedis.disconnect();
		}*/
	}
	
	
}
