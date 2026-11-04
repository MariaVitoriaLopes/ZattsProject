document.addEventListener('DOMContentLoaded', function () {
    const btnAdicionar = document.getElementById('btnAdicionarDispositivo');
    const btnCancelarNovo = document.getElementById('btnCancelarNovo');
    const formNovo = document.getElementById('formNovoDispositivo');
    const lista = document.getElementById('listaDispositivos');
    const vazio = document.getElementById('dispVazio');
    const campoNome = document.getElementById('dispNome');
    const campoCodigo = document.getElementById('dispCodigo');

    function atualizarVazio() {
        const total = lista.querySelectorAll('.disp-item').length;
        vazio.hidden = total > 0;
    }

    function abrirPainel() {
        formNovo.hidden = false;
        campoNome.focus();
    }

    function fecharPainel() {
        formNovo.hidden = true;
        formNovo.reset();
    }

    function criarItem(nome, codigo) {
        const item = document.createElement('li');
        item.className = 'disp-item';

        const icone = document.createElement('div');
        icone.className = 'disp-icone';
        const iconeSimbolo = document.createElement('span');
        iconeSimbolo.className = 'material-symbols-outlined';
        iconeSimbolo.textContent = 'sensors';
        icone.appendChild(iconeSimbolo);

        const info = document.createElement('div');
        info.className = 'disp-info';
        const titulo = document.createElement('strong');
        titulo.className = 'disp-nome';
        titulo.textContent = nome;
        const detalhe = document.createElement('span');
        detalhe.className = 'disp-detalhe';
        detalhe.textContent = 'Código ' + codigo + ' · Aguardando primeira leitura';
        info.appendChild(titulo);
        info.appendChild(detalhe);

        const status = document.createElement('span');
        status.className = 'disp-status disp-conectado';
        const ponto = document.createElement('span');
        ponto.className = 'disp-ponto';
        status.appendChild(ponto);
        status.appendChild(document.createTextNode('Conectado'));

        const acoes = document.createElement('div');
        acoes.className = 'disp-acoes';

        const btnRenomear = document.createElement('button');
        btnRenomear.type = 'button';
        btnRenomear.className = 'disp-btn-icone';
        btnRenomear.setAttribute('data-acao', 'renomear');
        btnRenomear.setAttribute('aria-label', 'Renomear dispositivo');
        const iconeEditar = document.createElement('span');
        iconeEditar.className = 'material-symbols-outlined';
        iconeEditar.textContent = 'edit';
        btnRenomear.appendChild(iconeEditar);

        const btnRemover = document.createElement('button');
        btnRemover.type = 'button';
        btnRemover.className = 'disp-btn-icone disp-btn-remover';
        btnRemover.setAttribute('data-acao', 'remover');
        btnRemover.setAttribute('aria-label', 'Remover dispositivo');
        const iconeExcluir = document.createElement('span');
        iconeExcluir.className = 'material-symbols-outlined';
        iconeExcluir.textContent = 'delete';
        btnRemover.appendChild(iconeExcluir);

        acoes.appendChild(btnRenomear);
        acoes.appendChild(btnRemover);

        item.appendChild(icone);
        item.appendChild(info);
        item.appendChild(status);
        item.appendChild(acoes);

        return item;
    }

    function iniciarRenomear(item, botao) {
        const titulo = item.querySelector('.disp-nome');
        const nomeAtual = titulo.textContent;
        const icone = botao.querySelector('.material-symbols-outlined');

        const entrada = document.createElement('input');
        entrada.type = 'text';
        entrada.className = 'disp-nome-edicao';
        entrada.value = nomeAtual;
        entrada.maxLength = 60;

        titulo.replaceWith(entrada);
        icone.textContent = 'check';
        botao.setAttribute('data-acao', 'confirmar');
        botao.setAttribute('aria-label', 'Confirmar nome');
        entrada.focus();
        entrada.select();

        function finalizar(salvar) {
            const novoNome = entrada.value.trim();
            titulo.textContent = salvar && novoNome ? novoNome : nomeAtual;
            entrada.replaceWith(titulo);
            icone.textContent = 'edit';
            botao.setAttribute('data-acao', 'renomear');
            botao.setAttribute('aria-label', 'Renomear dispositivo');
        }

        entrada.addEventListener('keydown', function (evento) {
            if (evento.key === 'Enter') {
                evento.preventDefault();
                finalizar(true);
            }

            if (evento.key === 'Escape') {
                finalizar(false);
            }
        });

        botao.addEventListener('click', function confirmar() {
            botao.removeEventListener('click', confirmar);
            finalizar(true);
        });
    }

    btnAdicionar.addEventListener('click', abrirPainel);
    btnCancelarNovo.addEventListener('click', fecharPainel);

    formNovo.addEventListener('submit', function (evento) {
        evento.preventDefault();

        const nome = campoNome.value.trim();
        const codigo = campoCodigo.value.trim().toUpperCase();

        if (!nome || !codigo) {
            formNovo.reportValidity();
            return;
        }

        lista.appendChild(criarItem(nome, codigo));
        fecharPainel();
        atualizarVazio();
    });

    lista.addEventListener('click', function (evento) {
        const botao = evento.target.closest('.disp-btn-icone');

        if (!botao) {
            return;
        }

        const item = botao.closest('.disp-item');
        const acao = botao.getAttribute('data-acao');

        if (acao === 'renomear') {
            iniciarRenomear(item, botao);
        }

        if (acao === 'remover') {
            const nome = item.querySelector('.disp-nome, .disp-nome-edicao');
            const texto = nome.value || nome.textContent;

            if (confirm('Remover o dispositivo "' + texto + '"?')) {
                item.remove();
                atualizarVazio();
            }
        }
    });

    atualizarVazio();
});