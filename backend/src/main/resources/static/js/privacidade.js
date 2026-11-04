document.addEventListener('DOMContentLoaded', function () {
    const interruptores = document.querySelectorAll('.priv-interruptor');
    const btnExportar = document.getElementById('btnExportarDados');
    const btnApagar = document.getElementById('btnApagarHistorico');
    const retorno = document.getElementById('privRetorno');

    let temporizador = null;

    function ler(chave, padrao) {
        if (window.zattsTema) {
            return window.zattsTema.ler(chave, padrao);
        }

        return padrao;
    }

    function gravar(chave, valor) {
        if (window.zattsTema) {
            window.zattsTema.gravar(chave, valor);
        }
    }

    function mostrarRetorno(texto, erro) {
        clearTimeout(temporizador);

        retorno.textContent = texto;
        retorno.classList.toggle('priv-retorno-erro', Boolean(erro));
        retorno.hidden = false;

        temporizador = setTimeout(function () {
            retorno.hidden = true;
        }, 6000);
    }

    interruptores.forEach(function (interruptor) {
        const chave = 'zatts-priv-' + interruptor.id;
        const salvo = ler(chave, null);

        if (salvo !== null) {
            interruptor.checked = salvo === 'sim';
        }

        interruptor.addEventListener('change', function () {
            gravar(chave, interruptor.checked ? 'sim' : 'nao');
        });
    });

    btnExportar.addEventListener('click', function () {
        if (!confirm('Deseja exportar uma cópia dos seus dados?')) {
            return;
        }

        btnExportar.disabled = true;

        setTimeout(function () {
            btnExportar.disabled = false;
            mostrarRetorno('Exportação solicitada com sucesso.', false);
        }, 800);
    });

    btnApagar.addEventListener('click', function () {
        const confirmar = confirm(
            'Tem certeza que deseja apagar o histórico de consumo?\n\n' +
            'Todas as leituras salvas serão removidas e a ação não pode ser desfeita.'
        );

        if (!confirmar) {
            return;
        }

        btnApagar.disabled = true;

        setTimeout(function () {
            btnApagar.disabled = false;
            mostrarRetorno('Histórico de consumo apagado.', false);
        }, 800);
    });
});