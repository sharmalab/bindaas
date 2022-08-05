package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

public enum MongoDBQueryOperationType {

	find(new FindOperationHandler()),count(new CountOperationHandler()),distinct(new DistinctOperationHandler()),
	group(new GroupOperationHandler()),mapReduce(new MapReduceOperationHandler()),aggregate(new AggregateOperationHandler());
	
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
