// Ativa ou desativa a linha de horário quando o checkbox do dia é marcado/desmarcado
function toggleDisp(checkbox, valorEnum) {
    const row = document.getElementById('row-' + valorEnum);
    const inputs = row.querySelectorAll('.input-hora-texto');

    if (checkbox.checked) {
        row.classList.add('ativa');
        inputs.forEach(i => { i.disabled = false; });
    } else {
        row.classList.remove('ativa');
        inputs.forEach(i => {
            i.disabled = true;
            i.value = '';
            i.classList.remove('input-invalido');
        });
        // Limpa o erro da linha ao desmarcar
        const errSpan = document.getElementById('err-hora-' + valorEnum);
        if (errSpan) errSpan.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', () => {

    const form          = document.getElementById('formCadastro');
    const cpfInput      = document.getElementById('formCadastro:cpfMedico');
    const cepInput      = document.getElementById('formCadastro:cepMedico');
    const fotoInput     = document.getElementById('formCadastro:fotoMedico');
    const telefoneInput = document.getElementById('formCadastro:telefoneMedico');
    const fotoCirculo   = document.getElementById('fotoCirculo');
    const fotoPreview   = document.getElementById('fotoPreview');
    const fotoIcone     = document.getElementById('fotoIcone');

    const ESPECIALIDADES = [
        'Cardiologia', 'Dermatologia', 'Pediatria', 'Ortopedia',
        'Ginecologia', 'Oftalmologia', 'Neurologia', 'Psiquiatria',
        'Endocrinologia', 'Urologia', 'Otorrinolaringologia',
        'Gastroenterologia', 'Clínico Geral', 'Oncologia',
        'Reumatologia', 'Infectologia'
    ];

    let especialidadesSelecionadas = [];


    // -------------------------------------------------------------------
    // FOTO
    // -------------------------------------------------------------------

    // Abre o seletor de arquivo ao clicar no círculo da foto
    if (fotoCirculo) fotoCirculo.addEventListener('click', () => fotoInput.click());

    if (fotoInput) {
        fotoInput.addEventListener('change', function () {
            const file = this.files[0];
            if (!file) return;

            // Bloqueia arquivos maiores que 5 MB
            if (file.size > 5 * 1024 * 1024) {
                mostrarToast('Foto maior que 5 MB. Escolha outra.', 'erro');
                return;
            }

            // Mostra o preview da imagem selecionada
            const reader = new FileReader();
            reader.onload = e => {
                fotoPreview.src = e.target.result;
                fotoPreview.style.display = 'block';
                fotoIcone.style.display = 'none';
            };
            reader.readAsDataURL(file);
        });
    }


    // -------------------------------------------------------------------
    // MÁSCARAS DE CAMPOS
    // -------------------------------------------------------------------

    // CPF: formata como 000.000.000-00
    if (cpfInput) {
        cpfInput.addEventListener('input', function () {
            let v = this.value.replace(/\D/g, '').slice(0, 11);
            v = v.replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
            this.value = v;
        });
        cpfInput.addEventListener('blur', function () {
            const cpf = this.value.replace(/\D/g, '');
            marcarErro('formCadastro:cpfMedico', 'cpf', cpf.length !== 11, 'CPF incompleto');
        });
    }

    // CEP: formata como 00000-000
    if (cepInput) {
        cepInput.addEventListener('input', function () {
            let v = this.value.replace(/\D/g, '').slice(0, 8);
            if (v.length > 5) v = v.slice(0, 5) + '-' + v.slice(5);
            this.value = v;
        });
        cepInput.addEventListener('blur', function () {
            const cep = this.value.replace(/\D/g, '');
            marcarErro('formCadastro:cepMedico', 'cep', cep.length !== 8, 'CEP inválido');
        });
    }

    // Telefone: formata como (00) 00000-0000 ou (00) 0000-0000
    if (telefoneInput) {
        telefoneInput.addEventListener('input', function () {
            let v = this.value.replace(/\D/g, '').slice(0, 11);
            if (v.length > 10) {
                v = v.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
            } else {
                v = v.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
            }
            this.value = v;
        });
        telefoneInput.addEventListener('blur', function () {
            const tel = this.value.replace(/\D/g, '');
            marcarErro('formCadastro:telefoneMedico', 'telefone', tel.length < 10, 'Telefone inválido');
        });
    }

    // Nome: só verifica se está preenchido
    const nomeInput = document.getElementById('formCadastro:nomeMedico');
    if (nomeInput) {
        nomeInput.addEventListener('blur', function () {
            const ok = this.value.trim().length > 0;
            marcarErro('formCadastro:nomeMedico', 'nome', !ok, 'Nome é obrigatório');
        });
    }

    // CRM: aceita só números, máximo 6 dígitos
    const crmInput = document.getElementById('formCadastro:crmMedico');
    if (crmInput) {
        crmInput.setAttribute('maxlength', '6');
        crmInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 6);
        });
        crmInput.addEventListener('blur', function () {
            const ok = /^\d{1,6}$/.test(this.value.trim());
            marcarErro('formCadastro:crmMedico', 'crm', !ok, 'CRM deve ter até 6 dígitos numéricos');
        });
    }

    // UF: valida se foi selecionada
    const ufEl = document.getElementById('formCadastro:ufCrm');
    if (ufEl) {
        ufEl.addEventListener('change', function () {
            marcarErro('formCadastro:ufCrm', 'uf', !this.value, 'Selecione a UF do CRM');
        });
    }

    // Valor da consulta: formata como moeda e limita a R$ 1.000,00
    const valorInput = document.getElementById('formCadastro:valorConsulta');
    if (valorInput) {
        valorInput.addEventListener('input', function () {
            let digits = this.value.replace(/\D/g, '');
            if (!digits) { this.value = ''; return; }

            // Teto de R$ 1.000,00 = 100000 centavos
            if (parseInt(digits) > 100000) digits = '100000';

            // Converte centavos para reais e formata com vírgula e pontos
            let num = (parseInt(digits) / 100).toFixed(2);
            let [intPart, decPart] = num.split('.');
            intPart = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
            this.value = intPart + ',' + decPart;
        });

        valorInput.addEventListener('blur', function () {
            const digits = this.value.replace(/\D/g, '');
            const valor = parseInt(digits || '0') / 100;
            marcarErro('formCadastro:valorConsulta', 'valor', valor <= 0, 'Informe um valor maior que zero');
        });
    }

    // Duração: só inteiros entre 5 e 999 minutos
    const duracaoInput = document.getElementById('formCadastro:tempoConsulta');
    if (duracaoInput) {
        duracaoInput.setAttribute('maxlength', '3');
        duracaoInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 3);
        });
        duracaoInput.addEventListener('blur', function () {
            const t = parseInt(this.value);
            marcarErro('formCadastro:tempoConsulta', 'tempo',
                isNaN(t) || t < 5 || t > 999, 'Duração deve ser entre 5 e 999 minutos');
        });
    }

    // Data de nascimento: máscara DD/MM/AAAA e validação de idade mínima de 24 anos
    const dataNascInput = document.getElementById('formCadastro:dataNascimento');
    if (dataNascInput) {

        // Aplica a máscara conforme o usuário digita
        dataNascInput.addEventListener('input', function () {
            let v = this.value.replace(/\D/g, '').slice(0, 8);
            if (v.length > 4) {
                v = v.slice(0, 2) + '/' + v.slice(2, 4) + '/' + v.slice(4);
            } else if (v.length > 2) {
                v = v.slice(0, 2) + '/' + v.slice(2);
            }
            this.value = v;
        });

        // Valida a idade ao sair do campo
        dataNascInput.addEventListener('blur', function () {
            const partes = this.value.split('/');
            let erro = true;
            if (partes.length === 3 && partes[2].length === 4) {
                const nascDate = new Date(+partes[2], +partes[1] - 1, +partes[0]);
                const hoje = new Date();
                let idade = hoje.getFullYear() - nascDate.getFullYear();
                const m = hoje.getMonth() - nascDate.getMonth();
                if (m < 0 || (m === 0 && hoje.getDate() < nascDate.getDate())) idade--;
                erro = isNaN(nascDate.getTime()) || idade < 24;
            }
            marcarErro('formCadastro:dataNascimento', 'nasc', erro, 'O médico deve ter no mínimo 24 anos');
        });
    }

    // Email: valida formato básico
    const emailInput = document.getElementById('formCadastro:emailMedico');
    if (emailInput) {
        emailInput.addEventListener('blur', function () {
            const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.value.trim());
            marcarErro('formCadastro:emailMedico', 'email', !ok, 'E-mail inválido');
        });
    }

    // Senha: mínimo de 6 caracteres
    const senhaInput = document.getElementById('formCadastro:senhaMedico');
    if (senhaInput) {
        senhaInput.addEventListener('blur', function () {
            marcarErro('formCadastro:senhaMedico', 'senha', this.value.length < 6, 'Mínimo 6 caracteres');
        });
    }


    // -------------------------------------------------------------------
    // INPUTS DE HORA (disponibilidade semanal)
    // -------------------------------------------------------------------

    // Começa todos os campos de hora desabilitados, só ativa quando o dia é marcado
    document.querySelectorAll('.input-hora-texto').forEach(input => {
        input.disabled = true;

		input.addEventListener('keydown', function (e) {
		    if (e.key === ':') {
		        e.preventDefault();
		        const v = this.value.replace(/\D/g, '');
		        if (v.length >= 2) {
		            this.value = v.slice(0, 2) + ':' + v.slice(2, 4);
		            this.setSelectionRange(3, 3);
		        }
		    }
		});

		input.addEventListener('input', function () {
		    const cursor = this.selectionStart;
		    let v = this.value.replace(/\D/g, '').slice(0, 4);

		    // Limita o primeiro dígito da hora a 0 ou 1 ou 2
		    if (v.length >= 1 && parseInt(v[0]) > 2) v = '0' + v.slice(0, 3);

		    // Limita a hora máxima a 23
		    if (v.length >= 2 && parseInt(v.slice(0, 2)) > 23) v = '23' + v.slice(2);

		    // Limita o primeiro dígito dos minutos a 0, 3 ou 4 (já que só aceita :00, :30, :45)
		    if (v.length >= 3 && ![0, 3, 4].includes(parseInt(v[2]))) v = v.slice(0, 2) + '0';

		    // Limita os minutos completos a 00, 30 ou 45
		    if (v.length === 4) {
		        const mm = parseInt(v.slice(2, 4));
		        if (mm > 45) v = v.slice(0, 2) + '45';
		        else if (mm > 30 && mm < 45) v = v.slice(0, 2) + '30';
		        else if (mm > 0 && mm < 30) v = v.slice(0, 2) + '00';
		    }

		    if (v.length > 2) v = v.slice(0, 2) + ':' + v.slice(2);
		    this.value = v;
		    const novoCursor = cursor >= 3 ? cursor + 1 : cursor;
		    this.setSelectionRange(novoCursor, novoCursor);
		    this.classList.remove('input-invalido');
		});

        // Valida o horário ao sair do campo
        input.addEventListener('blur', function () {
            const dia     = this.dataset.dia;
            const tipo    = this.dataset.tipo;
            const row     = document.getElementById('row-' + dia);
            const errSpan = document.getElementById('err-hora-' + dia);
            const errMsg  = errSpan ? errSpan.querySelector('.err-hora-msg') : null;

            // Só valida se o dia está com o checkbox marcado
            const checkbox = row ? row.querySelector('input[type=checkbox]') : null;
            if (!checkbox || !checkbox.checked) return;

            const val = this.value.trim();

            // Formato obrigatório: HH:MM
            if (!/^\d{2}:\d{2}$/.test(val)) {
                marcarHoraInvalida(this, errSpan, errMsg, 'Use o formato HH:MM');
                return;
            }

            const [hh, mm] = val.split(':').map(Number);

            // Hora tem que estar entre 00 e 23
            if (hh < 0 || hh > 23) {
                marcarHoraInvalida(this, errSpan, errMsg, 'Hora inválida (00–23)');
                return;
            }

            // Minutos só podem ser :00, :30 ou :45
            if (![0, 30, 45].includes(mm)) {
                marcarHoraInvalida(this, errSpan, errMsg, 'Minutos: apenas :00, :30 ou :45');
                return;
            }

            // Se for o campo de fim, valida que é depois do início
            if (tipo === 'fim') {
                const inicioInput = row.querySelector('[data-tipo="inicio"]');
                if (inicioInput && /^\d{2}:\d{2}$/.test(inicioInput.value)) {
                    const [hi, mi] = inicioInput.value.split(':').map(Number);
                    const totalInicio = hi * 60 + mi;
                    const totalFim    = hh * 60 + mm;
                    if (totalFim <= totalInicio) {
                        marcarHoraInvalida(this, errSpan, errMsg, 'Fim deve ser depois do início');
                        return;
                    }
                }
            }

            // Se chegou aqui, está tudo certo — limpa o erro se nenhum campo do dia estiver inválido
            this.classList.remove('input-invalido');
            const inputs = row ? row.querySelectorAll('.input-hora-texto') : [];
            const algumInvalido = Array.from(inputs).some(i => i.classList.contains('input-invalido'));
            if (!algumInvalido && errSpan) errSpan.style.display = 'none';
        });
    });

    // Marca um campo de hora como inválido e exibe a mensagem de erro
    function marcarHoraInvalida(input, errSpan, errMsg, mensagem) {
        input.classList.add('input-invalido');
        if (errMsg) errMsg.textContent = mensagem;
        if (errSpan) errSpan.style.display = 'flex';
    }


    // -------------------------------------------------------------------
    // CHIPS DE ESPECIALIDADES
    // -------------------------------------------------------------------

    function inicializarChips() {
        const container = document.getElementById('chipsContainer');
        if (!container) return;

        ESPECIALIDADES.forEach(nome => {
            const chip = document.createElement('div');
            chip.className = 'chip';
            chip.textContent = nome;
            chip.dataset.nome = nome;
            chip.addEventListener('click', () => toggleChip(chip, nome));
            container.appendChild(chip);
        });
    }

    function toggleChip(chip, nome) {
        if (chip.classList.contains('bloqueado')) return;

        if (chip.classList.contains('ativo')) {
            chip.classList.remove('ativo');
            especialidadesSelecionadas = especialidadesSelecionadas.filter(e => e !== nome);
        } else {
            // Não deixa selecionar mais de 3
            if (especialidadesSelecionadas.length >= 3) {
                mostrarToast('Máximo de 3 especialidades permitidas.', 'erro');
                return;
            }
            chip.classList.add('ativo');
            especialidadesSelecionadas.push(nome);
        }

        // Atualiza o campo hidden que manda os dados para o bean
        const hiddenInput = document.getElementById('formCadastro:especialidadesHidden');
        if (hiddenInput) hiddenInput.value = especialidadesSelecionadas.join(',');

        atualizarChips();
        if (especialidadesSelecionadas.length > 0) ocultarErro('especialidade');
    }

    function atualizarChips() {
        // Bloqueia os chips não selecionados quando já tem 3 escolhidas
        const max = especialidadesSelecionadas.length >= 3;
        document.querySelectorAll('.chip').forEach(c => {
            if (!c.classList.contains('ativo')) c.classList.toggle('bloqueado', max);
        });

        const contador = document.getElementById('chipsContador');
        if (contador) contador.textContent = `${especialidadesSelecionadas.length}/3 selecionadas`;
    }


    // -------------------------------------------------------------------
    // HELPERS DE ERRO
    // -------------------------------------------------------------------

    function marcarErro(inputId, sufixo, temErro, msg) {
        const el = document.getElementById(inputId);
        if (el) el.classList.toggle('input-invalido', temErro);
        temErro ? exibirErro(sufixo, msg) : ocultarErro(sufixo);
    }

    function exibirErro(sufixo, msg) {
        const span = document.getElementById(`err-${sufixo}`);
        if (!span) return;
        if (msg) span.innerHTML = `<i class="fas fa-exclamation-circle"></i> ${msg}`;
        span.classList.add('visivel');
    }

    function ocultarErro(sufixo) {
        const span = document.getElementById(`err-${sufixo}`);
        if (span) span.classList.remove('visivel');
    }


    // -------------------------------------------------------------------
    // VALIDAÇÃO GERAL (chamada no submit)
    // -------------------------------------------------------------------

    function validarTudo() {
        let valido = true;

        // Nome
        const nome = nomeInput ? nomeInput.value.trim() : '';
        if (!nome) {
            marcarErro('formCadastro:nomeMedico', 'nome', true, 'Nome é obrigatório');
            valido = false;
        } else {
            marcarErro('formCadastro:nomeMedico', 'nome', false);
        }

        // CPF
        const cpf = cpfInput ? cpfInput.value.replace(/\D/g, '') : '';
        if (cpf.length !== 11) {
            marcarErro('formCadastro:cpfMedico', 'cpf', true, 'CPF incompleto');
            valido = false;
        } else {
            marcarErro('formCadastro:cpfMedico', 'cpf', false);
        }

        // Telefone
        const tel = telefoneInput ? telefoneInput.value.replace(/\D/g, '') : '';
        if (tel.length < 10) {
            marcarErro('formCadastro:telefoneMedico', 'telefone', true, 'Telefone inválido');
            valido = false;
        } else {
            marcarErro('formCadastro:telefoneMedico', 'telefone', false);
        }

        // Data de nascimento + idade mínima de 24 anos
        const nascVal = dataNascInput ? dataNascInput.value : '';
        const partes = nascVal.split('/');
        let nascOk = false;
        if (partes.length === 3 && partes[2].length === 4) {
            const nascDate = new Date(+partes[2], +partes[1] - 1, +partes[0]);
            const hoje = new Date();
            const idade = hoje.getFullYear() - nascDate.getFullYear()
                - (hoje < new Date(hoje.getFullYear(), nascDate.getMonth(), nascDate.getDate()) ? 1 : 0);
            nascOk = !isNaN(nascDate.getTime()) && idade >= 24;
        }
        if (!nascOk) {
            marcarErro('formCadastro:dataNascimento', 'nasc', true, 'O médico deve ter no mínimo 24 anos');
            valido = false;
        } else {
            marcarErro('formCadastro:dataNascimento', 'nasc', false);
        }

        // CRM
        const crm = crmInput ? crmInput.value.trim() : '';
        if (!/^\d{1,6}$/.test(crm)) {
            marcarErro('formCadastro:crmMedico', 'crm', true, 'CRM deve ter até 6 dígitos numéricos');
            valido = false;
        } else {
            marcarErro('formCadastro:crmMedico', 'crm', false);
        }

        // UF
        const uf = ufEl ? ufEl.value : '';
        if (!uf) {
            marcarErro('formCadastro:ufCrm', 'uf', true, 'Selecione a UF do CRM');
            valido = false;
        } else {
            marcarErro('formCadastro:ufCrm', 'uf', false);
        }

        // CEP
        const cep = cepInput ? cepInput.value.replace(/\D/g, '') : '';
        if (cep.length !== 8) {
            marcarErro('formCadastro:cepMedico', 'cep', true, 'CEP inválido');
            valido = false;
        } else {
            marcarErro('formCadastro:cepMedico', 'cep', false);
        }

        // Valor da consulta
        const digits = valorInput ? valorInput.value.replace(/\D/g, '') : '';
        const valor = parseInt(digits || '0') / 100;
        if (isNaN(valor) || valor <= 0) {
            marcarErro('formCadastro:valorConsulta', 'valor', true, 'Informe um valor maior que zero');
            valido = false;
        } else {
            marcarErro('formCadastro:valorConsulta', 'valor', false);
        }

        // Duração da consulta
        const tempo = duracaoInput ? parseInt(duracaoInput.value) : 0;
        if (isNaN(tempo) || tempo < 5 || tempo > 999) {
            marcarErro('formCadastro:tempoConsulta', 'tempo', true, 'Duração deve ser entre 5 e 999 minutos');
            valido = false;
        } else {
            marcarErro('formCadastro:tempoConsulta', 'tempo', false);
        }

        // Especialidades
        if (especialidadesSelecionadas.length === 0) {
            exibirErro('especialidade', 'Selecione ao menos uma especialidade');
            valido = false;
        } else {
            ocultarErro('especialidade');
        }

        // Disponibilidade: verifica dias marcados e valida os horários de cada um
        let dispValida = true;

        const linhasAtivas = Array.from(document.querySelectorAll('.disp-row')).filter(row => {
            const cb = row.querySelector('input[type=checkbox]');
            return cb && cb.checked;
        });

        if (linhasAtivas.length === 0) {
            exibirErro('disp', 'Selecione ao menos um dia de disponibilidade');
            dispValida = false;
        } else {
            ocultarErro('disp');

            linhasAtivas.forEach(row => {
                const dia     = row.id.replace('row-', '');
                const errSpan = document.getElementById('err-hora-' + dia);
                const errMsg  = errSpan ? errSpan.querySelector('.err-hora-msg') : null;
                const inputs  = row.querySelectorAll('.input-hora-texto');
                const [inInicio, inFim] = inputs;

                // Valida um campo de hora individualmente
                const validarCampo = (input, label) => {
                    const val = input.value.trim();

                    if (!val) {
                        marcarHoraInvalida(input, errSpan, errMsg, `${label} é obrigatório`);
                        return false;
                    }
                    if (!/^\d{2}:\d{2}$/.test(val)) {
                        marcarHoraInvalida(input, errSpan, errMsg, 'Use o formato HH:MM');
                        return false;
                    }

                    const [hh, mm] = val.split(':').map(Number);

                    if (hh > 23) {
                        marcarHoraInvalida(input, errSpan, errMsg, 'Hora inválida (00–23)');
                        return false;
                    }
                    if (![0, 30, 45].includes(mm)) {
                        marcarHoraInvalida(input, errSpan, errMsg, 'Minutos: apenas :00, :30 ou :45');
                        return false;
                    }

                    return true;
                };

                const okInicio = validarCampo(inInicio, 'Hora início');
                const okFim    = validarCampo(inFim, 'Hora fim');

                // Se os dois campos são válidos, ainda verifica se fim é depois do início
                if (okInicio && okFim) {
                    const [hi, mi] = inInicio.value.split(':').map(Number);
                    const [hf, mf] = inFim.value.split(':').map(Number);
                    if ((hf * 60 + mf) <= (hi * 60 + mi)) {
                        marcarHoraInvalida(inFim, errSpan, errMsg, 'Fim deve ser depois do início');
                        dispValida = false;
                    }
                } else {
                    dispValida = false;
                }
            });
        }

        if (!dispValida) valido = false;

        // Email
        const email = emailInput ? emailInput.value.trim() : '';
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            marcarErro('formCadastro:emailMedico', 'email', true, 'E-mail inválido');
            valido = false;
        } else {
            marcarErro('formCadastro:emailMedico', 'email', false);
        }

        // Senha
        const senha = senhaInput ? senhaInput.value : '';
        if (senha.length < 6) {
            marcarErro('formCadastro:senhaMedico', 'senha', true, 'Mínimo 6 caracteres');
            valido = false;
        } else {
            marcarErro('formCadastro:senhaMedico', 'senha', false);
        }

        return valido;
    }


    // -------------------------------------------------------------------
    // SUBMIT
    // -------------------------------------------------------------------

    if (form) {
        form.addEventListener('submit', function (e) {

            // Converte o valor de 1.000,00 para 1000.00 antes de mandar pro servidor
            if (valorInput) {
                valorInput.value = valorInput.value
                    .replace(/\./g, '')
                    .replace(',', '.');
            }

            if (!validarTudo()) {
                e.preventDefault();
                mostrarToast('Por favor, corrija os erros no formulário.', 'erro');

                // Rola suavemente até o primeiro campo com erro
                const primeiro = document.querySelector('.input-invalido');
                if (primeiro) primeiro.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }


    // -------------------------------------------------------------------
    // TOAST
    // -------------------------------------------------------------------

    function mostrarToast(mensagem, tipo = 'sucesso') {
        const toast = document.getElementById('toast');
        if (!toast) return;
        toast.textContent = mensagem;
        toast.className = `toast ${tipo} visivel`;
        setTimeout(() => toast.classList.remove('visivel'), 3000);
    }


	
	document.querySelectorAll('.disp-row').forEach(row => {
	    const cb = row.querySelector('input[type=checkbox]');
	    if (cb && cb.checked) {
	        row.classList.add('ativa');
	        row.querySelectorAll('.input-hora-texto').forEach(i => { i.disabled = false; });
	    }
	});

    inicializarChips();

});