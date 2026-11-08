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

    async function atualizarPontoNaoLidas() {

        const ponto =
            document.getElementById("pontoNotificacoes");

        if (!ponto) {
            return;
        }

        try {

            const resposta = await fetch(
                "/notificacoes/nao-lidas/contagem",
                {
                    credentials: "same-origin"
                }
            );

            if (!resposta.ok) {
                return;
            }

            const dados = await resposta.json();

            ponto.hidden =
                !(dados.quantidade > 0);

        } catch (erro) {

            console.error(
                "Erro ao atualizar contador de notificações:",
                erro
            );
        }
    }

    async function marcarComoLida(item) {

        const id =
            item.dataset.notificacaoId;

        const jaLida =
            item.dataset.lida === "true";

        if (!id || jaLida) {
            return;
        }

        try {

            const resposta = await fetch(
                "/notificacoes/"
                + encodeURIComponent(id)
                + "/lida",
                {
                    method: "POST",
                    credentials: "same-origin"
                }
            );

            if (resposta.status === 401) {

                window.location.href = "/login";
                return;
            }

            if (!resposta.ok) {

                console.error(
                    "Não foi possível marcar a notificação como lida."
                );

                return;
            }

            item.dataset.lida = "true";

            await atualizarPontoNaoLidas();

        } catch (erro) {

            console.error(
                "Erro ao marcar notificação como lida:",
                erro
            );
        }
    }

    // NAVEGAÇÃO ENTRE ABAS

    abas.forEach(aba => {

        aba.addEventListener(
            "click",
            function () {

                selecionarAba(
                    aba.dataset.aba
                );
            }
        );
    });

    const parametros =
        new URLSearchParams(
            window.location.search
        );

    const abaUrl =
        parametros.get("aba");

    if (abaUrl && secoes[abaUrl]) {

        selecionarAba(abaUrl);

    } else if (
        window.location.hash === "#personalizados"
    ) {

        selecionarAba("personalizados");

    } else if (
        window.location.hash === "#metas"
    ) {

        selecionarAba("metas");
    }

    document
        .querySelectorAll(
            "#aba-geral .notificacao[data-notificacao-id]"
        )
        .forEach(item => {

            item.addEventListener(
                "click",
                function () {

                    marcarComoLida(item);
                }
            );
        });

});