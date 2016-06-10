package edu.emory.cci.bindaas.core.system;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.core.system.event.CriticalDiskSpaceEvent;
import edu.emory.cci.bindaas.core.system.event.CriticalMemoryEvent;
import edu.emory.cci.bindaas.core.system.event.DeadlockDetectedEvent;
import edu.emory.cci.bindaas.framework.event.BindaasEventConstants;

public class SystemHealthCheckService implements Runnable {

	private Log log = LogFactory.getLog(getClass());
	private Long pollInterval ;
	private Double diskspaceThreshold;
	private Double memoryThreshold;
	
	
	
	public Long getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(Long pollInterval) {
		this.pollInterval = pollInterval;
	}

	public Double getDiskspaceThreshold() {
		return diskspaceThreshold;
	}

	public void setDiskspaceThreshold(Double diskspaceThreshold) {
		this.diskspaceThreshold = diskspaceThreshold;
	}

	public Double getMemoryThreshold() {
		return memoryThreshold;
	}

	public void setMemoryThreshold(Double memoryThreshold) {
		this.memoryThreshold = memoryThreshold;
	}

	public void init() throws Exception
	{
		CriticalMemoryEvent.addTopic(BindaasEventConstants.CRITICAL_MEMORY);
		CriticalDiskSpaceEvent.addTopic(BindaasEventConstants.CRITICAL_DISK_SPACE);
		DeadlockDetectedEvent.addTopic(BindaasEventConstants.DEADLOCK_DETECTED);
		
		Thread t = new Thread(this, "SystemHealthCheckService Thread");
		t.start();
	}
	
	@Override
	public void run() {
		
		while(true){
			checkMemory();
			checkDiskSpace();
			checkDeadlock();
			
			try {
				Thread.sleep(pollInterval);
			} catch (InterruptedException e) {
				log.error("SystemHealthCheckService Thread interrupted" , e);
				break;
			}
		}
	}
	
	private void checkMemory()
	{
		long totalMemory = Runtime.getRuntime().totalMemory(); 
		long usedMemory = totalMemory - Runtime.getRuntime().freeMemory();
		Double utilization = ((double)usedMemory/(double)totalMemory) * 100;
		

		if(utilization > memoryThreshold)
		{
			CriticalMemoryEvent.EventData eventData = new CriticalMemoryEvent.EventData();
			eventData.setFreeMemory(Runtime.getRuntime().freeMemory() + "");
			eventData.setTotalMemory(totalMemory + "");
			eventData.setUsedMemoryPercentage(utilization + "%");
			
			CriticalMemoryEvent event = new CriticalMemoryEvent();
			event.setData(eventData);

			event.emitSynchronously();
		}
		
	}
	
	private void checkDiskSpace()
	{
	    File[] roots = File.listRoots();


	    for (File root : roots) {
	      
	      String rootPath = root.getAbsolutePath();
	      long totalSpace = root.getTotalSpace();
	      long freeSpace = root.getFreeSpace();
	      long usageSpace = root.getUsableSpace();
	      
	      Double utilization = ((double)(totalSpace - freeSpace)/(double)totalSpace) * 100;

	      if(utilization > diskspaceThreshold)
			{
				CriticalDiskSpaceEvent.EventData eventData = new CriticalDiskSpaceEvent.EventData();
				eventData.setFreeSpace(freeSpace + "");
				eventData.setRootPath(rootPath);
				eventData.setTotalSpace(totalSpace + "");
				eventData.setUsagebleSpace(usageSpace + "");
				eventData.setUtilization(utilization + "%");
				
				CriticalDiskSpaceEvent event = new CriticalDiskSpaceEvent();
				event.setData(eventData);

				event.emitSynchronously();
			}
	      
	    }
		
		
	}
	
	private void checkDeadlock()
	{
		ThreadMXBean threadMxBean =   ManagementFactory.getThreadMXBean();
		long[] threadIds = threadMxBean.findDeadlockedThreads();
		
		if (threadIds != null) {
		    ThreadInfo[] infos = threadMxBean.getThreadInfo(threadIds);

		    for (ThreadInfo info : infos) {
		        StackTraceElement[] stack = info.getStackTrace();
		        DeadlockDetectedEvent.EventData eventData = new DeadlockDetectedEvent.EventData();
		        eventData.setStackTraces(stack);
		        
		        DeadlockDetectedEvent event = new DeadlockDetectedEvent();
		        event.setData(eventData);
		        

		        event.emitSynchronously();
		        
		    }
		}
	}

}
