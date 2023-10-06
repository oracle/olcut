/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.oracle.labs.mlrg.olcut.config.protobuf;

import com.google.protobuf.TextFormat;
import com.oracle.labs.mlrg.olcut.config.ConfigurationData;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.PropertyException;
import com.oracle.labs.mlrg.olcut.config.SerializedObject;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoader;
import com.oracle.labs.mlrg.olcut.config.io.ConfigLoaderException;
import com.oracle.labs.mlrg.olcut.config.io.URLLoader;
import com.oracle.labs.mlrg.olcut.config.property.GlobalProperties;
import com.oracle.labs.mlrg.olcut.config.property.ListProperty;
import com.oracle.labs.mlrg.olcut.config.property.MapProperty;
import com.oracle.labs.mlrg.olcut.config.property.SimpleProperty;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.ComponentProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.ConfigFileProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.ConfigProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.PropertyListProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.PropertyMapProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.SerializedObjectProto;
import com.oracle.labs.mlrg.olcut.util.IOUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A loader for configuration data stored in protobuf format.
 */
public final class ProtoLoader implements ConfigLoader {

    private static final Logger logger = Logger.getLogger(ProtoLoader.class.getName());

    private final URLLoader parent;

    private final Map<String, ConfigurationData> rpdMap;

    private final Map<String, ConfigurationData> existingRPD;

    private final Map<String, SerializedObject> serializedObjects;

    private final GlobalProperties globalProperties;

    private final boolean parseTextFormat;

    /**
     * Constructs a configuration loader for protobuf data.
     * @param parent The URL loader for this instance of the configuration manager.
     * @param rpdMap The configuration data for the current instance.
     * @param existingRPD Any pre-existing configuration data which may be inherited.
     * @param serializedObjects The current set of serialized objects.
     * @param globalProperties The global properties.
     * @param parseTextFormat Should this loader parse text protobufs or binary ones.
     */
    public ProtoLoader(URLLoader parent, Map<String, ConfigurationData> rpdMap,
                                  Map<String, ConfigurationData> existingRPD,
                                  Map<String, SerializedObject> serializedObjects,
                                  GlobalProperties globalProperties, boolean parseTextFormat) {
        this.parent = parent;
        this.rpdMap = rpdMap;
        this.existingRPD = existingRPD;
        this.serializedObjects = serializedObjects;
        this.globalProperties = globalProperties;
        this.parseTextFormat = parseTextFormat;
    }

    @Override
    public void load(URL url) throws ConfigLoaderException {
        AccessController.doPrivileged((PrivilegedAction<Void>)
                () -> {
                    String workingDir = "";
                    if (url.getProtocol().equals("file")) {
                        workingDir = new File(url.getFile()).getParent();
                    } else if (IOUtil.isDisallowedProtocol(url)) {
                        throw new ConfigLoaderException("Unable to load configurations from URLs with protocol: " + url.getProtocol());
                    }
                    try (InputStream is = url.openStream()) {
                        ConfigProto proto;
                        if (parseTextFormat) {
                            ConfigProto.Builder protoBuilder = ConfigProto.newBuilder();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                            TextFormat.getParser().merge(reader,protoBuilder);
                            proto = protoBuilder.build();
                        } else {
                            proto = ConfigProto.parseFrom(is);
                        }
                        parseConfigProto(workingDir,proto);
                    } catch (IOException e) {
                        throw new ConfigLoaderException(e, e.getMessage());
                    }
                    return null;
                }
        );
    }

    @Override
    public void load(InputStream stream) throws ConfigLoaderException {
        try {
            ConfigProto proto;
            if (parseTextFormat) {
                ConfigProto.Builder protoBuilder = ConfigProto.newBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                TextFormat.getParser().merge(reader, protoBuilder);
                proto = protoBuilder.build();
            } else {
                proto = ConfigProto.parseFrom(stream);
            }
            parseConfigProto("", proto);
        } catch (IOException e) {
            String msg = "Error while parsing input: " + e.getMessage();
            throw new ConfigLoaderException(e, msg);
        }
    }

    /**
     * Parses the config proto into this loader.
     * @param workingDir The current working directory if known.
     * @param proto The proto to parse.
     */
    private void parseConfigProto(String workingDir, ConfigProto proto) {
        // Parse global properties
        for (Map.Entry<String,String> e : proto.getPropertiesMap().entrySet()) {
            try {
                globalProperties.setValue(e.getKey(), e.getValue());
            } catch (PropertyException ex) {
                throw new ConfigLoaderException("Invalid global property name: " + e.getKey());
            }
        }

        // Parse additional files
        for (ConfigFileProto file : proto.getConfigFileList()) {
            String name = file.getName();
            String value = file.getValue();
            if (name.isEmpty() || value.isEmpty()) {
                throw new ConfigLoaderException("File element must have "
                        + "'name' and 'value' attributes, found " + name + ", " + value);
            }
            try {
                URL newURL = ConfigurationManager.class.getResource(value);
                if (newURL == null) {
                    File newFile = new File(value);
                    if (!newFile.isAbsolute()) {
                        newFile = new File(workingDir,value);
                    }
                    newURL = newFile.toURI().toURL();
                }
                parent.addURL(newURL);
            } catch (MalformedURLException ex) {
                throw new ConfigLoaderException(ex, "Incorrectly formatted file element " + name + " with value " + value);
            }
        }

        // Parse serialized objects
        for (SerializedObjectProto serObj : proto.getSerializedObjectList()) {
            String name = serObj.getName();
            String type = serObj.getType();
            String location = serObj.getLocation();
            if (name.isEmpty() || type.isEmpty() || location.isEmpty()) {
                throw new ConfigLoaderException("Serialized element must have 'name', 'type' and 'location' elements, found " + serObj.toString());
            }
            serializedObjects.put(name, new SerializedObject(name, location, type));
        }

        // Parse components
        for (ComponentProto component : proto.getComponentsList()) {
            parseComponentProto(component);
        }
    }

    /**
     * Parses the component proto into this loader.
     * @param component The component to parse.
     */
    private void parseComponentProto(ComponentProto component) {
        String name = component.getName();
        String type = component.getType();
        String override = component.getOverride();
        //
        // Check for a badly formed component tag.
        if (name.isEmpty() || (type.isEmpty() && override.isEmpty())) {
            throw new ConfigLoaderException("Component element must specify "
                    + "'name' and either 'type' or 'inherit' attributes, found " + component);
        }

        boolean exportable = component.getExportable();
        boolean importable = component.getImportable();
        if (!exportable && component.hasLeaseTime()) {
            throw new ConfigLoaderException("lease timeout " + component.getLeaseTime() +
                    " specified for component '" + name + "' that does not have export set");
        } else if (component.hasLeaseTime() && component.getLeaseTime() < 0) {
                throw new ConfigLoaderException("lease timeout "
                        + component.getLeaseTime() + " must be greater than 0, for component " + name);
        }
        long leaseTime = component.hasLeaseTime() ? component.getLeaseTime() : ConfigurationData.DEFAULT_LEASE_TIME;
        String entriesName = component.hasEntries() ? component.getEntries() : null;
        String serializedForm = component.hasSerialized() ? component.getSerialized() : null;

        ConfigurationData rpd;
        if (!override.isEmpty()) {
            //
            // If we're overriding an existing type, then we should pull
            // its property set, copy it and override it. Note that we're
            // not doing any type checking here, so it's possible to specify
            // a type for override that is incompatible with the specified
            // properties. If that's the case, then things might get
            // really weird. We'll log an override with a specified type
            // just in case.
            ConfigurationData spd = rpdMap.get(override);
            if (spd == null) {
                if (existingRPD == null || !existingRPD.containsKey(override)) {
                    throw new ConfigLoaderException("Failed to find base component '"+override+"' inherited from '"+name+"'.");
                } else {
                    spd = existingRPD.get(override);
                }
            }
            if (!type.isEmpty() && !type.equals(spd.getClassName())) {
                logger.log(Level.FINE, String.format("Overriding component %s with component %s, new type is %s overridden type was %s",
                        spd.getName(), name, type, spd.getClassName()));
            }
            if (type.isEmpty()) {
                type = spd.getClassName();
            }
            rpd = new ConfigurationData(name, type, spd.getProperties(), serializedForm, entriesName, exportable, importable, leaseTime);
        } else {
            if (rpdMap.get(name) != null) {
                throw new ConfigLoaderException("duplicate definition for "
                        + name);
            }
            rpd = new ConfigurationData(name, type, serializedForm, entriesName, exportable, importable, leaseTime);
        }

        // Read out simple properties
        for (Map.Entry<String,String> e : component.getPropertiesMap().entrySet()) {
            rpd.add(e.getKey(), new SimpleProperty(e.getValue()));
        }

        // Read out map properties
        for (PropertyMapProto e : component.getMapPropertyList()) {
            // Must be map
            Map<String, SimpleProperty> mapOutput = new HashMap<>();
            for (Map.Entry<String,String> mapProp : e.getElementsMap().entrySet()) {
                mapOutput.put(mapProp.getKey(), new SimpleProperty(mapProp.getValue()));
            }
            rpd.add(e.getName(), new MapProperty(mapOutput));
        }

        // Read out list properties
        for (PropertyListProto e : component.getListPropertyList()) {
            List<SimpleProperty> listOutput = new ArrayList<>();
            List<Class<?>> classListOutput = new ArrayList<>();

            for (String item : e.getItemList()) {
                listOutput.add(new SimpleProperty(item));
            }

            for (String listType : e.getTypeList()) {
                try {
                    classListOutput.add(Class.forName(listType));
                } catch (ClassNotFoundException cnfe) {
                    throw new ConfigLoaderException("Unable to find class "
                            + listType + " in component '" + name + "', propertylist '" + e.getName() + "'");
                }
            }

            ListProperty listProp;
            if (classListOutput.isEmpty()) {
                listProp = new ListProperty(listOutput);
            } else {
                listProp = new ListProperty(listOutput,classListOutput);
            }
            rpd.add(e.getName(), listProp);
        }
        rpdMap.put(rpd.getName(),rpd);
    }
}
