package com.sun.labs.util.props.dist;

import com.sun.jini.config.ConfigUtil;
import com.sun.jini.tool.ClassServer;
import com.sun.labs.util.LabsLogFormatter;
import com.sun.labs.util.jeri.DebugILFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.labs.util.props.ComponentListener;
import com.sun.labs.util.props.Config;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurableName;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
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
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.InvocationLayerFactory;
import net.jini.jeri.tcp.TcpServerEndpoint;
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
    private static final Logger logger = Logger.getLogger(ComponentRegistry.class.getName());

    @ConfigurableName
    private String name;

    /**
     * The groups that we should use to do discovery of the Jini registrar.
     */
    @Config
    private String[] groupList;

    /**
     * A property for the default lease timeout for components that we want
     * to register.
     */
    @Config
    private long defaultLeaseTime = 100000;

    /**
     * A property for the wait time to use when looking up services, in seconds.  The default
     * is 2 seconds.
     */
    @Config
    private long lookupWait = 2;

    /**
     * A property for the number of times that we'll try a service lookup before
     * deciding nothings' there.
     */
    @Config
    private int lookupTries = 50;

    /**
     * The host where the Jini registry is running.  By default,
     * we'll use multicast to find the registry, which is indicated by the 
     * empty string.
     */
    @Config
    private String registryHost = "";

    /**
     * The port on which the Jini registry is listening.
     */
    @Config
    private int registryPort = 4160;

    /**
     * The port that the class server should use.
     */
    @Config
    private int csPort = 1104;

    /**
     * The directory that the class server should serve files
     * from.  This is a string composed of directories separated by the 
     * {@link File#pathSeparator} character.  The default value is the empty
     * string, which will result in no class server being started.
     */
    @Config
    private String csDirs = "";

    /**
     * The current hostname.
     */
    @Config
    private String hostName = "";

    /**
     * A property for an explicit codebase, which will be added to any dynamically
     * generated codebase.
     */
    @Config
    private String codebase = "";

    /**
     * A property for the jars that that class server will be serving, and
     * therefore the components of the codebase.
     */
    @Config(genericType=String.class)
    private List<String> codebaseJars = new ArrayList<>();
    
    /**
     * A property for some extra paths to be served via the class server.
     */
    @Config(genericType=String.class)
    private List<String> codebasePaths = new ArrayList<>();

    /**
     * A configuration property for the name of the security policy file to use.
     * If this property is not specified, then the policy specified on the 
     * command line will be used.
     */
    @Config
    private String securityPolicy = null;
    
    @Config
    boolean debugRMI;
    
    @Config
    public String jiniConfigFile = null;
    private Configuration jiniConfig;

    @Config(genericType=Integer.class)
    private Map<String,Integer> debugReportMap = new HashMap<>();
    
    /**
     * An HTTP server for our classes.
     */
    private ClassServer classServer;

    /* TODO: check if this field is necessary. It must be accessed via reflection if so.
     * The configuration manager that created us.  We'll need this to get the 
     * instance names of components that want to register themselves.
     * private ConfigurationManager cm;
     */

    /**
     * A service registry manager for our components.
     */
    private ServiceDiscoveryManager sdm;

    /**
     * A list of service registrations for the services that we have registered.
     */
    private List<ServiceRegistration> registrations = new ArrayList<>();


    /**
     * A map from classes to lookup caches for those classes.
     */
    private Map<Class, LookupCache> caches = new HashMap<>();

    /**
     * A map from the things that we've looked up to the property sheets for 
     * those objects.  We'll be using this to reconfigure things.
     */
    private Set<Configurable> lookedUp = new HashSet<>();
    
    /**
     * A map from the classes of things that we've looked up to the property
     * sheets for those looker-uppers.  We'll use this to add new items of this
     * type if they become available.
     */
    private Map<Class, Set<ComponentListener>> classListeners = new HashMap<>();

    /**
     * Exporters for the services that we've registered and for the things we
     * were asked to export if necessary.  We need to unexport these when we shut
     * down.
     */
    private Map<Remote, Exporter> exporters = new HashMap<>();
    
    /**
     * A map from components to exported versions of the components so that we
     * can avoid double exports, which is bad.
     */
    private Map<Remote,Remote> exported = new HashMap<>();

    @Override
    public void postConfig() throws PropertyException {
        if(jiniConfigFile != null) {
            try {
                jiniConfig = ConfigurationProvider.getInstance(new String[] {jiniConfigFile});
            } catch(ConfigurationException ex) {
                throw new PropertyException(ex, name, "jiniConfigFile", "Error reading jini config");
            }
        }

        //
        // Get the groups that we want to discover.
        if(groupList.length == 0) {
            groupList = LookupDiscovery.ALL_GROUPS;
        }

        //
        // If there's no security manager, but there's a security policy provided,
        // then make sure we have a security manager that's going to follow the
        // policy!
        if(System.getSecurityManager() == null && securityPolicy != null) {
            if(!(new File(securityPolicy)).exists()) {
                throw new PropertyException(name, "securityPolicy",
                                "Security policy " + securityPolicy
                                + " does not exist");
            }
            System.setProperty("java.security.policy", securityPolicy);
            System.setSecurityManager(new SecurityManager());
        }

        //
        // Get the local host name, if there was not one provided.
        if(hostName.equals("")) {
            try {
                hostName = ConfigUtil.getHostName();
            } catch(UnknownHostException ex) {
                throw new PropertyException(ex, name, "hostName",
                        "Unknown host setting hostName");
            }
        }

        //
        // Construct a codebase.
        if(!codebaseJars.isEmpty() || !codebasePaths.isEmpty() || codebase != null) {
            StringBuilder sb = new StringBuilder();

            //
            // Add URIs for the jar files specified in the properties.
            for(String jar : codebaseJars) {
                sb.append(String.format("http://%s:%d/%s ", hostName, csPort,
                        jar));
            }

            //
            // Add URIs for the paths specified in the properties.
            for(String path : codebasePaths) {
                sb.append(String.format("http://%s:%d/%s ", hostName, csPort,
                        path));
            }

            //
            // Add any explicit codebase settings.
            if(codebase != null) {
                sb.append(codebase);
            }

            //
            // Set the codebase for RMI.
            System.setProperty("java.rmi.server.codebase", sb.toString());
        } else {
            //
            // No explicit codebase?  Give a URI for the top of the class
            // server.
            System.setProperty("java.rmi.server.codebase",
                    String.format("http://%s:%d/", hostName, csPort));
        }

        //
        // We may want to start a class server for the things registered by
        // this component registry. We'll do this if the directories to serve
        // are not empty.
        if(!csDirs.equals("")) {
            try {
                classServer = new ClassServer(csPort, csDirs, true, true);
                classServer.start();
            } catch(java.io.IOException ioe) {
                throw new PropertyException(ioe, name, "csPort",
                        "Unable to start class server");
            }
        }

        //
        // If we were given a registry host, but it looks like a URL, then
        // read a set of properties from the URL and get the registryHost
        // property to use for the name.  This indirection allows us to
        // get a registry host when we're deploying Web apps some place like
        // EC2 that doesn't do multicast and doesn't provide for custom
        // name resolution.
        if(registryHost != null && !registryHost.equals("")) {
            String hostString = registryHost;
            try {
                URL u = new URL(registryHost);
                Properties props = new Properties();
                props.load(u.openStream());
                registryHost = props.getProperty("registryHost");
                if(registryHost == null) {
                    throw new PropertyException(name, "registryHost",
                            "Properties at URL " + hostString +
                                    " do not include registryHost property");
                } else {
                    logger.info("Got registry host: " + registryHost + " from properties");
                }
            } catch(MalformedURLException ex) {
                //
                // This is OK, it might not actually be a URL!
            } catch(IOException ioe) {
                throw new PropertyException(name, "registryHost",
                        "Unable to read properties at URL " + hostString);
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
                logger.finer(String.format("Using lookup: %s", lookup));
            }

            //
            // Get the service discovery manager and add ourself as a listener
            // for discovery events.
            sdm = new ServiceDiscoveryManager(new LookupDiscoveryManager(groupList,
                    lookups,
                    null),
                    new LeaseRenewalManager());
            sdm.getDiscoveryManager().addDiscoveryListener(this);
            logger.finer("Got SDM " + sdm);
        } catch(IOException ioe) {
            throw new PropertyException(ioe, name,
                    "serviceDiscoveryManager",
                    "Unable to create service discovery manager");
        }
    }

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
                logger.severe("Error getting services" + rx);
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
    public void register(Configurable c, ServablePropertySheet ps) throws PropertyException {

        if(!(c instanceof Remote)) {
            throw new IllegalArgumentException("Component class " +
                    c.getClass() +
                    " does not implement java.rmi.Remote, unable to register");
        }

        logger.finer(String.format("Registering %s", ps.getInstanceName()));
        
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
                logger.finest(String.format("Lookup try %d", i));
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
            } else {
                logger.fine(String.format("Got %d registries", regs.length));
            }

            //
            // Register the service in all of the registries that we found.
            for(int i = 0; i < regs.length; i++) {
                ServiceRegistration sr = regs[i].register(si, leaseTime);

                if(logger.isLoggable(Level.FINER)) {
                    logger.finer(String.format("Registering %s with %s", si, regs[i]));
                }

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
    public Remote getRemote(Remote r, Configurable c) {

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
            
            InvocationLayerFactory ilf;
            if(debugRMI) {
                ilf = new DebugILFactory();
                ((DebugILFactory) ilf).setReportMap(debugReportMap);
            } else {
                ilf = new BasicILFactory();
            }
            Exporter ex = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), ilf);
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
    public Configurable[] lookup(Class c, int maxMatches, ComponentListener cl) {
        if(sdm == null) {
            return new Configurable[0];
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
            return new Configurable[0];
        }
     
        addListener(c, cl);
        Configurable[] ret = new Configurable[sis.length];
        for(int i = 0; i < sis.length; i++) {
            ret[i] = (Configurable) sis[i].service;
            lookedUp.add(ret[i]);
        }
        return ret;
    }

    /**
     * Looks up a component in the lookup service.
     * @param cps the property sheet for the component to look up.
     * @param cl a listener for the component to be looked up so that it may
     * be notified of changes to things of the given type.  May be <code>null</code>
     * @return the named component, or <code>null</code> if no such component
     * can be found.
     */
    public Configurable lookup(ServablePropertySheet cps, ComponentListener cl) {

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
                return null;
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

        if(logger.isLoggable(Level.FINER)) {
            logger.finer(String.format("Got component: %s", si.service));
        }

        //
        // Remember that we looked up the component and add any listener.
        Configurable component = (Configurable) si.service;
        lookedUp.add(component);
        addListener(c, cl);
        return component;
    }

    protected boolean wasLookedUp(Configurable c) {
        return lookedUp.contains(c);
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
        Configurable component = (Configurable) si.service;
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
        Configurable component = (Configurable) si.service;
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
        lookedUp.remove((Configurable) pre.service);
        lookedUp.add((Configurable) post.service);
        for(Class iface : c.getInterfaces()) {
            Set<ComponentListener> listeners = classListeners.get(iface);
            if(listeners == null) {
                continue;
            }
            for(ComponentListener l : listeners) {
                l.componentRemoved((Configurable) pre.service);
                l.componentAdded((Configurable) post.service);
            }
        }
    }

    public void discovered(DiscoveryEvent e) {
        if(logger.isLoggable(Level.FINER)) {
            logger.finer(String.format("Discovered %d registrars", e.getRegistrars().length));
            for(ServiceRegistrar sr : e.getRegistrars()) {
                logger.finer(String.format(" Registrar %s", sr));
            }
        }
    }

    public void discarded(DiscoveryEvent e) {
    }

    /**
     * A main program that will dump the components registered in a component
     * registry.  The group to use for discovery should be defined on the command
     * line using -Dgroup=&lt;groupname&gt.
     */
    public static void main(String[] args) throws Exception {
        //
        // Use the labs format logging.
        for(Handler h : Logger.getLogger("").getHandlers()) {
            h.setLevel(Level.ALL);
            h.setFormatter(new LabsLogFormatter());
            try {
                h.setEncoding("utf-8");
            } catch(Exception ex) {
            }
        }
        String group = System.getProperty("group");
        if(group == null) {
            System.err.println(String.format("System property group must be specified"));
            return;
        }
        URL configURL;
        if(args.length > 0) {
            configURL = (new File(args[0])).toURI().toURL();
        } else {
            configURL = ComponentRegistry.class.getResource("registryConfig.xml");
        }
        JiniConfigurationManager cm = new JiniConfigurationManager(configURL);
        ComponentRegistry cr = cm.getComponentRegistry();
        if(cr == null) {
            System.err.println(String.format("No component registry defined in %s", configURL));
        }
        Thread.sleep(3000);
        Map<String,List<String>> m = cr.dumpJiniServices();
        for(Map.Entry<String, List<String>> e : m.entrySet()) {
            System.out.println(String.format("Registrar: %s has %d services", e.getKey(), e.getValue().size()));
            for(String s : e.getValue()) {
                System.out.println(String.format(" Service: %s", s));
            }
        }
    }
}
