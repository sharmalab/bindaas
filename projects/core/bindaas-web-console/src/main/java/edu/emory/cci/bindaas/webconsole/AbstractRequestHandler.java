package edu.emory.cci.bindaas.webconsole;


public abstract class AbstractRequestHandler implements IRequestHandler {

	private String[] segments;

	@Override
	public String[] getUriTemplateSegments() {
		if(segments == null)
		{
			segments = getUriTemplate().split("/");
		}
		return segments;
	}

}
