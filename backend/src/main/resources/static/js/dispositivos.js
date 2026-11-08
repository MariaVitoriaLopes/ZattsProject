document.addEventListener(
    "DOMContentLoaded",
    function () {

        const lista =
            document.getElementById(
                "listaDispositivos"
            );

        const vazio =
            document.getElementById(
                "dispVazio"
            );

        const contagem =
            document.getElementById(
                "dispContagem"
            );

        if (!lista) {
            return;
        }

        function atualizarEstadoLista() {

            const total =
                lista.querySelectorAll(
                    ".disp-item"
                ).length;

            if (contagem) {

                contagem.textContent =
                    total === 1
                        ? "1 dispositivo"
                        : total
                        + " dispositivos";
            }

            if (vazio) {
                vazio.hidden =
                    total > 0;
            }
        }

        lista.addEventListener(
            "click",
            async function (evento) {

                const botao =
                    evento.target.closest(
                        ".disp-btn-desconectar"
                    );

                if (!botao) {
                    return;
                }

                const id =
                    botao.dataset.sessaoId;

                const atual =
                    botao.dataset.atual
                    === "true";

                if (!id) {
                    return;
                }

                const mensagem =
                    atual
                        ? "Deseja desconectar este dispositivo? Você será levado para a tela de login."
                        : "Deseja desconectar este dispositivo da sua conta?";

                if (!confirm(mensagem)) {
                    return;
                }

                botao.disabled = true;

                try {

                    const resposta =
                        await fetch(
                            "/configuracoes/dispositivos/"
                            + encodeURIComponent(id)
                            + "/desconectar",
                            {
                                method: "POST",
                                credentials: "same-origin"
                            }
                        );

                    if (
                        resposta.status === 401
                    ) {

                        window.location.href =
                            "/login";

                        return;
                    }

                    let dados = {};

                    try {
                        dados =
                            await resposta.json();
                    } catch (erro) {
                        // Resposta sem JSON.
                    }

                    if (!resposta.ok) {

                        throw new Error(
                            dados.erro
                            || "Não foi possível desconectar a sessão."
                        );
                    }

                    if (dados.atual === true) {

                        window.location.href =
                            "/login";

                        return;
                    }

                    const item =
                        botao.closest(
                            ".disp-item"
                        );

                    if (item) {
                        item.remove();
                    }

                    atualizarEstadoLista();

                } catch (erro) {

                    console.error(
                        "Erro ao desconectar sessão:",
                        erro
                    );

                    alert(
                        erro.message
                        || "Não foi possível desconectar a sessão."
                    );

                    botao.disabled = false;
                }
            }
        );

        atualizarEstadoLista();
    }
);