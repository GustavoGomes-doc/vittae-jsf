document.addEventListener('DOMContentLoaded', () => {

    const container = document.getElementById('mainContainer');
    const signInBtn = document.getElementById('signInBtn');
    const signUpBtn = document.getElementById('signUpBtn');
    const signUpBtn2 = document.getElementById('signUpBtn2');

    // --- ANIMAÇÕES DA TELA ---
    if (signUpBtn) signUpBtn.addEventListener('click', () => container.classList.add("register-active"));
    if (signInBtn) signInBtn.addEventListener('click', () => container.classList.remove("register-active"));
    if (signUpBtn2) signUpBtn2.addEventListener('click', () => container.classList.add('register-active'));

    // --- MÁSCARA DE CPF (Já existente) ---
    const aplicarMascaraCPF = () => {
        const cpfInputs = document.querySelectorAll('input[placeholder*="CPF"]');
        cpfInputs.forEach(input => {
            input.addEventListener('input', (e) => {
                let value = e.target.value.replace(/\D/g, ""); 
                if (value.length <= 11) {
                    value = value.replace(/(\d{3})(\d)/, "$1.$2");
                    value = value.replace(/(\d{3})(\d)/, "$1.$2");
                    value = value.replace(/(\d{3})(\d{1,2})$/, "$1-$2");
                }
                e.target.value = value;
            });
        });
    };

	const aplicarMascaraTelefone = () => {
	    const telInputs = document.querySelectorAll('input[id*="telefone"]');
	    telInputs.forEach(input => {
	        input.addEventListener('input', (e) => {
	            let v = e.target.value.replace(/\D/g, ""); // Remove letras
	            if (v.length > 11) v = v.substring(0, 11); // Limita 11 dígitos
	            
	            if (v.length >= 2) {
	                v = "(" + v.substring(0, 2) + ")" + v.substring(2);
	            }
	            if (v.length >= 9) {
	                v = v.substring(0, 8) + "-" + v.substring(8);
	            }
	            e.target.value = v;
	        });
	    });
	};

    // Executa as máscaras ao carregar
    aplicarMascaraCPF();
    aplicarMascaraTelefone();

    // DICA PARA JSF: Reaplicar máscaras após chamadas AJAX
    if (typeof jsf !== 'undefined') {
        jsf.ajax.addOnEvent(function(data) {
            if (data.status === 'success') {
                aplicarMascaraCPF();
                aplicarMascaraTelefone();
            }
        });
    }
});