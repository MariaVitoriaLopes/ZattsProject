
package br.edu.fatec.zattsproject.backend.service;

import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;

    public List<Usuario> listar() {
        return repository.findAll();
    }

    public Usuario buscarPorId(String id) {
        return repository.findById(id).orElse(null);
    }

    public ResultadoUsuario atualizarDados(
            String id,
            String nome,
            String email,
            String telefone) {

        Usuario usuario = buscarPorId(id);

        if (usuario == null) {
            return ResultadoUsuario.NAO_ENCONTRADO;
        }

        if (nome == null || nome.isBlank()
                || email == null || email.isBlank()) {
            return ResultadoUsuario.CAMPOS_OBRIGATORIOS;
        }

        email = email.trim().toLowerCase();

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResultadoUsuario.EMAIL_INVALIDO;
        }

        if (repository.existsByEmailAndIdNot(email, id)) {
            return ResultadoUsuario.EMAIL_JA_CADASTRADO;
        }

        usuario.setNome(nome.trim());
        usuario.setEmail(email);
        usuario.setTelefone(telefone == null ? "" : telefone.trim());

        repository.save(usuario);

        return ResultadoUsuario.SUCESSO;
    }

    public ResultadoUsuario atualizarSenha(
            String id,
            String senha,
            String confirmarSenha) {

        Usuario usuario = buscarPorId(id);

        if (usuario == null) {
            return ResultadoUsuario.NAO_ENCONTRADO;
        }

        if (senha == null || senha.isBlank()
                || confirmarSenha == null || confirmarSenha.isBlank()) {
            return ResultadoUsuario.CAMPOS_OBRIGATORIOS;
        }

        if (!senha.equals(confirmarSenha)) {
            return ResultadoUsuario.SENHAS_DIFERENTES;
        }

        if (!senha.matches("^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {
            return ResultadoUsuario.SENHA_INVALIDA;
        }

        usuario.setSenha(passwordEncoder.encode(senha));
        repository.save(usuario);

        return ResultadoUsuario.SUCESSO;
    }

    public ResultadoUsuario excluir(String id) {

        Usuario usuario = buscarPorId(id);

        if (usuario == null) {
            return ResultadoUsuario.NAO_ENCONTRADO;
        }

        Query query = new Query(
                Criteria.where("usuarioId").is(id)
        );

        mongoTemplate.remove(query, "configuracoes");
        mongoTemplate.remove(query, "alertas");
        mongoTemplate.remove(query, "ContaEnergia");
        mongoTemplate.remove(query, "Questionario");
        mongoTemplate.remove(query, "sessoes");

        repository.delete(usuario);

        return ResultadoUsuario.SUCESSO;
    }

    public enum ResultadoUsuario {
        SUCESSO,
        CAMPOS_OBRIGATORIOS,
        EMAIL_INVALIDO,
        EMAIL_JA_CADASTRADO,
        SENHAS_DIFERENTES,
        SENHA_INVALIDA,
        NAO_ENCONTRADO
    }
}
