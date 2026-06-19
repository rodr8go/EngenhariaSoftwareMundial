package torneoapp.junit;

import java.lang.reflect.Method;

/**
 * Corre testes individualmente ou suites completas.
 *
 * Uso:
 *   java -cp ... torneoapp.junit.TestRunner NomeClasse#nomeMetodo   → 1 teste
 *   java -cp ... torneoapp.junit.TestRunner NomeClasse              → suite completa
 *   java -cp ... torneoapp.junit.TestRunner                         → todos os testes
 */
public class TestRunner {

    private static final String[] ALL_SUITES = {
        "torneoapp.tests.JogoTest",
        "torneoapp.tests.TorneioTest",
        "torneoapp.tests.EquipaTest",
        "torneoapp.tests.JogadorTest",
        "torneoapp.tests.EstadioTest",
    };

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            // Corre tudo
            int[] result = {0, 0};
            for (String suite : ALL_SUITES) runSuite(Class.forName(suite), result);
            printSummary(result[0], result[1]);
        } else {
            String arg = args[0];
            int[] result = {0, 0};
            if (arg.contains("#")) {
                // Corre um único teste: Classe#metodo
                String[] parts = arg.split("#", 2);
                Class<?> cls = resolveClass(parts[0]);
                Method m = cls.getDeclaredMethod(parts[1]);
                runMethod(cls.getDeclaredConstructor().newInstance(), m, result);
            } else {
                // Corre suite completa
                runSuite(resolveClass(arg), result);
            }
            printSummary(result[0], result[1]);
        }
    }

    private static Class<?> resolveClass(String name) throws ClassNotFoundException {
        // Aceita nome simples (JogoTest) ou completo (torneoapp.tests.JogoTest)
        if (name.contains(".")) return Class.forName(name);
        for (String s : ALL_SUITES)
            if (s.endsWith("." + name)) return Class.forName(s);
        throw new ClassNotFoundException("Suite não encontrada: " + name);
    }

    private static void runSuite(Class<?> cls, int[] result) throws Exception {
        System.out.println("\n╔══ " + cls.getSimpleName() + " ══╗");
        Object inst = cls.getDeclaredConstructor().newInstance();
        java.lang.reflect.Method[] methods = cls.getDeclaredMethods();
        java.util.Arrays.sort(methods, java.util.Comparator.comparing(java.lang.reflect.Method::getName));
        for (java.lang.reflect.Method m : methods)
            if (m.getAnnotation(Test.class) != null) runMethod(inst, m, result);
    }

    private static void runMethod(Object inst, java.lang.reflect.Method m, int[] result) {
        System.out.print("  " + m.getName() + " … ");
        try {
            m.invoke(inst);
            System.out.println("✓ PASS");
            result[0]++;
        } catch (java.lang.reflect.InvocationTargetException ite) {
            System.out.println("✗ FAIL");
            System.out.println("    → " + ite.getCause().getMessage());
            result[1]++;
            System.exit(1); // falha imediata quando corre 1 teste sozinho
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            result[1]++;
            System.exit(1);
        }
    }

    private static void printSummary(int passed, int failed) {
        System.out.printf("%n══ PASS: %d  FAIL: %d  TOTAL: %d ══%n", passed, failed, passed + failed);
        System.exit(failed > 0 ? 1 : 0);
    }
}
