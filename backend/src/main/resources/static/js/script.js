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

function mostrarSenha(idCampo, icone){

    const input = document.getElementById(idCampo);


    const campo = icone.parentElement;


    const olhoAberto = campo.querySelector(".visibility");

    const olhoFechado = campo.querySelector(".visibility_off");



    if(input.type === "password"){


        input.type = "text";


        olhoAberto.style.display = "none";

        olhoFechado.style.display = "block";


    } else {


        input.type = "password";


        olhoAberto.style.display = "block";

        olhoFechado.style.display = "none";

    }

}

const email = document.getElementById("email");
const senha = document.getElementById("senha");
const confirmarSenha = document.getElementById("confirmarSenha");



const requisitos = {

    tamanho:
        document.getElementById("req-tamanho"),

    maiuscula:
        document.getElementById("req-maiuscula"),

    minuscula:
        document.getElementById("req-minuscula"),

    numero:
        document.getElementById("req-numero"),

    especial:
        document.getElementById("req-especial"),

    confirmacao:
        document.getElementById("req-confirmacao")
};





function alterarStatus(elemento, valido){

    if(valido){

        elemento.style.color = "#34b502";

    }else{

        elemento.style.color = "#FF8000FF";

    }

}






function validarSenha(){


    const valor = senha.value;



    alterarStatus(
        requisitos.tamanho,
        valor.length >= 8
    );



    alterarStatus(
        requisitos.maiuscula,
        /[A-Z]/.test(valor)
    );



    alterarStatus(
        requisitos.minuscula,
        /[a-z]/.test(valor)
    );



    alterarStatus(
        requisitos.numero,
        /\d/.test(valor)
    );



    alterarStatus(
        requisitos.especial,
        /[^A-Za-z0-9]/.test(valor)
    );



    alterarStatus(
        requisitos.confirmacao,
        valor === confirmarSenha.value &&
        valor.length > 0
    );

}



senha.addEventListener(
    "input",
    validarSenha
);


confirmarSenha.addEventListener(
    "input",
    validarSenha
);








function validarEmail(valor){


    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor);


}






document.querySelector("form")
    .addEventListener("submit", function(event){



        if(email.value.trim() === ""){

            alert("Digite seu e-mail.");

            event.preventDefault();

            return;

        }



        if(!validarEmail(email.value)){


            alert("Digite um e-mail válido.");

            event.preventDefault();

            return;

        }





        if(senha.value.trim() === ""){


            alert("Digite uma senha.");

            event.preventDefault();

            return;

        }





        const senhaForte =
            senha.value.length >= 8 &&
            /[A-Z]/.test(senha.value) &&
            /[a-z]/.test(senha.value) &&
            /\d/.test(senha.value) &&
            /[^A-Za-z0-9]/.test(senha.value);




        if(!senhaForte){


            alert(
                "A senha não atende aos requisitos."
            );


            event.preventDefault();

            return;

        }





        if(senha.value !== confirmarSenha.value){


            alert(
                "As senhas não coincidem."
            );


            event.preventDefault();


        }


    });

