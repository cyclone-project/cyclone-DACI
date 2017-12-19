package nl.uva.sne.daci.authzsvc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//import javax.xml.bind.annotation.XmlRootElement;
//import javax.xml.bind.annotation.XmlType;

import nl.uva.sne.daci.authzsvc.AuthzResponse;
import nl.uva.sne.daci.authzsvc.AuthzSvc.DecisionType;

//@XmlRootElement(name="AuthzResponse", namespace="http://sne.uva.nl/daci/authzsvc")
//@XmlType(propOrder = {"token", "decision"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthzResponse{

	private String token;
		
	private DecisionType decision;
	
	public AuthzResponse(){
		
	}
	
	public AuthzResponse(DecisionType decision) {
		this.decision = decision;
		this.token = null;
	}
	
	public AuthzResponse(DecisionType decision, String token) {
		this.decision = decision;
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}

	public DecisionType getDecision() {
		return decision;
	}
	
	public void setDecision(DecisionType decision) {
		this.decision = decision;
	}
}
