package edu.emory.cci.bindaas.datasource.provider.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.datasource.provider.http.model.HTTPQuery;
import edu.emory.cci.bindaas.datasource.provider.http.model.SubmitEndpointProperties;
import edu.emory.cci.bindaas.framework.api.ISubmitHandler;
import edu.emory.cci.bindaas.framework.model.QueryResult;
import edu.emory.cci.bindaas.framework.model.RequestContext;
import edu.emory.cci.bindaas.framework.model.SubmitEndpoint;
import edu.emory.cci.bindaas.framework.provider.exception.AbstractHttpCodeException;
import edu.emory.cci.bindaas.framework.provider.exception.NetworkConnectionException;
import edu.emory.cci.bindaas.framework.provider.exception.QueryExecutionFailedException;
import edu.emory.cci.bindaas.framework.provider.exception.SubmitExecutionFailedException;
import edu.emory.cci.bindaas.framework.util.GSONUtil;
import edu.emory.cci.bindaas.framework.util.IOUtils;
import edu.emory.cci.bindaas.framework.util.StandardMimeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;

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
        QueryResult result = new QueryResult();

        try {

            SubmitEndpointProperties submitEndpointProperties = GSONUtil
                    .getGSONInstance().fromJson(endpointProperties,
                            SubmitEndpointProperties.class);

            String submitUrl = submitEndpointProperties.getUrl();

            OutputStream outStream = new FileOutputStream(submitEndpointProperties.getOutputfileName());

            outStream.write(data.getBytes());

            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(submitUrl);
            httppost.setEntity(new FileEntity(new File("C:/Users/Marjie/Downloads/twisted_DNA_bundles/monotwist.L1.v1.json")));

            final HttpResponse response = httpclient.execute(httppost);

            if(response!=null && response.getStatusLine().getStatusCode() == 200 && response.getEntity()!=null)
            {

                result.setCallback(new QueryResult.Callback() {

                    @Override
                    public void callback(OutputStream servletOutputStream, Properties context)
                            throws AbstractHttpCodeException {

                        try {
                            response.getEntity().writeTo(servletOutputStream);
                        } catch (IOException e) {
                            log.error(e);
                            throw new NetworkConnectionException(getClass().getName(), 1 , e);
                        }
                    }
                });

                result.setMimeType(response.getFirstHeader("Content-Type").getValue());

            }
            else
            {
                throw new Exception("Error connecting remote URL [" + submitUrl + "]. Server Response [" + response.getStatusLine().toString() + "]" );

            }


//            queryResult.setData(new ByteArrayInputStream(data.getBytes()));
//            queryResult.setMimeType(StandardMimeType.CSV.toString());

        } catch (IOException e) {
            log.error(e);
            throw new SubmitExecutionFailedException(HTTPProvider.class.getName(), HTTPProvider.VERSION, e);
        } catch (Exception e) {
            log.error(e);
            throw new QueryExecutionFailedException(HTTPProvider.class.getName(), HTTPProvider.VERSION);
        }

        return result;
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
