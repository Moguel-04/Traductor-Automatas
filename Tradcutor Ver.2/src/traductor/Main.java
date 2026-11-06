package traductor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        Diccionario.cargarCSV("palabras.csv");

        BufferedReader teclado = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8)
        );

        System.out.println("========================================");
        System.out.println("TRADUCTOR DE C# A PYTHON / JAVASCRIPT");
        System.out.println("========================================");

        while (true) {

            try {
                System.out.println("\nSelecciona el metodo de entrada:");
                System.out.println("1. Cargar codigo desde un archivo");
                System.out.println("2. Introducir codigo manualmente");
                System.out.println("3. Salir");
                System.out.print("\nOpcion: ");

                String opcion = teclado.readLine();

                if (opcion == null || opcion.trim().equals("3")) {
                    System.out.println("\n¡Hasta luego!");
                    break;
                }

                String textoCompleto;
                Path archivoSalidaBase = null;

                switch (opcion.trim()) {

                    case "1":

                        System.out.print("\nRuta del archivo .cs o .txt: ");
                        String ruta = teclado.readLine();

                        if (ruta == null || ruta.trim().isEmpty()) {
                            System.out.println("No se proporciono una ruta.");
                            continue;
                        }

                        Path archivoEntrada = Paths.get(ruta.trim());

                        String nombreArchivo = archivoEntrada
                                .getFileName()
                                .toString()
                                .toLowerCase();

                        if (!nombreArchivo.endsWith(".cs")
                                && !nombreArchivo.endsWith(".txt")) {

                            System.out.println(
                                    "ERROR: Solo se admiten archivos .cs y .txt."
                            );
                            continue;
                        }

                        if (!Files.exists(archivoEntrada)
                                || !Files.isRegularFile(archivoEntrada)) {

                            System.out.println(
                                    "ERROR: No se encontro el archivo indicado."
                            );
                            continue;
                        }

                        StringBuilder contenido = new StringBuilder();

                        try (BufferedReader lector = Files.newBufferedReader(
                                archivoEntrada,
                                StandardCharsets.UTF_8)) {

                            String linea;

                            while ((linea = lector.readLine()) != null) {
                                contenido.append(linea).append("\n");
                            }
                        }

                        textoCompleto = contenido.toString();
                        archivoSalidaBase = archivoEntrada;

                        System.out.println(
                                "\nArchivo cargado correctamente."
                        );

                        break;

                    case "2":

                        System.out.println(
                                "\nIntroduce el codigo C#."
                        );

                        System.out.println(
                                "Escribe :fin en una linea independiente para terminar.\n"
                        );

                        StringBuilder codigoManual = new StringBuilder();
                        String linea;

                        while ((linea = teclado.readLine()) != null) {

                            if (linea.trim().equals(":fin")) {
                                break;
                            }

                            codigoManual.append(linea).append("\n");
                        }

                        textoCompleto = codigoManual.toString();

                        if (textoCompleto.trim().isEmpty()) {
                            System.out.println(
                                    "ERROR: No se introdujo ningun codigo."
                            );
                            continue;
                        }

                        System.out.println(
                                "\nCodigo recibido correctamente."
                        );

                        break;

                    default:

                        System.out.println(
                                "Opcion no valida. Intenta nuevamente."
                        );

                        continue;
                }

                // Seleccion del lenguaje destino.
                System.out.print(
                        "\nLenguaje destino (python/javascript): "
                );

                String destino = teclado.readLine();

                if (destino == null || destino.trim().isEmpty()) {
                    destino = "python";
                }

                destino = destino.trim().toLowerCase();

                if (!destino.equals("python")
                        && !destino.equals("javascript")) {

                    System.out.println(
                            "ERROR: Lenguaje destino no valido."
                    );

                    continue;
                }

                // Procesamiento comun para ambas modalidades.
                procesarCodigo(
                        textoCompleto,
                        archivoSalidaBase,
                        destino
                );

            } catch (IOException e) {

                System.out.println(
                        "\nERROR al leer la entrada: " + e.getMessage()
                );

            } catch (Exception e) {

                System.out.println(
                        "\nERROR inesperado: " + e.getMessage()
                );

                e.printStackTrace();
            }

            System.out.println(
                    "\n----------------------------------------"
            );
        }
    }

    // ==================================================
    // PROCESAMIENTO DEL CODIGO FUENTE
    // ==================================================

    private static void procesarCodigo(
            String textoCompleto,
            Path archivoSalidaBase,
            String destino) {

        try {

            Semantico.limpiar();

            System.out.println(
                    "\n========== ANALISIS LEXICO-SINTACTICO =========="
            );

            // Crear el analizador con el codigo recibido.
            Traductor parser = new Traductor(
                    new StringReader(textoCompleto)
            );

            // Construir el arbol sintactico.
            Nodo raiz = parser.programa();

            // Ejecutar las validaciones semanticas.
            Semantico.validarPrograma(raiz);

            System.out.println(
                    "\n========== RESULTADO DEL ANALISIS =========="
            );

            if (Semantico.hayErrores()) {

                System.out.println(
                        "✗ Se encontraron errores semanticos:"
                );

                Semantico.imprimirErrores();

                System.out.println(
                        "\nNo se generara traduccion hasta corregir los errores."
                );

                return;
            }

            System.out.println("✓ Sintaxis valida.");

            // Generar codigo segun el lenguaje seleccionado.
            String codigoGenerado;
            String extension;

            if (destino.equals("javascript")) {

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
                    "\n========== CODIGO TRADUCIDO =========="
            );

            System.out.println(codigoGenerado);

            // Determinar el nombre y la ubicacion del archivo de salida.
            Path archivoSalida;

            if (archivoSalidaBase != null) {

                String nombre = archivoSalidaBase
                        .getFileName()
                        .toString();

                int punto = nombre.lastIndexOf('.');

                if (punto > 0) {
                    nombre = nombre.substring(0, punto);
                }

                archivoSalida = archivoSalidaBase.resolveSibling(
                        nombre + extension
                );

            } else {

                archivoSalida = Paths.get(
                        "codigo_traducido" + extension
                );
            }

            // Guardar el resultado utilizando UTF-8.
            try (BufferedWriter escritor = Files.newBufferedWriter(
                    archivoSalida,
                    StandardCharsets.UTF_8)) {

                escritor.write(codigoGenerado);
            }

            System.out.println(
                    "\nTraduccion guardada correctamente en:"
            );

            System.out.println(
                    archivoSalida.toAbsolutePath()
            );

        } catch (ParseException e) {

            System.out.println(
                    "\n========== ERROR SINTACTICO =========="
            );

            System.out.println("✗ " + e.getMessage());

        } catch (IOException e) {

            System.out.println(
                    "\nERROR al guardar el archivo: " + e.getMessage()
            );

        } catch (Exception e) {

            System.out.println(
                    "\nERROR durante el procesamiento: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
