// Aguarda a página carregar
document.addEventListener("DOMContentLoaded", function() {

    // 1. MÁSCARA DE CPF (000.000.000-00)
    const cpfInput = document.getElementById("cpfCadastro");
    if (cpfInput) {
        cpfInput.addEventListener("input", function(e) {
            let v = e.target.value.replace(/\D/g, ""); // Remove tudo o que não é dígito
            v = v.replace(/(\d{3})(\d)/, "$1.$2");     // Coloca o primeiro ponto
            v = v.replace(/(\d{3})(\d)/, "$1.$2");     // Coloca o segundo ponto
            v = v.replace(/(\d{3})(\d{1,2})$/, "$1-$2"); // Coloca o traço
            e.target.value = v;
        });
    }

    // 2. MÁSCARA DE TELEFONE ((11) 90000-0000)
    const telInput = document.querySelector(".js-phone");
    if (telInput) {
        telInput.addEventListener("input", function(e) {
            let v = e.target.value.replace(/\D/g, ""); 
            v = v.replace(/^(\d{2})(\d)/g, "($1) $2"); // Coloca os parênteses
            v = v.replace(/(\d)(\d{4})$/, "$1-$2");    // Coloca o traço no meio
            e.target.value = v;
        });
    }

    // 3. MÁSCARA DE CEP (00000-000)
    const cepInput = document.getElementById("cep");
    if (cepInput) {
        cepInput.addEventListener("input", function(e) {
            let v = e.target.value.replace(/\D/g, "");
            v = v.replace(/^(\d{5})(\d)/, "$1-$2");    // Coloca o traço após o 5º dígito
            e.target.value = v;
        });
    }

});