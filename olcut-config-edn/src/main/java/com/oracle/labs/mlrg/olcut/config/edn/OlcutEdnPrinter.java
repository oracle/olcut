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

package com.oracle.labs.mlrg.olcut.config.edn;

import us.bpsm.edn.Symbol;
import us.bpsm.edn.printer.Printer;
import us.bpsm.edn.printer.Printers;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

public class OlcutEdnPrinter implements Printer {

    private Printer p;

    public OlcutEdnPrinter(OutputStreamWriter ps) {
        this.p = Printers.newPrinter(Printers.prettyProtocolBuilder()
                .put(List.class, prettyWriteListFn())
                .put(Map.class, prettyWriteMapFn()).build(), ps);
    }

    private static Fn<List<?>> prettyWriteListFn() {
        return (self, writer) -> {
            boolean vec = self instanceof RandomAccess;
            String start = vec ? "[" : "(";
            String end = vec ? "]" : ")";
            String mode = !self.isEmpty() && self.get(0) instanceof Symbol ? ((Symbol) self.get(0)).getName().toLowerCase() : "";
            int configHasMap = 0;
            writer.append(start);
            for(int i = 0; i<self.size(); i++) {
                if(i > 0) { // writing separators
                    if(mode.equals("config")) {
                        writer.append("\n\t");
                    } else if(mode.equals("component")) {
                        if(self.size() >= 4 && self.get(3) instanceof Map<?, ?>) {
                            configHasMap = 1;
                        }
                        if(i < 2 + configHasMap) {
                            writer.append(" ");
                        } else if(i == 2 + configHasMap) {
                            writer.append("\n\t\t");
                        } else if(i % 2 == 0) {
                            writer.append("\n\t\t");
                        } else {
                            writer.append(" ");
                        }
                    } else {
                        writer.append(' ');
                    }

                }
                writer.printValue(self.get(i));
            }
            writer.append(end);
        };
    }

    private static Fn<Map<?, ?>> prettyWriteMapFn() {
        return (self, writer) -> {
            String sep = "";
            writer.append('{');
            for(Map.Entry<?, ?> e: self.entrySet()) {
                writer.append(sep);
                writer.printValue(e.getKey());
                writer.append(' ');
                writer.printValue(e.getValue());
                sep = " ";
            }
            writer.append('}');
        };
    }

    @Override
    public Printer printValue(Object o) {
        return p.printValue(o);
    }

    @Override
    public Printer append(CharSequence charSequence) {
        return p.append(charSequence);
    }

    @Override
    public Printer append(char c) {
        return p.append(c);
    }

    @Override
    public Printer softspace() {
        return p.softspace();
    }

    @Override
    public void close() {
        p.close();
    }
}
