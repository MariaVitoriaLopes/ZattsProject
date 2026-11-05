document.addEventListener('DOMContentLoaded', function () {
    const secoes = document.querySelectorAll('.cfg-notif-secao');
    const links = document.querySelectorAll('.cfg-notif-menu a');

    let preferenciasPersistidas = {};

    function possuiChave(chave) {
        return Object.prototype.hasOwnProperty.call(
            preferenciasPersistidas,
            chave
        );
    }

    async function salvarPreferencias() {
        try {
            const resposta = await fetch(
                '/configuracoes/notificacoes/preferencias',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(preferenciasPersistidas)
                }
            );

            if (resposta.status === 401) {
                window.location.href = '/login';
                return;
            }

            if (!resposta.ok) {
                const retorno = await resposta.json();

                console.error(
                    retorno.erro ||
                    'Não foi possível salvar as preferências de notificações.'
                );
            }

        } catch (erro) {
            console.error(
                'Erro ao salvar preferências de notificações:',
                erro
            );
        }
    }

    function configurarSecoes() {
        secoes.forEach(function (secao) {
            const interruptor =
                secao.querySelector('.interruptor');

            const caixas = Array.from(
                secao.querySelectorAll(
                    '.cfg-notif-linha:not(.cfg-notif-cabecalho) ' +
                    'input[type="checkbox"][data-chave]'
                )
            );

            const chaveSecao = interruptor.dataset.chave;

            let estadoAnterior = caixas.map(function (caixa) {
                const chave = caixa.dataset.chave;

                const valor = possuiChave(chave)
                    ? Boolean(preferenciasPersistidas[chave])
                    : caixa.checked;

                preferenciasPersistidas[chave] = valor;

                return valor;
            });

            const secaoDesativada =
                possuiChave(chaveSecao)
                    ? Boolean(preferenciasPersistidas[chaveSecao])
                    : estadoAnterior.every(function (valor) {
                        return !valor;
                    });

            preferenciasPersistidas[chaveSecao] =
                secaoDesativada;

            interruptor.checked = secaoDesativada;

            caixas.forEach(function (caixa, indice) {
                caixa.disabled = secaoDesativada;

                caixa.checked = secaoDesativada
                    ? false
                    : estadoAnterior[indice];

                caixa.addEventListener('change', function () {
                    preferenciasPersistidas[caixa.dataset.chave] =
                        caixa.checked;

                    estadoAnterior[indice] = caixa.checked;

                    salvarPreferencias();
                });
            });

            interruptor.addEventListener(
                'change',
                function () {

                    if (interruptor.checked) {
                        estadoAnterior = caixas.map(
                            function (caixa) {
                                preferenciasPersistidas[
                                    caixa.dataset.chave
                                ] = caixa.checked;

                                return caixa.checked;
                            }
                        );

                        preferenciasPersistidas[chaveSecao] = true;

                        caixas.forEach(function (caixa) {
                            caixa.checked = false;
                            caixa.disabled = true;
                        });

                    } else {
                        preferenciasPersistidas[chaveSecao] = false;

                        caixas.forEach(
                            function (caixa, indice) {
                                caixa.disabled = false;
                                caixa.checked =
                                    estadoAnterior[indice];

                                preferenciasPersistidas[
                                    caixa.dataset.chave
                                ] = estadoAnterior[indice];
                            }
                        );
                    }

                    salvarPreferencias();
                }
            );
        });
    }

    async function carregarPreferencias() {
        try {
            const resposta = await fetch(
                '/configuracoes/notificacoes/preferencias'
            );

            if (resposta.status === 401) {
                window.location.href = '/login';
                return;
            }

            if (!resposta.ok) {
                throw new Error(
                    'Não foi possível carregar as preferências.'
                );
            }

            preferenciasPersistidas = await resposta.json();

        } catch (erro) {
            console.error(
                'Erro ao carregar preferências de notificações:',
                erro
            );

            preferenciasPersistidas = {};
        }

        configurarSecoes();
    }

    function marcarLink(id) {
        links.forEach(function (link) {
            link.classList.toggle(
                'ativo',
                link.getAttribute('href') === '#' + id
            );
        });
    }

    links.forEach(function (link) {
        link.addEventListener('click', function () {
            marcarLink(
                link.getAttribute('href').substring(1)
            );
        });
    });

    if ('IntersectionObserver' in window) {
        const observador =
            new IntersectionObserver(
                function (entradas) {
                    entradas.forEach(function (entrada) {
                        if (entrada.isIntersecting) {
                            marcarLink(entrada.target.id);
                        }
                    });
                },
                {
                    rootMargin: '-40% 0px -55% 0px'
                }
            );

        secoes.forEach(function (secao) {
            observador.observe(secao);
        });
    }

    const idInicial =
        window.location.hash.substring(1);

    if (
        idInicial &&
        document.getElementById(idInicial)
    ) {
        marcarLink(idInicial);
    }

    carregarPreferencias();
});
