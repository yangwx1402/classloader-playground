package com.oldratlee.cowra.launcher;

import com.oldratlee.cowra.DelegateType;
import com.oldratlee.cowra.CowraClassLoader;
import com.oldratlee.cowra.matcher.CowraMatcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

/**
 * @author ding.lid
 */
public class CowraLauncher {
    public static final String COWRA_MATCHER_CLASS = "cowra.matcher.class";
    public static final String COWRA_SHARED_DELEGATE_CONFIGS = "cowra.shared.delegate.configs";

    public static final String COWRA_SHARED_LIB_DIR = "cowra.shared.lib.dir";

    public static final String COWRA_APP_NAMES = "cowra.app.names";

    public static final String COWRA_APP_LIB_DIR_PREFIX = "cowra.app.%s.lib.dir";
    public static final String COWRA_APP_DELEGATE_CONFIGS_PREFIX = "cowra.app.%s.delegate.configs";
    public static final String COWRA_APP_MAIN_CLASS_PREFIX = "cowra.app.%s.main.class";
    public static final String COWRA_APP_MAIN_ARGS_PREFIX = "cowra.app.%s.main.args";

    public static final String MAIN_METHOD_NAME = "main";

    private static final CowraLauncher launcher = new CowraLauncher();

    public static final Map<String, CowraClassLoader> app2CowraClassLoaderMap = new HashMap<>();

    public static CowraLauncher getLauncher() {
        return launcher;
    }

    public ClassLoader createClassLoader(final URL[] urls) {
        return new CowraClassLoader(urls, new EnumMap<DelegateType, List<String>>(DelegateType.class));
    }

    public static void main(String[] args) throws IOException {
        ClassLoader appParentClassLoader = ClassLoader.getSystemClassLoader();
        final CowraMatcher matcher = getMatcher();

        if (System.getProperty(COWRA_SHARED_LIB_DIR) != null && System.getProperty(COWRA_SHARED_LIB_DIR).length() > 0) {
            Map<DelegateType, List<String>> sharedDelegateConfigs = convertDelegateConfigs(System.getProperty(COWRA_SHARED_DELEGATE_CONFIGS));
            URL[] sharedLibs = getClassPathFromLibDir(System.getProperty(COWRA_SHARED_LIB_DIR));
            CowraClassLoader sharedClassLoader = new CowraClassLoader(sharedLibs, sharedDelegateConfigs, matcher);
            appParentClassLoader = sharedClassLoader;
        }

        String appNames = System.getProperty(COWRA_APP_NAMES);
        String[] appNameArray = appNames.split("\\s*,\\s*");

        for (String appName : appNameArray) {
            Map<DelegateType, List<String>> appDelegateConfigs = convertDelegateConfigs(
                    System.getProperty(String.format(COWRA_APP_LIB_DIR_PREFIX, appName)));
            URL[] appLibs = getClassPathFromLibDir(
                    System.getProperty(String.format(COWRA_APP_DELEGATE_CONFIGS_PREFIX, appName)));
            CowraClassLoader appClassLoader = new CowraClassLoader(appLibs, appDelegateConfigs, matcher, appParentClassLoader);
            app2CowraClassLoaderMap.put(appName, appClassLoader);
        }

        for (Map.Entry<String, CowraClassLoader> entry : app2CowraClassLoaderMap.entrySet()) {
            invokeAppMain(entry.getKey(), entry.getValue());
        }
    }

    static CowraMatcher getMatcher() {
        String matcherClassName = System.getProperty(COWRA_MATCHER_CLASS);
        if (matcherClassName == null || matcherClassName.trim().length() ==0) {
            return null;
        }

        try {
            Class<?> matcherClass = Class.forName(matcherClassName);
            if (!CowraMatcher.class.isAssignableFrom(matcherClass)) {
                throw new IllegalStateException("CowraMatcher class " + matcherClassName +
                        " is not subclass of " + CowraMatcher.class.getName());
            }
            return (CowraMatcher) matcherClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Fail to init matcher " + matcherClassName + ", cause: " + e.getMessage(), e);
        }
    }

    static Map<DelegateType, List<String>> convertDelegateConfigs(String delegateConfigs) {
        Map<DelegateType, List<String>> ret = new EnumMap<>(DelegateType.class);

        String[] delegateConfigArray = delegateConfigs.split("\\s*;\\s*");
        for (String delegateConfig : delegateConfigArray) {
            String[] delegateTypeAndPatterns = delegateConfig.trim().split("\\s*=\\s*");
            if (delegateConfigs.length() != 2) {
                throw new IllegalStateException("Wrong delegate type and pattern: " + Arrays.toString(delegateTypeAndPatterns));
            }
            DelegateType delegateType = DelegateType.valueOf(delegateConfigArray[0]);
            String patterns = delegateConfigArray[1];
            ret.put(delegateType, toPatternList(patterns));
        }

        return ret;
    }

    static List<String> toPatternList(String patterns) {
        String[] split = patterns.split("\\s*,\\s*");

        List<String> ret = new ArrayList<>();
        for (String s : split) {
            s = s.trim();
            if (s.length() == 0) continue;
            ret.add(s);
        }
        return ret;
    }

    static URL[] getClassPathFromLibDir(String libPaths) throws IOException {
        List<URL> ret = new ArrayList<>();

        String[] libPathArray = libPaths.split("\\s*:\\s*");
        for (String libPath : libPathArray) {
            File file = new File(libPath);
            if (!file.exists()) {
                continue;
            }
            if (!file.isDirectory() && libPath.endsWith(".jar")) {
                ret.add(file.getCanonicalFile().toURI().toURL());
                continue;
            }
            File[] subFiles = file.listFiles();
            if (null == subFiles) {
                throw new IllegalStateException("Fail to list files from dir " + file);
            }
            for (File subFile : subFiles) {
                if (subFile.isDirectory()) {
                    ret.add(subFile.getCanonicalFile().toURI().toURL());
                } else if (!file.isDirectory() && libPath.endsWith(".jar")) {
                    ret.add(file.getCanonicalFile().toURI().toURL());
                }
            }
        }

        return ret.toArray(new URL[ret.size()]);
    }

    static void invokeAppMain(final String appName, ClassLoader appClassLoader) {
        try {
            String mainClassName = System.getProperty(String.format(COWRA_APP_MAIN_CLASS_PREFIX, appName));
            String appArgs = System.getProperty(String.format(COWRA_APP_MAIN_ARGS_PREFIX, appName));

            Class<?> mainClass = appClassLoader.loadClass(mainClassName);
            final String[] argArray = appArgs.split("\\s+"); // TODO argument can not contain space! 

            final Method mainMethod = mainClass.getMethod(MAIN_METHOD_NAME, new Class[]{String[].class});
            int modifiers = mainMethod.getModifiers();
            if (!Modifier.isPublic(modifiers) || !Modifier.isStatic(modifiers)) {
                throw new IllegalStateException(String.format(
                        "the main method of main class(%s) of App(%s) is NOT public static!",
                        mainClassName, appName));
            }

            Thread appMainThread = new AppThread(mainMethod, argArray, appName);
            appMainThread.setContextClassLoader(appClassLoader);
            appMainThread.start();
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Fail to load app %s, cause: %s", appName, e.getMessage()), e);
        }
    }
}
