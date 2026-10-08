int contador = 0;
int limite = 5;
string mensaje = "Iniciando";
boolean activo = true;
double promedio = 10.5;

print(mensaje);

while (contador < limite)
{
    print(contador);

    if (contador == 2)
    {
        print("Llegamos al punto medio");
    }
    else
    {
        print("Continuamos");
    }

    contador = contador + 1;
}

do
{
    print(contador);
    contador = contador - 1;
}
while (contador > 0);

for (i = 0; i < 3; i = i + 1)
{
    println(i);
}

switch (contador)
{
    case 0:
        print("El contador termino en cero");
        break;

    case 1:
        print("El contador termino en uno");
        break;

    default:
        print("El contador tiene otro valor");
        break;
}

foreach (int numero in numeros)
{
    print(numero);
}

return;