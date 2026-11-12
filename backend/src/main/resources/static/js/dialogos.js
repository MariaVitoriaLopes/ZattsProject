(function () {
    const icones = {
        info: "info",
        sucesso: "check_circle",
        erro: "error",
        aviso: "warning",
        pergunta: "priority_high"
    };

    const titulos = {
        info: "Informação",
        sucesso: "Tudo certo",
        erro: "Algo deu errado",
        aviso: "Atenção",
        pergunta: "Confirmação"
    };

    const fila = [];
    let aberto = false;

    function detectarTipo(mensagem) {
        const texto = String(mensagem).toLowerCase();

        if (/erro|falha|não foi possível|nao foi possivel|inválid|invalid|expirou/.test(texto)) {
            return "erro";
        }

        if (/sucesso|salvo|salva|atualizad|cadastrad|enviad|criad|exclu/.test(texto)) {
            return "sucesso";
        }

        return "info";
    }

    function normalizar(mensagem, opcoes, confirmar) {
        const config = opcoes || {};
        const texto = mensagem == null ? "" : String(mensagem);
        const tipo = icones[config.tipo]
            ? config.tipo
            : (confirmar ? "pergunta" : detectarTipo(texto));

        return {
            mensagem: texto,
            confirmar: confirmar,
            tipo: tipo,
            titulo: config.titulo || titulos[tipo],
            textoConfirmar: config.textoConfirmar || (confirmar ? "Confirmar" : "Entendi"),
            textoCancelar: config.textoCancelar || "Cancelar",
            perigo: Boolean(config.perigo)
        };
    }

    function criarBotao(texto, classe) {
        const botao = document.createElement("button");
        botao.type = "button";
        botao.className = "zd-botao " + classe;
        botao.textContent = texto;
        return botao;
    }

    function abrir(item) {
        aberto = true;

        let fechado = false;
        const focoAnterior = document.activeElement;

        const fundo = document.createElement("div");
        fundo.className = "zd-fundo";

        const caixa = document.createElement("div");
        caixa.className = "zd-caixa zd-" + item.tipo;
        caixa.setAttribute("role", item.confirmar ? "alertdialog" : "dialog");
        caixa.setAttribute("aria-modal", "true");
        caixa.setAttribute("aria-labelledby", "zdTitulo");
        caixa.setAttribute("aria-describedby", "zdMensagem");

        const icone = document.createElement("span");
        icone.className = "material-symbols-outlined zd-icone";
        icone.textContent = icones[item.tipo];

        const titulo = document.createElement("h2");
        titulo.id = "zdTitulo";
        titulo.className = "zd-titulo";
        titulo.textContent = item.titulo;

        const mensagem = document.createElement("p");
        mensagem.id = "zdMensagem";
        mensagem.className = "zd-mensagem";
        mensagem.textContent = item.mensagem;

        const acoes = document.createElement("div");
        acoes.className = "zd-acoes";

        const botoes = [];
        let botaoCancelar = null;

        if (item.confirmar) {
            botaoCancelar = criarBotao(item.textoCancelar, "zd-botao-secundario");
            acoes.appendChild(botaoCancelar);
            botoes.push(botaoCancelar);
        }

        const botaoOk = criarBotao(
            item.textoConfirmar,
            item.perigo ? "zd-botao-perigo" : "zd-botao-principal"
        );
        acoes.appendChild(botaoOk);
        botoes.push(botaoOk);

        function fechar(resultado) {
            if (fechado) {
                return;
            }

            fechado = true;
            document.removeEventListener("keydown", teclado, true);
            fundo.classList.remove("zd-visivel");

            setTimeout(function () {
                fundo.remove();
                aberto = false;

                if (focoAnterior && typeof focoAnterior.focus === "function") {
                    focoAnterior.focus();
                }

                item.resolver(resultado);
                proximo();
            }, 150);
        }

        function teclado(evento) {
            if (evento.key === "Escape") {
                evento.preventDefault();
                fechar(false);
                return;
            }

            if (evento.key === "Tab") {
                evento.preventDefault();

                const indice = botoes.indexOf(document.activeElement);

                const destino = evento.shiftKey
                    ? (indice <= 0 ? botoes.length - 1 : indice - 1)
                    : (indice + 1) % botoes.length;

                botoes[destino].focus();
            }
        }

        botaoOk.addEventListener("click", function () {
            fechar(true);
        });

        if (botaoCancelar) {
            botaoCancelar.addEventListener("click", function () {
                fechar(false);
            });
        }

        fundo.addEventListener("mousedown", function (evento) {
            if (evento.target === fundo) {
                fechar(false);
            }
        });

        document.addEventListener("keydown", teclado, true);

        caixa.appendChild(icone);
        caixa.appendChild(titulo);
        caixa.appendChild(mensagem);
        caixa.appendChild(acoes);
        fundo.appendChild(caixa);
        document.body.appendChild(fundo);

        requestAnimationFrame(function () {
            fundo.classList.add("zd-visivel");
        });

        const focoInicial = item.perigo && botaoCancelar ? botaoCancelar : botaoOk;
        focoInicial.focus();
    }

    function proximo() {
        if (aberto || fila.length === 0) {
            return;
        }

        if (!document.body) {
            document.addEventListener("DOMContentLoaded", proximo, { once: true });
            return;
        }

        abrir(fila.shift());
    }

    function enfileirar(dados) {
        return new Promise(function (resolver) {
            fila.push(Object.assign({}, dados, { resolver: resolver }));
            proximo();
        });
    }

    window.Dialogo = {
        alerta: function (mensagem, opcoes) {
            return enfileirar(normalizar(mensagem, opcoes, false));
        },

        confirmar: function (mensagem, opcoes) {
            return enfileirar(normalizar(mensagem, opcoes, true));
        }
    };

    window.alert = function (mensagem) {
        window.Dialogo.alerta(mensagem);
    };

    document.addEventListener("click", async function (evento) {
        const alvo = evento.target.closest("[data-confirmar]");

        if (!alvo || alvo.dataset.confirmado === "true") {
            return;
        }

        evento.preventDefault();
        evento.stopPropagation();

        const confirmado = await window.Dialogo.confirmar(
            alvo.dataset.confirmar,
            { perigo: alvo.dataset.perigo !== "false" }
        );

        if (!confirmado) {
            return;
        }

        alvo.dataset.confirmado = "true";
        alvo.click();
        delete alvo.dataset.confirmado;
    }, true);
})();