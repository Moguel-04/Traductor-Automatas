/* ---------- 1. NÚMEROS DE LÍNEA ---------- */

function actualizarNumeros(idNumeros, texto, mostrarSiVacio, ignorarSaltoFinal) {
  const el = $(idNumeros);
  if (!el) return;
  if (ignorarSaltoFinal && texto.endsWith("\n")) texto = texto.slice(0, -1);
  if (texto === "" && !mostrarSiVacio) { el.textContent = ""; return; }
  const total = texto === "" ? 1 : texto.split("\n").length;
  const lineas = new Array(total);
  for (let i = 0; i < total; i++) lineas[i] = i + 1;
  el.textContent = lineas.join("\n");
}

function sincronizarNumeros(idOrigen, idNumeros) {
  const origen = $(idOrigen);
  const numeros = $(idNumeros);
  origen.addEventListener("scroll", () => { numeros.scrollTop = origen.scrollTop; });
}

/* ---------- 2. ANÁLISIS CON PESTAÑAS ---------- */

function seleccionarTab(nombre) {
  document.querySelectorAll("#tabs-analisis [data-tab]").forEach((b) => {
    b.dataset.activo = String(b.dataset.tab === nombre);
  });
  document.querySelectorAll(".panel-analisis").forEach((p) => {
    p.hidden = p.dataset.panel !== nombre;
  });
}

function iniciarTabs() {
  document.querySelectorAll("#tabs-analisis [data-tab]").forEach((b) => {
    b.addEventListener("click", () => seleccionarTab(b.dataset.tab));
  });
}

function contarPor(items, claveFn) {
  const conteo = {};
  for (const it of items) {
    const k = claveFn(it);
    conteo[k] = (conteo[k] || 0) + 1;
  }
  return Object.entries(conteo).sort((a, b) => b[1] - a[1]);
}

function pintarBarras(idContenedor, pares) {
  const cont = $(idContenedor);
  cont.innerHTML = "";
  if (pares.length === 0) {
    cont.textContent = "Sin datos todavía.";
    return;
  }
  const max = Math.max(...pares.map((p) => p[1]));
  for (const [nombre, n] of pares) {
    const fila = document.createElement("div");
    fila.className = "barra";
    const etiqueta = document.createElement("span");
    etiqueta.className = "barra-nombre";
    etiqueta.textContent = nombre;
    const pista = document.createElement("div");
    pista.className = "barra-pista";
    const relleno = document.createElement("div");
    relleno.className = "barra-relleno";
    relleno.style.width = (n / max) * 100 + "%";
    pista.appendChild(relleno);
    const cantidad = document.createElement("span");
    cantidad.className = "barra-num";
    cantidad.textContent = n;
    fila.append(etiqueta, pista, cantidad);
    cont.appendChild(fila);
  }
}

// Lista de errores de una etapa 
function pintarListaErrores(idLista, errores) {
  const lista = $(idLista);
  lista.innerHTML = "";
  for (const e of errores) {
    const [titulo, ...resto] = e.split("\n");
    const li = document.createElement("li");
    const t = document.createElement("strong");
    t.textContent = titulo;
    li.appendChild(t);
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

function pintarAnalisis(r, analisis, estados) {
  const textoEstado = { ok: "Sin errores", error: "Con errores", pendiente: "No evaluado" };
  const de = (prefijo) => r.errores.filter((e) => e.startsWith(prefijo));
  const errLex = de("Léxico");
  const errSin = de("Sintáctico");
  const errSem = de("Semántico");

  for (const etapa of ["lexico", "sintactico", "semantico"]) {
    const el = $("pildora-" + etapa);
    el.dataset.estado = estados[etapa];
    el.textContent = textoEstado[estados[etapa]];
  }

  // Léxico
  $("num-err-lexico").textContent = errLex.length;
  $("tiempo-analisis").textContent = r.tiempoMS + " ms";
  pintarBarras("barras-tokens", contarPor(analisis.tokens, (t) => t.clasificacion || t.token).slice(0, 8));
  pintarListaErrores("err-lexico", errLex);
  $("msg-lexico").textContent =
    estados.lexico === "ok" ? "Todos los símbolos del código fueron reconocidos." : "";

  // Sintáctico
  const nombresRegla = analisis.reglas.map((x) => x.split("->")[0].trim());
  $("num-reglas").textContent = analisis.reglas.length;
  $("num-reglas-distintas").textContent = new Set(nombresRegla).size;
  $("num-err-sintactico").textContent = errSin.length;
  pintarBarras("barras-reglas", contarPor(nombresRegla, (x) => x).slice(0, 8));
  pintarListaErrores("err-sintactico", errSin);
  $("msg-sintactico").textContent =
    estados.sintactico === "ok" ? "La estructura del código cumple la gramática."
    : estados.sintactico === "pendiente" ? "No se evaluó porque el análisis léxico encontró un error."
    : "";

  // Semántico
  $("num-err-semantico").textContent = errSem.length;
  $("num-err-total").textContent = r.errores.length;
  $("tiempo-analisis-2").textContent = r.tiempoMS + " ms";
  pintarListaErrores("err-semantico", errSem);
  $("msg-semantico").textContent =
    estados.semantico === "ok" ? "No se encontraron variables sin declarar ni otros problemas semánticos."
    : estados.semantico === "pendiente" ? "No se evaluó porque una etapa anterior encontró errores."
    : "";

  seleccionarTab(
    estados.lexico === "error" ? "lexico"
    : estados.sintactico === "error" ? "sintactico"
    : estados.semantico === "error" ? "semantico"
    : "lexico");
}

/* ---------- 3. PANELES DE INICIO ---------- */

function resumenHistorial() {
  const h = cargarHistorial();
  const ok = h.filter((e) => e.estado === "ok").length;
  return { h, ok, error: h.length - ok };
}

// Anillo de éxito / errores 
function pintarDonut(ok, error) {
  const total = ok + error;
  const R = 48;
  const C = 2 * Math.PI * R;
  const largoOk = total ? (C * ok) / total : 0;
  const largoError = total ? (C * error) / total : 0;
  const pct = total ? Math.round((ok / total) * 100) : 0;
  const arco = (largo, desfase, relleno) =>
    largo > 0
      ? `<circle cx="60" cy="60" r="${R}" class="donut-arco" stroke="url(#${relleno})"
           stroke-dasharray="${largo} ${C - largo}" stroke-dashoffset="${-desfase}"
           transform="rotate(-90 60 60)"/>`
      : "";
  $("donut").innerHTML = `
    <svg viewBox="0 0 120 120" role="img" aria-label="Porcentaje de traducciones completadas">
      <defs>
        <linearGradient id="gOk" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#10b981"/><stop offset="1" stop-color="#0ea5e9"/>
        </linearGradient>
        <linearGradient id="gError" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#a78bfa"/><stop offset="1" stop-color="#f472b6"/>
        </linearGradient>
      </defs>
      <circle cx="60" cy="60" r="${R}" class="donut-pista"/>
      ${arco(largoOk, 0, "gOk")}
      ${arco(largoError, largoOk, "gError")}
      <text x="60" y="60" class="donut-num">${total ? pct + "%" : "—"}</text>
      <text x="60" y="76" class="donut-et">${total ? "éxito" : "sin datos"}</text>
    </svg>`;
  $("leg-ok").textContent = ok;
  $("leg-error").textContent = error;
}

function agregarDato(dl, etiqueta, valor, estado) {
  const dt = document.createElement("dt");
  dt.textContent = etiqueta;
  const dd = document.createElement("dd");
  dd.textContent = valor;
  if (estado) dd.dataset.estado = estado;
  dl.append(dt, dd);
}

function pintarUltima(h) {
  const cont = $("ultima-traduccion");
  cont.innerHTML = "";
  if (h.length === 0) {
    cont.textContent = "Todavía no has traducido nada.";
    return;
  }
  const e = h[0];
  const dl = document.createElement("dl");
  agregarDato(dl, "Archivo", e.nombre);
  agregarDato(dl, "Estado", e.estado === "ok" ? "Completado" : "Con errores", e.estado);
  agregarDato(dl, "Tiempo", e.tiempoMS + " ms");
  agregarDato(dl, "Fecha", new Date(e.fecha).toLocaleString("es-MX"));
  cont.appendChild(dl);
}

function pintarRecientes(h) {
  const cuerpo = $("tabla-recientes");
  cuerpo.innerHTML = "";
  $("recientes-vacio").hidden = h.length > 0;
  for (const e of h.slice(0, 5)) {
    const fila = document.createElement("tr");
    [e.nombre, new Date(e.fecha).toLocaleString("es-MX"), e.estado === "ok" ? "Completado" : "Con errores", e.tiempoMS + " ms"]
      .forEach((dato, i) => {
        const td = document.createElement("td");
        td.textContent = dato;
        if (i === 2) td.dataset.estado = e.estado;
        fila.appendChild(td);
      });
    cuerpo.appendChild(fila);
  }
}

function pintarInicio() {
  const r = resumenHistorial();
  pintarDonut(r.ok, r.error);
  pintarUltima(r.h);
  pintarRecientes(r.h);
}