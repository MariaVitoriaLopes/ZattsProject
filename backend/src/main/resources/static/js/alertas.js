document.addEventListener("DOMContentLoaded", function () {

    const modal = document.getElementById("modalAlerta");
    const form = document.getElementById("formAlerta");

    if (!modal || !form) return;

    const titulo = document.getElementById("tituloModal");
    const nome = document.getElementById("nomeAlerta");
    const categoria = document.getElementById("categoriaAlerta");
    const tipo = document.getElementById("tipoAlerta");
    const valor = document.getElementById("valorLimite");
    const percentual = document.getElementById("percentualLimite");

    const canalPush = form.querySelector(
        'input[name="canalPush"]'
    );

    const canalEmail = form.querySelector(
        'input[name="canalEmail"]'
    );

    const canalApp = form.querySelector(
        'input[name="canalApp"]'
    );

    const lembrete = document.getElementById("lembreteAlerta");
    const repetir = document.getElementById("repetirAlerta");

    const ativo = document.getElementById("ativoAlerta");
    const ativoFalso = document.getElementById("ativoFalso");

    const labelLimite = document.getElementById("labelLimite");
    const textoSalvar = document.getElementById("textoSalvarAlerta");

    const icones = form.querySelectorAll(
        'input[name="icone"]'
    );

    const csrfInput = form.querySelector(
        'input[type="hidden"][name]:not([name="ativo"])'
    );

    const urlCriar = form.action;

    function atualizarTipo() {

        if (tipo.value === "GASTO") {
            labelLimite.textContent = "Limite de gastos (R$)";
            valor.placeholder = "Ex.: 150";
        } else {
            labelLimite.textContent = "Limite de consumo (kWh)";
            valor.placeholder = "Ex.: 200";
        }
    }

    function selecionarIcone(valorIcone) {

        const iconeDesejado = valorIcone || "bolt";

        icones.forEach(radio => {
            radio.checked = radio.value === iconeDesejado;
        });
    }

    function abrirModal() {
        modal.hidden = false;
        nome.focus();
    }

    function fecharModal() {
        modal.hidden = true;
    }

    tipo.addEventListener("change", atualizarTipo);

    // CRIAR ALERTA

    document.getElementById("btnCriarAlerta")?.addEventListener(
        "click",
        function () {

            form.reset();
            form.action = urlCriar;

            ativoFalso.disabled = false;

            titulo.textContent = "Novo alerta";
            textoSalvar.textContent = "Criar novo alerta";

            atualizarTipo();
            abrirModal();
        }
    );

    // EDITAR ALERTA

    document.querySelectorAll(".editar-alerta").forEach(botao => {

        botao.addEventListener("click", function () {

            const dados = botao.dataset;

            form.reset();

            form.action = urlCriar.replace(
                /\/criar$/,
                "/" + encodeURIComponent(dados.id) + "/editar"
            );

            titulo.textContent = "Editar alerta";
            textoSalvar.textContent = "Salvar alerta";

            nome.value = dados.nome || "";

            categoria.value =
                dados.categoria || "ALERTA";

            tipo.value =
                dados.tipo || "CONSUMO";

            valor.value =
                dados.valor || "";

            percentual.value =
                dados.percentual || "";

            selecionarIcone(
                dados.icone || "bolt"
            );

            canalPush.checked =
                dados.canalPush !== "false";

            canalEmail.checked =
                dados.canalEmail !== "false";

            canalApp.checked =
                dados.canalApp !== "false";

            lembrete.value =
                dados.lembrete || "NA_HORA";

            repetir.value =
                dados.repetir || "1_HORA";

            ativo.checked =
                dados.ativo !== "false";

            ativoFalso.disabled = false;

            atualizarTipo();
            abrirModal();
        });
    });

    // FECHAR MODAL

    document.getElementById("btnFecharModal")
        ?.addEventListener("click", fecharModal);

    document.getElementById("btnCancelar")
        ?.addEventListener("click", fecharModal);

    modal.addEventListener("click", function (event) {

        if (event.target === modal) {
            fecharModal();
        }
    });

    document.addEventListener("keydown", function (event) {

        if (event.key === "Escape" && !modal.hidden) {
            fecharModal();
        }
    });

    // GARANTE O VALOR CORRETO DO CAMPO "ATIVO"

    form.addEventListener("submit", function () {
        ativoFalso.disabled = ativo.checked;
    });

    // EXCLUIR ALERTA

    document.querySelectorAll(".excluir-alerta").forEach(
        formulario => {

            formulario.addEventListener(
                "submit",
                function (event) {

                    if (!confirm(
                        "Tem certeza que deseja excluir este alerta?"
                    )) {
                        event.preventDefault();
                    }
                }
            );
        }
    );

    // ATIVAR / DESATIVAR ALERTA

    document.querySelectorAll(".toggle-alerta").forEach(
        checkbox => {

            checkbox.addEventListener(
                "change",
                async function () {

                    const id = checkbox.dataset.id;
                    const novoEstado = checkbox.checked;
                    const estadoAnterior = !novoEstado;

                    if (!id) {
                        checkbox.checked = estadoAnterior;
                        return;
                    }

                    checkbox.disabled = true;

                    try {

                        const parametros =
                            new URLSearchParams();

                        parametros.append(
                            "ativo",
                            String(novoEstado)
                        );

                        if (csrfInput) {
                            parametros.append(
                                csrfInput.name,
                                csrfInput.value
                            );
                        }

                        const resposta = await fetch(
                            "/notificacoes/alertas/" +
                            encodeURIComponent(id) +
                            "/ativar",
                            {
                                method: "POST",
                                headers: {
                                    "Content-Type":
                                        "application/x-www-form-urlencoded"
                                },
                                body: parametros.toString(),
                                credentials: "same-origin"
                            }
                        );

                        if (resposta.status === 401) {

                            checkbox.checked =
                                estadoAnterior;

                            alert(
                                "Sua sessão expirou. Faça login novamente."
                            );

                            window.location.href =
                                "/login";

                            return;
                        }

                        if (!resposta.ok) {

                            checkbox.checked =
                                estadoAnterior;

                            alert(
                                "Não foi possível atualizar o alerta."
                            );

                            return;
                        }

                    } catch (erro) {

                        checkbox.checked =
                            estadoAnterior;

                        console.error(
                            "Erro ao atualizar alerta:",
                            erro
                        );

                        alert(
                            "Não foi possível atualizar o alerta."
                        );

                    } finally {
                        checkbox.disabled = false;
                    }
                }
            );
        }
    );

});
