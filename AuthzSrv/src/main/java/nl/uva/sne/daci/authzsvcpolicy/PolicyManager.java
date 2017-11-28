package nl.uva.sne.daci.authzsvcpolicy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import nl.uva.sne.daci.utils.XACMLUtil;
import nl.uva.sne.xacml.policy.finder.PolicyFinder;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.xml.sax.SAXException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class PolicyManager {
	private static final transient org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PolicyManager.class);
		
	JedisPool jedisPool;

	private String serverAddress;

	private String domainKeyRoot;
	
	public PolicyManager(String policyKeyPrefix, String redisServerAddress) {
		this.serverAddress = redisServerAddress;
		
		this.domainKeyRoot = policyKeyPrefix;
		GenericObjectPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(1000);
		config.setMaxIdle(1000);
		System.out.println("************* THE REDIS ADDRESS FOR THE POLICY MANAGER " + this.serverAddress);
		jedisPool = new JedisPool(config, this.serverAddress);
		//System.out.println("REDIS ADDRESS FOr authz service .. " + this.serverAddress);
	}

	public PolicyFinder createPolicyFinder(String tenantId) {
		
		String tenantKeyRoot = getTenantKeyRoot(tenantId);
//		System.out.println("Tenant key root: " + tenantKeyRoot);
		PolicyFinder pf = new TenantPolicyFinderImpl(tenantKeyRoot, this);
				
		return pf;
	}

	private String getTenantKeyRoot(String tenantId) {
		if (tenantId == null || tenantId.isEmpty())
			throw new RuntimeException("Empty tenant identifier");
		return domainKeyRoot + ":" + tenantId;
	}

	/**
	 * Return a PolicyType or a PolicySetType instance
	 * @param key
	 * @return
	 */
	public Object getPolicy(String key) {
		log.debug("Get policy: " + key);
		Jedis jedis = null;
		try{
		 jedis = jedisPool.getResource();
		}catch(Exception e){
			System.err.println("Exception getResource " + e.getMessage());
		}
		try{
			String v = jedis.get(key);
			
			InputStream is = new ByteArrayInputStream(v.getBytes());
			
			try {
				PolicyType p;
				p = XACMLUtil.unmarshalPolicyType(is);
				if (p != null)
					return p;
				
				PolicySetType ps = XACMLUtil.unmarshalPolicySetType(is);
				if (ps != null)
					return ps;				
			} catch (ParserConfigurationException | SAXException | IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			
			throw new RuntimeException("Unable to marshal policy:" + v);
			
		}finally {
			jedisPool.close();// .returnResource(jedis);
		}		
	}
}
