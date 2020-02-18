package edu.emory.cci.bindaas.datasource.provider.mongodb.operation;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import com.mongodb.*;
import com.mongodb.util.JSON;
import edu.emory.cci.bindaas.datasource.provider.mongodb.MongoDBProvider;
import edu.emory.cci.bindaas.datasource.provider.mongodb.model.OutputFormatProps;
import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.OutputFormatRegistry;
import edu.emory.cci.bindaas.framework.model.ProviderException;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static edu.emory.cci.bindaas.datasource.provider.mongodb.MongoDBProvider.getAuthorizationRulesCache;

public class AggregateOperationHandler implements IOperationHandler
{
    private Log log = LogFactory.getLog(getClass());

    @Override
    public QueryResult handleOperation(DBCollection collection,
                                       OutputFormatProps outputFormatProps, JsonObject operationArguments , OutputFormatRegistry registry, Object role, boolean authorization)
            throws ProviderException {
        AggregateOperationHandler.AggregateOperationDescriptor operationDescriptor = GSONUtil.getGSONInstance().fromJson(operationArguments, AggregateOperationHandler.AggregateOperationDescriptor.class);
        validateArguments(operationDescriptor);

        try{
            DBObject match = (DBObject) JSON.parse(operationDescriptor.match.toString());
            DBObject group = operationDescriptor.group == null ? null : (DBObject) JSON.parse(operationDescriptor.group.toString());
            DBObject sort = operationDescriptor.sort == null ? null : (DBObject) JSON.parse(operationDescriptor.sort.toString());


            DBCursor cursor = collection.find(match);
            if(role != null & authorization) {
                for(DBObject o : cursor) {
                    if(!getAuthorizationRulesCache().getIfPresent(role.toString()).
                            contains(o.get("project").toString())){
                        throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION, "Not authorized to execute this query.");
                    }
                }
            }
            DBObject matchobj = new BasicDBObject("$match", match);
            DBObject groupobj = new BasicDBObject("$group", group);
            DBObject sortobj = null;
            if(sort!=null){
                sortobj = new BasicDBObject("$sort", sort);
            }
            ArrayList<DBObject> pipeline = new ArrayList<DBObject>();
            pipeline.add(matchobj);
            pipeline.add(groupobj);
            if(sortobj!=null){
                pipeline.add(sortobj);
            }
            AggregationOutput aggregateOutput = collection.aggregate(pipeline);
            Iterable<DBObject> result = aggregateOutput.results();
            QueryResult queryResult = new QueryResult();
            queryResult.setMimeType(StandardMimeType.JSON.toString());
            queryResult.setData(new ByteArrayInputStream(result.toString().getBytes()));
            return queryResult;

        }
        catch(Exception e)
        {
            log.error(e);
            throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
        }
    }

    private  void validateArguments(AggregateOperationHandler.AggregateOperationDescriptor operationDescriptor) throws ProviderException
    {
        try {
            check(operationDescriptor!=null ,"Invalid query. Arguments cannot as [AggregateOperationDescriptor]");
            check(operationDescriptor.match!=null ,"Invalid query. AggregateOperationDescriptor missing parameter [match]");
            check(operationDescriptor.group!=null ,"Invalid query. AggregateOperationDescriptor missing parameter [group]");
        } catch (Exception e) {
            log.error(e);
            throw new ProviderException(MongoDBProvider.class.getName() , MongoDBProvider.VERSION ,e);
        }

    }

    private static void check(boolean condition , String message) throws Exception
    {
        if(!condition) throw new Exception(message);
    }

    public static class AggregateOperationDescriptor {

        @Expose public JsonObject match;
        @Expose public JsonObject group;
        @Expose public JsonObject sort;

        public JsonObject getMatch() {
            return match;
        }

        public void setMatch(JsonObject match) {
            this.match = match;
        }

        public JsonObject getGroup() {
            return group;
        }

        public void setGroup(JsonObject group) {
            this.group = group;
        }

        public JsonObject getSort() {
            return sort;
        }

        public void setSort(JsonObject sort) {
            this.sort = sort;
        }

    }
}
