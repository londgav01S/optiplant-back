# PLAN DE TRABAJO — Sistema de Inventario Multi-Sucursal
**Prueba Técnica – OptiPlant Consultores**
**Estándar de calidad: ISO/IEC 25010**

---

## Estado actual del proyecto

| Componente | Estado |
|---|---|
| Entidades JPA | ✅ Completo (Venta.java restaurada) |
| DTOs request/response | ⚠️ Parcial (ApiResponse, DespachoTransferenciaRequest, LineaRecepcionRequest, DashboardResponse, MovimientoResponse vacíos) |
| Repositorios | ⚠️ Parcial (InventarioRepository vacío, faltan queries) |
| Interfaces de servicio | ⚠️ Parcial (ProductoService, TransferenciaService vacíos; firmas incorrectas en CompraService, InventarioService) |
| Implementaciones de servicio | ❌ Todos son stubs (notImplemented) |
| Controladores REST | ⚠️ Solo AuthController + CompraController |
| Autenticación JWT | ❌ AuthServiceImpl es placeholder |
| Pruebas unitarias | ⚠️ Solo CompraServiceTest (cobertura ~8%) |
| Build (build.gradle) | ❌ Faltan dependencias críticas (web, validation, JWT, PostgreSQL, Flyway, Swagger) |
| Migraciones Flyway | ⚠️ V1, V2, V4, V5 (falta V3, V6) |
| Docker Compose | ❓ Por verificar |
| README | ⚠️ Incompleto |
| Swagger/OpenAPI | ⚠️ Config existe pero sin anotaciones en endpoints |

---

## Fases de desarrollo

---

### FASE 1 — Infraestructura y compilación base
**Objetivo:** Que el proyecto compile y levante sin errores.
**RNF cubiertos:** RT-03 (Docker), RT-01 (capas separadas)

#### 1.1 Corregir `build.gradle`
- [ ] Agregar `spring-boot-starter-web` (controllers REST)
- [ ] Agregar `spring-boot-starter-validation` (@Valid, @NotNull)
- [ ] Agregar `org.postgresql:postgresql` (driver DB)
- [ ] Agregar `org.flywaydb:flyway-core` (migraciones)
- [ ] Agregar JJWT (`jjwt-api:0.12.x`, `jjwt-impl`, `jjwt-jackson`)
- [ ] Agregar `springdoc-openapi-starter-webmvc-ui` (Swagger)
- [ ] Corregir dependencias de test (usar `spring-boot-starter-test`, `spring-security-test`, `mockito-core`)
- [ ] Agregar H2 en scope `testRuntimeOnly` para tests

#### 1.2 Completar archivos vacíos — DTOs
- [ ] `dto/ApiResponse.java` — record genérico con success, message, data, timestamp + factory methods
- [ ] `dto/request/LineaRecepcionRequest.java` — idDetalle + cantidadRecibida
- [ ] `dto/request/DespachoTransferenciaRequest.java` — transportista + fechaEstimadaLlegada + lineas
- [ ] `dto/response/DashboardResponse.java` — KPIs del dashboard
- [ ] `dto/response/MovimientoResponse.java` — datos del movimiento de inventario
- [ ] Actualizar `dto/request/RecepcionCompraRequest.java` — remover campo `idOrden` (ya va en el path)

#### 1.3 Completar interfaces vacías
- [ ] `service/ProductoService.java` — listar, crear, obtenerPorId, actualizar, desactivar
- [ ] `service/TransferenciaService.java` — listar, crear(+usuarioId), obtenerPorId, aprobar(+usuarioId), rechazar, despachar, recepcionar, definirTratamientoFaltante
- [ ] Actualizar `service/CompraService.java` — agregar `usuarioId` a crear() y recepcionar()
- [ ] Actualizar `service/VentaService.java` — agregar `usuarioId` a crear()
- [ ] Actualizar `service/InventarioService.java` — firmas de registrarIngreso(+TipoMovimiento, +precioUnitario, +usuarioId) y registrarRetiro(+TipoMovimiento, +usuarioId)

#### 1.4 Completar repositorios vacíos/incompletos
- [ ] `repository/InventarioRepository.java`:
  - `findByProductoIdAndSucursalId(productoId, sucursalId)`
  - `findBySucursalId(sucursalId)`
  - `findBajoStockMinimo()` — JPQL: stockActual < stockMinimo
  - Paginación: `findAll(Pageable)`, filtros por sucursalId/productoId
- [ ] Actualizar `OrdenCompraRepository.java`:
  - `findByIdWithDetalles(id)` — JOIN FETCH detalles
  - Métodos paginados con filtros
- [ ] Actualizar `VentaRepository.java` — métodos paginados
- [ ] Actualizar `ProductoRepository.java` — búsqueda paginada con filtros nombre/sku
- [ ] Actualizar `UsuarioRepository.java` — búsqueda paginada con filtros
- [ ] Actualizar `TransferenciaRepository.java` — métodos paginados

#### 1.5 Clases de excepción
- [ ] `exception/BusinessException.java` — para reglas de negocio
- [ ] `exception/ResourceNotFoundException.java` — para entidades no encontradas
- [ ] `exception/GlobalExceptionHandler.java` — @RestControllerAdvice con handlers para:
  - BusinessException → 422 Unprocessable Entity
  - ResourceNotFoundException → 404 Not Found
  - MethodArgumentNotValidException → 400 Bad Request (validación)
  - UnsupportedOperationException → 501 Not Implemented
  - Exception genérica → 500 Internal Server Error + log

---

### FASE 2 — Seguridad y autenticación (RNF-11, RNF-12, RNF-13)
**Objetivo:** JWT funcional, RBAC por roles, trazabilidad de acciones.

#### 2.1 Autenticación JWT real
- [x] Implementar `AuthServiceImpl.java`:
  - Verificar usuario activo en DB
  - Validar contraseña con BCrypt
  - Generar JWT con claims (email, rol, sucursalId)
  - Retornar `AuthResponse` (token, email, nombreCompleto)
- [x] Actualizar `UserDetailsServiceImpl.java`:
  - Cargar usuario real desde `UsuarioRepository`
  - Retornar `UserDetails` con autoridades = `ROLE_<rolNombre>`
- [x] Verificar `JwtAuthFilter.java` extrae claims correctamente
- [x] Verificar `SecurityConfig.java` aplica filtros y rutas correctas
- [x] Agregar seed de usuarios admin/gerente/operador en V5 (o nueva migración V6)

#### 2.2 Control de acceso RBAC
**Jerarquía:** ADMIN > GERENTE > OPERADOR
- [x] ADMIN: acceso total a todos los endpoints
- [x] GERENTE: lectura global, escritura en su sucursal
- [x] OPERADOR: solo operaciones de su sucursal
- [x] Aplicar `@PreAuthorize` en todos los controllers existentes

Nota de alcance actual: reglas RBAC aplicadas y validadas en `CompraController`; endpoints permitidos actualmente devuelven 501 en operaciones aún no implementadas del servicio.

---

### FASE 3 — Implementación de servicios CRUD base
**Objetivo:** Operaciones básicas funcionando.
**RNF cubiertos:** RNF-01 (completitud), RNF-02 (corrección), RNF-14 (modularidad)

#### Patrones de diseño a aplicar en todos los servicios:

#### 3.1 `SucursalServiceImpl.java`
#### 3.2 `UsuarioServiceImpl.java`
- [x] `listar(page, size, activo, sucursalId)` — paginado con filtros
- [x] `crear(request)` — hashear password con BCrypt, validar email único, asignar rol y sucursal
- [x] `obtenerPorId(id)`
- [x] `actualizar(id, request)` — nombre, apellido, rol, sucursal
- [x] `cambiarPassword(id, nuevaPassword)` — hashear nueva password
- [x] `desactivar(id)` — soft delete

#### 3.3 `ProductoServiceImpl.java`
- [x] `listar(page, size, nombre, sku)` — paginado con filtros
- [x] `crear(request)` — validar SKU único global, crear unidades de medida
- [x] `obtenerPorId(id)` — con sus unidades
- [x] `actualizar(id, request)` — nombre, descripción, unidades
- [x] `desactivar(id)` — validar que no tenga stock activo en alguna sucursal

#### 3.4 `ProveedorServiceImpl.java`
- [x] `listarActivos()` — findByActivoTrue()
- [x] `crear(proveedor)` — nuevo proveedor
- [x] `obtenerPorId(id)` — con sus órdenes de compra
- [x] `historialCompras(proveedorId, desde, hasta)` — órdenes en rango de fechas

#### 3.5 `ListaPreciosServiceImpl.java`
- [x] `listarActivas()` — listas activas
- [x] `crear(nombre, descripcion)` — nueva lista activa
- [x] `actualizar(id, nombre, descripcion, activo)` — incluyendo activar/desactivar

---

### FASE 4 — Módulo de inventario (RF-02, RF-03, RF-04, RF-08, RF-13)
**Objetivo:** Gestión completa con trazabilidad inmutable (RT-05).

#### 4.1 `InventarioServiceImpl.java`
- [x] `consultarGlobal(page, size, sucursalId, productoId)` — paginado con filtros
- [x] `consultarPorSucursal(sucursalId)` — todos los productos de la sucursal
- [x] `obtenerPorId(id)`
- [x] `actualizarConfig(id, request)` — stockMinimo, stockMaximo
- [x] `registrarIngreso(inventarioId, tipo, cantidad, motivo, precioUnitario, usuarioId)`:
  - Actualizar stockActual (suma)
  - Recalcular CPP: `(stockAntes * cppAntes + cantidad * precio) / (stockAntes + cantidad)`
  - Registrar MovimientoInventario (trazabilidad inmutable)
  - Si stock >= stockMinimo: resolver alertas activas del producto
- [x] `registrarRetiro(inventarioId, tipo, cantidad, motivo, usuarioId)`:
  - Validar stock suficiente (lanzar BusinessException si no hay)
  - Actualizar stockActual (resta)
  - Registrar MovimientoInventario (trazabilidad inmutable)
  - Si stock < stockMinimo: crear alerta STOCK_MINIMO
- [x] `historialMovimientos(inventarioId, page, size)` — paginado ordenado por fecha desc

**Clean Code:**
- Método privado `crearMovimiento(inventario, usuario, tipo, cantidad, motivo, referencia)`
- Método privado `evaluarAlertas(inventario)` — separar lógica de alertas
- Constante para estado "ACTIVA" y "RESUELTA"

#### 4.2 `AlertaServiceImpl.java`
- [x] `listarActivas(sucursalId, tipo)` — filtrar por sucursal y/o tipo
- [x] `resolver(id)` — cambiar estado a RESUELTA, setear fechaResolucion

---

### FASE 5 — Módulo de compras con CPP (RF-05, HU-CP-01)
**RNF:** RNF-02 (exactitud CPP = 100%), RN-03 (CPP automático)

#### 5.1 `CompraServiceImpl.java`
- [x] `listar(page, size, sucursalId, proveedorId, estado)` — paginado con filtros
- [x] `crear(request, usuarioId)`:
  - Cargar proveedor, sucursal, usuario (lanzar ResourceNotFoundException si no existen)
  - Para cada línea: cargar producto, calcular subtotal con descuento
  - Crear OrdenCompra en estado PENDIENTE
  - No modificar inventario al crear (solo al recepcionar)
- [x] `obtenerPorId(id)` — con detalles (JOIN FETCH)
- [x] `cancelar(id)`:
  - Validar estado = PENDIENTE (lanzar BusinessException si no)
  - Cambiar estado a CANCELADA
- [x] `recepcionar(id, request, usuarioId)`:
  - Cargar orden con detalles (findByIdWithDetalles)
  - Validar estado = PENDIENTE
  - Para cada línea recibida: actualizar cantidadRecibida, llamar inventarioService.registrarIngreso (CPP automático)
  - Si alguna línea recibida < pedida → estado RECIBIDA_CON_FALTANTES, else RECIBIDA
  - Registrar fechaRecepcion

**Patrón Template Method** — descomponer recepcionar en:
- `procesarLineasRecepcion(orden, lineas, usuarioId)`
- `determinarEstadoFinal(orden)` — RECIBIDA vs RECIBIDA_CON_FALTANTES

---

### FASE 6 — Módulo de ventas con validación de stock (RF-06, RN-05)
**RNF:** RNF-02 (validación stock = 100%), RF-12 (listas de precios)

#### 6.1 `VentaServiceImpl.java`
- [ ] `listar(page, size, sucursalId, desde, hasta)` — paginado con filtros de fecha
- [ ] `crear(request, usuarioId)`:
  - Cargar sucursal, usuario, listaPrecios (opcional)
  - Para cada línea: cargar producto, cargar inventario producto+sucursal
  - **Validar stock antes de procesar cualquier línea** (lanzar BusinessException si falta stock)
  - Obtener precio de listaPrecios, aplicar descuento de línea
  - Calcular subtotal de cada línea
  - Calcular subtotal total, aplicar descuento global, calcular total
  - Guardar Venta con detalles
  - Registrar retiros de inventario por cada línea (inventarioService.registrarRetiro)
- [ ] `obtenerPorId(id)` — con detalles
- [ ] `anular(id, motivoAnulacion)`:
  - Validar estado = CONFIRMADA
  - Reintegrar stock (inventarioService.registrarIngreso con tipo DEVOLUCION)
  - Cambiar estado a ANULADA

**Patrón Template Method** — descomponer crear en:
- `validarStockParaTodasLasLineas(lineas, sucursal)` — falla temprano si no hay stock
- `inicializarVenta(sucursal, usuario, listaPrecios)` — crear encabezado
- `procesarLineas(venta, lineas, listaPrecios)` — líneas + subtotales
- `aplicarDescuentoGlobal(venta, subtotal, pct)` — total final
- `registrarRetiros(venta, sucursal, usuarioId)` — actualizar inventario

**Utility Class MonedaUtils:**
```java
MonedaUtils.aplicarDescuento(base, porcentaje) // base * (1 - pct/100)
MonedaUtils.monetario(valor) // setScale(2, HALF_UP)
```

---

### FASE 7 — Módulo de transferencias entre sucursales (RF-07)
**Objetivo:** Flujo completo de 5 pasos.
**RNF:** RN-02 (no stock negativo), RN-05 (validación)

**Flujo completo:**
```
PENDIENTE_APROBACION → EN_PREPARACION → EN_TRANSITO → RECIBIDA/RECIBIDA_CON_FALTANTES
                    ↓
                RECHAZADA
```

#### 7.1 `TransferenciaServiceImpl.java`
- [ ] `listar(page, size, sucursalId, estado)` — paginado
- [ ] `crear(request, usuarioId)`:
  - Validar sucursales origen ≠ destino
  - Cargar sucursalOrigen, sucursalDestino, usuario
  - Validar que haya stock para cada producto en sucursal origen
  - Crear Transferencia en estado PENDIENTE_APROBACION con detalles
- [ ] `obtenerPorId(id)` — con detalles
- [ ] `aprobar(id, usuarioId)`:
  - Validar estado = PENDIENTE_APROBACION
  - Cambiar a EN_PREPARACION, registrar usuarioAprueba
- [ ] `rechazar(id, motivo)`:
  - Validar estado = PENDIENTE_APROBACION
  - Cambiar a RECHAZADA, registrar motivoRechazo
- [ ] `despachar(id, request)`:
  - Validar estado = EN_PREPARACION
  - Para cada línea: validar stock en origen, registrar retiro (TRANSFERENCIA_SALIDA)
  - Actualizar cantidadDespachada en detalles
  - Cambiar estado a EN_TRANSITO, registrar transportista, fechaDespacho, fechaEstimadaLlegada
- [ ] `recepcionar(id, request)`:
  - Validar estado = EN_TRANSITO
  - Para cada línea: registrar ingreso en destino (TRANSFERENCIA_ENTRADA)
  - Calcular faltante = cantidadDespachada - cantidadRecibida
  - Si hay faltantes → RECIBIDA_CON_FALTANTES, si no → RECIBIDA
  - Registrar fechaRecepcion
- [ ] `definirTratamientoFaltante(transferenciaId, detalleId, tratamiento)`:
  - Validar estado = RECIBIDA_CON_FALTANTES
  - Actualizar tratamientoFaltante del detalle
  - Si tratamiento = REENVIO: crear nueva transferencia de faltante
  - Si tratamiento = AJUSTE_ACEPTADO: no acción adicional
  - Si tratamiento = RECLAMACION: registrar nota

---

### FASE 8 — Dashboard analítico y logística (RF-09, RF-10, HU-DB-01)
**RNF:** RNF-04 (< 2s para reportes), RF-09 (ventas últimos 4 meses)

#### 8.1 `DashboardServiceImpl.java`
- [ ] `dashboardSucursal(sucursalId)`:
  - ventasDelDia = sum(total) de ventas de hoy en la sucursal
  - ventasDelMes = sum(total) de ventas del mes en la sucursal
  - alertasActivas = count de alertas ACTIVA en la sucursal
  - transferenciasPendientes = count PENDIENTE_APROBACION o EN_PREPARACION de la sucursal
  - ordenesCompraPendientes = count PENDIENTE en la sucursal
- [ ] `dashboardGlobal()`:
  - Mismas métricas pero sin filtro de sucursal (admin)
  - Incluir ventasMensuales (últimos 4 meses, incluyendo años anteriores si aplica)
  - Incluir top productos bajo stock mínimo

#### 8.2 `LogisticaServiceImpl.java`
- [ ] `reporte(sucursalOrigenId, sucursalDestinoId, desde)`:
  - Listar transferencias completadas del rango
  - Para cada una: calcular porcentajeCumplimiento = (cantRecibida / cantSolicitada) * 100
  - calcular faltanteTotal
- [ ] `enTransito()`:
  - Listar todas las transferencias en estado EN_TRANSITO

---

### FASE 9 — Controladores REST (RNF-12 RBAC, RT-02)
**Objetivo:** API REST completa con control de acceso por roles.
**RNF:** RF-12 (RBAC), RNF-04 (respuestas < 500ms), RNF-15 (Swagger ≥ 90%)

Todos los controllers:
- Retornan `ResponseEntity<ApiResponse<T>>`
- Inyectan usuario autenticado vía `Authentication` cuando se requiere
- Aplican `@PreAuthorize` por endpoint
- Tienen anotaciones Swagger (`@Operation`, `@Tag`, `@ApiResponse`)

#### 9.1 Controllers a crear:
- [ ] `ProductoController` — GET /api/productos, POST, GET /{id}, PUT /{id}, DELETE /{id}
- [ ] `SucursalController` — GET /api/sucursales, POST, GET /{id}, PUT /{id}, DELETE /{id}
- [ ] `UsuarioController` — GET /api/usuarios, POST, GET /{id}, PUT /{id}, PATCH /{id}/password, DELETE /{id}
- [ ] `ProveedorController` — GET /api/proveedores, POST, GET /{id}, GET /{id}/historial-compras
- [ ] `ListaPreciosController` — GET /api/listas-precios, POST, PUT /{id}
- [ ] `InventarioController` — GET /api/inventario, GET /sucursal/{id}, GET /{id}, PUT /{id}/config, POST /{id}/ingresos, POST /{id}/retiros, GET /{id}/movimientos
- [ ] `AlertaController` — GET /api/alertas, PATCH /{id}/resolver, POST /config
- [ ] `VentaController` — GET /api/ventas, POST, GET /{id}, PATCH /{id}/anular
- [ ] `TransferenciaController` — GET /api/transferencias, POST, GET /{id}, PATCH /{id}/aprobar, PATCH /{id}/rechazar, PATCH /{id}/despachar, POST /{id}/recepcion, PATCH /{id}/detalles/{detalleId}/tratamiento
- [ ] `DashboardController` — GET /api/dashboard/sucursal/{id}, GET /api/dashboard/global
- [ ] `LogisticaController` — GET /api/logistica/reporte, GET /api/logistica/en-transito
- [ ] Actualizar `CompraController` — firmas ya cambiadas en service

#### 9.2 Mapeo de roles por endpoint:

| Módulo | ADMIN | GERENTE | OPERADOR |
|---|---|---|---|
| Usuarios CRUD | ✅ | ✅ (solo su sucursal) | ❌ |
| Sucursales CRUD | ✅ | ✅ ver | ✅ ver |
| Productos CRUD | ✅ | ✅ | ✅ ver |
| Inventario consulta | ✅ | ✅ | ✅ (su sucursal) |
| Inventario config | ✅ | ✅ | ❌ |
| Inventario movimientos | ✅ | ✅ | ✅ |
| Ventas | ✅ | ✅ | ✅ |
| Compras | ✅ | ✅ | ✅ |
| Transferencias | ✅ | ✅ | ✅ |
| Dashboard | ✅ | ✅ | ❌ |
| Logística | ✅ | ✅ | ❌ |

---

### FASE 10 — Pruebas unitarias (RNF-14: cobertura ≥ 60%)

#### Servicios a probar (mínimo 3 casos por servicio):

| Servicio | Casos de prueba | Estado |
|---|---|---|
| CompraServiceImpl | crear orden PENDIENTE, cancelar no-PENDIENTE, recepcionar actualiza CPP, recepcionar parcial = RECIBIDA_CON_FALTANTES | ✅ Completo |
| VentaServiceImpl | crear con stock OK, rechazar sin stock, anular devuelve stock, validación precio | ⬜ Pendiente |
| TransferenciaServiceImpl | crear, aprobar, despachar descuenta origen, recepcionar parcial = RECIBIDA_CON_FALTANTES | ⬜ Pendiente |
| InventarioServiceImpl | registrarIngreso actualiza stock+CPP, registrarRetiro falla sin stock, genera alerta | ⬜ Pendiente |
| ProductoServiceImpl | crear producto, SKU duplicado lanza excepción, desactivar con stock lanza excepción | ⬜ Pendiente |
| DashboardServiceImpl | ventasDelDia, ventasMensuales 4 meses, productosBajoStock | ⬜ Pendiente |
| AuthServiceImpl | login exitoso, login usuario inactivo, login password incorrecta | ⬜ Pendiente |

**Meta:** ≥ 60% cobertura en capa service (RNF-14)

---

### FASE 11 — Documentación técnica (RNF-15)

#### 11.1 Swagger/OpenAPI
- [ ] Verificar que `OpenApiConfig.java` tiene info completa
- [ ] Agregar `@Tag` en cada controller
- [ ] Agregar `@Operation` + `@Parameter` en cada endpoint
- [ ] Agregar `@ApiResponse` con los posibles códigos HTTP
- [ ] Meta: ≥ 90% endpoints documentados

#### 11.2 README.md
Secciones obligatorias:
- [ ] Descripción del sistema y contexto
- [ ] Stack tecnológico con justificación
- [ ] Requisitos previos (Docker, Java 21)
- [ ] Instrucciones de instalación (`docker compose up`)
- [ ] Variables de entorno y configuración
- [ ] Arquitectura en capas (diagrama textual)
- [ ] Módulos implementados con RF/RNF asociados
- [ ] Decisiones de diseño (patrones usados + justificación)
- [ ] Endpoints API (resumen con link a Swagger)
- [ ] Credenciales de prueba (admin, gerente, operador)
- [ ] Declaración de uso de IA

#### 11.3 Comentarios en código crítico
- [ ] `InventarioServiceImpl.registrarIngreso` — fórmula CPP
- [ ] `VentaServiceImpl.crear` — flujo de validación de stock
- [ ] `TransferenciaServiceImpl.despachar` — lógica de faltantes
- [ ] `JwtUtil` — configuración de signing key

---

### FASE 12 — Infraestructura Docker y migraciones (RT-03, RNF-17)

#### 12.1 Migraciones Flyway faltantes
- [ ] Verificar V3 (falta en el proyecto, V4 necesita V3 primero)
- [ ] Crear V6 con datos seed completos (admin, gerente, operador + sucursales + productos de prueba)

#### 12.2 Docker Compose
- [ ] Verificar/crear `docker-compose.yml` con:
  - Servicio PostgreSQL con health check
  - Servicio backend con variables de entorno
  - Servicio frontend (cuando esté listo)
  - Red compartida entre servicios
  - Volumen persistente para la DB
- [ ] Verificar `application-docker.yml` tiene la configuración correcta
- [ ] Probar que `docker compose up` levanta todo sin configuración manual

#### 12.3 Archivo `.env.example`
- [ ] Documentar todas las variables de entorno requeridas
- [ ] `JWT_SECRET`, `SPRING_DATASOURCE_*`, `SERVER_PORT`, etc.

---

## Buenas prácticas y patrones de diseño aplicados

### Patrones de diseño
| Patrón | Dónde se aplica | Justificación |
|---|---|---|
| **Repository** | Todos los `*Repository.java` | Abstracción de la capa de datos, Spring Data JPA |
| **Service Layer** | Todos los `*ServiceImpl.java` | Centraliza lógica de negocio, separación de responsabilidades |
| **DTO** | `*Request/*.java` y `*Response/*.java` | Desacopla la API del modelo de dominio interno |
| **Static Factory Method** | `Inventario.nuevo(producto, sucursal)` | Creación segura con invariantes garantizadas |
| **Template Method** | `VentaServiceImpl.crear()`, `CompraServiceImpl.recepcionar()` | Algoritmos descompuestos en pasos nombrados |
| **Utility Class** | `MonedaUtils` | Centraliza cálculos monetarios (BigDecimal, HALF_UP) |
| **Guard Clause** | Inicio de cada método de servicio | Validaciones tempranas, reduce anidamiento |
| **Builder (via records)** | Todos los DTOs como `record` | Inmutabilidad, legibilidad |
| **Chain of Responsibility** | `JwtAuthFilter` → `SecurityFilterChain` | Procesamiento de requests en cadena |

### Clean Code
- Métodos ≤ 20 líneas (Template Method para métodos complejos)
- Nombres descriptivos: `validarStockParaTodasLasLineas`, `calcularCostoPromedioPonderado`
- Sin números mágicos: constantes nombradas (`ESTADO_ACTIVA = "ACTIVA"`)
- SRP: cada clase tiene una responsabilidad (separar servicio de notificaciones si crece)
- DRY: `MonedaUtils` elimina repetición de código BigDecimal
- Sin comentarios obvios: solo comentarios de WHY (fórmulas, restricciones no evidentes)

---

## Métricas de calidad a cumplir

| Métrica | Meta | Cómo cumplir |
|---|---|---|
| Cobertura de pruebas unitarias | ≥ 60% | Fase 10: 7 servicios × 3-4 casos = 25+ tests |
| Exactitud CPP | 100% | Test en CompraServiceTest + fórmula en InventarioServiceImpl |
| Validación de stock en ventas | 100% | Test en VentaServiceImpl |
| Endpoints documentados en Swagger | ≥ 90% | Fase 11.1: @Operation en todos los endpoints |
| Clases/métodos con comentarios | ≥ 80% | Comentarios en puntos críticos (Fase 11.3) |
| Tasa de error HTTP | < 1% | GlobalExceptionHandler mapea correctamente |
| Trazabilidad inmutable | 100% | MovimientoInventario nunca se elimina (RT-05) |

---

## Checklist final antes de entrega

- [ ] `docker compose up` levanta backend + DB sin errores
- [ ] Login con usuarios seed (admin/gerente/operador) retorna JWT válido
- [ ] Flujo de venta completo: crear venta → descuenta stock → historial registrado
- [ ] Flujo de compra completo: crear orden → recepcionar → stock actualizado + CPP recalculado
- [ ] Flujo de transferencia completo: crear → aprobar → despachar → recepcionar → faltante definido
- [ ] Dashboard retorna KPIs correctos
- [ ] Alerta generada automáticamente al caer bajo stock mínimo
- [ ] Swagger accesible en `/swagger-ui.html` con ≥ 90% endpoints
- [ ] README completo con todas las secciones obligatorias
- [ ] Tests pasan sin errores (mvn test o gradle test)
- [ ] Cobertura ≥ 60% verificable con JaCoCo

---

## Orden de implementación recomendado

```
FASE 1 → FASE 2 → FASE 3 → FASE 4 → FASE 5 → FASE 6 → FASE 7 → FASE 8 → FASE 9 → FASE 10 → FASE 11 → FASE 12
```

**Tiempo estimado:**
- Fases 1-4: ~3-4 horas (infraestructura + servicios base)
- Fases 5-8: ~4-5 horas (lógica de negocio compleja)
- Fases 9-10: ~3-4 horas (controllers + tests)
- Fases 11-12: ~2-3 horas (docs + docker)
