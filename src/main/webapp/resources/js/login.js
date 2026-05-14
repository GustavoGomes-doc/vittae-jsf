document.addEventListener('DOMContentLoaded', () => {

    const container = document.getElementById('mainContainer');
    const signInBtn = document.getElementById('signInBtn');
    const signUpBtn = document.getElementById('signUpBtn');
    const signUpBtn2 = document.getElementById('signUpBtn2');

    // --- ANIMAÇÕES DA TELA (Deslizar painel) ---
    if (signUpBtn) {
        signUpBtn.addEventListener('click', () => container.classList.add("register-active"));
    }

    if (signInBtn) {
        signInBtn.addEventListener('click', () => container.classList.remove("register-active"));
    }

    if (signUpBtn2) {
        signUpBtn2.addEventListener('click', () => container.classList.add('register-active'));
    }

    // --- MÁSCARA DE CPF ---
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

    // --- VALIDAÇÃO DO FORMULÁRIO DE LOGIN PARA JSF ---
    // Em JSF, o formulário é submetido nativamente para o Bean.
    const loginForm = document.getElementById('loginForm'); 
    const loginError = document.getElementById('loginError');
    const btnLogar = document.getElementById('btnLogar');

    if (loginForm) {
        loginForm.addEventListener('submit', (e) => {
            // Dica: Se você usar <h:form id="loginForm"> e <h:inputText id="loginCpf">,
            // o ID gerado pelo JSF será "loginForm:loginCpf". Ajuste os IDs abaixo se necessário!
            const cpfInput = document.getElementById('loginCpf');
            const senhaInput = document.getElementById('loginSenha');

            // Pega o valor e já remove a pontuação para verificar o tamanho
            const cpf = cpfInput ? cpfInput.value.replace(/\D/g, "") : "";
            const senha = senhaInput ? senhaInput.value : "";

            // Validação simples de Front-end (evita ir no servidor atoa)
            if (cpf.length !== 11 || senha.trim() === "") {
                e.preventDefault(); // Impede o envio para o JSF
                
                if (loginError) {
                    loginError.innerText = "Por favor, preencha o CPF corretamente e informe a senha.";
                    loginError.style.display = "block";
                }
            } else {
                // Se tudo estiver preenchido, DEIXA O FORMULÁRIO IR!
                // Ocultamos erros e damos um feedback visual.
                if (loginError) loginError.style.display = "none";
                if (btnLogar) btnLogar.innerText = "Acessando...";
                
                // NOTA: SEM 'e.preventDefault()' aqui. 
                // O navegador enviará os dados e o botão <h:commandButton> chamará o método no seu LoginBean.
            }
        });
    }
});