package traductor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServidorWeb {
    public static void main(String[] args) throws Exception {
        ServicioTraduccion.iniciar();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/ping", exchange -> {
            byte[] datos = "Servidor funcionando".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, datos.length);
            exchange.getResponseBody().write(datos);
            exchange.close();
        });

        server.createContext("/api/traducir", exchange -> {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                responder(exchange, 405, "text/plain; charset=utf-8", "Usa POST");
                return;
            }
            String codigo = new String(exchange.getRequestBody().readAllBytes(),
                                      StandardCharsets.UTF_8);
            ServicioTraduccion.Resultado r = ServicioTraduccion.traducir(codigo);
            responder(exchange, 200, "application/json; charset=utf-8", aJson(r));
        } catch (Exception e) {
        responder(exchange, 500, "text/plain; charset=utf-8", "Error interno: " + e);
        }
    });

        server.start();
        server.createContext("/", exchange -> {
            try {
                String ruta = exchange.getRequestURI().getPath();
                if (ruta.equals("/")) ruta = "/Index.html";

                Path base = Path.of("web").toAbsolutePath().normalize();
                Path archivo = base.resolve(ruta.substring(1)).normalize();

                
                if (!archivo.startsWith(base) || !Files.isRegularFile(archivo)) {
                    responder(exchange, 404, "text/plain; charset=utf-8", "No encontrado");
                    return;
                }

                byte[] datos = Files.readAllBytes(archivo);
                exchange.getResponseHeaders().set("Content-Type", tipoDe(archivo.toString()));
                exchange.sendResponseHeaders(200, datos.length);
                exchange.getResponseBody().write(datos);
                exchange.close();
            } 
            catch (Exception e) {
                responder(exchange, 500, "text/plain; charset=utf-8", "Error interno: " + e);
            }
        });
        System.out.println("Servidor en http://localhost:8080");
    }
    private static void responder(HttpExchange ex, int codigo, String tipo, String cuerpo)
        throws IOException {
        byte[] datos = cuerpo.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.sendResponseHeaders(codigo, datos.length);
        ex.getResponseBody().write(datos);
        ex.close();
    }

    private static String aJson(ServicioTraduccion.Resultado r) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":").append(r.ok);
        sb.append(",\"python\":\"").append(escapar(r.python)).append("\"");
        sb.append(",\"log\":\"").append(escapar(r.log)).append("\"");
        sb.append(",\"errores\":[");
        for (int i = 0; i < r.errores.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapar(r.errores.get(i))).append("\"");
        }
        sb.append("],\"tiempoMS\":").append(r.tiempoMS).append("}");
        return sb.toString();
    }

    private static String escapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
    private static String tipoDe(String nombre){
        String n = nombre.toLowerCase();
        if (n.endsWith(".html")) return "text/html; charset=utf-8";
        if (n.endsWith(".css"))  return "text/css; charset=utf-8";
        if (n.endsWith(".js"))   return "text/javascript; charset=utf-8";
        if (n.endsWith(".json")) return "application/json; charset=utf-8";
        if (n.endsWith(".svg"))  return "image/svg+xml";
        if (n.endsWith(".png"))  return "image/png";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }
}