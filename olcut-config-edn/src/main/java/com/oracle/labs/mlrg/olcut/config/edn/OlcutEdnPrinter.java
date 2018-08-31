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
