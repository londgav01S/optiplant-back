-- Crea registros de inventario (stock=0) para cada combinación producto-sucursal
-- que aún no exista. Garantiza que todo producto activo aparezca en el inventario
-- de cada sucursal activa desde el momento en que se aplica esta migración.
INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, costo_promedio_ponderado, fecha_ultima_actualizacion)
SELECT p.id, s.id, 0, 0, 0, NOW()
FROM productos p
CROSS JOIN sucursales s
WHERE p.activo = TRUE
  AND s.activo = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM inventario i
      WHERE i.id_producto = p.id AND i.id_sucursal = s.id
  );
