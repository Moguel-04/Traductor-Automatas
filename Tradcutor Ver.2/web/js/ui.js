//Todo lo que dibuja en pantalla
const $ = (id) => document.getElementById(id);

function actualizarContador() {
  const texto = $("codigo-cs").value;
  const lineas = texto === "" ? 0 : texto.split("\n").length;
  $("contador-cs").textContent = "Líneas: " + lineas + " | " + texto.length + " caracteres";
  actualizarNumeros("numeros-cs", texto, true);
}

// Convierte el log de Java en { tokens: [...], reglas: [...] }
function parsearLog(log) {
  const tokens = [];
  const reglas = [];
  let actual = null;
  const sinAcentos = (s) => s.normalize("NFD").replace(/[\u0300-\u036f]/g, "");

  for (const linea of log.split(/\r?\n/)) {
    const l = linea.trim();
    if (l === "") continue;
    if (/^-{3,}$/.test(l)) { actual = null; continue; }
    if (l.startsWith("Regla:")) { reglas.push(l.slice(6).trim()); continue; }

    const i = l.indexOf(":");
    if (i === -1) continue;
    const clave = sinAcentos(l.slice(0, i)).toLowerCase().trim();
    const valor = l.slice(i + 1).trim();

    if (clave === "token") {
      actual = { token: valor, lexema: "", clasificacion: "", funcion: "" };
      tokens.push(actual);
    } else if (actual) {
      if (clave === "lexema") actual.lexema = valor;
      else if (clave === "clasificacion") actual.clasificacion = valor;
      else if (clave === "funcion") actual.funcion = valor;
    }
  }
  return { tokens, reglas };
}

function estadoEtapas(errores) {
  const tiene = (p) => errores.some((e) => e.startsWith(p));
  if (tiene("Léxico")) return { lexico: "error", sintactico: "pendiente", semantico: "pendiente" };
  if (tiene("Sintáctico")) return { lexico: "ok", sintactico: "error", semantico: "pendiente" };
  if (tiene("Semántico")) return { lexico: "ok", sintactico: "ok", semantico: "error" };
  if (errores.length > 0) return { lexico: "pendiente", sintactico: "pendiente", semantico: "pendiente" };
  return { lexico: "ok", sintactico: "ok", semantico: "ok" };
}

function pintarChecklist(estados) {
  for (const etapa of ["lexico", "sintactico", "semantico"]) {
    const el = $("estado-" + etapa);
    el.dataset.estado = estados[etapa];            // ok | error | pendiente (para tu CSS)
    el.textContent = { ok: "✓", error: "✗", pendiente: "—" }[estados[etapa]];
  }
}

function pintarErrores(errores) {
  const lista = $("lista-errores");
  lista.innerHTML = "";
  for (const e of errores) {
    const [titulo, ...resto] = e.split("\n");
    const li = document.createElement("li");
    li.dataset.tipo = titulo.split(/[ (:]/)[0].toLowerCase();   // léxico, sintáctico...
    const p = document.createElement("strong");
    p.textContent = titulo;
    li.appendChild(p);
    if (resto.length > 0) {
      const d = document.createElement("details");
      const s = document.createElement("summary");
      s.textContent = "Ver detalles";
      const pre = document.createElement("pre");
      pre.textContent = resto.join("\n");
      d.append(s, pre);
      li.appendChild(d);
    }
    lista.appendChild(li);
  }
}

function pintarTokens(tokens) {
  const cuerpo = $("tabla-tokens");
  cuerpo.innerHTML = "";
  for (const t of tokens) {
    const fila = document.createElement("tr");
    for (const dato of [t.token, t.lexema, t.clasificacion, t.funcion]) {
      const td = document.createElement("td");
      td.textContent = dato;
      fila.appendChild(td);
    }
    cuerpo.appendChild(fila);
  }
  $("total-tokens").textContent = tokens.length;
}

function pintarReglas(reglas) {
  const lista = $("lista-reglas");
  lista.innerHTML = "";
  for (const r of reglas) {
    const li = document.createElement("li");
    li.textContent = r;
    lista.appendChild(li);
  }
}

function mostrarResultado(r) {
  $("salida-py").textContent = r.python;
  actualizarNumeros("numeros-py", r.python, false, true);
  $("tiempo").textContent = r.tiempoMS + " ms";
  pintarErrores(r.errores);
  pintarChecklist(estadoEtapas(r.errores));
  const analisis = parsearLog(r.log);
  pintarTokens(analisis.tokens);
  pintarReglas(analisis.reglas);
  pintarAnalisis(r, analisis, estadoEtapas(r.errores));
}

function mostrarFalloConexion(e) {
  pintarErrores(["No se pudo conectar con el servidor. ¿Está encendido ServidorWeb?\n" + e]);
}
// ---------- Vistas, estadísticas, historial y tema ----------
const VISTAS = ["inicio", "traducir", "analisis", "historial", "creditos"];

function mostrarVista(nombre) {
  if (!VISTAS.includes(nombre)) nombre = "inicio";
  document.querySelectorAll(".vista").forEach((v) => {
    v.hidden = v.dataset.vista !== nombre;
  });
  document.querySelectorAll("#menu [data-ir]").forEach((b) => {
    b.dataset.activo = String(b.dataset.ir === nombre);   // para tu CSS: [data-activo="true"]
  });
  pintarSaludo(nombre);
  if (nombre === "inicio") pintarEstadisticas();
  if (nombre === "historial") pintarHistorial();
}

function pintarEstadisticas() {
  const h = cargarHistorial();
  $("stat-total").textContent = h.filter((e) => e.estado === "ok").length;
  $("stat-archivos").textContent = h.filter((e) => e.origen === "archivo").length;
  $("stat-historial").textContent = h.length;
  $("stat-errores").textContent = h.filter((e) => e.estado === "error").length;
  pintarInicio();
}

function pintarHistorial() {
  const h = cargarHistorial();
  const cuerpo = $("tabla-historial");
  cuerpo.innerHTML = "";
  $("historial-vacio").hidden = h.length > 0;
  for (const e of h) {
    const fila = document.createElement("tr");
    const datos = [
      e.nombre,
      new Date(e.fecha).toLocaleString("es-MX"),
      e.estado === "ok" ? "Completado" : "Con errores",
      e.tiempoMS + " ms",
    ];
    datos.forEach((d, i) => {
      const td = document.createElement("td");
      td.textContent = d;
      if (i === 2) td.dataset.estado = e.estado;           // ok | error (para tu CSS)
      fila.appendChild(td);
    });
    cuerpo.appendChild(fila);
  }
}

function aplicarTema(tema) {
  document.documentElement.dataset.tema = tema;            // CSS: [data-tema="claro"]
  try { localStorage.setItem("traductor.tema", tema); } catch (e) {}
}

function cargarTema() {
  let tema = "oscuro";
  try { tema = localStorage.getItem("traductor.tema") || "oscuro"; } catch (e) {}
  aplicarTema(tema);
}
// ---------- Saludo / mensaje de cabecera por sección ----------
function saludoSegunHora() {
  const h = new Date().getHours();
  if (h < 12) return "¡Buenos días!";
  if (h < 19) return "¡Buenas tardes!";
  return "¡Buenas noches!";
}

const MENSAJES = {
  inicio: () => {
    const total = cargarHistorial().length;
    return {
      titulo: saludoSegunHora(),
      texto: total === 0
        ? "Aún no has traducido nada. Empieza cuando quieras."
        : "Llevas " + total + (total === 1 ? " traducción." : " traducciones."),
    };
  },
  traducir: () => ({
    titulo: "Traducir código",
    texto: "Escribe o carga tu código C# y conviértelo a Python.",
  }),
  analisis: () => ({
    titulo: "Análisis del código",
    texto: "La última traducción generó " + $("total-tokens").textContent + " tokens.",
  }),
  creditos: () => ({
    titulo: "Créditos",
    texto: "Las personas detrás del proyecto.",
  }),
  historial: () => {
    const total = cargarHistorial().length;
    return {
      titulo: "Historial",
      texto: total === 0
        ? "Todavía no hay nada guardado."
        : total === 1 ? "Tu última traducción." : "Tus últimas " + total + " traducciones.",
    };
  },
};

function pintarSaludo(nombre) {
  const m = (MENSAJES[nombre] || MENSAJES.inicio)();
  $("saludo-titulo").textContent = m.titulo;
  $("saludo-texto").textContent = m.texto;
}

function pintarSaludoResultado(r) {
  $("saludo-titulo").textContent = r.ok ? "¡Traducción lista!" : "Hay algo que corregir";
  $("saludo-texto").textContent = r.ok
    ? "Se tradujo en " + r.tiempoMS + " ms."
    : (r.errores.length === 1 ? "Se encontró 1 error." : "Se encontraron " + r.errores.length + " errores.") + " Revisa la sección Errores.";
}
function agregarFila(tbody, valores) {
  const tr = document.createElement("tr");
  valores.forEach((v) => {
    const td = document.createElement("td");
    td.textContent = v;
    tr.appendChild(td);
  });
  tbody.appendChild(tr);
}

function llenarLista(lista, textos) {
  lista.innerHTML = "";
  textos.forEach((t) => {
    const li = document.createElement("li");
    li.textContent = t;
    lista.appendChild(li);
  });
}

function llenarReporte(d) {
  const r = d.resultado;
  const analisis = parsearLog(r.log);
  const estados = estadoEtapas(r.errores);
  const fecha = new Date(d.fecha).toLocaleString("es-MX");

  $("rep-subtitulo").textContent = d.nombre + " — " + fecha;

  const resumen = $("rep-resumen");
  resumen.innerHTML = "";
  agregarFila(resumen, ["Archivo de origen", d.nombre]);
  agregarFila(resumen, ["Fecha", fecha]);
  agregarFila(resumen, ["Dirección", "C# → Python"]);
  agregarFila(resumen, ["Estado", r.ok ? "Completado" : "Con errores"]);
  agregarFila(resumen, ["Líneas/caracteres", d.codigo.split("\n").length + " / " + d.codigo.length]);
  agregarFila(resumen, ["Tiempo", r.tiempoMS + " ms"]);
  agregarFila(resumen, ["Tokens", analisis.tokens.length]);
  const texto = { ok: "Correcto", error: "Con error", pendiente: "No evaluado" };
    llenarLista($("rep-etapas"), [
    "Léxico: " + texto[estados.lexico],
    "Sintáctico: " + texto[estados.sintactico],
    "Semántico: " + texto[estados.semantico],
  ]);
  llenarLista($("rep-errores"),
    r.errores.length ? r.errores.map((e) => e.split("\n")[0]) : ["Sin errores."]);

  // Resumen de tokens por clasificación
  const conteo = {};
  for (const t of analisis.tokens) {
    const k = t.clasificacion || t.token;
    conteo[k] = (conteo[k] || 0) + 1;
  }
  const resumenTokens = $("rep-tokens-resumen");
  resumenTokens.innerHTML = "";
  Object.entries(conteo)
    .sort((a, b) => b[1] - a[1])
    .forEach(([nombre, cantidad]) => agregarFila(resumenTokens, [nombre, cantidad]));

  // Primeros 40 tokens
  const primeros = $("rep-tokens");
  primeros.innerHTML = "";
  analisis.tokens.slice(0, 40).forEach((t) =>
    agregarFila(primeros, [t.token, t.lexema, t.clasificacion, t.funcion]));

  // Primeras 40 reglas
  llenarLista($("rep-reglas"), analisis.reglas.slice(0, 40));

  // Códigos
  $("rep-codigo-fuente").textContent = d.codigo;
  $("rep-codigo-salida").textContent = r.python;
}
function limpiarResultado() {
  $("salida-py").textContent = "";
  actualizarNumeros("numeros-py", "", false, true);
  $("tiempo").textContent = "—";
  pintarErrores([]);
  pintarChecklist({ lexico: "pendiente", sintactico: "pendiente", semantico: "pendiente" });
  pintarSaludo("traducir");
}