package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

public enum MongoDBModifyOperationType {

	update(new UpdateOperationHandler()) , delete(new DeleteOperationHandler());
	
	MongoDBModifyOperationType(IOperationHandler handler)
	{
		this.handler = handler;
	}
	
	private IOperationHandler handler;
	
	public IOperationHandler getHandler()
	{
		return handler;
	}
}
