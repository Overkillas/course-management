package io.github.kaike.course.repository;

import io.github.kaike.course.domain.Course;

/**
 * Projeção de leitura: um curso (com o centro já materializado) e a quantidade de alunos
 * matriculados nele. Dá nome e tipo ao resultado da consulta agregada, em vez de devolver um
 * Object[] cru para quem chama.
 */
public record CourseWithStudentCount(Course course, long studentCount) {
}
