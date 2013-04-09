package edu.emory.cci.bindaas.core.util;

import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.util.StopWatch;

import edu.emory.cci.bindaas.core.bundle.Activator;
/**
 * If profiling is enabled this service profiles the application for N requests.
 * @author nadir
 *
 */
public class ProfilerService {

	
	private static Integer MAX = 100;
	private LinkedBlockingDeque<StopWatch> queue;
	private ThreadLocal<StopWatch> localStopWatch;
	
	public void init()
	{
		queue = new LinkedBlockingDeque<StopWatch>(MAX);
		localStopWatch = new ThreadLocal<StopWatch>();
		Activator.getContext().registerService(ProfilerService.class.getName(), this, null);
	}
	
	public boolean isEnabled()
	{
		if(System.getProperties().containsKey("profile"))
			return true;
		else
			return false;
	}
	
	public StopWatch createStopWatch(String requestID)
	{
		StopWatch stopWatch = new StopWatch(requestID);
		synchronized (queue) {
			if(!queue.offer(stopWatch))
			{
				queue.remove();
				queue.add(stopWatch);
			}
			
			localStopWatch.set(stopWatch);
		}
		
		return stopWatch;
	}
	
	public StopWatch getThreadLocalStopWatch()
	{
		return localStopWatch.get();
	}
	
	public String printSummary(){
		StringBuffer buffer = new StringBuffer();
		synchronized (queue) {
			for(StopWatch sw : queue)
			{
				buffer.append(sw.prettyPrint()).append("\n");
			}
		}
		return buffer.toString();
	}
}
