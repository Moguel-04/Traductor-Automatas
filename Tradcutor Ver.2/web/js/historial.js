// Guarda el historial en el navegador (localStorage)
const CLAVE_HISTORIAL = "traductor.historial";

function cargarHistorial() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_HISTORIAL)) || [];
  } catch (e) {
    return [];
  }
}

function guardarEntrada(entrada) {
  const lista = cargarHistorial();
  lista.unshift(entrada);                      
  try {
    localStorage.setItem(CLAVE_HISTORIAL, JSON.stringify(lista.slice(0, 100)));
  } catch (e) { /* si el navegador no deja guardar, simplemente no se guarda */ }
}

function borrarHistorial() {
  localStorage.removeItem(CLAVE_HISTORIAL);
}