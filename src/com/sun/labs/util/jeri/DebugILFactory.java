package com.sun.labs.util.jeri;

import java.lang.reflect.InvocationHandler;
import java.rmi.Remote;
import java.rmi.server.ExportException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import net.jini.core.constraint.MethodConstraints;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.InvocationDispatcher;
import net.jini.jeri.ObjectEndpoint;
import net.jini.jeri.ServerCapabilities;

/**
 *
 * @author stgreen
 */
public class DebugILFactory extends BasicILFactory {
    
    private static final Logger logger = Logger.getLogger(DebugILFactory.class.getName());
    
    private MethodConstraints serverConstraints = null;
    private Class permissionClass = null;
    
    private Map<String,Integer> reportMap;


    public DebugILFactory() {
        super();
    }

    public DebugILFactory(MethodConstraints serverConstraints, Class permissionClass) {
        super(serverConstraints, permissionClass);
        this.serverConstraints = serverConstraints;
        this.permissionClass = permissionClass;
    }

    public DebugILFactory(MethodConstraints serverConstraints, Class permissionClass, ClassLoader loader) {
        super(serverConstraints, permissionClass, loader);
        this.serverConstraints = serverConstraints;
        this.permissionClass = permissionClass;
    }

    public void setReportMap(Map<String, Integer> reportMap) {
        this.reportMap = reportMap;
    }

    @Override
    protected InvocationDispatcher createInvocationDispatcher(Collection methods, Remote impl, ServerCapabilities caps) throws ExportException {
        if(impl == null) {
            throw new NullPointerException("impl is null");
        }
        return new DebugInvocationDispatcher(methods, caps,
                serverConstraints,
                permissionClass,
                getClassLoader(), reportMap);
    }

    @Override
    protected InvocationHandler createInvocationHandler(Class[] interfaces, Remote impl, ObjectEndpoint oe) throws ExportException {
        for(Class iface : interfaces) {
            if(iface == null) {
                throw new NullPointerException();
            }
        }
        if(impl == null) {
            throw new NullPointerException();
        }
        return new DebugInvocationHandler(oe, serverConstraints, reportMap);
    }
}
