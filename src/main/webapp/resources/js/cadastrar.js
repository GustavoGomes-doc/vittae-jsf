document.addEventListener('DOMContentLoaded', function () {

    // ── Referências ──────────────────────────────────────────────────────────
    var form         = document.getElementById('formCadastro');
    var cpfInput     = document.getElementById('formCadastro:cpfMedico');
    var telefoneInput= document.getElementById('formCadastro:telefoneMedico');
    var fotoInput    = document.getElementById('formCadastro:fotoMedico');
    var fotoCirculo  = document.getElementById('fotoCirculo');
    var fotoPreview  = document.getElementById('fotoPreview');
    var fotoIcone    = document.getElementById('fotoIcone');
    var nascInput    = document.getElementById('formCadastro:dataNascimento');
    var valorInput   = document.getElementById('formCadastro:valorConsulta');
    var nomeInput    = document.getElementById('formCadastro:nomeMedico');

    var ESPECIALIDADES = [
        'Cardiologia','Dermatologia','Pediatria','Ortopedia',
        'Ginecologia','Oftalmologia','Neurologia','Psiquiatria',
        'Endocrinologia','Urologia','Otorrinolaringologia',
        'Gastroenterologia','Clínico Geral','Oncologia',
        'Reumatologia','Infectologia'
    ];

    var especialidadesSelecionadas = [];

    // ── Foto ─────────────────────────────────────────────────────────────────
    if (fotoCirculo) fotoCirculo.addEventListener('click', function () { fotoInput.click(); });

    if (fotoInput) {
        fotoInput.addEventListener('change', function () {
            var file = this.files[0];
            if (!file) return;
            if (file.size > 5 * 1024 * 1024) { mostrarToast('Foto maior que 5 MB.', 'erro'); return; }
            var reader = new FileReader();
            reader.onload = function (e) {
                fotoPreview.src = e.target.result;
                fotoPreview.style.display = 'block';
                fotoIcone.style.display = 'none';
            };
            reader.readAsDataURL(file);
        });
    }

    // ── Máscara CPF ──────────────────────────────────────────────────────────
    if (cpfInput) {
        cpfInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').slice(0, 11);
            v = v.replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
            this.value = v;
        });
    }

    // ── Máscara Telefone ─────────────────────────────────────────────────────
    if (telefoneInput) {
        telefoneInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').slice(0, 11);
            if (v.length > 10) {
                v = v.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
            } else {
                v = v.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
            }
            this.value = v;
        });
    }

    // ── Capitalização automática do nome ─────────────────────────────────────
    if (nomeInput) {
        nomeInput.addEventListener('input', function () {
            var pos = this.selectionStart;
            this.value = this.value.replace(/\b\w/g, function (c) { return c.toUpperCase(); });
            this.setSelectionRange(pos, pos);
        });
    }

    // ── Máscara valor da consulta ────────────────────────────────────────────
    // Digita apenas números, formata como moeda brasileira
    if (valorInput) {
        valorInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').slice(0, 6); // máx 9999,99
            if (v === '') { this.value = ''; return; }
            var num = parseInt(v, 10);
            // Converte centavos: 35000 → 350,00
            var reais = Math.floor(num / 100);
            var centavos = num % 100;
            this.value = reais + ',' + (centavos < 10 ? '0' : '') + centavos;
        });

        // Ao sair do campo, normaliza para envio (troca , por . para o backend)
        valorInput.addEventListener('blur', function () {
            var v = this.value.replace('.', '').replace(',', '.');
            var num = parseFloat(v);
            if (!isNaN(num) && num > 0) {
                // Mantém display com vírgula, mas o hidden pode ser ajustado se necessário
                var reais = Math.floor(num);
                var cents = Math.round((num - reais) * 100);
                this.value = reais + ',' + (cents < 10 ? '0' : '') + cents;
            }
        });
    }

    // ── Data de Nascimento com hint de idade ─────────────────────────────────
    if (nascInput) {
        // Cria hint dinâmico — insere depois do input, antes do h:message
        var hintNasc = document.createElement('span');
        hintNasc.id = 'hint-nasc';
        hintNasc.style.cssText = 'font-size:12px;font-weight:600;display:block;margin-top:3px;min-height:16px;';
        // Insere logo após o input de nascimento
        nascInput.parentNode.insertBefore(hintNasc, nascInput.nextSibling);

        nascInput.addEventListener('input', function () {
            // Máscara DD/MM/AAAA
            var v = this.value.replace(/\D/g, '').slice(0, 8);
            v = v.replace(/(\d{2})(\d)/, '$1/$2').replace(/(\d{2})(\d)/, '$1/$2');
            this.value = v;

            hintNasc.style.display = 'none';
            ocultarErro('nasc');

            if (v.length < 10) return;

            var partes = v.split('/');
            var dia = parseInt(partes[0], 10);
            var mes = parseInt(partes[1], 10);
            var ano = parseInt(partes[2], 10);
            var data = new Date(ano, mes - 1, dia);

            var invalida = isNaN(data.getTime())
                || data.getDate() !== dia
                || data.getMonth() !== mes - 1
                || data.getFullYear() !== ano
                || data > new Date();

            if (invalida) {
                hintNasc.style.display = 'block';
                hintNasc.style.color = '#ef4444';
                hintNasc.textContent = 'Data inválida';
                return;
            }

            var hoje = new Date();
            var idade = hoje.getFullYear() - data.getFullYear();
            var m = hoje.getMonth() - data.getMonth();
            if (m < 0 || (m === 0 && hoje.getDate() < data.getDate())) idade--;

            hintNasc.style.display = 'block';

            if (idade < 25) {
                hintNasc.style.color = '#ef4444';
                hintNasc.textContent = idade + ' anos — mínimo 25 anos para cadastro';
                marcarInputErro('formCadastro:dataNascimento', true);
            } else if (idade > 85) {
                hintNasc.style.color = '#ef4444';
                hintNasc.textContent = idade + ' anos — máximo 85 anos para cadastro';
                marcarInputErro('formCadastro:dataNascimento', true);
            } else {
                hintNasc.style.color = '#34d399';
                hintNasc.textContent = idade + ' anos';
                marcarInputErro('formCadastro:dataNascimento', false);
            }
        });
    }

    // ── Máscara de horário ───────────────────────────────────────────────────
    // Digita "9" → "09:00" | "123" → "12:30" | "1234" → "12:34"
    function aplicarMascaraHora(input) {
        input.addEventListener('keydown', function (e) {
            // Permite: backspace, delete, tab, setas, ':'
            var permite = [8, 9, 35, 36, 37, 38, 39, 40, 46];
            if (permite.indexOf(e.keyCode) !== -1) return;
            // Bloqueia qualquer coisa que não seja número
            if (e.key < '0' || e.key > '9') {
                e.preventDefault();
                return;
            }
            // Bloqueia se já tem 5 caracteres (HH:MM completo)
            if (this.value.length >= 5 && this.selectionStart === this.selectionEnd) {
                e.preventDefault();
            }
        });

        input.addEventListener('input', function () {
            var raw = this.value.replace(/\D/g, '').slice(0, 4);
            if (raw.length === 0) { this.value = ''; return; }

            // Se o primeiro dígito for >= 3, hora só pode ser de 1 dígito → adiciona 0 na frente
            // Ex: digita "8" → raw vira "08" → insere "08:" automaticamente
            if (raw.length === 1 && parseInt(raw, 10) >= 3) {
                raw = '0' + raw;
                // Com 2 dígitos já formados, insere o ':' e posiciona cursor após ele
                this.value = raw + ':';
                return;
            }

            // Insere ':' automaticamente após os 2 primeiros dígitos
            if (raw.length <= 2) {
                this.value = raw;
                // Se completou 2 dígitos e primeiro dígito indica hora válida, insere ':'
                if (raw.length === 2) {
                    this.value = raw + ':';
                }
            } else {
                this.value = raw.slice(0, 2) + ':' + raw.slice(2);
            }
        });
    }

    // ── Toggle disponibilidade (abre/fecha campos de hora) ───────────────────
    window.toggleDisp = function (checkbox, valorEnum) {
        var row = document.getElementById('row-' + valorEnum);
        if (!row) return;

        if (checkbox.checked) {
            row.classList.add('ativa');
            // Habilita e aplica máscara nos inputs de hora dessa linha
            var inputs = row.querySelectorAll('.input-hora-texto');
            inputs.forEach(function (inp) {
                inp.disabled = false;
                inp.style.opacity = '1';
                inp.style.pointerEvents = 'auto';
                // Aplica máscara apenas uma vez
                if (!inp.dataset.mascaraAplicada) {
                    aplicarMascaraHora(inp);
                    inp.dataset.mascaraAplicada = '1';

                    // Validação ao sair do campo
                    inp.addEventListener('blur', function () {
                        validarHorasDia(valorEnum);
                    });
                }
            });
        } else {
            row.classList.remove('ativa');
            var inputs = row.querySelectorAll('.input-hora-texto');
            inputs.forEach(function (inp) {
                inp.value = '';
                inp.disabled = true;
                inp.style.opacity = '0.35';
                inp.style.pointerEvents = 'none';
            });
            ocultarErroHora(valorEnum);
        }
    };

    function horaParaMinutos(horaStr) {
        if (!horaStr || horaStr.length < 5) return null;
        var partes = horaStr.split(':');
        return parseInt(partes[0], 10) * 60 + parseInt(partes[1], 10);
    }

    function validarHorasDia(valorEnum) {
        var row = document.getElementById('row-' + valorEnum);
        if (!row) return true;

        var inputs = row.querySelectorAll('.input-hora-texto');
        if (inputs.length < 2) return true;

        var horaInicio = inputs[0].value;
        var horaFim    = inputs[1].value;

        if (!horaInicio || !horaFim) return true; // ainda digitando

        var minInicio = horaParaMinutos(horaInicio);
        var minFim    = horaParaMinutos(horaFim);

        // Mínimo 07:00, máximo 19:00
        if (minInicio < 7 * 60) {
            mostrarErroHora(valorEnum, 'Início mínimo: 07:00');
            return false;
        }
        if (minFim > 19 * 60) {
            mostrarErroHora(valorEnum, 'Fim máximo: 19:00');
            return false;
        }
        if (minInicio >= minFim) {
            mostrarErroHora(valorEnum, 'Início deve ser menor que o fim');
            return false;
        }

        ocultarErroHora(valorEnum);
        return true;
    }

    function mostrarErroHora(valorEnum, msg) {
        var span = document.getElementById('err-hora-' + valorEnum);
        if (!span) return;
        var msgEl = span.querySelector('.err-hora-msg');
        if (msgEl) msgEl.textContent = msg || 'Horário inválido';
        span.style.display = 'flex';
    }

    function ocultarErroHora(valorEnum) {
        var span = document.getElementById('err-hora-' + valorEnum);
        if (span) span.style.display = 'none';
    }

    // Inicializa os inputs de hora como desabilitados
    document.querySelectorAll('.input-hora-texto').forEach(function (inp) {
        inp.disabled = true;
        inp.style.opacity = '0.35';
        inp.style.pointerEvents = 'none';
    });

    // ── Chips de especialidade ───────────────────────────────────────────────
    function inicializarChips() {
        var container = document.getElementById('chipsContainer');
        if (!container) return;
        ESPECIALIDADES.forEach(function (nome) {
            var chip = document.createElement('div');
            chip.className = 'chip';
            chip.textContent = nome;
            chip.dataset.nome = nome;
            chip.addEventListener('click', function () { toggleChip(chip, nome); });
            container.appendChild(chip);
        });
    }

    function toggleChip(chip, nome) {
        if (chip.classList.contains('bloqueado')) return;
        if (chip.classList.contains('ativo')) {
            chip.classList.remove('ativo');
            especialidadesSelecionadas = especialidadesSelecionadas.filter(function (e) { return e !== nome; });
        } else {
            if (especialidadesSelecionadas.length >= 3) {
                mostrarToast('Máximo de 3 especialidades.', 'erro');
                return;
            }
            chip.classList.add('ativo');
            especialidadesSelecionadas.push(nome);
        }
        var hidden = document.getElementById('formCadastro:especialidadesHidden');
        if (hidden) hidden.value = especialidadesSelecionadas.join(',');
        atualizarChips();
        if (especialidadesSelecionadas.length > 0) ocultarErro('especialidade');
    }

    function atualizarChips() {
        var max = especialidadesSelecionadas.length >= 3;
        document.querySelectorAll('.chip').forEach(function (c) {
            if (!c.classList.contains('ativo')) c.classList.toggle('bloqueado', max);
        });
        var contador = document.getElementById('chipsContador');
        if (contador) contador.textContent = especialidadesSelecionadas.length + '/3 selecionadas';
    }

    // ── Helpers de erro ──────────────────────────────────────────────────────
    function exibirErro(sufixo, msg) {
        var span = document.getElementById('err-' + sufixo);
        if (span) { if (msg) span.textContent = msg; span.classList.add('visivel'); }
    }

    function ocultarErro(sufixo) {
        var span = document.getElementById('err-' + sufixo);
        if (span) span.classList.remove('visivel');
    }

    function marcarInputErro(inputId, temErro) {
        var el = document.getElementById(inputId);
        if (el) el.classList.toggle('input-invalido', temErro);
    }

    // ── Validação completa ───────────────────────────────────────────────────
    function validarFormulario() {
        var valido = true;

        // Nome
        var nomeEl = document.getElementById('formCadastro:nomeMedico');
        var nome = nomeEl ? nomeEl.value.trim() : '';
        marcarInputErro('formCadastro:nomeMedico', nome.split(/\s+/).length < 2);
        if (nome.split(/\s+/).length < 2) { exibirErro('nome', 'Informe nome e sobrenome'); valido = false; }
        else ocultarErro('nome');

        // CPF
        var cpf = cpfInput ? cpfInput.value.replace(/\D/g, '') : '';
        marcarInputErro('formCadastro:cpfMedico', cpf.length !== 11);
        if (cpf.length !== 11) { exibirErro('cpf', 'CPF incompleto'); valido = false; }
        else ocultarErro('cpf');

        // Telefone
        var tel = telefoneInput ? telefoneInput.value.replace(/\D/g, '') : '';
        marcarInputErro('formCadastro:telefoneMedico', tel.length < 10);
        if (tel.length < 10) { exibirErro('telefone', 'Telefone inválido'); valido = false; }
        else ocultarErro('telefone');

        // Data nascimento + idade
        var nasc = nascInput ? nascInput.value : '';
        var idadeValida = false;
        if (nasc.length < 10) {
            marcarInputErro('formCadastro:dataNascimento', true);
            exibirErro('nasc', 'Data obrigatória');
            valido = false;
        } else {
            var partes = nasc.split('/');
            var data = new Date(partes[2], partes[1] - 1, partes[0]);
            var hoje = new Date();
            var idade = hoje.getFullYear() - data.getFullYear();
            var mm = hoje.getMonth() - data.getMonth();
            if (mm < 0 || (mm === 0 && hoje.getDate() < data.getDate())) idade--;

            if (idade < 25 || idade > 85) {
                marcarInputErro('formCadastro:dataNascimento', true);
                exibirErro('nasc', idade < 25 ? 'Mínimo 25 anos' : 'Máximo 85 anos');
                valido = false;
            } else {
                marcarInputErro('formCadastro:dataNascimento', false);
                ocultarErro('nasc');
                idadeValida = true;
            }
        }

        // CRM
        var crmEl = document.getElementById('formCadastro:crmMedico');
        var crm = crmEl ? crmEl.value.trim() : '';
        marcarInputErro('formCadastro:crmMedico', !/^\d{1,6}$/.test(crm));
        if (!/^\d{1,6}$/.test(crm)) { exibirErro('crm', 'CRM inválido (até 6 dígitos)'); valido = false; }
        else ocultarErro('crm');

        // UF
        var ufEl = document.getElementById('formCadastro:ufCrm');
        var uf = ufEl ? ufEl.value : '';
        marcarInputErro('formCadastro:ufCrm', !uf);
        if (!uf) { exibirErro('uf', 'Selecione a UF'); valido = false; }
        else ocultarErro('uf');

        // Valor consulta
        var valorStr = valorInput ? valorInput.value.replace(',', '.') : '';
        var valor = parseFloat(valorStr);
        marcarInputErro('formCadastro:valorConsulta', isNaN(valor) || valor <= 0 || valor > 1000);
        if (isNaN(valor) || valor <= 0) { exibirErro('valor', 'Informe um valor maior que zero'); valido = false; }
        else if (valor > 1000) { exibirErro('valor', 'Valor máximo: R$ 1.000,00'); valido = false; }
        else ocultarErro('valor');

        // Duração
        var tempoEl = document.getElementById('formCadastro:tempoConsulta');
        var tempo = tempoEl ? parseInt(tempoEl.value) : 0;
        marcarInputErro('formCadastro:tempoConsulta', isNaN(tempo) || tempo < 21 || tempo > 90);
        if (isNaN(tempo) || tempo < 21 || tempo > 90) { exibirErro('tempo', 'Duração entre 21 e 90 minutos'); valido = false; }
        else ocultarErro('tempo');

        // Especialidades
        if (especialidadesSelecionadas.length === 0) { exibirErro('especialidade'); valido = false; }
        else ocultarErro('especialidade');

        // Disponibilidades
        var checkboxes = document.querySelectorAll('#formCadastro input[type="checkbox"]');
        var diasMarcados = Array.from(checkboxes).some(function (el) { return el.checked; });
        if (!diasMarcados) { exibirErro('disp', 'Selecione ao menos um dia'); valido = false; }
        else {
            // Valida horários de cada dia marcado
            var diasEnums = ['SEGUNDA','TERCA','QUARTA','QUINTA','SEXTA','SABADO'];
            diasEnums.forEach(function (e) {
                var row = document.getElementById('row-' + e);
                if (row && row.classList.contains('ativa')) {
                    if (!validarHorasDia(e)) valido = false;
                }
            });
            ocultarErro('disp');
        }

        // Email
        var emailEl = document.getElementById('formCadastro:emailMedico');
        var email = emailEl ? emailEl.value.trim() : '';
        var emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
        marcarInputErro('formCadastro:emailMedico', !emailOk);
        if (!emailOk) { exibirErro('email', 'E-mail inválido'); valido = false; }
        else ocultarErro('email');

        // Senha
        var senhaEl = document.getElementById('formCadastro:senhaMedico');
        var senha = senhaEl ? senhaEl.value : '';
        marcarInputErro('formCadastro:senhaMedico', senha.length < 6);
        if (senha.length < 6) { exibirErro('senha', 'Mínimo 6 caracteres'); valido = false; }
        else ocultarErro('senha');

        return valido;
    }

    // ── Submit ───────────────────────────────────────────────────────────────
    if (form) {
        form.addEventListener('submit', function (e) {
            if (!validarFormulario()) {
                e.preventDefault();
                mostrarToast('Corrija os erros antes de salvar.', 'erro');
            }
        });
    }

    // ── Toast ────────────────────────────────────────────────────────────────
    function mostrarToast(mensagem, tipo) {
        var toast = document.getElementById('toast');
        if (!toast) return;
        toast.textContent = mensagem;
        toast.className = 'toast ' + (tipo || 'sucesso') + ' visivel';
        setTimeout(function () { toast.classList.remove('visivel'); }, 3000);
    }

    // ── Init ─────────────────────────────────────────────────────────────────
    inicializarChips();

});