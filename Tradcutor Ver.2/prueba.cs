// Pruebas
int[] numeros = { 5, 10, 15, 20 };

int sumarElementos(int[] datos)
{
    int suma = 0;

    foreach (int numero in datos)
    {
        suma += numero;
    }

    return suma;
}

double calcularPromedio(int[] datos)
{
    int suma = sumarElementos(datos);
    double promedio = Convert.ToDouble(suma) / datos.Length;

    return promedio;
}

string mensaje = null;
string nombre = "Kain";

int limite = int.Parse("10");
double promedio = calcularPromedio(numeros);
bool activo = bool.Parse("true");

char inicial = Convert.ToChar(65);
int codigo = Convert.ToInt32(inicial);

int sumaTotal = sumarElementos(numeros);
int acumulado = 0;
int residuo = 17 % 5;

for (int i = 0; i < numeros.Length; i++)
{
    if (numeros[i] == 10)
    {
        continue;
    }

    acumulado += numeros[i];
}

while (acumulado < 45)
{
    acumulado++;

    if (acumulado == 42)
    {
        continue;
    }

    if (acumulado == 44)
    {
        break;
    }
}

do
{
    acumulado--;
}
while (acumulado > 42);

int opcion = 2;

switch (opcion)
{
    case 1:
        acumulado += 1;
        break;

    case 2:
        acumulado += 3;
        break;

    default:
        acumulado = 0;
        break;
}

if (mensaje == null)
{
    mensaje = "Resumen";
}

try
{
    if (promedio > limite && activo)
    {
        Console.WriteLine("El promedio supera el limite");
    }
    else
    {
        Console.WriteLine("El promedio no supera el limite");
    }

    Console.WriteLine(mensaje);
    Console.WriteLine(nombre);
    Console.WriteLine(promedio);
    Console.WriteLine(sumaTotal);
    Console.WriteLine(acumulado);
    Console.WriteLine(residuo);
    Console.WriteLine(inicial);
    Console.WriteLine(codigo);

    int divisor = 0;
    int resultado = sumaTotal / divisor;

    Console.WriteLine(resultado);
}
catch (Exception)
{
    Console.WriteLine("Se capturo una excepcion");
}
finally
{
    Console.WriteLine("Proceso finalizado");
}

Console.WriteLine(activo);
