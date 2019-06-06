package edu.emory.cci.bindaas.datasource.provider.http;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.SubmitExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HTTPSubmitHandler implements ISubmitHandler {

    private Log log = LogFactory.getLog(getClass());

    @Override
    public QueryResult submit(JsonObject dataSource,
                              JsonObject endpointProperties, InputStream is, RequestContext requestContext)
            throws AbstractHttpCodeException {

        try {
            String data = IOUtils.toString(is);
            return submit(dataSource, endpointProperties, data, requestContext);
        } catch (IOException e) {
            log.error(e);
            throw new SubmitExecutionFailedException(HTTPProvider.class.getName(), HTTPProvider.VERSION, e);
        }
    }

    @Override
    public QueryResult submit(JsonObject dataSource,
                       JsonObject endpointProperties, String data, RequestContext requestContext)
            throws AbstractHttpCodeException {
        QueryResult queryResult = new QueryResult();

        try {
            OutputStream outStream = new FileOutputStream("output.csv");
            outStream.write(data.getBytes());

            queryResult.setData(new ByteArrayInputStream(data.getBytes()));
            queryResult.setMimeType(StandardMimeType.CSV.toString());

        } catch (IOException e) {
            log.error(e);
            throw new SubmitExecutionFailedException(HTTPProvider.class.getName(), HTTPProvider.VERSION, e);
        }

        return queryResult;
    }

    @Override
    public SubmitEndpoint validateAndInitializeSubmitEndpoint(
            SubmitEndpoint submitEndpoint) throws AbstractHttpCodeException {
        submitEndpoint.setType(SubmitEndpoint.Type.MULTIPART);
        return submitEndpoint;
    }

    @Override
    public JsonObject getSubmitPropertiesSchema() {
        // TODO Auto-generated method stub
        return null;
    }

}
