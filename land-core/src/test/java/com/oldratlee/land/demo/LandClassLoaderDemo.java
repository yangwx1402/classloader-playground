package com.oldratlee.land.demo;

import com.oldratlee.land.Constants;
import com.oldratlee.land.DelegateType;
import com.oldratlee.land.LandClassLoader;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oldratlee.land.util.Utils.invokeMain;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author ding.lid
 */
public class LandClassLoaderDemo {
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            Map<DelegateType, List<String>> delegateConfig = new HashMap<>();
            delegateConfig.put(DelegateType.PARENT_CHILD, Arrays.asList("com.foo.p2.", "com.foo.p3.."));

            ClassLoader classLoader = new LandClassLoader(new URL[]{Constants.lib_common, Constants.lib_api, Constants.lib_impl}, delegateConfig);

            invokeMain(classLoader.loadClass("com.foo.Foo"));
            invokeMain(classLoader.loadClass("com.foo.p1.P1C1"));
            try {
                invokeMain(classLoader.loadClass("com.foo.p2.P2C1"));
//            fail();
            } catch (ClassNotFoundException e) {
                assertThat(e.getMessage(), containsString("(PARENT_ONLY) not found in parent class loader"));
            }
            Thread.sleep(10000);
        }
    }
}
