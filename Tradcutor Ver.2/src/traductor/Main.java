package traductor;

import java.io.*;

public class Main {

    public static void main(String[] args) throws Exception {

        Diccionario.cargarCSV("palabras.csv");

        System.out.println("========================================");
        System.out.println("TRADUCTOR DE C# A PYTHON / JAVASCRIPT");
        System.out.println("========================================\n");

        BufferedReader teclado =
                new BufferedReader(
                        new InputStreamReader(System.in, "UTF-8"));

        while (true) {

            System.out.print("Archivo .cs a traducir (o 'salir'): ");
            String archivo = teclado.readLine();

            if (archivo == null ||
                archivo.trim().isEmpty() ||
                archivo.equalsIgnoreCase("salir")) {

                System.out.println("¡Hasta luego!");
                break;
            }

            System.out.print("Lenguaje destino (python/javascript): ");
            String destino = teclado.readLine();

            if (destino == null)
                destino = "python";

            destino = destino.trim().toLowerCase();

            try {

                StringBuilder contenido = new StringBuilder();

                BufferedReader fr =
                        new BufferedReader(new FileReader(archivo));

                String linea;

                while ((linea = fr.readLine()) != null) {
                    contenido.append(linea).append("\n");
                }

                fr.close();

                String textoCompleto = contenido.toString();

                Semantico.limpiar();

                System.out.println(
                        "\n========== ANALISIS LEXICO-SINTACTICO ==========");

                Traductor parser =
                        new Traductor(
                                new StringReader(textoCompleto));

                Nodo raiz = parser.programa();

                System.out.println(
                        "\n========== RESULTADO DEL ANALISIS ==========");

                if (Semantico.hayErrores()) {

                    System.out.println(
                            "✗ Se encontraron errores semanticos:");

                    Semantico.imprimirErrores();

                    System.out.println(
                            "\nNo se generara traduccion hasta corregir los errores.");

                    continue;
                }

                System.out.println("✓ Sintaxis valida.");

                String codigoGenerado;
                String extension;

                if (destino.startsWith("j")) {

                    GeneradorJavaScript generador =
                            new GeneradorJavaScript();

                    codigoGenerado = generador.generar(raiz);
                    extension = ".js";

                } else {

                    GeneradorPython generador =
                            new GeneradorPython();

                    codigoGenerado = generador.generar(raiz);
                    extension = ".py";
                }

                System.out.println(
                        "\n========== CODIGO TRADUCIDO ==========");

                System.out.println(codigoGenerado);

                String archivoSalida =
                        archivo.replaceAll(
                                "\\.[^.]*$", "") + extension;

                PrintWriter pw =
                        new PrintWriter(
                                new FileWriter(archivoSalida));

                pw.print(codigoGenerado);
                pw.close();

                System.out.println(
                        "\n(Guardado en " +
                        archivoSalida + ")");

            } catch (FileNotFoundException e) {

                System.out.println(
                        "\nERROR: No se encontro el archivo '" +
                        archivo + "'");

            } catch (ParseException e) {

                System.out.println(
                        "\n========== ERROR SINTACTICO ==========");

                System.out.println("✗ " + e.getMessage());

            } catch (Exception e) {

                System.out.println(
                        "\nERROR: " + e.getMessage());

                e.printStackTrace();
            }

            System.out.println(
                    "\n----------------------------------------\n");
        }
    }
}