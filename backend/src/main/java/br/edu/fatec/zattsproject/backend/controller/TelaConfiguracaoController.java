package br.edu.fatec.zattsproject.backend.controller;


import br.edu.fatec.zattsproject.backend.model.Usuario;
import br.edu.fatec.zattsproject.backend.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class TelaConfiguracaoController {


    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;



    @PostMapping("/configuracoes/editar")
    public String editarDados(
            @RequestParam String nome,
            @RequestParam String telefone,
            @RequestParam String email,
            HttpSession session,
            RedirectAttributes atributos
    ){


        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");


        if(usuario == null){
            return "redirect:/login";
        }



        usuario.setNome(nome);
        usuario.setTelefone(telefone);
        usuario.setEmail(email);


        repository.save(usuario);


        session.setAttribute(
                "usuarioLogado",
                usuario
        );


        atributos.addFlashAttribute(
                "sucesso",
                "Dados atualizados com sucesso!"
        );


        return "redirect:/configuracoes";
    }





    @PostMapping("/configuracoes/senha")
    public String alterarSenha(
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            HttpSession session,
            RedirectAttributes atributos
    ){


        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");


        if(usuario == null){
            return "redirect:/login";
        }



        if(!senha.equals(confirmarSenha)){


            atributos.addFlashAttribute(
                    "erro",
                    "As senhas não coincidem."
            );


            return "redirect:/configuracoes";
        }



        usuario.setSenha(
                passwordEncoder.encode(senha)
        );


        repository.save(usuario);



        session.setAttribute(
                "usuarioLogado",
                usuario
        );



        atributos.addFlashAttribute(
                "sucesso",
                "Senha alterada com sucesso!"
        );


        return "redirect:/configuracoes";
    }







    @GetMapping("/logout")
    public String sair(
            HttpSession session
    ){

        session.invalidate();

        return "redirect:/login";
    }






    @PostMapping("/configuracoes/excluir")
    public String excluirConta(
            HttpSession session
    ){

        Usuario usuario =
                (Usuario) session.getAttribute("usuarioLogado");


        if(usuario != null){

            repository.deleteById(
                    usuario.getId()
            );
        }


        session.invalidate();


        return "redirect:/login";
    }

}