package nl.uva.sne.daci.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;


import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySetType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;

import org.xml.sax.SAXException;

public class PolicySetupUtil {
	public static String loadPolicySet(String policysetFile) throws Exception{
		
		PolicySetType psRoot = XACMLUtil.unmarshalPolicySetType(policysetFile);
		if (psRoot != null && psRoot.getPolicySetId() != null) {
			List<JAXBElement<?>> jaxbs = psRoot.getPolicySetOrPolicyOrPolicySetIdReference();
			if (jaxbs != null && jaxbs.size() > 0)
				return XACMLUtil.marshal(psRoot);
		}
		
		System.err.println("Unable to load policyset");
		return null;
	}

	
	public static Map<String,String> loadPolicyorPolicySets(String policyFile) throws Exception{
		
		PolicySetType psRoot = XACMLUtil.unmarshalPolicySetType(policyFile);
		// map of <policyId, policy_data>
		Map<String, String> policies = new HashMap<String, String>();
		if (psRoot != null && psRoot.getPolicySetId() != null) {
			//List<JAXBElement<?>> jaxbs = psRoot.getPolicySetOrPolicyOrPolicySetIdReference();
			//if (jaxbs != null && jaxbs.size() > 0)
			policies.put(psRoot.getPolicySetId(), XACMLUtil.marshal(psRoot));
		}else{
			PolicyType pRoot = XACMLUtil.unmarshalPolicyType(policyFile);
			if (pRoot != null && pRoot.getPolicyId() != null) {
				//List<JAXBElement<?>> jaxbs = pRoot.get .getPolicySetOrPolicyOrPolicySetIdReference();
				//if (jaxbs != null && jaxbs.size() > 0)
				policies.put(pRoot.getPolicyId(), XACMLUtil.marshal(pRoot));
			}
		}
		if (policies.isEmpty()) System.err.println("Unable to load policyset");
		return policies.isEmpty() ? null : policies;
	}
	
	
	public static Map<String, String> loadPolicies(String xmlFile) throws ParserConfigurationException, SAXException, IOException {
		
		Map<String, String> policies = new HashMap<String, String>();
		PolicyType pRoot = XACMLUtil.unmarshalPolicyType(xmlFile);
		if (pRoot != null && pRoot.getPolicyId() != null){
			policies.put(pRoot.getPolicyId(), XACMLUtil.marshal(pRoot));
			return policies;
		}
		
		PolicySetType psRoot = XACMLUtil.unmarshalPolicySetType(xmlFile);
		// map of <policyId, policy_data>
		for(JAXBElement<?> jaxbElement : psRoot.getPolicySetOrPolicyOrPolicySetIdReference()) {
			
			Object value = jaxbElement.getValue();
			
			if (value instanceof PolicySetType) {
				
				PolicySetType ps = (PolicySetType)value;				
				policies.put(ps.getPolicySetId(), XACMLUtil.marshal(ps));
			} else if (value instanceof PolicyType) {
				
				PolicyType p = (PolicyType)value;				
				policies.put(p.getPolicyId(), XACMLUtil.marshal(p));
			} else {
				System.err.println("Unknown object data type:" + value.toString());
			}
		}
		
		return policies;
	}
	
	
	
	/******************************************************/
	
	/*XML InputStream ...*/
	public static String loadPolicySet(InputStream policysetFile) throws Exception{
		
		PolicySetType psRoot = XACMLUtil.unmarshalPolicySetType(policysetFile);
		if (psRoot != null && psRoot.getPolicySetId() != null) {
			List<JAXBElement<?>> jaxbs = psRoot.getPolicySetOrPolicyOrPolicySetIdReference();
			if (jaxbs != null && jaxbs.size() > 0)
				return XACMLUtil.marshal(psRoot);
		}
		
		System.err.println("Unable to load policyset");
		return null;
	}
	
	
	
	public static Map<String,String> loadPolicyorPolicySets(InputStream  xmlInputStr) throws Exception{
		
		PolicySetType psRoot = XACMLUtil.unmarshalPolicySetType(xmlInputStr);
		// map of <policyId, policy_data>
		Map<String, String> policies = new HashMap<String, String>();
		if (psRoot != null && psRoot.getPolicySetId() != null) {
			//List<JAXBElement<?>> jaxbs = psRoot.getPolicySetOrPolicyOrPolicySetIdReference();
			//if (jaxbs != null && jaxbs.size() > 0)
			policies.put(psRoot.getPolicySetId(), XACMLUtil.marshal(psRoot));
		}else{
			PolicyType pRoot = XACMLUtil.unmarshalPolicyType(xmlInputStr);
			if (pRoot != null && pRoot.getPolicyId() != null) {
				//List<JAXBElement<?>> jaxbs = pRoot.get .getPolicySetOrPolicyOrPolicySetIdReference();
				//if (jaxbs != null && jaxbs.size() > 0)
				policies.put(pRoot.getPolicyId(), XACMLUtil.marshal(pRoot));
			}
		}
		if (policies.isEmpty()) System.err.println("Unable to load policyset");
		return policies.isEmpty() ? null : policies;
	}
	
	public static Map<String, String> loadPolicies(InputStream xmlInputStr) throws ParserConfigurationException, SAXException, IOException {
		
		PolicySetType psRoot = XACMLUtil.unmarshalPolicySetType(xmlInputStr);
		
		// map of <policyId, policy_data>
		Map<String, String> policies = new HashMap<String, String>();
		for(JAXBElement<?> jaxbElement : psRoot.getPolicySetOrPolicyOrPolicySetIdReference()) {
			
			Object value = jaxbElement.getValue();
			
			if (value instanceof PolicySetType) {
				
				PolicySetType ps = (PolicySetType)value;				
				policies.put(ps.getPolicySetId(), XACMLUtil.marshal(ps));
			} else if (value instanceof PolicyType) {
				
				PolicyType p = (PolicyType)value;				
				policies.put(p.getPolicyId(), XACMLUtil.marshal(p));
			} else {
				System.err.println("Unknown object data type:" + value.toString());
			}
		}
		
		return policies;
	}
	
	
}
