package com.oracle.labs.mlrg.olcut.provenance.io;

import com.oracle.labs.mlrg.olcut.provenance.ObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.PrimitiveProvenance;
import com.oracle.labs.mlrg.olcut.provenance.Provenance;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceException;
import com.oracle.labs.mlrg.olcut.provenance.ProvenanceUtil.HashType;
import com.oracle.labs.mlrg.olcut.provenance.primitives.BooleanProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.ByteProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.CharProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DateTimeProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.DoubleProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.EnumProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.FloatProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.HashProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.IntProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.LongProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.ShortProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.StringProvenance;
import com.oracle.labs.mlrg.olcut.provenance.primitives.URIProvenance;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 *
 */
public final class SimpleMarshalledProvenance implements FlatMarshalledProvenance {

    private final String key;

    private final String value;

    private final String additional;

    private final String provenanceClassName;

    private final boolean isReference;

    public <E extends Enum> SimpleMarshalledProvenance(EnumProvenance<E> enumProv) {
        this(enumProv.getKey(), enumProv.getValue().toString(), enumProv.getClass().getName(), false, enumProv.getEnumClass());
    }

    public SimpleMarshalledProvenance(HashProvenance provenance) {
        this(provenance.getKey(), provenance.getValue(), provenance.getClass().getName(), false, provenance.getType().toString());
    }

    public <T> SimpleMarshalledProvenance(PrimitiveProvenance<T> provenance) {
        this(provenance.getKey(), provenance.getValue().toString(), provenance.getClass().getName(), false, "");
    }

    public SimpleMarshalledProvenance(String key, String value, ObjectProvenance provenance) {
        this(key, value, provenance.getClass().getName(), true, "");
    }

    private SimpleMarshalledProvenance(String key, String value, String provenanceClassName, boolean isReference, String additional) {
        this.key = key;
        this.value = value;
        this.provenanceClassName = provenanceClassName;
        this.isReference = isReference;
        this.additional = additional;
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
            throw new ProvenanceException("Attempted to unmarshall a reference via `unmarshallPrimitive`");
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
            } else if (provClass.equals(DateTimeProvenance.class)) {
                unmarshalled = new DateTimeProvenance(key, OffsetDateTime.parse(value));
            } else if (provClass.equals(DoubleProvenance.class)) {
                unmarshalled = new DoubleProvenance(key,Double.parseDouble(value));
            } else if (provClass.equals(EnumProvenance.class)) {
                Class<? extends Enum> enumClass = (Class<? extends Enum>) Class.forName(additional);
                Enum<? extends Enum> enumValue = Enum.valueOf(enumClass,value);
                unmarshalled = new EnumProvenance<>(key,enumValue);
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
            } else if (provClass.equals(URIProvenance.class)) {
                unmarshalled = new URIProvenance(key,new URI(value));
            } else {
                throw new ProvenanceException("Unknown Provenance subclass, found " + provClass.getName());
            }
            return unmarshalled;
        } catch (NumberFormatException e) {
            throw new ProvenanceException("Failed to parse number for provenance " + key + ".", e);
        } catch (IllegalArgumentException e) {
            throw new ProvenanceException("Failed to parse enum constant for provenance " + key + ".", e);
        } catch (URISyntaxException e) {
            throw new ProvenanceException("Found invalid URI when unmarshalling provenance with key " + key + ".", e);
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
