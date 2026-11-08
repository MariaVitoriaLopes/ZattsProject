document.addEventListener('DOMContentLoaded', function () {
    const todas = document.getElementById('cfgNotifTodas');
    const secoes = Array.from(document.querySelectorAll('.cfg-notif-secao .interruptor'));

    let estadoAnterior = secoes.map(function (secao) {
        return secao.checked;
    });

    const todasDesligadas = secoes.every(function (secao) {
        return !secao.checked;
    });

    if (todasDesligadas) {
        todas.checked = true;

        estadoAnterior = secoes.map(function () {
            return true;
        });

        secoes.forEach(function (secao) {
            secao.disabled = true;
        });
    }

    todas.addEventListener('change', function () {
        if (todas.checked) {
            estadoAnterior = secoes.map(function (secao) {
                return secao.checked;
            });

            secoes.forEach(function (secao) {
                secao.checked = false;
                secao.disabled = true;
            });

            return;
        }

        const algumaLigada = estadoAnterior.some(Boolean);

        secoes.forEach(function (secao, indice) {
            secao.disabled = false;
            secao.checked = algumaLigada ? estadoAnterior[indice] : true;
        });
    });
});