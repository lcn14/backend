# 🩺 Backend - Sistema de Gestión Clínica AURA
Este proyecto representa el backend del sistema de gestión de citas, pacientes y servicios de una clínica de terapias.

---

## 📌 Tecnologías Seleccionadas

| Elemento          | Tecnología         | Justificación Breve |
|-------------------|--------------------|----------------------|
| Lenguaje          | Java               | Familiaridad del equipo, fuerte en backend |
| Framework         | Spring Boot        | Facilita la creación de APIs REST |
| Base de Datos     | MySQL              | Conocimiento previo del equipo, relacional |
| Control de Versiones | Git + GitHub    | Trabajo en equipo, historial claro |
| Gestión de ramas  | Gitflow            | Organización y colaboración efectiva |

---

## 🧱 Estructura Base del Proyecto

```bash
backend/
├── src/
│   ├── main/
│   │   ├── java/            # Código Java (controladores, servicios, etc.)
│   │   └── resources/       # Configuración: application.yml, etc.
├── docs/                    # Documentación técnica del proyecto
└── README.md                # Este archivo


🌱 **Gestión de Ramas (Gitflow)**

Usaremos el modelo **Gitflow** para organizarnos:

- **main**: Rama principal, contiene el código en producción.
- **develop**: Rama de integración, aquí se mezclan las features.
- **feature/nombre**: Cada funcionalidad nueva se trabaja en su propia rama.
- **hotfix/nombre**: Correcciones urgentes sobre main.
- **release/nombre**: Preparación para una nueva versión.

---

🧠 **Convenciones para commits**

Usaremos esta convención para los mensajes de commit:

- **feat**: para nuevas funcionalidades.
- **fix**: para correcciones.
- **docs**: para documentación.
- **refactor**: para mejoras internas.
- **test**: para pruebas.
- **chore**: tareas menores (formato, build, etc.).


