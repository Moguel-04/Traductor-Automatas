//Eventos y arranque
const EJEMPLO = `int edad = 25;
string nombre = "España";
if (edad >= 18)
{
    Console.WriteLine("Mayor de edad");
}`;

document.addEventListener("DOMContentLoaded", () => {
  const entrada = $("codigo-cs");
  const botonTraducir = $("btn-traducir");
  let origen = { nombre: "Editor", tipo: "editor" };   // de dónde viene el código
  let ultimoReporte = null;
  // ----- tema y navegación -----
  cargarTema();
  $("btn-tema").addEventListener("click", () => {
    const actual = document.documentElement.dataset.tema;
    aplicarTema(actual === "oscuro" ? "claro" : "oscuro");
  });

  document.querySelectorAll("[data-ir]").forEach((b) => {
    b.addEventListener("click", () => { location.hash = b.dataset.ir; });
  });
  window.addEventListener("hashchange", () => mostrarVista(location.hash.slice(1)));


  // ----- editor -----
  iniciarTabs();
  sincronizarNumeros("codigo-cs", "numeros-cs");
  sincronizarNumeros("salida-py", "numeros-py");
  entrada.value = EJEMPLO;
  actualizarContador();
  entrada.addEventListener("input", actualizarContador);

  async function traducir() {
    botonTraducir.disabled = true;
    botonTraducir.textContent = "Traduciendo...";
    let resultado = null;
    const codigoEnviado = entrada.value;  
    try {
      resultado = await traducirCodigo(codigoEnviado);
    } catch (e) {
      mostrarFalloConexion(e);
    } finally {
      botonTraducir.disabled = false;
      botonTraducir.textContent = "Traducir";
    }
    if (resultado) {
      mostrarResultado(resultado);
      pintarSaludoResultado(resultado);
      guardarEntrada({
        fecha: Date.now(),
        nombre: origen.nombre,
        origen: origen.tipo,
        estado: resultado.ok ? "ok" : "error",
        tiempoMS: resultado.tiempoMS,
        errores: resultado.errores.length,
      });
      ultimoReporte = { fecha: Date.now(), nombre: origen.nombre, codigo: codigoEnviado, resultado };
      $("btn-reporte").disabled = false;
    }
  }

  botonTraducir.addEventListener("click", traducir);
  entrada.addEventListener("keydown", (ev) => {
    if (ev.ctrlKey && ev.key === "Enter") traducir();
  });

  $("btn-limpiar").addEventListener("click", () => {
    entrada.value = "";
    origen = { nombre: "Editor", tipo: "editor" };
    actualizarContador();
    limpiarResultado();
    ultimoReporte = null;
    $("btn-reporte").disabled = true;
    $("archivo-cs").value = "";   
  });

  $("btn-copiar").addEventListener("click", async () => {
    await navigator.clipboard.writeText($("salida-py").textContent);
  });

  $("btn-descargar").addEventListener("click", () => {
    const blob = new Blob([$("salida-py").textContent], { type: "text/plain;charset=utf-8" });
    const a = document.createElement("a");
    a.href = URL.createObjectURL(blob);
    a.download = "traduccion.py";
    a.click();
    URL.revokeObjectURL(a.href);
  });

  $("archivo-cs").addEventListener("change", async (ev) => {
    const archivo = ev.target.files[0];
    if (!archivo) return;

    if (!/\.(cs|txt)$/i.test(archivo.name)) {
      pintarErrores(["Solo se pueden cargar archivos .cs o .txt."]);
      return;
    }

    const bytes = await archivo.arrayBuffer();
    let texto;
    try {
      texto = new TextDecoder("utf-8", { fatal: true }).decode(bytes);  
    } catch (e) {
      texto = new TextDecoder("windows-1252").decode(bytes);    
    }

    entrada.value = texto;
    origen = { nombre: archivo.name, tipo: "archivo" };
    actualizarContador();
  });
  $("btn-reporte").addEventListener("click", () => {
    if (!ultimoReporte) return;
      llenarReporte(ultimoReporte);
      const tituloOriginal = document.title;
      const base = ultimoReporte.nombre.replace(/\.[^.]+$/, "");   // quita la extensión
      document.title = "Reporte_" + base + "_" + new Date().toLocaleDateString("sv-SE");
      window.addEventListener("afterprint", () => { document.title = tituloOriginal; }, { once: true });
      window.print();
  });
  // ----- historial -----
  $("btn-borrar-historial").addEventListener("click", () => {
    borrarHistorial();
    pintarHistorial();
  });

    mostrarVista(location.hash.slice(1) || "inicio");
});