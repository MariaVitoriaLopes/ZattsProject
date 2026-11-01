
document.addEventListener("DOMContentLoaded", function () {

    iniciarRelogio();
    iniciarGraficos();
    carregarDashboard();

    setInterval(carregarDashboard, 10000);

});

document.addEventListener("zatts:tema", function () {

    if (typeof Chart !== "undefined") {
        iniciarGraficos();
    }

    atualizarMeta();

});

// =====================================================
// DADOS DO DASHBOARD
// =====================================================

let leiturasMinuto = [];
let leiturasHora = [];

let metaConsumoKwh = 150;
let consumoMensalKwh = 85.5;

// Dados fictícios mantidos durante a sessão
const historicoSimuladoMinuto = new Map();
const historicoSimuladoHora = new Map();

// =====================================================
// RELÓGIO EM TEMPO REAL
// =====================================================

function iniciarRelogio() {

    function atualizarHorario() {

        const agora = new Date();

        const horas = String(agora.getHours()).padStart(2, "0");
        const minutos = String(agora.getMinutes()).padStart(2, "0");
        const segundos = String(agora.getSeconds()).padStart(2, "0");

        const clock = document.getElementById("live-clock");

        if (clock) {
            clock.textContent = `${horas}:${minutos}:${segundos}`;
        }

        const data = document.getElementById("live-date");

        if (data) {

            let dataFormatada = agora.toLocaleDateString("pt-BR", {
                weekday: "long",
                day: "numeric",
                month: "long",
                year: "numeric"
            });

            dataFormatada =
                dataFormatada.charAt(0).toUpperCase() +
                dataFormatada.slice(1);

            data.textContent = dataFormatada;
        }
    }

    atualizarHorario();

    setInterval(atualizarHorario, 1000);
}

// =====================================================
// CONFIGURAÇÃO GERAL CHART.JS
// =====================================================

function configurarChart() {

    if (typeof Chart === "undefined") {
        return;
    }

    Chart.defaults.font.family = "'Inter', sans-serif";
    Chart.defaults.color = coresDoTema().texto;
}

function coresDoTema() {

    const escuro =
        document.documentElement.getAttribute("data-tema") === "escuro";

    const destaque = getComputedStyle(document.documentElement)
        .getPropertyValue("--cor-primaria-400")
        .trim() || "#E6FB1A";

    return {
        destaque: destaque,
        texto: escuro ? "#A3A3A3" : "#9CA3AF",
        grade: escuro ? "#2C2C2C" : "#F3F4F6",
        tooltip: escuro ? "#2A2A2A" : "#1E293B"
    };
}

function hexParaRgba(hex, alfa) {

    const limpo = hex.replace("#", "");

    const completo = limpo.length === 3
        ? limpo.split("").map(c => c + c).join("")
        : limpo;

    const r = parseInt(completo.substring(0, 2), 16);
    const g = parseInt(completo.substring(2, 4), 16);
    const b = parseInt(completo.substring(4, 6), 16);

    return `rgba(${r},${g},${b},${alfa})`;
}

function destruirGrafico(elemento) {

    if (typeof Chart === "undefined" || !elemento) {
        return;
    }

    const existente = Chart.getChart(elemento);

    if (existente) {
        existente.destroy();
    }
}

// =====================================================
// FORMATAÇÃO E TRATAMENTO DOS DADOS
// =====================================================

function numeroValido(valor) {

    if (valor === null || valor === undefined || valor === "") {
        return null;
    }

    const numero = Number(valor);

    return Number.isFinite(numero) && numero >= 0
        ? numero
        : null;
}

function formatarKwh(valor) {

    return valor.toLocaleString("pt-BR", {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    }) + " kWh";
}

function formatarReais(valor) {

    return valor.toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}

function inicioMinuto(data) {

    const resultado = new Date(data);
    resultado.setSeconds(0, 0);

    return resultado;
}

function inicioHora(data) {

    const resultado = new Date(data);
    resultado.setMinutes(0, 0, 0);

    return resultado;
}

// =====================================================
// INICIALIZAÇÃO DOS GRÁFICOS
// =====================================================

function iniciarGraficos() {

    if (typeof Chart === "undefined") {
        return;
    }

    configurarChart();

    graficoConsumo();
    graficoComparativo();
    graficoConta();

    atualizarGraficosConsumo();
}

// =====================================================
// GRÁFICO CONSUMO EM TEMPO REAL
// =====================================================

function graficoConsumo() {

    const elemento = document.getElementById("chartConsumoRealtime");

    if (!elemento || typeof Chart === "undefined") {
        return;
    }

    destruirGrafico(elemento);

    const cores = coresDoTema();
    const contexto = elemento.getContext("2d");

    const gradiente = contexto.createLinearGradient(0, 0, 0, 350);

    gradiente.addColorStop(0, hexParaRgba(cores.destaque, 0.45));
    gradiente.addColorStop(1, hexParaRgba(cores.destaque, 0.03));

    new Chart(elemento, {

        type: "line",

        data: {
            labels: [],
            datasets: [{
                data: [],
                borderColor: cores.destaque,
                backgroundColor: gradiente,
                borderWidth: 2,
                fill: true,
                tension: 0.2,
                pointRadius: 4,
                pointBackgroundColor: cores.destaque,
                spanGaps: false
            }]
        },

        options: {

            responsive: true,
            maintainAspectRatio: false,
            animation: false,

            plugins: {

                legend: {
                    display: false
                },

                tooltip: {
                    backgroundColor: cores.tooltip,
                    callbacks: {
                        label: function (context) {
                            return `${context.raw} kWh`;
                        }
                    }
                }

            },

            scales: {

                x: {
                    grid: { display: false },
                    border: { display: false }
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

// =====================================================
// GRÁFICO CONSUMO DIÁRIO POR HORA
// =====================================================

function graficoComparativo() {

    const elemento = document.getElementById("chartResumoComparativo");

    if (!elemento || typeof Chart === "undefined") {
        return;
    }

    destruirGrafico(elemento);

    const cores = coresDoTema();

    new Chart(elemento, {

        type: "bar",

        data: {
            labels: [],
            datasets: [{
                data: [],
                backgroundColor: cores.destaque,
                borderRadius: 6,
                borderSkipped: false,
                maxBarThickness: 35
            }]
        },

        options: {

            responsive: true,
            maintainAspectRatio: false,
            animation: false,

            plugins: {

                legend: {
                    display: false
                },

                tooltip: {
                    backgroundColor: cores.tooltip,
                    callbacks: {
                        label: function (context) {
                            return `${context.raw} kWh`;
                        }
                    }
                }

            },

            scales: {

                x: {
                    grid: { display: false },
                    border: { display: false }
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

// =====================================================
// ATUALIZAÇÃO DOS GRÁFICOS
// =====================================================

function atualizarGraficosConsumo() {

    if (typeof Chart === "undefined") {
        return;
    }

    const elementoMinuto = document.getElementById("chartConsumoRealtime");
    const elementoHora = document.getElementById("chartResumoComparativo");

    const graficoMinuto = elementoMinuto
        ? Chart.getChart(elementoMinuto)
        : null;

    const graficoHora = elementoHora
        ? Chart.getChart(elementoHora)
        : null;

    // GRÁFICO POR MINUTO
    // Últimos 10 minutos, incluindo o atual.

    if (graficoMinuto) {

        const agora = inicioMinuto(new Date());
        const mapa = new Map();

        leiturasMinuto.forEach(leitura => {

            const chave = inicioMinuto(leitura.instante).getTime();
            mapa.set(chave, leitura.consumoKwh);

        });

        const labels = [];
        const valores = [];

        for (let i = 10; i >= 0; i--) {

            const instante = new Date(agora.getTime() - i * 60000);

            labels.push(
                instante.toLocaleTimeString("pt-BR", {
                    hour: "2-digit",
                    minute: "2-digit"
                })
            );

            valores.push(
                mapa.has(instante.getTime())
                    ? mapa.get(instante.getTime())
                    : null
            );
        }

        graficoMinuto.data.labels = labels;
        graficoMinuto.data.datasets[0].data = valores;
        graficoMinuto.update();
    }

    // GRÁFICO POR HORA
    // Últimas 10 horas, incluindo a atual.

    if (graficoHora) {

        const agora = inicioHora(new Date());
        const mapa = new Map();

        leiturasHora.forEach(leitura => {

            const chave = inicioHora(leitura.instante).getTime();
            mapa.set(chave, leitura.consumoKwh);

        });

        const labels = [];
        const valores = [];

        for (let i = 9; i >= 0; i--) {

            const instante = new Date(agora.getTime() - i * 3600000);

            labels.push(
                instante.toLocaleTimeString("pt-BR", {
                    hour: "2-digit"
                }) + "h"
            );

            valores.push(
                mapa.has(instante.getTime())
                    ? mapa.get(instante.getTime())
                    : null
            );
        }

        graficoHora.data.labels = labels;
        graficoHora.data.datasets[0].data = valores;
        graficoHora.update();
    }
}

// =====================================================
// META DE CONSUMO MENSAL
// =====================================================

function atualizarMeta() {

    const card = document.getElementById("cardStatusMeta");
    const texto = document.getElementById("textoStatusMeta");
    const descricao = document.getElementById("descricaoStatusMeta");
    const icone = document.getElementById("iconeStatusMeta");

    const progresso = document.getElementById("progressoMeta");
    const limite = document.getElementById("limiteMeta");
    const consumoTexto = document.getElementById("consumoMensal");
    const metaTexto = document.getElementById("valorMeta");

    if (!card || !progresso) {
        return;
    }

    // COR DA BARRA CONFORME O TEMA ESCOLHIDO

    const cores = coresDoTema();

    progresso.style.backgroundColor = cores.destaque;

    card.classList.remove(
        "status-verde",
        "status-laranja",
        "status-vermelho",
        "status-sem-meta"
    );

    const meta = numeroValido(metaConsumoKwh);
    const consumo = numeroValido(consumoMensalKwh);

    if (meta === null || meta <= 0) {

        card.classList.add("status-sem-meta");

        if (texto) texto.textContent = "Nenhuma meta configurada";
        if (descricao) descricao.textContent = "Cadastre uma meta em Notificações";
        if (icone) icone.textContent = "flag";

        progresso.style.width = "0%";

        if (limite) limite.textContent = "Sem meta";
        if (metaTexto) metaTexto.textContent = "-- kWh";

        if (consumoTexto) {
            consumoTexto.textContent =
                consumo === null ? "-- kWh" : formatarKwh(consumo);
        }

        return;
    }

    if (limite) limite.textContent = formatarKwh(meta);
    if (metaTexto) metaTexto.textContent = formatarKwh(meta);

    if (consumo === null) {

        card.classList.add("status-sem-meta");

        if (texto) texto.textContent = "Aguardando dados de consumo";
        if (descricao) descricao.textContent = "Meta: " + formatarKwh(meta);
        if (icone) icone.textContent = "flag";

        if (consumoTexto) consumoTexto.textContent = "-- kWh";

        progresso.style.width = "0%";

        return;
    }

    const percentual = (consumo / meta) * 100;

    if (consumoTexto) {
        consumoTexto.textContent = formatarKwh(consumo);
    }

    progresso.style.width = Math.min(percentual, 100) + "%";

    if (descricao) {
        descricao.textContent =
            percentual.toFixed(1).replace(".", ",") +
            "% da meta mensal atingida";
    }

    if (percentual >= 100) {

        card.classList.add("status-vermelho");

        if (texto) {
            texto.textContent = percentual > 100
                ? "Você ultrapassou sua meta mensal!"
                : "Você atingiu sua meta mensal!";
        }

        if (icone) icone.textContent = "flag";

    } else if (percentual >= 80) {

        card.classList.add("status-laranja");

        if (texto) texto.textContent = "Você está perto da sua meta!";
        if (icone) icone.textContent = "warning";

    } else {

        card.classList.add("status-verde");

        if (texto) texto.textContent = "Você está dentro da sua meta!";
        if (icone) icone.textContent = "flag_check";

    }
}

// =====================================================
// DADOS FICTÍCIOS DO DASHBOARD
// =====================================================

function carregarDashboard() {

    const agora = new Date();

    // CONSUMO POR MINUTO

    leiturasMinuto = [];

    for (let i = 10; i >= 0; i--) {

        const instante = inicioMinuto(
            new Date(agora.getTime() - i * 60000)
        );

        const chave = instante.getTime();

        if (!historicoSimuladoMinuto.has(chave)) {

            const consumo = 0.2 + Math.random() * 0.6;

            historicoSimuladoMinuto.set(
                chave,
                Number(consumo.toFixed(2))
            );
        }

        leiturasMinuto.push({
            instante: instante,
            consumoKwh: historicoSimuladoMinuto.get(chave)
        });
    }

    // CONSUMO POR HORA

    leiturasHora = [];

    for (let i = 9; i >= 0; i--) {

        const instante = inicioHora(
            new Date(agora.getTime() - i * 3600000)
        );

        const chave = instante.getTime();

        if (!historicoSimuladoHora.has(chave)) {

            const consumo = 0.4 + Math.random() * 1.8;

            historicoSimuladoHora.set(
                chave,
                Number(consumo.toFixed(2))
            );
        }

        leiturasHora.push({
            instante: instante,
            consumoKwh: historicoSimuladoHora.get(chave)
        });
    }

    // META MENSAL FICTÍCIA

    metaConsumoKwh = 150;
    consumoMensalKwh = 85.5;

    // Remove horários antigos da memória

    const primeiroMinuto = inicioMinuto(
        new Date(agora.getTime() - 10 * 60000)
    ).getTime();

    const primeiraHora = inicioHora(
        new Date(agora.getTime() - 9 * 3600000)
    ).getTime();

    for (const chave of historicoSimuladoMinuto.keys()) {
        if (chave < primeiroMinuto) {
            historicoSimuladoMinuto.delete(chave);
        }
    }

    for (const chave of historicoSimuladoHora.keys()) {
        if (chave < primeiraHora) {
            historicoSimuladoHora.delete(chave);
        }
    }

    atualizarGraficosConsumo();
    atualizarMeta();

    // Remove mensagens antigas de erro da API

    const estadoMinuto = document.getElementById("estadoConsumoRealtime");
    const estadoHora = document.getElementById("estadoConsumoDiario");

    if (estadoMinuto) {
        estadoMinuto.textContent = "";
    }

    if (estadoHora) {
        estadoHora.textContent = "";
    }
}

// =====================================================
// GRÁFICO DO VALOR DA CONTA
// DADOS DEMONSTRATIVOS
// =====================================================

function graficoConta() {

    const elemento = document.getElementById("chartValorConta");

    if (!elemento || typeof Chart === "undefined") {
        return;
    }

    destruirGrafico(elemento);

    const cores = coresDoTema();

    const agora = new Date();

    const meses = [];

    for (let i = 5; i >= 0; i--) {

        const data = new Date(
            agora.getFullYear(),
            agora.getMonth() - i,
            1
        );

        const nome = data.toLocaleDateString("pt-BR", {
            month: "short"
        });

        meses.push(
            nome.replace(".", "").charAt(0).toUpperCase() +
            nome.replace(".", "").slice(1)
        );
    }

    new Chart(elemento, {

        type: "bar",

        data: {

            labels: meses,

            datasets: [{
                data: [160, 195, 142, 162, 108, 116],
                backgroundColor: cores.destaque,
                borderRadius: 6,
                borderSkipped: false,
                barThickness: 16
            }]

        },

        options: {

            responsive: true,
            maintainAspectRatio: false,

            plugins: {

                legend: {
                    display: false
                },

                tooltip: {
                    backgroundColor: cores.tooltip,
                    callbacks: {
                        label: function (context) {
                            return formatarReais(context.raw);
                        }
                    }
                }

            },

            scales: {

                x: {
                    grid: { display: false },
                    border: { display: false }
                },

                y: {
                    min: 0,
                    max: 300,
                    ticks: {
                        stepSize: 50,
                        callback: function (valor) {

                            if (valor === 0) {
                                return "0";
                            }

                            return `R$ ${valor}`;
                        }
                    },
                    border: { display: false },
                    grid: { color: cores.grade }
                }

            }

        }

    });
}
