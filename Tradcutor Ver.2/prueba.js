let contador = 0;
let limite = 5;
let mensaje = "Iniciando";
let activo = true;
let promedio = 10.5;
console.log(mensaje);
while (contador < limite) {
  console.log(contador);
  if (contador == 2) {
    console.log("Llegamos al punto medio");
  } else {
    console.log("Continuamos");
  }
  contador = contador + 1;
}
do {
  console.log(contador);
  contador = contador - 1;
} while (contador > 0);
for (let i = 0; i < 3; i = i + 1) {
  console.log(i);
}
switch (contador) {
  case 0:
    console.log("El contador termino en cero");
    break;
    break;
  case 1:
    console.log("El contador termino en uno");
    break;
    break;
  default:
    console.log("El contador tiene otro valor");
    break;
    break;
}
for (const numero of numeros) {
  console.log(numero);
}
return;
