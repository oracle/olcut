/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.util.props;

import java.util.EnumSet;

/**
 *
 */
public class EnumConfigurable implements Configurable {
    
    public enum Type { A, B, C, D, E, F};
    
    @ConfigEnum(type = EnumConfigurable.Type.class)
    public static final String PROP_ENUM1 = "enum1";

    @ConfigEnum(type = EnumConfigurable.Type.class, defaultValue = "A")
    public static final String PROP_ENUM2 = "enum2";

    @ConfigEnumSet(type = EnumConfigurable.Type.class, defaultList = {"A", "F"})
    public static final String PROP_ENUMSET1 = "enumSet1";

    Type enum1;
    
    Type enum2;

    EnumSet<Type> enumSet1;

    public void newProperties(PropertySheet ps) throws PropertyException {
        enum1 = (Type) ps.getEnum(PROP_ENUM1);
        enum2 = (Type) ps.getEnum(PROP_ENUM2);
        enumSet1 = (EnumSet<Type>) ps.getEnumSet(PROP_ENUMSET1);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final EnumConfigurable other = (EnumConfigurable) obj;
        if(this.enum1 != other.enum1) {
            return false;
        }
        if(this.enum2 != other.enum2) {
            return false;
        }
        if(this.enumSet1 != other.enumSet1 &&
                (this.enumSet1 == null || !this.enumSet1.equals(other.enumSet1))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.enum1 != null ? this.enum1.hashCode() : 0);
        hash = 89 * hash + (this.enum2 != null ? this.enum2.hashCode() : 0);
        hash = 89 * hash + (this.enumSet1 != null ? this.enumSet1.hashCode() : 0);
        return hash;
    }

    

}
