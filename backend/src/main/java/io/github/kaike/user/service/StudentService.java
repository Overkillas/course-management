package io.github.kaike.user.service;

import io.github.kaike.shared.exceptions.ConflictException;
import io.github.kaike.shared.exceptions.ResourceNotFoundException;
import io.github.kaike.user.domain.User;
import io.github.kaike.user.domain.UserRole;
import io.github.kaike.user.dtos.CreateStudentRequest;
import io.github.kaike.user.dtos.StudentResponse;
import io.github.kaike.user.mapper.UserMapper;
import io.github.kaike.user.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Regra de negócio do aluno: cadastrar, listar e excluir. O cadastro define o aluno como
 * papel ALUNO, marca a senha para troca no primeiro acesso e nunca persiste senha em texto
 * puro (apenas o hash).
 */
@ApplicationScoped
public class StudentService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Inject
    public StudentService(UserRepository userRepository, UserMapper mapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<StudentResponse> listAll() {
        return mapper.toStudentResponseList(userRepository.listByRole(UserRole.ALUNO));
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request) {
        // 409 com mensagem clara (e não resposta genérica) é aceitável aqui porque o endpoint
        // é administrativo: o admin já enxerga todos os alunos pelo GET /students, então avisar
        // "e-mail já cadastrado" não revela nada novo (não há risco de enumeration). A resposta
        // genérica anti-enumeration fica para os fluxos públicos (login, recuperação de senha).
        if (userRepository.emailExists(request.email())) {
            throw new ConflictException("E-mail já cadastrado");
        }

        User student = new User();
        student.setName(request.name());
        student.setEmail(request.email());
        student.setPasswordHash(passwordEncoder.hash(request.password()));
        student.setRole(UserRole.ALUNO);
        student.setMustChangePassword(true);

        userRepository.persist(student);
        return mapper.toStudentResponse(student);
    }

    @Transactional
    public void delete(Integer id) {
        boolean deleted = userRepository.deleteById(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Aluno " + id + " não encontrado");
        }
    }
}
