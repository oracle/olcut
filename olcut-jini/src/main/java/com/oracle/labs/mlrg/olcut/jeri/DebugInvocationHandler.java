package com.oracle.labs.mlrg.olcut.jeri;

import com.oracle.labs.mlrg.olcut.util.NanoWatch;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import net.jini.core.constraint.MethodConstraints;
import net.jini.jeri.BasicInvocationHandler;
import net.jini.jeri.ObjectEndpoint;

/**
 *
 */
public class DebugInvocationHandler extends BasicInvocationHandler {

    private static final Logger logger = Logger.getLogger(DebugInvocationHandler.class.getName());

    Map<String, Integer> reportMap;

    Map<String, NanoWatch> reportW;

    public DebugInvocationHandler(ObjectEndpoint oe, MethodConstraints serverConstraints, Map<String, Integer> reportMap) {
        super(oe, serverConstraints);
        this.reportMap = reportMap != null ? reportMap : new HashMap<String, Integer>();
        reportW = new HashMap<String, NanoWatch>();
        for(String method : this.reportMap.keySet()) {
            reportW.put(method, new NanoWatch());
        }
    }

    public DebugInvocationHandler(BasicInvocationHandler other, MethodConstraints clientConstraints, Map<String, Integer> reportMap) {
        super(other, clientConstraints);
        this.reportMap = reportMap != null ? reportMap : new HashMap<String, Integer>();
    }

    @Override
    protected void marshalArguments(Object proxy, Method method, Object[] args, ObjectOutputStream out, Collection context) throws IOException {
        NanoWatch mw = reportW.get(method.getName());
        if(mw != null) {
            mw.start();
        }
        //
        // Write the time we started marshalling.
        long t1 = System.currentTimeMillis();
        out.writeLong(t1);
        super.marshalArguments(proxy, method, args, out, context);
        long t2 = System.currentTimeMillis();
        
        //
        // Write the time we finished.
        out.writeLong(t2);
        if(mw != null) {
            mw.stop();
            Integer reportInterval = reportMap.get(method.getName());
            if(mw.getClicks() % reportInterval == 0) {
                logger.info(String.format("%s %d calls marshal %.2fms/call (%.2f ms total)",
                        method.getName(),
                        mw.getClicks(),
                        mw.getAvgTimeMillis(), mw.getTimeMillis()));
            }
        }
    }

    @Override
    protected void marshalMethod(Object proxy, Method method, ObjectOutputStream out, Collection context) throws IOException {
        super.marshalMethod(proxy, method, out, context);
    }
}
