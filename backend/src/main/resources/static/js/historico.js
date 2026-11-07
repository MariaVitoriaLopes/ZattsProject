document.addEventListener('DOMContentLoaded', function () {
    const pagina = document.querySelector('.hist-pagina');
    const usuarioId = pagina.dataset.usuarioId;

    const abas = document.querySelectorAll('.hist-aba');
    const vistas = {
        semanal: document.getElementById('vistaSemanal'),
        mensal: document.getElementById('vistaMensal'),
        comparativo: document.getElementById('vistaComparativo')
    };

    const seletorMensal = document.getElementById('mensalSeletor');
    const estadoMensal = document.getElementById('mensalEstado');
    const canvasMensal = document.getElementById('graficoMensal');

    const seletorSemanal = document.getElementById('semanalSeletor');
    const estadoSemanal = document.getElementById('semanalEstado');
    const canvasSemanal = document.getElementById('graficoSemanal');

    let consumos = [];
    let graficoMensal = null;
    let ultimoMensal = null;
    let graficoSemanal = null;
    let ultimaSemanal = null;

    const seletorComparativo = document.getElementById('comparativoSeletor');
    const estadoComparativo = document.getElementById('comparativoEstado');
    const canvasComparativo = document.getElementById('graficoComparativo');
    let graficoComparativo = null;
    let ultimoComparativo = null;

    function paraData(valor) {
        if (Array.isArray(valor)) {
            return new Date(valor[0], valor[1] - 1, valor[2], valor[3] || 0, valor[4] || 0, valor[5] || 0);
        }

        return new Date(valor);
    }

    function chaveMes(data) {
        return data.getFullYear() + '-' + String(data.getMonth() + 1).padStart(2, '0');
    }

    function chaveAnterior(chave) {
        const partes = chave.split('-');
        return chaveMes(new Date(Number(partes[0]), Number(partes[1]) - 2, 1));
    }

    function nomeMes(ano, mes) {
        const texto = new Date(ano, mes - 1, 1).toLocaleDateString('pt-BR', { month: 'long' });
        return texto.charAt(0).toUpperCase() + texto.slice(1);
    }

    function formatarNumero(valor, minimo, maximo) {
        return Number(valor).toLocaleString('pt-BR', {
            minimumFractionDigits: minimo,
            maximumFractionDigits: maximo
        });
    }

    function definirEstado(texto) {
        estadoMensal.textContent = texto || '';
        estadoMensal.hidden = !texto;
    }

    function prepararLeituras(lista) {
        return lista
            .map(function (item) {
                return {
                    data: paraData(item.dataHora),
                    kwh: Number(item.consumoKwh) || 0,
                    reais: Number(item.valorEstimadoReais) || 0
                };
            })
            .filter(function (item) {
                return !isNaN(item.data.getTime());
            });
    }

    function totaisDoMes(chave) {
        const partes = chave.split('-');
        const ano = Number(partes[0]);
        const mes = Number(partes[1]);
        const dias = new Date(ano, mes, 0).getDate();
        const kwhPorDia = new Array(dias).fill(0);

        let kwh = 0;
        let reais = 0;
        let leituras = 0;

        consumos.forEach(function (item) {
            if (chaveMes(item.data) !== chave) {
                return;
            }

            kwhPorDia[item.data.getDate() - 1] += item.kwh;
            kwh += item.kwh;
            reais += item.reais;
            leituras++;
        });

        return { ano: ano, mes: mes, dias: dias, kwhPorDia: kwhPorDia, kwh: kwh, reais: reais, leituras: leituras };
    }

    function mesesDisponiveis() {
        const chaves = new Set([chaveMes(new Date())]);

        consumos.forEach(function (item) {
            chaves.add(chaveMes(item.data));
        });

        return Array.from(chaves).sort().reverse();
    }

    function montarSeletor() {
        seletorMensal.innerHTML = '';

        mesesDisponiveis().forEach(function (chave) {
            const partes = chave.split('-');
            const opcao = document.createElement('option');

            opcao.value = chave;
            opcao.textContent = 'Mês de ' + nomeMes(Number(partes[0]), Number(partes[1])) + ' de ' + partes[0];
            seletorMensal.appendChild(opcao);
        });
    }

    function desenharMensal() {
        if (!ultimoMensal || typeof Chart === 'undefined') {
            return;
        }

        const cores = coresDoTema();
        const mesTexto = String(ultimoMensal.mes).padStart(2, '0');

        const rotulos = ultimoMensal.kwhPorDia.map(function (valor, indice) {
            return String(indice + 1).padStart(2, '0') + '/' + mesTexto;
        });

        const valores = ultimoMensal.kwhPorDia.map(function (valor) {
            return Math.round(valor * 100) / 100;
        });

        if (graficoMensal) {
            graficoMensal.destroy();
        }

        Chart.defaults.font.family = "'Inter', sans-serif";
        Chart.defaults.color = cores.texto;

        graficoMensal = new Chart(canvasMensal, {
            type: 'bar',
            data: {
                labels: rotulos,
                datasets: [{
                    data: valores,
                    backgroundColor: cores.destaque,
                    borderRadius: 6,
                    borderSkipped: false,
                    maxBarThickness: 40
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: cores.tooltip,
                        callbacks: {
                            label: function (contexto) {
                                return formatarNumero(contexto.raw, 0, 2) + ' kWh';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: { maxRotation: 0 }
                    },
                    y: {
                        beginAtZero: true,
                        border: { display: false },
                        grid: { color: cores.grade }
                    }
                }
            }
        });
    }

    function atualizarMensal() {
        const chave = seletorMensal.value;
        const atual = totaisDoMes(chave);
        const anterior = totaisDoMes(chaveAnterior(chave));

        document.getElementById('mensalKwh').textContent = formatarNumero(atual.kwh, 0, 1);
        document.getElementById('mensalCusto').textContent = formatarNumero(atual.reais, 2, 2);
        document.getElementById('mensalRotuloAtual').textContent = 'Mês de ' + nomeMes(atual.ano, atual.mes);
        document.getElementById('mensalValorAtual').textContent = formatarReais(atual.reais);
        document.getElementById('mensalValorAnterior').textContent = formatarReais(anterior.reais);

        definirEstado(atual.leituras === 0 ? 'Sem leituras neste mês.' : '');

        ultimoMensal = atual;
        desenharMensal();
    }
    const MESES_CURTOS = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
    const DIAS_SEMANA = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];

    function doisDigitos(numero) {
        return String(numero).padStart(2, '0');
    }

    function chaveDia(data) {
        return data.getFullYear() + '-' + doisDigitos(data.getMonth() + 1) + '-' + doisDigitos(data.getDate());
    }

    function dataDaChave(chave) {
        const partes = chave.split('-');
        return new Date(Number(partes[0]), Number(partes[1]) - 1, Number(partes[2]));
    }

    function segundaDaSemana(data) {
        const inicio = new Date(data.getFullYear(), data.getMonth(), data.getDate());
        inicio.setDate(inicio.getDate() - ((inicio.getDay() + 6) % 7));
        return inicio;
    }

    function chaveSemanaAnterior(chave) {
        const inicio = dataDaChave(chave);
        return chaveDia(new Date(inicio.getFullYear(), inicio.getMonth(), inicio.getDate() - 7));
    }

    function diasDaSemana(chave) {
        const inicio = dataDaChave(chave);
        const dias = [];

        for (let i = 0; i < 7; i++) {
            dias.push(new Date(inicio.getFullYear(), inicio.getMonth(), inicio.getDate() + i));
        }

        return dias;
    }

    function intervaloCurto(dias) {
        const primeiro = dias[0];
        const ultimo = dias[6];
        const inicio = doisDigitos(primeiro.getDate()) + '/' + MESES_CURTOS[primeiro.getMonth()];
        const fim = doisDigitos(ultimo.getDate()) + '/' + MESES_CURTOS[ultimo.getMonth()];

        if (primeiro.getFullYear() === ultimo.getFullYear()) {
            return inicio + ' - ' + fim + ' de ' + ultimo.getFullYear();
        }

        return inicio + ' de ' + primeiro.getFullYear() + ' - ' + fim + ' de ' + ultimo.getFullYear();
    }

    function intervaloLongo(dias) {
        const primeiro = dias[0];
        const ultimo = dias[6];

        return 'Semana de ' + primeiro.getDate() + ' de ' + MESES_CURTOS[primeiro.getMonth()] + '. - '
            + ultimo.getDate() + ' de ' + MESES_CURTOS[ultimo.getMonth()] + '. ' + ultimo.getFullYear();
    }

    function corComAlfa(cor, alfa) {
        const limpo = cor.replace('#', '');

        if (!/^([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/.test(limpo)) {
            return cor;
        }

        const completo = limpo.length === 3
            ? limpo.split('').map(function (c) { return c + c; }).join('')
            : limpo;

        const r = parseInt(completo.substring(0, 2), 16);
        const g = parseInt(completo.substring(2, 4), 16);
        const b = parseInt(completo.substring(4, 6), 16);

        return 'rgba(' + r + ',' + g + ',' + b + ',' + alfa + ')';
    }

    function definirEstadoSemanal(texto) {
        estadoSemanal.textContent = texto || '';
        estadoSemanal.hidden = !texto;
    }

    function totaisDaSemana(chave) {
        const dias = diasDaSemana(chave);
        const indices = {};

        dias.forEach(function (dia, indice) {
            indices[chaveDia(dia)] = indice;
        });

        const kwhPorDia = new Array(7).fill(0);
        let kwh = 0;
        let reais = 0;
        let leituras = 0;

        consumos.forEach(function (item) {
            const indice = indices[chaveDia(item.data)];

            if (indice === undefined) {
                return;
            }

            kwhPorDia[indice] += item.kwh;
            kwh += item.kwh;
            reais += item.reais;
            leituras++;
        });

        return { dias: dias, kwhPorDia: kwhPorDia, kwh: kwh, reais: reais, leituras: leituras };
    }

    function semanasDisponiveis() {
        const chaves = new Set([chaveDia(segundaDaSemana(new Date()))]);

        consumos.forEach(function (item) {
            chaves.add(chaveDia(segundaDaSemana(item.data)));
        });

        return Array.from(chaves).sort().reverse();
    }

    function montarSeletorSemanal() {
        seletorSemanal.innerHTML = '';

        semanasDisponiveis().forEach(function (chave) {
            const opcao = document.createElement('option');

            opcao.value = chave;
            opcao.textContent = intervaloCurto(diasDaSemana(chave));
            seletorSemanal.appendChild(opcao);
        });
    }

    function desenharSemanal() {
        if (!ultimaSemanal || typeof Chart === 'undefined' || vistas.semanal.hidden) {
            return;
        }

        const cores = coresDoTema();
        const escuro = document.documentElement.getAttribute('data-tema') === 'escuro';

        const rotulos = ultimaSemanal.dias.map(function (dia, indice) {
            return [DIAS_SEMANA[indice], doisDigitos(dia.getDate()) + '/' + doisDigitos(dia.getMonth() + 1)];
        });

        const valores = ultimaSemanal.kwhPorDia.map(function (valor) {
            return Math.round(valor * 100) / 100;
        });

        if (graficoSemanal) {
            graficoSemanal.destroy();
        }

        Chart.defaults.font.family = "'Inter', sans-serif";
        Chart.defaults.color = cores.texto;

        graficoSemanal = new Chart(canvasSemanal, {
            type: 'line',
            data: {
                labels: rotulos,
                datasets: [{
                    data: valores,
                    borderColor: cores.destaque,
                    borderWidth: 3,
                    cubicInterpolationMode: 'monotone',
                    fill: true,
                    backgroundColor: function (contexto) {
                        const area = contexto.chart.chartArea;

                        if (!area) {
                            return corComAlfa(cores.destaque, 0.2);
                        }

                        const gradiente = contexto.chart.ctx.createLinearGradient(0, area.top, 0, area.bottom);
                        gradiente.addColorStop(0, corComAlfa(cores.destaque, 0.35));
                        gradiente.addColorStop(1, corComAlfa(cores.destaque, 0.02));

                        return gradiente;
                    },
                    pointRadius: 7,
                    pointHoverRadius: 9,
                    pointBackgroundColor: cores.destaque,
                    pointBorderColor: escuro ? '#1C1C1C' : '#FFFFFF',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: cores.tooltip,
                        callbacks: {
                            title: function (itens) {
                                const rotulo = itens[0].label;
                                return Array.isArray(rotulo) ? rotulo.join(' ') : rotulo;
                            },
                            label: function (contexto) {
                                return formatarNumero(contexto.raw, 0, 2) + ' kWh';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: { maxRotation: 0 }
                    },
                    y: {
                        beginAtZero: true,
                        border: { display: false },
                        grid: { color: cores.grade }
                    }
                }
            }
        });
    }

    function atualizarSemanal() {
        const chave = seletorSemanal.value;
        const atual = totaisDaSemana(chave);
        const anterior = totaisDaSemana(chaveSemanaAnterior(chave));

        document.getElementById('semanalKwh').textContent = formatarNumero(atual.kwh, 0, 1);
        document.getElementById('semanalCusto').textContent = formatarNumero(atual.reais, 2, 2);
        document.getElementById('semanalRotuloAtual').textContent = intervaloLongo(atual.dias);
        document.getElementById('semanalValorAtual').textContent = formatarReais(atual.reais);
        document.getElementById('semanalValorAnterior').textContent = formatarReais(anterior.reais);

        definirEstadoSemanal(atual.leituras === 0 ? 'Sem leituras nesta semana.' : '');

        ultimaSemanal = atual;
        desenharSemanal();
    }

    function definirEstadoComparativo(texto) {
        estadoComparativo.textContent = texto || '';
        estadoComparativo.hidden = !texto;
    }

    function rotuloSeletorComparativo(chave) {
        const partes = chave.split('-');
        const ano = Number(partes[0]);
        const mes = Number(partes[1]);
        const anteriorPartes = chaveAnterior(chave).split('-');
        const anoAnterior = Number(anteriorPartes[0]);
        const mesAnterior = Number(anteriorPartes[1]);

        if (ano === anoAnterior) {
            return 'Mês de ' + MESES_CURTOS[mes - 1] + '. e ' + MESES_CURTOS[mesAnterior - 1] + '. de ' + ano;
        }

        return 'Mês de ' + MESES_CURTOS[mes - 1] + '. de ' + ano + ' e ' + MESES_CURTOS[mesAnterior - 1] + '. de ' + anoAnterior;
    }

    function montarSeletorComparativo() {
        seletorComparativo.innerHTML = '';

        mesesDisponiveis().forEach(function (chave) {
            const opcao = document.createElement('option');

            opcao.value = chave;
            opcao.textContent = rotuloSeletorComparativo(chave);
            seletorComparativo.appendChild(opcao);
        });
    }

    function desenharComparativo() {
        if (!ultimoComparativo || typeof Chart === 'undefined' || vistas.comparativo.hidden) {
            return;
        }

        const atual = ultimoComparativo.atual;
        const anterior = ultimoComparativo.anterior;
        const cores = coresDoTema();
        const escuro = document.documentElement.getAttribute('data-tema') === 'escuro';
        const corSecundaria = '#FF5A00';
        const borda = escuro ? '#1C1C1C' : '#FFFFFF';

        const rotulos = atual.kwhPorDia.map(function (valor, indice) {
            return doisDigitos(indice + 1);
        });

        const valoresAtual = atual.kwhPorDia.map(function (valor) {
            return Math.round(valor * 100) / 100;
        });

        const valoresAnterior = anterior.kwhPorDia.slice(0, atual.dias).map(function (valor) {
            return Math.round(valor * 100) / 100;
        });

        if (graficoComparativo) {
            graficoComparativo.destroy();
        }

        Chart.defaults.font.family = "'Inter', sans-serif";
        Chart.defaults.color = cores.texto;

        graficoComparativo = new Chart(canvasComparativo, {
            type: 'line',
            data: {
                labels: rotulos,
                datasets: [
                    {
                        label: 'Consumo em ' + nomeMes(atual.ano, atual.mes),
                        data: valoresAtual,
                        borderColor: cores.destaque,
                        borderWidth: 3,
                        cubicInterpolationMode: 'monotone',
                        fill: false,
                        pointRadius: 5,
                        pointHoverRadius: 7,
                        pointBackgroundColor: cores.destaque,
                        pointBorderColor: borda,
                        pointBorderWidth: 2
                    },
                    {
                        label: 'Consumo em ' + nomeMes(anterior.ano, anterior.mes),
                        data: valoresAnterior,
                        borderColor: corSecundaria,
                        borderWidth: 3,
                        cubicInterpolationMode: 'monotone',
                        fill: false,
                        pointRadius: 5,
                        pointHoverRadius: 7,
                        pointBackgroundColor: corSecundaria,
                        pointBorderColor: borda,
                        pointBorderWidth: 2
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: cores.tooltip,
                        callbacks: {
                            title: function (itens) {
                                return 'Dia ' + itens[0].label;
                            },
                            label: function (contexto) {
                                return contexto.dataset.label + ': ' + formatarNumero(contexto.raw, 0, 2) + ' kWh';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        border: { display: false },
                        ticks: { maxRotation: 0 }
                    },
                    y: {
                        beginAtZero: true,
                        border: { display: false },
                        grid: { color: cores.grade }
                    }
                }
            }
        });
    }

    function atualizarComparativo() {
        const chave = seletorComparativo.value;
        const atual = totaisDoMes(chave);
        const anterior = totaisDoMes(chaveAnterior(chave));
        const nomeAtual = nomeMes(atual.ano, atual.mes);
        const nomeAnterior = nomeMes(anterior.ano, anterior.mes);

        document.getElementById('comparativoRotuloKwhAtual').textContent = 'Mês de ' + nomeAtual;
        document.getElementById('comparativoKwhAtual').textContent = formatarNumero(atual.kwh, 0, 1) + ' kWh';
        document.getElementById('comparativoKwhAnterior').textContent = formatarNumero(anterior.kwh, 0, 1) + ' kWh';
        document.getElementById('comparativoRotuloValorAtual').textContent = 'Mês de ' + nomeAtual;
        document.getElementById('comparativoValorAtual').textContent = formatarReais(atual.reais);
        document.getElementById('comparativoValorAnterior').textContent = formatarReais(anterior.reais);
        document.getElementById('comparativoLegendaAtual').textContent = 'Consumo em ' + nomeAtual;
        document.getElementById('comparativoLegendaAnterior').textContent = 'Consumo em ' + nomeAnterior;

        const variacao = document.getElementById('comparativoVariacao');
        const variacaoTexto = document.getElementById('comparativoVariacaoTexto');
        const diferenca = Math.round((atual.reais - anterior.reais) * 100) / 100;

        variacao.classList.remove('hist-variacao-economia', 'hist-variacao-aumento');

        if (atual.leituras === 0 || anterior.leituras === 0) {
            variacao.textContent = formatarReais(0);
            variacaoTexto.textContent = 'Sem dados para comparar';
        } else if (diferenca < 0) {
            variacao.textContent = formatarReais(Math.abs(diferenca));
            variacao.classList.add('hist-variacao-economia');
            variacaoTexto.textContent = 'Houve economia em relação ao mês anterior';
        } else if (diferenca > 0) {
            variacao.textContent = formatarReais(diferenca);
            variacao.classList.add('hist-variacao-aumento');
            variacaoTexto.textContent = 'Houve aumento em relação ao mês anterior';
        } else {
            variacao.textContent = formatarReais(0);
            variacaoTexto.textContent = 'Sem variação em relação ao mês anterior';
        }

        if (atual.leituras === 0) {
            definirEstadoComparativo('Sem leituras no mês escolhido.');
        } else if (anterior.leituras === 0) {
            definirEstadoComparativo('Sem leituras no mês anterior para comparar.');
        } else {
            definirEstadoComparativo('');
        }

        ultimoComparativo = { atual: atual, anterior: anterior };
        desenharComparativo();
    }

    function atualizarTudo() {
        montarSeletor();
        atualizarMensal();
        montarSeletorSemanal();
        atualizarSemanal();
        montarSeletorComparativo();
        atualizarComparativo();
    }

    function carregar() {
        if (!usuarioId) {
            atualizarTudo();
            definirEstado('Não foi possível identificar o usuário.');
            definirEstadoSemanal('Não foi possível identificar o usuário.');
            definirEstadoComparativo('Não foi possível identificar o usuário.');
            return;
        }

        definirEstado('Carregando leituras...');
        definirEstadoSemanal('Carregando leituras...');
        definirEstadoComparativo('Carregando leituras...');

        fetch('/api/consumos/usuario/' + encodeURIComponent(usuarioId))
            .then(function (resposta) {
                if (!resposta.ok) {
                    throw new Error('Falha ao carregar as leituras');
                }

                return resposta.json();
            })
            .then(function (lista) {
                consumos = prepararLeituras(Array.isArray(lista) ? lista : []);
                atualizarTudo();
            })
            .catch(function () {
                consumos = [];
                atualizarTudo();
                definirEstado('Não foi possível carregar as leituras. Tente novamente mais tarde.');
                definirEstadoSemanal('Não foi possível carregar as leituras. Tente novamente mais tarde.');
                definirEstadoComparativo('Não foi possível carregar as leituras. Tente novamente mais tarde.');
            });
    }

    abas.forEach(function (aba) {
        aba.addEventListener('click', function () {
            abas.forEach(function (outra) {
                const ativa = outra === aba;
                outra.classList.toggle('ativa', ativa);
                outra.setAttribute('aria-selected', String(ativa));
            });

            Object.keys(vistas).forEach(function (nome) {
                vistas[nome].hidden = nome !== aba.dataset.vista;
            });

            if (aba.dataset.vista === 'mensal') {
                desenharMensal();
            }

            if (aba.dataset.vista === 'semanal') {
                desenharSemanal();
            }

            if (aba.dataset.vista === 'comparativo') {
                desenharComparativo();
            }
        });
    });

    const btnExportarPdf = document.getElementById('btnExportarPdf');

    function exportarPdf() {
        const raiz = document.documentElement;
        const temaOriginal = raiz.getAttribute('data-tema');

        function restaurar() {
            window.removeEventListener('afterprint', restaurar);

            if (temaOriginal === null) {
                raiz.removeAttribute('data-tema');
            } else {
                raiz.setAttribute('data-tema', temaOriginal);
            }

            document.dispatchEvent(new Event('zatts:tema'));
        }

        window.addEventListener('afterprint', restaurar);
        raiz.setAttribute('data-tema', 'claro');
        document.dispatchEvent(new Event('zatts:tema'));

        [graficoMensal, graficoSemanal, graficoComparativo].forEach(function (grafico) {
            if (grafico) {
                grafico.resize();
                grafico.update('none');
            }
        });

        setTimeout(function () {
            window.print();
        }, 300);
    }

    btnExportarPdf.addEventListener('click', exportarPdf);

    seletorMensal.addEventListener('change', atualizarMensal);
    seletorSemanal.addEventListener('change', atualizarSemanal);
    seletorComparativo.addEventListener('change', atualizarComparativo);
    document.addEventListener('zatts:tema', desenharMensal);
    document.addEventListener('zatts:tema', desenharSemanal);
    document.addEventListener('zatts:tema', desenharComparativo);

    carregar();
});