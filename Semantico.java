import java.util.*;

/**
 * RECONSTRUIDA a partir del uso que hace Analizador.jj:
 *   Semantico.limpiar(), Semantico.declararVariable(nombre, tipo),
 *   Semantico.validarOperacionAritmetica(tipo1, tipo2, operador),
 *   Semantico.hayErrores(), Semantico.imprimirErrores(), Semantico.error(msg)
 * Si ya tienes tu propia versión original, reemplaza este archivo por el tuyo
 * (revisa que los nombres de método coincidan con lo que llama tu .jj real).
 */
public class Semantico {

    private static List<String> errores = new ArrayList<>();
    private static Map<String, String> tablaVariables = new HashMap<>();

    public static void limpiar() {
        errores.clear();
        tablaVariables.clear();
    }

    public static void declararVariable(String nombre, String tipo) {
        if (tablaVariables.containsKey(nombre)) {
            errores.add("Variable '" + nombre + "' ya fue declarada.");
        } else {
            tablaVariables.put(nombre, tipo);
        }
    }

    /**
     * Validación mínima: no permite mezclar cadenas con operadores aritméticos
     * distintos de '+' (concatenación). Ajusta las reglas según lo que
     * necesites para tu proyecto.
     */
    public static void validarOperacionAritmetica(String tipo1, String tipo2, String operador) {
        boolean hayCadena = "cadena".equals(tipo1) || "cadena".equals(tipo2);
        if (hayCadena && !"+".equals(operador)) {
            errores.add("Operación '" + operador + "' inválida entre tipos '" + tipo1 + "' y '" + tipo2 + "'.");
        }
    }

    public static void error(String mensaje) {
        errores.add(mensaje);
    }

    public static boolean hayErrores() {
        return !errores.isEmpty();
    }

    public static void imprimirErrores() {
        for (String e : errores) {
            System.out.println("  ✗ " + e);
        }
    }
}
