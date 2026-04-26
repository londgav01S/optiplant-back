INSERT INTO roles (nombre, descripcion)
VALUES
    ('ADMIN', 'Acceso total al sistema'),
    ('GERENTE', 'Gestion operativa por sucursal y aprobacion de transferencias'),
    ('OPERADOR', 'Operacion diaria de ventas, compras y solicitudes de transferencia')
ON CONFLICT (nombre) DO NOTHING;

