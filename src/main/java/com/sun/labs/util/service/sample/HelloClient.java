package com.sun.labs.util.service.sample;

import com.sun.labs.util.LabsLogFormatter;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.service.ConfigurableServiceStarter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A client for the hello service.
 */
public class HelloClient {
    private static final Logger logger = Logger.getLogger(HelloClient.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //
        // Set up the Java logging with our log formatter, which is nicer
        // than the default, IMHO.
        Logger rl = Logger.getLogger("");
        for (Handler h : rl.getHandlers()) {
            h.setLevel(Level.ALL);
            h.setFormatter(new LabsLogFormatter());
            try {
                h.setEncoding("utf-8");
            } catch (Exception ex) {
                rl.severe("Error setting output encoding");
            }
        }

        //
        // Read the configuration file.
        String configFileName = args[0];
        ConfigurationManager cm = null;
        try {
            //
            // See if we can get a resource for the configuration file first.
            // This is mostly a convenience.
            URL cu = ConfigurableServiceStarter.class.getResource(configFileName);
            if (cu == null) {
                cu = (new File(configFileName)).toURI().toURL();
            }
            cm = new ConfigurationManager(cu);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, String.format("Error reading config file"), ex);
        }
        
        if(cm == null) {
            System.err.println("No configuration manager!");
            return;
        }
        
        //
        // Get all of the registered hello services. Note that we use the interface
        // here and not the implementation. We need to do this because we'll only
        // get a stub that implements the implementation.
        List<HelloService> hellos = cm.lookupAll(com.sun.labs.util.service.sample.HelloService.class, null);
        System.out.format("Got %d hello services\n", hellos.size());
        for(HelloService hello : hellos) {
            try {
                System.out.format("%s says %s\n", hello, hello.hello());
            } catch (RemoteException ex) {
                logger.log(Level.SEVERE, String.format("Error helloing %s", hello), ex);
            }
        }
    }
    
}
