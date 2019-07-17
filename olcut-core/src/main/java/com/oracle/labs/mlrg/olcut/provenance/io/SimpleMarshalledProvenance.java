package com.oracle.labs.mlrg.olcut.provenance.io;

import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceException;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil.HashType;
import com.oracle.labs.mlrg.olcut.provenance.primitives.BooleanProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.ByteProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.CharProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DateProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DateTimeProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DoubleProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.EnumProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.FileProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.FloatProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.HashProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.IntProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.LongProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.ShortProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.StringProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.TimeProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.URLProvenance;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Objects;

/**
 * A marshalled provenance representing a primitive type, or a reference to
 * another {@link ObjectMarshalledProvenance} in the marshalled object stream.
 *
 * If the {@link PrimitiveProvenance} requires extra information beyond it's
 * key and value, this class must be updated to have a specific constructor
 * for that type.
 */
public final class SimpleMarshalledProvenance implements FlatMarshalledProvenance {

    private final String key;

    private final String value;

    private final String additional;

    private final String provenanceClassName;

    private final boolean isReference;

    /**
     * Constructs a SimpleMarshalledProvenance from an enum provenance,
     * storing the enum class.
     * @param enumProv The enum provenance to store.
     * @param <E> The type of the enum.
     */
    public <E extends Enum> SimpleMarshalledProvenance(EnumProvenance<E> enumProv) {
        this(enumProv.getKey(), enumProv.getValue().toString(), enumProv.getClass().getName(), false, enumProv.getEnumClass());
    }

    /**
     * Constructs a SimpleMarshalledProvenance from a hash provenance,
     * storing the hash type.
     * @param provenance A hash provenance.
     */
    public SimpleMarshalledProvenance(HashProvenance provenance) {
        this(provenance.getKey(), provenance.getValue(), provenance.getClass().getName(), false, provenance.getType().toString());
    }

    /**
     * Constructs a SimpleMarshalledProvenance from a PrimitiveProvenance.
     * @param provenance The provenance.
     * @param <T> The type of primitive.
     */
    public <T> SimpleMarshalledProvenance(PrimitiveProvenance<T> provenance) {
        this(provenance.getKey(), provenance.getValue().toString(), provenance.getClass().getName(), false, "");
    }

    /**
     * Constructs a SimpleMarshalledProvenance which refers to the specified
     * ObjectProvenance. The value must match the name given to that
     * object in the current marshalled object stream.
     * @param key The key.
     * @param value The name of the provenance object in the marshalled object stream.
     * @param provenance The provenance object.
     */
    public SimpleMarshalledProvenance(String key, String value, ObjectProvenance provenance) {
        this(key, value, provenance.getClass().getName(), true, "");
    }

    /**
     * Used for deserialisation.
     * @param key The key.
     * @param value The value of this provenance.
     * @param provenanceClassName The class name of the provenance.
     * @param isReference Is this object a reference to another provenance in the stream.
     * @param additional Any additional information like hash type or enum class.
     */
    public SimpleMarshalledProvenance(String key, String value, String provenanceClassName, boolean isReference, String additional) {
        this.key = key;
        this.value = value;
        this.provenanceClassName = provenanceClassName;
        this.isReference = isReference;
        this.additional = additional;
    }

    @Override
    public String toString() {
        return "SimpleMarshalledProvenance{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", additional='" + additional + '\'' +
                ", provenanceClassName='" + provenanceClassName + '\'' +
                ", isReference=" + isReference +
                '}';
    }

    /**
     * Only unmarshalls the Provenance if it's a PrimitiveProvenance,
     * throws ProvenanceException if it stores a reference to an ObjectProvenance.
     * @param <T> The type of the PrimitiveProvenance.
     * @return A PrimitiveProvenance instance.
     */
    @SuppressWarnings("unchecked")//Suppressing enum casting warnings.
    public <T> PrimitiveProvenance<T> unmarshallPrimitive() {
        if (isReference) {
            throw new ProvenanceException("Attempted to unmarshall a reference via 'unmarshallPrimitive'");
        }
        try {
            Class<?> provClass = Class.forName(provenanceClassName);

            PrimitiveProvenance unmarshalled;
            if (provClass.equals(BooleanProvenance.class)) {
                unmarshalled = new BooleanProvenance(key,Boolean.parseBoolean(value));
            } else if (provClass.equals(ByteProvenance.class)) {
                unmarshalled = new ByteProvenance(key,Byte.parseByte(value));
            } else if (provClass.equals(CharProvenance.class)) {
                unmarshalled = new CharProvenance(key,value.charAt(0));
            } else if (provClass.equals(DateProvenance.class)) {
                unmarshalled = new DateProvenance(key, LocalDate.parse(value));
            } else if (provClass.equals(DateTimeProvenance.class)) {
                unmarshalled = new DateTimeProvenance(key, OffsetDateTime.parse(value));
            } else if (provClass.equals(DoubleProvenance.class)) {
                unmarshalled = new DoubleProvenance(key,Double.parseDouble(value));
            } else if (provClass.equals(EnumProvenance.class)) {
                Class<? extends Enum> enumClass = (Class<? extends Enum>) Class.forName(additional);
                Enum<? extends Enum> enumValue = Enum.valueOf(enumClass,value);
                unmarshalled = new EnumProvenance<>(key,enumValue);
            } else if (provClass.equals(FileProvenance.class)) {
                unmarshalled = new FileProvenance(key,new File(value));
            } else if (provClass.equals(FloatProvenance.class)) {
                unmarshalled = new FloatProvenance(key,Float.parseFloat(value));
            } else if (provClass.equals(HashProvenance.class)) {
                unmarshalled = new HashProvenance(HashType.valueOf(additional),key,value);
            } else if (provClass.equals(IntProvenance.class)) {
                unmarshalled = new IntProvenance(key,Integer.parseInt(value));
            } else if (provClass.equals(LongProvenance.class)) {
                unmarshalled = new LongProvenance(key,Long.parseLong(value));
            } else if (provClass.equals(ShortProvenance.class)) {
                unmarshalled = new ShortProvenance(key,Short.parseShort(value));
            } else if (provClass.equals(StringProvenance.class)) {
                unmarshalled = new StringProvenance(key,value);
            } else if (provClass.equals(TimeProvenance.class)) {
                unmarshalled = new TimeProvenance(key, OffsetTime.parse(value));
            } else if (provClass.equals(URLProvenance.class)) {
                unmarshalled = new URLProvenance(key,new URL(value));
            } else {
                throw new ProvenanceException("Unknown Provenance subclass, found " + provClass.getName());
            }
            return unmarshalled;
        } catch (MalformedURLException e) {
            throw new ProvenanceException("Failed to parse url for provenance " + key + ".", e);
        } catch (NumberFormatException e) {
            throw new ProvenanceException("Failed to parse number for provenance " + key + ".", e);
        } catch (IllegalArgumentException e) {
            throw new ProvenanceException("Failed to parse enum constant for provenance " + key + ".", e);
        } catch (ClassNotFoundException e) {
            throw new ProvenanceException("Failed to load class for " + provenanceClassName, e);
        }
    }

    public boolean isReference() {
        return isReference;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getProvenanceClassName() {
        return provenanceClassName;
    }

    public String getAdditional() {
        return additional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleMarshalledProvenance)) return false;
        SimpleMarshalledProvenance that = (SimpleMarshalledProvenance) o;
        return isReference == that.isReference &&
                key.equals(that.key) &&
                value.equals(that.value) &&
                provenanceClassName.equals(that.provenanceClassName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, provenanceClassName);
    }
}
