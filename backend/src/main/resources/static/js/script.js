document.addEventListener("DOMContentLoaded", () => {

    iniciarRelogio();

    iniciarGraficos();

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


    if(typeof Chart === "undefined"){
        return;
    }


    Chart.defaults.font.family =
        "'Inter', sans-serif";


    Chart.defaults.color =
        "#9CA3AF";


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







// =====================================================
// Gráfico Consumo em tempo real
// =====================================================


function graficoConsumo(){



    const elemento =
        document.getElementById(
            "chartConsumoRealtime"
        );



    if(!elemento){
        return;
    }



    new Chart(
        elemento,
        {

            type:"bar",


            data:{


                labels:[
                    "5h",
                    "6h",
                    "7h",
                    "8h",
                    "9h",
                    "10h",
                    "11h",
                    "12h",
                    "13h",
                    "14h",
                    "15h"
                ],


                datasets:[{


                    data:[
                        4,
                        6,
                        10,
                        15,
                        22,
                        29,
                        19,
                        22,
                        12,
                        13,
                        16
                    ],


                    backgroundColor:
                        "#D2F500",


                    borderRadius:
                        6,


                    borderSkipped:
                        false,


                    barThickness:
                        16


                }]


            },



            options:{


                responsive:true,


                maintainAspectRatio:false,



                plugins:{


                    legend:{
                        display:false
                    },


                    tooltip:{


                        backgroundColor:
                            "#1E293B",


                        callbacks:{


                            label:
                                function(context){

                                    return `${context.raw} kWh`;

                                }


                        }


                    }


                },



                scales:{


                    x:{


                        grid:{
                            display:false
                        },


                        border:{
                            display:false
                        }


                    },



                    y:{


                        min:0,


                        max:50,


                        ticks:{
                            stepSize:10
                        },


                        border:{
                            display:false
                        },


                        grid:{
                            color:"#F3F4F6"
                        }


                    }



                }



            }



        }

    );

}





// =====================================================
// Gráfico Comparativo últimos meses
// =====================================================


function graficoComparativo(){



    const elemento =
        document.getElementById(
            "chartResumoComparativo"
        );



    if(!elemento){
        return;
    }



    const contexto =
        elemento.getContext("2d");



    const gradiente =
        contexto.createLinearGradient(
            0,
            0,
            0,
            300
        );



    gradiente.addColorStop(
        0,
        "rgba(210,245,0,0.45)"
    );


    gradiente.addColorStop(
        1,
        "rgba(210,245,0,0.02)"
    );





    new Chart(
        contexto,
        {


            type:"line",


            data:{


                labels:[

                    "Abril",
                    "Maio",
                    "Junho",
                    "Julho",
                    "Agosto",
                    "Setembro"

                ],


                datasets:[{


                    data:[

                        190,
                        170,
                        220,
                        260,
                        230,
                        166

                    ],


                    borderColor:
                        "#C2E200",


                    backgroundColor:
                    gradiente,


                    borderWidth:
                        3,


                    fill:true,


                    tension:
                        0.2,


                    pointRadius:
                        5,


                    pointBackgroundColor:
                        "#B2D400"


                }]


            },



            options:{


                responsive:true,


                maintainAspectRatio:false,



                plugins:{


                    legend:{
                        display:false
                    },


                    tooltip:{


                        callbacks:{


                            label:
                                function(context){

                                    return `${context.raw} kWh`;

                                }


                        }


                    }


                },



                scales:{


                    x:{


                        grid:{
                            display:false
                        },


                        border:{
                            display:false
                        }


                    },



                    y:{


                        min:0,


                        max:300,


                        ticks:{
                            stepSize:50
                        },


                        border:{
                            display:false
                        },


                        grid:{
                            color:"#F3F4F6"
                        }


                    }



                }


            }



        }

    );


}








// =====================================================
// Gráfico Valor da conta
// =====================================================


function graficoConta(){



    const elemento =
        document.getElementById(
            "chartValorConta"
        );



    if(!elemento){
        return;
    }



    new Chart(
        elemento,
        {


            type:"bar",


            data:{


                labels:[

                    "Abr",
                    "Mai",
                    "Jun",
                    "Jul",
                    "Ago",
                    "Set"

                ],



                datasets:[{


                    data:[

                        160,
                        195,
                        142,
                        162,
                        108,
                        116

                    ],


                    backgroundColor:
                        "#D2F500",


                    borderRadius:
                        6,


                    borderSkipped:false,


                    barThickness:
                        16


                }]


            },



            options:{


                responsive:true,


                maintainAspectRatio:false,



                plugins:{


                    legend:{
                        display:false
                    },


                    tooltip:{


                        callbacks:{


                            label:
                                function(context){

                                    return `R$ ${context.raw},00`;

                                }


                        }


                    }


                },



                scales:{


                    x:{


                        grid:{
                            display:false
                        },


                        border:{
                            display:false
                        }


                    },



                    y:{


                        min:0,


                        max:300,


                        ticks:{


                            stepSize:50,


                            callback:
                                function(valor){

                                    if(valor === 0){
                                        return "0";
                                    }


                                    return `R$ ${valor}`;

                                }


                        },


                        border:{
                            display:false
                        },


                        grid:{
                            color:"#F3F4F6"
                        }



                    }


                }



            }



        }

    );


}



