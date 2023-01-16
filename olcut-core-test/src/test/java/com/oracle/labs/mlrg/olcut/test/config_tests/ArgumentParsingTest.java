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

package com.oracle.labs.mlrg.olcut.test.config_tests;

import com.oracle.labs.mlrg.olcut.config.ArgumentException;
import com.oracle.labs.mlrg.olcut.config.Configurable;
import com.oracle.labs.mlrg.olcut.config.ConfigurationManager;
import com.oracle.labs.mlrg.olcut.config.Option;
import com.oracle.labs.mlrg.olcut.config.Options;
import com.oracle.labs.mlrg.olcut.config.PropertySheet;
import com.oracle.labs.mlrg.olcut.config.UsageException;
import com.oracle.labs.mlrg.olcut.test.config.StringConfigurable;
import com.oracle.labs.mlrg.olcut.test.config.StringListConfigurable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ArgumentParsingTest {

    @BeforeAll
    public static void setup() {
        Logger logger = Logger.getLogger(PropertySheet.class.getName());
        logger.setLevel(Level.SEVERE);
    }

    @Test
    public void testStringSplitting() {
        String unix = "nasty,input,string\\,with spaces,\"quoted,commas\",and\\,other things.";
        String win = "nasty,input,string^,with spaces,\"quoted,commas\",and^,other things.";
        String a = System.getProperty("os.name").toLowerCase().startsWith("windows") ? win : unix;
        List<String> expectedOutput = Arrays.asList("nasty", "input", "string,with spaces", "\"quoted,commas\"", "and,other things.");

        List<String> output = ConfigurationManager.parseStringList(a);
        assertEquals(expectedOutput, output, "String parsing failed");
    }

    @Test
    public void testConfigLoading() throws IOException {
        String moduleStub = this.getClass().getName() + "|";
        String[] args = new String[]{"-c", moduleStub + "stringListConfig.xml", "--other-arguments", "that-get-in-the-way"};
        loadFromArgs("-c", args);
        args = new String[]{"--config-file", moduleStub + "stringListConfig.xml", "--other-arguments", "that-get-in-the-way"};
        loadFromArgs("--config-file", args);
        args = new String[]{"-o", "--config-file", moduleStub + "stringListConfig.xml," + moduleStub + "componentListConfig.xml", "-s"};
        loadFromArgs("--config-file with multiple files", args);
        args = new String[]{"-o", "--config-file", moduleStub + "componentListConfig.xml", "-s", "-c", moduleStub + "stringListConfig.xml"};
        loadFromArgs("overriding --config-file with -c", args);
    }

    public void loadFromArgs(String name, String[] args) throws IOException {
        ParsingOptions o = new ParsingOptions();
        ConfigurationManager cm = new ConfigurationManager(args, o);
        StringListConfigurable slc = (StringListConfigurable) cm.lookup("listTest");
        Assertions.assertEquals("a", slc.strings.get(0), "Loading from " + name + " failed.");
        Assertions.assertEquals("b", slc.strings.get(1), "Loading from " + name + " failed.");
        Assertions.assertEquals("c", slc.strings.get(2), "Loading from " + name + " failed.");
    }

    @Test
    public void testConfigurableOverride() throws IOException {
        String[] args = new String[]{"-c", this.getClass().getName() + "|stringListConfig.xml", "--@listTest.strings", "alpha,beta,gamma"};
        ConfigurationManager cm = new ConfigurationManager(args);

        StringListConfigurable slc = (StringListConfigurable) cm.lookup("listTest");
        Assertions.assertEquals("alpha", slc.strings.get(0), "Configurable overriding failed.");
        Assertions.assertEquals("beta", slc.strings.get(1), "Configurable overriding failed.");
        Assertions.assertEquals("gamma", slc.strings.get(2), "Configurable overriding failed.");
    }

    @Test
    public void testConfigurableGlobalPropertyOverride() throws IOException {
        OverridingOptions o = new OverridingOptions();
        String[] args = new String[]{"-c", this.getClass().getName() + "|stringListConfig.xml", "-s", "overridingTest"};
        ConfigurationManager cm = new ConfigurationManager(args, o);

        Assertions.assertEquals("primates", o.slc.strings.get(0), "Configurable overriding failed.");
        Assertions.assertEquals("monkeys", o.slc.strings.get(1), "Configurable overriding failed.");
        Assertions.assertEquals("lemurs", o.slc.strings.get(2), "Configurable overriding failed.");
    }

    @Test
    public void testConfigurableOverrideAndLoad() throws IOException {
        OverridingOptions o = new OverridingOptions();
        String[] args = new String[]{"-c", this.getClass().getName() + "|stringListConfig.xml", "--string-list-configurable", "listTest", "--@listTest.strings", "alpha,beta,gamma"};
        ConfigurationManager cm = new ConfigurationManager(args, o);

        StringListConfigurable slc = o.slc;
        Assertions.assertEquals("alpha", slc.strings.get(0), "Configurable overriding failed.");
        Assertions.assertEquals("beta", slc.strings.get(1), "Configurable overriding failed.");
        Assertions.assertEquals("gamma", slc.strings.get(2), "Configurable overriding failed.");
    }

    @Test
    public void testGlobalOverride() throws IOException {
        String[] args = new String[]{"-c", this.getClass().getName() + "|globalPropertyConfig.xml", "--@a", "apollo", "--@monkeys", "gibbons"};
        ConfigurationManager cm = new ConfigurationManager(args);

        assertEquals("apollo", cm.getGlobalProperty("a"), "Global property overriding failed.");
        assertEquals("gibbons", cm.getGlobalProperty("monkeys"), "Global property overriding failed.");
        assertEquals("beta", cm.getGlobalProperty("b"), "Global property overriding failed.");
    }

    @Test
    public void testConfigurableOverrideFail() throws IOException {
        assertThrows(ArgumentException.class, () -> {
            String[] args = new String[]{"--@listTest.strings", "alpha,beta,gamma"};
            ConfigurationManager cm = new ConfigurationManager(args);
        });
    }

    @Test
    public void testNoConfigParam() throws IOException {
        assertThrows(ArgumentException.class, () -> {
            String[] args = new String[]{"-c"};
            ConfigurationManager cm = new ConfigurationManager(args);
        });
    }

    @Test
    public void testNoConfigParamStr() throws IOException {
        assertThrows(ArgumentException.class, () -> {
            String[] args = new String[]{"--config-file"};
            ConfigurationManager cm = new ConfigurationManager(args);
        });
    }

    @Test
    public void testInvalidConfigParam() throws IOException {
        assertThrows(ArgumentException.class, () -> {
            String[] args = new String[]{"-c", "monkeys"};
            ConfigurationManager cm = new ConfigurationManager(args);
        });
    }

    @Test
    public void testDuplicateCharArguments() throws IOException {
        assertThrows(ArgumentException.class, () -> {
            String[] args = new String[]{"-c", this.getClass().getName() + "|stringListConfig.xml"};
            Options o = new DuplicateCharOptions();
            ConfigurationManager cm = new ConfigurationManager(args, o);
        });
    }

    @Test
    public void testDuplicateLongArguments() throws IOException {
        assertThrows(ArgumentException.class, () -> {
            String[] args = new String[]{"-c", this.getClass().getName() + "|stringListConfig.xml"};
            Options o = new DuplicateLongOptions();
            ConfigurationManager cm = new ConfigurationManager(args, o);
        });
    }

    @Test
    public void testAllOptions() throws IOException {
        Options o = new AllOfTheOptions();
        AllOfTheOptions factory = AllOfTheOptions.generate();
        String[] args = factory.generateArguments();

        ConfigurationManager cm = new ConfigurationManager(args, o);

        assertEquals(factory, o, "Generated and parsed options not the same");
    }

    @Test
    public void testNoConfigFileOptions() throws IOException {
        String[] args = new String[]{"-c", "totally-not-a-config-file"};
        NoConfigOptions o = new NoConfigOptions();
        ConfigurationManager cm;
        cm = new ConfigurationManager(args, o, false);
        assertNotNull(cm);
        assertEquals(args[1], o.notAConfigFile, "Failed to parse out option");

        try {
            o = new NoConfigOptions();
            cm = new ConfigurationManager(args, o, true);
            fail("Accepted an config file option when it shouldn't have");
        } catch (ArgumentException e) {
        }
    }

    @Test
    public void testInvalidNoConfigFileOptions() throws IOException {
        String[] args = new String[]{"-c", this.getClass().getName() + "|stringListConfig.xml", "-s", "listTest"};
        InvalidNoConfigOptions o = new InvalidNoConfigOptions();
        ConfigurationManager cm;
        cm = new ConfigurationManager(args, o, true);
        assertNotNull(cm);

        try {
            o = new InvalidNoConfigOptions();
            cm = new ConfigurationManager(args, o, false);
            fail("Accepted a generic configurable argument when it shouldn't have");
        } catch (ArgumentException e) {
        }
    }

    public static void main(String[] args) throws IOException {
        Options o = new AllOfTheOptions();
        ConfigurationManager cm;
        try {
            cm = new ConfigurationManager(args, o);
        } catch (UsageException e) {
            System.out.println(e.getUsage());
            return;
        }

        String[] unparsedArgs = cm.getUnnamedArguments();

        System.out.println("Unparsed arguments:");
        for (String s : unparsedArgs) {
            System.out.println(s);
        }
    }

    public static class DuplicateCharOptions implements Options {
        @Option(charName = 'd', longName = "dvorak", usage = "test hard")
        public String dvorak;

        @Option(charName = 'd', longName = "diplodocus", usage = "test hard 2: test harder")
        public String diplodocus;
    }

    public static class DuplicateLongOptions implements Options {
        @Option(charName = 'v', longName = "dvorak", usage = "test hard")
        public String dvorak;

        @Option(charName = 'd', longName = "dvorak", usage = "test hard 2: test harder")
        public String dvorak2;
    }

    public static class OverridingOptions implements Options {
        @Option(charName = 's', longName = "string-list-configurable", usage = "String list configurable")
        public StringListConfigurable slc;
    }

    public static class ParsingOptions implements Options {
        @Option(charName = 'o', longName = "other", usage = "test hard")
        public boolean other;

        @Option(charName = 's', longName = "surrounding", usage = "test hard 2: test harder")
        public boolean surrounding;

        @Option(longName = "other-arguments", usage = "test hard with a vengeance")
        public String otherArguments;
    }

    public static class NoConfigOptions implements Options {
        @Option(charName = 'c', longName = "not-a-config-file-honest", usage = "test hard")
        public String notAConfigFile;
    }

    public static class InvalidNoConfigOptions implements Options {
        @Option(charName = 's', longName = "string-list-configurable", usage = "test hard 2: test harder")
        public StringListConfigurable slc;
        @Option(charName = 'l', longName = "list-string-list-configurable", usage = "test hard with a vengeance")
        public List<StringListConfigurable> lslc;
    }

    public static class AllOfTheOptions implements Options {
        private static final Logger logger = Logger.getLogger(AllOfTheOptions.class.getName());

        @Option(charName = 'a', longName = "alpha", usage = "alpha")
        private boolean alpha;
        @Option(charName = 'b', longName = "beta", usage = "beta")
        private byte beta;
        @Option(longName = "gamma", usage = "gamma")
        private short gamma;
        @Option(charName = 'd', longName = "delta", usage = "")
        private int delta;
        @Option(charName = 'e', longName = "epsilon", usage = "")
        private long epsilon;
        @Option(charName = 'f', longName = "zeta", usage = "")
        private float zeta;
        @Option(charName = 'g', longName = "eta", usage = "")
        private double eta;
        @Option(longName = "theta", usage = "")
        private String theta;
        @Option(longName = "iota", usage = "")
        private byte[] iota;
        @Option(charName = 'j', longName = "kappa", usage = "")
        private short[] kappa;
        @Option(charName = 'k', longName = "lambda", usage = "")
        private int[] lambda;
        @Option(charName = 'l', longName = "mu", usage = "")
        private long[] mu;
        @Option(charName = 'm', longName = "nu", usage = "")
        private float[] nu;
        @Option(charName = 'n', longName = "xi", usage = "")
        private double[] xi;
        @Option(charName = 'o', longName = "omicron", usage = "")
        private StringConfigurable omicron;
        @Option(charName = 'p', longName = "pi", usage = "")
        private String[] pi;
        @Option(charName = 'q', longName = "rho", usage = "")
        private Configurable[] rho;
        @Option(charName = 'r', longName = "sigma", usage = "")
        private List<String> sigma;
        @Option(charName = 's', longName = "tau", usage = "")
        private EnumSet<Foo> tau;
        @Option(charName = 't', longName = "upsilon", usage = "")
        private Set<String> upsilon;
        @Option(charName = 'u', longName = "phi", usage = "")
        private AtomicInteger phi;
        @Option(charName = 'v', longName = "chi", usage = "")
        private AtomicLong chi;
        @Option(charName = 'w', longName = "psi", usage = "")
        private File psi;
        @Option(charName = 'x', longName = "omega", usage = "")
        private Path omega;
        @Option(charName = 'y', longName = "\uD83D\uDE01", usage = "")
        private Random a;
        @Option(charName = 'z', longName = "\uD83D\uDE00", usage = "")
        private Foo b;

        public enum Foo {BAR, BAZ, QUUX;}

        @Override
        public String toString() {
            return "AllOfTheOptions{" +
                    "alpha=" + alpha +
                    ",\n beta=" + beta +
                    ",\n gamma=" + gamma +
                    ",\n delta=" + delta +
                    ",\n epsilon=" + epsilon +
                    ",\n zeta=" + zeta +
                    ",\n eta=" + eta +
                    ",\n theta='" + theta + '\'' +
                    ",\n iota=" + Arrays.toString(iota) +
                    ",\n kappa=" + Arrays.toString(kappa) +
                    ",\n lambda=" + Arrays.toString(lambda) +
                    ",\n mu=" + Arrays.toString(mu) +
                    ",\n nu=" + Arrays.toString(nu) +
                    ",\n xi=" + Arrays.toString(xi) +
                    ",\n omicron=" + omicron +
                    ",\n pi=" + Arrays.toString(pi) +
                    ",\n rho=" + Arrays.toString(rho) +
                    ",\n sigma=" + sigma +
                    ",\n tau=" + tau +
                    ",\n upsilon=" + upsilon +
                    ",\n phi=" + phi +
                    ",\n chi=" + chi +
                    ",\n psi=" + psi +
                    ",\n omega=" + omega +
                    ",\n a=" + a +
                    ",\n b=" + b +
                    "}\n";
        }

        public static AllOfTheOptions generate() {
            AllOfTheOptions o = new AllOfTheOptions();

            o.alpha = true;
            o.beta = 10;
            o.gamma = 123;
            o.delta = 12345;
            o.epsilon = 1234567890L;
            o.zeta = 2.78f;
            o.eta = 3.14;
            o.theta = "cos(theta)";
            o.iota = new byte[]{127, 126, 125};
            o.kappa = new short[]{5, 6, 7};
            o.lambda = new int[]{-1, -2, -3};
            o.mu = new long[]{1L, 2L, 3L};
            o.nu = new float[]{3.1f, 2.8f};
            o.xi = new double[]{3.14, 2.78};
            o.omicron = new StringConfigurable("a", "b", "c");
            o.pi = new String[]{"tau", "is", "better", "than", "pi"};
            o.rho = new Configurable[]{o.omicron, new StringConfigurable("d", "e", "f")};
            o.sigma = new ArrayList<>();
            o.sigma.addAll(Arrays.asList("foo", "bar", "bar", "baz", "quux"));
            o.tau = EnumSet.of(Foo.BAR);
            o.upsilon = new HashSet<>();
            o.upsilon.addAll(Arrays.asList("foo", "bar", "quux"));
            o.phi = new AtomicInteger(123456);
            o.chi = new AtomicLong(1234567890L);
            o.psi = new File("file.txt");
            o.omega = Paths.get("path.txt");
            o.a = new Random(1);
            o.b = Foo.BAZ;

            return o;
        }

        public String[] generateArguments() {
            ArrayList<String> a = new ArrayList<>();

            a.add("-c");
            a.add("com.oracle.labs.mlrg.olcut.test.config_tests.ArgumentParsingTest|stringConfig.xml");
            a.add("-ab");
            a.add(beta + "");
            a.add("--gamma");
            a.add(gamma + "");
            a.add("-d");
            a.add(delta + "");
            a.add("-e");
            a.add(epsilon + "");
            a.add("-f");
            a.add(zeta + "");
            a.add("-g");
            a.add(eta + "");
            a.add("--theta");
            a.add(theta + "");
            a.add("--iota");
            a.add(iota[0] + "," + iota[1] + "," + iota[2]);
            a.add("-j");
            a.add(kappa[0] + "," + kappa[1] + "," + kappa[2]);
            a.add("-k");
            a.add(lambda[0] + "," + lambda[1] + "," + lambda[2]);
            a.add("-l");
            a.add(mu[0] + "," + mu[1] + "," + mu[2]);
            a.add("-m");
            a.add(nu[0] + "," + nu[1]);
            a.add("-n");
            a.add(xi[0] + "," + xi[1]);
            a.add("-o");
            a.add("a"); //Reference to StringConfigurable in stringConfig.xml
            a.add("-p");
            a.add(pi[0] + "," + pi[1] + "," + pi[2] + "," + pi[3] + "," + pi[4]);
            a.add("-q");
            a.add("a,b"); //Array reference to StringConfigurable
            a.add("-r");
            a.add("foo,bar,bar,baz,quux");
            a.add("-s");
            a.add(Foo.BAR.toString());
            a.add("-t");
            a.add("foo,bar,quux");
            a.add("-u");
            a.add(phi.get() + "");
            a.add("-v");
            a.add(chi.get() + "");
            a.add("-w");
            a.add(psi.getName() + "");
            a.add("-x");
            a.add(omega.getFileName() + "");
            a.add("-y");
            a.add(this.a.nextInt() + "");
            a.add("-z");
            a.add(b.toString() + "");

            return a.toArray(new String[0]);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AllOfTheOptions that = (AllOfTheOptions) o;

            if (alpha != that.alpha) {
                logger.info("Field " + alpha + " differs from " + this.alpha);
                return false;
            }
            if (beta != that.beta) {
                logger.info("Field " + beta + " differs from " + this.beta);
                return false;
            }
            if (gamma != that.gamma) {
                logger.info("Field " + gamma + " differs from " + this.gamma);
                return false;
            }
            if (delta != that.delta) {
                logger.info("Field " + delta + " differs from " + this.delta);
                return false;
            }
            if (epsilon != that.epsilon) {
                logger.info("Field " + epsilon + " differs from " + this.epsilon);
                return false;
            }
            if (Float.compare(that.zeta, zeta) != 0) {
                logger.info("Field " + zeta + " differs from " + this.zeta);
                return false;
            }
            if (Double.compare(that.eta, eta) != 0) {
                logger.info("Field " + eta + " differs from " + this.eta);
                return false;
            }
            if (theta != null ? !theta.equals(that.theta) : that.theta != null) {
                logger.info("Field " + theta + " differs from " + this.theta);
                return false;
            }
            if (!Arrays.equals(iota, that.iota)) {
                logger.info("Field " + iota + " differs from " + this.iota);
                return false;
            }
            if (!Arrays.equals(kappa, that.kappa)) {
                logger.info("Field " + kappa + " differs from " + this.kappa);
                return false;
            }
            if (!Arrays.equals(lambda, that.lambda)) {
                logger.info("Field " + lambda + " differs from " + this.lambda);
                return false;
            }
            if (!Arrays.equals(mu, that.mu)) {
                logger.info("Field " + mu + " differs from " + this.mu);
                return false;
            }
            if (!Arrays.equals(nu, that.nu)) {
                logger.info("Field " + nu + " differs from " + this.nu);
                return false;
            }
            if (!Arrays.equals(xi, that.xi)) {
                logger.info("Field " + xi + " differs from " + this.xi);
                return false;
            }
            if (omicron != null ? !omicron.equals(that.omicron) : that.omicron != null) {
                logger.info("Field " + omicron + " differs from " + this.omicron);
                return false;
            }
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(pi, that.pi)) {
                logger.info("Field " + pi + " differs from " + this.pi);
                return false;
            }
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(rho, that.rho)) {
                logger.info("Field " + rho + " differs from " + this.rho);
                return false;
            }
            if (sigma != null ? !sigma.equals(that.sigma) : that.sigma != null) {
                logger.info("Field " + sigma + " differs from " + this.sigma);
                return false;
            }
            if (tau != null ? !tau.equals(that.tau) : that.tau != null) {
                logger.info("Field " + tau + " differs from " + this.tau);
                return false;
            }
            if (upsilon != null ? !upsilon.equals(that.upsilon) : that.upsilon != null) {
                logger.info("Field " + upsilon + " differs from " + this.upsilon);
                return false;
            }
            if (phi != null ? !(phi.intValue() == that.phi.intValue()) : that.phi != null) {
                logger.info("Field " + phi + " differs from " + this.phi);
                return false;
            }
            if (chi != null ? !(chi.intValue() == that.chi.intValue()) : that.chi != null) {
                logger.info("Field " + chi + " differs from " + this.chi);
                return false;
            }
            if (psi != null ? !psi.equals(that.psi) : that.psi != null) {
                logger.info("Field " + psi + " differs from " + this.psi);
                return false;
            }
            if (omega != null ? !omega.equals(that.omega) : that.omega != null) {
                logger.info("Field " + omega + " differs from " + this.omega);
                return false;
            }
            return b == that.b;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = (alpha ? 1 : 0);
            result = 31 * result + (int) beta;
            result = 31 * result + (int) gamma;
            result = 31 * result + delta;
            result = 31 * result + (int) (epsilon ^ (epsilon >>> 32));
            result = 31 * result + (zeta != +0.0f ? Float.floatToIntBits(zeta) : 0);
            temp = Double.doubleToLongBits(eta);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            result = 31 * result + (theta != null ? theta.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(iota);
            result = 31 * result + Arrays.hashCode(kappa);
            result = 31 * result + Arrays.hashCode(lambda);
            result = 31 * result + Arrays.hashCode(mu);
            result = 31 * result + Arrays.hashCode(nu);
            result = 31 * result + Arrays.hashCode(xi);
            result = 31 * result + (omicron != null ? omicron.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(pi);
            result = 31 * result + Arrays.hashCode(rho);
            result = 31 * result + (sigma != null ? sigma.hashCode() : 0);
            result = 31 * result + (tau != null ? tau.hashCode() : 0);
            result = 31 * result + (upsilon != null ? upsilon.hashCode() : 0);
            result = 31 * result + (phi != null ? phi.hashCode() : 0);
            result = 31 * result + (chi != null ? chi.hashCode() : 0);
            result = 31 * result + (psi != null ? psi.hashCode() : 0);
            result = 31 * result + (omega != null ? omega.hashCode() : 0);
            result = 31 * result + (a != null ? a.hashCode() : 0);
            result = 31 * result + (b != null ? b.hashCode() : 0);
            return result;
        }
    }
}