package com.oracle.labs.mlrg.olcut.config;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Configurable with all the possible field types.
 */
public class AllFieldsConfigurable implements Configurable {
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
    public List<Random> listRandomField;
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
    public Random randomField;

    @Config
    public Type enumField;

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
        if (name != null ? !name.equals(that.name) : that.name != null) {logger.log(Level.INFO,"name differs, this = " + name + ", other = " + that.name); return false;}
        if (BoolField != null ? !BoolField.equals(that.BoolField) : that.BoolField != null) {logger.log(Level.INFO,"BoolField differs, this = " + BoolField + ", other = " + that.BoolField); return false;}
        if (ByteField != null ? !ByteField.equals(that.ByteField) : that.ByteField != null) {logger.log(Level.INFO,"ByteField differs, this = " + ByteField + ", other = " + that.ByteField); return false;}
        if (characterField != null ? !characterField.equals(that.characterField) : that.characterField != null) {logger.log(Level.INFO,"characterField differs, this = " + characterField + ", other = " + that.characterField); return false;}
        if (ShortField != null ? !ShortField.equals(that.ShortField) : that.ShortField != null) {logger.log(Level.INFO,"ShortField differs, this = " + ShortField + ", other = " + that.ShortField); return false;}
        if (integerField != null ? !integerField.equals(that.integerField) : that.integerField != null) {logger.log(Level.INFO,"integerField differs, this = " + integerField + ", other = " + that.integerField); return false;}
        if (LongField != null ? !LongField.equals(that.LongField) : that.LongField != null) {logger.log(Level.INFO,"LongField differs, this = " + LongField + ", other = " + that.LongField); return false;}
        if (FloatField != null ? !FloatField.equals(that.FloatField) : that.FloatField != null) {logger.log(Level.INFO,"FloatField differs, this = " + FloatField + ", other = " + that.FloatField); return false;}
        if (DoubleField != null ? !DoubleField.equals(that.DoubleField) : that.DoubleField != null) {logger.log(Level.INFO,"DoubleField differs, this = " + DoubleField + ", other = " + that.DoubleField); return false;}
        if (stringField != null ? !stringField.equals(that.stringField) : that.stringField != null) {logger.log(Level.INFO,"stringField differs, this = " + stringField + ", other = " + that.stringField); return false;}
        if (!Arrays.equals(byteArrayField, that.byteArrayField)) {logger.log(Level.INFO,"byteArrayField differs, this = " + byteArrayField + ", other = " + that.byteArrayField); return false;}
        if (!Arrays.equals(charArrayField, that.charArrayField)) {logger.log(Level.INFO,"charArrayField differs, this = " + charArrayField + ", other = " + that.charArrayField); return false;}
        if (!Arrays.equals(shortArrayField, that.shortArrayField)) {logger.log(Level.INFO,"shortArrayField differs, this = " + shortArrayField + ", other = " + that.shortArrayField); return false;}
        if (!Arrays.equals(intArrayField, that.intArrayField)) {logger.log(Level.INFO,"intArrayField differs, this = " + intArrayField + ", other = " + that.intArrayField); return false;}
        if (!Arrays.equals(longArrayField, that.longArrayField)) {logger.log(Level.INFO,"longArrayField differs, this = " + longArrayField + ", other = " + that.longArrayField); return false;}
        if (!Arrays.equals(floatArrayField, that.floatArrayField)) {logger.log(Level.INFO,"floatArrayField differs, this = " + floatArrayField + ", other = " + that.floatArrayField); return false;}
        if (!Arrays.equals(doubleArrayField, that.doubleArrayField)) {logger.log(Level.INFO,"doubleArrayField differs, this = " + doubleArrayField + ", other = " + that.doubleArrayField); return false;}
        if (configurableField != null ? !configurableField.equals(that.configurableField) : that.configurableField != null)
            {logger.log(Level.INFO,"configurableField differs, this = " + configurableField + ", other = " + that.configurableField); return false;}
        if (configurableSubclassField != null ? !configurableSubclassField.equals(that.configurableSubclassField) : that.configurableSubclassField != null)
            {logger.log(Level.INFO,"configurableSubclassField differs, this = " + configurableSubclassField + ", other = " + that.configurableSubclassField); return false;}
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(stringArrayField, that.stringArrayField)) {logger.log(Level.INFO,"stringArrayField differs, this = " + stringArrayField + ", other = " + that.stringArrayField); return false;}
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(configurableArrayField, that.configurableArrayField)) {logger.log(Level.INFO,"configurableArrayField differs, this = " + configurableArrayField + ", other = " + that.configurableArrayField); return false;}
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(configurableSubclassArrayField, that.configurableSubclassArrayField)) {logger.log(Level.INFO,"configurableSubclassArrayField differs, this = " + configurableSubclassArrayField + ", other = " + that.configurableSubclassArrayField); return false;}
        if (listDoubleField != null ? !listDoubleField.equals(that.listDoubleField) : that.listDoubleField != null)
            {logger.log(Level.INFO,"listDoubleField differs, this = " + listDoubleField + ", other = " + that.listDoubleField); return false;}
        if (listStringField != null ? !listStringField.equals(that.listStringField) : that.listStringField != null)
            {logger.log(Level.INFO,"listStringField differs, this = " + listStringField + ", other = " + that.listStringField); return false;}
        if (listConfigurableSubclassField != null ? !listConfigurableSubclassField.equals(that.listConfigurableSubclassField) : that.listConfigurableSubclassField != null)
            {logger.log(Level.INFO,"listConfigurableSubclassField differs, this = " + listConfigurableSubclassField + ", other = " + that.listConfigurableSubclassField); return false;}
        if (setDoubleField != null ? !setDoubleField.equals(that.setDoubleField) : that.setDoubleField != null)
            {logger.log(Level.INFO,"setDoubleField differs, this = " + setDoubleField + ", other = " + that.setDoubleField); return false;}
        if (setStringField != null ? !setStringField.equals(that.setStringField) : that.setStringField != null)
            {logger.log(Level.INFO,"setStringField differs, this = " + setStringField + ", other = " + that.setStringField); return false;}
        if (setPathField != null ? !setPathField.equals(that.setPathField) : that.setPathField != null) {logger.log(Level.INFO,"setPathField differs, this = " + setPathField + ", other = " + that.setPathField); return false;}
        if (setConfigurableSubclassField != null ? !setConfigurableSubclassField.equals(that.setConfigurableSubclassField) : that.setConfigurableSubclassField != null)
            {logger.log(Level.INFO,"setConfigurableSubclassField differs, this = " + setConfigurableSubclassField + ", other = " + that.setConfigurableSubclassField); return false;}
        if (mapDoubleField != null ? !mapDoubleField.equals(that.mapDoubleField) : that.mapDoubleField != null)
            {logger.log(Level.INFO,"mapDoubleField differs, this = " + mapDoubleField + ", other = " + that.mapDoubleField); return false;}
        if (mapStringField != null ? !mapStringField.equals(that.mapStringField) : that.mapStringField != null)
            {logger.log(Level.INFO,"mapStringField differs, this = " + mapStringField + ", other = " + that.mapStringField); return false;}
        if (mapFileField != null ? !mapFileField.equals(that.mapFileField) : that.mapFileField != null)
            {logger.log(Level.INFO,"mapFileField differs, this = " + mapFileField + ", other = " + that.mapFileField); return false;}
        if (mapConfigurableSubclassField != null ? !mapConfigurableSubclassField.equals(that.mapConfigurableSubclassField) : that.mapConfigurableSubclassField != null)
            {logger.log(Level.INFO,"mapConfigurableSubclassField differs, this = " + mapConfigurableSubclassField + ", other = " + that.mapConfigurableSubclassField); return false;}
        if (enumSetField != null ? !enumSetField.equals(that.enumSetField) : that.enumSetField != null) {logger.log(Level.INFO,"enumSetField differs, this = " + enumSetField + ", other = " + that.enumSetField); return false;}
        if (atomicIntegerField != null ? atomicIntegerField.intValue() != that.atomicIntegerField.intValue() : that.atomicIntegerField != null)
            {logger.log(Level.INFO,"atomicIntegerField differs, this = " + atomicIntegerField + ", other = " + that.atomicIntegerField); return false;}
        if (atomicLongField != null ? atomicLongField.longValue() != that.atomicLongField.longValue() : that.atomicLongField != null)
            {logger.log(Level.INFO,"atomicLongField differs, this = " + atomicLongField + ", other = " + that.atomicLongField); return false;}
        if (fileField != null ? !fileField.equals(that.fileField) : that.fileField != null) {logger.log(Level.INFO,"fileField differs, this = " + fileField + ", other = " + that.fileField); return false;}
        if (pathField != null ? !pathField.equals(that.pathField) : that.pathField != null) {logger.log(Level.INFO,"pathField differs, this = " + pathField + ", other = " + that.pathField); return false;}
        /* java.util.Random doesn't implement equals.
         * if (listRandomField != null ? !listRandomField.equals(that.listRandomField) : that.listRandomField != null)
         * {logger.log(Level.INFO,"listRandomField differs, this = " + listRandomField + ", other = " + that.listRandomField); return false;}
         * if (randomField != null ? !randomField.equals(that.randomField) : that.randomField != null) {logger.log(Level.INFO,"randomField differs, this = " + randomField + ", other = " + that.randomField); return false;}
         */
        return enumField != null ? enumField.equals(that.enumField) : that.enumField == null;
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
        /* java.util.Random doesn't do hashCode consistent with equals either.
        result = 31 * result + (listRandomField != null ? listRandomField.hashCode() : 0);
        result = 31 * result + (randomField != null ? randomField.hashCode() : 0);
        */
        result = 31 * result + (enumField != null ? enumField.hashCode() : 0);
        return result;
    }
}
