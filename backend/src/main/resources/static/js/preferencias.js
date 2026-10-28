if (!window.zattsTema) {
    throw new Error('tema.js nao foi carregado antes do preferencias.js');
}

const radiosTema = document.querySelectorAll('input[name="tema"]');
const radiosCor = document.querySelectorAll('input[name="cor"]');
const seletorCor = document.getElementById('corPersonalizada');
const radioOutra = document.querySelector('input[name="cor"][value="outra"]');
const bolinhaOutra = document.getElementById('bolinhaOutra');
const selectIdioma = document.getElementById('idioma');

const temaSalvo = zattsTema.ler('zatts-tema', 'automatico');
const corSalva = zattsTema.ler('zatts-cor', 'padrao');
const corPersonalizadaSalva = zattsTema.ler('zatts-cor-personalizada', '#E6FB1A');
const idiomaSalvo = zattsTema.ler('zatts-idioma', 'pt-BR');

function pintarBolinha(cor) {
    bolinhaOutra.style.background = cor;
}

radiosTema.forEach(function (radio) {
    radio.checked = radio.value === temaSalvo;

    radio.addEventListener('change', function () {
        zattsTema.definirTema(radio.value);
    });
});

radiosCor.forEach(function (radio) {
    radio.checked = radio.value === corSalva;

    radio.addEventListener('change', function () {
        zattsTema.definirCor(radio.value, seletorCor.value);
    });
});

seletorCor.value = corPersonalizadaSalva;

if (corSalva === 'outra') {
    pintarBolinha(corPersonalizadaSalva);
}

seletorCor.addEventListener('click', function () {
    radioOutra.checked = true;
    zattsTema.definirCor('outra', seletorCor.value);
    pintarBolinha(seletorCor.value);
});

seletorCor.addEventListener('input', function () {
    radioOutra.checked = true;
    zattsTema.definirCor('outra', seletorCor.value);
    pintarBolinha(seletorCor.value);
});

selectIdioma.value = idiomaSalvo;

selectIdioma.addEventListener('change', function () {
    zattsTema.gravar('zatts-idioma', selectIdioma.value);
});