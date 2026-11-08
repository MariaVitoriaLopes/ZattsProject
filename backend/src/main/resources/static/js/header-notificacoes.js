document.addEventListener('DOMContentLoaded', async function () {
    const ponto = document.getElementById('pontoNotificacoes');

    if (!ponto) {
        return;
    }

    try {
        const resposta = await fetch(
            '/notificacoes/nao-lidas/contagem'
        );

        if (resposta.status === 401) {
            return;
        }

        if (!resposta.ok) {
            console.error(
                'Não foi possível consultar as notificações não lidas.'
            );
            return;
        }

        const dados = await resposta.json();

        ponto.hidden = !(
            dados.quantidade &&
            dados.quantidade > 0
        );

    } catch (erro) {
        console.error(
            'Erro ao consultar notificações não lidas:',
            erro
        );
    }
});