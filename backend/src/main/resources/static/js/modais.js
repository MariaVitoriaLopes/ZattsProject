function abrirModalExcluir(){

    document
        .getElementById("modalExcluir")
        .classList.add("ativo");

}



function fecharModalExcluir(){

    document
        .getElementById("modalExcluir")
        .classList.remove("ativo");

}




function confirmarExclusao(){

    document
        .getElementById("formExcluir")
        .submit();

}