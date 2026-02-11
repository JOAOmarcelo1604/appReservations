-- Adiciona a coluna para o link do calendário do Vrbo
ALTER TABLE public.units ADD COLUMN vrbo_url VARCHAR(500);

-- (Opcional) Se quiser garantir que não duplique, mas como é URL, deixe nullable