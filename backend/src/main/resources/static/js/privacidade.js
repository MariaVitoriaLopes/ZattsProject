document.addEventListener('DOMContentLoaded', function () {
    const interruptores = document.querySelectorAll('.priv-interruptor');
    const btnExportar = document.getElementById('btnExportarDados');
    const btnApagar = document.getElementById('btnApagarHistorico');
    const retorno = document.getElementById('privRetorno');

    let temporizador = null;

    function mostrarRetorno(texto, erro) {
        clearTimeout(temporizador);

        retorno.textContent = texto;
        retorno.classList.toggle('priv-retorno-erro', Boolean(erro));
        retorno.hidden = false;

        temporizador = setTimeout(function () {
            retorno.hidden = true;
        }, 6000);
    }

    async function salvarPreferencias() {
        const dados = new URLSearchParams();

        dados.append(
            'compartilharDadosUso',
            document.getElementById('privUsoAnonimo').checked
        );

        dados.append(
            'recomendacoesPersonalizadas',
            document.getElementById('privRecomendacoes').checked
        );

        dados.append(
            'comparacaoResidencias',
            document.getElementById('privComparativo').checked
        );

        try {
            const resposta = await fetch(
                '/configuracoes/privacidade',
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

            const retornoBackend = await resposta.json();

            if (!resposta.ok) {
                mostrarRetorno(
                    retornoBackend.erro ||
                    'Não foi possível salvar suas preferências.',
                    true
                );
                return;
            }

            mostrarRetorno(
                retornoBackend.mensagem ||
                'Preferências salvas com sucesso.',
                false
            );

        } catch (erro) {
            console.error(
                'Erro ao salvar preferências de privacidade:',
                erro
            );

            mostrarRetorno(
                'Não foi possível conectar ao servidor.',
                true
            );
        }
    }

    interruptores.forEach(function (interruptor) {
        interruptor.addEventListener(
            'change',
            salvarPreferencias
        );
    });

    btnExportar.addEventListener('click', async function () {
        if (!confirm('Deseja exportar uma cópia dos seus dados?')) {
            return;
        }

        btnExportar.disabled = true;

        try {
            const resposta = await fetch(
                '/configuracoes/privacidade/exportar'
            );

            if (resposta.status === 401) {
                window.location.href = '/login';
                return;
            }

            if (!resposta.ok) {
                const retornoBackend = await resposta.json();

                mostrarRetorno(
                    retornoBackend.erro ||
                    'Não foi possível exportar seus dados.',
                    true
                );
                return;
            }

            const arquivo = await resposta.blob();
            const url = URL.createObjectURL(arquivo);

            const link = document.createElement('a');

            link.href = url;
            link.download = 'zatts-dados.json';

            document.body.appendChild(link);
            link.click();
            link.remove();

            URL.revokeObjectURL(url);

            mostrarRetorno(
                'Dados exportados com sucesso.',
                false
            );

        } catch (erro) {
            console.error(
                'Erro ao exportar dados:',
                erro
            );

            mostrarRetorno(
                'Não foi possível conectar ao servidor.',
                true
            );

        } finally {
            btnExportar.disabled = false;
        }
    });

    btnApagar.addEventListener('click', async function () {
        const confirmar = confirm(
            'Tem certeza que deseja apagar o histórico de consumo?\n\n' +
            'Todas as leituras salvas serão removidas e a ação não pode ser desfeita.'
        );

        if (!confirmar) {
            return;
        }

        btnApagar.disabled = true;

        try {
            const resposta = await fetch(
                '/configuracoes/privacidade/apagar-historico',
                {
                    method: 'POST'
                }
            );

            if (resposta.status === 401) {
                window.location.href = '/login';
                return;
            }

            const retornoBackend = await resposta.json();

            if (!resposta.ok) {
                mostrarRetorno(
                    retornoBackend.erro ||
                    'Não foi possível apagar o histórico de consumo.',
                    true
                );
                return;
            }

            mostrarRetorno(
                retornoBackend.mensagem ||
                'Histórico de consumo apagado com sucesso.',
                false
            );

        } catch (erro) {
            console.error(
                'Erro ao apagar histórico de consumo:',
                erro
            );

            mostrarRetorno(
                'Não foi possível conectar ao servidor.',
                true
            );

        } finally {
            btnApagar.disabled = false;
        }
    });
});
