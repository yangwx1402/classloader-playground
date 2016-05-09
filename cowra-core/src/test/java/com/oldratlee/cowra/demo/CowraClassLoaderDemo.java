package com.oldratlee.cowra.demo;

import com.oldratlee.cowra.Constants;
import com.oldratlee.cowra.DelegateType;
import com.oldratlee.cowra.CowraClassLoader;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oldratlee.cowra.DelegateType.PARENT_ONLY;
import static com.oldratlee.cowra.util.Utils.invokeMain;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author ding.lid
 */
public class CowraClassLoaderDemo {
    public static void main(String[] args) throws Exception {
        Map<DelegateType, List<String>> delegateConfig = new HashMap<>();
        delegateConfig.put(PARENT_ONLY, Arrays.asList("com.foo.p2.", "com.foo.p3.."));

        ClassLoader classLoader = new CowraClassLoader(new URL[]{Constants.lib_common}, delegateConfig);

        invokeMain(classLoader.loadClass("com.foo.Foo"));
        invokeMain(classLoader.loadClass("com.foo.p1.P1C1"));
        try {
            invokeMain(classLoader.loadClass("com.foo.p2.P2C1"));
            fail();
        } catch (ClassNotFoundException e) {
            assertThat(e.getMessage(), containsString("(PARENT_ONLY) not found in parent class loader"));
        }
    }
}
