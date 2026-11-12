document.addEventListener("DOMContentLoaded", function () {

    const abas = document.querySelectorAll(".abas-config .aba");

    const secoes = {
        geral: document.getElementById("aba-geral"),
        metas: document.getElementById("aba-metas"),
        personalizados: document.getElementById("aba-personalizados")
    };

    const chaveOcultas = "zatts:notificacoesOcultas";
    const listaGeral = document.querySelector("#aba-geral .lista-notificacoes");
    const vazioGeral = document.getElementById("vazioGeral");
    const topoAcoes = document.getElementById("topoAcoes");

    function lerOcultas() {
        try {
            return JSON.parse(localStorage.getItem(chaveOcultas)) || [];
        } catch (erro) {
            return [];
        }
    }

    function salvarOcultas(ids) {
        try {
            localStorage.setItem(chaveOcultas, JSON.stringify(ids));
        } catch (erro) {
            console.error(erro);
        }
    }

    function atualizarTopo() {
        if (!topoAcoes) {
            return;
        }

        const restantes = listaGeral
            ? listaGeral.querySelectorAll(".notificacao").length
            : 0;

        topoAcoes.hidden = !secoes.geral || secoes.geral.hidden || restantes === 0;
    }

    function verificarListaVazia() {
        if (listaGeral && vazioGeral) {
            const restantes = listaGeral.querySelectorAll(".notificacao").length;
            listaGeral.hidden = restantes === 0;
            vazioGeral.hidden = restantes > 0;
        }

        atualizarTopo();
    }

    function removerItem(item) {
        item.remove();
        verificarListaVazia();
    }

    async function excluirNoServidor(url) {
        try {
            const resposta = await fetch(url, {
                method: "DELETE",
                credentials: "same-origin"
            });

            if (resposta.status === 401) {
                window.location.href = "/login";
                return false;
            }

            return resposta.ok || resposta.status === 404;
        } catch (erro) {
            console.error("Erro ao excluir notificação:", erro);
            return false;
        }
    }

    async function enviarLida(id) {
        try {
            const resposta = await fetch(
                "/notificacoes/" + encodeURIComponent(id) + "/lida",
                {
                    method: "POST",
                    credentials: "same-origin"
                }
            );

            if (resposta.status === 401) {
                window.location.href = "/login";
                return false;
            }

            return resposta.ok;
        } catch (erro) {
            console.error("Erro ao marcar notificação como lida:", erro);
            return false;
        }
    }

    async function garantirLida(item) {
        if (item.dataset.lida === "true") {
            return true;
        }

        const ok = await enviarLida(item.dataset.notificacaoId);

        if (ok) {
            item.dataset.lida = "true";
        }

        return ok;
    }

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
            atualizarTopo();
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

    const ocultas = lerOcultas();

    if (listaGeral) {
        listaGeral
            .querySelectorAll(".notificacao[data-notificacao-id]")
            .forEach(item => {
                if (ocultas.includes(item.dataset.notificacaoId)) {
                    item.remove();
                }
            });
    }

    verificarListaVazia();

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

            const botaoLida = item.querySelector('[data-acao="lida"]');
            const botaoExcluir = item.querySelector('[data-acao="excluir"]');

            if (botaoLida) {
                botaoLida.addEventListener("click", async function (evento) {
                    evento.stopPropagation();

                    if (await garantirLida(item)) {
                        await atualizarPontoNaoLidas();
                    }
                });
            }

            if (botaoExcluir) {
                botaoExcluir.addEventListener("click", async function (evento) {
                    evento.stopPropagation();

                    const url = "/notificacoes/" + encodeURIComponent(item.dataset.notificacaoId);

                    if (await excluirNoServidor(url)) {
                        removerItem(item);
                        await atualizarPontoNaoLidas();
                    }
                });
            }
        });

    const btnMarcarTodas = document.getElementById("btnMarcarTodas");
    const btnLimparTodas = document.getElementById("btnLimparTodas");

    if (btnMarcarTodas && listaGeral) {
        btnMarcarTodas.addEventListener("click", async function () {
            const itens = Array.from(
                listaGeral.querySelectorAll(".notificacao[data-notificacao-id]")
            );

            await Promise.all(itens.map(garantirLida));
            await atualizarPontoNaoLidas();
        });
    }

    if (btnLimparTodas && listaGeral) {
        btnLimparTodas.addEventListener("click", async function () {
            const confirmado = await Dialogo.confirmar(
                "Todas as notificações serão apagadas e essa ação não pode ser desfeita.",
                { titulo: "Limpar notificações", textoConfirmar: "Limpar", perigo: true }
            );

            if (!confirmado) {
                return;
            }

            if (await excluirNoServidor("/notificacoes")) {
                listaGeral
                    .querySelectorAll(".notificacao")
                    .forEach(removerItem);

                await atualizarPontoNaoLidas();
            }
        });
    }

});