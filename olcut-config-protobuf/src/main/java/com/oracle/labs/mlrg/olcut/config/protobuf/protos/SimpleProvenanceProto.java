// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: olcut_proto.proto

// Protobuf Java Version: 3.25.5
package com.oracle.labs.mlrg.olcut.config.protobuf.protos;

/**
 * <pre>
 *
 *A SimpleMarshalledProvenance proto.
 * </pre>
 *
 * Protobuf type {@code olcut.SimpleProvenanceProto}
 */
public final class SimpleProvenanceProto extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:olcut.SimpleProvenanceProto)
    SimpleProvenanceProtoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SimpleProvenanceProto.newBuilder() to construct.
  private SimpleProvenanceProto(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SimpleProvenanceProto() {
    key_ = "";
    value_ = "";
    additional_ = "";
    provenanceClassName_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SimpleProvenanceProto();
  }

  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.oracle.labs.mlrg.olcut.config.protobuf.protos.OlcutProto.internal_static_olcut_SimpleProvenanceProto_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.oracle.labs.mlrg.olcut.config.protobuf.protos.OlcutProto.internal_static_olcut_SimpleProvenanceProto_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto.class, com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto.Builder.class);
  }

  public static final int INDEX_FIELD_NUMBER = 1;
  private int index_ = 0;
  /**
   * <code>int32 index = 1;</code>
   * @return The index.
   */
  @java.lang.Override
  public int getIndex() {
    return index_;
  }

  public static final int KEY_FIELD_NUMBER = 2;
  @SuppressWarnings("serial")
  private volatile java.lang.Object key_ = "";
  /**
   * <code>string key = 2;</code>
   * @return The key.
   */
  @java.lang.Override
  public java.lang.String getKey() {
    java.lang.Object ref = key_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      key_ = s;
      return s;
    }
  }
  /**
   * <code>string key = 2;</code>
   * @return The bytes for key.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getKeyBytes() {
    java.lang.Object ref = key_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      key_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int VALUE_FIELD_NUMBER = 3;
  @SuppressWarnings("serial")
  private volatile java.lang.Object value_ = "";
  /**
   * <code>string value = 3;</code>
   * @return The value.
   */
  @java.lang.Override
  public java.lang.String getValue() {
    java.lang.Object ref = value_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      value_ = s;
      return s;
    }
  }
  /**
   * <code>string value = 3;</code>
   * @return The bytes for value.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getValueBytes() {
    java.lang.Object ref = value_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      value_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ADDITIONAL_FIELD_NUMBER = 4;
  @SuppressWarnings("serial")
  private volatile java.lang.Object additional_ = "";
  /**
   * <code>string additional = 4;</code>
   * @return The additional.
   */
  @java.lang.Override
  public java.lang.String getAdditional() {
    java.lang.Object ref = additional_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      additional_ = s;
      return s;
    }
  }
  /**
   * <code>string additional = 4;</code>
   * @return The bytes for additional.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getAdditionalBytes() {
    java.lang.Object ref = additional_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      additional_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int PROVENANCE_CLASS_NAME_FIELD_NUMBER = 5;
  @SuppressWarnings("serial")
  private volatile java.lang.Object provenanceClassName_ = "";
  /**
   * <code>string provenance_class_name = 5;</code>
   * @return The provenanceClassName.
   */
  @java.lang.Override
  public java.lang.String getProvenanceClassName() {
    java.lang.Object ref = provenanceClassName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      provenanceClassName_ = s;
      return s;
    }
  }
  /**
   * <code>string provenance_class_name = 5;</code>
   * @return The bytes for provenanceClassName.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getProvenanceClassNameBytes() {
    java.lang.Object ref = provenanceClassName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      provenanceClassName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int IS_REFERENCE_FIELD_NUMBER = 6;
  private boolean isReference_ = false;
  /**
   * <code>bool is_reference = 6;</code>
   * @return The isReference.
   */
  @java.lang.Override
  public boolean getIsReference() {
    return isReference_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (index_ != 0) {
      output.writeInt32(1, index_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(key_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, key_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(value_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, value_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(additional_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, additional_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(provenanceClassName_)) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, provenanceClassName_);
    }
    if (isReference_ != false) {
      output.writeBool(6, isReference_);
    }
    getUnknownFields().writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (index_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, index_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(key_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, key_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(value_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, value_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(additional_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, additional_);
    }
    if (!com.google.protobuf.GeneratedMessageV3.isStringEmpty(provenanceClassName_)) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, provenanceClassName_);
    }
    if (isReference_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(6, isReference_);
    }
    size += getUnknownFields().getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto)) {
      return super.equals(obj);
    }
    com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto other = (com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto) obj;

    if (getIndex()
        != other.getIndex()) return false;
    if (!getKey()
        .equals(other.getKey())) return false;
    if (!getValue()
        .equals(other.getValue())) return false;
    if (!getAdditional()
        .equals(other.getAdditional())) return false;
    if (!getProvenanceClassName()
        .equals(other.getProvenanceClassName())) return false;
    if (getIsReference()
        != other.getIsReference()) return false;
    if (!getUnknownFields().equals(other.getUnknownFields())) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + INDEX_FIELD_NUMBER;
    hash = (53 * hash) + getIndex();
    hash = (37 * hash) + KEY_FIELD_NUMBER;
    hash = (53 * hash) + getKey().hashCode();
    hash = (37 * hash) + VALUE_FIELD_NUMBER;
    hash = (53 * hash) + getValue().hashCode();
    hash = (37 * hash) + ADDITIONAL_FIELD_NUMBER;
    hash = (53 * hash) + getAdditional().hashCode();
    hash = (37 * hash) + PROVENANCE_CLASS_NAME_FIELD_NUMBER;
    hash = (53 * hash) + getProvenanceClassName().hashCode();
    hash = (37 * hash) + IS_REFERENCE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getIsReference());
    hash = (29 * hash) + getUnknownFields().hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }

  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * <pre>
   *
   *A SimpleMarshalledProvenance proto.
   * </pre>
   *
   * Protobuf type {@code olcut.SimpleProvenanceProto}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:olcut.SimpleProvenanceProto)
      com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProtoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.oracle.labs.mlrg.olcut.config.protobuf.protos.OlcutProto.internal_static_olcut_SimpleProvenanceProto_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.oracle.labs.mlrg.olcut.config.protobuf.protos.OlcutProto.internal_static_olcut_SimpleProvenanceProto_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto.class, com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto.Builder.class);
    }

    // Construct using com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto.newBuilder()
    private Builder() {

    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);

    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      bitField0_ = 0;
      index_ = 0;
      key_ = "";
      value_ = "";
      additional_ = "";
      provenanceClassName_ = "";
      isReference_ = false;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.oracle.labs.mlrg.olcut.config.protobuf.protos.OlcutProto.internal_static_olcut_SimpleProvenanceProto_descriptor;
    }

    @java.lang.Override
    public com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto getDefaultInstanceForType() {
      return com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto.getDefaultInstance();
    }

    @java.lang.Override
    public com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto build() {
      com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto buildPartial() {
      com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto result = new com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto(this);
      if (bitField0_ != 0) { buildPartial0(result); }
      onBuilt();
      return result;
    }

    private void buildPartial0(com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto result) {
      int from_bitField0_ = bitField0_;
      if (((from_bitField0_ & 0x00000001) != 0)) {
        result.index_ = index_;
      }
      if (((from_bitField0_ & 0x00000002) != 0)) {
        result.key_ = key_;
      }
      if (((from_bitField0_ & 0x00000004) != 0)) {
        result.value_ = value_;
      }
      if (((from_bitField0_ & 0x00000008) != 0)) {
        result.additional_ = additional_;
      }
      if (((from_bitField0_ & 0x00000010) != 0)) {
        result.provenanceClassName_ = provenanceClassName_;
      }
      if (((from_bitField0_ & 0x00000020) != 0)) {
        result.isReference_ = isReference_;
      }
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto) {
        return mergeFrom((com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto other) {
      if (other == com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto.getDefaultInstance()) return this;
      if (other.getIndex() != 0) {
        setIndex(other.getIndex());
      }
      if (!other.getKey().isEmpty()) {
        key_ = other.key_;
        bitField0_ |= 0x00000002;
        onChanged();
      }
      if (!other.getValue().isEmpty()) {
        value_ = other.value_;
        bitField0_ |= 0x00000004;
        onChanged();
      }
      if (!other.getAdditional().isEmpty()) {
        additional_ = other.additional_;
        bitField0_ |= 0x00000008;
        onChanged();
      }
      if (!other.getProvenanceClassName().isEmpty()) {
        provenanceClassName_ = other.provenanceClassName_;
        bitField0_ |= 0x00000010;
        onChanged();
      }
      if (other.getIsReference() != false) {
        setIsReference(other.getIsReference());
      }
      this.mergeUnknownFields(other.getUnknownFields());
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              index_ = input.readInt32();
              bitField0_ |= 0x00000001;
              break;
            } // case 8
            case 18: {
              key_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000002;
              break;
            } // case 18
            case 26: {
              value_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000004;
              break;
            } // case 26
            case 34: {
              additional_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000008;
              break;
            } // case 34
            case 42: {
              provenanceClassName_ = input.readStringRequireUtf8();
              bitField0_ |= 0x00000010;
              break;
            } // case 42
            case 48: {
              isReference_ = input.readBool();
              bitField0_ |= 0x00000020;
              break;
            } // case 48
            default: {
              if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                done = true; // was an endgroup tag
              }
              break;
            } // default:
          } // switch (tag)
        } // while (!done)
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.unwrapIOException();
      } finally {
        onChanged();
      } // finally
      return this;
    }
    private int bitField0_;

    private int index_ ;
    /**
     * <code>int32 index = 1;</code>
     * @return The index.
     */
    @java.lang.Override
    public int getIndex() {
      return index_;
    }
    /**
     * <code>int32 index = 1;</code>
     * @param value The index to set.
     * @return This builder for chaining.
     */
    public Builder setIndex(int value) {

      index_ = value;
      bitField0_ |= 0x00000001;
      onChanged();
      return this;
    }
    /**
     * <code>int32 index = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearIndex() {
      bitField0_ = (bitField0_ & ~0x00000001);
      index_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object key_ = "";
    /**
     * <code>string key = 2;</code>
     * @return The key.
     */
    public java.lang.String getKey() {
      java.lang.Object ref = key_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        key_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string key = 2;</code>
     * @return The bytes for key.
     */
    public com.google.protobuf.ByteString
        getKeyBytes() {
      java.lang.Object ref = key_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        key_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string key = 2;</code>
     * @param value The key to set.
     * @return This builder for chaining.
     */
    public Builder setKey(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      key_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }
    /**
     * <code>string key = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearKey() {
      key_ = getDefaultInstance().getKey();
      bitField0_ = (bitField0_ & ~0x00000002);
      onChanged();
      return this;
    }
    /**
     * <code>string key = 2;</code>
     * @param value The bytes for key to set.
     * @return This builder for chaining.
     */
    public Builder setKeyBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      key_ = value;
      bitField0_ |= 0x00000002;
      onChanged();
      return this;
    }

    private java.lang.Object value_ = "";
    /**
     * <code>string value = 3;</code>
     * @return The value.
     */
    public java.lang.String getValue() {
      java.lang.Object ref = value_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        value_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string value = 3;</code>
     * @return The bytes for value.
     */
    public com.google.protobuf.ByteString
        getValueBytes() {
      java.lang.Object ref = value_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        value_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string value = 3;</code>
     * @param value The value to set.
     * @return This builder for chaining.
     */
    public Builder setValue(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      value_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }
    /**
     * <code>string value = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearValue() {
      value_ = getDefaultInstance().getValue();
      bitField0_ = (bitField0_ & ~0x00000004);
      onChanged();
      return this;
    }
    /**
     * <code>string value = 3;</code>
     * @param value The bytes for value to set.
     * @return This builder for chaining.
     */
    public Builder setValueBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      value_ = value;
      bitField0_ |= 0x00000004;
      onChanged();
      return this;
    }

    private java.lang.Object additional_ = "";
    /**
     * <code>string additional = 4;</code>
     * @return The additional.
     */
    public java.lang.String getAdditional() {
      java.lang.Object ref = additional_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        additional_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string additional = 4;</code>
     * @return The bytes for additional.
     */
    public com.google.protobuf.ByteString
        getAdditionalBytes() {
      java.lang.Object ref = additional_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        additional_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string additional = 4;</code>
     * @param value The additional to set.
     * @return This builder for chaining.
     */
    public Builder setAdditional(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      additional_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }
    /**
     * <code>string additional = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearAdditional() {
      additional_ = getDefaultInstance().getAdditional();
      bitField0_ = (bitField0_ & ~0x00000008);
      onChanged();
      return this;
    }
    /**
     * <code>string additional = 4;</code>
     * @param value The bytes for additional to set.
     * @return This builder for chaining.
     */
    public Builder setAdditionalBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      additional_ = value;
      bitField0_ |= 0x00000008;
      onChanged();
      return this;
    }

    private java.lang.Object provenanceClassName_ = "";
    /**
     * <code>string provenance_class_name = 5;</code>
     * @return The provenanceClassName.
     */
    public java.lang.String getProvenanceClassName() {
      java.lang.Object ref = provenanceClassName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        provenanceClassName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string provenance_class_name = 5;</code>
     * @return The bytes for provenanceClassName.
     */
    public com.google.protobuf.ByteString
        getProvenanceClassNameBytes() {
      java.lang.Object ref = provenanceClassName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        provenanceClassName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string provenance_class_name = 5;</code>
     * @param value The provenanceClassName to set.
     * @return This builder for chaining.
     */
    public Builder setProvenanceClassName(
        java.lang.String value) {
      if (value == null) { throw new NullPointerException(); }
      provenanceClassName_ = value;
      bitField0_ |= 0x00000010;
      onChanged();
      return this;
    }
    /**
     * <code>string provenance_class_name = 5;</code>
     * @return This builder for chaining.
     */
    public Builder clearProvenanceClassName() {
      provenanceClassName_ = getDefaultInstance().getProvenanceClassName();
      bitField0_ = (bitField0_ & ~0x00000010);
      onChanged();
      return this;
    }
    /**
     * <code>string provenance_class_name = 5;</code>
     * @param value The bytes for provenanceClassName to set.
     * @return This builder for chaining.
     */
    public Builder setProvenanceClassNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) { throw new NullPointerException(); }
      checkByteStringIsUtf8(value);
      provenanceClassName_ = value;
      bitField0_ |= 0x00000010;
      onChanged();
      return this;
    }

    private boolean isReference_ ;
    /**
     * <code>bool is_reference = 6;</code>
     * @return The isReference.
     */
    @java.lang.Override
    public boolean getIsReference() {
      return isReference_;
    }
    /**
     * <code>bool is_reference = 6;</code>
     * @param value The isReference to set.
     * @return This builder for chaining.
     */
    public Builder setIsReference(boolean value) {

      isReference_ = value;
      bitField0_ |= 0x00000020;
      onChanged();
      return this;
    }
    /**
     * <code>bool is_reference = 6;</code>
     * @return This builder for chaining.
     */
    public Builder clearIsReference() {
      bitField0_ = (bitField0_ & ~0x00000020);
      isReference_ = false;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:olcut.SimpleProvenanceProto)
  }

  // @@protoc_insertion_point(class_scope:olcut.SimpleProvenanceProto)
  private static final com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto();
  }

  public static com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SimpleProvenanceProto>
      PARSER = new com.google.protobuf.AbstractParser<SimpleProvenanceProto>() {
    @java.lang.Override
    public SimpleProvenanceProto parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      Builder builder = newBuilder();
      try {
        builder.mergeFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(builder.buildPartial());
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(e)
            .setUnfinishedMessage(builder.buildPartial());
      }
      return builder.buildPartial();
    }
  };

  public static com.google.protobuf.Parser<SimpleProvenanceProto> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SimpleProvenanceProto> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

