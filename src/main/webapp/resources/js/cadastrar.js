//ativa ou desativa a linha de horario quando o checkbox do dia e marcado/desmarcado
function toggleDisp(checkbox, valorEnum) {
    var row = document.getElementById('row-' + valorEnum);
    var inputs = row.querySelectorAll('.input-hora-texto');

    if (checkbox.checked) {
        row.classList.add('ativa');
        for (var i = 0; i < inputs.length; i++) {
            inputs[i].disabled = false;
        }
    } else {
        row.classList.remove('ativa');
        for (var j = 0; j < inputs.length; j++) {
            var input = inputs[j];
            input.disabled = true;
            input.value = '';
            input.classList.remove('input-invalido');
        }
        //limpa o erro da linha ao desmarcar o dia
        var errSpan = document.getElementById('err-hora-' + valorEnum);
        if (errSpan) errSpan.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', function() {

    var form = document.getElementById('formCadastro');
    var cpfInput = document.getElementById('formCadastro:cpfMedico');
    var fotoInput = document.getElementById('formCadastro:fotoMedico');
    var telefoneInput = document.getElementById('formCadastro:telefoneMedico');
    var fotoCirculo = document.getElementById('fotoCirculo');
    var fotoPreview = document.getElementById('fotoPreview');
    var fotoIcone = document.getElementById('fotoIcone');

    var ESPECIALIDADES = [
        'Cardiologia', 'Dermatologia', 'Pediatria', 'Ortopedia',
        'Ginecologia', 'Oftalmologia', 'Neurologia', 'Psiquiatria',
        'Endocrinologia', 'Urologia', 'Otorrinolaringologia',
        'Gastroenterologia', 'Clínico Geral', 'Oncologia',
        'Reumatologia', 'Infectologia'
    ];

    var especialidadesSelecionadas = [];

	
	//foto
    if (fotoCirculo) {
        fotoCirculo.addEventListener('click', function() {
            fotoInput.click();
        });
    }

    if (fotoInput) {
        fotoInput.addEventListener('change', function () {
            var file = this.files[0];
            if (!file) return;

            //bloqueia arquivos maiores que 5 MB
            if (file.size > 5 * 1024 * 1024) {
                mostrarToast('Foto maior que 5 MB. Escolha outra.', 'erro');
                return;
            }

            //mostra o preview da imagem selecionada
            var reader = new FileReader();
            reader.onload = function(e) {
                fotoPreview.src = e.target.result;
                fotoPreview.style.display = 'block';
                fotoIcone.style.display = 'none';
            };
            reader.readAsDataURL(file);
        });
    }

	//mascara de campos
    //cpf
    if (cpfInput) {
        cpfInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').slice(0, 11);
            v = v.replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
            this.value = v;
        });
        cpfInput.addEventListener('blur', function () {
            var cpf = this.value.replace(/\D/g, '');
            marcarErro('formCadastro:cpfMedico', 'cpf', cpf.length !== 11, 'CPF incompleto');
        });
    }

    //telefone
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
        telefoneInput.addEventListener('blur', function () {
            var tel = this.value.replace(/\D/g, '');
            marcarErro('formCadastro:telefoneMedico', 'telefone', tel.length < 10, 'Telefone invalido');
        });
    }

    //nome: so verifica se esta preenchido
    var nomeInput = document.getElementById('formCadastro:nomeMedico');
    if (nomeInput) {
        nomeInput.addEventListener('blur', function () {
            var ok = this.value.trim().length > 0;
            marcarErro('formCadastro:nomeMedico', 'nome', !ok, 'Nome e obrigatorio');
        });
    }

    //CRM: aceita so numeros, maximo 6 digitos
    var crmInput = document.getElementById('formCadastro:crmMedico');
    if (crmInput) {
        crmInput.setAttribute('maxlength', '6');
        crmInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 6);
        });
        crmInput.addEventListener('blur', function () {
            var ok = /^\d{1,6}$/.test(this.value.trim());
            marcarErro('formCadastro:crmMedico', 'crm', !ok, 'CRM deve ter ate 6 digitos numericos');
        });
    }

    // UF: valida se foi selecionada
    var ufEl = document.getElementById('formCadastro:ufCrm');
    if (ufEl) {
        ufEl.addEventListener('change', function () {
            marcarErro('formCadastro:ufCrm', 'uf', !this.value, 'Selecione a UF do CRM');
        });
    }

    //valor da consulta
    var valorInput = document.getElementById('formCadastro:valorConsulta');
    if (valorInput) {
        valorInput.addEventListener('input', function () {
            var digits = this.value.replace(/\D/g, '');
            if (!digits) { this.value = ''; return; }

            //teto de R$ 1.000,00 = 100000 centavos
            if (parseInt(digits, 10) > 100000) digits = '100000';

            //converte centavos para reais e formata com virgula e pontos
            var num = (parseInt(digits, 10) / 100).toFixed(2);
            var partesNum = num.split('.');
            var intPart = partesNum[0];
            var decPart = partesNum[1];
            intPart = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
            this.value = intPart + ',' + decPart;
        });

        valorInput.addEventListener('blur', function () {
            var digits = this.value.replace(/\D/g, '');
            var valor = parseInt(digits || '0', 10) / 100;
            marcarErro('formCadastro:valorConsulta', 'valor', valor <= 0, 'Informe um valor maior que zero');
        });
    }

    //duracao
    var duracaoInput = document.getElementById('formCadastro:tempoConsulta');
    if (duracaoInput) {
        duracaoInput.setAttribute('maxlength', '3');
        duracaoInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 3);
        });
        duracaoInput.addEventListener('blur', function () {
            var t = parseInt(this.value, 10);
            marcarErro('formCadastro:tempoConsulta', 'tempo',
                isNaN(t) || t < 15 || t > 90, 'Duracao deve ser entre 15 e 90 minutos');
        });
    }

    //data de nascimento: mascara DD/MM/AAAA e validacao de idade minima de 24 anos
    var dataNascInput = document.getElementById('formCadastro:dataNascimento');
    if (dataNascInput) {
        //alica a mascara conforme o usuario digita
        dataNascInput.addEventListener('input', function () {
            var v = this.value.replace(/\D/g, '').slice(0, 8);
            if (v.length > 4) {
                v = v.slice(0, 2) + '/' + v.slice(2, 4) + '/' + v.slice(4);
            } else if (v.length > 2) {
                v = v.slice(0, 2) + '/' + v.slice(2);
            }
            this.value = v;
        });

        //valida a idade ao sair do campo
        dataNascInput.addEventListener('blur', function () {
            var partes = this.value.split('/');
            var erro = true;
            if (partes.length === 3 && partes[2].length === 4) {
                var nascDate = new Date(+partes[2], +partes[1] - 1, +partes[0]);
                var hoje = new Date();
                var idade = hoje.getFullYear() - nascDate.getFullYear();
                var m = hoje.getMonth() - nascDate.getMonth();
                if (m < 0 || (m === 0 && hoje.getDate() < nascDate.getDate())) idade--;
                erro = isNaN(nascDate.getTime()) || idade < 24;
            }
            marcarErro('formCadastro:dataNascimento', 'nasc', erro, 'O medico deve ter no minimo 24 anos');
        });
    }

    //email
    var emailInput = document.getElementById('formCadastro:emailMedico');
    if (emailInput) {
        emailInput.addEventListener('blur', function () {
            var ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.value.trim());
            marcarErro('formCadastro:emailMedico', 'email', !ok, 'E-mail invalido');
        });
    }

    //senha
    var senhaInput = document.getElementById('formCadastro:senhaMedico');
    if (senhaInput) {
        senhaInput.addEventListener('blur', function () {
            marcarErro('formCadastro:senhaMedico', 'senha', this.value.length < 6, 'Minimo 6 caracteres');
        });
    }

	  
	//inputs hra
    var todosInputsHora = document.querySelectorAll('.input-hora-texto');
    for (var idxHora = 0; idxHora < todosInputsHora.length; idxHora++) {
        var inputHora = todosInputsHora[idxHora];
        inputHora.disabled = true;

        //digitar :
        inputHora.addEventListener('keydown', function (e) {
            if (e.key === ':') {
                e.preventDefault();
                var v = this.value.replace(/\D/g, '');
                if (v.length >= 2) {
                    this.value = v.slice(0, 2) + ':' + v.slice(2, 4);
                    this.setSelectionRange(3, 3);
                }
            }
        });

        //aplica mascara HH:MM e limita valores invalidos enquanto digita
        inputHora.addEventListener('input', function () {
            var cursor = this.selectionStart;
            var v = this.value.replace(/\D/g, '').slice(0, 4);

            //primeiro digito da hora: so pode ser 0, 1 ou 2
            if (v.length >= 1 && parseInt(v[0], 10) > 2) v = '0' + v.slice(0, 3);

            // hora maxima e 23
            if (v.length >= 2 && parseInt(v.slice(0, 2), 10) > 23) v = '23' + v.slice(2);

            //primeiro digito dos minutos: so 0, 3 ou 4 (para :00, :30, :45)
            if (v.length >= 3) {
                var m1 = parseInt(v[2], 10);
                if (m1 !== 0 && m1 !== 3 && m1 !== 4) {
                    v = v.slice(0, 2) + '0';
                }
            }

            //minutos completos: so 00, 30 ou 45 — arredonda para o mais proximo
            if (v.length === 4) {
                var mm = parseInt(v.slice(2, 4), 10);
                if (mm > 45) v = v.slice(0, 2) + '45';
                else if (mm > 30) v = v.slice(0, 2) + '30';
                else if (mm > 0 && mm < 30) v = v.slice(0, 2) + '00';
            }

            if (v.length > 2) v = v.slice(0, 2) + ':' + v.slice(2);
            this.value = v;

            //reposiciona o cursor depois da formatacao
            var novoCursor = cursor >= 3 ? cursor + 1 : cursor;
            this.setSelectionRange(novoCursor, novoCursor);
            this.classList.remove('input-invalido');
        });

        //valida o horario ao sair do campo
        inputHora.addEventListener('blur', function () {
            var dia = this.getAttribute('data-dia');
            var tipo = this.getAttribute('data-tipo');
            var row = document.getElementById('row-' + dia);
            var errSpan = document.getElementById('err-hora-' + dia);
            var errMsg = errSpan ? errSpan.querySelector('.err-hora-msg') : null;

            //so valida se o dia esta com o checkbox marcado
            var checkbox = row ? row.querySelector('input[type=checkbox]') : null;
            if (!checkbox || !checkbox.checked) return;

            var val = this.value.trim();

            //formato obrigatorio: HH:MM
            if (!/^\d{2}:\d{2}$/.test(val)) {
                marcarHoraInvalida(this, errSpan, errMsg, 'Use o formato HH:MM');
                return;
            }

            var partesHM = val.split(':');
            var hh = parseInt(partesHM[0], 10);
            var mm = parseInt(partesHM[1], 10);

            //hora tem que estar entre 00 e 23
            if (hh < 0 || hh > 23) {
                marcarHoraInvalida(this, errSpan, errMsg, 'Hora invalida (00–23)');
                return;
            }

            //minutos so podem ser :00, :30 ou :45
            if (mm !== 0 && mm !== 30 && mm !== 45) {
                marcarHoraInvalida(this, errSpan, errMsg, 'Minutos: apenas :00, :30 ou :45');
                return;
            }

            //se for o campo de fim, valida que e depois do inicio
            if (tipo === 'fim') {
                var inicioInput = row.querySelector('[data-tipo="inicio"]');
                if (inicioInput && /^\d{2}:\d{2}$/.test(inicioInput.value)) {
                    var partesInicio = inicioInput.value.split(':');
                    var hi = parseInt(partesInicio[0], 10);
                    var mi = parseInt(partesInicio[1], 10);
                    var totalInicio = hi * 60 + mi;
                    var totalFim = hh * 60 + mm;
                    if (totalFim <= totalInicio) {
                        marcarHoraInvalida(this, errSpan, errMsg, 'Fim deve ser depois do inicio');
                        return;
                    }
                }
            }

            //tudo certo, limpa o erro se nenhum campo do dia estiver invalido
            this.classList.remove('input-invalido');
            var todosInputsRow = row ? row.querySelectorAll('.input-hora-texto') : [];
            var algumInvalido = false;
            for (var r = 0; r < todosInputsRow.length; r++) {
                if (todosInputsRow[r].classList.contains('input-invalido')) {
                    algumInvalido = true;
                    break;
                }
            }
            if (!algumInvalido && errSpan) errSpan.style.display = 'none';
        });
    }

    //marca um campo de hora como invalido e exibe a mensagem de erro inline
    function marcarHoraInvalida(input, errSpan, errMsg, mensagem) {
        input.classList.add('input-invalido');
        if (errMsg) errMsg.textContent = mensagem;
        if (errSpan) errSpan.style.display = 'flex';
    }

    // ativa as linhas que ja vierem marcadas ao carregar a pagina
    var dispRows = document.querySelectorAll('.disp-row');
    for (var d = 0; d < dispRows.length; d++) {
        var r = dispRows[d];
        var cb = r.querySelector('input[type=checkbox]');
        if (cb && cb.checked) {
            r.classList.add('ativa');
            var inps = r.querySelectorAll('.input-hora-texto');
            for (var z = 0; z < inps.length; z++) {
                inps[z].disabled = false;
            }
        }
    }

	
	//chips especialidade
    function inicializarChips() {
        var container = document.getElementById('chipsContainer');
        if (!container) return;

        for (var c = 0; c < ESPECIALIDADES.length; c++) {
            //isolando o escopo com uma IIFE para garantir o click com ES5
            (function(nomeEspec) {
                var chip = document.createElement('div');
                chip.className = 'chip';
                chip.textContent = nomeEspec;
                chip.setAttribute('data-nome', nomeEspec);
                chip.addEventListener('click', function() {
                    toggleChip(chip, nomeEspec);
                });
                container.appendChild(chip);
            })(ESPECIALIDADES[c]);
        }
    }

    function toggleChip(chip, nome) {
        if (chip.classList.contains('bloqueado')) return;

        if (chip.classList.contains('ativo')) {
            chip.classList.remove('ativo');
            var novoArray = [];
            for (var i = 0; i < especialidadesSelecionadas.length; i++) {
                if (especialidadesSelecionadas[i] !== nome) {
                    novoArray.push(especialidadesSelecionadas[i]);
                }
            }
            especialidadesSelecionadas = novoArray;
        } else {
            //nao deixa selecionar mais de 3
            if (especialidadesSelecionadas.length >= 3) {
                mostrarToast('Maximo de 3 especialidades permitidas.', 'erro');
                return;
            }
            chip.classList.add('ativo');
            especialidadesSelecionadas.push(nome);
        }

        //atualiza o campo hidden que manda os dados para o bean
        var hiddenInput = document.getElementById('formCadastro:especialidadesHidden');
        if (hiddenInput) hiddenInput.value = especialidadesSelecionadas.join(',');

        atualizarChips();
        if (especialidadesSelecionadas.length > 0) ocultarErro('especialidade');
    }

    function atualizarChips() {
        //bloqueia os chips nao selecionados quando ja tem 3 escolhidas
        var max = especialidadesSelecionadas.length >= 3;
        var chipsNodes = document.querySelectorAll('.chip');
        for (var i = 0; i < chipsNodes.length; i++) {
            var c = chipsNodes[i];
            if (!c.classList.contains('ativo')) {
                if (max) {
                    c.classList.add('bloqueado');
                } else {
                    c.classList.remove('bloqueado');
                }
            }
        }

        var contador = document.getElementById('chipsContador');
        if (contador) contador.textContent = especialidadesSelecionadas.length + '/3 selecionadas';
    }
	
	//erro
    function marcarErro(inputId, sufixo, temErro, msg) {
        var el = document.getElementById(inputId);
        if (el) {
            if (temErro) {
                el.classList.add('input-invalido');
            } else {
                el.classList.remove('input-invalido');
            }
        }
        if (temErro) {
            exibirErro(sufixo, msg);
        } else {
            ocultarErro(sufixo);
        }
    }

    function exibirErro(sufixo, msg) {
        var span = document.getElementById('err-' + sufixo);
        if (!span) return;
        if (msg) span.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + msg;
        span.classList.add('visivel');
    }

    function ocultarErro(sufixo) {
        var span = document.getElementById('err-' + sufixo);
        if (span) span.classList.remove('visivel');
    }

	//funcao geral
    function validarTudo() {
        var valido = true;

        //nome
        var nome = nomeInput ? nomeInput.value.trim() : '';
        if (!nome) {
            marcarErro('formCadastro:nomeMedico', 'nome', true, 'Nome e obrigatorio');
            valido = false;
        } else {
            marcarErro('formCadastro:nomeMedico', 'nome', false);
        }

        // CPF
        var cpf = cpfInput ? cpfInput.value.replace(/\D/g, '') : '';
        if (cpf.length !== 11) {
            marcarErro('formCadastro:cpfMedico', 'cpf', true, 'CPF incompleto');
            valido = false;
        } else {
            marcarErro('formCadastro:cpfMedico', 'cpf', false);
        }

        //telefone
        var tel = telefoneInput ? telefoneInput.value.replace(/\D/g, '') : '';
        if (tel.length < 10) {
            marcarErro('formCadastro:telefoneMedico', 'telefone', true, 'Telefone invalido');
            valido = false;
        } else {
            marcarErro('formCadastro:telefoneMedico', 'telefone', false);
        }

        //data nasc
        var nascVal = dataNascInput ? dataNascInput.value : '';
        var partes = nascVal.split('/');
        var nascOk = false;
        if (partes.length === 3 && partes[2].length === 4) {
            var nascDate = new Date(+partes[2], +partes[1] - 1, +partes[0]);
            var hoje = new Date();
            var idade = hoje.getFullYear() - nascDate.getFullYear();
            var dataComparacao = new Date(hoje.getFullYear(), nascDate.getMonth(), nascDate.getDate());
            if (hoje < dataComparacao) {
                idade--;
            }
            nascOk = !isNaN(nascDate.getTime()) && idade >= 24;
        }
        if (!nascOk) {
            marcarErro('formCadastro:dataNascimento', 'nasc', true, 'O medico deve ter no minimo 24 anos');
            valido = false;
        } else {
            marcarErro('formCadastro:dataNascimento', 'nasc', false);
        }

        // CRM
        var crm = crmInput ? crmInput.value.trim() : '';
        if (!/^\d{1,6}$/.test(crm)) {
            marcarErro('formCadastro:crmMedico', 'crm', true, 'CRM deve ter ate 6 digitos numericos');
            valido = false;
        } else {
            marcarErro('formCadastro:crmMedico', 'crm', false);
        }

        // UF
        var uf = ufEl ? ufEl.value : '';
        if (!uf) {
            marcarErro('formCadastro:ufCrm', 'uf', true, 'Selecione a UF do CRM');
            valido = false;
        } else {
            marcarErro('formCadastro:ufCrm', 'uf', false);
        }

        // vlor da consulta
		var valorCampo = valorInput ? valorInput.value : '';
		var valorLimpo = valorCampo.replace(/\./g, '').replace(',', '.');
		var valorCons = parseFloat(valorLimpo);

		if (isNaN(valorCons) || valorCons <= 0) {
		    marcarErro('formCadastro:valorConsulta', 'valor', true, 'Informe um valor maior que zero');
		    valido = false;
		} else if (valorCons > 1000) { 
		    marcarErro('formCadastro:valorConsulta', 'valor', true, 'Valor excedido');
		    valido = false;
		} else {
		    marcarErro('formCadastro:valorConsulta', 'valor', false);
		}

        // duracao da consulta
        var tempo = duracaoInput ? parseInt(duracaoInput.value, 10) : 0;
        if (isNaN(tempo) || tempo < 5 || tempo > 999) {
            marcarErro('formCadastro:tempoConsulta', 'tempo', true, 'Duracao deve ser entre 5 e 999 minutos');
            valido = false;
        } else {
            marcarErro('formCadastro:tempoConsulta', 'tempo', false);
        }

        //especialidades
        if (especialidadesSelecionadas.length === 0) {
            exibirErro('especialidade', 'Selecione ao menos uma especialidade');
            valido = false;
        } else {
            ocultarErro('especialidade');
        }

        //disponibilidade: verifica dias marcados e valida os horarios de cada um
        var dispValida = true;

        var todasAsLinhas = document.querySelectorAll('.disp-row');
        var linhasAtivas = [];
        for (var idx = 0; idx < todasAsLinhas.length; idx++) {
            var rowCb = todasAsLinhas[idx].querySelector('input[type=checkbox]');
            if (rowCb && rowCb.checked) {
                linhasAtivas.push(todasAsLinhas[idx]);
            }
        }

        if (linhasAtivas.length === 0) {
            exibirErro('disp', 'Selecione ao menos um dia de disponibilidade');
            dispValida = false;
        } else {
            ocultarErro('disp');

            for (var n = 0; n < linhasAtivas.length; n++) {
                var linhaAtual = linhasAtivas[n];
                var diaID = linhaAtual.id.replace('row-', '');
                var errSpanDisp = document.getElementById('err-hora-' + diaID);
                var errMsgDisp = errSpanDisp ? errSpanDisp.querySelector('.err-hora-msg') : null;
                
                var inpsHora = linhaAtual.querySelectorAll('.input-hora-texto');
                var inInicio = inpsHora[0];
                var inFim = inpsHora[1];

                //valida um campo de hora individualmente
                var validarCampo = function(input, label) {
                    var val = input.value.trim();

                    if (!val) {
                        marcarHoraInvalida(input, errSpanDisp, errMsgDisp, label + ' e obrigatorio');
                        return false;
                    }
                    if (!/^\d{2}:\d{2}$/.test(val)) {
                        marcarHoraInvalida(input, errSpanDisp, errMsgDisp, 'Use o formato HH:MM');
                        return false;
                    }

                    var prts = val.split(':');
                    var hr = parseInt(prts[0], 10);
                    var mn = parseInt(prts[1], 10);

                    if (hr > 23) {
                        marcarHoraInvalida(input, errSpanDisp, errMsgDisp, 'Hora invalida (00–23)');
                        return false;
                    }
                    if (mn !== 0 && mn !== 30 && mn !== 45) {
                        marcarHoraInvalida(input, errSpanDisp, errMsgDisp, 'Minutos: apenas :00, :30 ou :45');
                        return false;
                    }

                    return true;
                };

                var okInicio = validarCampo(inInicio, 'Hora inicio');
                var okFim = validarCampo(inFim, 'Hora fim');

                //se os dois campos estao ok, ainda verifica se fim e depois do inicio
                if (okInicio && okFim) {
                    var ptIni = inInicio.value.split(':');
                    var hi = parseInt(ptIni[0], 10);
                    var mi = parseInt(ptIni[1], 10);
                    
                    var ptFim = inFim.value.split(':');
                    var hf = parseInt(ptFim[0], 10);
                    var mf = parseInt(ptFim[1], 10);
                    
                    if ((hf * 60 + mf) <= (hi * 60 + mi)) {
                        marcarHoraInvalida(inFim, errSpanDisp, errMsgDisp, 'Fim deve ser depois do inicio');
                        dispValida = false;
                    }
                } else {
                    dispValida = false;
                }
            }
        }

        if (!dispValida) valido = false;

        //email
        var email = emailInput ? emailInput.value.trim() : '';
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            marcarErro('formCadastro:emailMedico', 'email', true, 'E-mail invalido');
            valido = false;
        } else {
            marcarErro('formCadastro:emailMedico', 'email', false);
        }

        // Senha
        var senha = senhaInput ? senhaInput.value : '';
        if (senha.length < 6) {
            marcarErro('formCadastro:senhaMedico', 'senha', true, 'Minimo 6 caracteres');
            valido = false;
        } else {
            marcarErro('formCadastro:senhaMedico', 'senha', false);
        }

        return valido;
    }
	
	//submit
    if (form) {
        form.addEventListener('submit', function (e) {
            // Converte o valor de 1.000,00 para 1000.00 antes de mandar pro servidor
            if (valorInput) {
                valorInput.value = valorInput.value.replace(/\./g, '').replace(',', '.');
            }

            if (!validarTudo()) {
                e.preventDefault();
                mostrarToast('Por favor, corrija os erros no formulario.', 'erro');

                // Rola suavemente ate o primeiro campo com erro
                var primeiro = document.querySelector('.input-invalido');
                if (primeiro) primeiro.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }


    function mostrarToast(mensagem, tipo) {
        var tipoToast = tipo ? tipo : 'sucesso';
        var toast = document.getElementById('toast');
        if (!toast) return;
        
        toast.textContent = mensagem;
        toast.className = 'toast ' + tipoToast + ' visivel';
        
        setTimeout(function() {
            toast.classList.remove('visivel');
        }, 3000);
    }


    inicializarChips();

});