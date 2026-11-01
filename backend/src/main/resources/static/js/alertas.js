
document.addEventListener("DOMContentLoaded", function () {

    const modal = document.getElementById("modalAlerta");
    const form = document.getElementById("formAlerta");

    if (!modal || !form) return;

    const titulo = document.getElementById("tituloModal");
    const nome = document.getElementById("nomeAlerta");
    const tipo = document.getElementById("tipoAlerta");
    const valor = document.getElementById("valorLimite");
    const percentual = document.getElementById("percentualLimite");
    const ativo = document.getElementById("ativoAlerta");
    const ativoFalso = document.getElementById("ativoFalso");
    const campoAtivo = document.getElementById("campoAtivo");
    const labelLimite = document.getElementById("labelLimite");

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

    function abrirModal() {
        modal.hidden = false;
        nome.focus();
    }

    function fecharModal() {
        modal.hidden = true;
    }

    tipo.addEventListener("change", atualizarTipo);

    // CRIAR ALERTA

    document.getElementById("btnCriarAlerta")?.addEventListener("click", function () {

        form.reset();
        form.action = urlCriar;

        titulo.textContent = "Criar alerta";

        campoAtivo.hidden = true;
        ativoFalso.disabled = true;

        atualizarTipo();
        abrirModal();

    });

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

            nome.value = dados.nome || "";
            tipo.value = dados.tipo || "CONSUMO";
            valor.value = dados.valor || "";
            percentual.value = dados.percentual || "";
            ativo.checked = dados.ativo === "true";

            campoAtivo.hidden = false;
            ativoFalso.disabled = false;

            atualizarTipo();
            abrirModal();

        });

    });

    // FECHAR MODAL

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

    document.querySelectorAll(".excluir-alerta").forEach(formulario => {

        formulario.addEventListener("submit", function (event) {

            if (!confirm("Tem certeza que deseja excluir este alerta?")) {
                event.preventDefault();
            }

        });

    });

    // ATIVAR / DESATIVAR ALERTA
    // O endpoint para o interruptor ainda precisa ser conectado.

    document.querySelectorAll(".toggle-alerta").forEach(checkbox => {

        checkbox.addEventListener("change", function () {

            checkbox.checked = !checkbox.checked;

            alert(
                "Para ativar ou desativar diretamente, é necessário " +
                "conectar o endpoint no backend. Por enquanto, use Editar alerta."
            );

        });

    });

});
