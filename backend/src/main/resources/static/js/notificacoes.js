
document.addEventListener("DOMContentLoaded", function () {

    const abas = document.querySelectorAll(".abas-config .aba");

    const secoes = {
        geral: document.getElementById("aba-geral"),
        metas: document.getElementById("aba-metas"),
        personalizados: document.getElementById("aba-personalizados")
    };

    function selecionarAba(nomeAba) {

        if (!secoes[nomeAba]) {
            return;
        }

        abas.forEach(aba => {

            aba.classList.toggle(
                "ativa",
                aba.dataset.aba === nomeAba
            );

        });

        Object.entries(secoes).forEach(([nome, secao]) => {

            if (secao) {
                secao.hidden = nome !== nomeAba;
            }

        });

    }

    // NAVEGAÇÃO ENTRE AS ABAS

    abas.forEach(aba => {

        aba.addEventListener("click", function () {
            selecionarAba(aba.dataset.aba);
        });

    });

    // ABRIR ABA PELA URL

    const parametros = new URLSearchParams(window.location.search);
    const abaUrl = parametros.get("aba");

    if (abaUrl && secoes[abaUrl]) {

        selecionarAba(abaUrl);

    } else if (window.location.hash === "#personalizados") {

        selecionarAba("personalizados");

    } else if (window.location.hash === "#metas") {

        selecionarAba("metas");

    }

});
