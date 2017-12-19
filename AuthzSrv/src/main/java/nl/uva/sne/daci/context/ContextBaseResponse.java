package nl.uva.sne.daci.context;

import nl.uva.sne.daci.context.ContextResponse;

//FT:03.02.2017
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextBaseResponse implements ContextResponse {

	private ContextDecision decision;
	private String grantToken;
	
	public ContextBaseResponse(ContextDecision decision){
		this.decision = decision;
	}
	public ContextBaseResponse(String grantToken){
		this.decision = ContextDecision.NEED_REMOTE_DECISION;
		this.grantToken = grantToken;
	}
	
	public ContextBaseResponse(){
		this.decision = ContextDecision.ERROR;
	}
	
	@Override
	public ContextDecision getDecision() {
		return this.decision;
	}

	@Override
	public String getGrantToken() {
		return this.grantToken;
	}

}
