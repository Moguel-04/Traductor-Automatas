package traductor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServicioTraduccion{
    //Paquete de Respuesta
    public static class Resultado{
        public boolean ok;
        public String python = "";
        public String log = "";
        public List<String> errores = new ArrayList<>();
        public long tiempoMS;
    }
    //Una llamada al arranque del servidor
    public static void iniciar() throws Exception{
        Diccionario.cargarCSV("palabras.csv");
    }
    //Metodo principal
    public static synchronized Resultado traducir(String codigo){
        Resultado r = new Resultado();
        long inicio = System.nanoTime();
        
        if (codigo == null || codigo.isBlank()){
            r.errores.add("No hay codigo para traducir.");
            return r;
        }

        Nodo raiz = null;
        Semantico.limpiar();

        try{
            PrintStream original = System.out;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            
            try{
                System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
                Traductor parser = new Traductor(new StringReader(codigo));
                raiz = parser.programa();
            }
            finally{
                System.setOut(original); 
                r.log = buffer.toString(StandardCharsets.UTF_8);
            }
            //Analisis Semantico
            Semantico.validarPrograma(raiz);
            if (Semantico.hayErrores()){
                String texto = capturarErroresSemanticos();
                for (String linea : texto.split("\\R")){
                    String limpia = linea.replaceFirst("^[^\\p{L}]+","");
                    if (!limpia.isEmpty()){
                        r.errores.add("Semántico: "+limpia);
                    }
                }
            }
            else{
                r.python = new GeneradorPython().generar(raiz); 
                r.ok = true;
            }
        }
        catch(ParseException e){
            int linea = -1;
            if (e.currentToken != null && e.currentToken.next != null){
                linea = e.currentToken.next.beginLine;
            }
            r.errores.add("Sintáctico (linea "+ linea +"): "+ e.getMessage());
        }
        catch(TokenMgrError e){
            r.errores.add("Léxico: " + e.getMessage());
        }
        catch(Exception e){
            r.errores.add("Error interno: " + e);
        }
        
        r.tiempoMS = (System.nanoTime() - inicio)  / 1_000_000;
        return r;
    }
    private static String capturarErroresSemanticos(){
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try{
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            Semantico.imprimirErrores();
        }
        finally{
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
    public static void main(String[] args) throws Exception{
        iniciar();

        java.nio.file.Path ruta = java.nio.file.Path.of("semantico.cs");
        String codigo = java.nio.file.Files.readString(ruta);

        System.out.println("Leyendo: " + ruta.toAbsolutePath());
        System.out.println("Contenido:\n" + codigo);

        Resultado r = traducir(codigo);
        System.out.println("ok: " + r.ok);
        System.out.println("tiempo: " + r.tiempoMS + " ms");
        System.out.println("errores: " + r.errores);
        System.out.println("---- python ----");
        System.out.println(r.python);
}
}