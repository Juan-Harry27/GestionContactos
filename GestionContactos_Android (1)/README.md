# Gestión de contactos (Android / Java)

Proyecto Android nativo con tres pantallas y SQLite, preparado para abrir en Android Studio.

## Funciones incluidas
- Registro de contactos con país/código, nombre, teléfono, nota y foto opcional.
- Validación del nombre (letras y espacios) y teléfono (dígitos).
- Persistencia local SQLite.
- Lista de contactos y búsqueda.
- Selección de contacto para editar, eliminar, compartir, ver foto o llamar.
- Selector de imagen desde galería.
- Diálogo Sí/No antes de llamar y solicitud del permiso `CALL_PHONE`.

## Abrir
1. Extrae el ZIP.
2. En Android Studio pulsa **Open** y selecciona esta carpeta.
3. Acepta la sincronización de Gradle.
4. Si Android Studio solicita instalar Android SDK 35, instálalo desde SDK Manager.
5. Ejecuta en un emulador o dispositivo Android.

## Estructura
- `app/src/main/java/com/example/gestioncontactos/`: código Java.
- `app/src/main/res/layout/`: interfaces XML.
- `app/src/main/AndroidManifest.xml`: actividades y permiso de llamada.

## Nota de entrega
Antes de publicar, abre y ejecuta el proyecto en tu entorno, revisa el código y agrega tu nombre/número de cuenta y capturas reales en este README según las instrucciones de tu clase. No incluyas datos personales de terceros en el repositorio.
