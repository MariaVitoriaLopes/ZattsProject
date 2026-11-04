document.addEventListener('DOMContentLoaded', function () {
    const secoes = document.querySelectorAll('.cfg-notif-secao');
    const links = document.querySelectorAll('.cfg-notif-menu a');

    secoes.forEach(function (secao) {
        const interruptor = secao.querySelector('.interruptor');
        const caixas = Array.from(
            secao.querySelectorAll('.cfg-notif-linha:not(.cfg-notif-cabecalho) input[type="checkbox"]')
        );

        let estadoAnterior = caixas.map(function (caixa) {
            return caixa.checked;
        });

        const todasDesmarcadas = caixas.every(function (caixa) {
            return !caixa.checked;
        });

        interruptor.checked = todasDesmarcadas;

        caixas.forEach(function (caixa) {
            caixa.disabled = todasDesmarcadas;
        });

        if (todasDesmarcadas) {
            estadoAnterior = caixas.map(function () {
                return true;
            });
        }

        interruptor.addEventListener('change', function () {
            if (interruptor.checked) {
                estadoAnterior = caixas.map(function (caixa) {
                    return caixa.checked;
                });

                caixas.forEach(function (caixa) {
                    caixa.checked = false;
                    caixa.disabled = true;
                });
            } else {
                caixas.forEach(function (caixa, indice) {
                    caixa.disabled = false;
                    caixa.checked = estadoAnterior[indice];
                });
            }
        });
    });

    function marcarLink(id) {
        links.forEach(function (link) {
            link.classList.toggle('ativo', link.getAttribute('href') === '#' + id);
        });
    }

    links.forEach(function (link) {
        link.addEventListener('click', function () {
            marcarLink(link.getAttribute('href').substring(1));
        });
    });

    if ('IntersectionObserver' in window) {
        const observador = new IntersectionObserver(function (entradas) {
            entradas.forEach(function (entrada) {
                if (entrada.isIntersecting) {
                    marcarLink(entrada.target.id);
                }
            });
        }, { rootMargin: '-40% 0px -55% 0px' });

        secoes.forEach(function (secao) {
            observador.observe(secao);
        });
    }

    const idInicial = window.location.hash.substring(1);

    if (idInicial && document.getElementById(idInicial)) {
        marcarLink(idInicial);
    }
});