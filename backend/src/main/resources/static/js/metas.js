
document.addEventListener("DOMContentLoaded", function () {

    const modal = document.getElementById("modalMeta");
    const form = document.getElementById("formMeta");

    if (!modal || !form) return;

    const tituloModal = document.getElementById("tituloModalMeta");
    const tipo = document.getElementById("tipoMetaFormulario");
    const valor = document.getElementById("valorMetaFormulario");
    const labelValor = document.getElementById("labelValorMeta");

    const btnCriar = document.getElementById("btnCriarMeta");
    const btnSalvar = document.getElementById("btnSalvarMeta");

    const urlCriar = form.dataset.urlCriar;
    const urlEditar = form.dataset.urlEditar;

    // ATUALIZAR TIPO DE META

    function atualizarTipo() {

        if (tipo.value === "CONSUMO") {

            labelValor.textContent = "Meta mensal de consumo (kWh)";
            valor.placeholder = "Ex.: 200";

        } else if (tipo.value === "VALOR") {

            labelValor.textContent = "Meta mensal de gastos (R$)";
            valor.placeholder = "Ex.: 250";

        } else {

            labelValor.textContent = "Valor da meta";
            valor.placeholder = "Ex.: 250";

        }

    }

    // ABRIR E FECHAR MODAL

    function abrirModal() {
        modal.hidden = false;
        tipo.focus();
    }

    function fecharModal() {
        modal.hidden = true;
        form.reset();
    }

    // CONFIGURAR TIPOS DISPONIVEIS

    function configurarOpcoes(tipoAtual) {

        tipo.querySelectorAll("option").forEach(opcao => {

            if (!opcao.value) return;

            const permitida = tipoAtual
                ? opcao.value === tipoAtual
                : opcao.dataset.disponivel === "true";

            opcao.disabled = !permitida;
            opcao.hidden = !permitida;

        });

    }

    // CRIAR META

    btnCriar?.addEventListener("click", function () {

        form.reset();
        form.action = urlCriar;

        tituloModal.textContent = "Criar meta";
        btnSalvar.textContent = "Criar meta";

        configurarOpcoes(null);

        tipo.value = "";
        valor.value = "";

        atualizarTipo();
        abrirModal();

    });

    // EDITAR META

    document.querySelectorAll("#aba-metas .btn-editar-meta").forEach(botao => {

        botao.addEventListener("click", function () {

            form.reset();
            form.action = urlEditar;

            tituloModal.textContent = "Editar meta";
            btnSalvar.textContent = "Salvar alterações";

            configurarOpcoes(botao.dataset.tipo);

            tipo.value = botao.dataset.tipo;
            valor.value = botao.dataset.valor || "";

            atualizarTipo();
            abrirModal();

        });

    });

    // ALTERAR TIPO

    tipo.addEventListener("change", atualizarTipo);

    // FECHAR MODAL DE META

    document.getElementById("btnFecharModalMeta")?.addEventListener("click", fecharModal);
    document.getElementById("btnCancelarMeta")?.addEventListener("click", fecharModal);

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

    // VALIDAR META

    form.addEventListener("submit", function (event) {

        const numero = Number(valor.value.replace(",", "."));

        if (!tipo.value || !Number.isFinite(numero) || numero <= 0) {

            event.preventDefault();

            alert("Selecione um tipo e informe uma meta maior que zero.");

        }

    });

});
