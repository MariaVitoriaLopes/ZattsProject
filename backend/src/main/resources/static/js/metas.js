
document.addEventListener("DOMContentLoaded", function () {

    const modal = document.getElementById("modalMeta");
    const form = document.getElementById("formMeta");

    if (!modal || !form) return;

    const tituloModal = document.getElementById("tituloModalMeta");
    const tipo = document.getElementById("tipoMetaFormulario");
    const valor = document.getElementById("valorMetaFormulario");
    const labelValor = document.getElementById("labelValorMeta");

    const btnSalvar = document.getElementById("btnSalvarMeta");

    const urlCriar = form.dataset.urlCriar;
    const urlEditar = form.dataset.urlEditar;

    if (!tipo || !valor || !tituloModal || !labelValor || !btnSalvar) return;

    // =====================================================
    // ATUALIZAR TIPO DE META
    // =====================================================

    function atualizarTipo() {

        if (tipo.value === "CONSUMO") {

            labelValor.textContent = "Meta mensal de consumo (kWh)";
            valor.placeholder = "Ex.: 250";

        } else {

            labelValor.textContent = "Valor da meta";
            valor.placeholder = "Ex.: 250";
        }
    }

    // =====================================================
    // ABRIR E FECHAR MODAL
    // =====================================================

    function abrirModal() {

        modal.hidden = false;
        valor.focus();
    }

    function fecharModal() {

        modal.hidden = true;
        form.reset();
    }

    // =====================================================
    // CADASTRAR META
    // =====================================================

    function prepararCriacao() {

        form.reset();
        form.action = urlCriar;

        tituloModal.textContent = "Cadastrar meta";

        btnSalvar.querySelector("span:last-child").textContent =
            "Cadastrar meta";

        tipo.value = "CONSUMO";
        valor.value = "";

        atualizarTipo();
        abrirModal();
    }

    // =====================================================
    // EDITAR META
    // =====================================================

    function prepararEdicao(valorAtual) {

        form.reset();
        form.action = urlEditar;

        tituloModal.textContent = "Editar meta";

        btnSalvar.querySelector("span:last-child").textContent =
            "Salvar alterações";

        tipo.value = "CONSUMO";
        valor.value = valorAtual;

        atualizarTipo();
        abrirModal();
    }

    // =====================================================
    // BOTÃO DE EDITAR META
    // =====================================================

    window.abrirModalMeta = function () {

        const textoMeta = document.getElementById("valorMeta")
            ?.textContent || "";

        const valorAtual = textoMeta
            .replace("kWh", "")
            .trim()
            .replace(/\./g, "")
            .replace(",", ".");

        const numeroAtual = Number(valorAtual);

        const existeMeta =
            valorAtual !== "" &&
            Number.isFinite(numeroAtual) &&
            numeroAtual > 0;

        if (existeMeta) {

            prepararEdicao(String(numeroAtual));

        } else {

            prepararCriacao();
        }
    };

    // =====================================================
    // FECHAR MODAL
    // =====================================================

    document.getElementById("btnFecharModalMeta")
        ?.addEventListener("click", fecharModal);

    document.getElementById("btnCancelarMeta")
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

    // =====================================================
    // ATUALIZAR TIPO
    // =====================================================

    tipo.addEventListener("change", atualizarTipo);

    // =====================================================
    // VALIDAR META
    // =====================================================

    form.addEventListener("submit", function (event) {

        const numero = Number(valor.value.replace(",", "."));

        if (!Number.isFinite(numero) || numero <= 0) {

            event.preventDefault();

            alert("Informe uma meta maior que zero.");
        }
    });

});
