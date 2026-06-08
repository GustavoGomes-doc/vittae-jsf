document.addEventListener('DOMContentLoaded', function () {

    // Telefone
    var campoTel = document.querySelector('input[id$="novoTelefone"]');
    if (campoTel) {
        campoTel.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').substring(0, 11);
            if (v.length <= 10) {
                v = v.replace(/(\d{2})(\d)/, '($1) $2');
                v = v.replace(/(\d{4})(\d)/, '$1-$2');
            } else {
                v = v.replace(/(\d{2})(\d)/, '($1) $2');
                v = v.replace(/(\d{5})(\d)/, '$1-$2');
            }
            this.value = v;
        });
    }

    // Email
    var campoEmail = document.querySelector('input[id$="novoEmail"]');
    if (campoEmail) {
        campoEmail.addEventListener('blur', function () {
            var val = this.value;
            var valido = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
            this.style.borderColor = (!val || valido) ? '' : '#ef4444';
            var msg = document.getElementById('emailErro');
            if (!valido && val) {
                if (!msg) {
                    msg = document.createElement('span');
                    msg.id = 'emailErro';
                    msg.style.cssText = 'color:#ef4444;font-size:12px;margin-top:4px;display:block;';
                    msg.textContent = 'Email inválido';
                    campoEmail.parentNode.appendChild(msg);
                }
            } else if (msg) {
                msg.remove();
            }
        });
    }

    // Confirmação de senha em tempo real
    var novaSenha     = document.querySelector('input[id$="novaSenha"]');
    var confirmarSenha = document.querySelector('input[id$="confirmarSenha"]');
    var btnAlterarSenha = document.querySelector('.btn-perfil-salvar:last-of-type');

    function verificarSenhas() {
        if (!novaSenha || !confirmarSenha) return;
        var msg = document.getElementById('senhaMsg');
        if (!msg) {
            msg = document.createElement('span');
            msg.id = 'senhaMsg';
            msg.style.cssText = 'font-size:12px;margin-top:4px;display:block;';
            confirmarSenha.parentNode.appendChild(msg);
        }

        var nova = novaSenha.value;
        var confirmar = confirmarSenha.value;

        if (!confirmar) {
            msg.textContent = '';
            confirmarSenha.style.borderColor = '';
            return;
        }

        if (nova === confirmar) {
            msg.textContent = '✓ Senhas coincidem';
            msg.style.color = '#16a34a';
            confirmarSenha.style.borderColor = '#16a34a';
        } else {
            msg.textContent = '✗ Senhas não coincidem';
            msg.style.color = '#ef4444';
            confirmarSenha.style.borderColor = '#ef4444';
        }
    }

    // Força mínimo de 6 caracteres na nova senha
    if (novaSenha) {
        novaSenha.addEventListener('input', function () {
            var msg = document.getElementById('senhaTamanhoMsg');
            if (!msg) {
                msg = document.createElement('span');
                msg.id = 'senhaTamanhoMsg';
                msg.style.cssText = 'font-size:12px;margin-top:4px;display:block;';
                novaSenha.parentNode.appendChild(msg);
            }
            if (this.value.length > 0 && this.value.length < 6) {
                msg.textContent = '✗ Mínimo 6 caracteres';
                msg.style.color = '#ef4444';
                novaSenha.style.borderColor = '#ef4444';
            } else if (this.value.length >= 6) {
                msg.textContent = '✓ OK';
                msg.style.color = '#16a34a';
                novaSenha.style.borderColor = '#16a34a';
            } else {
                msg.textContent = '';
                novaSenha.style.borderColor = '';
            }
            verificarSenhas();
        });
    }

    if (confirmarSenha) {
        confirmarSenha.addEventListener('input', verificarSenhas);
    }

});