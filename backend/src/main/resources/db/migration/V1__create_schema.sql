-- Schema inicial do desafio (gerenciamento de cursos e alunos).
-- Fonte de verdade do schema; ver decisoes em backend/docs/db_diagram.md.
-- Ordem de criacao respeita as dependencias de FK: centers/users -> courses -> enrollments.

CREATE TABLE centers (
    id         INT          NOT NULL AUTO_INCREMENT,
    code       VARCHAR(10)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_centers PRIMARY KEY (id),
    CONSTRAINT uq_centers_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE users (
    id                   INT          NOT NULL AUTO_INCREMENT,
    name                 VARCHAR(100) NOT NULL,
    email                VARCHAR(255) NOT NULL,           -- credencial de login
    password_hash        VARCHAR(255) NOT NULL,           -- hash; nunca senha em texto puro
    role                 VARCHAR(20)  NOT NULL DEFAULT 'aluno', -- enum no codigo, persistido como string
    must_change_password BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE courses (
    id              INT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    center_id       INT          NOT NULL,
    total_semesters INT          NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_courses PRIMARY KEY (id),
    CONSTRAINT chk_courses_total_semesters CHECK (total_semesters > 0),
    -- Nao apaga centro com cursos vinculados.
    CONSTRAINT fk_courses_center FOREIGN KEY (center_id) REFERENCES centers (id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Tabela de juncao "livre": registra apenas o vinculo aluno <-> curso.
CREATE TABLE enrollments (
    id         INT       NOT NULL AUTO_INCREMENT,
    user_id    INT       NOT NULL,
    course_id  INT       NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_enrollments PRIMARY KEY (id),
    -- Regra central: um aluno nao se matricula duas vezes no mesmo curso.
    CONSTRAINT uq_enrollment_user_course UNIQUE (user_id, course_id),
    -- Excluir aluno ou curso remove as matriculas associadas.
    CONSTRAINT fk_enrollment_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Atende "listar alunos matriculados por curso" (WHERE course_id = ?).
CREATE INDEX idx_enrollment_course ON enrollments (course_id);
