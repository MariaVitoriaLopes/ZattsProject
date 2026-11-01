
const etapas = document.querySelectorAll('.quest-pergunta');
const barra = document.querySelector('.quest-progresso-barra');
const progresso = document.querySelector('.quest-progresso');
const btnAnterior = document.getElementById('btnAnterior');
const btnProxima = document.getElementById('btnProxima');
const textoProxima = document.getElementById('textoProxima');
const form = document.getElementById('formQuestionario');

const metaSim = document.getElementById('metaSim');
const metaNao = document.getElementById('metaNao');
const tipoConsumo = document.getElementById('tipoConsumo');
const tipoValor = document.getElementById('tipoValor');
const valorMeta = document.getElementById('valorMeta');

const escolhaTipoMeta = document.getElementById('escolhaTipoMeta');
const campoValorMeta = document.getElementById('campoValorMeta');
const rotuloValorMeta = document.getElementById('rotuloValorMeta');
const unidadeMeta = document.getElementById('unidadeMeta');

let atual = 0;
let enviando = false;

form.noValidate = true;

// Atualiza automaticamente os meses das contas
const nomeDoMes = new Intl.DateTimeFormat('pt-BR', {
    month: 'long'
});

document.querySelectorAll('[data-meses-atras]').forEach(function (el) {
    const data = new Date();
    data.setDate(1);
    data.setMonth(data.getMonth() - Number(el.dataset.mesesAtras));
    el.textContent = nomeDoMes.format(data);
});

// Mostra a pergunta atual
function mostrarEtapa() {

    etapas.forEach(function (etapa, i) {
        etapa.classList.toggle('ativa', i === atual);
    });

    const porcentagem = Math.round(
        ((atual + 1) / etapas.length) * 100
    );

    barra.style.width = porcentagem + '%';
    progresso.setAttribute('aria-valuenow', porcentagem);

    const ultima = atual === etapas.length - 1;

    btnAnterior.disabled = atual === 0;
    textoProxima.textContent = ultima ? 'Finalizar' : 'Próxima';

    btnProxima.type = 'button';
}

// Atualiza os campos condicionais da meta
function atualizarMeta() {

    const desejaMeta = metaSim.checked;
    const tipoSelecionado = tipoConsumo.checked || tipoValor.checked;

    escolhaTipoMeta.hidden = !desejaMeta;
    campoValorMeta.hidden = !desejaMeta || !tipoSelecionado;

    tipoConsumo.required = desejaMeta;
    tipoValor.required = desejaMeta;
    valorMeta.required = desejaMeta && tipoSelecionado;

    tipoConsumo.disabled = !desejaMeta;
    tipoValor.disabled = !desejaMeta;
    valorMeta.disabled = !desejaMeta || !tipoSelecionado;

    if (!desejaMeta) {
        tipoConsumo.checked = false;
        tipoValor.checked = false;
        valorMeta.value = '';
    }

    if (tipoConsumo.checked) {
        rotuloValorMeta.textContent = 'Qual é sua meta de consumo mensal?';
        unidadeMeta.textContent = 'kWh';
    } else if (tipoValor.checked) {
        rotuloValorMeta.textContent = 'Qual é sua meta de gasto mensal?';
        unidadeMeta.textContent = 'R$';
    }
}

// Valida a pergunta atual
function etapaValida() {

    const campos = etapas[atual].querySelectorAll('input, select');

    for (const campo of campos) {

        if (campo.disabled) {
            continue;
        }

        if (!campo.checkValidity()) {
            campo.reportValidity();
            return false;
        }
    }

    // Verifica os campos condicionais da última pergunta
    if (atual === etapas.length - 1 && metaSim.checked) {

        if (!tipoConsumo.checked && !tipoValor.checked) {
            alert('Selecione se sua meta é de consumo ou valor.');
            return false;
        }

        if (!valorMeta.value.trim()) {
            valorMeta.reportValidity();
            return false;
        }

        const numero = Number(valorMeta.value.replace(',', '.'));

        if (!Number.isFinite(numero) || numero <= 0) {
            alert('Informe uma meta maior que zero.');
            valorMeta.focus();
            return false;
        }
    }

    return true;
}

// Avança ou finaliza o questionário
btnProxima.addEventListener('click', function () {

    if (enviando) {
        return;
    }

    if (!etapaValida()) {
        return;
    }

    if (atual === etapas.length - 1) {

        for (let i = 0; i < etapas.length; i++) {

            atual = i;
            mostrarEtapa();

            if (!etapaValida()) {
                return;
            }
        }

        atual = etapas.length - 1;
        mostrarEtapa();

        enviando = true;
        btnProxima.disabled = true;
        textoProxima.textContent = 'Salvando...';

        form.requestSubmit();
        return;
    }

    atual++;
    mostrarEtapa();
});

// Volta para a pergunta anterior
btnAnterior.addEventListener('click', function () {

    if (atual > 0 && !enviando) {
        atual--;
        mostrarEtapa();
    }
});

// Atualiza a meta quando o usuário altera a escolha
[metaSim, metaNao, tipoConsumo, tipoValor].forEach(function (radio) {

    radio.addEventListener('change', function () {
        atualizarMeta();
    });
});

atualizarMeta();
mostrarEtapa();
