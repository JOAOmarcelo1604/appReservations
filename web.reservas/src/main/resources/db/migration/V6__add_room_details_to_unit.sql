-- V3__Add_Room_Details_To_Units.sql

ALTER TABLE units
ADD COLUMN bedrooms INT DEFAULT 1,    -- Quantidade de Quartos (Padrão 1 para não quebrar)
ADD COLUMN bathrooms INT DEFAULT 1,   -- Quantidade de Banheiros
ADD COLUMN beds INT DEFAULT 1;        -- Quantidade de Camas (Total)

-- (Opcional) Se quiser, pode rodar um update para corrigir os nulos ou defaults
-- UPDATE units SET bedrooms = 1 WHERE bedrooms IS NULL;