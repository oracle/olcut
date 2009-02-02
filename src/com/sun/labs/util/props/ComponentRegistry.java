package com.sun.labs.util.props;

import com.sun.jini.config.ConfigUtil;
import com.sun.jini.tool.ClassServer;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceMatches;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.DiscoveryEvent;
import net.jini.discovery.DiscoveryListener;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.export.Exporter;
import net.jini.jrmp.JrmpExporter;
import net.jini.lease.LeaseListener;
import net.jini.lease.LeaseRenewalEvent;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryEvent;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;

/**
 * A configurable container that can be used to register object proxies with a Jini
 * registry.
 */
public class ComponentRegistry implements Configurable, DiscoveryListener,
        ServiceDiscoveryListener, LeaseListener {

    /**
     * A property for the groups that we should use to do discovery of the 
     * Jini registar.
     */
    @ConfigStringList(defaultList = {})
    public static final String PROP_GROUP_LIST = "groupList";

    /**
     * A property for the default lease timeout for components that we want
     * to register.
     */
    @ConfigInteger(defaultValue = 100000)
    public static final String PROP_DEFAULT_LEASE_TIME = "defaultLeaseTime";

    private long defaultLeaseTime;

    /**
     * A property for the wait time to use when looking up services, in seconds.  The default
     * is 2 seconds.
     */
    @ConfigInteger(defaultValue = 2)
    public static final String PROP_LOOKUP_WAIT = "lookupWait";

    private long lookupWait;

    /**
     * A property for the number of times that we'll try a service lookup before
     * deciding nothings' there.
     */
    @ConfigInteger(defaultValue = 50)
    public static final String PROP_LOOKUP_TRIES = "lookupTries";

    private int lookupTries;

    /**
     * A property for the host where the Jini registry is running.  By default,
     * we'll use multicast to find the registry, which is indicated by the 
     * empty string.
     */
    @ConfigString(defaultValue = "")
    public static final String PROP_REGISTRY_HOST = "registryHost";

    /**
     * The host where the Jini registry is running.
     */
    private String registryHost;

    @ConfigInteger(defaultValue = 4160)
    public static final String PROP_REGISTRY_PORT = "registryPort";

    /**
     * The port on which the Jini registry is listening.
     */
    private int registryPort;

    /**
     * A property for the port that the class server should use.
     */
    @ConfigInteger(defaultValue = 1104)
    public static final String PROP_CS_PORT = "csPort";

    /**
     * A property for the directory that the class server should serve files
     * from.  This is a string composed of directories separated by the 
     * {@link File.pathSeparator} character.  The default value is the empty
     * string, which will result in no class server being started.
     */
    @ConfigString(defaultValue = "")
    public static final String PROP_CS_DIRS = "csDirs";

    /**
     * The current hostname.
     */
    @ConfigString(defaultValue = "")
    public static final String PROP_HOST_NAME = "hostName";

    /**
     * A property for an explicit codebase, which will be added to any dynamically
     * generated codebase.
     */
    @ConfigString(defaultValue = "")
    public static final String PROP_CODEBASE = "codebase";

    /**
     * A property for the jars that that class server will be serving, and
     * therefore the components of the codebase.
     */
    @ConfigStringList(defaultList = {})
    public static final String PROP_CODEBASE_JARS = "codebaseJars";

    /**
     * A configuration property for the name of the security policy file to use.
     * If this property is not specified, then the policy specified on the 
     * command line will be used.
     */
    @ConfigString(mandatory = false)
    public static final String PROP_SECURITY_POLICY = "securityPolicy";

    /**
     * An HTTP server for our classes.
     */
    private ClassServer classServer;

    /**
     * The configuration manager that created us.  We'll need this to get the 
     * instance names of components that want to register themselves.
     */
    private ConfigurationManager cm;

    /**
     * A service registry manager for our components.
     */
    private ServiceDiscoveryManager sdm;

    /**
     * A list of service registrations for the services that we have registered.
     */
    private List<ServiceRegistration> registrations;

    private Logger logger;

    /**
     * A map from classes to lookup caches for those classes.
     */
    private Map<Class, LookupCache> caches;

    /**
     * A map from the things that we've looked up to the property sheets for 
     * those objects.  We'll be using this to reconfigure things.
     */
    private Set<Component> lookedUp;
    
    /**
     * A map from the classes of things that we've looked up to the property
     * sheets for those looker-uppers.  We'll use this to add new items of this
     * type if they become available.
     */
    private Map<Class, Set<ComponentListener>> classListeners;

    /**
     * Exporters for the services that we've registered and for the things we
     * were asked to export if necessary.  We need to unexport these when we shut
     * down.
     */
    private Map<Remote, Exporter> exporters;
    
    /**
     * A map from components to exported versions of the components so that we
     * can avoid double exports, which is bad.
     */
    private Map<Remote,Remote> exported;

    /**
     * Gets a list of the services running in the registries that we're using
     * for lookups
     * @return A map from the string representation of the registries to a 
     * list of the string representation of the services running in the registry.
     * 
     */
    public Map<String,List<String>> dumpJiniServices() {
        ServiceRegistrar[] registrars =
                sdm.getDiscoveryManager().getRegistrars();
        Map<String,List<String>> ret = new HashMap<String, List<String>>();
        for(ServiceRegistrar r : registrars) {
            List<String> svcs = new ArrayList<String>();
            try {
                ServiceMatches sm = r.lookup(new ServiceTemplate(null, null,
                        null), Integer.MAX_VALUE);
                for(ServiceItem si : sm.items) {
                    svcs.add(si.toString());
                }
                ret.put(r.toString(), svcs);
            } catch(RemoteException rx) {
                logger.severe("Error geting services" + rx);
            }
        }
        return ret;
    }
    
    public Map<ServiceRegistrar,List<ServiceItem>> getJiniServices() {
        ServiceRegistrar[] registrars =
                sdm.getDiscoveryManager().getRegistrars();
        Map<ServiceRegistrar,List<ServiceItem>> ret =
                new HashMap<ServiceRegistrar, List<ServiceItem>>();
        for(ServiceRegistrar r : registrars) {
            List<ServiceItem> svcs = new ArrayList<ServiceItem>();
            try {
                ServiceMatches sm = r.lookup(new ServiceTemplate(null, null,
                        null), Integer.MAX_VALUE);
                for(ServiceItem si : sm.items) {
                    svcs.add(si);
                }
                ret.put(r, svcs);
            } catch(RemoteException rx) {
                logger.severe("Error geting services" + rx);
            }
        }
        return ret;
    }
    
    /**
     * Registers a proxy for the given component with the service registrar.
     * @param c the component to register
     * @param ps the property sheet associated with the component
     * @throws IllegalArgumentException if the given component does not implement
     * the {@link java.rmi.Remote} interface or if the component type has more
     * than one property sheet associated with it in the configuration.
     */
    public void register(Component c, PropertySheet ps) throws PropertyException {

        if(!(c instanceof Remote)) {
            throw new IllegalArgumentException("Component class " +
                    c.getClass() +
                    " does not implement java.rmi.Remote, unable to register");
        }

        //
        // See what lease time we should use.
        long leaseTime = ps.getLeaseTime();
        if(leaseTime <= 0) {
            leaseTime = defaultLeaseTime;
        }

        try {
            //
            // Make an exporter for the component, and get the proxy.  Make
            // sure to include any entries for the service.
            ServiceItem si =
                    new ServiceItem(null, getRemote((Remote) c),
                    ps.getEntries());

            //
            // If this is early on in the discovery process, we may not have found
            // any registrars yet, so we'll use the lookup timeouts to wait and 
            // see if any get discovered.
            ServiceRegistrar[] regs = null;
            for(int i = 0; i < lookupTries; i++) {
                regs = sdm.getDiscoveryManager().getRegistrars();
                if(regs.length > 0) {
                    break;
                }
                try {
                    Thread.sleep(lookupWait);
                } catch(InterruptedException ie) {
                    return;
                }
            }

            //
            // If nothing was discovered, then we're done.
            if(regs == null) {
                throw new PropertyException("registry", "serviceRegistrar",
                        "No registrars discovered");
            }

            //
            // Register the service in all of the registries that we found.
            for(int i = 0; i < regs.length; i++) {
                ServiceRegistration sr = regs[i].register(si, leaseTime);

                //
                // Register the lease for renewal.
                sdm.getLeaseRenewalManager().renewUntil(sr.getLease(),
                        Lease.FOREVER,
                        ps.getLeaseTime(),
                        this);
                registrations.add(sr);
            }
        } catch(RemoteException ex) {
            logger.log(Level.SEVERE, "Unable to register component", ex);
        }
    }

    /**
     * Gets a remote proxy suitable for passing to another object, if that is 
     * necessary.  Exporting the object is deemed necessary if the object to which 
     * we want to send the object is something that we looked up in the service
     * registrar.
     * 
     * @param r the object that we may want a proxy for
     * @param c the component to which we want to pass <code>r</code>
     * @return a proxy for the object, if one is required.
     */
    public Remote getRemote(Remote r, Component c) {

        //
        // If we haven't looked up this object, then just return the thing
        // we were passed.
        if(!lookedUp.contains(c)) {
            return r;
        }

        //
        // We did lookup the component, so we need to export the remote we
        // were passed.
        return getRemote(r);
    }

    /**
     * Exports the provided object so that a proxy may be passed to a remote
     * object.
     * 
     * @param r The object to export
     * @return The exported proxy.
     */
    public Remote getRemote(Remote r) {
        Remote rem = null;
        try {
            
            //
            // If we've already exported this component, then use the
            // exported version.
            rem = exported.get(r);
            if(rem != null) {
                return rem;
            }
            Exporter ex = new JrmpExporter();
            rem = ex.export(r);
            exporters.put(rem, ex);
            exported.put(r, rem);
        } catch(ExportException ex) {
            logger.log(Level.SEVERE, "Error exporting " + r, ex);
        }
        return rem;

    }

    /**
     * Unexports the provided remote interface, if we exported it.
     * @param r the interface to unexport, if necessary.
     */
    public void unexport(Remote r) {
        Exporter ex = exporters.remove(r);
        if(ex != null) {
            ex.unexport(true);
        }
    }
    
    private void addListener(Class c, ComponentListener cl) {
        if(cl == null) {
            return;
        }
        Set<ComponentListener> s = classListeners.get(c);
        if(s == null) {
            s = new HashSet<ComponentListener>();
            classListeners.put(c, s);
        }
        s.add(cl);
    }
    
    /**
     * Gets a number of components of the given type from the registrar.
     * @param c the class of the component to lookup
     * @param maxMatches the maximum number of matching components to
     * return
     * @param cl a listener for the component that is looked up, so that the caller
     * can be notified of service changes.
     * @return an array containing the matching components
     */
    public Component[] lookup(Class c, int maxMatches, ComponentListener cl) {
        if(sdm == null) {
            return new Component[0];
        }

        //
        // See if we have a lookup cache for this type.  If not, make one and 
        // add it to the map.
        LookupCache cache = caches.get(c);
        ServiceTemplate template = null;
        if(cache == null) {
            try {
                //
                // Note that we're matching against the class specified in the 
                // property sheet, which should be the remote interface.  We'll
                // match using whatever entries we were given.  Note that we're 
                // explicitly not matching against the instance name.
                template = new ServiceTemplate(null,
                        new Class[]{c}, null);
                cache = sdm.createLookupCache(template, null, this);
                caches.put(c, cache);
            } catch(RemoteException ex) {
                logger.log(Level.SEVERE, "Error creating lookup cache for " + c, ex);
            }
        }

        //
        // OK, here we go.  We're going to sleep wait while we do a few lookups.
        // There's no blocking calls to lookup in the cache, but we need the 
        // cache to get the service events.  Jan Newmarch's Jini guide says that
        // this is the way to do it, and I guess I believe him...
        ServiceItem[] sis = null;
        for(int i = 0; i < lookupTries; i++) {

            sis = cache.lookup(null, maxMatches);

            //
            // When we get one, we're done.
            if(sis != null && sis.length > 0) {
                break;
            }

            //
            // Snooze for a while.
            try {
                Thread.sleep(lookupWait);
            } catch(InterruptedException ie) {
                break;
            }
        }

        if(sis == null || sis.length == 0) {
            return new Component[0];
        }
     
        addListener(c, cl);
        Component[] ret = new Component[sis.length];
        for(int i = 0; i < sis.length; i++) {
            ret[i] = (Component) sis[i].service;
            lookedUp.add(ret[i]);
        }
        return ret;
    }

    /**
     * Looks up a component in the lookup service.
     * @param cps the propert sheet for the component to look up.
     * @param cl a listener for the component to be looked up so that it may
     * be notified of changes to things of the given type.  May be <code>null</code>
     * @return the named component, or <code>null</code> if no such component
     * can be found.
     */
    public Component lookup(PropertySheet cps, ComponentListener cl) {

        if(sdm == null) {
            return null;
        }

        //
        // See if we have a lookup cache for this type.  If not, make one and 
        // add it to the map.
        Class c = cps.getOwnerClass();
        LookupCache cache = caches.get(c);
        ServiceTemplate template = null;
        if(cache == null) {
            try {
                //
                // Note that we're matching against the class specified in the 
                // property sheet, which should be the remote interface.  We'll
                // match using whatever entries we were given.  Note that we're 
                // explicitly not matching against the instance name.
                template = new ServiceTemplate(null,
                        new Class[]{c}, cps.getEntries());
                cache = sdm.createLookupCache(template, null, this);
                caches.put(c, cache);
            } catch(RemoteException ex) {
                logger.log(Level.SEVERE, "Error creating lookup cache for " + c, ex);
            }
        }

        //
        // OK, here we go.  We're going to sleep wait while we do a few lookups.
        // There's no blocking calls to lookup in the cache, but we need the 
        // cache to get the service events.  Jan Newmarch's Jini guide says that
        // this is the way to do it, and I guess I believe him...
        ServiceItem si = null;
        for(int i = 0; i < lookupTries; i++) {

            si = cache.lookup(null);

            //
            // When we get one, we're done.
            if(si != null) {
                break;
            }

            //
            // Snooze for a while.
            try {
                Thread.sleep(lookupWait);
            } catch(InterruptedException ie) {
                break;
            }
        }

        if(si == null) {
            return null;
        }

        //
        // Remember that we looked up the component and add any listener.
        Component component = (Component) si.service;
        lookedUp.add(component);
        addListener(c, cl);
        return component;
    }

    protected boolean wasLookedUp(Component c) {
        return lookedUp.contains(c);
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        cm = ps.getConfigurationManager();
        defaultLeaseTime = ps.getInt(PROP_DEFAULT_LEASE_TIME);
        registryHost = ps.getString(PROP_REGISTRY_HOST);
        registryPort = ps.getInt(PROP_REGISTRY_PORT);
        lookupWait = ps.getInt(PROP_LOOKUP_WAIT) * 1000;
        lookupTries = ps.getInt(PROP_LOOKUP_TRIES);
        logger = ps.getLogger();

        //
        // Get the groups that we want to discover.
        String[] groups =
                ps.getStringList(PROP_GROUP_LIST).toArray(new String[0]);
        if(groups.length == 0) {
            groups = LookupDiscovery.ALL_GROUPS;
        }

        //
        // The objects that we've looked up and exported.
        lookedUp = new HashSet<Component>();
        classListeners = new HashMap<Class, Set<ComponentListener>>();
        exporters = new HashMap<Remote, Exporter>();
        exported = new HashMap<Remote, Remote>();
        caches = new HashMap<Class, LookupCache>();
        registrations = new ArrayList<ServiceRegistration>();

        String securityPolicy = ps.getString(PROP_SECURITY_POLICY);

        //
        // If there's no security manager, but there's a security policy provided,
        // then make sure we have a security manager that's going to follow the
        // policy!
        if(System.getSecurityManager() == null && securityPolicy != null) {

            //
            // If we got a security policy, then install it.
            if(securityPolicy != null) {
                if(!(new File(securityPolicy)).exists()) {
                    throw new PropertyException(ps.getInstanceName(),
                            PROP_SECURITY_POLICY, "Security policy " +
                            securityPolicy + " does not exist");
                }
                logger.info("Adding security manager for policy " + securityPolicy);
                System.setProperty("java.security.policy", securityPolicy);
                System.setSecurityManager(new RMISecurityManager());
            }
        }

        //
        // Get the local host name, if there was not one provided.
        String hostName = ps.getString(PROP_HOST_NAME);
        if(hostName.equals("")) {
            try {
                hostName = ConfigUtil.getHostName();
            } catch(UnknownHostException ex) {
                throw new PropertyException(ex, ps.getInstanceName(),
                        PROP_HOST_NAME,
                        "Unknown host setting hostName");
            }
        }

        //
        // Construct a codebase.
        int csPort = ps.getInt(PROP_CS_PORT);
        List<String> cbjars = ps.getStringList(PROP_CODEBASE_JARS);
        String explicitCB = ps.getString(PROP_CODEBASE);
        if(cbjars.size() > 0 || explicitCB != null) {
            StringBuilder sb = new StringBuilder();
            for(String jar : cbjars) {
                sb.append(String.format("http://%s:%d/%s ", hostName, csPort,
                        jar));
            }

            //
            // Add any explicit codebase settings.      
            sb.append(explicitCB);
            System.setProperty("java.rmi.server.codebase", sb.toString());
        }

        //
        // We may want to start a class server for the things registered by
        // this component registry. We'll do this if the directories to server
        // are not empty.
        String csDirs = ps.getString(PROP_CS_DIRS);
        if(!csDirs.equals("")) {
            try {
                classServer = new ClassServer(csPort, csDirs, true, true);
                classServer.start();
            } catch(java.io.IOException ioe) {
                throw new PropertyException(ioe, ps.getInstanceName(),
                        PROP_CS_PORT,
                        "Unable to start class server");
            }
        }

        //
        // Get our service discovery manager.
        try {

            //
            // If a registry host was specified, make sure that we're using that
            // registry!
            LookupLocator[] lookups = null;
            if(registryHost != null && !registryHost.equals("")) {
                LookupLocator lookup =
                        new LookupLocator(registryHost, registryPort);
                lookups = new LookupLocator[]{lookup};
            }

            //
            // Get the service discovery manager and add ourself as a listener
            // for discovery events.
            sdm = new ServiceDiscoveryManager(new LookupDiscoveryManager(groups,
                    lookups,
                    null),
                    new LeaseRenewalManager());
            sdm.getDiscoveryManager().addDiscoveryListener(this);
        } catch(IOException ioe) {
            throw new PropertyException(ioe, ps.getInstanceName(),
                    "serviceDiscoveryManager",
                    "Unable to create service discovery manager");
        }
    }

    /**
     * Unregisters the services.
     */
    public void shutdown() {

        logger.info("Shutting down component registry");
        for(ServiceRegistration sr : registrations) {
            try {
                sr.getLease().cancel();
            } catch(UnknownLeaseException ex) {
                logger.warning("Unknown lease when cancelling");
            } catch(RemoteException ex) {
                logger.warning("Error cancelling lease");
            }

        }

        //
        // Unexport the services we registered and the things that we 
        // were asked to export.
        for(Exporter ex : exporters.values()) {
            ex.unexport(true);
        }

        //
        // Terminate our lookup caches, service discovery manager and class server.
        for(LookupCache lc : caches.values()) {
            lc.terminate();
        }

        if(sdm != null) {
            sdm.terminate();
        }

        if(classServer != null) {
            classServer.terminate();
            classServer = null;
        }

    }

    public void notify(LeaseRenewalEvent e) {
        logger.info("Lease renewed: " + e);
    }

    public void serviceAdded(ServiceDiscoveryEvent e) {
        ServiceItem si = e.getPostEventServiceItem();
        Class c = si.service.getClass();
        Component component = (Component) si.service;
        lookedUp.add(component);
        
        //
        // We'll need to look for listeners on all of the interfaces that this
        // class implements.
        for(Class iface : c.getInterfaces()) {
            Set<ComponentListener> listeners = classListeners.get(iface);
            if(listeners == null) {
                continue;
            }
            for(ComponentListener l : listeners) {
                l.componentAdded(component);
            }
        }
    }

    public void serviceRemoved(ServiceDiscoveryEvent e) {

        //
        // Figure out what property sheet is associated with the service that
        // was removed and call its newProperties method, forcing it to get a
        // new remote component.
        ServiceItem si = e.getPreEventServiceItem();
        Component component = (Component) si.service;
        lookedUp.remove(component);
        Class c = component.getClass();
        for(Class iface : c.getInterfaces()) {
            Set<ComponentListener> listeners = classListeners.get(iface);
            if(listeners == null) {
                continue;
            }
            for(ComponentListener l : listeners) {
                l.componentRemoved(component);
            }
        }
    }

    public void serviceChanged(ServiceDiscoveryEvent e) {
        //
        // Changing is removing then adding a component as far as the listeners
        // are concerned.
        ServiceItem pre = e.getPreEventServiceItem();
        ServiceItem post = e.getPostEventServiceItem();
        Class c = pre.service.getClass();
        lookedUp.remove((Component) pre.service);
        lookedUp.add((Component) post.service);
        for(Class iface : c.getInterfaces()) {
            Set<ComponentListener> listeners = classListeners.get(iface);
            if(listeners == null) {
                continue;
            }
            for(ComponentListener l : listeners) {
                l.componentRemoved((Component) pre.service);
                l.componentAdded((Component) post.service);
            }
        }
    }

    public void discovered(DiscoveryEvent e) {
    }

    public void discarded(DiscoveryEvent e) {
    }
}
