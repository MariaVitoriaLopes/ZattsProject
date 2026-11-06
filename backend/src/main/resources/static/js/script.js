document.addEventListener("DOMContentLoaded", function () {
    iniciarRelogio();
});

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

            dataFormatada = dataFormatada.charAt(0).toUpperCase() + dataFormatada.slice(1);
            data.textContent = dataFormatada;
        }
    }

    atualizarHorario();
    setInterval(atualizarHorario, 1000);
}

// =====================================================
// UTILITÁRIOS E TEMAS PARA GRÁFICOS
// =====================================================

function coresDoTema() {
    const escuro = document.documentElement.getAttribute("data-tema") === "escuro";

    const destaque = getComputedStyle(document.documentElement)
        .getPropertyValue("--cor-primaria-400")
        .trim() || "#C2F012";

    return {
        destaque: destaque,
        texto: escuro ? "#A3A3A3" : "#9CA3AF",
        grade: escuro ? "#2C2C2C" : "#F3F4F6",
        tooltip: escuro ? "#2A2A2A" : "#1E293B"
    };
}

function formatarKwh(valor) {
    if (valor === null || valor === undefined) return "-- kWh";
    return Number(valor).toLocaleString("pt-BR", {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    }) + " kWh";
}

function formatarReais(valor) {
    if (valor === null || valor === undefined) return "R$ 0,00";
    return Number(valor).toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL"
    });
}