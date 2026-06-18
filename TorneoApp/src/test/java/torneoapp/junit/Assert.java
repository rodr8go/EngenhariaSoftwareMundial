package torneoapp.junit;

/** Asserções estilo JUnit 4/5. */
public class Assert {

    public static void assertEquals(Object expected, Object actual) {
        if (!safeEquals(expected, actual))
            throw new AssertionError("expected:<" + expected + "> but was:<" + actual + ">");
    }

    public static void assertEquals(long expected, long actual) {
        if (expected != actual)
            throw new AssertionError("expected:<" + expected + "> but was:<" + actual + ">");
    }
    public static void assertEquals(String msg, long expected, long actual) {
        if (expected != actual)
            throw new AssertionError(msg + " — expected:<" + expected + "> but was:<" + actual + ">");
    }
    public static void assertEquals(int expected, int actual) {
        if (expected != actual)
            throw new AssertionError("expected:<" + expected + "> but was:<" + actual + ">");
    }
    public static void assertEquals(String msg, int expected, int actual) {
        if (expected != actual)
            throw new AssertionError(msg + " — expected:<" + expected + "> but was:<" + actual + ">");
    }

    public static void assertEquals(double expected, double actual) {
        assertEquals(expected, actual, 1e-9);
    }

    public static void assertEquals(double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta)
            throw new AssertionError("expected:<" + expected + "> but was:<" + actual + ">");
    }

    public static void assertEquals(String msg, Object expected, Object actual) {
        if (!safeEquals(expected, actual))
            throw new AssertionError(msg + " — expected:<" + expected + "> but was:<" + actual + ">");
    }

    public static void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError("expected true but was false");
    }

    public static void assertTrue(String msg, boolean condition) {
        if (!condition) throw new AssertionError(msg);
    }

    public static void assertFalse(boolean condition) {
        if (condition) throw new AssertionError("expected false but was true");
    }

    public static void assertFalse(String msg, boolean condition) {
        if (condition) throw new AssertionError(msg);
    }

    public static void assertNull(Object obj) {
        if (obj != null) throw new AssertionError("expected null but was:<" + obj + ">");
    }

    public static void assertNull(String msg, Object obj) {
        if (obj != null) throw new AssertionError(msg + " — expected null but was:<" + obj + ">");
    }

    public static void assertNotNull(Object obj) {
        if (obj == null) throw new AssertionError("expected non-null but was null");
    }

    public static void assertNotNull(String msg, Object obj) {
        if (obj == null) throw new AssertionError(msg + " — expected non-null");
    }

    public static void fail(String msg) { throw new AssertionError(msg); }

    private static boolean safeEquals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
