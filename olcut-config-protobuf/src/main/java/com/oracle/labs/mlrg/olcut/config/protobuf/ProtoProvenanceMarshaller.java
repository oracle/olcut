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

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.ListProvenanceProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.MapProvenanceProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.ObjectProvenanceProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.RootProvenanceProto;
import com.oracle.labs.mlrg.olcut.config.protobuf.protos.SimpleProvenanceProto;
import com.oracle.labs.mlrg.olcut.provenance.io.FlatMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ListMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.MapMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.ObjectMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.provenance.io.SimpleMarshalledProvenance;
import com.oracle.labs.mlrg.olcut.util.MutableLong;
import com.oracle.labs.mlrg.olcut.util.Pair;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for serializing and deserializing provenances to/from protobufs.
 * <p>
 * Uses {@link Base64} to encode and decode binary protobufs into and out of Strings if requested, though it
 * is preferable to use protobuf's built-in text format.
 */
public final class ProtoProvenanceMarshaller {

    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();

    private final boolean textFormat;

    /**
     * Construct a ProtoProvenanceMarshaller.
     *
     * @param textFormat Output a pbtxt.
     */
    public ProtoProvenanceMarshaller(boolean textFormat) {
        this.textFormat = textFormat;
    }

    public String getFileExtension() {
        return textFormat ? "pbtxt" : "pb";
    }

    public List<ObjectMarshalledProvenance> deserializeFromFile(Path path) throws IOException {
        try {
            InputStream is = Files.newInputStream(path);
            RootProvenanceProto proto;
            if (textFormat) {
                RootProvenanceProto.Builder protoBuilder = RootProvenanceProto.newBuilder();
                TextFormat.getParser().merge(new InputStreamReader(is,StandardCharsets.UTF_8), protoBuilder);
                proto = protoBuilder.build();
            } else {
                proto = RootProvenanceProto.parseFrom(is);
            }
            return deserializeFromProto(proto);
        } catch (InvalidProtocolBufferException | TextFormat.ParseException e) {
            throw new IllegalArgumentException("Failed to parse protobuf",e);
        }
    }

    public List<ObjectMarshalledProvenance> deserializeFromString(String input) {
        try {
            RootProvenanceProto proto;
            if (textFormat) {
                RootProvenanceProto.Builder protoBuilder = RootProvenanceProto.newBuilder();
                TextFormat.getParser().merge(input, protoBuilder);
                proto = protoBuilder.build();
            } else {
                byte[] bytes = base64Decoder.decode(input);
                proto = RootProvenanceProto.parseFrom(bytes);
            }
            return deserializeFromProto(proto);
        } catch (InvalidProtocolBufferException | TextFormat.ParseException e) {
            throw new IllegalArgumentException("Failed to parse protobuf",e);
        }
    }

    /**
     * Deserialize the marshalled provenances from the supplied protobuf.
     * @param proto The protobuf to deserialize.
     * @return The marshalled provenances encoded in the protobuf.
     */
    public List<ObjectMarshalledProvenance> deserializeFromProto(RootProvenanceProto proto) {
        int totalProtos = proto.getLmpCount() + proto.getMmpCount() + proto.getOmpCount() + proto.getSmpCount();
        Message[] messages = new Message[totalProtos];
        // Unpack into map
        for (ObjectProvenanceProto p : proto.getOmpList()) {
            int curIndex = p.getIndex();
            if (messages[curIndex] != null) {
                throw new IllegalArgumentException("Invalid protobuf found, index " + curIndex + " collided, found '" + p.toString() + " and " + messages[curIndex].toString());
            } else {
                messages[curIndex] = p;
            }
        }
        for (SimpleProvenanceProto p : proto.getSmpList()) {
            int curIndex = p.getIndex();
            if (messages[curIndex] != null) {
                throw new IllegalArgumentException("Invalid protobuf found, index " + curIndex + " collided, found '" + p.toString() + " and " + messages[curIndex].toString());
            } else {
                messages[curIndex] = p;
            }
        }
        for (MapProvenanceProto p : proto.getMmpList()) {
            int curIndex = p.getIndex();
            if (messages[curIndex] != null) {
                throw new IllegalArgumentException("Invalid protobuf found, index " + curIndex + " collided, found '" + p.toString() + " and " + messages[curIndex].toString());
            } else {
                messages[curIndex] = p;
            }
        }
        for (ListProvenanceProto p : proto.getLmpList()) {
            int curIndex = p.getIndex();
            if (messages[curIndex] != null) {
                throw new IllegalArgumentException("Invalid protobuf found, index " + curIndex + " collided, found '" + p.toString() + " and " + messages[curIndex].toString());
            } else {
                messages[curIndex] = p;
            }
        }

        List<ObjectMarshalledProvenance> outputList = new ArrayList<>();

        // Deserialize the ObjectMarshalledProvenances
        for (ObjectProvenanceProto p : proto.getOmpList()) {
            Map<String,FlatMarshalledProvenance> provMap = new HashMap<>();
            for (Map.Entry<String,Integer> e : p.getValuesMap().entrySet()) {
                FlatMarshalledProvenance fmp = dispatchMessage(messages,e.getValue());
                provMap.put(e.getKey(),fmp);
            }

            ObjectMarshalledProvenance omp = new ObjectMarshalledProvenance(p.getObjectName(),provMap,p.getObjectClassName(),p.getProvenanceClassName());
            outputList.add(omp);
        }

        return outputList;
    }

    /**
     * Decode the FlatMarshalledProvenance specified by this message index.
     * @param messages The messages.
     * @param index The current index.
     * @return The FlatMarshalledProvenance specified by the index.
     */
    private static FlatMarshalledProvenance dispatchMessage(Message[] messages, int index) {
        Message curMessage = messages[index];
        if (curMessage instanceof SimpleProvenanceProto) {
            return decodeSMP((SimpleProvenanceProto) curMessage);
        } else if (curMessage instanceof ListProvenanceProto) {
            return decodeLMP(messages, (ListProvenanceProto) curMessage);
        } else if (curMessage instanceof MapProvenanceProto) {
            return decodeMMP(messages, (MapProvenanceProto) curMessage);
        } else {
            throw new IllegalStateException("Invalid protobuf, a message index points to an ObjectMarshalledProvenance");
        }
    }

    /**
     * Decodes a SimpleProvenanceProto into a SimpleMarshalledProvenance.
     * @param proto The proto to decode.
     * @return The decoded provenance.
     */
    private static SimpleMarshalledProvenance decodeSMP(SimpleProvenanceProto proto) {
        return new SimpleMarshalledProvenance(proto.getKey(),proto.getValue(),proto.getProvenanceClassName(),proto.getIsReference(),proto.getAdditional());
    }

    /**
     * Decodes a ListProvenanceProto into a ListMarshalledProvenance.
     * @param messages The messages.
     * @param proto The proto to decode.
     * @return The decoded list provenance.
     */
    private static ListMarshalledProvenance decodeLMP(Message[] messages, ListProvenanceProto proto) {
        List<FlatMarshalledProvenance> list = new ArrayList<>();

        for (Integer i : proto.getValuesList()) {
            list.add(dispatchMessage(messages,i));
        }

        return new ListMarshalledProvenance(list);
    }

    /**
     * Decodes a MapProvenanceProto into a MapMarshalledProvenance.
     * @param messages The messages.
     * @param proto The proto to decode.
     * @return The decoded map provenance.
     */
    private static MapMarshalledProvenance decodeMMP(Message[] messages, MapProvenanceProto proto) {
        Map<String,FlatMarshalledProvenance> map = new HashMap<>();

        for (Map.Entry<String,Integer> e : proto.getValuesMap().entrySet()) {
            map.put(e.getKey(), dispatchMessage(messages,e.getValue()));
        }

        return new MapMarshalledProvenance(map);
    }

    /**
     * Serialize the marshalled provenances into a protobuf.
     * @param marshalledProvenances The provenances to serialize.
     * @return A protobuf encoding all the provenances.
     */
    public RootProvenanceProto serializeToProto(List<ObjectMarshalledProvenance> marshalledProvenances) {
        RootProvenanceProto.Builder builder = RootProvenanceProto.newBuilder();

        // linearizing the provenances
        MutableLong counter = new MutableLong(0);
        for (ObjectMarshalledProvenance omp : marshalledProvenances) {
            convertProvenance(builder, counter, omp);
        }

        return builder.build();
    }

    /**
     * Encodes the ObjectMarshalledProvenance into the RootProvenanceProto.
     * @param builder The proto builder.
     * @param counter The index counter.
     * @param omp The provenance to encode.
     */
    private static void convertProvenance(RootProvenanceProto.Builder builder, MutableLong counter, ObjectMarshalledProvenance omp) {
        ObjectProvenanceProto.Builder ompBuilder = ObjectProvenanceProto.newBuilder();

        ompBuilder.setIndex(counter.intValue());
        counter.increment();

        ompBuilder.setObjectName(omp.getName());
        ompBuilder.setObjectClassName(omp.getObjectClassName());
        ompBuilder.setProvenanceClassName(omp.getProvenanceClassName());

        for (Map.Entry<String, FlatMarshalledProvenance> e : omp.getMap().entrySet()) {
            int count = dispatchFMP(builder, counter, e.getValue());
            ompBuilder.putValues(e.getKey(),count);
        }

        builder.addOmp(ompBuilder.build());
    }

    /**
     * Dispatch on the type of FlatMarshalledProvenance.
     * @param builder The root proto builder.
     * @param counter The provenance counter.
     * @param fmp The FMP to dispatch on.
     */
    private static int dispatchFMP(RootProvenanceProto.Builder builder, MutableLong counter, FlatMarshalledProvenance fmp) {
        if (fmp instanceof SimpleMarshalledProvenance) {
            SimpleMarshalledProvenance smp = (SimpleMarshalledProvenance) fmp;
            return encodeSMP(builder,counter,smp);
        } else if (fmp instanceof ListMarshalledProvenance) {
            ListMarshalledProvenance lmp = (ListMarshalledProvenance) fmp;
            return encodeLMP(builder,counter,lmp);
        } else if (fmp instanceof MapMarshalledProvenance) {
            MapMarshalledProvenance mmp = (MapMarshalledProvenance) fmp;
            return encodeMMP(builder,counter,mmp);
        } else {
            throw new RuntimeException("Should not reach here, unexpected FlatMarshalledProvenance subclass " + fmp.getClass());
        }
    }

    /**
     * Encodes the SimpleMarshalledProvenance into the RootProvenanceProto.
     * @param builder The proto builder.
     * @param counter The index counter.
     * @param smp The provenance to encode.
     * @return The index of this SimpleMarshalledProvenance
     */
    private static int encodeSMP(RootProvenanceProto.Builder builder, MutableLong counter, SimpleMarshalledProvenance smp) {
        SimpleProvenanceProto.Builder smpBuilder = SimpleProvenanceProto.newBuilder();

        int curIndex = counter.intValue();
        smpBuilder.setIndex(curIndex);
        counter.increment();

        smpBuilder.setKey(smp.getKey());
        smpBuilder.setValue(smp.getValue());
        smpBuilder.setAdditional(smp.getAdditional());
        smpBuilder.setProvenanceClassName(smp.getProvenanceClassName());
        smpBuilder.setIsReference(smp.isReference());

        builder.addSmp(smpBuilder.build());
        return curIndex;
    }

    /**
     * Encodes the ListMarshalledProvenance into the RootProvenanceProto.
     * @param builder The proto builder.
     * @param counter The index counter.
     * @param lmp The provenance to encode.
     * @return The index of this ListMarshalledProvenance
     */
    private static int encodeLMP(RootProvenanceProto.Builder builder, MutableLong counter, ListMarshalledProvenance lmp) {
        ListProvenanceProto.Builder lmpBuilder = ListProvenanceProto.newBuilder();

        int curIndex = counter.intValue();
        lmpBuilder.setIndex(curIndex);
        counter.increment();

        for (FlatMarshalledProvenance fmp : lmp) {
            lmpBuilder.addValues(dispatchFMP(builder,counter,fmp));
        }

        builder.addLmp(lmpBuilder.build());
        return curIndex;
    }

    /**
     * Encodes the MapMarshalledProvenance into the RootProvenanceProto.
     * @param builder The proto builder.
     * @param counter The index counter.
     * @param mmp The provenance to encode.
     * @return The index of this MapMarshalledProvenance
     */
    private static int encodeMMP(RootProvenanceProto.Builder builder, MutableLong counter, MapMarshalledProvenance mmp) {
        MapProvenanceProto.Builder mmpBuilder = MapProvenanceProto.newBuilder();

        int curIndex = counter.intValue();
        mmpBuilder.setIndex(curIndex);
        counter.increment();

        for (Pair<String,FlatMarshalledProvenance> p : mmp) {
            mmpBuilder.putValues(p.getA(),dispatchFMP(builder,counter,p.getB()));
        }

        builder.addMmp(mmpBuilder.build());
        return curIndex;
    }

    public String serializeToString(List<ObjectMarshalledProvenance> marshalledProvenances) {
        RootProvenanceProto proto = serializeToProto(marshalledProvenances);

        if (textFormat) {
            return proto.toString();
        } else {
            return base64Encoder.encodeToString(proto.toByteArray());
        }
    }

    public void serializeToFile(List<ObjectMarshalledProvenance> marshalledProvenances, Path path) throws IOException {
        RootProvenanceProto proto = serializeToProto(marshalledProvenances);

        if (textFormat) {
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                writer.println(proto.toString());
            }
        } else {
            try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(path))) {
                proto.writeTo(bos);
            }
        }
    }
}