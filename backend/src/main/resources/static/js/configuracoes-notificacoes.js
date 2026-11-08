document.addEventListener('DOMContentLoaded', function () {
    const chaveEstadoAnterior = 'zatts-notificacoes-estado-anterior';
    const todas = document.getElementById('cfgNotifTodas');
    const secoes = Array.from(
        document.querySelectorAll('.cfg-notif-secao .interruptor')
    );

    function carregarEstadoAnterior() {
        const estadoSalvo = sessionStorage.getItem(chaveEstadoAnterior);
        if (!estadoSalvo) {
            return null;
        }
        try {
            const estado = JSON.parse(estadoSalvo);

            if (
                Array.isArray(estado) &&
                estado.length === secoes.length
            ) {
                return estado;
            }
        } catch (erro) {
            console.error(
                'Erro ao carregar estado anterior das notificações:',
                erro
            );
        }
        return null;
    }
    function salvarEstadoAnterior(estado) {
        sessionStorage.setItem(
            chaveEstadoAnterior,
            JSON.stringify(estado)
        );
    }
    let estadoAnterior =
        carregarEstadoAnterior() ||
        secoes.map(function (secao) {
            return secao.checked;
        });

    async function salvarNotificacoes() {
        const dados = new URLSearchParams();
        secoes.forEach(function (secao) {
            dados.append(secao.name, secao.checked);
        });
        try {
            const resposta = await fetch(
                '/configuracoes/notificacoes',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type':
                            'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: dados
                }
            );
            if (resposta.status === 401) {
                window.location.href = '/login';
                return;
            }
            if (!resposta.ok) {
                console.error(
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

    const todasDesligadas = secoes.every(function (secao) {
        return !secao.checked;
    });
    if (todasDesligadas) {
        todas.checked = true;

        secoes.forEach(function (secao) {
            secao.disabled = true;
        });
    } else {
        todas.checked = false;

        estadoAnterior = secoes.map(function (secao) {
            return secao.checked;
        });

        salvarEstadoAnterior(estadoAnterior);
    }
    secoes.forEach(function (secao) {
        secao.addEventListener('change', function () {
            estadoAnterior = secoes.map(function (item) {
                return item.checked;
            });

            salvarEstadoAnterior(estadoAnterior);
            salvarNotificacoes();
        });
    });
    todas.addEventListener('change', function () {
        if (todas.checked) {
            estadoAnterior = secoes.map(function (secao) {
                return secao.checked;
            });
            salvarEstadoAnterior(estadoAnterior);

            secoes.forEach(function (secao) {
                secao.checked = false;
                secao.disabled = true;
            });
            salvarNotificacoes();
            return;
        }
        const estadoSalvo = carregarEstadoAnterior();
        if (estadoSalvo) {
            estadoAnterior = estadoSalvo;
        }
        const algumaLigada = estadoAnterior.some(Boolean);
        secoes.forEach(function (secao, indice) {
            secao.disabled = false;

            secao.checked = algumaLigada
                ? estadoAnterior[indice]
                : true;
        });
        estadoAnterior = secoes.map(function (secao) {
            return secao.checked;
        });
        salvarEstadoAnterior(estadoAnterior);
        salvarNotificacoes();
    });
});
