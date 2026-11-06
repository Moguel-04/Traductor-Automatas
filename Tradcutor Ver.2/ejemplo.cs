        // Variables
        int edad = 25, matematica = 85, ciencia = 90,
            historia = 78, x = 5, y = 10, z = 15;
        string nombre = "Carlos", apellido = "López",
            ciudad = "Madrid", pais = "España";
        bool activo = true;
        int puntuacion = 100;

        // Arreglos
        int[] edades = { 25, 30, 35, 40 };
        string[] frutas = { "manzana", "naranja", "plátano" };
        bool[] flags = { true, false, true };
        int[] numeros = { 1, 2, 3, 4, 5 };

        // Funciones
        static void Despedir()
        {
            Console.WriteLine("Adiós, mundo");
        }
        int Sumar(int a, int b)
        {
            int resultado = a + b;
            return resultado;
        }
        int respuesta = Sumar(10, 5);
        Console.WriteLine(respuesta);

        // WHILE principal
        int contador = 0;
        while (contador < 10)
        {
            if (contador % 2 == 0)
            {
                Console.WriteLine("Número par: " + contador);
                switch (contador)
                {
                    case 0:
                    {
                        Console.WriteLine("Iniciando conteo");
                        break;
                    }
                    case 2:
                    {
                        Console.WriteLine("Número par medio: " + contador);
                        if (activo == true)
                        {
                            Console.WriteLine("Sistema activo en caso 2");
                            int procesados = 0;
                            while (procesados < 3)
                            {
                                Console.WriteLine("Procesando: " + procesados);
                                procesados += 1;
                            }
                        }
                        break;
                    }
                    case 4:
                    {
                        Console.WriteLine("Número par medio: " + contador);
                        try
                        {
                            int calculo = contador / (contador - 2);
                            Console.WriteLine("Cálculo exitoso: " + calculo);
                        }
                        catch (DivideByZeroException)
                        {
                            Console.WriteLine("Error en cálculo para: " + contador);
                        }
                        break;
                    }
                    case 6:
                    {
                        Console.WriteLine("Número par medio: " + contador);
                        for (int i = 0; i < 2; i++)
                        {
                            Console.WriteLine("For interno en caso 6");
                        }
                        break;
                    }
                    case 8:
                    {
                        Console.WriteLine("Número par medio: " + contador);
                        // Longitud del texto
                        Console.WriteLine(("texto prueba").Length);
                        // Ordenar arreglo
                        Array.Sort(numeros);
                        break;
                    }
                    default:
                        Console.WriteLine("Otro número par: " + contador);
                        break;
                }
            }
            else
            {
                switch (contador)
                {
                    case 1:
                    {
                        Console.WriteLine("Número impar: " + contador);
                        if (edad >= 18)
                        {
                            Console.WriteLine("Usuario adulto verificando...");
                            switch (puntuacion)
                            {
                                case 100:
                                {
                                    Console.WriteLine("¡Puntuación perfecta!");
                                    int intentos = 2;
                                    while (intentos > 0)
                                    {
                                        Console.WriteLine("Intentos: " + intentos);
                                        if (intentos == 1)
                                        {
                                            Console.WriteLine("¡Último intento!");
                                        }
                                        intentos -= 1;
                                    }
                                    break;
                                }
                                case 50:
                                case 75:
                                {
                                    Console.WriteLine("Puntuación media");
                                    for (int j = 0; j < 2; j++)
                                    {
                                        Console.WriteLine("Optimizando...");
                                    }
                                    break;
                                }
                                default:
                                {
                                    Console.WriteLine("Puntuación: " + puntuacion);
                                    try
                                    {
                                        string[] datos = { "a", "b" };
                                        foreach (string item in datos)
                                        {
                                            Console.WriteLine("Dato: " + item);
                                        }
                                    }
                                    catch (Exception error)
                                    {
                                        Console.WriteLine("Error datos: " + error.Message);
                                    }
                                    break;
                                }
                            }
                        }
                        else
                        {
                            Console.WriteLine("Usuario menor de edad");
                            if (!(edad >= 16))
                            {
                                Console.WriteLine("Acceso restringido");
                            }
                        }
                        break;
                    }
                    case 3:
                    {
                        Console.WriteLine("Número impar: " + contador);
                        foreach (int num in numeros)
                        {
                            if (num > 2)
                            {
                                Console.WriteLine("Número > 2: " + num);
                                switch (num)
                                {
                                    case 3:
                                    {
                                        int calculoo = (num * 10) + (edad / 5);
                                        Console.WriteLine("Cálculo 3: " + calculoo);
                                        break;
                                    }
                                    case 4:
                                    {
                                        Console.WriteLine("Cuadrado perfecto");
                                        break;
                                    }
                                    case 5:
                                    {
                                        int multi = 1;
                                        while (multi <= 2)
                                        {
                                            Console.WriteLine(
                                                num + " x " + multi +
                                                " = " + (num * multi));
                                            multi += 1;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case 5:
                    {
                        Console.WriteLine("Número impar: " + contador);
                        int repeticiones = 0;
                        do
                        {
                            Console.WriteLine(
                                "Do-while ejecución: " + repeticiones +
                                " en contador: " + contador);
                            repeticiones += 1;
                        }
                        while (repeticiones < 3);
                        break;
                    }
                    case 7:
                    {
                        Console.WriteLine("Número impar: " + contador);
                        break;
                    }
                    case 9:
                    {
                        Console.WriteLine("Número impar: " + contador);
                        try
                        {
                            Console.WriteLine("Proceso crítico final...");
                            if (activo)
                            {
                                try
                                {
                                    int division = 100 / (edad - 25);
                                    Console.WriteLine("División: " + division);
                                }
                                catch (DivideByZeroException)
                                {
                                    Console.WriteLine("División fallida");
                                }
                            }
                            if (puntuacion < 0)
                            {
                                throw new InvalidOperationException(
                                    "Puntuación inválida");
                            }
                        }
                        catch (Exception errorCritico)
                        {
                            Console.WriteLine("Error: " + errorCritico.Message);
                            switch (errorCritico.Message)
                            {
                                case "Puntuación inválida":
                                    puntuacion = 0;
                                    Console.WriteLine("Puntuación reiniciada");
                                    break;
                                default:
                                    Console.WriteLine("Error no manejado");
                                    break;
                            }
                        }
                        finally
                        {
                            Console.WriteLine("Proceso finalizado");
                        }
                        break;
                    }
                }
            }
            contador += 1;
        }
        // Finalización del programa
        Console.WriteLine("Finalizando programa...");
        if (puntuacion > 50)
        {
            Console.WriteLine("Resultado satisfactorio");
            for (int k = 0; k < 2; k++)
            {
                try
                {
                    int finalScore = puntuacion * 2;
                    Console.WriteLine("Puntuación final: " + finalScore);
                    if (!(edad >= 18 && activo))
                    {
                        Console.WriteLine("Acceso denegado final");
                    }
                }
                catch (Exception)
                {
                    Console.WriteLine("Error cálculo final");
                }
            }
        }
        Console.WriteLine("=== RESUMEN FINAL ===");
        switch (puntuacion)
        {
            case 0:
                Console.WriteLine("Puntuación reiniciada: " + puntuacion);
                break;
            case 50:
                int numero = int.Parse(Console.ReadLine());
                Console.WriteLine(numero + 1);

                double decimal = double.Parse(Console.ReadLine());
                Console.WriteLine(decimal);

                int entero = Convert.ToInt32(Console.ReadLine());
                double precio = Convert.ToDouble(Console.ReadLine());

                Console.WriteLine(entero);
                Console.WriteLine(precio);

                string dato = "25";
                int num = int.Parse(dato);
                Console.WriteLine(num + 5);

                int prueba;
                prueba = Convert.ToInt32(Console.ReadLine());
                break;
            case 75:
                Console.WriteLine("Puntuación media final: " + puntuacion);
                break;
            case 100:
                Console.WriteLine("¡Puntuación perfecta final!");
                break;
            default:
                Console.WriteLine("Nombre: " + nombre + " " + apellido);
                Console.WriteLine("Edad: " + edad);
                Console.WriteLine("Puntuación: " + puntuacion);
                Console.WriteLine("Estado: " + activo);
                Console.WriteLine("Ciudad: " + ciudad);
                break;
        }
        Console.WriteLine("Programa ejecutado exitosamente");
        Despedir();