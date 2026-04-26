INSERT INTO unidades_medida (nombre, simbolo)
SELECT 'Unidad', 'und'
WHERE NOT EXISTS (
    SELECT 1 FROM unidades_medida um WHERE um.nombre = 'Unidad' AND um.simbolo = 'und'
);

INSERT INTO unidades_medida (nombre, simbolo)
SELECT 'Caja x 12', 'cja'
WHERE NOT EXISTS (
    SELECT 1 FROM unidades_medida um WHERE um.nombre = 'Caja x 12' AND um.simbolo = 'cja'
);

INSERT INTO listas_precios (nombre, descripcion, activo)
SELECT 'Precio Detal', 'Lista base para ventas al detal', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM listas_precios lp WHERE lp.nombre = 'Precio Detal'
);

INSERT INTO proveedores (nombre, contacto, telefono, email, condiciones_pago, activo)
SELECT 'Proveedor Generico', 'N/A', '0000000', 'proveedor@local.test', 'Contado', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM proveedores p WHERE p.nombre = 'Proveedor Generico'
);

INSERT INTO productos (sku, nombre, descripcion, activo)
VALUES ('SKU-DEMO-001', 'Producto Demo', 'Producto semilla para pruebas iniciales', TRUE)
ON CONFLICT (sku) DO NOTHING;

INSERT INTO producto_unidades (id_producto, id_unidad, es_principal, factor_conversion)
SELECT p.id, u.id, TRUE, 1
FROM productos p
JOIN unidades_medida u ON u.nombre = 'Unidad' AND u.simbolo = 'und'
WHERE p.sku = 'SKU-DEMO-001'
  AND NOT EXISTS (
      SELECT 1
      FROM producto_unidades pu
      WHERE pu.id_producto = p.id AND pu.id_unidad = u.id
  );

INSERT INTO precios_producto (id_lista, id_producto, precio)
SELECT lp.id, p.id, 10.0000
FROM listas_precios lp
JOIN productos p ON p.sku = 'SKU-DEMO-001'
WHERE lp.nombre = 'Precio Detal'
  AND NOT EXISTS (
      SELECT 1
      FROM precios_producto pp
      WHERE pp.id_lista = lp.id AND pp.id_producto = p.id
  );

