package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

public enum MongoDBOperation {

	find(new FindOperationHandler()),count(new CountOperationHandler()),group(new GroupOperationHandler()),mapReduce(new FindOperationHandler()) ;
	
	MongoDBOperation(IOperationHandler handler)
	{
		this.handler = handler;
	}
	
	private IOperationHandler handler;
	
	public IOperationHandler getHandler()
	{
		return handler;
	}
}
