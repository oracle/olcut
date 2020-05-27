package com.oracle.labs.mlrg.olcut.extras.completion;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

public class GenCompletion {
    private static final Logger logger = Logger.getLogger(GenCompletion.class.getName());

    public static boolean isMainClass(ClassNode cn) {
        //logger.info("Checking class with name: " + cn.name);
        return cn.methods.stream()
                .filter(m -> m.name.equalsIgnoreCase("main")).findFirst().map(m -> {
            //logger.info("found main candidate, access: " + (m.access == ACC_PUBLIC + ACC_STATIC) + " desc: " + m.desc);
            return m.access == ACC_PUBLIC + ACC_STATIC && m.desc.equals("([Ljava/lang/String;)V");
        }).orElse(false);
    }

    public static Optional<MethodNode> getMainClass(ClassNode cn) {
        return cn.methods.stream()
                .filter(m -> m.name.equalsIgnoreCase("main")
                        && m.access == ACC_PUBLIC + ACC_STATIC
                        && m.desc.equals("([Ljava/lang/String;)V")).findFirst();
    }

    public static boolean isOptionsClass(ClassNode cn) {
        return cn.interfaces.stream()
                .anyMatch(OptionCompletion.OPTIONS_CLASS::equals);
    }

    public static OptionsClass processOptionsClass(ClassNode cn) {
        OptionsClass optionsClass = new OptionsClass(cn);
        for(FieldNode field: cn.fields) {
            if(field.visibleAnnotations != null) {
                for (AnnotationNode vAnno : field.visibleAnnotations) {
                    if (vAnno.desc.equals(OptionCompletion.OPTION_CLASS)) {
                        if(vAnno.values.get(0).equals("charName")) { // charName is optional and may not be present
                            optionsClass.addCompletion(
                                    new OptionCompletion((char) vAnno.values.get(1), (String) vAnno.values.get(3), (String) vAnno.values.get(5)));
                        } else {
                            optionsClass.addCompletion(
                                    new OptionCompletion((String) vAnno.values.get(1), (String) vAnno.values.get(3)));
                        }
                    }
                }
            }
        }
        return optionsClass;
    }

    public static Stream<ClassCompletion> completions(File jarFile) {
        List<ClassNode> classes = new ArrayList<>();
        try(JarFile jar = new JarFile(jarFile)) {
            JarEntry entry;
            Enumeration<JarEntry> jarEnumeration = jar.entries();
            while(jarEnumeration.hasMoreElements()) {
                entry = jarEnumeration.nextElement();
                if(!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    ClassNode node = new ClassNode();
                    new ClassReader(jar.getInputStream(entry)).accept(node, ClassReader.SKIP_FRAMES);
                    classes.add(node);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading Classes", e);
            System.exit(1);
        }
        Map<ClassCompletion, MethodNode> mainClasses = new HashMap<>();
        Map<String, OptionsClass> optionsClasses = new HashMap<>();
        for(ClassNode clazz: classes) {
            if(isOptionsClass(clazz)) {
                optionsClasses.put(clazz.name, processOptionsClass(clazz));
            }
            getMainClass(clazz).ifPresent(m -> mainClasses.put(new ClassCompletion(clazz.name), m));
        }
        // Resolve Option Parents
        optionsClasses.forEach((k, v) -> v.findParents(optionsClasses));

        List<ClassCompletion> classCompletions = new ArrayList<>();
        for(Map.Entry<ClassCompletion, MethodNode> entry: mainClasses.entrySet()) {
            ClassCompletion classComp = entry.getKey();
            //entry.getValue().localVariables.stream().filter(lv -> optionsClasses.containsKey(lv.desc)).
            for(LocalVariableNode lv: entry.getValue().localVariables) {
                String optionClass = trimDesc(lv.desc);
                if(optionsClasses.containsKey(optionClass)) {
                    classComp.addOptions(optionsClasses.get(optionClass).getCompletions());
                    break;
                }
            }
            classCompletions.add(classComp);
        }
        return classCompletions.stream();
    }

    public static String trimDesc(String desc) {
        String trimmed = desc;
        if(trimmed.startsWith("L")) {
            trimmed = trimmed.substring(1);
        }
        if(trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public static void main(String[] args) {
        System.out.println(completions(new File(args[0]))
                .map(ClassCompletion::completionString)
                .collect(Collectors.joining("\n")));
    }
}
