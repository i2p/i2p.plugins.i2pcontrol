package net.i2p.i2pcontrol.util;

public abstract class IsJar {
    public static boolean isRunningJar(){
        IsJarTester isJar = new IsJarTester();
        String className = isJar.getClass().getName().replace('.', '/');
        String classJar =  isJar.getClass().getResource("/" + className + ".class").toString();
        if (classJar.startsWith("jar:"))
           return true;
        else
           return false;
    }
}
