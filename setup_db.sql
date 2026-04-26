-- Script SQL para crear la base de datos y usuario de inventario
-- Ejecutar como superusuario postgres

-- 1. Crear el usuario 'inventario_user'
CREATE USER inventario_user WITH PASSWORD 'inventario_pass';

-- 2. Crear la base de datos 'inventario_db' propiedad del usuario
CREATE DATABASE inventario_db OWNER inventario_user;

-- 3. Conectar a la base de datos y configurar permisos
\c inventario_db

-- 4. Dar todos los permisos sobre la base de datos
GRANT ALL PRIVILEGES ON DATABASE inventario_db TO inventario_user;

-- 5. Dar permisos sobre el esquema public
GRANT ALL PRIVILEGES ON SCHEMA public TO inventario_user;

-- 6. Configurar por defecto los permisos para nuevas tablas
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO inventario_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO inventario_user;

-- 7. Verificar que todo está correcto
SELECT usename FROM pg_user WHERE usename = 'inventario_user';
SELECT datname FROM pg_database WHERE datname = 'inventario_db';

-- 8. Fin del script
-- Ahora puedes conectar como: psql -U inventario_user -d inventario_db -h localhost

