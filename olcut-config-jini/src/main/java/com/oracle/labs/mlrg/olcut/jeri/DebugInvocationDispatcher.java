package com.oracle.labs.mlrg.olcut.jeri;

import com.oracle.labs.mlrg.olcut.util.NanoWatch;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.server.ExportException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import net.jini.core.constraint.MethodConstraints;
import net.jini.jeri.BasicInvocationDispatcher;
import net.jini.jeri.ServerCapabilities;

/**
 * An invocation dispatcher that will keep track of debugging information.
 */
public class DebugInvocationDispatcher extends BasicInvocationDispatcher {

    Map<String, Integer> reportMap;

    Map<String, NanoWatch> reportW;
    
    Map<String,Long> networkTime;
    
    Map<String,Long> marshalTime;

    public static final Logger logger = Logger.getLogger(DebugInvocationDispatcher.class.getName());

    public DebugInvocationDispatcher(Collection methods,
            ServerCapabilities serverCapabilities,
            MethodConstraints serverConstraints,
            Class permissionClass,
            ClassLoader loader, Map<String, Integer> reportMap) throws ExportException {
        super(methods, serverCapabilities, serverConstraints, permissionClass, loader);
        this.reportMap = reportMap != null ? reportMap : new HashMap<String, Integer>();
        reportW = new HashMap<String, NanoWatch>();
        networkTime = new HashMap<String, Long>();
        marshalTime = new HashMap<String, Long>();
        for(String method : this.reportMap.keySet()) {
            reportW.put(method, new NanoWatch());
            networkTime.put(method, 0L);
            marshalTime.put(method, 0L);
        }
    }
    
    @Override
    protected Object[] unmarshalArguments(Remote impl, Method method, ObjectInputStream in, Collection context) throws IOException, ClassNotFoundException {
        long arrival = System.currentTimeMillis();
        String mname = method.getName();
        NanoWatch uw = reportW.get(mname);
        if(uw != null) {
            uw.start();
        }
        long startMarshal = in.readLong();
        Object[] ret = super.unmarshalArguments(impl, method, in, context);
        long endMarshal = in.readLong();
        if(uw != null) {
            long done = System.currentTimeMillis();
            logger.info(String.format("read sm: %d (%d) em: %d (%d) mt: %d", startMarshal, 
                    arrival - startMarshal, 
                    endMarshal, 
                    endMarshal - startMarshal, 
                    done - arrival));
            uw.stop();
            networkTime.put(mname, networkTime.get(mname) + (endMarshal - arrival));
            marshalTime.put(mname, marshalTime.get(mname) + (endMarshal - startMarshal));
            Integer reportInterval = reportMap.get(method.getName());
            if(uw.getClicks() % reportInterval == 0) {
                logger.info(String.format("%s %d calls unmarshall %.2fms/call (%.2f ms total) mt: %.2f ms/call nt: %.2f ms/call",
                        method.getName(),
                        uw.getClicks(),
                        uw.getAvgTimeMillis(), uw.getTimeMillis(), 
                        marshalTime.get(mname) / (double) uw.getClicks(), 
                        networkTime.get(mname) / (double) uw.getClicks()));
            }
        }
        return ret;
    }

}
