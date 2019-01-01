package org.java.groovy;

public class Application {

    public static void main(String[] args) throws Exception {
        ClassLoader classLoader = new OverrideClassLoader("org.java.groovy", "override");
        Class<?> cls = Class.forName("org.java.groovy.Test1", true, classLoader);
        cls.newInstance();
    }
}
