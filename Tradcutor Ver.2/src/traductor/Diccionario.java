package traductor;

import java.io.*;
import java.util.*;

/**
 * RECONSTRUIDA a partir del uso que hace Analizador.jj:
 *   Diccionario.cargarCSV("palabras.csv")
 *   Diccionario.mapa.containsKey(palabra) / Diccionario.mapa.get(palabra)
 * Si ya tienes tu propia versión original, reemplaza este archivo por el tuyo.
 *
 * Formato esperado de palabras.csv (con encabezado):
 *   lexema,token,expresion,lenguaje,clasificacion,funcion
 * El campo "lenguaje" puede venir entre comillas con comas adentro
 * (ej. "C, JavaScript"), por eso el parseo respeta comillas.
 */
public class Diccionario {

    public static Map<String, PalabraInfo> mapa = new HashMap<>();

    public static void cargarCSV(String rutaArchivo) {
        mapa.clear();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaArchivo), "UTF-8"))) {

            String linea;
            boolean primera = true;
            while ((linea = br.readLine()) != null) {
                if (primera) { primera = false; continue; } // saltar encabezado
                if (linea.trim().isEmpty()) continue;

                List<String> campos = parsearLineaCSV(linea);
                if (campos.size() < 2) continue;

                String lexema        = get(campos, 0);
                String token         = get(campos, 1);
                String expresion     = get(campos, 2);
                String lenguaje      = get(campos, 3);
                String clasificacion = get(campos, 4);
                String funcion       = get(campos, 5);

                if (lexema.isEmpty()) continue;

                mapa.put(lexema.toLowerCase(),
                        new PalabraInfo(lexema, token, expresion, lenguaje, clasificacion, funcion));
            }
            System.out.println("Diccionario cargado: " + mapa.size() + " palabras (" + rutaArchivo + ")");
        } catch (IOException e) {
            System.out.println("AVISO: no se pudo cargar '" + rutaArchivo + "': " + e.getMessage());
            System.out.println("El analizador seguirá funcionando, pero sin datos extra del diccionario.");
        }
    }

    private static String get(List<String> campos, int i) {
        return i < campos.size() ? campos.get(i).trim() : "";
    }

    /** Parser CSV simple que respeta comillas dobles (para campos con comas adentro). */
    private static List<String> parsearLineaCSV(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean dentroComillas = false;

        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                dentroComillas = !dentroComillas;
            } else if (c == ',' && !dentroComillas) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos;
    }
}
