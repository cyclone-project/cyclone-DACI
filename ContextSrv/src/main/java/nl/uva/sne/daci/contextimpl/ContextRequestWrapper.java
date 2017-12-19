package nl.uva.sne.daci.contextimpl;

public class ContextRequestWrapper {

	private String tenantId; 
	private ContextRequestImpl request;
	
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public ContextRequestImpl getRequest() {
		return request;
	}
	public void setRequest(ContextRequestImpl request) {
		this.request = request;
	}
	
	
	
}
