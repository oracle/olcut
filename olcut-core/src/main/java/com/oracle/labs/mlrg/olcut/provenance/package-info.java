/**
 * Provides a provenance system for tracking properties of objects. Can convert the
 * provenance objects into a {@link com.oracle.labs.mlrg.olcut.config.ConfigurationData}
 * and also marshall them for serialisation/deserialisation.
 *
 * Principally used to track ML models, along with their hyperparameters and data,
 * though can be used for anything which needs a shadow copy of an object to record
 * its properties.
 *
 * The provenance system can be used without loading the classes of the host objects,
 * and so goes to great lengths to avoid class loading of those objects. This causes
 * some issues where provenances can only be checked after they are fully constructed,
 * and the configurations extracted may not necessarily map to the original object if
 * there is version skew or the serialised provenance was altered.
 */
package com.oracle.labs.mlrg.olcut.provenance;