CREATE TABLE IF NOT EXISTS productos (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS unidades_medida (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    simbolo VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS producto_unidades (
    id BIGSERIAL PRIMARY KEY,
    id_producto BIGINT NOT NULL,
    id_unidad BIGINT NOT NULL,
    es_principal BOOLEAN NOT NULL DEFAULT FALSE,
    factor_conversion NUMERIC(10,4) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_unidades_producto FOREIGN KEY (id_producto) REFERENCES productos (id),
    CONSTRAINT fk_producto_unidades_unidad FOREIGN KEY (id_unidad) REFERENCES unidades_medida (id),
    CONSTRAINT uk_producto_unidad UNIQUE (id_producto, id_unidad)
);

CREATE TABLE IF NOT EXISTS inventario (
    id BIGSERIAL PRIMARY KEY,
    id_producto BIGINT NOT NULL,
    id_sucursal BIGINT NOT NULL,
    stock_actual NUMERIC(12,4) NOT NULL DEFAULT 0,
    stock_minimo NUMERIC(12,4) NOT NULL DEFAULT 0,
    stock_maximo NUMERIC(12,4),
    costo_promedio_ponderado NUMERIC(14,4) NOT NULL DEFAULT 0,
    fecha_ultima_actualizacion TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventario_producto FOREIGN KEY (id_producto) REFERENCES productos (id),
    CONSTRAINT fk_inventario_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursales (id),
    CONSTRAINT uk_inventario_producto_sucursal UNIQUE (id_producto, id_sucursal),
    CONSTRAINT ck_inventario_stock_actual_non_negative CHECK (stock_actual >= 0)
);

CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id BIGSERIAL PRIMARY KEY,
    id_inventario BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    cantidad NUMERIC(12,4) NOT NULL,
    motivo VARCHAR(300),
    referencia_documento VARCHAR(100),
    fecha TIMESTAMP NOT NULL,
    stock_antes NUMERIC(12,4) NOT NULL,
    stock_despues NUMERIC(12,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movimientos_inventario_inventario FOREIGN KEY (id_inventario) REFERENCES inventario (id),
    CONSTRAINT fk_movimientos_inventario_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS proveedores (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    contacto VARCHAR(150),
    telefono VARCHAR(20),
    email VARCHAR(150),
    condiciones_pago VARCHAR(300),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ordenes_compra (
    id BIGSERIAL PRIMARY KEY,
    id_proveedor BIGINT NOT NULL,
    id_sucursal BIGINT NOT NULL,
    id_usuario_crea BIGINT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_estimada_entrega DATE,
    fecha_recepcion TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    total NUMERIC(14,2) NOT NULL,
    plazo_pago_dias INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ordenes_compra_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedores (id),
    CONSTRAINT fk_ordenes_compra_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursales (id),
    CONSTRAINT fk_ordenes_compra_usuario_crea FOREIGN KEY (id_usuario_crea) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS detalle_orden_compra (
    id BIGSERIAL PRIMARY KEY,
    id_orden BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad_pedida NUMERIC(12,4) NOT NULL,
    cantidad_recibida NUMERIC(12,4) NOT NULL DEFAULT 0,
    precio_unitario NUMERIC(14,4) NOT NULL,
    descuento NUMERIC(5,2) NOT NULL DEFAULT 0,
    subtotal NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_detalle_orden_orden FOREIGN KEY (id_orden) REFERENCES ordenes_compra (id),
    CONSTRAINT fk_detalle_orden_producto FOREIGN KEY (id_producto) REFERENCES productos (id)
);

CREATE TABLE IF NOT EXISTS listas_precios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS precios_producto (
    id BIGSERIAL PRIMARY KEY,
    id_lista BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    precio NUMERIC(14,4) NOT NULL,
    fecha_inicio DATE,
    fecha_fin DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_precios_producto_lista FOREIGN KEY (id_lista) REFERENCES listas_precios (id),
    CONSTRAINT fk_precios_producto_producto FOREIGN KEY (id_producto) REFERENCES productos (id)
);

CREATE TABLE IF NOT EXISTS ventas (
    id BIGSERIAL PRIMARY KEY,
    id_sucursal BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    id_lista_precios BIGINT,
    fecha TIMESTAMP NOT NULL,
    subtotal NUMERIC(14,2) NOT NULL,
    descuento_global NUMERIC(5,2) NOT NULL DEFAULT 0,
    total NUMERIC(14,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA',
    motivo_anulacion VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ventas_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursales (id),
    CONSTRAINT fk_ventas_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id),
    CONSTRAINT fk_ventas_lista_precios FOREIGN KEY (id_lista_precios) REFERENCES listas_precios (id)
);

CREATE TABLE IF NOT EXISTS detalle_ventas (
    id BIGSERIAL PRIMARY KEY,
    id_venta BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad NUMERIC(12,4) NOT NULL,
    precio_unitario NUMERIC(14,4) NOT NULL,
    descuento_linea NUMERIC(5,2) NOT NULL DEFAULT 0,
    subtotal NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_detalle_ventas_venta FOREIGN KEY (id_venta) REFERENCES ventas (id),
    CONSTRAINT fk_detalle_ventas_producto FOREIGN KEY (id_producto) REFERENCES productos (id)
);

CREATE TABLE IF NOT EXISTS transferencias (
    id BIGSERIAL PRIMARY KEY,
    id_sucursal_origen BIGINT NOT NULL,
    id_sucursal_destino BIGINT NOT NULL,
    id_usuario_solicita BIGINT NOT NULL,
    id_usuario_aprueba BIGINT,
    estado VARCHAR(40) NOT NULL DEFAULT 'PENDIENTE_APROBACION',
    urgencia VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    transportista VARCHAR(200),
    fecha_solicitud TIMESTAMP NOT NULL,
    fecha_despacho TIMESTAMP,
    fecha_estimada_llegada DATE,
    fecha_recepcion TIMESTAMP,
    motivo_rechazo VARCHAR(300),
    observaciones VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transferencias_sucursal_origen FOREIGN KEY (id_sucursal_origen) REFERENCES sucursales (id),
    CONSTRAINT fk_transferencias_sucursal_destino FOREIGN KEY (id_sucursal_destino) REFERENCES sucursales (id),
    CONSTRAINT fk_transferencias_usuario_solicita FOREIGN KEY (id_usuario_solicita) REFERENCES usuarios (id),
    CONSTRAINT fk_transferencias_usuario_aprueba FOREIGN KEY (id_usuario_aprueba) REFERENCES usuarios (id),
    CONSTRAINT ck_transferencias_sucursales_distintas CHECK (id_sucursal_origen <> id_sucursal_destino)
);

CREATE TABLE IF NOT EXISTS detalle_transferencias (
    id BIGSERIAL PRIMARY KEY,
    id_transferencia BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad_solicitada NUMERIC(12,4) NOT NULL,
    cantidad_despachada NUMERIC(12,4),
    cantidad_recibida NUMERIC(12,4),
    faltante NUMERIC(12,4) NOT NULL DEFAULT 0,
    tratamiento_faltante VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_detalle_transferencias_transferencia FOREIGN KEY (id_transferencia) REFERENCES transferencias (id),
    CONSTRAINT fk_detalle_transferencias_producto FOREIGN KEY (id_producto) REFERENCES productos (id)
);

CREATE TABLE IF NOT EXISTS alertas_stock (
    id BIGSERIAL PRIMARY KEY,
    id_inventario BIGINT NOT NULL,
    tipo_alerta VARCHAR(20) NOT NULL,
    valor_umbral NUMERIC(12,4) NOT NULL,
    stock_al_momento NUMERIC(12,4) NOT NULL,
    fecha_generacion TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_resolucion TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alertas_stock_inventario FOREIGN KEY (id_inventario) REFERENCES inventario (id)
);

CREATE INDEX IF NOT EXISTS idx_producto_unidades_producto ON producto_unidades (id_producto);
CREATE INDEX IF NOT EXISTS idx_producto_unidades_unidad ON producto_unidades (id_unidad);
CREATE INDEX IF NOT EXISTS idx_inventario_sucursal ON inventario (id_sucursal);
CREATE INDEX IF NOT EXISTS idx_inventario_producto ON inventario (id_producto);
CREATE INDEX IF NOT EXISTS idx_movimientos_inventario ON movimientos_inventario (id_inventario);
CREATE INDEX IF NOT EXISTS idx_movimientos_fecha ON movimientos_inventario (fecha);
CREATE INDEX IF NOT EXISTS idx_ordenes_compra_sucursal ON ordenes_compra (id_sucursal);
CREATE INDEX IF NOT EXISTS idx_ventas_sucursal_fecha ON ventas (id_sucursal, fecha);
CREATE INDEX IF NOT EXISTS idx_transferencias_estado ON transferencias (estado);
CREATE INDEX IF NOT EXISTS idx_alertas_stock_estado ON alertas_stock (estado);

