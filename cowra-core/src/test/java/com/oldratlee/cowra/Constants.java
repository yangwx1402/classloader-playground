package com.oldratlee.cowra;

import java.io.File;
import java.net.URL;

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
        try {
            lib_common = new File("../cowra-test-helper/cowra-test-helper-common/target/cowra-test-helper-common-0.0.1-SNAPSHOT.jar").getCanonicalFile().toURI().toURL();
            lib_api = new File("../cowra-test-helper/cowra-test-helper-api/target/cowra-test-helper-api-0.0.1-SNAPSHOT.jar").getCanonicalFile().toURI().toURL();
            lib_impl = new File("../cowra-test-helper/cowra-test-helper-impl/target/cowra-test-helper-impl-0.0.1-SNAPSHOT.jar").getCanonicalFile().toURI().toURL();

            System.out.printf("lib_common: %s\nlib_lib_api: %s\nlib_impl: %s",
                    lib_common, lib_api, lib_impl);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
