/**
 * RECONSTRUIDA a partir del uso que hace Analizador.jj (mostrarToken()):
 *   info.token, info.expresion, info.lenguaje, info.clasificacion, info.funcion
 * No tenía el archivo original — si ya tienes tu propia versión, reemplaza
 * esta clase por la tuya (deben coincidir los nombres de campo, o ajusta
 * Diccionario.java y mostrarToken() en consecuencia).
 */
public class PalabraInfo {
    public String lexema;
    public String token;
    public String expresion;
    public String lenguaje;
    public String clasificacion;
    public String funcion;

    public PalabraInfo(String lexema, String token, String expresion,
                        String lenguaje, String clasificacion, String funcion) {
        this.lexema = lexema;
        this.token = token;
        this.expresion = expresion;
        this.lenguaje = lenguaje;
        this.clasificacion = clasificacion;
        this.funcion = funcion;
    }
}
