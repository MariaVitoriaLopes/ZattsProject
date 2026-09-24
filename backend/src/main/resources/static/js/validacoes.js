function mostrarSenha(idCampo, icone){

    const input = document.getElementById(idCampo);


    const campo = icone.parentElement;


    const olhoAberto = campo.querySelector(".visibility");

    const olhoFechado = campo.querySelector(".visibility_off");



    if(input.type === "password"){


        input.type = "text";


        olhoAberto.style.display = "none";

        olhoFechado.style.display = "block";


    } else {


        input.type = "password";


        olhoAberto.style.display = "block";

        olhoFechado.style.display = "none";

    }

}

const email = document.getElementById("email");
const senha = document.getElementById("senha");
const confirmarSenha = document.getElementById("confirmarSenha");

// =====================================================
// Cadastro
// =====================================================

const requisitos = {

    tamanho:
        document.getElementById("req-tamanho"),

    maiuscula:
        document.getElementById("req-maiuscula"),

    minuscula:
        document.getElementById("req-minuscula"),

    numero:
        document.getElementById("req-numero"),

    especial:
        document.getElementById("req-especial"),

    confirmacao:
        document.getElementById("req-confirmacao")
};

function alterarStatus(elemento, valido){

    if(valido){

        elemento.style.color = "#0D734C";

    }else{

        elemento.style.color = "#FF8000FF";

    }

}

function validarSenha(){


    const valor = senha.value;



    alterarStatus(
        requisitos.tamanho,
        valor.length >= 8
    );



    alterarStatus(
        requisitos.maiuscula,
        /[A-Z]/.test(valor)
    );



    alterarStatus(
        requisitos.minuscula,
        /[a-z]/.test(valor)
    );



    alterarStatus(
        requisitos.numero,
        /\d/.test(valor)
    );



    alterarStatus(
        requisitos.especial,
        /[^A-Za-z0-9]/.test(valor)
    );



    alterarStatus(
        requisitos.confirmacao,
        valor === confirmarSenha.value &&
        valor.length > 0
    );

}

senha.addEventListener(
    "input",
    validarSenha
);


confirmarSenha.addEventListener(
    "input",
    validarSenha
);

function validarEmail(valor){


    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor);


}

// Validação envio email
// =====================================================

document.querySelector("form")
    .addEventListener("submit", function(event){



        if(email.value.trim() === ""){

            alert("Digite seu e-mail.");

            event.preventDefault();

            return;

        }



        if(!validarEmail(email.value)){


            alert("Digite um e-mail válido.");

            event.preventDefault();

            return;

        }





        if(senha.value.trim() === ""){


            alert("Digite uma senha.");

            event.preventDefault();

            return;

        }





        const senhaForte =
            senha.value.length >= 8 &&
            /[A-Z]/.test(senha.value) &&
            /[a-z]/.test(senha.value) &&
            /\d/.test(senha.value) &&
            /[^A-Za-z0-9]/.test(senha.value);




        if(!senhaForte){


            alert(
                "A senha não atende aos requisitos."
            );


            event.preventDefault();

            return;

        }





        if(senha.value !== confirmarSenha.value){


            alert(
                "As senhas não coincidem."
            );


            event.preventDefault();


        }


    });

// =====================================================
// configuraçoes
// =====================================================

// Mascara do telefone
// =====================================================

const telefone = document.getElementById("telefone");

if (telefone) {

    telefone.addEventListener("input", function (e) {

        let valor = e.target.value;

        // remove tudo que não é número
        valor = valor.replace(/\D/g, "");


        // limita para 11 números
        valor = valor.substring(0, 11);


        // aplica máscara
        if (valor.length <= 10) {

            valor = valor.replace(
                /^(\d{2})(\d)/,
                "($1) $2"
            );

            valor = valor.replace(
                /(\d{4})(\d)/,
                "$1-$2"
            );

        } else {

            valor = valor.replace(
                /^(\d{2})(\d)/,
                "($1) $2"
            );

            valor = valor.replace(
                /(\d{5})(\d)/,
                "$1-$2"
            );

        }


        e.target.value = valor;

    });

}

// Validar confirmação de nova senha
// =====================================================

const formAlterarSenha = document.getElementById("formAlterarSenha");


if(formAlterarSenha){

    formAlterarSenha.addEventListener("submit", function(event){

        const novaSenha = document.getElementById("novaSenha").value;
        const confirmarNovaSenha = document.getElementById("confirmarNovaSenha").value;


        if(novaSenha !== confirmarNovaSenha){

            event.preventDefault();


            alert("As senhas não coincidem.");

            return;
        }


        if(novaSenha.trim() === ""){

            event.preventDefault();

            alert("Digite uma nova senha.");

        }

    });

}