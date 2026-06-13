document.addEventListener('DOMContentLoaded', function () {

    // ══════════════════════════════════════════════
    // ANIMAÇÕES DA TELA (login/cadastro toggle)
    // ══════════════════════════════════════════════
    var container  = document.getElementById('mainContainer');
    var signInBtn  = document.getElementById('signInBtn');
    var signUpBtn  = document.getElementById('signUpBtn');
    var signUpBtn2 = document.getElementById('signUpBtn2');

    if (signUpBtn)  signUpBtn.addEventListener('click',  function () { container.classList.add('register-active'); });
    if (signInBtn)  signInBtn.addEventListener('click',  function () { container.classList.remove('register-active'); });
    if (signUpBtn2) signUpBtn2.addEventListener('click', function () { container.classList.add('register-active'); });

    // ══════════════════════════════════════════════
    // MÁSCARAS (reaplicáveis após AJAX)
    // ══════════════════════════════════════════════
    function aplicarMascaraCPF() {
        document.querySelectorAll('input[placeholder*="CPF"]').forEach(function (input) {
            if (input._cpfMask) return; // evita listener duplicado
            input._cpfMask = true;
            input.addEventListener('input', function (e) {
                var v = e.target.value.replace(/\D/g, '');
                if (v.length <= 11) {
                    v = v.replace(/(\d{3})(\d)/, '$1.$2');
                    v = v.replace(/(\d{3})(\d)/, '$1.$2');
                    v = v.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
                }
                e.target.value = v;
            });
        });
    }

    function aplicarMascaraTelefone() {
        document.querySelectorAll('input[id*="telefone"]').forEach(function (input) {
            if (input._telMask) return;
            input._telMask = true;
            input.addEventListener('input', function (e) {
                var v = e.target.value.replace(/\D/g, '');
                if (v.length > 11) v = v.substring(0, 11);
                if (v.length >= 2) v = '(' + v.substring(0, 2) + ')' + v.substring(2);
                if (v.length >= 9) v = v.substring(0, 8) + '-' + v.substring(8);
                e.target.value = v;
            });
        });
    }

    aplicarMascaraCPF();
    aplicarMascaraTelefone();

    // Reaplicar máscaras após AJAX do JSF
    if (typeof jsf !== 'undefined') {
        jsf.ajax.addOnEvent(function (data) {
            if (data.status === 'success') {
                aplicarMascaraCPF();
                aplicarMascaraTelefone();
            }
        });
    }

    // ══════════════════════════════════════════════
    // VALIDAÇÃO DE E-MAIL EM TEMPO REAL
    // ══════════════════════════════════════════════
    var emailInp  = document.getElementById('cadastroForm:email');
    var emailHint = document.getElementById('emailHint');

    if (emailInp && emailHint) {
        emailInp.addEventListener('input', function () {
            var v = this.value.trim();
            if (!v) {
                setHint(emailHint, '', '');
                setInputClass(emailInp, '');
                return;
            }
            if (!v.includes('@')) {
                setHint(emailHint, 'Falta o @ no e-mail', 'erro');
                setInputClass(emailInp, 'campo-erro');
                return;
            }
            var partes = v.split('@');
            if (!partes[1] || !partes[1].includes('.')) {
                setHint(emailHint, 'Domínio inválido (ex: gmail.com)', 'erro');
                setInputClass(emailInp, 'campo-erro');
                return;
            }
            if (!validarEmail(v)) {
                setHint(emailHint, 'E-mail inválido', 'erro');
                setInputClass(emailInp, 'campo-erro');
                return;
            }
            setHint(emailHint, '✓ E-mail válido', 'ok');
            setInputClass(emailInp, 'campo-ok');
        });
    }

    // ══════════════════════════════════════════════
    // CONFIRMAÇÃO DE SENHA EM TEMPO REAL
    // ══════════════════════════════════════════════
    var senhaInp  = document.getElementById('cadastroForm:senhaCadastro');
    var senha2Inp = document.getElementById('cadastroForm:confirmarSenha');
    var pwHint    = document.getElementById('pwHint');

    function checarSenhas() {
        if (!senha2Inp || !senhaInp) return;
        var s2 = senha2Inp.value;
        if (!s2) {
            setHint(pwHint, '', '');
            setInputClass(senha2Inp, '');
            return;
        }
        if (senhaInp.value === s2) {
            setHint(pwHint, '✓ Senhas coincidem', 'ok');
            setInputClass(senha2Inp, 'campo-ok');
        } else {
            setHint(pwHint, 'Senhas não coincidem', 'erro');
            setInputClass(senha2Inp, 'campo-erro');
        }
    }

    if (senhaInp)  senhaInp.addEventListener('input',  checarSenhas);
    if (senha2Inp) senha2Inp.addEventListener('input', checarSenhas);

    // ══════════════════════════════════════════════
    // ÍCONE OLHO NOS CAMPOS DE SENHA
    // ══════════════════════════════════════════════
    document.querySelectorAll('input[type="password"]').forEach(function (input) {
        var wrapper = input.parentNode;
        wrapper.style.position = 'relative';
        input.style.paddingRight = '42px';

        var btn = document.createElement('button');
        btn.type = 'button';
        btn.setAttribute('tabindex', '-1');
        btn.style.cssText =
            'position:absolute;right:10px;top:50%;transform:translateY(-50%);' +
            'background:none;border:none;cursor:pointer;color:#aaa;font-size:15px;z-index:99;padding:0;';
        btn.innerHTML = '<i class="fas fa-eye"></i>';

        btn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            var icone = this.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                icone.classList.replace('fa-eye', 'fa-eye-slash');
                btn.style.color = '#7c3aed';
            } else {
                input.type = 'password';
                icone.classList.replace('fa-eye-slash', 'fa-eye');
                btn.style.color = '#aaa';
            }
        });

        wrapper.appendChild(btn);
    });
});

// ══════════════════════════════════════════════
// MÁSCARA CPF TELA DE LOGIN (chamada via onkeyup)
// ══════════════════════════════════════════════
function mascaraCpfLogin(campo) {
    var v = campo.value.replace(/\D/g, '').substring(0, 11);
    if (v.length > 9)      v = v.replace(/^(\d{3})(\d{3})(\d{3})(\d{1,2})$/, '$1.$2.$3-$4');
    else if (v.length > 6) v = v.replace(/^(\d{3})(\d{3})(\d{1,3})$/, '$1.$2.$3');
    else if (v.length > 3) v = v.replace(/^(\d{3})(\d{1,3})$/, '$1.$2');
    campo.value = v;
}

// ══════════════════════════════════════════════
// MÁSCARAS CADASTRO (chamadas via onkeyup no xhtml)
// ══════════════════════════════════════════════
function mascaraCpf(campo) {
    var v = campo.value.replace(/\D/g, '').substring(0, 11);
    if (v.length > 9)      v = v.replace(/^(\d{3})(\d{3})(\d{3})(\d{1,2})$/, '$1.$2.$3-$4');
    else if (v.length > 6) v = v.replace(/^(\d{3})(\d{3})(\d{1,3})$/, '$1.$2.$3');
    else if (v.length > 3) v = v.replace(/^(\d{3})(\d{1,3})$/, '$1.$2');
    campo.value = v;
}

function mascaraTelefone(campo) {
    var v = campo.value.replace(/\D/g, '').substring(0, 11);
    if (v.length > 10)     v = v.replace(/^(\d{2})(\d{5})(\d{4})$/, '($1) $2-$3');
    else if (v.length > 6) v = v.replace(/^(\d{2})(\d{4,5})(\d{0,4})$/, '($1) $2-$3');
    else if (v.length > 2) v = v.replace(/^(\d{2})(\d{0,5})$/, '($1) $2');
    else if (v.length > 0) v = v.replace(/^(\d{0,2})$/, '($1');
    campo.value = v;
}

function mascaraCep(campo) {
    var v = campo.value.replace(/\D/g, '').substring(0, 8);
    if (v.length > 5) v = v.replace(/^(\d{5})(\d{1,3})$/, '$1-$2');
    campo.value = v;
}

// ══════════════════════════════════════════════
// TOGGLE SENHA TELA DE LOGIN (chamado via onclick)
// ══════════════════════════════════════════════
function toggleSenhaLogin(inputId, botao) {
    var input = document.getElementById(inputId);
    var icone = botao.querySelector('i');
    if (!input || !icone) return;
    if (input.type === 'password') {
        input.type = 'text';
        icone.classList.remove('fa-eye');
        icone.classList.add('fa-eye-slash');
    } else {
        input.type = 'password';
        icone.classList.remove('fa-eye-slash');
        icone.classList.add('fa-eye');
    }
}

// ══════════════════════════════════════════════
// HELPERS
// ══════════════════════════════════════════════
function validarEmail(v) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v);
}

function setHint(el, msg, tipo) {
    if (!el) return;
    el.textContent = msg;
    el.className = 'campo-hint' + (tipo ? ' ' + tipo : '');
}

function setInputClass(inp, cls) {
    if (!inp) return;
    inp.classList.remove('campo-ok', 'campo-erro');
    if (cls) inp.classList.add(cls);
}

// ══════════════════════════════════════════════
// BLOQUEIO NO SUBMIT
// ══════════════════════════════════════════════
function validarFormulario() {
    var emailInp  = document.getElementById('cadastroForm:email');
    var senhaInp  = document.getElementById('cadastroForm:senhaCadastro');
    var senha2Inp = document.getElementById('cadastroForm:confirmarSenha');

    if (emailInp && !validarEmail(emailInp.value.trim())) {
        emailInp.focus();
        return false;
    }
    if (senhaInp && senha2Inp && senhaInp.value !== senha2Inp.value) {
        senha2Inp.focus();
        return false;
    }
    return true;
}

// ══════════════════════════════════════════════
// TOAST DE SUCESSO
// ══════════════════════════════════════════════
function mostrarToast() {
    var toast = document.getElementById('toastSucesso');
    if (!toast) return;
    toast.classList.add('visivel');
    setTimeout(function () { toast.classList.remove('visivel'); }, 3500);
}

// Callback do f:ajax onevent="tratarEventoAjax"
function tratarEventoAjax(data) {
    if (data.status !== 'success') return;
    var msgPanel    = document.getElementById('cadastroForm:messages');
    var temErro     = msgPanel && msgPanel.querySelector('.ui-messages-error, [class*="error"]');
    var textoMsg    = msgPanel ? msgPanel.textContent.trim() : '';
    var cadastrouOk = textoMsg === '' || textoMsg.toLowerCase().includes('sucesso');
    if (!temErro && cadastrouOk) mostrarToast();
}
