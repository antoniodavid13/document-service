# Document Service — IlernaSmart

Microservicio de gestión de documentos PDF y generación de contenido educativo con IA para la plataforma IlernaSmart. Procesa documentos subidos por profesores y genera resúmenes automáticos mediante la API de Groq.

## Tecnologías

- Java 17
- Spring Boot 3
- Spring Security (validación JWT)
- MySQL
- Apache PDFBox (extracción de texto)
- Groq API — modelo `llama-3.3-70b-versatile`
- Maven

## Funcionalidad

Permite a los profesores subir documentos PDF organizados por temas y subtemas. Al subir un documento, el servicio extrae el texto (hasta 4.000 caracteres) y llama a la API de Groq para generar un resumen automático que servirá como base para los tests y el módulo de repaso.

## Endpoints principales

### Documentos

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/documents` | Subir documento PDF (multipart) |
| GET | `/api/documents/{id}` | Obtener documento por ID |
| GET | `/api/documents/subject/{subjectId}` | Documentos de una asignatura |
| GET | `/api/documents/{id}/chunks` | Fragmentos de texto del documento |
| DELETE | `/api/documents/{id}` | Eliminar documento |

### Temas y subtemas

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/documents/topics/subject/{subjectId}` | Temas de una asignatura con documentos |
| POST | `/api/documents/topics` | Crear tema |
| DELETE | `/api/documents/topics/{topicId}` | Eliminar tema |
| POST | `/api/documents/subtopics` | Crear subtema |

## Configuración

```yaml
# application.yml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ilernasmart_documents
    username: root
    password: tu_password
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 20MB

groq:
  api-key: tu_api_key_de_groq

jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

> ⚠️ La API Key de Groq se almacena en `application.yml` excluido mediante `.gitignore`. En producción se recomienda migrarla a una variable de entorno del servidor.

## Requisitos previos

- Java 17+
- MySQL 8+
- Maven 3.8+
- Cuenta en [Groq](https://console.groq.com) con API Key válida
- API Gateway en marcha en el puerto 8080

## Instalación y arranque

```bash
# Clonar el repositorio
git clone https://github.com/antoniodavid13/document-service.git
cd document-service

# Crear la base de datos
mysql -u root -p -e "CREATE DATABASE ilernasmart_documents;"

# Compilar y arrancar
mvn spring-boot:run
```

El servicio arranca en el puerto **8083**.

## Estructura del proyecto

```
src/main/java/
├── controller/        # DocumentController, TopicController
├── service/           # DocumentService, GroqService, PdfExtractorService
├── repository/        # DocumentRepository, TopicRepository
├── model/             # Document, Topic, Subtopic
├── dto/               # DocumentResponse, TopicResponse
└── security/          # JwtFilter, SecurityConfig
```

## Limitaciones conocidas

- **Calidad de extracción**: tablas, imágenes y texto en columnas dentro del PDF pueden no convertirse correctamente a texto plano, reduciendo la coherencia del contenido generado.
- **Truncado de contenido**: el texto enviado al modelo se limita a 4.000 caracteres, lo que puede excluir conceptos relevantes de documentos extensos.
