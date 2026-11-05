(function () {

    const raiz = document.documentElement;
    const escuroDoSistema =
        window.matchMedia('(prefers-color-scheme: dark)');

    let temporizadorSalvamento = null;

    function ler(chave, padrao) {
        try {
            return localStorage.getItem(chave) || padrao;
        } catch (erro) {
            return padrao;
        }
    }

    function gravar(chave, valor) {
        try {
            localStorage.setItem(chave, valor);
        } catch (erro) {
            return;
        }
    }

    function aplicarTema(modo) {

        const escuro =
            modo === 'escuro'
            || (
                modo === 'automatico'
                && escuroDoSistema.matches
            );

        raiz.setAttribute(
            'data-tema',
            escuro ? 'escuro' : 'claro'
        );

        document.dispatchEvent(
            new Event('zatts:tema')
        );
    }

    function aplicarCor(cor, personalizada) {

        raiz.setAttribute('data-cor', cor);

        if (cor === 'outra') {
            raiz.style.setProperty(
                '--cor-primaria-400',
                personalizada
            );
        } else {
            raiz.style.removeProperty(
                '--cor-primaria-400'
            );
        }

        document.dispatchEvent(
            new Event('zatts:tema')
        );
    }

    function preferenciasAtuais() {

        return {
            tema: ler(
                'zatts-tema',
                'automatico'
            ),
            corDestaque: ler(
                'zatts-cor',
                'padrao'
            ),
            corPersonalizada: ler(
                'zatts-cor-personalizada',
                '#E6FB1A'
            ),
            idioma: ler(
                'zatts-idioma',
                'pt-BR'
            )
        };
    }

    function salvarServidor() {

        clearTimeout(temporizadorSalvamento);

        temporizadorSalvamento =
            setTimeout(async function () {

                try {

                    await fetch(
                        '/configuracoes/aparencia',
                        {
                            method: 'POST',
                            headers: {
                                'Content-Type':
                                    'application/json'
                            },
                            body: JSON.stringify(
                                preferenciasAtuais()
                            )
                        }
                    );

                } catch (erro) {
                    // localStorage continua funcionando
                    // mesmo se o servidor estiver indisponível
                }

            }, 250);
    }

    async function carregarServidor() {

        try {

            const resposta = await fetch(
                '/configuracoes/aparencia'
            );

            if (!resposta.ok) {
                return;
            }

            const dados =
                await resposta.json();

            gravar(
                'zatts-tema',
                dados.tema
            );

            gravar(
                'zatts-cor',
                dados.corDestaque
            );

            gravar(
                'zatts-cor-personalizada',
                dados.corPersonalizada
            );

            gravar(
                'zatts-idioma',
                dados.idioma
            );

            aplicarTema(dados.tema);

            aplicarCor(
                dados.corDestaque,
                dados.corPersonalizada
            );

            document.dispatchEvent(
                new CustomEvent(
                    'zatts:aparencia-carregada',
                    {
                        detail: dados
                    }
                )
            );

        } catch (erro) {
            // mantém as preferências locais
        }
    }

    window.zattsTema = {

        ler: ler,
        gravar: gravar,

        definirTema: function (modo) {

            gravar(
                'zatts-tema',
                modo
            );

            aplicarTema(modo);
            salvarServidor();
        },

        definirCor: function (
                cor,
                personalizada) {

            const valor =
                personalizada
                || ler(
                    'zatts-cor-personalizada',
                    '#E6FB1A'
                );

            gravar(
                'zatts-cor',
                cor
            );

            if (cor === 'outra') {
                gravar(
                    'zatts-cor-personalizada',
                    valor
                );
            }

            aplicarCor(
                cor,
                valor
            );

            salvarServidor();
        },

        definirIdioma: function (idioma) {

            gravar(
                'zatts-idioma',
                idioma
            );

            salvarServidor();
        },

        alternar: function () {

            const proximo =
                raiz.getAttribute('data-tema')
                    === 'escuro'
                    ? 'claro'
                    : 'escuro';

            gravar(
                'zatts-tema',
                proximo
            );

            aplicarTema(proximo);
            salvarServidor();
        }
    };

    document.addEventListener(
        'click',
        function (evento) {

            if (
                evento.target.closest(
                    '[data-alternar-tema]'
                )
            ) {
                window.zattsTema.alternar();
            }
        }
    );

    aplicarTema(
        ler(
            'zatts-tema',
            'automatico'
        )
    );

    aplicarCor(
        ler(
            'zatts-cor',
            'padrao'
        ),
        ler(
            'zatts-cor-personalizada',
            '#E6FB1A'
        )
    );

    carregarServidor();

    escuroDoSistema.addEventListener(
        'change',
        function () {

            if (
                ler(
                    'zatts-tema',
                    'automatico'
                ) === 'automatico'
            ) {
                aplicarTema('automatico');
            }
        }
    );

})();