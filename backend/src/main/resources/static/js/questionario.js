const etapas = document.querySelectorAll('.quest-pergunta');
const barra = document.querySelector('.quest-progresso-barra');
const progresso = document.querySelector('.quest-progresso');
const btnAnterior = document.getElementById('btnAnterior');
const btnProxima = document.getElementById('btnProxima');
const textoProxima = document.getElementById('textoProxima');
const metaSim = document.getElementById('metaSim');
const metaNao = document.getElementById('metaNao');
const valorMeta = document.getElementById('valorMeta');

let atual = 0;

const nomeDoMes = new Intl.DateTimeFormat('pt-BR', { month: 'long' });

document.querySelectorAll('[data-meses-atras]').forEach(function (el) {
    const data = new Date();
    data.setDate(1);
    data.setMonth(data.getMonth() - Number(el.dataset.mesesAtras));
    el.textContent = nomeDoMes.format(data);
});

function mostrarEtapa() {
    etapas.forEach(function (etapa, i) {
        etapa.classList.toggle('ativa', i === atual);
    });

    const porcentagem = Math.round(((atual + 1) / etapas.length) * 100);
    barra.style.width = porcentagem + '%';
    progresso.setAttribute('aria-valuenow', porcentagem);

    const ultima = atual === etapas.length - 1;
    btnAnterior.disabled = atual === 0;
    textoProxima.textContent = ultima ? 'Finalizar' : 'Próxima';
    btnProxima.type = ultima ? 'submit' : 'button';
}

function etapaValida() {
    const campos = etapas[atual].querySelectorAll('input, select');

    for (const campo of campos) {
        if (!campo.reportValidity()) {
            return false;
        }
    }

    return true;
}

btnProxima.addEventListener('click', function () {
    if (atual === etapas.length - 1) {
        return;
    }

    if (!etapaValida()) {
        return;
    }

    atual++;
    mostrarEtapa();
});

btnAnterior.addEventListener('click', function () {
    if (atual > 0) {
        atual--;
        mostrarEtapa();
    }
});

[metaSim, metaNao].forEach(function (radio) {
    radio.addEventListener('change', function () {
        valorMeta.required = metaSim.checked;

        if (!metaSim.checked) {
            valorMeta.value = '';
        }
    });
});

mostrarEtapa();