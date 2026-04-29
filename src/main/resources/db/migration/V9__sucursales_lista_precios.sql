ALTER TABLE sucursales
    ADD COLUMN IF NOT EXISTS id_lista_precios BIGINT;

ALTER TABLE sucursales
    ADD CONSTRAINT fk_sucursales_lista_precios
    FOREIGN KEY (id_lista_precios) REFERENCES listas_precios (id);

UPDATE sucursales
SET id_lista_precios = (
    SELECT id
    FROM listas_precios
    WHERE nombre = 'Precio Detal'
    LIMIT 1
)
WHERE id_lista_precios IS NULL;