-- ===================================================
-- V7: SEED DATOS DEMO COMPLETOS
-- Cubre todos los estados de cada entidad para preview
-- ===================================================

-- ----------------------------------------------------------------
-- 1. SUCURSALES
-- ----------------------------------------------------------------
INSERT INTO sucursales (nombre, direccion, telefono, activo)
SELECT 'Sucursal Norte', 'Av. Norte 456, Bogotá', '6012345678', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sucursales WHERE nombre = 'Sucursal Norte');

INSERT INTO sucursales (nombre, direccion, telefono, activo)
SELECT 'Sucursal Sur', 'Calle 80 Sur #23-45, Bogotá', '6019876543', TRUE
WHERE NOT EXISTS (SELECT 1 FROM sucursales WHERE nombre = 'Sucursal Sur');

-- ----------------------------------------------------------------
-- 2. UNIDADES DE MEDIDA
-- ----------------------------------------------------------------
INSERT INTO unidades_medida (nombre, simbolo)
SELECT 'Litro', 'L' WHERE NOT EXISTS (SELECT 1 FROM unidades_medida WHERE simbolo = 'L');

INSERT INTO unidades_medida (nombre, simbolo)
SELECT 'Kilogramo', 'kg' WHERE NOT EXISTS (SELECT 1 FROM unidades_medida WHERE simbolo = 'kg');

INSERT INTO unidades_medida (nombre, simbolo)
SELECT 'Docena', 'doc' WHERE NOT EXISTS (SELECT 1 FROM unidades_medida WHERE simbolo = 'doc');

-- ----------------------------------------------------------------
-- 3. USUARIOS adicionales para Sucursal Norte
-- ----------------------------------------------------------------
INSERT INTO usuarios (nombre, apellido, email, password_hash, activo, id_rol, id_sucursal)
SELECT 'Gerente', 'Norte', 'gerente.norte@aptiplant.local',
       '$2a$10$uk5Whrzog8dqw5LqCkwWqOOV1pEkSWSSWZpz0PbnuCoHVZC2p2mx2',
       TRUE, r.id, s.id
FROM roles r JOIN sucursales s ON s.nombre = 'Sucursal Norte'
WHERE r.nombre = 'GERENTE'
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'gerente.norte@aptiplant.local');

INSERT INTO usuarios (nombre, apellido, email, password_hash, activo, id_rol, id_sucursal)
SELECT 'Operador', 'Norte', 'operador.norte@aptiplant.local',
       '$2a$10$LNI8Z7I3A58jmU1pOw.H/uAK9Yp.W5hbY2TxaJqs0neasTb74TcSG',
       TRUE, r.id, s.id
FROM roles r JOIN sucursales s ON s.nombre = 'Sucursal Norte'
WHERE r.nombre = 'OPERADOR'
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'operador.norte@aptiplant.local');

-- ----------------------------------------------------------------
-- 4. PROVEEDORES
-- ----------------------------------------------------------------
INSERT INTO proveedores (nombre, contacto, telefono, email, condiciones_pago, activo)
SELECT 'PlantaVerde S.A.', 'Juan Ramírez', '3101234567', 'ventas@plantaverde.co', '30 días', TRUE
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE nombre = 'PlantaVerde S.A.');

INSERT INTO proveedores (nombre, contacto, telefono, email, condiciones_pago, activo)
SELECT 'AgroSuministros Ltda', 'María González', '3209876543', 'contacto@agrosuministros.co', 'Contado', TRUE
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE nombre = 'AgroSuministros Ltda');

-- ----------------------------------------------------------------
-- 5. LISTAS DE PRECIOS
-- ----------------------------------------------------------------
INSERT INTO listas_precios (nombre, descripcion, activo)
SELECT 'Precio Mayorista', 'Lista para ventas al por mayor', TRUE
WHERE NOT EXISTS (SELECT 1 FROM listas_precios WHERE nombre = 'Precio Mayorista');

-- ----------------------------------------------------------------
-- 6. PRODUCTOS
-- ----------------------------------------------------------------
INSERT INTO productos (sku, nombre, descripcion, activo) VALUES
  ('PLT-ORQUI-001', 'Orquídea Phalaenopsis',  'Orquídea de interior, floración prolongada', TRUE),
  ('PLT-CACT-001',  'Cactus Saguaro Mini',     'Cactus de interior fácil cuidado',           TRUE),
  ('PLT-SUCU-001',  'Suculenta Echeveria',     'Suculenta roseta colorida',                  TRUE),
  ('INS-TIJE-001',  'Tijeras de Poda',         'Acero inoxidable, mango ergonómico',         TRUE),
  ('INS-REGA-001',  'Regadera 2L',             'Regadera plástica con pico fino',            TRUE),
  ('INS-PALA-001',  'Pala de Jardín',          'Pala pequeña para macetas',                  TRUE),
  ('SUS-TIER-001',  'Tierra Negra x 5kg',      'Sustrato orgánico enriquecido',              TRUE),
  ('SUS-FERT-001',  'Fertilizante NPK 1L',     'Fertilizante líquido concentrado',           TRUE)
ON CONFLICT (sku) DO NOTHING;

-- ----------------------------------------------------------------
-- 7. PRODUCTO_UNIDADES
-- ----------------------------------------------------------------
INSERT INTO producto_unidades (id_producto, id_unidad, es_principal, factor_conversion)
SELECT p.id, u.id, TRUE, 1
FROM productos p JOIN unidades_medida u ON u.simbolo = 'und'
WHERE p.sku IN ('PLT-ORQUI-001','PLT-CACT-001','PLT-SUCU-001','INS-TIJE-001','INS-REGA-001','INS-PALA-001')
  AND NOT EXISTS (SELECT 1 FROM producto_unidades pu WHERE pu.id_producto = p.id AND pu.id_unidad = u.id);

INSERT INTO producto_unidades (id_producto, id_unidad, es_principal, factor_conversion)
SELECT p.id, u.id, TRUE, 1
FROM productos p JOIN unidades_medida u ON u.simbolo = 'kg'
WHERE p.sku = 'SUS-TIER-001'
  AND NOT EXISTS (SELECT 1 FROM producto_unidades pu WHERE pu.id_producto = p.id AND pu.id_unidad = u.id);

INSERT INTO producto_unidades (id_producto, id_unidad, es_principal, factor_conversion)
SELECT p.id, u.id, TRUE, 1
FROM productos p JOIN unidades_medida u ON u.simbolo = 'L'
WHERE p.sku = 'SUS-FERT-001'
  AND NOT EXISTS (SELECT 1 FROM producto_unidades pu WHERE pu.id_producto = p.id AND pu.id_unidad = u.id);

INSERT INTO producto_unidades (id_producto, id_unidad, es_principal, factor_conversion)
SELECT p.id, u.id, FALSE, 12
FROM productos p JOIN unidades_medida u ON u.simbolo = 'cja'
WHERE p.sku IN ('PLT-CACT-001','PLT-SUCU-001')
  AND NOT EXISTS (SELECT 1 FROM producto_unidades pu WHERE pu.id_producto = p.id AND pu.id_unidad = u.id);

-- ----------------------------------------------------------------
-- 8. PRECIOS_PRODUCTO (Detal y Mayorista)
-- ----------------------------------------------------------------
INSERT INTO precios_producto (id_lista, id_producto, precio)
SELECT lp.id, p.id, v.precio
FROM listas_precios lp
CROSS JOIN (VALUES
  ('PLT-ORQUI-001', 85000.0000),
  ('PLT-CACT-001',  25000.0000),
  ('PLT-SUCU-001',  18000.0000),
  ('INS-TIJE-001',  32000.0000),
  ('INS-REGA-001',  22000.0000),
  ('INS-PALA-001',  15000.0000),
  ('SUS-TIER-001',  12000.0000),
  ('SUS-FERT-001',  28000.0000)
) AS v(sku, precio)
JOIN productos p ON p.sku = v.sku
WHERE lp.nombre = 'Precio Detal'
  AND NOT EXISTS (SELECT 1 FROM precios_producto pp WHERE pp.id_lista = lp.id AND pp.id_producto = p.id);

INSERT INTO precios_producto (id_lista, id_producto, precio)
SELECT lp.id, p.id, v.precio
FROM listas_precios lp
CROSS JOIN (VALUES
  ('PLT-ORQUI-001', 68000.0000),
  ('PLT-CACT-001',  18000.0000),
  ('PLT-SUCU-001',  13000.0000),
  ('INS-TIJE-001',  24000.0000),
  ('INS-REGA-001',  16000.0000),
  ('INS-PALA-001',  10000.0000),
  ('SUS-TIER-001',   9000.0000),
  ('SUS-FERT-001',  20000.0000)
) AS v(sku, precio)
JOIN productos p ON p.sku = v.sku
WHERE lp.nombre = 'Precio Mayorista'
  AND NOT EXISTS (SELECT 1 FROM precios_producto pp WHERE pp.id_lista = lp.id AND pp.id_producto = p.id);

-- ----------------------------------------------------------------
-- 9. INVENTARIO
-- ----------------------------------------------------------------
-- Casa Central: algunos bajo mínimo, uno sobre máximo
INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo, costo_promedio_ponderado, fecha_ultima_actualizacion)
SELECT p.id, s.id, v.stock_actual, v.stock_minimo, v.stock_maximo, v.costo, NOW()
FROM sucursales s
CROSS JOIN (VALUES
  ('PLT-ORQUI-001', 45.0000,  5.0000, 100.0000, 62000.00),
  ('PLT-CACT-001',   3.0000, 10.0000, 200.0000, 14000.00),
  ('PLT-SUCU-001',  80.0000,  5.0000,  60.0000,  9000.00),
  ('INS-TIJE-001',  20.0000,  5.0000,  50.0000, 18000.00),
  ('INS-REGA-001',   2.0000,  5.0000,  40.0000, 11000.00),
  ('INS-PALA-001',  15.0000,  5.0000,  30.0000,  8000.00),
  ('SUS-TIER-001',  60.0000, 10.0000, 150.0000,  6500.00),
  ('SUS-FERT-001',  25.0000,  5.0000,  80.0000, 15000.00)
) AS v(sku, stock_actual, stock_minimo, stock_maximo, costo)
JOIN productos p ON p.sku = v.sku
WHERE s.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM inventario i WHERE i.id_producto = p.id AND i.id_sucursal = s.id);

-- Sucursal Norte: algunos bajo mínimo
INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo, costo_promedio_ponderado, fecha_ultima_actualizacion)
SELECT p.id, s.id, v.stock_actual, v.stock_minimo, v.stock_maximo, v.costo, NOW()
FROM sucursales s
CROSS JOIN (VALUES
  ('PLT-ORQUI-001', 10.0000,  5.0000,  80.0000, 62000.00),
  ('PLT-CACT-001',  30.0000,  5.0000, 100.0000, 14000.00),
  ('PLT-SUCU-001',  12.0000,  5.0000,  80.0000,  9000.00),
  ('INS-TIJE-001',   8.0000,  5.0000,  30.0000, 18000.00),
  ('INS-REGA-001',  15.0000,  3.0000,  25.0000, 11000.00),
  ('SUS-TIER-001',   4.0000, 10.0000,  80.0000,  6500.00),
  ('SUS-FERT-001',  18.0000,  5.0000,  50.0000, 15000.00)
) AS v(sku, stock_actual, stock_minimo, stock_maximo, costo)
JOIN productos p ON p.sku = v.sku
WHERE s.nombre = 'Sucursal Norte'
  AND NOT EXISTS (SELECT 1 FROM inventario i WHERE i.id_producto = p.id AND i.id_sucursal = s.id);

-- ----------------------------------------------------------------
-- 10. MOVIMIENTOS DE INVENTARIO (todos los TipoMovimiento)
-- ----------------------------------------------------------------
-- COMPRA
INSERT INTO movimientos_inventario (id_inventario, id_usuario, tipo, cantidad, motivo, referencia_documento, fecha, stock_antes, stock_despues)
SELECT i.id, u.id, 'COMPRA', 50, 'Compra inicial de stock', 'OC-2026-001',
       NOW() - INTERVAL '30 days', 0, 50
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'PLT-ORQUI-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'operador@aptiplant.local'
WHERE NOT EXISTS (
  SELECT 1 FROM movimientos_inventario m
  WHERE m.id_inventario = i.id AND m.tipo = 'COMPRA' AND m.referencia_documento = 'OC-2026-001'
);

-- VENTA
INSERT INTO movimientos_inventario (id_inventario, id_usuario, tipo, cantidad, motivo, referencia_documento, fecha, stock_antes, stock_despues)
SELECT i.id, u.id, 'VENTA', 5, 'Venta al cliente', 'VTA-2026-001',
       NOW() - INTERVAL '10 days', 50, 45
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'PLT-ORQUI-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'operador@aptiplant.local'
WHERE NOT EXISTS (
  SELECT 1 FROM movimientos_inventario m
  WHERE m.id_inventario = i.id AND m.tipo = 'VENTA' AND m.referencia_documento = 'VTA-2026-001'
);

-- AJUSTE_POSITIVO
INSERT INTO movimientos_inventario (id_inventario, id_usuario, tipo, cantidad, motivo, referencia_documento, fecha, stock_antes, stock_despues)
SELECT i.id, u.id, 'AJUSTE_POSITIVO', 10, 'Corrección inventario físico', 'AJ-POS-2026-001',
       NOW() - INTERVAL '15 days', 10, 20
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'INS-TIJE-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'admin@aptiplant.local'
WHERE NOT EXISTS (
  SELECT 1 FROM movimientos_inventario m
  WHERE m.id_inventario = i.id AND m.referencia_documento = 'AJ-POS-2026-001'
);

-- AJUSTE_NEGATIVO
INSERT INTO movimientos_inventario (id_inventario, id_usuario, tipo, cantidad, motivo, referencia_documento, fecha, stock_antes, stock_despues)
SELECT i.id, u.id, 'AJUSTE_NEGATIVO', 5, 'Diferencia en conteo físico', 'AJ-NEG-2026-001',
       NOW() - INTERVAL '12 days', 25, 20
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'INS-TIJE-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'admin@aptiplant.local'
WHERE NOT EXISTS (
  SELECT 1 FROM movimientos_inventario m
  WHERE m.id_inventario = i.id AND m.referencia_documento = 'AJ-NEG-2026-001'
);

-- TRANSFERENCIA_SALIDA
INSERT INTO movimientos_inventario (id_inventario, id_usuario, tipo, cantidad, motivo, referencia_documento, fecha, stock_antes, stock_despues)
SELECT i.id, u.id, 'TRANSFERENCIA_SALIDA', 15, 'Transferencia a Sucursal Norte', 'TRF-2026-001',
       NOW() - INTERVAL '7 days', 18, 3
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'PLT-CACT-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'gerente@aptiplant.local'
WHERE NOT EXISTS (
  SELECT 1 FROM movimientos_inventario m
  WHERE m.id_inventario = i.id AND m.referencia_documento = 'TRF-2026-001' AND m.tipo = 'TRANSFERENCIA_SALIDA'
);

-- TRANSFERENCIA_ENTRADA
INSERT INTO movimientos_inventario (id_inventario, id_usuario, tipo, cantidad, motivo, referencia_documento, fecha, stock_antes, stock_despues)
SELECT i.id, u.id, 'TRANSFERENCIA_ENTRADA', 15, 'Recepción desde Casa Central', 'TRF-2026-001',
       NOW() - INTERVAL '6 days', 15, 30
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'PLT-CACT-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Sucursal Norte'
JOIN usuarios u ON u.email = 'gerente.norte@aptiplant.local'
WHERE NOT EXISTS (
  SELECT 1 FROM movimientos_inventario m
  WHERE m.id_inventario = i.id AND m.referencia_documento = 'TRF-2026-001' AND m.tipo = 'TRANSFERENCIA_ENTRADA'
);

-- DEVOLUCION
INSERT INTO movimientos_inventario (id_inventario, id_usuario, tipo, cantidad, motivo, referencia_documento, fecha, stock_antes, stock_despues)
SELECT i.id, u.id, 'DEVOLUCION', 2, 'Cliente devuelve producto en buen estado', 'DEV-2026-001',
       NOW() - INTERVAL '5 days', 43, 45
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'PLT-ORQUI-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'operador@aptiplant.local'
WHERE NOT EXISTS (
  SELECT 1 FROM movimientos_inventario m
  WHERE m.id_inventario = i.id AND m.referencia_documento = 'DEV-2026-001'
);

-- MERMA
INSERT INTO movimientos_inventario (id_inventario, id_usuario, tipo, cantidad, motivo, referencia_documento, fecha, stock_antes, stock_despues)
SELECT i.id, u.id, 'MERMA', 3, 'Plantas dañadas por helada', 'MRM-2026-001',
       NOW() - INTERVAL '3 days', 6, 3
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'PLT-CACT-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'gerente@aptiplant.local'
WHERE NOT EXISTS (
  SELECT 1 FROM movimientos_inventario m
  WHERE m.id_inventario = i.id AND m.referencia_documento = 'MRM-2026-001'
);

-- ----------------------------------------------------------------
-- 11. ORDENES DE COMPRA (todos los estados)
-- ----------------------------------------------------------------
-- PENDIENTE
INSERT INTO ordenes_compra (id_proveedor, id_sucursal, id_usuario_crea, fecha_creacion, fecha_estimada_entrega, estado, total, plazo_pago_dias)
SELECT pv.id, s.id, u.id,
       NOW() - INTERVAL '2 days',
       (NOW() + INTERVAL '5 days')::date,
       'PENDIENTE', 450000.00, 30
FROM proveedores pv
JOIN sucursales s ON s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'gerente@aptiplant.local'
WHERE pv.nombre = 'PlantaVerde S.A.'
  AND NOT EXISTS (
    SELECT 1 FROM ordenes_compra oc
    JOIN proveedores pv2 ON pv2.id = oc.id_proveedor AND pv2.nombre = 'PlantaVerde S.A.'
    WHERE oc.estado = 'PENDIENTE'
  );

-- RECIBIDA
INSERT INTO ordenes_compra (id_proveedor, id_sucursal, id_usuario_crea, fecha_creacion, fecha_estimada_entrega, fecha_recepcion, estado, total, plazo_pago_dias)
SELECT pv.id, s.id, u.id,
       NOW() - INTERVAL '20 days',
       (NOW() - INTERVAL '12 days')::date,
       NOW() - INTERVAL '10 days',
       'RECIBIDA', 320000.00, 0
FROM proveedores pv
JOIN sucursales s ON s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'operador@aptiplant.local'
WHERE pv.nombre = 'AgroSuministros Ltda'
  AND NOT EXISTS (
    SELECT 1 FROM ordenes_compra oc
    JOIN proveedores pv2 ON pv2.id = oc.id_proveedor AND pv2.nombre = 'AgroSuministros Ltda'
    WHERE oc.estado = 'RECIBIDA'
  );

-- RECIBIDA_CON_FALTANTES
INSERT INTO ordenes_compra (id_proveedor, id_sucursal, id_usuario_crea, fecha_creacion, fecha_estimada_entrega, fecha_recepcion, estado, total, plazo_pago_dias)
SELECT pv.id, s.id, u.id,
       NOW() - INTERVAL '35 days',
       (NOW() - INTERVAL '25 days')::date,
       NOW() - INTERVAL '22 days',
       'RECIBIDA_CON_FALTANTES', 180000.00, 15
FROM proveedores pv
JOIN sucursales s ON s.nombre = 'Sucursal Norte'
JOIN usuarios u ON u.email = 'gerente.norte@aptiplant.local'
WHERE pv.nombre = 'PlantaVerde S.A.'
  AND NOT EXISTS (
    SELECT 1 FROM ordenes_compra oc
    JOIN proveedores pv2 ON pv2.id = oc.id_proveedor AND pv2.nombre = 'PlantaVerde S.A.'
    WHERE oc.estado = 'RECIBIDA_CON_FALTANTES'
  );

-- CANCELADA
INSERT INTO ordenes_compra (id_proveedor, id_sucursal, id_usuario_crea, fecha_creacion, estado, total, plazo_pago_dias)
SELECT pv.id, s.id, u.id,
       NOW() - INTERVAL '45 days',
       'CANCELADA', 95000.00, 0
FROM proveedores pv
JOIN sucursales s ON s.nombre = 'Casa Central'
JOIN usuarios u ON u.email = 'admin@aptiplant.local'
WHERE pv.nombre = 'Proveedor Generico'
  AND NOT EXISTS (
    SELECT 1 FROM ordenes_compra oc
    JOIN proveedores pv2 ON pv2.id = oc.id_proveedor AND pv2.nombre = 'Proveedor Generico'
    WHERE oc.estado = 'CANCELADA'
  );

-- ----------------------------------------------------------------
-- 12. DETALLE ORDENES DE COMPRA
-- ----------------------------------------------------------------
-- OC PENDIENTE
INSERT INTO detalle_orden_compra (id_orden, id_producto, cantidad_pedida, cantidad_recibida, precio_unitario, descuento, subtotal)
SELECT oc.id, p.id, 5.0000, 0.0000, 62000.0000, 0.00, 310000.00
FROM ordenes_compra oc
JOIN proveedores pv ON pv.id = oc.id_proveedor AND pv.nombre = 'PlantaVerde S.A.'
JOIN productos p ON p.sku = 'PLT-ORQUI-001'
WHERE oc.estado = 'PENDIENTE'
  AND NOT EXISTS (SELECT 1 FROM detalle_orden_compra d WHERE d.id_orden = oc.id AND d.id_producto = p.id);

INSERT INTO detalle_orden_compra (id_orden, id_producto, cantidad_pedida, cantidad_recibida, precio_unitario, descuento, subtotal)
SELECT oc.id, p.id, 10.0000, 0.0000, 14000.0000, 0.00, 140000.00
FROM ordenes_compra oc
JOIN proveedores pv ON pv.id = oc.id_proveedor AND pv.nombre = 'PlantaVerde S.A.'
JOIN productos p ON p.sku = 'PLT-CACT-001'
WHERE oc.estado = 'PENDIENTE'
  AND NOT EXISTS (SELECT 1 FROM detalle_orden_compra d WHERE d.id_orden = oc.id AND d.id_producto = p.id);

-- OC RECIBIDA
INSERT INTO detalle_orden_compra (id_orden, id_producto, cantidad_pedida, cantidad_recibida, precio_unitario, descuento, subtotal)
SELECT oc.id, p.id, 20.0000, 20.0000, 6500.0000, 0.00, 130000.00
FROM ordenes_compra oc
JOIN proveedores pv ON pv.id = oc.id_proveedor AND pv.nombre = 'AgroSuministros Ltda'
JOIN productos p ON p.sku = 'SUS-TIER-001'
WHERE oc.estado = 'RECIBIDA'
  AND NOT EXISTS (SELECT 1 FROM detalle_orden_compra d WHERE d.id_orden = oc.id AND d.id_producto = p.id);

INSERT INTO detalle_orden_compra (id_orden, id_producto, cantidad_pedida, cantidad_recibida, precio_unitario, descuento, subtotal)
SELECT oc.id, p.id, 8.0000, 8.0000, 15000.0000, 5.00, 114000.00
FROM ordenes_compra oc
JOIN proveedores pv ON pv.id = oc.id_proveedor AND pv.nombre = 'AgroSuministros Ltda'
JOIN productos p ON p.sku = 'SUS-FERT-001'
WHERE oc.estado = 'RECIBIDA'
  AND NOT EXISTS (SELECT 1 FROM detalle_orden_compra d WHERE d.id_orden = oc.id AND d.id_producto = p.id);

-- OC RECIBIDA_CON_FALTANTES
INSERT INTO detalle_orden_compra (id_orden, id_producto, cantidad_pedida, cantidad_recibida, precio_unitario, descuento, subtotal)
SELECT oc.id, p.id, 10.0000, 7.0000, 9000.0000, 0.00, 63000.00
FROM ordenes_compra oc
JOIN proveedores pv ON pv.id = oc.id_proveedor AND pv.nombre = 'PlantaVerde S.A.'
JOIN productos p ON p.sku = 'PLT-SUCU-001'
WHERE oc.estado = 'RECIBIDA_CON_FALTANTES'
  AND NOT EXISTS (SELECT 1 FROM detalle_orden_compra d WHERE d.id_orden = oc.id AND d.id_producto = p.id);

INSERT INTO detalle_orden_compra (id_orden, id_producto, cantidad_pedida, cantidad_recibida, precio_unitario, descuento, subtotal)
SELECT oc.id, p.id, 15.0000, 12.0000, 7800.0000, 0.00, 93600.00
FROM ordenes_compra oc
JOIN proveedores pv ON pv.id = oc.id_proveedor AND pv.nombre = 'PlantaVerde S.A.'
JOIN productos p ON p.sku = 'PLT-CACT-001'
WHERE oc.estado = 'RECIBIDA_CON_FALTANTES'
  AND NOT EXISTS (SELECT 1 FROM detalle_orden_compra d WHERE d.id_orden = oc.id AND d.id_producto = p.id);

-- OC CANCELADA
INSERT INTO detalle_orden_compra (id_orden, id_producto, cantidad_pedida, cantidad_recibida, precio_unitario, descuento, subtotal)
SELECT oc.id, p.id, 10.0000, 0.0000, 9500.0000, 0.00, 95000.00
FROM ordenes_compra oc
JOIN proveedores pv ON pv.id = oc.id_proveedor AND pv.nombre = 'Proveedor Generico'
JOIN productos p ON p.sku = 'SKU-DEMO-001'
WHERE oc.estado = 'CANCELADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_orden_compra d WHERE d.id_orden = oc.id AND d.id_producto = p.id);

-- ----------------------------------------------------------------
-- 13. VENTAS (todos los estados + distribución mensual para dashboard)
-- ----------------------------------------------------------------
-- CONFIRMADA hoy
INSERT INTO ventas (id_sucursal, id_usuario, id_lista_precios, fecha, subtotal, descuento_global, total, estado)
SELECT s.id, u.id, lp.id, NOW(), 102000.00, 0.00, 102000.00, 'CONFIRMADA'
FROM sucursales s
JOIN usuarios u ON u.email = 'operador@aptiplant.local'
JOIN listas_precios lp ON lp.nombre = 'Precio Detal'
WHERE s.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM ventas v WHERE v.total = 102000.00 AND v.estado = 'CONFIRMADA');

-- CONFIRMADA hace 5 días (con descuento global)
INSERT INTO ventas (id_sucursal, id_usuario, id_lista_precios, fecha, subtotal, descuento_global, total, estado)
SELECT s.id, u.id, lp.id, NOW() - INTERVAL '5 days', 170000.00, 5.00, 161500.00, 'CONFIRMADA'
FROM sucursales s
JOIN usuarios u ON u.email = 'operador@aptiplant.local'
JOIN listas_precios lp ON lp.nombre = 'Precio Detal'
WHERE s.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM ventas v WHERE v.total = 161500.00 AND v.estado = 'CONFIRMADA');

-- CONFIRMADA hace 15 días - Sucursal Norte - lista mayorista
INSERT INTO ventas (id_sucursal, id_usuario, id_lista_precios, fecha, subtotal, descuento_global, total, estado)
SELECT s.id, u.id, lp.id, NOW() - INTERVAL '15 days', 218000.00, 0.00, 218000.00, 'CONFIRMADA'
FROM sucursales s
JOIN usuarios u ON u.email = 'operador.norte@aptiplant.local'
JOIN listas_precios lp ON lp.nombre = 'Precio Mayorista'
WHERE s.nombre = 'Sucursal Norte'
  AND NOT EXISTS (SELECT 1 FROM ventas v WHERE v.total = 218000.00 AND v.estado = 'CONFIRMADA');

-- CONFIRMADA hace 2 meses (para gráfico de dashboard)
INSERT INTO ventas (id_sucursal, id_usuario, id_lista_precios, fecha, subtotal, descuento_global, total, estado)
SELECT s.id, u.id, lp.id, NOW() - INTERVAL '2 months', 95000.00, 0.00, 95000.00, 'CONFIRMADA'
FROM sucursales s
JOIN usuarios u ON u.email = 'operador@aptiplant.local'
JOIN listas_precios lp ON lp.nombre = 'Precio Detal'
WHERE s.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM ventas v WHERE v.total = 95000.00 AND v.estado = 'CONFIRMADA');

-- CONFIRMADA hace 3 meses (para gráfico de dashboard)
INSERT INTO ventas (id_sucursal, id_usuario, id_lista_precios, fecha, subtotal, descuento_global, total, estado)
SELECT s.id, u.id, lp.id, NOW() - INTERVAL '3 months', 140000.00, 0.00, 140000.00, 'CONFIRMADA'
FROM sucursales s
JOIN usuarios u ON u.email = 'operador@aptiplant.local'
JOIN listas_precios lp ON lp.nombre = 'Precio Detal'
WHERE s.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM ventas v WHERE v.total = 140000.00 AND v.estado = 'CONFIRMADA');

-- ANULADA (con motivo)
INSERT INTO ventas (id_sucursal, id_usuario, id_lista_precios, fecha, subtotal, descuento_global, total, estado, motivo_anulacion)
SELECT s.id, u.id, lp.id, NOW() - INTERVAL '8 days', 85000.00, 0.00, 85000.00, 'ANULADA', 'Cliente canceló el pedido antes del despacho'
FROM sucursales s
JOIN usuarios u ON u.email = 'operador@aptiplant.local'
JOIN listas_precios lp ON lp.nombre = 'Precio Detal'
WHERE s.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM ventas v WHERE v.estado = 'ANULADA' AND v.total = 85000.00);

-- ----------------------------------------------------------------
-- 14. DETALLE VENTAS
-- ----------------------------------------------------------------
-- Venta de hoy (102000)
INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 1.0000, 85000.0000, 0.00, 85000.00
FROM ventas v JOIN productos p ON p.sku = 'PLT-ORQUI-001'
WHERE v.total = 102000.00 AND v.estado = 'CONFIRMADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 1.0000, 17000.0000, 0.00, 17000.00
FROM ventas v JOIN productos p ON p.sku = 'INS-REGA-001'
WHERE v.total = 102000.00 AND v.estado = 'CONFIRMADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

-- Venta con descuento (161500)
INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 2.0000, 85000.0000, 0.00, 170000.00
FROM ventas v JOIN productos p ON p.sku = 'PLT-ORQUI-001'
WHERE v.total = 161500.00 AND v.estado = 'CONFIRMADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

-- Venta mayorista (218000)
INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 3.0000, 68000.0000, 0.00, 204000.00
FROM ventas v JOIN productos p ON p.sku = 'PLT-ORQUI-001'
WHERE v.total = 218000.00 AND v.estado = 'CONFIRMADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 1.0000, 14000.0000, 0.00, 14000.00
FROM ventas v JOIN productos p ON p.sku = 'PLT-CACT-001'
WHERE v.total = 218000.00 AND v.estado = 'CONFIRMADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

-- Venta hace 2 meses (95000)
INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 5.0000, 12000.0000, 0.00, 60000.00
FROM ventas v JOIN productos p ON p.sku = 'SUS-TIER-001'
WHERE v.total = 95000.00 AND v.estado = 'CONFIRMADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 2.0000, 17500.0000, 0.00, 35000.00
FROM ventas v JOIN productos p ON p.sku = 'SUS-FERT-001'
WHERE v.total = 95000.00 AND v.estado = 'CONFIRMADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

-- Venta hace 3 meses (140000)
INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 5.0000, 25000.0000, 0.00, 125000.00
FROM ventas v JOIN productos p ON p.sku = 'PLT-CACT-001'
WHERE v.total = 140000.00 AND v.estado = 'CONFIRMADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 1.0000, 15000.0000, 0.00, 15000.00
FROM ventas v JOIN productos p ON p.sku = 'INS-PALA-001'
WHERE v.total = 140000.00 AND v.estado = 'CONFIRMADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

-- Venta ANULADA (85000)
INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario, descuento_linea, subtotal)
SELECT v.id, p.id, 1.0000, 85000.0000, 0.00, 85000.00
FROM ventas v JOIN productos p ON p.sku = 'PLT-ORQUI-001'
WHERE v.total = 85000.00 AND v.estado = 'ANULADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_ventas dv WHERE dv.id_venta = v.id AND dv.id_producto = p.id);

-- ----------------------------------------------------------------
-- 15. TRANSFERENCIAS (todos los estados)
-- ----------------------------------------------------------------
-- PENDIENTE_APROBACION (urgencia ALTA)
INSERT INTO transferencias (id_sucursal_origen, id_sucursal_destino, id_usuario_solicita, estado, urgencia, fecha_solicitud, observaciones)
SELECT so.id, sd.id, u.id, 'PENDIENTE_APROBACION', 'ALTA',
       NOW() - INTERVAL '1 day', 'Urgente para reponer stock bajo mínimo en sucursal destino'
FROM sucursales so JOIN sucursales sd ON sd.nombre = 'Sucursal Norte'
JOIN usuarios u ON u.email = 'operador.norte@aptiplant.local'
WHERE so.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM transferencias t WHERE t.estado = 'PENDIENTE_APROBACION');

-- EN_PREPARACION
INSERT INTO transferencias (id_sucursal_origen, id_sucursal_destino, id_usuario_solicita, id_usuario_aprueba, estado, urgencia, fecha_solicitud, observaciones)
SELECT so.id, sd.id, us.id, ua.id, 'EN_PREPARACION', 'NORMAL',
       NOW() - INTERVAL '3 days', 'Reposición mensual programada'
FROM sucursales so JOIN sucursales sd ON sd.nombre = 'Sucursal Norte'
JOIN usuarios us ON us.email = 'operador@aptiplant.local'
JOIN usuarios ua ON ua.email = 'gerente@aptiplant.local'
WHERE so.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM transferencias t WHERE t.estado = 'EN_PREPARACION');

-- EN_TRANSITO
INSERT INTO transferencias (id_sucursal_origen, id_sucursal_destino, id_usuario_solicita, id_usuario_aprueba, estado, urgencia, transportista, fecha_solicitud, fecha_despacho, fecha_estimada_llegada)
SELECT so.id, sd.id, us.id, ua.id, 'EN_TRANSITO', 'NORMAL', 'Transportadora Rápido SAS',
       NOW() - INTERVAL '5 days',
       NOW() - INTERVAL '2 days',
       (NOW() + INTERVAL '1 day')::date
FROM sucursales so JOIN sucursales sd ON sd.nombre = 'Sucursal Norte'
JOIN usuarios us ON us.email = 'operador@aptiplant.local'
JOIN usuarios ua ON ua.email = 'gerente@aptiplant.local'
WHERE so.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM transferencias t WHERE t.estado = 'EN_TRANSITO');

-- RECIBIDA (completa)
INSERT INTO transferencias (id_sucursal_origen, id_sucursal_destino, id_usuario_solicita, id_usuario_aprueba, estado, urgencia, transportista, fecha_solicitud, fecha_despacho, fecha_estimada_llegada, fecha_recepcion)
SELECT so.id, sd.id, us.id, ua.id, 'RECIBIDA', 'NORMAL', 'Mensajería Express',
       NOW() - INTERVAL '20 days',
       NOW() - INTERVAL '15 days',
       (NOW() - INTERVAL '12 days')::date,
       NOW() - INTERVAL '13 days'
FROM sucursales so JOIN sucursales sd ON sd.nombre = 'Sucursal Norte'
JOIN usuarios us ON us.email = 'operador@aptiplant.local'
JOIN usuarios ua ON ua.email = 'gerente@aptiplant.local'
WHERE so.nombre = 'Casa Central'
  AND NOT EXISTS (SELECT 1 FROM transferencias t WHERE t.estado = 'RECIBIDA');

-- RECIBIDA_CON_FALTANTES
INSERT INTO transferencias (id_sucursal_origen, id_sucursal_destino, id_usuario_solicita, id_usuario_aprueba, estado, urgencia, transportista, fecha_solicitud, fecha_despacho, fecha_estimada_llegada, fecha_recepcion)
SELECT so.id, sd.id, us.id, ua.id, 'RECIBIDA_CON_FALTANTES', 'ALTA', 'Mensajería Express',
       NOW() - INTERVAL '40 days',
       NOW() - INTERVAL '35 days',
       (NOW() - INTERVAL '32 days')::date,
       NOW() - INTERVAL '33 days'
FROM sucursales so JOIN sucursales sd ON sd.nombre = 'Casa Central'
JOIN usuarios us ON us.email = 'gerente.norte@aptiplant.local'
JOIN usuarios ua ON ua.email = 'admin@aptiplant.local'
WHERE so.nombre = 'Sucursal Norte'
  AND NOT EXISTS (SELECT 1 FROM transferencias t WHERE t.estado = 'RECIBIDA_CON_FALTANTES');

-- RECHAZADA
INSERT INTO transferencias (id_sucursal_origen, id_sucursal_destino, id_usuario_solicita, id_usuario_aprueba, estado, urgencia, fecha_solicitud, motivo_rechazo)
SELECT so.id, sd.id, us.id, ua.id, 'RECHAZADA', 'NORMAL',
       NOW() - INTERVAL '60 days',
       'Stock insuficiente en sucursal origen al momento de la aprobación'
FROM sucursales so JOIN sucursales sd ON sd.nombre = 'Casa Central'
JOIN usuarios us ON us.email = 'operador.norte@aptiplant.local'
JOIN usuarios ua ON ua.email = 'admin@aptiplant.local'
WHERE so.nombre = 'Sucursal Norte'
  AND NOT EXISTS (SELECT 1 FROM transferencias t WHERE t.estado = 'RECHAZADA');

-- ----------------------------------------------------------------
-- 16. DETALLE TRANSFERENCIAS (con todos los TratamientoFaltante)
-- ----------------------------------------------------------------
-- PENDIENTE_APROBACION (sin despacho ni recepción)
INSERT INTO detalle_transferencias (id_transferencia, id_producto, cantidad_solicitada, cantidad_despachada, cantidad_recibida, faltante)
SELECT t.id, p.id, 10.0000, NULL, NULL, 0.0000
FROM transferencias t JOIN productos p ON p.sku = 'PLT-CACT-001'
WHERE t.estado = 'PENDIENTE_APROBACION'
  AND NOT EXISTS (SELECT 1 FROM detalle_transferencias dt WHERE dt.id_transferencia = t.id AND dt.id_producto = p.id);

INSERT INTO detalle_transferencias (id_transferencia, id_producto, cantidad_solicitada, cantidad_despachada, cantidad_recibida, faltante)
SELECT t.id, p.id, 5.0000, NULL, NULL, 0.0000
FROM transferencias t JOIN productos p ON p.sku = 'SUS-TIER-001'
WHERE t.estado = 'PENDIENTE_APROBACION'
  AND NOT EXISTS (SELECT 1 FROM detalle_transferencias dt WHERE dt.id_transferencia = t.id AND dt.id_producto = p.id);

-- EN_PREPARACION (aprobada, aún sin despacho)
INSERT INTO detalle_transferencias (id_transferencia, id_producto, cantidad_solicitada, cantidad_despachada, cantidad_recibida, faltante)
SELECT t.id, p.id, 15.0000, NULL, NULL, 0.0000
FROM transferencias t JOIN productos p ON p.sku = 'PLT-SUCU-001'
WHERE t.estado = 'EN_PREPARACION'
  AND NOT EXISTS (SELECT 1 FROM detalle_transferencias dt WHERE dt.id_transferencia = t.id AND dt.id_producto = p.id);

-- EN_TRANSITO (despachada, pendiente recepción)
INSERT INTO detalle_transferencias (id_transferencia, id_producto, cantidad_solicitada, cantidad_despachada, cantidad_recibida, faltante)
SELECT t.id, p.id, 8.0000, 8.0000, NULL, 0.0000
FROM transferencias t JOIN productos p ON p.sku = 'SUS-TIER-001'
WHERE t.estado = 'EN_TRANSITO'
  AND NOT EXISTS (SELECT 1 FROM detalle_transferencias dt WHERE dt.id_transferencia = t.id AND dt.id_producto = p.id);

-- RECIBIDA (completa, sin faltante)
INSERT INTO detalle_transferencias (id_transferencia, id_producto, cantidad_solicitada, cantidad_despachada, cantidad_recibida, faltante)
SELECT t.id, p.id, 20.0000, 20.0000, 20.0000, 0.0000
FROM transferencias t JOIN productos p ON p.sku = 'INS-TIJE-001'
WHERE t.estado = 'RECIBIDA'
  AND NOT EXISTS (SELECT 1 FROM detalle_transferencias dt WHERE dt.id_transferencia = t.id AND dt.id_producto = p.id);

-- RECIBIDA_CON_FALTANTES - REENVIO
INSERT INTO detalle_transferencias (id_transferencia, id_producto, cantidad_solicitada, cantidad_despachada, cantidad_recibida, faltante, tratamiento_faltante)
SELECT t.id, p.id, 12.0000, 12.0000, 10.0000, 2.0000, 'REENVIO'
FROM transferencias t JOIN productos p ON p.sku = 'PLT-ORQUI-001'
WHERE t.estado = 'RECIBIDA_CON_FALTANTES'
  AND NOT EXISTS (SELECT 1 FROM detalle_transferencias dt WHERE dt.id_transferencia = t.id AND dt.id_producto = p.id);

-- RECIBIDA_CON_FALTANTES - AJUSTE_ACEPTADO
INSERT INTO detalle_transferencias (id_transferencia, id_producto, cantidad_solicitada, cantidad_despachada, cantidad_recibida, faltante, tratamiento_faltante)
SELECT t.id, p.id, 8.0000, 8.0000, 6.0000, 2.0000, 'AJUSTE_ACEPTADO'
FROM transferencias t JOIN productos p ON p.sku = 'PLT-SUCU-001'
WHERE t.estado = 'RECIBIDA_CON_FALTANTES'
  AND NOT EXISTS (SELECT 1 FROM detalle_transferencias dt WHERE dt.id_transferencia = t.id AND dt.id_producto = p.id);

-- RECHAZADA (nunca se despachó)
INSERT INTO detalle_transferencias (id_transferencia, id_producto, cantidad_solicitada, cantidad_despachada, cantidad_recibida, faltante)
SELECT t.id, p.id, 5.0000, NULL, NULL, 0.0000
FROM transferencias t JOIN productos p ON p.sku = 'SUS-FERT-001'
WHERE t.estado = 'RECHAZADA'
  AND NOT EXISTS (SELECT 1 FROM detalle_transferencias dt WHERE dt.id_transferencia = t.id AND dt.id_producto = p.id);

-- ----------------------------------------------------------------
-- 17. ALERTAS DE STOCK (STOCK_MINIMO y STOCK_MAXIMO, ACTIVA y RESUELTA)
-- ----------------------------------------------------------------
-- STOCK_MINIMO ACTIVA - PLT-CACT-001 Casa Central (3 < min 10)
INSERT INTO alertas_stock (id_inventario, tipo_alerta, valor_umbral, stock_al_momento, fecha_generacion, estado)
SELECT i.id, 'STOCK_MINIMO', 10.0000, 3.0000, NOW() - INTERVAL '3 days', 'ACTIVA'
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'PLT-CACT-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
WHERE NOT EXISTS (
  SELECT 1 FROM alertas_stock a WHERE a.id_inventario = i.id AND a.tipo_alerta = 'STOCK_MINIMO' AND a.estado = 'ACTIVA'
);

-- STOCK_MINIMO ACTIVA - INS-REGA-001 Casa Central (2 < min 5)
INSERT INTO alertas_stock (id_inventario, tipo_alerta, valor_umbral, stock_al_momento, fecha_generacion, estado)
SELECT i.id, 'STOCK_MINIMO', 5.0000, 2.0000, NOW() - INTERVAL '1 day', 'ACTIVA'
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'INS-REGA-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
WHERE NOT EXISTS (
  SELECT 1 FROM alertas_stock a WHERE a.id_inventario = i.id AND a.tipo_alerta = 'STOCK_MINIMO' AND a.estado = 'ACTIVA'
);

-- STOCK_MINIMO ACTIVA - SUS-TIER-001 Sucursal Norte (4 < min 10)
INSERT INTO alertas_stock (id_inventario, tipo_alerta, valor_umbral, stock_al_momento, fecha_generacion, estado)
SELECT i.id, 'STOCK_MINIMO', 10.0000, 4.0000, NOW() - INTERVAL '2 days', 'ACTIVA'
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'SUS-TIER-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Sucursal Norte'
WHERE NOT EXISTS (
  SELECT 1 FROM alertas_stock a WHERE a.id_inventario = i.id AND a.tipo_alerta = 'STOCK_MINIMO' AND a.estado = 'ACTIVA'
);

-- STOCK_MAXIMO ACTIVA - PLT-SUCU-001 Casa Central (80 > max 60)
INSERT INTO alertas_stock (id_inventario, tipo_alerta, valor_umbral, stock_al_momento, fecha_generacion, estado)
SELECT i.id, 'STOCK_MAXIMO', 60.0000, 80.0000, NOW() - INTERVAL '5 days', 'ACTIVA'
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'PLT-SUCU-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
WHERE NOT EXISTS (
  SELECT 1 FROM alertas_stock a WHERE a.id_inventario = i.id AND a.tipo_alerta = 'STOCK_MAXIMO' AND a.estado = 'ACTIVA'
);

-- STOCK_MINIMO RESUELTA (histórica - PLT-ORQUI-001 Casa Central)
INSERT INTO alertas_stock (id_inventario, tipo_alerta, valor_umbral, stock_al_momento, fecha_generacion, estado, fecha_resolucion)
SELECT i.id, 'STOCK_MINIMO', 5.0000, 2.0000, NOW() - INTERVAL '25 days', 'RESUELTA', NOW() - INTERVAL '20 days'
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'PLT-ORQUI-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
WHERE NOT EXISTS (
  SELECT 1 FROM alertas_stock a WHERE a.id_inventario = i.id AND a.tipo_alerta = 'STOCK_MINIMO' AND a.estado = 'RESUELTA'
);

-- STOCK_MAXIMO RESUELTA (histórica - SUS-FERT-001 Casa Central)
INSERT INTO alertas_stock (id_inventario, tipo_alerta, valor_umbral, stock_al_momento, fecha_generacion, estado, fecha_resolucion)
SELECT i.id, 'STOCK_MAXIMO', 80.0000, 95.0000, NOW() - INTERVAL '50 days', 'RESUELTA', NOW() - INTERVAL '45 days'
FROM inventario i
JOIN productos p ON p.id = i.id_producto AND p.sku = 'SUS-FERT-001'
JOIN sucursales s ON s.id = i.id_sucursal AND s.nombre = 'Casa Central'
WHERE NOT EXISTS (
  SELECT 1 FROM alertas_stock a WHERE a.id_inventario = i.id AND a.tipo_alerta = 'STOCK_MAXIMO' AND a.estado = 'RESUELTA'
);
