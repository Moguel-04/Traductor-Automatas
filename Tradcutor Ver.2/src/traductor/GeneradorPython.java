package traductor;

import java.util.*;

/**
 * Recorre el AST (Nodo "programa") y emite código Python equivalente.
 *
 * Cobertura actual (mismas construcciones que ya reconoce la gramática):
 *   declaracionVariable, asignacion, condicion (if/else), cicloMientras,
 *   cicloPara, cicloDoWhile, cicloForeach, estructuraSwitch, escritura,
 *   break/continue/return, bloque, expresiones aritméticas y lógicas.
 *
 * Es intencionalmente simple (concatenación de strings con indentación
 * manual) para que sea fácil de leer, depurar y corregir/ampliar.
 */
public class GeneradorPython {

    private static final String INDENT = "    ";

    public String generar(Nodo raiz) {
        StringBuilder sb = new StringBuilder();
        for (Nodo hijo : raiz.hijos) {
            sb.append(generarInstruccion(hijo, 0));
        }
        return sb.toString();
    }

    private String tab(int nivel) {
        return INDENT.repeat(Math.max(0, nivel));
    }

    private String generarInstruccion(Nodo n, int nivel) {
        switch (n.tipo) {
            case "declaracionVariable": return generarDeclaracion(n, nivel);
            case "asignacion":          return tab(nivel) + n.valor + " = " + generarExpr(n.hijo(0)) + "\n";
            case "asignacionSimple":    return tab(nivel) + n.valor + " = " + generarExpr(n.hijo(0)) + "\n";
            case "incrementoDecremento": return generarIncrementoDecremento(n, nivel);
            case "condicion":           return generarCondicion(n, nivel);
            case "cicloMientras":       return tab(nivel) + "while " + generarExpr(n.hijo(0)) + ":\n"
                                                 + generarBloque(n.hijo(1), nivel + 1);
            case "cicloPara":           return generarPara(n, nivel);
            case "cicloDoWhile":        return generarDoWhile(n, nivel);
            case "cicloForeach":        return tab(nivel) + "for " + n.valor + " in " + n.atributo("coleccion") + ":\n"
                                                 + generarBloque(n.hijo(0), nivel + 1);
            case "estructuraSwitch":    return generarSwitch(n, nivel);
            case "escritura":           return tab(nivel) + "print(" + generarExpr(n.hijo(0)) + ")\n";
            case "breakInstruccion":    return tab(nivel) + "break\n";
            case "continueInstruccion": return tab(nivel) + "continue\n";
            case "returnInstruccion":   return tab(nivel) + "return" + (n.hijos.isEmpty() ? "" : " " + generarExpr(n.hijo(0))) + "\n";
            case "bloque":              return generarBloque(n, nivel);
            default:
                return tab(nivel) + "# TODO: nodo no soportado aun -> " + n.tipo + "\n";
        }
    }

    private String generarDeclaracion(Nodo n, int nivel) {
        String nombre = n.valor;
        if (!n.hijos.isEmpty()) {
            return tab(nivel) + nombre + " = " + generarExpr(n.hijo(0)) + "\n";
        }
        // sin valor inicial -> usar un valor por defecto según el tipo de C#
        String valorDefault = switch (n.atributoPorDefecto("tipoDato", "")) {
            case "int", "long", "short", "byte" -> "0";
            case "double", "float" -> "0.0";
            case "boolean" -> "False";
            case "string" -> "\"\"";
            case "char" -> "''";
            default -> "None";
        };
        return tab(nivel) + nombre + " = " + valorDefault + "\n";
    }
    
    private String generarIncrementoDecremento(Nodo n, int nivel) {
        String operador = n.atributoPorDefecto("operador", "");

        if ("++".equals(operador)) {
            return tab(nivel) + n.valor + " += 1\n";
        }

        if ("--".equals(operador)) {
            return tab(nivel) + n.valor + " -= 1\n";
        }

        return "";
    }
    
    private String generarCondicion(Nodo n, int nivel) {
        StringBuilder sb = new StringBuilder();
        sb.append(tab(nivel)).append("if ").append(generarExpr(n.hijo(0))).append(":\n");
        sb.append(generarBloque(n.hijo(1), nivel + 1));
        if ("true".equals(n.atributo("tieneElse"))) {
            sb.append(tab(nivel)).append("else:\n");
            sb.append(generarBloque(n.hijo(2), nivel + 1));
        }
        return sb.toString();
    }

    private String generarPara(Nodo n, int nivel) {
        Nodo init = n.hijo(0);
        Nodo cond = n.hijo(1);
        Nodo upd = n.hijo(2);
        Nodo cuerpo = n.hijo(3);

        StringBuilder sb = new StringBuilder();

        // Inicialización
        if ("++".equals(init.atributo("operadorAsignacion"))) {
            sb.append(tab(nivel)).append(init.valor).append(" += 1\n");
        } else if ("--".equals(init.atributo("operadorAsignacion"))) {
            sb.append(tab(nivel)).append(init.valor).append(" -= 1\n");
        } else {
            sb.append(tab(nivel))
              .append(init.valor)
              .append(" = ")
              .append(generarExpr(init.hijo(0)))
              .append("\n");
        }

        // Condición
        sb.append(tab(nivel))
          .append("while ")
          .append(generarExpr(cond))
          .append(":\n");

        sb.append(generarBloque(cuerpo, nivel + 1));

        // Actualización
        String operador = upd.atributoPorDefecto("operadorAsignacion", "=");

        if ("++".equals(operador)) {
            sb.append(tab(nivel + 1))
              .append(upd.valor)
              .append(" += 1\n");
        } else if ("--".equals(operador)) {
            sb.append(tab(nivel + 1))
              .append(upd.valor)
              .append(" -= 1\n");
        } else {
            sb.append(tab(nivel + 1))
              .append(upd.valor)
              .append(" = ")
              .append(generarExpr(upd.hijo(0)))
              .append("\n");
        }

        return sb.toString();
    }

    private String generarDoWhile(Nodo n, int nivel) {
        Nodo cuerpo = n.hijo(0), cond = n.hijo(1);
        StringBuilder sb = new StringBuilder();
        sb.append(tab(nivel)).append("while True:\n");
        sb.append(generarBloque(cuerpo, nivel + 1));
        sb.append(tab(nivel + 1)).append("if not (").append(generarExpr(cond)).append("):\n");
        sb.append(tab(nivel + 2)).append("break\n");
        return sb.toString();
    }

    private String generarSwitch(Nodo n, int nivel) {
        // Se traduce como cadena if / elif / else para mantener compatibilidad
        // con cualquier version de Python (match/case requiere 3.10+).
        StringBuilder sb = new StringBuilder();
        String expr = generarExpr(n.hijo(0));
        boolean primero = true;
        for (int i = 1; i < n.hijos.size(); i++) {
            Nodo caso = n.hijo(i);
            if (caso.tipo.equals("casoSwitch")) {
                String etiqueta = primero ? "if" : "elif";
                sb.append(tab(nivel)).append(etiqueta).append(" ").append(expr)
                  .append(" == ").append(generarExpr(caso.hijo(0))).append(":\n");
                sb.append(generarCuerpoCaso(caso, 1, nivel + 1));
                primero = false;
            } else if (caso.tipo.equals("casoDefault")) {
                sb.append(tab(nivel)).append("else:\n");
                sb.append(generarCuerpoCaso(caso, 0, nivel + 1));
            }
        }
        return sb.toString();
    }

    private String generarCuerpoCaso(Nodo caso, int desdeIndice, int nivel) {
        StringBuilder sb = new StringBuilder();
        for (int j = desdeIndice; j < caso.hijos.size(); j++) {
            sb.append(generarInstruccion(caso.hijo(j), nivel));
        }
        if (sb.length() == 0) sb.append(tab(nivel)).append("pass\n");
        return sb.toString();
    }

    private String generarBloque(Nodo bloque, int nivel) {
        if (bloque == null || bloque.hijos.isEmpty()) {
            return tab(nivel) + "pass\n";
        }
        StringBuilder sb = new StringBuilder();
        for (Nodo inst : bloque.hijos) {
            sb.append(generarInstruccion(inst, nivel));
        }
        return sb.toString();
    }

    /** Genera expresiones simples y expresiones anidadas. */
    private String generarExpr(Nodo n) {

        if (n == null) {
            return "";
        }

        // Valor simple
        if (n.tipo.equals("valor")) {
            return generarValor(n);
        }

        // Expresión agrupada con paréntesis
        if (n.tipo.equals("agrupacion")) {
            return "(" + generarExpr(n.hijo(0)) + ")";
        }

        String op = n.atributo("operador");

        // Negación lógica
        if ("!".equals(op)) {
            return "not " + generarExpr(n.hijo(0));
        }

        // Negación aritmética
        if ("unario-".equals(op)) {
            return "-" + generarExpr(n.hijo(0));
        }

        // Expresión con dos operandos
        if (n.hijos.size() == 2 && op != null) {
            String izquierda = generarExpr(n.hijo(0));
            String derecha = generarExpr(n.hijo(1));

            String operador = op;

            // Operadores lógicos de C# a Python
            if ("&&".equals(operador)) {
                operador = "and";
            } else if ("||".equals(operador)) {
                operador = "or";
            }

            return izquierda + " " + operador + " " + derecha;
        }

        // Si el nodo solo contiene una expresión, la seguimos recorriendo
        if (n.hijos.size() == 1) {
            return generarExpr(n.hijo(0));
        }

        return "";
}

    private String generarValor(Nodo v) {
        String tipo = v.atributo("tipoValor");
        return switch (tipo == null ? "" : tipo) {
            case "true" -> "True";
            case "false" -> "False";
            case "null" -> "None";
            case "cadena" -> normalizarCadena(v.valor);
            default -> v.valor; // identificador, numero, decimal
        };
    }

    private String normalizarCadena(String lexema) {
        // Convierte comillas simples de C# a comillas dobles estilo Python
        if (lexema.startsWith("'") && lexema.endsWith("'")) {
            return "\"" + lexema.substring(1, lexema.length() - 1) + "\"";
        }
        return lexema;
    }
}
