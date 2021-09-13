/*
 * Copyright (c) 2004-2021, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationDataTest {

    @Test
    public void structuralEqualsAllConfig() {
        final String aName = "all-config";
        ConfigurationManager cm = new ConfigurationManager("allConfig.xml");
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup(aName);
        List<ConfigurationData> a = cm.getComponentNames().stream()
                .map(cm::getConfigurationData)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        cm.close();
        cm = new ConfigurationManager();
        final String bName = cm.importConfigurable(ac);

        List<ConfigurationData> b = cm.getComponentNames().stream()
                .map(cm::getConfigurationData)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        ConfigurationData aRoot = a.stream().filter(cd -> cd.getName().equals(aName)).findFirst().get();
        ConfigurationData bRoot = b.stream().filter(cd -> cd.getName().equals(bName)).findFirst().get();

        assertTrue(ConfigurationData.structuralEquals(a, b, aName, bName));

        ac.stringField = "Something different from before";

        cm.close();
        cm = new ConfigurationManager();

        String b2Name = cm.importConfigurable(ac);

        b = cm.getComponentNames().stream()
                .map(cm::getConfigurationData)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        assertFalse(ConfigurationData.structuralEquals(a, b, aName, bName));
    }

    @Test
    public void structuralEqualsProvenanceRoundtrip() {
        ConfigurationManager cm = new ConfigurationManager("allConfig.xml");
        AllFieldsConfigurable ac = (AllFieldsConfigurable) cm.lookup("all-config");

        cm.close();
        cm = new ConfigurationManager();

        String aName = cm.importConfigurable(ac);
        List<ConfigurationData> a = cm.getComponentNames().stream()
                .map(cm::getConfigurationData)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        List<ConfigurationData> b = ProvenanceUtil.extractConfiguration(ac.getProvenance());

        String bName = b.get(0).getName();

        assertTrue(ConfigurationData.structuralEquals(a, b, aName, bName));
    }

}
