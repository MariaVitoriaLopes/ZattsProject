document.addEventListener("DOMContentLoaded", function () {

    const abas = document.querySelectorAll(".abas-config .aba");

    const secoes = {
        geral: document.getElementById("aba-geral"),
        metas: document.getElementById("aba-metas"),
        personalizados: document.getElementById("aba-personalizados")
    };

    function selecionarAba(nomeAba) {

        if (!secoes[nomeAba]) return;

        abas.forEach(aba => {
            aba.classList.toggle("ativa", aba.dataset.aba === nomeAba);
        });

        Object.entries(secoes).forEach(([nome, secao]) => {
            if (secao) secao.hidden = nome !== nomeAba;
        });

    }

    abas.forEach(aba => {

        aba.addEventListener("click", function () {
            selecionarAba(aba.dataset.aba);
        });

    });

    const parametros = new URLSearchParams(window.location.search);

    if (parametros.get("aba") === "metas") {
        selecionarAba("metas");
    } else if (window.location.hash === "#personalizados") {
        selecionarAba("personalizados");
    }

    // SOMENTE ALERTAS PERSONALIZADOS

    const modal = document.getElementById("modalAlerta");
    const form = document.getElementById("formAlerta");

    if (!modal || !form) return;

    const tituloModal = document.getElementById("tituloModal");
    const nome = document.getElementById("nomeAlerta");
    const tipo = document.getElementById("tipoAlerta");
    const valor = document.getElementById("valorLimite");
    const percentual = document.getElementById("percentualLimite");
    const ativo = document.getElementById("ativoAlerta");
    const ativoFalso = document.getElementById("ativoFalso");
    const campoAtivo = document.getElementById("campoAtivo");
    const labelLimite = document.getElementById("labelLimite");

    const urlCriar = form.getAttribute("action");

    function atualizarTipo() {

        if (tipo.value === "GASTO") {
            labelLimite.textContent = "Limite de gastos (R$)";
            valor.placeholder = "Ex.: 150";
        } else {
            labelLimite.textContent = "Limite de consumo (kWh)";
            valor.placeholder = "Ex.: 200";
        }

    }

    tipo.addEventListener("change", atualizarTipo);

    function abrirModal() {
        modal.hidden = false;
        nome.focus();
    }

    function fecharModal() {
        modal.hidden = true;
    }

    // CRIAR ALERTA

    const btnCriar = document.getElementById("btnCriarAlerta");

    if (btnCriar) {

        btnCriar.addEventListener("click", function () {

            form.reset();
            form.action = urlCriar;

            tituloModal.textContent = "Criar alerta";
            campoAtivo.hidden = true;
            ativoFalso.disabled = true;

            atualizarTipo();
            abrirModal();

        });

    }

    // EDITAR ALERTA

    document.querySelectorAll("#aba-personalizados .notificacao-editar").forEach(botao => {

        botao.addEventListener("click", function () {

            form.reset();

            const dados = botao.dataset;

            form.action = urlCriar.replace(
                /\/criar$/,
                "/" + encodeURIComponent(dados.id) + "/editar"
            );

            tituloModal.textContent = "Editar alerta";

            nome.value = dados.nome;
            tipo.value = dados.tipo;
            valor.value = dados.valor || "";
            percentual.value = dados.percentual || "";
            ativo.checked = dados.ativo === "true";

            campoAtivo.hidden = false;
            ativoFalso.disabled = false;

            atualizarTipo();
            abrirModal();

        });

    });

    // FECHAR MODAL DE ALERTA

    document.getElementById("btnFecharModal")?.addEventListener("click", fecharModal);
    document.getElementById("btnCancelar")?.addEventListener("click", fecharModal);

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

    // EXCLUIR ALERTA

    document.querySelectorAll("#aba-personalizados .form-excluir").forEach(formExcluir => {

        formExcluir.addEventListener("submit", function (event) {

            if (!confirm("Tem certeza que deseja excluir este alerta?")) {
                event.preventDefault();
            }

        });

    });

});
