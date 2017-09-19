package nl.uva.sne.daci.tenant.tenantadmin;

/**
 * 	urn:eu:geysers:daci:policy:xacml3:domain-id:inter-tenant:<trustor-id>"
 * @author canhnt
 *
 */
public class Configuration {

	//public static final String PROVIDER_KEY_STYLE = "urn:eu:geysers:daci:%s:policy:xacml3:provider:root";
	
	//public static final String INTERTENANT_KEY_STYLE = "urn:eu:geysers:daci:%s:policy:xacml3:inter-tenant:";

	public static final String DACI_TENANT_CONFIG_KEY = "urn:eu:geysers:daci:%s:config:tenants";
	
	public static final String TENANTID_DELIMITER = ";";
	
	
	
	public static final String REDIS_KEYPREFIX_FORMAT = "urn:eu:geysers:daci:%s:policy:xacml3:intra-tenant";

	public static final String PROVIDER_KEY_STYLE = "urn:eu:geysers:daci:%s:policy:xacml3:provider:root";
	
	public static final String INTERTENANT_KEY_STYLE = "urn:eu:geysers:daci:%s:policy:xacml3:inter-tenant:";
	
	
		
}
