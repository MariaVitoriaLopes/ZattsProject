document.addEventListener('DOMContentLoaded', function () {
    const lista = document.getElementById('listaDispositivos');
    const vazio = document.getElementById('dispVazio');
    const contagem = document.getElementById('dispContagem');

    function atualizar() {
        const total = lista.querySelectorAll('.disp-item').length;

        contagem.textContent = total === 1 ? '1 dispositivo' : total + ' dispositivos';
        vazio.hidden = total > 0;
    }

    lista.addEventListener('click', function (evento) {
        const botao = evento.target.closest('[data-acao="desconectar"]');

        if (!botao) {
            return;
        }

        const item = botao.closest('.disp-item');
        const nome = item.querySelector('.disp-nome').textContent;

        if (!confirm('Desconectar "' + nome + '" da sua conta?')) {
            return;
        }

        botao.disabled = true;

        setTimeout(function () {
            item.remove();
            atualizar();
        }, 400);
    });

    atualizar();
});