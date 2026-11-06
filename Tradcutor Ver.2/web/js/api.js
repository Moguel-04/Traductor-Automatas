//Habla con el servidor Java
async function traducirCodigo(codigo) {
  const resp = await fetch("/api/traducir", { method: "POST", body: codigo });
  if (!resp.ok) throw new Error("El servidor respondió " + resp.status);
  return await resp.json();
}