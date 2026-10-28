(function () {
    const raiz = document.documentElement;
    const escuroDoSistema = window.matchMedia('(prefers-color-scheme: dark)');

    function ler(chave, padrao) {
        try {
            return localStorage.getItem(chave) || padrao;
        } catch (erro) {
            return padrao;
        }
    }

    function gravar(chave, valor) {
        try {
            localStorage.setItem(chave, valor);
        } catch (erro) {
            return;
        }
    }

    function aplicarTema(modo) {
        const escuro = modo === 'escuro' || (modo === 'automatico' && escuroDoSistema.matches);
        raiz.setAttribute('data-tema', escuro ? 'escuro' : 'claro');
        document.dispatchEvent(new Event('zatts:tema'));
    }

    function aplicarCor(cor, personalizada) {
        raiz.setAttribute('data-cor', cor);

        if (cor === 'outra') {
            raiz.style.setProperty('--cor-primaria-400', personalizada);
        } else {
            raiz.style.removeProperty('--cor-primaria-400');
        }

        document.dispatchEvent(new Event('zatts:tema'));
    }

    window.zattsTema = {
        ler: ler,
        gravar: gravar,

        definirTema: function (modo) {
            gravar('zatts-tema', modo);
            aplicarTema(modo);
        },

        definirCor: function (cor, personalizada) {
            const valor = personalizada || ler('zatts-cor-personalizada', '#E6FB1A');

            gravar('zatts-cor', cor);

            if (cor === 'outra') {
                gravar('zatts-cor-personalizada', valor);
            }

            aplicarCor(cor, valor);
        }
    };

    aplicarTema(ler('zatts-tema', 'automatico'));
    aplicarCor(ler('zatts-cor', 'padrao'), ler('zatts-cor-personalizada', '#E6FB1A'));

    escuroDoSistema.addEventListener('change', function () {
        if (ler('zatts-tema', 'automatico') === 'automatico') {
            aplicarTema('automatico');
        }
    });
})();