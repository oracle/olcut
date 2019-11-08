package com.oracle.labs.mlrg.olcut.config;

import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenancable;
import com.oracle.labs.mlrg.olcut.provenance.impl.ConfiguredObjectProvenanceImpl;

public class RedactedConfigurable implements Configurable, Provenancable<ConfiguredObjectProvenance> {

    @Config(mandatory=true)
    public String present;

    @Config(redact=true)
    public String redacted;

    @Config(mandatory=true,redact=true)
    public String mandatoryRedacted;

    /**
     * For olcut.
     */
    private RedactedConfigurable() {}

    public RedactedConfigurable(String present, String redacted, String mandatoryRedacted) {
        this.present = present;
        this.redacted = redacted;
        this.mandatoryRedacted = mandatoryRedacted;
    }

    @Override
    public ConfiguredObjectProvenance getProvenance() {
        return new ConfiguredObjectProvenanceImpl(this,"RedactedConfigurable");
    }

}
