package edu.emory.cci.bindaas.datasource.provider.mongodb.util;

import java.io.IOException;
import java.io.InputStream;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import edu.emory.cci.bindaas.datasource.provider.mongodb.outputformat.IFormatHandler.OnFinishHandler;
/**
 * Thread-Safe implementation
 * @author nadir
 *
 */
public class JsonResultSetInputStream extends InputStream{

	private DBCursor dbCursor;
	private byte[] buffer;
	private Integer position;
	private boolean finished;
	private boolean isFirst;
	private OnFinishHandler finishHandler;
	
	public JsonResultSetInputStream(DBCursor dbCursor, OnFinishHandler finishHandler)
	{
		this.dbCursor = dbCursor;
		this.finishHandler = finishHandler;
		isFirst = true;
	}
	
	private byte[] readFirstObject()
	{
		DBObject dbo = this.dbCursor.next();
		return JSON.serialize(dbo).getBytes();
	}
	
	private byte[] readNextObject()
	{
		DBObject dbo = this.dbCursor.next();
		return String.format(",%s", JSON.serialize(dbo)).getBytes();
	}
	
	
	
	
	@Override
	public void close() throws IOException {
		try {
			finishHandler.finish();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public synchronized int read() throws IOException {
			
            if(buffer == null) {
                // first call of read method
                buffer = "[" . getBytes();
                position = 0;
                return buffer[position++] & (0xff);
            } else {
                // not first call of read method
                if(position < buffer.length) {
                    // buffer already has some data in, which hasn't been read yet - returning it
                    return buffer[position++] & (0xff);
                } else {
                    // all data from buffer was read - checking whether there is next row and re-filling buffer
                    if(!dbCursor.hasNext() && !finished) {
                    	buffer = "]".getBytes();
                    	position = 0;
                    	finished = true;
                    	return buffer[position++] & (0xff); // the buffer was read to the end and there is no rows - end of input stream
                    } else if(dbCursor.hasNext() && !finished){
                        // there is next row - converting it to byte array and re-filling buffer
                        if(isFirst)
                        	{
                        		buffer = readFirstObject();
                        		isFirst = false;
                        	}
                        else
                        	buffer = readNextObject();
                        position = 0;
                        return buffer[position++] & (0xff);
                    }
                    else{
                    	// finished
                    	return -1;
                    }
                }
            }
        
	}

}
