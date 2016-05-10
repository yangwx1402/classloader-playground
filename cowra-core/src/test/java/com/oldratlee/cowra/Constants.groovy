package com.oldratlee.cowra;

/**
 * @author ding.lid
 */
public class Constants {
    public static final URL lib_common;
    public static final URL lib_api;
    public static final URL lib_impl;

    public static final String lib_package_api = "com.bar.api";
    public static final String lib_class_api = "com.bar.api.ApiC0";
    public static final String lib_package_impl = "com.bar.impl";
    public static final String lib_class_impl = "com.bar.impl.ImplC0";

    static {
        lib_common = getFile('test-helper-common')
        lib_api = getFile('test-helper-api')
        lib_impl = getFile('test-helper-impl')

        println "lib_common: $lib_common\nlib_lib_api: $lib_api\nlib_impl: $lib_impl"
    }

    private static getFile(String pattern) {
        def names = new FileNameFinder().getFileNames('..', "**/*$pattern-*.jar")
        assert names.size() == 1
        return new File(names[0]).toURI().toURL()
    }
}
