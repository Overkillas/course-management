-- Limita total_semesters a no maximo 100, espelhando a constraint @Max(100) da aplicacao.
-- Recria o CHECK existente (que so garantia > 0) acrescentando o teto superior.
ALTER TABLE courses DROP CHECK chk_courses_total_semesters;
ALTER TABLE courses ADD CONSTRAINT chk_courses_total_semesters CHECK (total_semesters > 0 AND total_semesters <= 100);
