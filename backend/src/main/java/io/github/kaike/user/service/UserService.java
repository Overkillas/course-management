package io.github.kaike.user.service;

import io.github.kaike.shared.exceptions.ConflictException;
import io.github.kaike.shared.exceptions.ResourceNotFoundException;
import io.github.kaike.user.domain.User;
import io.github.kaike.user.domain.UserRole;
import io.github.kaike.user.dtos.ChangePasswordRequest;
import io.github.kaike.user.dtos.CreateStudentRequest;
import io.github.kaike.user.dtos.StudentResponse;
import io.github.kaike.user.dtos.UserResponse;
import io.github.kaike.user.mapper.UserMapper;
import io.github.kaike.user.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Regra de negócio em torno do usuário (um service por agregado). Reúne a gestão de alunos
 * pelo admin (cadastrar, listar e excluir, sempre no subconjunto ALUNO) e o self-service do
 * próprio usuário autenticado (consultar o perfil e trocar a própria senha).
 * Quem pode chamar cada operação é decidido nos resources, não aqui (StudentResource é
 * admin-only, MeResource é qualquer autenticado).
 */
@ApplicationScoped
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Inject
    public UserService(UserRepository userRepository, UserMapper mapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<StudentResponse> listStudents() {
        return mapper.toStudentResponseList(userRepository.listByRole(UserRole.ALUNO));
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
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

        // O pré-check acima resolve o duplicado comum; este try/catch cobre a corrida: dois
        // cadastros simultâneos com o mesmo e-mail passam o emailExists() ao mesmo tempo e só a
        // UNIQUE do banco barra o segundo. persistAndFlush força o INSERT agora (não no commit),
        // para a violação estourar aqui e virar 409 em vez de 500.
        try {
            userRepository.persistAndFlush(student);
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new ConflictException("E-mail já cadastrado");
            }
            throw e;
        }
        return mapper.toStudentResponse(student);
    }

    @Transactional
    public void deleteStudent(Integer id) {
        // Só exclui se for de fato um aluno: o endpoint /students não deve apagar um admin por
        // id. Um usuário inexistente ou de outro papel é tratado como "aluno não encontrado".
        User student = userRepository.findByIdOptional(id)
            .filter(user -> user.getRole() == UserRole.ALUNO)
            .orElseThrow(() -> new ResourceNotFoundException("Aluno " + id + " não encontrado"));
        userRepository.delete(student);
    }

    public UserResponse getProfile(Integer userId) {
        User user = userRepository.findByIdOptional(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário " + userId + " não encontrado"));
        return mapper.toUserResponse(user);
    }

    /**
     * Troca a senha do próprio usuário e zera a marca de troca obrigatória. A nova senha já chega
     * validada como forte pelo Bean Validation. A entidade é gerenciada, então o UPDATE sai no
     * commit da transação.
     */
    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepository.findByIdOptional(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário " + userId + " não encontrado"));

        user.setPasswordHash(passwordEncoder.hash(request.newPassword()));
        user.setMustChangePassword(false);
    }
}
