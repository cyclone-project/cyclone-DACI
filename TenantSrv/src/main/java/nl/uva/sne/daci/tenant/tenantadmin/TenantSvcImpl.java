/**
 * 
 */
package nl.uva.sne.daci.tenant.tenantadmin;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import redis.clients.jedis.Jedis;

/**
 * @author fturkmen
 *
 */
public class TenantSvcImpl implements TenantSvc {

	private static final Logger logger = LoggerFactory.getLogger(TenantSvcImpl.class);

	String redisAddress;
	String domain;
	
	// Key to the list of tenant identifiers stored in the Redis server
	private String tenantListConfigKey;
	
	
	public TenantSvcImpl(String redis, String dom) {
		redisAddress = redis;
		domain = dom;
		tenantListConfigKey = String.format(Configuration.DACI_TENANT_CONFIG_KEY, domain);
	}
	
	
	private boolean elementExists(String[] strList, String str){
		for (String s : strList)
			if (s.equals(str)) return true;
		return false;
	}
	
	
	
	/* (non-Javadoc)
	 * @see nl.uva.sne.daci.tenantmanager.TenantSvc#reserve(java.lang.String)
	 */
	@Override
	public boolean createTenant(String tenantId) {
		logger.info("Reserve a new tenant with Id: {}", tenantId);	
		// - Add a new entry in tenant-table: tenant-id | vi-description | tenant-pubkey
		boolean result = false;
		Jedis jedis = new Jedis(redisAddress);
		try{
			String strTenants = jedis.get(tenantListConfigKey);
			if (strTenants == null || !elementExists(strTenants.split(";"), tenantId)){
				strTenants = (strTenants == null ? "" : (strTenants + Configuration.TENANTID_DELIMITER)) 
						+ tenantId;
				jedis.set(tenantListConfigKey, strTenants);
				logger.info("Added tenant" + tenantId + " to " + tenantListConfigKey);
				result = true;
			}else logger.error("Tenant exists");
		}finally{		
			jedis.disconnect();
		}
		return result;
	}

	
	
	
	
	/* (non-Javadoc)
	 * @see nl.uva.sne.daci.tenantmanager.TenantSvc#decommission(java.lang.String)
	 */
	@Override
	public boolean removeTenant(String tenantId) {
		logger.info("Decommission the tenant with Id: {}", tenantId);
		/**TODO
		 * - Remove tenant's delegation policies in DelegationPoliciesDB
		 * - Remove tenant's authz policies in Tenant Authz DB
		 * - Remove tenant's record in TenantDB
		 */
		Jedis jedis = new Jedis(redisAddress);
		try{
			String strTenants = jedis.get(tenantListConfigKey);
			if (strTenants == null || strTenants.indexOf(tenantId) == -1) return false;
			if (tenantId.length() == strTenants.length())
				jedis.del(tenantListConfigKey);
			else{
				String[] tenants = StringUtils.split(strTenants, ";");
				String[] tenants_new = new String[tenants.length - 1];
				int j = 0;
				for (String t : tenants) 
					if (!t.equals(tenantId)){ 
						tenants_new[j++] = t;
					}
				jedis.set(tenantListConfigKey, StringUtils.join(tenants_new,";"));
			}
			logger.info("Removed tenant " + tenantId + " from " + tenantListConfigKey);
			System.out.println("Removed tenant " + tenantId + " from " + tenantListConfigKey);
			return true;
	
		}finally{		
			jedis.disconnect();
		}
	
	}

	
	public boolean checkTenant(String tenantId) {
		logger.info("Checking the tenant with Id: {}", tenantId);
		Jedis jedis = new Jedis(redisAddress);
		try{
			String strTenants = jedis.get(tenantListConfigKey);
			int index = strTenants.indexOf(tenantId);
			if (index != -1) return true;
			
			
		}finally{		
			jedis.disconnect();
		}
		
		return false;
	}
	
	
/*	 (non-Javadoc)
	 * @see nl.uva.sne.daci.tenantmanager.TenantSvc#instantiate(java.lang.String)	 
	@Override
	public int instantiateTenant(String tenantId) {
		logger.info("Instantiate the tenant with Id: {}", tenantId);
	
		 * - Generate delegation policy: DelegationPolicyManager:generatePolicy(tenantId, viDescription)
		 * - Add a new row in Tenant Authz DB:  |tenant-id| tenant-policies  (empty)| 
		 	
		return 0;
	}*/

	
	
	public static void main(String[] args){
		TenantSvcImpl imp = new TenantSvcImpl("localhost", "demo-uva");
		//imp.createTenant("tenant1");
		imp.removeTenant("tenant2");
		
	}
	
	
}
