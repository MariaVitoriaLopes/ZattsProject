document.addEventListener('DOMContentLoaded', function () {
    const busca = document.getElementById('ajudaBusca');
    const perguntas = document.querySelectorAll('.ajuda-pergunta');
    const vazio = document.getElementById('ajudaVazio');
    const form = document.getElementById('formSuporte');
    const mensagem = document.getElementById('ajudaMensagem');
    const contador = document.getElementById('ajudaContador');
    const botao = document.getElementById('btnEnviarSuporte');
    const retorno = document.getElementById('ajudaRetorno');

    let temporizador = null;

    function normalizar(texto) {
        return texto
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '');
    }

    function filtrar() {
        const termo = normalizar(busca.value.trim());
        let visiveis = 0;

        perguntas.forEach(function (pergunta) {
            const combina = termo === '' || normalizar(pergunta.textContent).includes(termo);

            pergunta.hidden = !combina;

            if (combina) {
                visiveis++;
            }

            if (termo === '') {
                pergunta.open = false;
            }
        });

        vazio.hidden = visiveis > 0;
    }

    function atualizarContador() {
        contador.textContent = mensagem.value.length + ' / ' + mensagem.maxLength;
    }

    function mostrarRetorno(texto, erro) {
        clearTimeout(temporizador);

        retorno.textContent = texto;
        retorno.classList.toggle('ajuda-retorno-erro', Boolean(erro));
        retorno.hidden = false;

        temporizador = setTimeout(function () {
            retorno.hidden = true;
        }, 6000);
    }

    busca.addEventListener('input', filtrar);
    mensagem.addEventListener('input', atualizarContador);

    form.addEventListener('submit', async function (evento) {
        evento.preventDefault();

        if (!form.checkValidity()) {
            form.reportValidity();
            return;
        }

        botao.disabled = true;

        try {
            const dados = new FormData(form);

            const resposta = await fetch(form.action, {
                method: 'POST',
                body: dados
            });

            const retorno = await resposta.json();

            if (!resposta.ok) {
                throw new Error(
                    retorno.erro || 'Não foi possível enviar a mensagem.'
                );
            }

            form.reset();
            atualizarContador();

            mostrarRetorno(
                retorno.mensagem || 'Mensagem enviada. Responderemos pelo seu e-mail.',
                false
            );

        } catch (erro) {
            mostrarRetorno(
                erro.message || 'Não foi possível enviar a mensagem. Tente novamente.',
                true
            );

        } finally {
            botao.disabled = false;
        }
    });

    atualizarContador();
});