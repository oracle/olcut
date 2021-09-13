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

import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenancable;
import com.oracle.labs.mlrg.olcut.provenance.impl.ConfiguredObjectProvenanceImpl;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Configurable with all the possible field types.
 */
public class AllFieldsConfigurable implements Configurable, Provenancable<ConfiguredObjectProvenance> {
    private static final Logger logger = Logger.getLogger(AllFieldsConfigurable.class.getName());

    public enum Type {A, B, C, D, E, F};

    @ConfigurableName
    public String name;

    //Primitives
    @Config
    public boolean boolField;
    @Config
    public Boolean BoolField;

    @Config
    public byte byteField;
    @Config
    public Byte ByteField;

    @Config
    public char charField;
    @Config
    public Character characterField;

    @Config
    public short shortField;
    @Config
    public Short ShortField;

    @Config
    public int intField;
    @Config
    public Integer integerField;

    @Config
    public long longField;
    @Config
    public Long LongField;

    @Config
    public float floatField;
    @Config
    public Float FloatField;

    @Config
    public double doubleField;
    @Config
    public Double DoubleField;

    @Config
    public String stringField;

    //Primitive array types
    @Config
    public byte[] byteArrayField;

    @Config
    public char[] charArrayField;

    @Config
    public short[] shortArrayField;

    @Config
    public int[] intArrayField;

    @Config
    public long[] longArrayField;

    @Config
    public float[] floatArrayField;

    @Config
    public double[] doubleArrayField;

    @Config
    public boolean[] booleanArrayField;

    //Configurable classes
    @Config
    public Configurable configurableField;
    @Config
    public StringConfigurable configurableSubclassField;

    //Object array types
    @Config
    public String[] stringArrayField;

    @Config
    public Configurable[] configurableArrayField;
    @Config
    public StringConfigurable[] configurableSubclassArrayField;

    //Generic types - requires genericType argument to be set
    @Config
    public List<Double> listDoubleField;
    @Config
    public List<String> listStringField;
    @Config
    public List<StringConfigurable> listConfigurableSubclassField;

    @Config
    public Set<Double> setDoubleField;
    @Config
    public Set<String> setStringField;
    @Config
    public Set<Path> setPathField;
    @Config
    public Set<StringConfigurable> setConfigurableSubclassField;

    @Config
    public Map<String, Double> mapDoubleField;
    @Config
    public Map<String, String> mapStringField;
    @Config
    public Map<String, File> mapFileField;
    @Config
    public Map<String, StringConfigurable> mapConfigurableSubclassField;

    @Config
    public EnumSet<Type> enumSetField;

    //Misc types
    @Config
    public AtomicInteger atomicIntegerField;
    @Config
    public AtomicLong atomicLongField;

    @Config
    public File fileField;

    @Config
    public Path pathField;

    @Config
    public URL urlField;

    @Config
    public LocalDate dateField;

    @Config
    public OffsetDateTime dateTimeField;

    @Config
    public OffsetTime timeField;

    @Config
    public Type enumField;

    /**
     * Normalizes the paths.
     */
    @Override
    public void postConfig() {
        fileField = fileField.getAbsoluteFile();
        pathField = pathField.toAbsolutePath().normalize();
        Set<Path> newSet = new HashSet<>();
        for (Path p : setPathField) {
            newSet.add(p.toAbsolutePath().normalize());
        }
        setPathField = newSet;
        Map<String,File> newMap = new HashMap<>();
        for (Map.Entry<String,File> e : mapFileField.entrySet()) {
            newMap.put(e.getKey(),e.getValue().getAbsoluteFile());
        }
        mapFileField = newMap;
    }

    @Override
    public String toString() {
        return "AllFieldsConfigurable{" +
                "name='" + name + '\'' +
                ", boolField=" + boolField +
                ", BoolField=" + BoolField +
                ", byteField=" + byteField +
                ", ByteField=" + ByteField +
                ", charField=" + charField +
                ", characterField=" + characterField +
                ", shortField=" + shortField +
                ", ShortField=" + ShortField +
                ", intField=" + intField +
                ", integerField=" + integerField +
                ", longField=" + longField +
                ", LongField=" + LongField +
                ", floatField=" + floatField +
                ", FloatField=" + FloatField +
                ", doubleField=" + doubleField +
                ", DoubleField=" + DoubleField +
                ", stringField='" + stringField + '\'' +
                ", byteArrayField=" + Arrays.toString(byteArrayField) +
                ", charArrayField=" + Arrays.toString(charArrayField) +
                ", shortArrayField=" + Arrays.toString(shortArrayField) +
                ", intArrayField=" + Arrays.toString(intArrayField) +
                ", longArrayField=" + Arrays.toString(longArrayField) +
                ", floatArrayField=" + Arrays.toString(floatArrayField) +
                ", doubleArrayField=" + Arrays.toString(doubleArrayField) +
                ", configurableField=" + configurableField +
                ", configurableSubclassField=" + configurableSubclassField +
                ", stringArrayField=" + Arrays.toString(stringArrayField) +
                ", configurableArrayField=" + Arrays.toString(configurableArrayField) +
                ", configurableSubclassArrayField=" + Arrays.toString(configurableSubclassArrayField) +
                ", listDoubleField=" + listDoubleField +
                ", listStringField=" + listStringField +
                ", listConfigurableSubclassField=" + listConfigurableSubclassField +
                ", setDoubleField=" + setDoubleField +
                ", setStringField=" + setStringField +
                ", setPathField=" + setPathField +
                ", setConfigurableSubclassField=" + setConfigurableSubclassField +
                ", mapDoubleField=" + mapDoubleField +
                ", mapStringField=" + mapStringField +
                ", mapFileField=" + mapFileField +
                ", mapConfigurableSubclassField=" + mapConfigurableSubclassField +
                ", enumSetField=" + enumSetField +
                ", atomicIntegerField=" + atomicIntegerField +
                ", atomicLongField=" + atomicLongField +
                ", fileField=" + fileField +
                ", pathField=" + pathField +
                ", urlField=" + urlField +
                ", dateField=" + dateField +
                ", dateTimeField=" + dateTimeField +
                ", timeField=" + timeField +
                ", enumField=" + enumField +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AllFieldsConfigurable that = (AllFieldsConfigurable) o;

        if (boolField != that.boolField) {logger.log(Level.INFO,"boolField differs, this = " + boolField + ", other = " + that.boolField); return false;}
        if (byteField != that.byteField) {logger.log(Level.INFO,"byteField differs, this = " + byteField + ", other = " + that.byteField); return false;}
        if (charField != that.charField) {logger.log(Level.INFO,"charField differs, this = " + charField + ", other = " + that.charField); return false;}
        if (shortField != that.shortField) {logger.log(Level.INFO,"shortField differs, this = " + shortField + ", other = " + that.shortField); return false;}
        if (intField != that.intField) {logger.log(Level.INFO,"intField differs, this = " + intField + ", other = " + that.intField); return false;}
        if (longField != that.longField) {logger.log(Level.INFO,"longField differs, this = " + longField + ", other = " + that.longField); return false;}
        if (Float.compare(that.floatField, floatField) != 0) {logger.log(Level.INFO,"floatField differs, this = " + floatField + ", other = " + that.floatField); return false;}
        if (Double.compare(that.doubleField, doubleField) != 0) {logger.log(Level.INFO,"doubleField differs, this = " + doubleField + ", other = " + that.doubleField); return false;}
        //if (!Objects.equals(name, that.name)) {logger.log(Level.INFO,"name differs, this = " + name + ", other = " + that.name); return false;}
        if (!Objects.equals(BoolField, that.BoolField)) {logger.log(Level.INFO,"BoolField differs, this = " + BoolField + ", other = " + that.BoolField); return false;}
        if (!Objects.equals(ByteField, that.ByteField)) {logger.log(Level.INFO,"ByteField differs, this = " + ByteField + ", other = " + that.ByteField); return false;}
        if (!Objects.equals(characterField, that.characterField)) {logger.log(Level.INFO,"characterField differs, this = " + characterField + ", other = " + that.characterField); return false;}
        if (!Objects.equals(ShortField, that.ShortField)) {logger.log(Level.INFO,"ShortField differs, this = " + ShortField + ", other = " + that.ShortField); return false;}
        if (!Objects.equals(integerField, that.integerField)) {logger.log(Level.INFO,"integerField differs, this = " + integerField + ", other = " + that.integerField); return false;}
        if (!Objects.equals(LongField, that.LongField)) {logger.log(Level.INFO,"LongField differs, this = " + LongField + ", other = " + that.LongField); return false;}
        if (!Objects.equals(FloatField, that.FloatField)) {logger.log(Level.INFO,"FloatField differs, this = " + FloatField + ", other = " + that.FloatField); return false;}
        if (!Objects.equals(DoubleField, that.DoubleField)) {logger.log(Level.INFO,"DoubleField differs, this = " + DoubleField + ", other = " + that.DoubleField); return false;}
        if (!Objects.equals(stringField, that.stringField)) {logger.log(Level.INFO,"stringField differs, this = " + stringField + ", other = " + that.stringField); return false;}
        if (!Arrays.equals(byteArrayField, that.byteArrayField)) {logger.log(Level.INFO,"byteArrayField differs, this = " + byteArrayField + ", other = " + that.byteArrayField); return false;}
        if (!Arrays.equals(charArrayField, that.charArrayField)) {logger.log(Level.INFO,"charArrayField differs, this = " + charArrayField + ", other = " + that.charArrayField); return false;}
        if (!Arrays.equals(shortArrayField, that.shortArrayField)) {logger.log(Level.INFO,"shortArrayField differs, this = " + shortArrayField + ", other = " + that.shortArrayField); return false;}
        if (!Arrays.equals(intArrayField, that.intArrayField)) {logger.log(Level.INFO,"intArrayField differs, this = " + intArrayField + ", other = " + that.intArrayField); return false;}
        if (!Arrays.equals(longArrayField, that.longArrayField)) {logger.log(Level.INFO,"longArrayField differs, this = " + longArrayField + ", other = " + that.longArrayField); return false;}
        if (!Arrays.equals(floatArrayField, that.floatArrayField)) {logger.log(Level.INFO,"floatArrayField differs, this = " + floatArrayField + ", other = " + that.floatArrayField); return false;}
        if (!Arrays.equals(doubleArrayField, that.doubleArrayField)) {logger.log(Level.INFO,"doubleArrayField differs, this = " + doubleArrayField + ", other = " + that.doubleArrayField); return false;}
        if (!Objects.equals(configurableField, that.configurableField))
            {logger.log(Level.INFO,"configurableField differs, this = " + configurableField + ", other = " + that.configurableField); return false;}
        if (!Objects.equals(configurableSubclassField, that.configurableSubclassField))
            {logger.log(Level.INFO,"configurableSubclassField differs, this = " + configurableSubclassField + ", other = " + that.configurableSubclassField); return false;}
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(stringArrayField, that.stringArrayField)) {logger.log(Level.INFO,"stringArrayField differs, this = " + stringArrayField + ", other = " + that.stringArrayField); return false;}
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(configurableArrayField, that.configurableArrayField)) {logger.log(Level.INFO,"configurableArrayField differs, this = " + configurableArrayField + ", other = " + that.configurableArrayField); return false;}
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(configurableSubclassArrayField, that.configurableSubclassArrayField)) {logger.log(Level.INFO,"configurableSubclassArrayField differs, this = " + configurableSubclassArrayField + ", other = " + that.configurableSubclassArrayField); return false;}
        if (!Objects.equals(listDoubleField, that.listDoubleField))
            {logger.log(Level.INFO,"listDoubleField differs, this = " + listDoubleField + ", other = " + that.listDoubleField); return false;}
        if (!Objects.equals(listStringField, that.listStringField))
            {logger.log(Level.INFO,"listStringField differs, this = " + listStringField + ", other = " + that.listStringField); return false;}
        if (!Objects.equals(listConfigurableSubclassField, that.listConfigurableSubclassField))
            {logger.log(Level.INFO,"listConfigurableSubclassField differs, this = " + listConfigurableSubclassField + ", other = " + that.listConfigurableSubclassField); return false;}
        if (!Objects.equals(setDoubleField, that.setDoubleField))
            {logger.log(Level.INFO,"setDoubleField differs, this = " + setDoubleField + ", other = " + that.setDoubleField); return false;}
        if (!Objects.equals(setStringField, that.setStringField))
            {logger.log(Level.INFO,"setStringField differs, this = " + setStringField + ", other = " + that.setStringField); return false;}
        if (!Objects.equals(setPathField, that.setPathField)) {logger.log(Level.INFO,"setPathField differs, this = " + setPathField + ", other = " + that.setPathField); return false;}
        if (!Objects.equals(setConfigurableSubclassField, that.setConfigurableSubclassField))
            {logger.log(Level.INFO,"setConfigurableSubclassField differs, this = " + setConfigurableSubclassField + ", other = " + that.setConfigurableSubclassField); return false;}
        if (!Objects.equals(mapDoubleField, that.mapDoubleField))
            {logger.log(Level.INFO,"mapDoubleField differs, this = " + mapDoubleField + ", other = " + that.mapDoubleField); return false;}
        if (!Objects.equals(mapStringField, that.mapStringField))
            {logger.log(Level.INFO,"mapStringField differs, this = " + mapStringField + ", other = " + that.mapStringField); return false;}
        if (!Objects.equals(mapFileField, that.mapFileField))
            {logger.log(Level.INFO,"mapFileField differs, this = " + mapFileField + ", other = " + that.mapFileField); return false;}
        if (!Objects.equals(mapConfigurableSubclassField, that.mapConfigurableSubclassField))
            {logger.log(Level.INFO,"mapConfigurableSubclassField differs, this = " + mapConfigurableSubclassField + ", other = " + that.mapConfigurableSubclassField); return false;}
        if (!Objects.equals(enumSetField, that.enumSetField)) {logger.log(Level.INFO,"enumSetField differs, this = " + enumSetField + ", other = " + that.enumSetField); return false;}
        if (atomicIntegerField != null ? atomicIntegerField.intValue() != that.atomicIntegerField.intValue() : that.atomicIntegerField != null)
            {logger.log(Level.INFO,"atomicIntegerField differs, this = " + atomicIntegerField + ", other = " + that.atomicIntegerField); return false;}
        if (atomicLongField != null ? atomicLongField.longValue() != that.atomicLongField.longValue() : that.atomicLongField != null)
            {logger.log(Level.INFO,"atomicLongField differs, this = " + atomicLongField + ", other = " + that.atomicLongField); return false;}
        if (!Objects.equals(fileField, that.fileField)) {logger.log(Level.INFO,"fileField differs, this = " + fileField + ", other = " + that.fileField); return false;}
        if (!Objects.equals(pathField, that.pathField)) {logger.log(Level.INFO,"pathField differs, this = " + pathField + ", other = " + that.pathField); return false;}
        if (!Objects.equals(urlField, that.urlField)) {logger.log(Level.INFO,"urlField differs, this = " + urlField + ", other = " + that.urlField); return false;}
        if (!Objects.equals(dateField, that.dateField)) {logger.log(Level.INFO,"dateField differs, this = " + dateField + ", other = " + that.dateField); return false;}
        if (!Objects.equals(dateTimeField, that.dateTimeField)) {logger.log(Level.INFO,"dateTimeField differs, this = " + dateTimeField + ", other = " + that.dateTimeField); return false;}
        if (!Objects.equals(timeField, that.timeField)) {logger.log(Level.INFO,"timeField differs, this = " + timeField + ", other = " + that.timeField); return false;}
        return Objects.equals(enumField, that.enumField);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (boolField ? 1 : 0);
        result = 31 * result + (BoolField != null ? BoolField.hashCode() : 0);
        result = 31 * result + (int) byteField;
        result = 31 * result + (ByteField != null ? ByteField.hashCode() : 0);
        result = 31 * result + (int) charField;
        result = 31 * result + (characterField != null ? characterField.hashCode() : 0);
        result = 31 * result + (int) shortField;
        result = 31 * result + (ShortField != null ? ShortField.hashCode() : 0);
        result = 31 * result + intField;
        result = 31 * result + (integerField != null ? integerField.hashCode() : 0);
        result = 31 * result + (int) (longField ^ (longField >>> 32));
        result = 31 * result + (LongField != null ? LongField.hashCode() : 0);
        result = 31 * result + (floatField != +0.0f ? Float.floatToIntBits(floatField) : 0);
        result = 31 * result + (FloatField != null ? FloatField.hashCode() : 0);
        temp = Double.doubleToLongBits(doubleField);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (DoubleField != null ? DoubleField.hashCode() : 0);
        result = 31 * result + (stringField != null ? stringField.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(byteArrayField);
        result = 31 * result + Arrays.hashCode(charArrayField);
        result = 31 * result + Arrays.hashCode(shortArrayField);
        result = 31 * result + Arrays.hashCode(intArrayField);
        result = 31 * result + Arrays.hashCode(longArrayField);
        result = 31 * result + Arrays.hashCode(floatArrayField);
        result = 31 * result + Arrays.hashCode(doubleArrayField);
        result = 31 * result + (configurableField != null ? configurableField.hashCode() : 0);
        result = 31 * result + (configurableSubclassField != null ? configurableSubclassField.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(stringArrayField);
        result = 31 * result + Arrays.hashCode(configurableArrayField);
        result = 31 * result + Arrays.hashCode(configurableSubclassArrayField);
        result = 31 * result + (listDoubleField != null ? listDoubleField.hashCode() : 0);
        result = 31 * result + (listStringField != null ? listStringField.hashCode() : 0);
        result = 31 * result + (listConfigurableSubclassField != null ? listConfigurableSubclassField.hashCode() : 0);
        result = 31 * result + (setDoubleField != null ? setDoubleField.hashCode() : 0);
        result = 31 * result + (setStringField != null ? setStringField.hashCode() : 0);
        result = 31 * result + (setPathField != null ? setPathField.hashCode() : 0);
        result = 31 * result + (setConfigurableSubclassField != null ? setConfigurableSubclassField.hashCode() : 0);
        result = 31 * result + (mapDoubleField != null ? mapDoubleField.hashCode() : 0);
        result = 31 * result + (mapStringField != null ? mapStringField.hashCode() : 0);
        result = 31 * result + (mapFileField != null ? mapFileField.hashCode() : 0);
        result = 31 * result + (mapConfigurableSubclassField != null ? mapConfigurableSubclassField.hashCode() : 0);
        result = 31 * result + (enumSetField != null ? enumSetField.hashCode() : 0);
        result = 31 * result + (atomicIntegerField != null ? atomicIntegerField.intValue() : 0);
        result = 31 * result + (atomicLongField != null ? atomicLongField.intValue() : 0);
        result = 31 * result + (fileField != null ? fileField.hashCode() : 0);
        result = 31 * result + (pathField != null ? pathField.hashCode() : 0);
        result = 31 * result + (urlField != null ? urlField.hashCode() : 0);
        result = 31 * result + (dateField != null ? dateField.hashCode() : 0);
        result = 31 * result + (dateTimeField != null ? dateTimeField.hashCode() : 0);
        result = 31 * result + (timeField != null ? timeField.hashCode() : 0);
        result = 31 * result + (enumField != null ? enumField.hashCode() : 0);
        return result;
    }

    @Override
    public ConfiguredObjectProvenance getProvenance() {
        return new ConfiguredObjectProvenanceImpl(this,"Test");
    }
}
