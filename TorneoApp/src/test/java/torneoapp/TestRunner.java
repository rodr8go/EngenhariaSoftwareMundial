package torneoapp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal test runner — no external dependencies.
 * Run with: java -cp <classpath> torneoapp.TestRunner
 */
public class TestRunner {

    static int passed = 0, failed = 0;
    static List<String> failures = new ArrayList<>();

    public static void assertTrue(String msg, boolean condition) {
        if (condition) { passed++; System.out.println("  ✓ " + msg); }
        else           { failed++; failures.add("FAIL: " + msg); System.out.println("  ✗ FAIL: " + msg); }
    }

    public static void assertEquals(String msg, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok) { passed++; System.out.println("  ✓ " + msg); }
        else    { failed++; String f = "FAIL: " + msg + " — expected=" + expected + " actual=" + actual;
                  failures.add(f); System.out.println("  ✗ " + f); }
    }

    public static void assertNull(String msg, Object obj) {
        assertTrue(msg + " (expected null)", obj == null);
    }

    public static void assertNotNull(String msg, Object obj) {
        assertTrue(msg + " (expected non-null)", obj != null);
    }

    public static void main(String[] args) throws Exception {
        Class<?>[] suites = {
            torneoapp.tests.JogoTest.class,
            torneoapp.tests.TorneioTest.class,
            torneoapp.tests.EquipaTest.class,
            torneoapp.tests.JogadorTest.class,
            torneoapp.tests.EstadioTest.class,
        };

        for (Class<?> suite : suites) {
            System.out.println("\n══ " + suite.getSimpleName() + " ══");
            Object inst = suite.getDeclaredConstructor().newInstance();
            for (Method m : suite.getDeclaredMethods()) {
                if (m.getName().startsWith("test")) {
                    System.out.println("\n[" + m.getName() + "]");
                    try { m.invoke(inst); }
                    catch (Exception e) {
                        failed++;
                        String f = "EXCEPTION in " + m.getName() + ": " + e.getCause();
                        failures.add(f); System.out.println("  ✗ " + f);
                    }
                }
            }
        }

        System.out.println("\n══════════════════════════════");
        System.out.println("  PASSED: " + passed);
        System.out.println("  FAILED: " + failed);
        if (!failures.isEmpty()) {
            System.out.println("\nFailures:");
            failures.forEach(f -> System.out.println("  " + f));
        }
        System.out.println("══════════════════════════════");
        System.exit(failed > 0 ? 1 : 0);
    }
}
