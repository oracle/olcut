// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: olcut_proto.proto

// Protobuf Java Version: 3.25.5
package com.oracle.labs.mlrg.olcut.config.protobuf.protos;

public interface ListProvenanceProtoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:olcut.ListProvenanceProto)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 index = 1;</code>
   * @return The index.
   */
  int getIndex();

  /**
   * <code>repeated int32 values = 2;</code>
   * @return A list containing the values.
   */
  java.util.List<java.lang.Integer> getValuesList();
  /**
   * <code>repeated int32 values = 2;</code>
   * @return The count of values.
   */
  int getValuesCount();
  /**
   * <code>repeated int32 values = 2;</code>
   * @param index The index of the element to return.
   * @return The values at the given index.
   */
  int getValues(int index);
}
