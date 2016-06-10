package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

public enum MongoDBQueryOperationType {

	find(new FindOperationHandler()),count(new CountOperationHandler()),group(new GroupOperationHandler()),mapReduce(new FindOperationHandler()) ;
	
	MongoDBQueryOperationType(IOperationHandler handler)
	{
		this.handler = handler;
	}
	
	private IOperationHandler handler;
	
	public IOperationHandler getHandler()
	{
		return handler;
	}
}
