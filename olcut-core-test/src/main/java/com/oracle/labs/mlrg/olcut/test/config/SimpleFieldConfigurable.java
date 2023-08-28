/*
 * Copyright (c) 2004-2020, Oracle and/or its affiliates.
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

package com.oracle.labs.mlrg.olcut.test.config;

import com.oracle.labs.mlrg.olcut.config.Config;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurableName;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SimpleFieldConfigurable implements Configurable {
    private static final Logger logger = Logger.getLogger(SimpleFieldConfigurable.class.getName());

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleFieldConfigurable that = (SimpleFieldConfigurable) o;

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
        return true;
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
        return result;
    }
}
