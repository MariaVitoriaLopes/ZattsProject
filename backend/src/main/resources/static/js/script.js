document.addEventListener("DOMContentLoaded", () => {

    iniciarRelogio();

    iniciarGraficos();

});

document.addEventListener("zatts:tema", () => {

    if(typeof Chart !== "undefined"){
        iniciarGraficos();
    }

});
// =====================================================
// Relógio em tempo real
// =====================================================

function iniciarRelogio() {


    function atualizarHorario() {


        const agora = new Date();


        const horas = String(agora.getHours()).padStart(2, "0");
        const minutos = String(agora.getMinutes()).padStart(2, "0");
        const segundos = String(agora.getSeconds()).padStart(2, "0");



        const clock = document.getElementById("live-clock");


        if (clock) {

            clock.innerHTML =
                `${horas}:${minutos}:${segundos}`;

        }




        const data = document.getElementById("live-date");


        if(data){


            let dataFormatada =
                agora.toLocaleDateString(
                    "pt-BR",
                    {
                        weekday:"long",
                        day:"numeric",
                        month:"long",
                        year:"numeric"
                    }
                );


            dataFormatada =
                dataFormatada.charAt(0).toUpperCase()
                +
                dataFormatada.slice(1);



            data.innerHTML = dataFormatada;


        }


    }



    atualizarHorario();


    setInterval(
        atualizarHorario,
        1000
    );

}





// =====================================================
// Configuração geral Chart.js
// =====================================================


function configurarChart(){


    if  (typeof Chart === "undefined"){
        return;
    }


    Chart.defaults.font.family =
        "'Inter', sans-serif";


    Chart.defaults.color =
        coresDoTema().texto;

}
function coresDoTema(){

    const escuro =
        document.documentElement.getAttribute("data-tema") === "escuro";

    const destaque =
        getComputedStyle(document.documentElement)
            .getPropertyValue("--cor-primaria-400")
            .trim() || "#E6FB1A";

    return {
        destaque: destaque,
        texto: escuro ? "#A3A3A3" : "#9CA3AF",
        grade: escuro ? "#2C2C2C" : "#F3F4F6",
        tooltip: escuro ? "#2A2A2A" : "#1E293B"
    };

}

function hexParaRgba(hex, alfa){

    const limpo = hex.replace("#", "");

    const completo = limpo.length === 3
        ? limpo.split("").map(function(c){ return c + c; }).join("")
        : limpo;

    const r = parseInt(completo.substring(0, 2), 16);
    const g = parseInt(completo.substring(2, 4), 16);
    const b = parseInt(completo.substring(4, 6), 16);

    return `rgba(${r},${g},${b},${alfa})`;

}

function destruirGrafico(elemento){

    const existente = Chart.getChart(elemento);

    if(existente){
        existente.destroy();
    }

}




// =====================================================
// Inicialização dos gráficos
// =====================================================


function iniciarGraficos(){


    configurarChart();


    graficoConsumo();


    graficoComparativo();


    graficoConta();


}

// conexão SSE pra alimentar apenas o gráfico de consumo
let instanciaGraficoConsumo = null;
let conexaoSSE = null;

// =====================================================
// Gráfico Consumo em tempo real
// =====================================================

function graficoConsumo(){

    const elemento =
        document.getElementById("chartConsumoRealtime");

    if (!elemento) {
        return;
    }

//    destruirGrafico(elemento);
    if (instanciaGraficoConsumo) {
        instanciaGraficoConsumo.destroy();
    }

    const cores = coresDoTema();

     instanciaGraficoConsumo = new Chart(elemento, {

        type: "bar",

        data: {
            labels: [],

            datasets: [{
                label: "Consumo de Energia",
                data: [],
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
                        label: function(context){
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
                    min: 0,
                    max: 50,
                    ticks: { stepSize: 10 },
                    border: { display: false },
                    grid: { color: cores.grade }
                }

            }

        }

    });

    if (conexaoSSE) {
        conexaoSSE.close();
    }

    conexaoSSE = new EventSource("api/consumos/stream");

    conexaoSSE.addEventListener("consumo-horario", event => {
        const leitura = JSON.parse(event.data)
        const data = new Date(leitura.dataHora)
        const hora = String(data.getHours()).padStart(2,"0");
        const label = `${hora}h`;

        const labels = instanciaGraficoConsumo.data.labels;
        const valores = instanciaGraficoConsumo.data.datasets[0].data;

        let index = labels.indexOf(label);

        if (index === -1) {
            index = labels.length;
            labels.push(label);
            valores.push(0);
        }

        valores[index] = leitura.consumoKwh;

        while (labels.length > 24) {
            labels.shift();
            valores.shift();
        }

        instanciaGraficoConsumo.update("none");
    });

    conexaoSSE.onerror = erro => {
        console.error("Erro na conexão SSE: ", erro);
    };
}


function graficoComparativo(){

    const elemento =
        document.getElementById("chartResumoComparativo");

    if(!elemento){
        return;
    }

    destruirGrafico(elemento);

    const cores = coresDoTema();

    const contexto =
        elemento.getContext("2d");

    const gradiente =
        contexto.createLinearGradient(0, 0, 0, 300);

    gradiente.addColorStop(0, hexParaRgba(cores.destaque, 0.45));
    gradiente.addColorStop(1, hexParaRgba(cores.destaque, 0.02));

    new Chart(contexto, {

        type: "line",

        data: {

            labels: [
                "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro"
            ],

            datasets: [{
                data: [190, 170, 220, 260, 230, 166],
                borderColor: cores.destaque,
                backgroundColor: gradiente,
                borderWidth: 3,
                fill: true,
                tension: 0.2,
                pointRadius: 5,
                pointBackgroundColor: cores.destaque
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
                        label: function(context){
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
                    min: 0,
                    max: 300,
                    ticks: { stepSize: 50 },
                    border: { display: false },
                    grid: { color: cores.grade }
                }

            }

        }

    });

}


function graficoConta(){

    const elemento =
        document.getElementById("chartValorConta");

    if(!elemento){
        return;
    }

    destruirGrafico(elemento);

    const cores = coresDoTema();

    new Chart(elemento, {

        type: "bar",

        data: {

            labels: ["Abr", "Mai", "Jun", "Jul", "Ago", "Set"],

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
                        label: function(context){
                            return `R$ ${context.raw},00`;
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
                        callback: function(valor){

                            if(valor === 0){
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
