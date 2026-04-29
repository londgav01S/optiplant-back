-- V10__agregar_campos_cliente_venta.sql
-- Agregar campos de cliente a la tabla ventas
ALTER TABLE ventas
ADD COLUMN cliente_nombre VARCHAR(255) NOT NULL DEFAULT 'Consumidor Final';

ALTER TABLE ventas
ADD COLUMN cliente_documento VARCHAR(50);

-- Remover el default después de aplicar
ALTER TABLE ventas
ALTER COLUMN cliente_nombre DROP DEFAULT;
