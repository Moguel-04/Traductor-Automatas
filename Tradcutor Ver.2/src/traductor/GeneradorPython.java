package traductor;

import java.util.*;

public class GeneradorPython {

    private static final String INDENT = "    ";
    // Permite distinguir si un break pertenece a un ciclo o a un switch.
    private final Deque<String> contextosRuptura = new ArrayDeque<>();
    // Guarda la actualización correspondiente al ciclo donde aparece continue.
    private final Deque<String> actualizacionesContinue = new ArrayDeque<>();

    public String generar(Nodo raiz) {
        contextosRuptura.clear();
        actualizacionesContinue.clear();

        StringBuilder sb = new StringBuilder();

        if (contieneConversion(raiz)
                || contieneDivisionEntera(raiz)
                || contieneModuloEntero(raiz)
                || contieneModuloDecimal(raiz)) {

            if (contieneConversion(raiz)) {
                sb.append(generarAuxiliaresConversion());
                sb.append("\n");
            }

            if (contieneDivisionEntera(raiz)) {
                sb.append(generarAuxiliarDivisionEntera());
                sb.append("\n");
            }

            if (contieneModuloEntero(raiz)) {
                sb.append(generarAuxiliarModuloEntero());
                sb.append("\n");
            }

            if (contieneModuloDecimal(raiz)) {
                sb.append(generarAuxiliarModuloDecimal());
                sb.append("\n");
            }
        }

        for (Nodo hijo : raiz.hijos) {
            sb.append(generarInstruccion(hijo, 0));
        }

        return sb.toString();
    }
    
    private String tab(int nivel) {
        return INDENT.repeat(Math.max(0, nivel));
    }

    private boolean contieneConversion(Nodo n) {
        if (n == null) {
            return false;
        }

        if ("conversionTipo".equals(n.tipo)) {
            return true;
        }

        for (Nodo hijo : n.hijos) {
            if (contieneConversion(hijo)) {
                return true;
            }
        }

        return false;
    }
    
    private boolean contieneDivisionEntera(Nodo n) {
        if (n == null) {
            return false;
        }
        
        if (("asignacion".equals(n.tipo)
                || "asignacionSimple".equals(n.tipo))
                && "/=".equals(n.atributo("operadorAsignacion"))
                && "int".equals(
                        normalizarTipo(Semantico.obtenerTipoVariable(n.valor)))) {
            return true;
        }
        
        if ("expresion".equals(n.tipo)
                && "/".equals(n.atributo("operador"))
                && n.hijos.size() == 2
                && "int".equals(inferirTipoExpr(n.hijo(0)))
                && "int".equals(inferirTipoExpr(n.hijo(1)))) {
            return true;
        }

        for (Nodo hijo : n.hijos) {
            if (contieneDivisionEntera(hijo)) {
                return true;
            }
        }

        return false;
    }

    private boolean contieneModuloEntero(Nodo n) {
        if (n == null) {
            return false;
        }

        // Módulo entre dos expresiones enteras.
        if ("expresion".equals(n.tipo)
                && "%".equals(n.atributo("operador"))
                && n.hijos.size() == 2
                && "int".equals(inferirTipoExpr(n.hijo(0)))
                && "int".equals(inferirTipoExpr(n.hijo(1)))) {
            return true;
        }

        // Asignación compuesta de módulo sobre una variable entera.
        if (("asignacion".equals(n.tipo)
                || "asignacionSimple".equals(n.tipo))
                && "%=".equals(n.atributo("operadorAsignacion"))
                && "int".equals(
                        normalizarTipo(
                                Semantico.obtenerTipoVariable(n.valor)))) {
            return true;
        }

        for (Nodo hijo : n.hijos) {
            if (contieneModuloEntero(hijo)) {
                return true;
            }
        }

        return false;
    }
    
    private boolean contieneModuloDecimal(Nodo n) {
        if (n == null) {
            return false;
        }

        // Modulo donde al menos uno de los operandos es decimal.
        if ("expresion".equals(n.tipo)
                && "%".equals(n.atributo("operador"))
                && n.hijos.size() == 2) {

            String tipoIzquierdo = inferirTipoExpr(n.hijo(0));
            String tipoDerecho = inferirTipoExpr(n.hijo(1));

            if ("float".equals(tipoIzquierdo)
                    || "float".equals(tipoDerecho)) {
                return true;
            }
        }

        // Asignacion compuesta de modulo sobre una variable decimal.
        if (("asignacion".equals(n.tipo)
                || "asignacionSimple".equals(n.tipo))
                && "%=".equals(n.atributo("operadorAsignacion"))
                && "float".equals(
                        normalizarTipo(
                                Semantico.obtenerTipoVariable(n.valor)))) {
            return true;
        }

        for (Nodo hijo : n.hijos) {
            if (contieneModuloDecimal(hijo)) {
                return true;
            }
        }

        return false;
    }
    
    private String generarAuxiliarDivisionEntera() {
        return """
            # Division entera compatible con C#.

            def _cs_integer_division(a, b):
                if b == 0:
                    raise ZeroDivisionError("Division entre cero")

                resultado = abs(a) // abs(b)

                if (a < 0) != (b < 0):
                    return -resultado

                return resultado

            """;
    }
    
    private String generarAuxiliarModuloEntero() {
        return """
            # Modulo entero compatible con C#.

            def _cs_integer_modulo(a, b):
                if b == 0:
                    raise ZeroDivisionError("Modulo entre cero")

                cociente = abs(a) // abs(b)

                if (a < 0) != (b < 0):
                    cociente = -cociente

                return a - cociente * b

            """;
    }
    
    private String generarAuxiliarModuloDecimal() {
        return """
            # Modulo decimal compatible con C#.

            import math

            def _cs_float_modulo(a, b):
                if b == 0:
                    raise ZeroDivisionError("Modulo entre cero")

                return math.fmod(a, b)

            """;
    }

    private String generarAuxiliaresConversion() {
        return """
            # Funciones auxiliares para conservar el comportamiento de C#.

            def _cs_parse_integer(valor, minimo, maximo):
                texto = str(valor).strip()
                digitos = texto[1:] if texto[:1] in ("+", "-") else texto

                if not digitos or not digitos.isascii() or not digitos.isdigit():
                    raise ValueError("Formato de entero no valido")

                resultado = int(texto, 10)

                if resultado < minimo or resultado > maximo:
                    raise OverflowError("Valor fuera del rango permitido")

                return resultado


            def _cs_parse_float(valor):
                return float(str(valor).strip())


            def _cs_parse_bool(valor):
                texto = str(valor).strip().lower()

                if texto == "true":
                    return True

                if texto == "false":
                    return False

                raise ValueError("La cadena no representa un valor booleano valido")


            def _cs_parse_char(valor):
                texto = str(valor)

                if len(texto) != 1:
                    raise ValueError("Se esperaba exactamente un caracter")

                return texto


            def _cs_convert_integer(valor, minimo, maximo):
                if valor is None:
                    resultado = 0
                elif isinstance(valor, bool):
                    resultado = int(valor)
                elif isinstance(valor, str):
                    return _cs_parse_integer(valor, minimo, maximo)
                else:
                    resultado = int(round(valor))

                if resultado < minimo or resultado > maximo:
                    raise OverflowError("Valor fuera del rango permitido")

                return resultado

               
            def _cs_convert_char_to_integer(valor, minimo, maximo):
                if not isinstance(valor, str) or len(valor) != 1:
                    raise TypeError("Tipo no valido para Convert.ToInt32")

                resultado = ord(valor)

                if resultado < minimo or resultado > maximo:
                    raise OverflowError("Valor fuera del rango permitido")

                return resultado
               

            def _cs_convert_float(valor):
                if valor is None:
                    return 0.0

                return float(valor)


            def _cs_convert_single(valor):
                import struct

                resultado = _cs_convert_float(valor)

                try:
                    return struct.unpack("f", struct.pack("f", resultado))[0]
                except OverflowError:
                    raise OverflowError("Valor fuera del rango de Single")


            def _cs_convert_bool(valor):
                if valor is None:
                    return False

                if isinstance(valor, str):
                    return _cs_parse_bool(valor)

                return bool(valor)


            def _cs_convert_char(valor):
                if valor is None:
                    return "\\0"

                if isinstance(valor, str):
                    if len(valor) != 1:
                        raise ValueError("Se esperaba exactamente un caracter")

                    return valor

                if isinstance(valor, bool) or not isinstance(valor, int):
                    raise TypeError("Tipo no valido para Convert.ToChar")

                if valor < 0 or valor > 65535:
                    raise OverflowError("Valor fuera del rango de Char")

                return chr(valor)


            def _cs_convert_string(valor):
                if valor is None:
                    return ""

                return str(valor)

            """;
    }
    
    private String generarInstruccion(Nodo n, int nivel) {
        switch (n.tipo) {
            case "comentario": return generarComentario(n, nivel);
            case "declaracionVariable": return generarDeclaracion(n, nivel);
            case "declaracionesMultiples": StringBuilder declaraciones = new StringBuilder();
                for (Nodo variable : n.hijos) {
                    declaraciones.append(
                        generarDeclaracion(variable, nivel)
                    );
                }
                return declaraciones.toString();
            case "operacionArreglo":    return generarOperacionArreglo(n, nivel);
            case "copiarArreglo":       return generarCopiarArreglo(n, nivel);
            case "asignacion":          return generarAsignacion(n, nivel);
            case "asignacionSimple":    return generarAsignacion(n, nivel);
            case "incrementoDecremento": return generarIncrementoDecremento(n, nivel);
            case "condicion":           return generarCondicion(n, nivel);
            case "cicloPara":           return generarPara(n, nivel);
            case "manejoExcepciones":   return generarManejoExcepciones(n, nivel);
            case "throwInstruccion":    return generarThrow(n, nivel);
            case "cicloDoWhile":        return generarDoWhile(n, nivel);
            case "estructuraSwitch":    return generarSwitch(n, nivel);
            case "escritura":           return tab(nivel) + "print(" + generarExpr(n.hijo(0)) + ")\n";
            case "lecturaConsola":      return tab(nivel) + "input()\n";
            case "cicloMientras":       return generarMientras(n, nivel);
            case "cicloForeach":        return generarForeach(n, nivel);
            case "breakInstruccion":    return generarBreak(nivel);
            case "continueInstruccion": return generarContinue(nivel);
            case "funcion":             return generarFuncion(n, nivel);
            case "llamadaFuncion": return tab(nivel) + generarExpr(n) + "\n";
            case "returnInstruccion":   return tab(nivel) + "return" + (n.hijos.isEmpty() ? "" : " " + generarExpr(n.hijo(0))) + "\n";
            case "bloque":              return generarBloque(n, nivel);
            default:
                return tab(nivel) + "# TODO: nodo no soportado aun -> " + n.tipo + "\n";
        }
    }
    
    private String generarComentario(Nodo n, int nivel) {
        String comentario = n.valor;

        if (comentario.startsWith("//")) {
            return tab(nivel)
                    + "#"
                    + comentario.substring(2)
                    + "\n";
        }

        if (comentario.startsWith("/*")
                && comentario.endsWith("*/")) {

            String contenido = comentario.substring(
                    2, comentario.length() - 2
            );

            String[] lineas = contenido.split("\\R", -1);
            StringBuilder resultado = new StringBuilder();

            for (String linea : lineas) {
                resultado.append(tab(nivel))
                        .append("#")
                        .append(linea.isEmpty() ? "" : " " + linea.stripLeading())
                        .append("\n");
            }

            return resultado.toString();
        }

        return tab(nivel) + "# " + comentario + "\n";
    }

    private String generarDeclaracion(Nodo n, int nivel) {
        String nombre = n.valor;

        if (!n.hijos.isEmpty()) {
            return tab(nivel) + nombre + " = "
                    + generarExpr(n.hijo(0)) + "\n";
        }

        return "";
    }
    
    private String generarAsignacion(Nodo n, int nivel) {
        String operador = n.atributoPorDefecto(
                "operadorAsignacion", "=");

        String destino;
        Nodo expresion;

        if ("true".equals(n.atributo("asignacionArreglo"))) {
            destino = n.valor + "[" + generarExpr(n.hijo(0)) + "]";
            expresion = n.hijo(1);
        } else {
            destino = n.valor;
            expresion = n.hijo(0);
        }

        String valor = generarExpr(expresion);

        // C# realiza división entera cuando el destino es entero.
        if ("/=".equals(operador)
                && "int".equals(
                        normalizarTipo(Semantico.obtenerTipoVariable(n.valor)))
                && "int".equals(inferirTipoExpr(expresion))) {

            return tab(nivel)
                    + destino + " = _cs_integer_division("
                    + destino + ", " + valor + ")\n";
        }

        // Módulo entero compatible con C#.
        if ("%=".equals(operador)
                && "int".equals(
                        normalizarTipo(
                                Semantico.obtenerTipoVariable(n.valor)))
                && "int".equals(inferirTipoExpr(expresion))) {

            return tab(nivel)
                    + destino + " = _cs_integer_modulo("
                    + destino + ", " + valor + ")\n";
        }
        
        if ("%=".equals(operador)
                && "float".equals(
                        normalizarTipo(
                                Semantico.obtenerTipoVariable(n.valor)))) {

            return tab(nivel) + destino + " = _cs_float_modulo("
                    + destino + ", " + valor + ")\n";
        }
        
        return tab(nivel) + destino + " " + operador + " "
                + valor + "\n";
    }

    private String generarContinue(int nivel) {
        StringBuilder sb = new StringBuilder();

        if (!actualizacionesContinue.isEmpty()) {
            String actualizacion = actualizacionesContinue.peek();

            if (!actualizacion.isEmpty()) {
                sb.append(tab(nivel))
                  .append(actualizacion)
                  .append("\n");
            }
        }

        sb.append(tab(nivel)).append("continue\n");

        return sb.toString();
    }
    
    private String generarFuncion(Nodo n, int nivel) {
        StringBuilder sb = new StringBuilder();

        String nombre = n.valor;
        List<String> parametros = new ArrayList<>();

        // Todos los hijos excepto el último son parámetros.
        int cantidadParametros = Math.max(0, n.hijos.size() - 1);

        for (int i = 0; i < cantidadParametros; i++) {
            Nodo parametro = n.hijos.get(i);

            if ("parametro".equals(parametro.tipo)) {
                parametros.add(parametro.valor);
            }
        }

        // Generar encabezado de la función.
        sb.append(tab(nivel))
                .append("def ")
                .append(nombre)
                .append("(")
                .append(String.join(", ", parametros))
                .append("):\n");

        // Generar cuerpo de la función.
        if (!n.hijos.isEmpty()) {
            Nodo cuerpo = n.hijos.get(n.hijos.size() - 1);

            if ("bloque".equals(cuerpo.tipo)) {
                sb.append(generarBloque(cuerpo, nivel + 1));
            } else {
                sb.append(tab(nivel + 1)).append("pass\n");
            }
        } else {
            sb.append(tab(nivel + 1)).append("pass\n");
        }

        sb.append("\n");

        return sb.toString();
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
    
    private String generarMientras(Nodo n, int nivel) {
        actualizacionesContinue.push("");

        try {
            return tab(nivel)
                    + "while " + generarExpr(n.hijo(0)) + ":\n"
                    + generarBloqueConContexto(
                        n.hijo(1), nivel + 1, "ciclo"
                    );
        } finally {
            actualizacionesContinue.pop();
        }
    }

    private String generarForeach(Nodo n, int nivel) {
        actualizacionesContinue.push("");

        try {
            return tab(nivel)
                    + "for " + n.valor + " in "
                    + n.atributo("coleccion") + ":\n"
                    + generarBloqueConContexto(
                        n.hijo(0), nivel + 1, "ciclo"
                    );
        } finally {
            actualizacionesContinue.pop();
        }
    }

    private String generarBreak(int nivel) {
        if (!contextosRuptura.isEmpty()) {
            String contexto = contextosRuptura.peek();

            if ("switch".equals(contexto)) {
                // En C#, este break termina el switch.
                // El if/elif generado en Python termina por sí mismo.
                return "";
            }

            if ("ciclo".equals(contexto)) {
                return tab(nivel) + "break\n";
            }
        }

        // Evita generar un break inválido en Python.
        return tab(nivel)
                + "# break sin ciclo ni switch contenedor\n";
    }

    private String generarBloqueConContexto(
            Nodo bloque, int nivel, String contexto) {

        contextosRuptura.push(contexto);

        try {
            return generarBloque(bloque, nivel);
        } finally {
            contextosRuptura.pop();
        }
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

        String rango = construirRangePython(n);

        // Si la conversion es segura, utilizar range().
        if (rango != null) {

            String variable = obtenerVariableFor(n);

            return tab(nivel)
                    + "for " + variable + " in " + rango + ":\n"
                    + generarBloqueConContexto(
                            n.hijo(3), nivel + 1, "ciclo");
        }

        // Respaldo: conservar la condicion y actualizacion originales.
        Nodo init = n.hijo(0);
        Nodo cond = n.hijo(1);
        Nodo upd = n.hijo(2);
        Nodo cuerpo = n.hijo(3);

        StringBuilder sb = new StringBuilder();

        if (init != null && !"sinInicializacion".equals(init.tipo)) {
            sb.append(generarInstruccion(init, nivel));
        }

        String actualizacion = obtenerActualizacionPara(upd);

        sb.append(tab(nivel))
          .append("while ")
          .append(generarExpr(cond))
          .append(":\n");

        actualizacionesContinue.push(actualizacion);

        try {
            sb.append(
                generarBloqueConContexto(cuerpo, nivel + 1, "ciclo")
            );
        } finally {
            actualizacionesContinue.pop();
        }

        // La actualizacion tambien se ejecuta al terminar
        // normalmente cada iteracion.
        if (!actualizacion.isEmpty()) {
            sb.append(tab(nivel + 1))
              .append(actualizacion)
              .append("\n");
        }

        return sb.toString();
    }

    private String obtenerActualizacionPara(Nodo n) {
        if (n == null) {
            return "";
        }

        String operador = n.atributoPorDefecto(
            "operadorAsignacion", "="
        );

        if ("++".equals(operador)) {
            return n.valor + " += 1";
        }

        if ("--".equals(operador)) {
            return n.valor + " -= 1";
        }

        Nodo expresion = n.hijo(0);

        return n.valor + " " + operador + " " + generarExpr(expresion);
    }
    
    private String obtenerVariableFor(Nodo n) {

        if (n == null || n.hijos.isEmpty()) {
            return null;
        }

        Nodo init = n.hijo(0);

        if (init == null) {
            return null;
        }

        if ("declaracionesMultiples".equals(init.tipo)
                && !init.hijos.isEmpty()) {

            return init.hijo(0).valor;
        }

        if ("asignacionSimple".equals(init.tipo)) {
            return init.valor;
        }

        return null;
    }

    private String construirRangePython(Nodo n) {

        if (n == null || n.hijos.size() < 4) {
            return null;
        }

        Nodo init = n.hijo(0);
        Nodo cond = n.hijo(1);
        Nodo upd = n.hijo(2);
        Nodo cuerpo = n.hijo(3);

        if (init == null || cond == null || upd == null) {
            return null;
        }

        String variable = obtenerVariableFor(n);

        if (variable == null || variable.isEmpty()) {
            return null;
        }

        String inicio = obtenerInicioFor(init);

        if (inicio == null) {
            return null;
        }

        String fin = obtenerFinFor(cond, variable);

        if (fin == null) {
            return null;
        }

        String paso = obtenerPasoFor(upd, variable);

        if (paso == null) {
            return null;
        }

        // Comprobar que el paso tenga una direccion conocida.
        int signoPaso = obtenerSignoPaso(paso);

        if (signoPaso == 0) {
            return null;
        }

        String operador = cond.atributo("operador");

        // Evitar conversiones con direccion incompatible.
        if (("<".equals(operador) || "<=".equals(operador))
                && signoPaso < 0) {
            return null;
        }

        if ((">".equals(operador) || ">=".equals(operador))
                && signoPaso > 0) {
            return null;
        }

        // No convertir si el cuerpo modifica la variable de control.
        if (modificaVariable(cuerpo, variable)) {
            return null;
        }

        // Obtener las variables utilizadas en el limite.
        Set<String> variablesLimite = new HashSet<>();

        recolectarIdentificadores(cond.hijo(1), variablesLimite);

        // No convertir si el cuerpo modifica alguna variable del limite.
        for (String variableLimite : variablesLimite) {

            if (modificaVariable(cuerpo, variableLimite)) {
                return null;
            }
        }

        return "range(" + inicio + ", " + fin + ", " + paso + ")";
    }
    
    private boolean modificaVariable(Nodo n, String variable) {

        if (n == null || variable == null) {
            return false;
        }

        // Asignaciones normales y compuestas.
        if (("asignacion".equals(n.tipo)
                || "asignacionSimple".equals(n.tipo)
                || "incrementoDecremento".equals(n.tipo))
                && variable.equals(n.valor)) {

            return true;
        }

        // Revisar recursivamente los bloques y expresiones internas.
        for (Nodo hijo : n.hijos) {

            if (modificaVariable(hijo, variable)) {
                return true;
            }
        }

        return false;
    }
    
    private void recolectarIdentificadores(
            Nodo n, Set<String> identificadores) {

        if (n == null) {
            return;
        }

        // Identificar variables utilizadas en expresiones.
        if ("valor".equals(n.tipo)
                && "identificador".equals(
                        n.atributo("tipoValor"))
                && n.valor != null) {

            identificadores.add(n.valor);
        }

        // Recorrer expresiones anidadas.
        for (Nodo hijo : n.hijos) {

            recolectarIdentificadores(hijo, identificadores);
        }
    }
    
    private int obtenerSignoPaso(String paso) {

        if (paso == null) {
            return 0;
        }

        paso = paso.trim();

        try {
            long valor = Long.parseLong(paso);

            if (valor > 0) {
                return 1;
            }

            if (valor < 0) {
                return -1;
            }

            return 0;

        } catch (NumberFormatException e) {
            // No conocemos el signo de expresiones o variables.
            return 0;
        }
    }

    private String obtenerInicioFor(Nodo init) {

        if ("declaracionesMultiples".equals(init.tipo)
                && !init.hijos.isEmpty()) {

            Nodo v = init.hijo(0);

            if (!v.hijos.isEmpty()) {
                return generarExpr(v.hijo(0));
            }
        }

        if ("asignacionSimple".equals(init.tipo)
                && !init.hijos.isEmpty()) {

            return generarExpr(init.hijo(0));
        }

        return null;
    }

    private String obtenerFinFor(Nodo cond, String variable) {

        if (cond == null
                || !"expresionLogica".equals(cond.tipo)
                || cond.hijos.size() != 2) {
            return null;
        }

        String op = cond.atributo("operador");

        Nodo izq = cond.hijo(0);
        Nodo der = cond.hijo(1);

        if (!"valor".equals(izq.tipo)
                || !"identificador".equals(
                        izq.atributo("tipoValor"))
                || !variable.equals(izq.valor)) {
            return null;
        }

        if (!"<".equals(op)
                && !"<=".equals(op)
                && !">".equals(op)
                && !">=".equals(op)) {
            return null;
        }

        String limite = generarExpr(der);

        switch (op) {

            case "<":
            case ">":
                return limite;

            case "<=":
                return "(" + limite + " + 1)";

            case ">=":
                return "(" + limite + " - 1)";

            default:
                return null;
        }
    }

    private String obtenerPasoFor(Nodo upd, String variable) {

        if (upd == null || variable == null
                || !variable.equals(upd.valor)) {
            return null;
        }

        String op = upd.atributoPorDefecto(
                "operadorAsignacion", "=");

        switch (op) {

            case "++":
                return "1";

            case "--":
                return "-1";

            case "+=":
                if (upd.hijos.size() != 1) {
                    return null;
                }

                return generarExpr(upd.hijo(0));

            case "-=":
                if (upd.hijos.size() != 1) {
                    return null;
                }

                return "-(" + generarExpr(upd.hijo(0)) + ")";

            default:
                return null;
        }
    }
    
    private String generarManejoExcepciones(Nodo n, int nivel) {
        StringBuilder sb = new StringBuilder();

        Nodo cuerpoTry = n.hijo(0);

        sb.append(tab(nivel)).append("try:\n");
        sb.append(generarBloque(cuerpoTry, nivel + 1));

        boolean tieneCatch = false;
        boolean tieneFinally =
                "true".equals(n.atributo("tieneFinally"));

        for (int i = 1; i < n.hijos.size(); i++) {
            Nodo hijo = n.hijo(i);

            if ("clausulaCatch".equals(hijo.tipo)) {
                tieneCatch = true;

                String tipoExcepcion =
                        hijo.atributo("tipoExcepcion");
                String variableExcepcion =
                        hijo.atributo("variableExcepcion");

                String tipoPython =
                        convertirTipoExcepcion(tipoExcepcion);

                sb.append(tab(nivel))
                  .append("except ")
                  .append(tipoPython);

                if (variableExcepcion != null
                        && !variableExcepcion.isEmpty()) {
                    sb.append(" as ").append(variableExcepcion);
                }

                sb.append(":\n");
                sb.append(generarBloque(hijo.hijo(0), nivel + 1));
            } else if ("bloque".equals(hijo.tipo) && tieneFinally) {
                sb.append(tab(nivel)).append("finally:\n");
                sb.append(generarBloque(hijo, nivel + 1));
            }
        }

        return sb.toString();
    }

    private String convertirTipoExcepcion(String tipoCSharp) {
        if (tipoCSharp == null || tipoCSharp.isEmpty()) {
            return "Exception";
        }

        return switch (tipoCSharp) {
            case "Exception" -> "Exception";
            case "ArithmeticException", "DivideByZeroException" ->
                "ArithmeticError";
            case "FormatException", "InvalidCastException" ->
                "ValueError";
            case "IndexOutOfRangeException" ->
                "IndexError";
            case "NullReferenceException" ->
                "AttributeError";
            default -> "Exception";
        };
    }
    
    private String generarThrow(Nodo n, int nivel) {
        StringBuilder sb = new StringBuilder();

        sb.append(tab(nivel));

        if (n.hijos.isEmpty()) {
            // Re-lanzar la excepción actual dentro de un catch.
            sb.append("raise\n");
        } else {
            sb.append("raise ");
            sb.append(generarExpr(n.hijo(0)));
            sb.append("\n");
        }

        return sb.toString();
    }
    
    private String generarDoWhile(Nodo n, int nivel) {
        Nodo cuerpo = n.hijo(0);
        Nodo cond = n.hijo(1);

        StringBuilder sb = new StringBuilder();

        sb.append(tab(nivel)).append("while True:\n");

        actualizacionesContinue.push("");

        try {
            sb.append(
                generarBloqueConContexto(cuerpo, nivel + 1, "ciclo")
            );
        } finally {
            actualizacionesContinue.pop();
        }

        sb.append(tab(nivel + 1))
          .append("if not (")
          .append(generarExpr(cond))
          .append("):\n");

        sb.append(tab(nivel + 2)).append("break\n");

        return sb.toString();
    }
    
    private String generarSwitch(Nodo n, int nivel) {
        StringBuilder sb = new StringBuilder();
        String expr = generarExpr(n.hijo(0));
        boolean primero = true;

        int i = 1;

        while (i < n.hijos.size()) {
            Nodo caso = n.hijo(i);

            if (caso.tipo.equals("casoSwitch")) {
                List<Nodo> etiquetas = new ArrayList<>();
                etiquetas.add(caso);

                // Agrupar etiquetas consecutivas sin instrucciones.
                while (caso.hijos.size() == 1
                        && i + 1 < n.hijos.size()
                        && n.hijo(i + 1).tipo.equals("casoSwitch")) {

                    i++;
                    caso = n.hijo(i);
                    etiquetas.add(caso);
                }

                StringBuilder condicion = new StringBuilder();

                for (int j = 0; j < etiquetas.size(); j++) {
                    if (j > 0) {
                        condicion.append(" or ");
                    }

                    condicion.append(expr)
                             .append(" == ")
                             .append(generarExpr(etiquetas.get(j).hijo(0)));
                }

                String palabra = primero ? "if" : "elif";

                sb.append(tab(nivel))
                  .append(palabra)
                  .append(" ")
                  .append(condicion)
                  .append(":\n");

                sb.append(generarCuerpoCaso(caso, 1, nivel + 1));

                primero = false;
                i++;

            } else if (caso.tipo.equals("casoDefault")) {
                sb.append(tab(nivel)).append("else:\n");
                sb.append(generarCuerpoCaso(caso, 0, nivel + 1));
                i++;

            } else {
                i++;
            }
        }

        return sb.toString();
    }

    private String generarCuerpoCaso(
            Nodo caso, int desdeIndice, int nivel) {

        StringBuilder sb = new StringBuilder();

        contextosRuptura.push("switch");

        try {
            for (int j = desdeIndice; j < caso.hijos.size(); j++) {
                sb.append(generarInstruccion(caso.hijo(j), nivel));
            }
        } finally {
            contextosRuptura.pop();
        }

        if (sb.length() == 0) {
            sb.append(tab(nivel)).append("pass\n");
        }

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
        
        // Lectura de datos desde la consola
        if (n.tipo.equals("lecturaConsola")) {
            return "input()";
        }
        
        // Conversiones de tipos de C# a Python.
        if ("conversionTipo".equals(n.tipo)) {

            String tipo = n.atributo("tipo");
            String origen = n.atributo("origen");
            String argumento = generarExpr(n.hijo(0));

            // Conversiones mediante Parse.
            if ("Parse".equals(origen)) {

                switch (tipo) {

                    case "int":
                        return "_cs_convert_integer("
                                + argumento
                                + ", -2147483648, 2147483647)";

                    case "long":
                        return "_cs_parse_integer("
                                + argumento
                                + ", -9223372036854775808, 9223372036854775807)";

                    case "short":
                        return "_cs_parse_integer("
                                + argumento
                                + ", -32768, 32767)";

                    case "byte":
                        return "_cs_parse_integer("
                                + argumento
                                + ", 0, 255)";

                    case "float":
                    case "double":
                        return "_cs_parse_float("
                                + argumento + ")";

                    case "bool":
                        return "_cs_parse_bool("
                                + argumento + ")";

                    case "char":
                        return "_cs_parse_char("
                                + argumento + ")";
                }
            }

            // Conversiones mediante Convert.
            if ("Convert".equals(origen)) {

                switch (tipo) {

                    case "byte":
                        return "_cs_convert_integer("
                                + argumento + ", 0, 255)";

                    case "short":
                        return "_cs_convert_integer("
                                + argumento + ", -32768, 32767)";

                    case "int": {
                        String tipoArgumento = inferirTipoExpr(n.hijo(0));

                        if ("char".equals(tipoArgumento)) {
                            return "_cs_convert_char_to_integer("
                                    + argumento
                                    + ", -2147483648, 2147483647)";
                        }

                        return "_cs_convert_integer("
                                + argumento
                                + ", -2147483648, 2147483647)";
                    }

                    case "long":
                        return "_cs_convert_integer("
                                + argumento
                                + ", -9223372036854775808, 9223372036854775807)";

                    case "float":
                        return "_cs_convert_single("
                                + argumento + ")";

                    case "double":
                        return "_cs_convert_float("
                                + argumento + ")";

                    case "bool":
                        return "_cs_convert_bool("
                                + argumento + ")";

                    case "char":
                        return "_cs_convert_char("
                                + argumento + ")";

                    case "string":
                        return "_cs_convert_string("
                                + argumento + ")";
                }
            }

            return argumento;
        }
        
        if ("nuevaExcepcion".equals(n.tipo)) {
            String tipo = n.atributo("tipo");

            if (tipo == null || tipo.isEmpty()) {
                tipo = "Exception";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(tipo).append("(");

            for (int i = 0; i < n.hijos.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }

                sb.append(generarExpr(n.hijo(i)));
            }

            sb.append(")");
            return sb.toString();
        }
        
        // Llamadas a funciones
        if (n.tipo.equals("llamadaFuncion")) {
            StringBuilder llamada = new StringBuilder();
            llamada.append(n.valor).append("(");

            for (int i = 0; i < n.hijos.size(); i++) {
                if (i > 0) {
                    llamada.append(", ");
                }

                llamada.append(generarExpr(n.hijo(i)));
            }

            llamada.append(")");
            return llamada.toString();
        }
        
        // Array.IndexOf(arreglo, elemento)
        if ("busquedaIndiceArreglo".equals(n.tipo)) {
            String arreglo = n.valor;
            String elemento = generarExpr(n.hijo(0));

            return arreglo + ".index(" + elemento + ")"
                    + " if " + elemento + " in " + arreglo
                    + " else -1";
        }

        // Arreglo literal: {10, 20, 30} -> [10, 20, 30]
        if ("arreglo".equals(n.tipo)) {
            StringBuilder sb = new StringBuilder("[");

            for (int i = 0; i < n.hijos.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(generarExpr(n.hijo(i)));
            }

            sb.append("]");
            return sb.toString();
        }

        // Acceso por índice: numeros[0]
        if ("accesoArreglo".equals(n.tipo)) {
            return n.valor + "[" + generarExpr(n.hijo(0)) + "]";
        }

        // Propiedad Length de una variable o expresión.
        if ("propiedadArreglo".equals(n.tipo)) {
            if (!n.hijos.isEmpty()) {
                return "len(" + generarExpr(n.hijo(0)) + ")";
            }

            return "len(" + n.valor + ")";
        }
        
        // Propiedad Message de una excepción de C#.
        // error.Message -> error.args[0] si existe un mensaje.
        if ("propiedadExcepcion".equals(n.tipo)) {
            return "(" + n.valor
                    + ".args[0] if " + n.valor
                    + ".args else \"\")";
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
            Nodo nodoIzquierdo = n.hijo(0);
            Nodo nodoDerecho = n.hijo(1);

            String izquierda = generarExpr(nodoIzquierdo);
            String derecha = generarExpr(nodoDerecho);

            String operador = op;

            String tipoIzquierdo = inferirTipoExpr(nodoIzquierdo);
            String tipoDerecho = inferirTipoExpr(nodoDerecho);

            // Concatenación de cadenas.
            if ("+".equals(operador)) {
                if ("cadena".equals(tipoIzquierdo)
                        || "cadena".equals(tipoDerecho)) {

                    if (!"cadena".equals(tipoIzquierdo)) {
                        izquierda = "str(" + izquierda + ")";
                    }

                    if (!"cadena".equals(tipoDerecho)) {
                        derecha = "str(" + derecha + ")";
                    }
                }
            }

            // División entera: conserva el comportamiento de C#.
            if ("/".equals(operador)
                    && "int".equals(tipoIzquierdo)
                    && "int".equals(tipoDerecho)) {
                return "_cs_integer_division("
                        + izquierda + ", " + derecha + ")";
            }
            
            // Modulo entero: conserva el comportamiento de C#.
            if ("%".equals(operador)
                    && "int".equals(tipoIzquierdo)
                    && "int".equals(tipoDerecho)) {
                return "_cs_integer_modulo("
                        + izquierda + ", " + derecha + ")";
            }

            // Modulo decimal: conserva el signo del dividendo como en C#.
            if ("%".equals(operador)
                    && ("float".equals(tipoIzquierdo)
                        || "float".equals(tipoDerecho))) {
                return "_cs_float_modulo("
                        + izquierda + ", " + derecha + ")";
            }

            if ("&&".equals(operador)) operador = "and";
            else if ("||".equals(operador)) operador = "or";

            return izquierda + " " + operador + " " + derecha;
        }

        // Si el nodo solo contiene una expresión, la seguimos recorriendo
        if (n.hijos.size() == 1) {
            return generarExpr(n.hijo(0));
        }

        return "";
    }
    
    /** Determina el tipo de una expresión para generar operaciones compatibles con Python. */
    private String inferirTipoExpr(Nodo n) {
        if (n == null) {
            return null;
        }

        // Conversiones de tipos.
        if ("conversionTipo".equals(n.tipo)) {
            String tipo = n.atributo("tipo");

            if (tipo == null) {
                return null;
            }

            switch (tipo) {

                case "int":
                case "long":
                case "short":
                case "byte":
                    return "int";

                case "double":
                case "float":
                    return "float";

                case "bool":
                    return "bool";

                case "char":
                    return "char";

                case "string":
                    return "cadena";

                default:
                    return null;
            }
        }

        // Literales e identificadores.
        if ("valor".equals(n.tipo)) {
            String tipoValor = n.atributo("tipoValor");

            if ("cadena".equals(tipoValor)) {
                return "cadena";
            }

            if ("caracter".equals(tipoValor)) {
                return "char";
            }

            if ("true".equals(tipoValor) || "false".equals(tipoValor)) {
                return "bool";
            }

            if ("null".equals(tipoValor)) {
                return "null";
            }

            if ("decimal".equals(tipoValor)) {
                return "float";
            }

            if ("numero".equals(tipoValor)) {
                return "int";
            }

            // Si es un identificador, consultar el tipo declarado.
            String tipoVariable = Semantico.obtenerTipoVariable(n.valor);

            if (tipoVariable != null) {
                return normalizarTipo(tipoVariable);
            }

            // Compatibilidad con nodos numéricos sin tipoValor.
            if (n.valor != null) {
                if (n.valor.matches("\\d+")) {
                    return "int";
                }

                if (n.valor.matches("\\d+\\.\\d+")) {
                    return "float";
                }
            }

            return null;
        }

        if ("agrupacion".equals(n.tipo)) {
            return inferirTipoExpr(n.hijo(0));
        }
        
        // Una expresión unaria conserva el tipo de su operando.
        if (n.hijos.size() == 1
                && "unario-".equals(n.atributo("operador"))) {
            return inferirTipoExpr(n.hijo(0));
        }

        String operador = n.atributo("operador");

        if (n.hijos.size() == 2 && operador != null) {
            String tipoIzq = inferirTipoExpr(n.hijo(0));
            String tipoDer = inferirTipoExpr(n.hijo(1));

            // Concatenación de cadenas.
            if ("+".equals(operador)
                    && ("cadena".equals(tipoIzq)
                    || "cadena".equals(tipoDer))) {
                return "cadena";
            }

            // Operaciones relacionales y lógicas.
            if ("&&".equals(operador) || "||".equals(operador)
                    || "==".equals(operador) || "!=".equals(operador)
                    || "<".equals(operador) || ">".equals(operador)
                    || "<=".equals(operador) || ">=".equals(operador)) {
                return "bool";
            }

            // Operaciones aritméticas.
            if ("+".equals(operador) || "-".equals(operador)
                    || "*".equals(operador) || "/".equals(operador)
                    || "%".equals(operador)) {

                if ("float".equals(tipoIzq) || "float".equals(tipoDer)) {
                    return "float";
                }

                if ("int".equals(tipoIzq) && "int".equals(tipoDer)) {
                    return "int";
                }
            }
        }

        return null;
    }

    /** Unifica nombres de tipos para las comprobaciones internas. */
    private String normalizarTipo(String tipo) {
        if (tipo == null) {
            return null;
        }

        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "string", "cadena" -> "cadena";
            case "char" -> "char";
            case "int", "long", "short", "byte" -> "int";
            case "double", "float" -> "float";
            case "bool", "boolean" -> "bool";
            default -> tipo;
        };
    }
    
    private String generarValor(Nodo v) {
        String tipo = v.atributo("tipoValor");
        return switch (tipo == null ? "" : tipo) {
            case "true" -> "True";
            case "false" -> "False";
            case "null" -> "None";
            case "cadena", "caracter" -> normalizarCadena(v.valor);
            default -> v.valor;
        };
    }

    private String normalizarCadena(String lexema) {
        // Convierte comillas simples de C# a comillas dobles estilo Python
        if (lexema.startsWith("'") && lexema.endsWith("'")) {
            return "\"" + lexema.substring(1, lexema.length() - 1) + "\"";
        }
        return lexema;
    }
    
    private String generarOperacionArreglo(Nodo n, int nivel) {
        String nombreArreglo = n.valor;
        String operacion = n.atributo("operacion");

        if ("Sort".equals(operacion)) {
            return tab(nivel) + nombreArreglo + ".sort()\n";
        }

        if ("Reverse".equals(operacion)) {
            return tab(nivel) + nombreArreglo + ".reverse()\n";
        }

        return tab(nivel) + "# Operacion de arreglo no soportada\n";
    }
    
    private String generarCopiarArreglo(Nodo n, int nivel) {
        String origen = n.atributo("origen");
        String destino = n.atributo("destino");
        String cantidad = generarExpr(n.hijo(0));

        return tab(nivel) + destino + "[:"
                + cantidad + "] = "
                + origen + "[:" + cantidad + "]\n";
    }
}
