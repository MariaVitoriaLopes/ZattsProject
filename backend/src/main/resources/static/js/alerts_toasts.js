const toasts = document.querySelectorAll(".toast-mensagem");


toasts.forEach(toast => {

    setTimeout(() => {

        toast.style.opacity = "0";

        setTimeout(() => {
            toast.remove();
        }, 500);

    }, 3000);

});