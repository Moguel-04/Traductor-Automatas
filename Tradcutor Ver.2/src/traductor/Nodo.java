package traductor;

import java.util.*;

/**
 * Nodo genérico del Árbol de Sintaxis Abstracta (AST).
 *
 * En vez de crear una clase distinta por cada construcción del lenguaje
 * (NodoIf, NodoWhile, NodoAsignacion, ...), usamos UN solo tipo de nodo
 * con:
 *   - tipo:      qué construcción representa ("condicion", "cicloMientras",
 *                "asignacion", "valor", etc.) — lo usan los generadores
 *                para saber qué emitir.
 *   - valor:     el lexema principal cuando aplica (nombre de variable,
 *                operador, literal, etc.)
 *   - atributos: datos extra con forma clave->valor (ej. tipoDato=int)
 *   - hijos:     sub-nodos, en el orden en que deben procesarse.
 *
 * Es más fácil de extender que crear una jerarquía de clases: si agregas
 * una construcción nueva al lenguaje, solo agregas un "tipo" nuevo y un
 * caso nuevo en los generadores.
 */
public class Nodo {

    public String tipo;
    public String valor;
    public Map<String, String> atributos = new LinkedHashMap<>();
    public List<Nodo> hijos = new ArrayList<>();

    public Nodo(String tipo) {
        this.tipo = tipo;
    }

    public Nodo(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public Nodo agregar(Nodo hijo) {
        if (hijo != null) hijos.add(hijo);
        return this;
    }

    // Guarda un atributo
    public Nodo atributo(String clave, String valor) {
        atributos.put(clave, valor);
        return this;
    }

    // Obtiene un atributo
    public String atributo(String clave) {
        return atributos.get(clave);
    }

    // Obtiene un atributo y devuelve un valor por defecto si no existe
    public String atributoPorDefecto(String clave, String porDefecto) {
        return atributos.getOrDefault(clave, porDefecto);
    }

    public Nodo hijo(int i) {
        return (i >= 0 && i < hijos.size()) ? hijos.get(i) : null;
    }

    /** Representación en texto del árbol, útil para depurar. */
    @Override
    public String toString() {
        return toString(0);
    }

    private String toString(int nivel) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(nivel)).append(tipo);

        if (valor != null) {
            sb.append(" [").append(valor).append("]");
        }

        if (!atributos.isEmpty()) {
            sb.append(" ").append(atributos);
        }

        sb.append("\n");

        for (Nodo h : hijos) {
            sb.append(h.toString(nivel + 1));
        }

        return sb.toString();
    }
}