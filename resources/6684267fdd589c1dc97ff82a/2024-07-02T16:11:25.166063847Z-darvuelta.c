#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Función para invertir una cadena
void invertirCadena(char *str) {
    int longitud = strlen(str);
    for (int i = 0; i < longitud / 2; ++i) {
        char temp = str[i];
        str[i] = str[longitud - i - 1];
        str[longitud - i - 1] = temp;
    }
}

int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Uso: %s <archivo_entrada> <archivo_salida>\n", argv[0]);
        return EXIT_FAILURE;
    }

    // Nombres de los archivos de entrada y salida desde los argumentos
    char *archivoEntrada = argv[1];
    char *archivoSalida = argv[2];

    // Abrir el archivo de entrada en modo lectura
    FILE *entrada = fopen(archivoEntrada, "r");
    if (entrada == NULL) {
        perror("Error al abrir el archivo de entrada");
        return EXIT_FAILURE;
    }

    // Mover el puntero al final del archivo para obtener el tamaño
    fseek(entrada, 0, SEEK_END);
    long tamaño = ftell(entrada);
    rewind(entrada);

    // Reservar memoria para el contenido del archivo
    char *contenido = (char *)malloc((tamaño + 1) * sizeof(char));
    if (contenido == NULL) {
        perror("Error al asignar memoria");
        fclose(entrada);
        return EXIT_FAILURE;
    }

    // Leer el contenido del archivo
    fread(contenido, sizeof(char), tamaño, entrada);
    contenido[tamaño] = '\0';  // Añadir el carácter nulo al final

    // Cerrar el archivo de entrada
    fclose(entrada);

    // Invertir la cadena
    invertirCadena(contenido);

    // Abrir el archivo de salida en modo escritura
    FILE *salida = fopen(archivoSalida, "w");
    if (salida == NULL) {
        perror("Error al abrir el archivo de salida");
        free(contenido);
        return EXIT_FAILURE;
    }

    // Escribir la cadena invertida en el archivo de salida
    fwrite(contenido, sizeof(char), tamaño, salida);

    // Cerrar el archivo de salida
    fclose(salida);

    // Liberar la memoria
    free(contenido);

    printf("El archivo ha sido invertido y guardado en '%s'\n", archivoSalida);

    return EXIT_SUCCESS;
}
