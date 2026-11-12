document.addEventListener('DOMContentLoaded', function () {
    const ponto = document.getElementById('pontoNotificacoes');

    if (!ponto) {
        return;
    }

    async function atualizar() {
        try {
            const resposta = await fetch(
                '/notificacoes/nao-lidas/contagem',
                { credentials: 'same-origin' }
            );

            if (!resposta.ok) {
                return;
            }

            const dados = await resposta.json();

            ponto.hidden = !(dados.quantidade > 0);

        } catch (erro) {
            console.error(
                'Erro ao consultar notificações não lidas:',
                erro
            );
        }
    }

    atualizar();

    setInterval(atualizar, 15000);

    document.addEventListener('visibilitychange', function () {
        if (!document.hidden) {
            atualizar();
        }
    });
});