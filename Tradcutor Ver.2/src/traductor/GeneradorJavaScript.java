package traductor;

import java.util.*;

/**
 * Recorre el AST (Nodo "programa") y emite código JavaScript equivalente.
 * Misma cobertura de construcciones que GeneradorPython — ver ese archivo
 * para el detalle de qué representa cada tipo de nodo.
 */
public class GeneradorJavaScript {

    private static final String INDENT = "  ";

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
            case "asignacion":          return tab(nivel) + n.valor + " = " + generarExpr(n.hijo(0)) + ";\n";
            case "asignacionSimple":    return n.valor + " = " + generarExpr(n.hijo(0));
            case "incrementoDecremento": return generarIncrementoDecremento(n, nivel);
            case "condicion":           return generarCondicion(n, nivel);
            case "cicloMientras":       return tab(nivel) + "while (" + generarExpr(n.hijo(0)) + ") {\n"
                                                 + generarBloque(n.hijo(1), nivel + 1)
                                                 + tab(nivel) + "}\n";
            case "cicloPara":           return generarPara(n, nivel);
            case "cicloDoWhile":        return generarDoWhile(n, nivel);
            case "cicloForeach":        return tab(nivel) + "for (const " + n.valor + " of " + n.atributo("coleccion") + ") {\n"
                                                 + generarBloque(n.hijo(0), nivel + 1)
                                                 + tab(nivel) + "}\n";
            case "estructuraSwitch":    return generarSwitch(n, nivel);
            case "escritura":           return tab(nivel) + "console.log(" + generarExpr(n.hijo(0)) + ");\n";
            case "breakInstruccion":    return tab(nivel) + "break;\n";
            case "continueInstruccion": return tab(nivel) + "continue;\n";
            case "returnInstruccion":   return tab(nivel) + "return" + (n.hijos.isEmpty() ? "" : " " + generarExpr(n.hijo(0))) + ";\n";
            case "bloque":              return generarBloque(n, nivel);
            default:
                return tab(nivel) + "// TODO: nodo no soportado aun -> " + n.tipo + "\n";
        }
    }

    private String generarDeclaracion(Nodo n, int nivel) {
        String nombre = n.valor;
        if (!n.hijos.isEmpty()) {
            return tab(nivel) + "let " + nombre + " = " + generarExpr(n.hijo(0)) + ";\n";
        }
        String valorDefault = switch (n.atributoPorDefecto("tipoDato", "")) {
            case "int", "long", "short", "byte" -> "0";
            case "double", "float" -> "0.0";
            case "boolean" -> "false";
            case "string" -> "\"\"";
            case "char" -> "''";
            default -> "null";
        };
        return tab(nivel) + "let " + nombre + " = " + valorDefault + ";\n";
    }

    private String generarIncrementoDecremento(Nodo n, int nivel) {
        String operador = n.atributoPorDefecto("operador", "");

        if ("++".equals(operador)) {
            return tab(nivel) + n.valor + "++;\n";
        }

        if ("--".equals(operador)) {
            return tab(nivel) + n.valor + "--;\n";
        }

        return "";
    }
    
    private String generarCondicion(Nodo n, int nivel) {
        StringBuilder sb = new StringBuilder();
        sb.append(tab(nivel)).append("if (").append(generarExpr(n.hijo(0))).append(") {\n");
        sb.append(generarBloque(n.hijo(1), nivel + 1));
        if ("true".equals(n.atributo("tieneElse"))) {
            sb.append(tab(nivel)).append("} else {\n");
            sb.append(generarBloque(n.hijo(2), nivel + 1));
        }
        sb.append(tab(nivel)).append("}\n");
        return sb.toString();
    }

    private String generarPara(Nodo n, int nivel) {
        Nodo init = n.hijo(0);
        Nodo cond = n.hijo(1);
        Nodo upd = n.hijo(2);
        Nodo cuerpo = n.hijo(3);

        StringBuilder sb = new StringBuilder();

        // -------------------------
        // INICIALIZACION
        // -------------------------
        sb.append(tab(nivel));

        String opInit = init.atributoPorDefecto("operadorAsignacion", "=");

        if ("++".equals(opInit)) {
            sb.append(init.valor).append("++");
        } else if ("--".equals(opInit)) {
            sb.append(init.valor).append("--");
        } else {
            sb.append("let ")
              .append(init.valor)
              .append(" = ")
              .append(generarExpr(init.hijo(0)));
        }

        sb.append("; ");

        // -------------------------
        // CONDICION
        // -------------------------
        sb.append(generarExpr(cond));
        sb.append("; ");

        // -------------------------
        // ACTUALIZACION
        // -------------------------
        String opUpd = upd.atributoPorDefecto("operadorAsignacion", "=");

        if ("++".equals(opUpd)) {
            sb.append(upd.valor).append("++");
        } else if ("--".equals(opUpd)) {
            sb.append(upd.valor).append("--");
        } else {
            sb.append(upd.valor)
              .append(" = ")
              .append(generarExpr(upd.hijo(0)));
        }

        sb.append(" {\n");

        // -------------------------
        // CUERPO
        // -------------------------
        sb.append(generarBloque(cuerpo, nivel + 1));

        sb.append(tab(nivel)).append("}\n");

        return sb.toString();
    }

    private String generarDoWhile(Nodo n, int nivel) {
        Nodo cuerpo = n.hijo(0), cond = n.hijo(1);
        StringBuilder sb = new StringBuilder();
        sb.append(tab(nivel)).append("do {\n");
        sb.append(generarBloque(cuerpo, nivel + 1));
        sb.append(tab(nivel)).append("} while (").append(generarExpr(cond)).append(");\n");
        return sb.toString();
    }

    private String generarSwitch(Nodo n, int nivel) {
        StringBuilder sb = new StringBuilder();
        sb.append(tab(nivel)).append("switch (").append(generarExpr(n.hijo(0))).append(") {\n");
        for (int i = 1; i < n.hijos.size(); i++) {
            Nodo caso = n.hijo(i);
            if (caso.tipo.equals("casoSwitch")) {
                sb.append(tab(nivel + 1)).append("case ").append(generarExpr(caso.hijo(0))).append(":\n");
                sb.append(generarCuerpoCaso(caso, 1, nivel + 2));
                sb.append(tab(nivel + 2)).append("break;\n");
            } else if (caso.tipo.equals("casoDefault")) {
                sb.append(tab(nivel + 1)).append("default:\n");
                sb.append(generarCuerpoCaso(caso, 0, nivel + 2));
                sb.append(tab(nivel + 2)).append("break;\n");
            }
        }
        sb.append(tab(nivel)).append("}\n");
        return sb.toString();
    }

    private String generarCuerpoCaso(Nodo caso, int desdeIndice, int nivel) {
        StringBuilder sb = new StringBuilder();
        for (int j = desdeIndice; j < caso.hijos.size(); j++) {
            sb.append(generarInstruccion(caso.hijo(j), nivel));
        }
        return sb.toString();
    }

    private String generarBloque(Nodo bloque, int nivel) {
        if (bloque == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Nodo inst : bloque.hijos) {
            sb.append(generarInstruccion(inst, nivel));
        }
        return sb.toString();
    }

    private String generarExpr(Nodo n) {

        if (n == null) {
            return "";
        }

        if (n.tipo.equals("valor")) {
            return generarValor(n);
        }

        if (n.tipo.equals("agrupacion")) {
            return "(" + generarExpr(n.hijo(0)) + ")";
        }

        String op = n.atributo("operador");

        if ("!".equals(op)) {
            return "!" + generarExpr(n.hijo(0));
        }

        if ("unario-".equals(op)) {
            return "-" + generarExpr(n.hijo(0));
        }

        if (n.hijos.size() == 2 && op != null) {
            String izquierda = generarExpr(n.hijo(0));
            String derecha = generarExpr(n.hijo(1));

            String operador = op;

            if ("&&".equals(operador)) {
                operador = "&&";
            } else if ("||".equals(operador)) {
                operador = "||";
            }

            return izquierda + " " + operador + " " + derecha;
        }

        if (n.hijos.size() == 1) {
            return generarExpr(n.hijo(0));
        }

        return "";
    }

    private String generarValor(Nodo v) {
        String tipo = v.atributo("tipoValor");
        return switch (tipo == null ? "" : tipo) {
            case "true" -> "true";
            case "false" -> "false";
            case "null" -> "null";
            case "cadena" -> normalizarCadena(v.valor);
            default -> v.valor;
        };
    }

    private String normalizarCadena(String lexema) {
        if (lexema.startsWith("'") && lexema.endsWith("'")) {
            return "\"" + lexema.substring(1, lexema.length() - 1) + "\"";
        }
        return lexema;
    }
}
