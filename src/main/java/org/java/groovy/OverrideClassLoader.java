package org.java.groovy;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class OverrideClassLoader extends GroovyClassLoader {

    private final String packageName;
    private final Map<String, File> overrides;

    public OverrideClassLoader(String packageName, String overrideDir) throws IOException {
        super(null, CustomCompilerConfiguration.INSTANCE, true);
        this.packageName = Objects.requireNonNull(packageName);
        this.overrides = overrideDir != null && !overrideDir.isEmpty() ?
                Files.walk(Paths.get(overrideDir))
                        .filter(FileSystems.getDefault().getPathMatcher("glob:**.groovy")::matches)
                        .collect(Collectors.toMap(OverrideClassLoader::toClassName, Path::toFile)) :
                Collections.emptyMap();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith(packageName)) {
            File file = overrides.get(name);
            if (file != null) {
                try {
                    return parseClass(file);
                } catch (IOException ex) {
                    throw new ClassNotFoundException(name, ex);
                }
            }

            return super.findClass(name);
        } else {
            return findSystemClass(name);
        }
    }

    private static String toClassName(Path path) {
        StringBuilder sb = new StringBuilder(64);
        int last = path.getNameCount() - 1;
        for (int i = 1; i < last; i++) {
            sb.append(path.getName(i)).append(".");
        }
        String fileName = path.getName(last).toString();
        sb.append(fileName, 0, fileName.lastIndexOf("."));
        return sb.toString();
    }

    private static class CustomCompilerConfiguration {
        private static final CompilerConfiguration INSTANCE = new CompilerConfiguration();

        static {
            INSTANCE.setClasspath(System.getProperty("java.class.path"));
            INSTANCE.setTargetBytecode(CompilerConfiguration.JDK8);
            INSTANCE.getOptimizationOptions().put(CompilerConfiguration.INVOKEDYNAMIC, Boolean.TRUE);
        }
    }
}
