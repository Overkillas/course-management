-- Centros sao dado de referencia, carregados por seed (nao ha CRUD de centros no escopo).
-- Apenas o codigo (sigla); nome completo e atributos institucionais ficam para evolucao futura.
INSERT INTO centers (code) VALUES
    ('CCT'), -- Centro de Ciencias Tecnologicas
    ('CCS'), -- Centro de Ciencias da Saude
    ('CCJ'), -- Centro de Ciencias Juridicas
    ('CCA'), -- Centro de Ciencias Administrativas
    ('CCH'); -- Centro de Ciencias Humanas
