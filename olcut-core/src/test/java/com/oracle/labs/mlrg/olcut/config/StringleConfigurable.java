package com.oracle.labs.mlrg.olcut.config;

/**
 *
 */
public class StringleConfigurable extends StringConfigurable {

    @Config
    String four = "";

    @Config
    String five = "";

    public StringleConfigurable() {}

    public StringleConfigurable(String one, String two, String three, String four, String five) {
        super(one,two,three);
        this.four = four;
        this.five = five;
    }

    @Override
    public String toString() {
        return "StringleConfigurable{" + "one=" + one + ", two=" + two + ", three=" + three + ", four=" + four + ", five=" +five + '}';
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StringleConfigurable) {
            StringleConfigurable sc = (StringleConfigurable) other;
            return one.equals(sc.one) && two.equals(sc.two) && three.equals(sc.three) && four.equals(sc.four) && five.equals(sc.five);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = one.hashCode();
        result = 31 * result + two.hashCode();
        result = 31 * result + three.hashCode();
        result = 31 * result + four.hashCode();
        result = 31 * result + five.hashCode();
        return result;
    }
}
