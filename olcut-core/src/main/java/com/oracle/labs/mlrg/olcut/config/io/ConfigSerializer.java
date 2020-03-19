package com.oracle.labs.mlrg.olcut.config.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenancable;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil;

public class ConfigSerializer {

	public static void writeObject(Provenancable<ConfiguredObjectProvenance> provenancable, ObjectOutputStream outputStream) throws IOException{
        ConfiguredObjectProvenance provenance = provenancable.getProvenance();
        List<ConfigurationData> configurationData = ProvenanceUtil.extractConfiguration(provenance);
        String componentName = configurationData.get(0).getName();
        outputStream.writeObject(componentName);
        outputStream.writeObject(provenance);
	}

	public static Provenancable<ConfiguredObjectProvenance> readObject(ObjectInputStream inputStream) throws ClassNotFoundException, IOException 
    {
		String componentName = (String) inputStream.readObject();
		ConfiguredObjectProvenance provenance = (ConfiguredObjectProvenance) inputStream.readObject();
		List<ConfigurationData> configurationData = ProvenanceUtil.extractConfiguration(provenance);
        ConfigurationManager cm = new ConfigurationManager();
        cm.addConfiguration(configurationData);
        @SuppressWarnings("unchecked")
		Provenancable<ConfiguredObjectProvenance> provenancable = (Provenancable<ConfiguredObjectProvenance>) cm.lookup(componentName);
        cm.close();
        return provenancable;
    }
	
}
