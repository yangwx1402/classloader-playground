package com.oldratlee.cowra.matcher;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author ding.lid
 */
public class DefaultCowraMatcherTest {
    CowraMatcher matcher = new DefaultCowraMatcher();
    
    @Test
    public void testMatch_1() throws Exception {
        assertTrue(matcher.match("foo.A", "foo.A"));

        assertTrue(matcher.match("foo.A", "foo.*"));
        assertTrue(matcher.match("foo.Air123", "foo.*"));
        assertFalse(matcher.match("foo1.Air123", "foo.*"));
        assertFalse(matcher.match("foo.p.A", "foo.*"));

        assertTrue(matcher.match("foo.A", "foo.**"));
        assertTrue(matcher.match("foo.p1.B", "foo.**"));
        assertFalse(matcher.match("foo1.A", "foo.**"));
    }
}
