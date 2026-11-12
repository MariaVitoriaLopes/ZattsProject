
document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("formConfiguracoes");

    const btnEditar = document.getElementById("btnEditarTudo");
    const botoesEdicao = document.getElementById("botoesEdicao");
    const btnCancelar = document.getElementById("btnCancelarTudo");

    const campos = form.querySelectorAll("input, select");

    const cep = document.getElementById("cep");

    let valoresOriginais = {};

    // ==========================================
    // ATIVAR EDIÇÃO
    // ==========================================

    function ativarEdicao() {

        valoresOriginais = {};

        campos.forEach(campo => {

            valoresOriginais[campo.id] = campo.value;

            if (campo.tagName === "SELECT") {
                campo.disabled = false;
            } else {
                campo.readOnly = false;
            }

        });

        btnEditar.hidden = true;
        botoesEdicao.hidden = false;

        document.getElementById("nomeCompleto").focus();
    }

    // ==========================================
    // CANCELAR EDIÇÃO
    // ==========================================

    function cancelarEdicao() {

        campos.forEach(campo => {

            campo.value = valoresOriginais[campo.id] ?? "";

            if (campo.tagName === "SELECT") {
                campo.disabled = true;
            } else {
                campo.readOnly = true;
            }

        });

        btnEditar.hidden = false;
        botoesEdicao.hidden = true;
    }

    if (btnEditar) {
        btnEditar.addEventListener("click", ativarEdicao);
    }

    if (btnCancelar) {
        btnCancelar.addEventListener("click", cancelarEdicao);
    }

    // ==========================================
    // BUSCAR ENDEREÇO PELO CEP
    // ==========================================

    let ultimaConsultaCEP = "";

    async function buscarCEP() {

        const numeroCEP = cep.value.replace(/\D/g, "");

        if (numeroCEP.length !== 8) {
            return;
        }

        if (numeroCEP === ultimaConsultaCEP) {
            return;
        }

        ultimaConsultaCEP = numeroCEP;

        try {

            const resposta = await fetch(
                `https://viacep.com.br/ws/${numeroCEP}/json/`
            );

            if (!resposta.ok) {
                throw new Error("Falha na consulta do CEP.");
            }

            const endereco = await resposta.json();

            if (endereco.erro) {
                alert("CEP não encontrado.");
                ultimaConsultaCEP = "";
                return;
            }

            document.getElementById("logradouro").value =
                endereco.logradouro || "";

            document.getElementById("bairro").value =
                endereco.bairro || "";

            document.getElementById("cidade").value =
                endereco.localidade || "";

            document.getElementById("uf").value =
                endereco.uf || "";

        } catch (erro) {

            console.error("Erro ao consultar CEP:", erro);

            ultimaConsultaCEP = "";

            alert("Não foi possível consultar o CEP. Tente novamente.");

        }

    }

    cep.addEventListener("input", function () {

        let valor = cep.value.replace(/\D/g, "");

        if (valor.length > 5) {
            valor = valor.replace(/^(\d{5})(\d)/, "$1-$2");
        }

        cep.value = valor;

        if (valor.replace(/\D/g, "").length < 8) {
            ultimaConsultaCEP = "";
        }

        if (valor.replace(/\D/g, "").length === 8) {
            buscarCEP();
        }

    });

    // ==========================================
    // SALVAR ALTERAÇÕES
    // ==========================================

    form.addEventListener("submit", function (event) {

        const senha = document.getElementById("senha").value;
        const confirmarSenha =
            document.getElementById("confirmarSenha").value;

        if (senha || confirmarSenha) {

            if (!senha || !confirmarSenha) {
                event.preventDefault();
                alert("Preencha os dois campos de senha.");
                return;
            }

            if (senha !== confirmarSenha) {
                event.preventDefault();
                alert("As senhas não coincidem.");
                return;
            }

            const senhaValida =
                /^(?=\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

            if (!senhaValida.test(senha)) {
                event.preventDefault();

                alert(
                    "A senha deve ter pelo menos 8 caracteres, " +
                    "uma letra maiúscula, uma letra minúscula, " +
                    "um número e um caractere especial."
                );

                return;
            }

        }

        if (!form.checkValidity()) {
            event.preventDefault();
            form.reportValidity();
            return;
        }

    });

    // ==========================================
    // FORMULÁRIO PARA OUTRAS AÇÕES
    // ==========================================

    function enviarAcao(url) {

        const formulario = document.createElement("form");

        formulario.method = "POST";
        formulario.action = url;

        const csrf = form.querySelector('input[name="_csrf"]');

        if (csrf) {

            const token = document.createElement("input");

            token.type = "hidden";
            token.name = "_csrf";
            token.value = csrf.value;

            formulario.appendChild(token);
        }

        document.body.appendChild(formulario);

        formulario.submit();
    }

    // ==========================================
    // SAIR
    // ==========================================

    const btnSair = document.querySelector(".btn-sair");

    if (btnSair) {
        btnSair.addEventListener("click", async function (e) {
            e.preventDefault(); // Evita comportamento padrão se for link/botão direto

            const confirmar = await Dialogo.confirmar(
                "Deseja realmente sair da sessão atual?",
                {
                    titulo: "Sair da sessão",
                    textoConfirmar: "Sair",
                    perigo: false
                }
            );

            if (confirmar) {
                enviarAcao("/logout");
            }
        });
    }

    // ==========================================
    // EXCLUIR CONTA
    // ==========================================

    const btnExcluir = document.querySelector(".btn-excluir");

    if (btnExcluir) {

        btnExcluir.addEventListener("click", async function (e) {
            e.preventDefault();

            const confirmar = await Dialogo.confirmar(
                "Tem certeza que deseja excluir sua conta?\n\n" +
                "Todos os seus dados serão excluídos permanentemente.",
                {
                    titulo: "Excluir conta",
                    textoConfirmar: "Excluir conta",
                    perigo: true
                }
            );

            if (confirmar) {
                enviarAcao("/configuracoes/excluir");
            }

        });

    }

});
