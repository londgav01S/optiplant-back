INSERT INTO sucursales (nombre, direccion, telefono, activo)
SELECT 'Casa Central', 'Direccion pendiente', '0000000', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM sucursales s WHERE s.nombre = 'Casa Central'
);

INSERT INTO usuarios (nombre, apellido, email, password_hash, activo, id_rol, id_sucursal)
SELECT
    'Admin',
    'Sistema',
    'admin@aptiplant.local',
    '$2a$10$bKeNOhKoXQpnA3EdzuOC.eWyukKGz5jWuop0VJuzRdtRZRcJo74PC',
    TRUE,
    r.id,
    NULL
FROM roles r
WHERE r.nombre = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM usuarios u WHERE u.email = 'admin@aptiplant.local'
  );

INSERT INTO usuarios (nombre, apellido, email, password_hash, activo, id_rol, id_sucursal)
SELECT
    'Gerente',
    'Sucursal',
    'gerente@aptiplant.local',
    '$2a$10$uk5Whrzog8dqw5LqCkwWqOOV1pEkSWSSWZpz0PbnuCoHVZC2p2mx2',
    TRUE,
    r.id,
    s.id
FROM roles r
JOIN sucursales s ON s.nombre = 'Casa Central'
WHERE r.nombre = 'GERENTE'
  AND NOT EXISTS (
      SELECT 1 FROM usuarios u WHERE u.email = 'gerente@aptiplant.local'
  );

INSERT INTO usuarios (nombre, apellido, email, password_hash, activo, id_rol, id_sucursal)
SELECT
    'Operador',
    'Sucursal',
    'operador@aptiplant.local',
    '$2a$10$LNI8Z7I3A58jmU1pOw.H/uAK9Yp.W5hbY2TxaJqs0neasTb74TcSG',
    TRUE,
    r.id,
    s.id
FROM roles r
JOIN sucursales s ON s.nombre = 'Casa Central'
WHERE r.nombre = 'OPERADOR'
  AND NOT EXISTS (
      SELECT 1 FROM usuarios u WHERE u.email = 'operador@aptiplant.local'
  );