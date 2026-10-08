package traductor;
/**
 * Construye a mano un AST equivalente a este código C#:
 *
 *   int contador = 0;
 *   while (contador < 5) {
 *       print(contador);
 *       contador = contador + 1;
 *   }
 *   if (contador == 5) {
 *       print("listo");
 *   } else {
 *       print("error");
 *   }
 *
 * y corre los dos generadores, SIN necesitar el parser de JavaCC todavia.
 * Sirve para validar que GeneradorPython/GeneradorJavaScript funcionan
 * antes de conectar el parser real.
 */
public class PruebaGeneradores {
    public static void main(String[] args) {
        Nodo raiz = new Nodo("programa");

        // int contador = 0;
        Nodo decl = new Nodo("declaracionVariable", "contador");
        decl.atributo("tipoDato", "int");
        Nodo cero = new Nodo("valor", "0"); cero.atributo("tipoValor", "numero");
        Nodo expDecl = new Nodo("expresion"); expDecl.agregar(cero);
        decl.agregar(expDecl);
        raiz.agregar(decl);

        // while (contador < 5) { print(contador); contador = contador + 1; }
        Nodo cond = new Nodo("expresionLogica"); cond.atributo("operador", "<");
        Nodo vContador = new Nodo("valor", "contador"); vContador.atributo("tipoValor", "identificador");
        Nodo v5 = new Nodo("valor", "5"); v5.atributo("tipoValor", "numero");
        cond.agregar(vContador).agregar(v5);

        Nodo cuerpoWhile = new Nodo("bloque");
        Nodo printC = new Nodo("escritura");
        Nodo expPrint = new Nodo("expresion"); expPrint.agregar(vContador);
        printC.agregar(expPrint);
        cuerpoWhile.agregar(printC);

        Nodo asign = new Nodo("asignacion", "contador");
        Nodo expSuma = new Nodo("expresion"); expSuma.atributo("operador", "+");
        Nodo uno = new Nodo("valor", "1"); uno.atributo("tipoValor", "numero");
        expSuma.agregar(vContador).agregar(uno);
        asign.agregar(expSuma);
        cuerpoWhile.agregar(asign);

        Nodo cicloMientras = new Nodo("cicloMientras");
        cicloMientras.agregar(cond).agregar(cuerpoWhile);
        raiz.agregar(cicloMientras);

        // if (contador == 5) { print("listo"); } else { print("error"); }
        Nodo condIf = new Nodo("expresionLogica"); condIf.atributo("operador", "==");
        condIf.agregar(vContador).agregar(v5);

        Nodo bloqueIf = new Nodo("bloque");
        Nodo printListo = new Nodo("escritura");
        Nodo cadenaListo = new Nodo("valor", "\"listo\""); cadenaListo.atributo("tipoValor", "cadena");
        Nodo expListo = new Nodo("expresion"); expListo.agregar(cadenaListo);
        printListo.agregar(expListo);
        bloqueIf.agregar(printListo);

        Nodo bloqueElse = new Nodo("bloque");
        Nodo printError = new Nodo("escritura");
        Nodo cadenaError = new Nodo("valor", "\"error\""); cadenaError.atributo("tipoValor", "cadena");
        Nodo expError = new Nodo("expresion"); expError.agregar(cadenaError);
        printError.agregar(expError);
        bloqueElse.agregar(printError);

        Nodo condicion = new Nodo("condicion");
        condicion.atributo("tieneElse", "true");
        condicion.agregar(condIf).agregar(bloqueIf).agregar(bloqueElse);
        raiz.agregar(condicion);

        System.out.println("=== AST ===");
        System.out.println(raiz);

        System.out.println("=== PYTHON ===");
        System.out.println(new GeneradorPython().generar(raiz));

        System.out.println("=== JAVASCRIPT ===");
        System.out.println(new GeneradorJavaScript().generar(raiz));
    }
}
