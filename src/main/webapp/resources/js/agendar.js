(function () {
    "use strict";
	
    var state = {
        step: 1,
        pacienteNome: null,
        especialidade: null,
        medicoId: null,
        medicoNome: null,
        medicoValor: null,
        dataSelecionada: null,
        horaSelecionada: null,
        tipoConsulta: null,
        diasDisponiveis: [],
        calAno: null,
        calMes: null
    };

    var hoje = new Date();
    state.calAno = hoje.getFullYear();
    state.calMes = hoje.getMonth();

    var DIAS_SEMANA_MAP = {
        0: 'DOMINGO',
        1: 'SEGUNDA',
        2: 'TERCA',
        3: 'QUARTA',
        4: 'QUINTA',
        5: 'SEXTA',
        6: 'SABADO'
    };

    var MESES_PT = ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
                    'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'];

    function init() {
        aplicarMascaras();
        aplicarCapitalizacao();
        monitorarNascimento();
        monitorarNascimentoResponsavel(); // ← NOVO: hint de idade para o responsável
        renderizarCalendario();
		carregarEspecialidades();
    }

    /* ═══════════════════════════════════════════════
       MÁSCARAS
    ═══════════════════════════════════════════════ */
    function aplicarMascaras() {
        document.querySelectorAll('.mask-cpf').forEach(function(el) {
            el.addEventListener('input', function(e) {
                var v = e.target.value.replace(/\D/g,'').slice(0,11);
                v = v.replace(/(\d{3})(\d)/,'$1.$2')
                     .replace(/(\d{3})(\d)/,'$1.$2')
                     .replace(/(\d{3})(\d{1,2})$/,'$1-$2');
                e.target.value = v;
            });
        });

        document.querySelectorAll('.mask-phone').forEach(function(el) {
            el.addEventListener('input', function(e) {
                var v = e.target.value.replace(/\D/g,'').slice(0,11);
                v = v.replace(/(\d{2})(\d)/,'($1) $2')
                     .replace(/(\d{5})(\d)/,'$1-$2');
                e.target.value = v;
            });
        });

        document.querySelectorAll('.mask-nascimento').forEach(function(el) {
            el.addEventListener('input', function(e) {
                var v = e.target.value.replace(/\D/g,'').slice(0,8);
                v = v.replace(/(\d{2})(\d)/,'$1/$2')
                     .replace(/(\d{2})(\d)/,'$1/$2');
                e.target.value = v;
            });
        });
    }

    /* ═══════════════════════════════════════════════
       CAPITALIZAÇÃO AUTOMÁTICA
    ═══════════════════════════════════════════════ */
    function capitalizarPalavras(str) {
        return str.replace(/\b\w/g, function(c) { return c.toUpperCase(); });
    }

    function aplicarCapitalizacao() {
        var campoNome = document.getElementById('fAg:pacNome');
        if (campoNome) {
            campoNome.addEventListener('input', function(e) {
                var pos = e.target.selectionStart;
                e.target.value = capitalizarPalavras(e.target.value);
                e.target.setSelectionRange(pos, pos);
            });
        }

        var campoResp = document.getElementById('respNome');
        if (campoResp) {
            campoResp.addEventListener('input', function(e) {
                var pos = e.target.selectionStart;
                e.target.value = capitalizarPalavras(e.target.value);
                e.target.setSelectionRange(pos, pos);
            });
        }
    }

    /* ═══════════════════════════════════════════════
       MONITORAR NASCIMENTO (PACIENTE)
    ═══════════════════════════════════════════════ */
    function monitorarNascimento() {
        var campo = document.getElementById('fAg:pacNascimento');
        if (!campo) return;
        campo.addEventListener('input', function() {
            var v = campo.value;
            var hint  = document.getElementById('menorHint');
            var errEl = document.getElementById('errNasc');

            if (v.length < 10) {
                esconderMenor();
                esconderErro('errNasc', campo);
                return;
            }

            var partes = v.split('/');
            if (partes.length !== 3) {
                esconderMenor();
                return;
            }

            var dia  = parseInt(partes[0], 10);
            var mes  = parseInt(partes[1], 10);
            var ano  = parseInt(partes[2], 10);
            var data = new Date(ano, mes - 1, dia);

            var dataInvalida = isNaN(data.getTime())
                || data.getDate()  !== dia
                || data.getMonth() !== mes - 1
                || data.getFullYear() !== ano;

            if (dataInvalida || data > new Date()) {
                esconderMenor();
                mostrarErro('errNasc', campo);
                if (errEl) errEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> Data inválida';
                return;
            }

            var idade = calcularIdade(data);

            if (idade < 1) {
                esconderMenor();
                mostrarErro('errNasc', campo);
                if (errEl) errEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> Idade inválida (menos de 1 ano)';
                return;
            }

            esconderErro('errNasc', campo);

            if (idade < 18) {
                if (hint) {
                    hint.textContent = '⚠ ' + idade + ' anos • Menor de idade';
                    hint.classList.add('visible');
                }
                var bloco = document.getElementById('blocoResponsavel');
                if (bloco) bloco.classList.add('visible');
            } else {
                esconderMenor();
            }
        });
    }

    /* ═══════════════════════════════════════════════
       MONITORAR NASCIMENTO (RESPONSÁVEL) — NOVO
       Mostra hint de idade igual ao do paciente.
       Se a idade for < 18, exibe aviso de inválido.
    ═══════════════════════════════════════════════ */
    function monitorarNascimentoResponsavel() {
        var campo = document.getElementById('respNascimento');
        if (!campo) return;

        // Cria o elemento de hint dinamicamente (igual ao menorHint do paciente)
        var hint = document.getElementById('respIdadeHint');
        if (!hint) {
            hint = document.createElement('span');
            hint.id = 'respIdadeHint';
            hint.className = 'ag-menor-hint'; // mesma classe do paciente
            campo.parentNode.insertBefore(hint, campo.nextSibling);
        }

        campo.addEventListener('input', function() {
            var v = campo.value;
            var errEl = document.getElementById('errRespNasc');

            // Esconde hint enquanto digita
            hint.classList.remove('visible');

            if (v.length < 10) {
                esconderErro('errRespNasc', campo);
                return;
            }

            var partes = v.split('/');
            if (partes.length !== 3) return;

            var dia  = parseInt(partes[0], 10);
            var mes  = parseInt(partes[1], 10);
            var ano  = parseInt(partes[2], 10);
            var data = new Date(ano, mes - 1, dia);

            var dataInvalida = isNaN(data.getTime())
                || data.getDate()  !== dia
                || data.getMonth() !== mes - 1
                || data.getFullYear() !== ano
                || data > new Date();

            if (dataInvalida) {
                mostrarErro('errRespNasc', campo);
                if (errEl) errEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> Data inválida';
                return;
            }

            var idade = calcularIdade(data);

            // Mostra a idade calculada no hint (igual ao do paciente)
            hint.classList.add('visible');

            if (idade < 18) {
                // Menor de idade: hint vermelho/laranja de aviso
                hint.textContent = '⚠ ' + idade + ' anos • Responsável deve ter +18 anos';
                hint.style.color = '#f87171'; // tom vermelho para diferenciar do aviso do paciente
                mostrarErro('errRespNasc', campo);
                if (errEl) errEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> O responsável deve ser maior de idade (+18)';
            } else {
                // Maior de idade: hint verde tranquilizador
                hint.textContent = '✓ ' + idade + ' anos • Maior de idade';
                hint.style.color = '#34d399';
                esconderErro('errRespNasc', campo);
            }
        });
    }

    function esconderMenor() {
        var hint  = document.getElementById('menorHint');
        var bloco = document.getElementById('blocoResponsavel');
        if (hint)  hint.classList.remove('visible');
        if (bloco) {
            bloco.classList.remove('visible');
            var rNome = document.getElementById('respNome');
            var rCpf  = document.getElementById('respCpf');
            var rPar  = document.getElementById('respParentesco');
            var rNasc = document.getElementById('respNascimento');
            if (rNome) rNome.value = '';
            if (rCpf)  rCpf.value  = '';
            if (rPar)  rPar.value  = '';
            if (rNasc) rNasc.value = '';

            // Limpa também o hint do responsável ao esconder o bloco
            var respHint = document.getElementById('respIdadeHint');
            if (respHint) respHint.classList.remove('visible');
        }
    }

    function calcularIdade(dataNasc) {
        var hoje = new Date();
        var idade = hoje.getFullYear() - dataNasc.getFullYear();
        var m = hoje.getMonth() - dataNasc.getMonth();
        if (m < 0 || (m === 0 && hoje.getDate() < dataNasc.getDate())) idade--;
        return idade;
    }

    window.toggleConvenio = function(val) {
        var campo = document.getElementById('campoCarteirinha');
        if (val === 'convenio') {
            campo.classList.add('visible');
            document.getElementById('numCarteirinha').placeholder = 'Número da carteirinha';
        } else {
            campo.classList.remove('visible');
        }
    };

    window.proximoStep = function() {
        if (!validarStep(state.step)) return;
        if (state.step < 4) {
            state.step++;
            renderizarStep();
        }
    };

    window.voltarStep = function() {
        if (state.step > 1) {
            state.step--;
            renderizarStep();
        }
    };

    function renderizarStep() {
        for (var i = 1; i <= 4; i++) {
            var p = document.getElementById('panel' + i);
            if (p) p.classList.toggle('active', i === state.step);
        }

        for (var i = 1; i <= 4; i++) {
            var circle = document.getElementById('sc' + i);
            var name   = document.getElementById('sn' + i);
            if (!circle || !name) continue;

            circle.classList.remove('done','active');
            name.classList.remove('done','active');

            if (i < state.step) {
                circle.classList.add('done');
                circle.innerHTML = '✓';
                name.classList.add('done');
            } else if (i === state.step) {
                circle.classList.add('active');
                circle.innerHTML = i;
                name.classList.add('active');
            } else {
                circle.innerHTML = i;
            }

            var linha = document.getElementById('sl' + i);
            if (linha) linha.classList.toggle('done', i < state.step);
        }

        var btnV = document.getElementById('btnVoltar');
        var btnP = document.getElementById('btnProximo');
        var btnS = document.getElementById('btnSubmitJSF');

        btnV.style.visibility = state.step === 1 ? 'hidden' : 'visible';

        if (state.step === 4) {
            btnP.style.display  = 'none';
            if (btnS) btnS.style.display = 'inline-flex';
        } else {
            btnP.style.display  = 'inline-flex';
            if (btnS) btnS.style.display = 'none';
        }

        if (state.step === 4 && state.medicoId) {
            carregarDisponibilidade(state.medicoId);
        }
    }

    function validarStep(step) {
        if (step === 1) return validarDados();
        if (step === 2) return validarEspecialidade();
        if (step === 3) return validarMedico();
        if (step === 4) return validarDataHora();
        return true;
    }

    /* ═══════════════════════════════════════════════
       VALIDAÇÕES STEP 1
    ═══════════════════════════════════════════════ */
    function validarDados() {
        var ok = true;

        // Nome paciente
        var nome = document.getElementById('fAg:pacNome');
        if (!nome || nome.value.trim().split(/\s+/).length < 2) {
            mostrarErro('errNome', nome); ok = false;
        } else { esconderErro('errNome', nome); }

        // Nascimento Paciente
        var nasc = document.getElementById('fAg:pacNascimento');
        var idadePaciente = null;
        if (!nasc || nasc.value.length < 10) {
            mostrarErro('errNasc', nasc); ok = false;
        } else {
            var partes = nasc.value.split('/');
            var dataNasc = new Date(partes[2], partes[1] - 1, partes[0]);
            var dataInvalida = isNaN(dataNasc.getTime()) || dataNasc > new Date();
            idadePaciente = calcularIdade(dataNasc);

            if (dataInvalida || idadePaciente < 1) {
                mostrarErro('errNasc', nasc); ok = false;
            } else {
                esconderErro('errNasc', nasc);
            }
        }

        // CPF paciente
        var cpf = document.getElementById('fAg:pacCpf');
        if (!cpf || cpf.value.replace(/\D/g,'').length !== 11) {
            mostrarErro('errCpf', cpf); ok = false;
        } else { esconderErro('errCpf', cpf); }

        // Gênero
        var sexo = document.getElementById('fAg:pacGenero');
        if (!sexo || !sexo.value) {
            mostrarErro('errSexo', sexo); ok = false;
        } else { esconderErro('errSexo', sexo); }

        // Telefone
        var tel = document.getElementById('fAg:pacTelefone');
        if (!tel || tel.value.replace(/\D/g,'').length < 10) {
            mostrarErro('errTel', tel); ok = false;
        } else { esconderErro('errTel', tel); }

        // Termos
        var t1 = document.getElementById('termoVerdade');
        var t2 = document.getElementById('termoLgpd');
        if (!t1 || !t1.checked) { mostrarErro('errTermo1', null); ok = false; }
        else { esconderErro('errTermo1', null); }
        if (!t2 || !t2.checked) { mostrarErro('errTermo2', null); ok = false; }
        else { esconderErro('errTermo2', null); }

        // RESPONSÁVEL LEGAL
        var bloco = document.getElementById('blocoResponsavel');
        if (bloco && bloco.classList.contains('visible') && idadePaciente !== null && idadePaciente < 18) {

            // ── VALIDAÇÃO 1: Nome do Responsável ─────────────────────────────
            // Não pode ser vazio. Não pode ser igual ao nome do paciente.
            var rNome = document.getElementById('respNome');
            var nomePaciente = nome ? nome.value.trim().toLowerCase() : '';
            var nomeResp     = rNome ? rNome.value.trim().toLowerCase() : '';

            if (!rNome || !rNome.value.trim()) {
                mostrarErro('errRespNome', rNome);
                var errNomeEl = document.getElementById('errRespNome');
                if (errNomeEl) errNomeEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> Obrigatório';
                ok = false;
            } else if (nomeResp === nomePaciente) {
                mostrarErro('errRespNome', rNome);
                var errNomeEl = document.getElementById('errRespNome');
                if (errNomeEl) errNomeEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> Deve ser diferente do nome do paciente';
                ok = false;
            } else {
                esconderErro('errRespNome', rNome);
            }

            // ── VALIDAÇÃO 2: CPF do Responsável ──────────────────────────────
            // Deve ter 11 dígitos. Não pode ser igual ao CPF do paciente.
            var rCpf = document.getElementById('respCpf');
            var cpfPacienteStr = cpf ? cpf.value.replace(/\D/g, '') : '';
            var cpfRespStr     = rCpf ? rCpf.value.replace(/\D/g, '') : '';

            if (!rCpf || cpfRespStr.length !== 11) {
                mostrarErro('errRespCpf', rCpf);
                var errCpfEl = document.getElementById('errRespCpf');
                if (errCpfEl) errCpfEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> CPF inválido';
                ok = false;
            } else if (cpfRespStr === cpfPacienteStr) {
                mostrarErro('errRespCpf', rCpf);
                var errCpfEl = document.getElementById('errRespCpf');
                if (errCpfEl) errCpfEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> O CPF não pode ser igual ao do paciente';
                ok = false;
            } else {
                esconderErro('errRespCpf', rCpf);
            }

            // ── VALIDAÇÃO 3: Nascimento do Responsável ────────────────────────
            // Data deve ser válida E responsável deve ter 18 anos ou mais.
            var rNasc = document.getElementById('respNascimento');
            if (!rNasc || rNasc.value.length < 10) {
                mostrarErro('errRespNasc', rNasc);
                var errNascEl = document.getElementById('errRespNasc');
                if (errNascEl) errNascEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> Data inválida';
                ok = false;
            } else {
                var partesR      = rNasc.value.split('/');
                var dataNascResp = new Date(partesR[2], partesR[1] - 1, partesR[0]);
                var dataInvalidaR = isNaN(dataNascResp.getTime()) || dataNascResp > new Date();
                var idadeResp     = calcularIdade(dataNascResp);

                if (dataInvalidaR) {
                    mostrarErro('errRespNasc', rNasc);
                    var errNascEl = document.getElementById('errRespNasc');
                    if (errNascEl) errNascEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> Data inválida';
                    ok = false;
                } else if (idadeResp < 18) {
                    mostrarErro('errRespNasc', rNasc);
                    var errNascEl = document.getElementById('errRespNasc');
                    if (errNascEl) errNascEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> O responsável deve ser maior de idade (+18)';
                    ok = false;
                } else {
                    esconderErro('errRespNasc', rNasc);
                }
            }

            // Parentesco
            var rParent = document.getElementById('respParentesco');
            if (!rParent || !rParent.value) {
                mostrarErro('errRespParent', rParent); ok = false;
            } else { esconderErro('errRespParent', rParent); }
        }

        // Motivo
        var motivo = document.getElementById('fAg:motivoConsulta');
        if (!motivo || motivo.value.trim().length < 5) {
            mostrarErro('errMotivo', motivo); ok = false;
        } else { esconderErro('errMotivo', motivo); }

        if (ok) {
            state.pacienteNome = obterNomePaciente();
            atualizarResumo('paciente', state.pacienteNome);
        }
        return ok;
    }

    function validarEspecialidade() {
        var hidden = document.getElementById('fAg:especialidadeHidden');
        if (!hidden || !hidden.value) {
            mostrarErro('errEsp', null); return false;
        }
        esconderErro('errEsp', null);
        return true;
    }

    function validarMedico() {
        var hidden = document.getElementById('fAg:medicoIdHidden');
        if (!hidden || !hidden.value) {
            mostrarErro('errMedico', null); return false;
        }
        esconderErro('errMedico', null);
        return true;
    }

	function validarDataHora() {
	    // 1. Pega os elementos ocultos do JSF que estão no seu XHTML
	    var dataH = document.getElementById('fAg:dataConsultaHidden');
	    var horaH = document.getElementById('fAg:horaConsultaHidden');
	    var tipoH = document.getElementById('fAg:tipoConsultaHidden');

	    // 2. Se o "state" sumiu, mas o input do JSF tem valor, recupera para o state
	    if (!state.dataSelecionada && dataH && dataH.value) {
	        state.dataSelecionada = dataH.value;
	    }
	    if (!state.horaSelecionada && horaH && horaH.value) {
	        state.horaSelecionada = horaH.value;
	    }

	    // 3. LOG DE DEPURAÇÃO (Abra o Console do navegador [F12] para ver o que está vazio)
	    console.log("=== VERIFICAÇÃO DE DADOS ===");
	    console.log("State Data:", state.dataSelecionada, " | InputHidden Data:", dataH ? dataH.value : "não achou input");
	    console.log("State Hora:", state.horaSelecionada, " | InputHidden Hora:", horaH ? horaH.value : "não achou input");

	    // 4. VALIDAÇÃO REAL: Verifica tanto no state quanto no input oculto
	    var temData = state.dataSelecionada || (dataH && dataH.value);
	    var temHora = state.horaSelecionada || (horaH && horaH.value);

	    if (!temData || !temHora) {
	        mostrarErro('errData', null); 
	        return false; // Trava aqui e mostra o erro na tela
	    }
	    
	    // Se chegou aqui, os dados existem! Esconde o erro da tela
	    esconderErro('errData', null);

	    // 5. Força o preenchimento dos inputs para o envio do formulário/fetch
	    if (dataH) dataH.value = state.dataSelecionada || dataH.value;
	    if (horaH) horaH.value = state.horaSelecionada || horaH.value;
	    
	    // Força um valor padrão para o tipo, já que é enfeite
	    if (tipoH) {
	        tipoH.value = state.tipoConsulta || 'rotina';
	    }
	    
	    return true;
	}

    function mostrarErro(id, campo) {
        var el = document.getElementById(id);
        if (el) el.classList.add('visible');
        if (campo) campo.classList.add('error');
    }

    function esconderErro(id, campo) {
        var el = document.getElementById(id);
        if (el) el.classList.remove('visible');
        if (campo) campo.classList.remove('error');
    }

    function obterNomePaciente() {
        var el = document.getElementById('fAg:pacNome');
        return el ? el.value.trim() : '';
    }

    /* ═══════════════════════════════════════════════
       ESPECIALIDADE & MÉDICO
    ═══════════════════════════════════════════════ */
    window.selecionarEsp = function(card, nome) {
        document.querySelectorAll('.ag-esp-card').forEach(function(c) {
            c.classList.remove('selected');
        });
        card.classList.add('selected');

        var hidden = document.getElementById('fAg:especialidadeHidden');
        if (hidden) hidden.value = nome;
        state.especialidade = nome;

        atualizarResumo('especialidade', nome);
        esconderErro('errEsp', null);

        carregarMedicosPorEsp(nome);
    };

    function carregarMedicosPorEsp(especialidade) {
        var grid = document.getElementById('medicosGrid');
        var sub  = document.getElementById('subMedico');
        if (!grid) return;

        grid.innerHTML = '<div style="text-align:center;padding:32px;color:#a5b4fc">⏳ Carregando médicos...</div>';

        fetch('http://localhost:9090/api/medicos?especialidade=' + encodeURIComponent(especialidade))
            .then(function(r) { return r.json(); })
            .then(function(medicos) {
                renderizarCardsMedicos(medicos);
                if (sub) sub.innerHTML = 'Profissionais em <strong>' + especialidade + '</strong>. Escolha o ideal para você.';
            })
            .catch(function(err) {
                console.error('Erro ao carregar médicos:', err);
                grid.innerHTML = '<div style="text-align:center;padding:32px;color:#f87171">Erro ao carregar médicos. Tente novamente.</div>';
            });
    }

    function renderizarCardsMedicos(medicos) {
        var grid = document.getElementById('medicosGrid');
        if (!grid) return;
        grid.innerHTML = '';

        if (!medicos || medicos.length === 0) {
            grid.innerHTML = '<div style="text-align:center;padding:32px;color:#a5b4fc">Nenhum médico disponível para essa especialidade.</div>';
            return;
        }

        medicos.forEach(function(med) {
            var iniciais = (med.nome || '').split(' ').map(function(p) { return p[0]; }).join('').slice(0,2).toUpperCase();
            var cores = ['#4f46e5','#7c3aed','#2563eb','#0891b2','#059669','#d97706'];
            var cor   = cores[med.id % cores.length] || '#4f46e5';

            var card = document.createElement('div');
            card.className = 'ag-medico-card';
            card.setAttribute('data-nome', med.nome || '');
            card.setAttribute('data-crm',  med.crm  || '');
            card.setAttribute('data-id',   med.id   || '');
            card.setAttribute('data-valor', med.valorConsulta || '');
            card.onclick = function() { window.selecionarMedico(card); };

            var fotoHtml = med.foto
                ? '<img src="data:image/jpeg;base64,' + med.foto + '" style="width:100%;height:100%;object-fit:cover;border-radius:50%" />'
                : iniciais;

            var tagsHtml = (med.especialidades || []).map(function(e) {
                return '<span class="ag-medico-tag">' + e + '</span>';
            }).join('');

            var valor = med.valorConsulta ? parseFloat(med.valorConsulta).toFixed(2).replace('.',',') : '—';

            card.innerHTML =
                '<div class="ag-medico-check"><i class="fas fa-check"></i></div>' +
                '<div class="ag-medico-avatar" style="background:' + cor + '">' + fotoHtml + '</div>' +
                '<div class="ag-medico-nome">' + (med.nome || '') + '</div>' +
                '<div class="ag-medico-crm">CRM ' + (med.crm || '') + '</div>' +
                '<div class="ag-medico-tags">' + tagsHtml + '</div>' +
                '<div class="ag-medico-info">' +
                    '<div class="ag-medico-info-item"><span class="ag-medico-info-label">Duração</span><span class="ag-medico-info-value">' + (med.tempoConsultaMinutos || '—') + ' min</span></div>' +
                    '<div class="ag-medico-info-item"><span class="ag-medico-info-label">Consulta</span><span class="ag-medico-info-value">R$ ' + valor + '</span></div>' +
                    '<div class="ag-medico-info-item"><span class="ag-medico-info-label">Telefone</span><span class="ag-medico-info-value">' + (med.telefone || '—') + '</span></div>' +
                '</div>';

            grid.appendChild(card);
        });
    }

	window.selecionarMedico = function(card) {
	        document.querySelectorAll('.ag-medico-card').forEach(function(c) {
	            c.classList.remove('selected');
	        });
	        card.classList.add('selected');

	        var id    = card.getAttribute('data-id');
	        var nome  = card.getAttribute('data-nome');
	        var valor = card.getAttribute('data-valor');

	        var hidden = document.getElementById('fAg:medicoIdHidden');
	        if (hidden) hidden.value = id;

	        state.medicoId    = id;
	        state.medicoNome  = nome;
	        state.medicoValor = valor;

	        atualizarResumo('medico', nome);
	        atualizarResumo('valor', valor);
	        esconderErro('errMedico', null);

	        var sub = document.getElementById('subDataHora');
	        if (sub) sub.textContent = 'Com ' + nome + '.';
	    };

	    window.filtrarMedicos = function() {
	        var q = (document.getElementById('buscaMedico').value || '').toLowerCase();
	        document.querySelectorAll('.ag-medico-card').forEach(function(c) {
	            var nome = (c.getAttribute('data-nome') || '').toLowerCase();
	            var crm  = (c.getAttribute('data-crm')  || '').toLowerCase();
	            c.style.display = (nome.includes(q) || crm.includes(q)) ? '' : 'none';
	        });
	    };

	    window.selecionarTipo = function(card, tipo) {
	        document.querySelectorAll('.ag-tipo-card').forEach(function(c) {
	            c.classList.remove('selected');
	        });
	        card.classList.add('selected');
	        state.tipoConsulta = tipo;
	        esconderErro('errTipo', null);
	    };

	    /* ═══════════════════════════════════════════════
	       CALENDÁRIO & HORÁRIOS
	    ═══════════════════════════════════════════════ */
	    function carregarDisponibilidade(medicoId) {
	        fetch('http://localhost:9090/api/medicos/' + medicoId + '/horarios-livres')
	            .then(function(r) { return r.json(); })
	            .then(function(data) {
	                state.diasDisponiveis = data.diasDisponiveis || [];
	                renderizarCalendario();
	            })
	            .catch(function(err) {
	                console.error('Erro ao carregar disponibilidade:', err);
	                state.diasDisponiveis = [];
	                renderizarCalendario();
	            });
	    }

	    function buscarHorariosLivres(medicoId, dataISO) {
	        var grid = document.getElementById('horariosGrid');
	        if (grid) grid.innerHTML = '<div class="ag-horarios-empty"><div class="ag-empty-icon">⏳</div><div class="ag-empty-text">Carregando horários...</div></div>';

	        fetch('http://localhost:9090/api/medicos/' + medicoId + '/horarios-livres?data=' + dataISO)
	            .then(function(r) { return r.json(); })
	            .then(function(data) {
	                renderizarHorarios(data.horariosLivres || []);
	            })
	            .catch(function(err) {
	                console.error('Erro ao buscar horários:', err);
	                renderizarHorarios([]);
	            });
	    }

	    function diasDisponiveisMes(ano, mes) {
	        var diasComSlots = [];
	        var totalDias = new Date(ano, mes + 1, 0).getDate();
	        for (var d = 1; d <= totalDias; d++) {
	            var data = new Date(ano, mes, d);
	            var diaSemanaEnum = DIAS_SEMANA_MAP[data.getDay()];
	            if (state.diasDisponiveis.indexOf(diaSemanaEnum) !== -1) {
	                diasComSlots.push(d);
	            }
	        }
	        return diasComSlots;
	    }

	    function pad(n) { return n < 10 ? '0'+n : n; }

	    function renderizarCalendario() {
	        var grid = document.getElementById('calGrid');
	        var titulo = document.getElementById('calMesAno');
	        if (!grid || !titulo) return;

	        titulo.textContent = MESES_PT[state.calMes] + ' De ' + state.calAno;

	        var diasDisponiveis = diasDisponiveisMes(state.calAno, state.calMes);
	        var primeiroDia = new Date(state.calAno, state.calMes, 1).getDay();
	        var totalDias   = new Date(state.calAno, state.calMes + 1, 0).getDate();
	        var hojeData    = new Date();

	        grid.innerHTML = '';

	        ['D','S','T','Q','Q','S','S'].forEach(function(d) {
	            var div = document.createElement('div');
	            div.className = 'ag-cal-dow';
	            div.textContent = d;
	            grid.appendChild(div);
	        });

	        for (var e = 0; e < primeiroDia; e++) {
	            var vazio = document.createElement('div');
	            vazio.className = 'ag-cal-day empty';
	            grid.appendChild(vazio);
	        }

	        for (var d = 1; d <= totalDias; d++) {
	            var dataAtual = new Date(state.calAno, state.calMes, d);
	            var div = document.createElement('div');
	            div.className = 'ag-cal-day';
	            div.textContent = d;

	            var passado  = dataAtual < new Date(hojeData.getFullYear(), hojeData.getMonth(), hojeData.getDate());
	            var temSlot  = diasDisponiveis.indexOf(d) !== -1;
	            var isoData  = state.calAno + '-' + pad(state.calMes+1) + '-' + pad(d);
	            var selected = state.dataSelecionada === isoData;

	            if (passado || !temSlot) {
	                div.classList.add('disabled');
	            } else {
	                div.classList.add('has-slots');
	                if (selected) div.classList.add('selected');
	                (function(iso, dNum) {
	                    div.onclick = function() { selecionarDia(iso, dNum); };
	                })(isoData, d);
	            }

	            grid.appendChild(div);
	        }
	    }
		
	    function carregarEspecialidades() {
	        Promise.all([
	            fetch('http://localhost:9090/api/especialidades').then(function(r) { return r.json(); }),
	            fetch('http://localhost:9090/api/medicos').then(function(r) { return r.json(); })
	        ]).then(function(results) {
	            var especialidades = results[0];
	            var medicos = results[1];

	            var comMedico = new Set();
	            medicos.forEach(function(med) {
	                (med.especialidades || []).forEach(function(e) { comMedico.add(e); });
	            });

	            var grid = document.getElementById('espGrid');
	            if (!grid) return;
	            grid.innerHTML = '';

	            especialidades
	                .filter(function(esp) { return comMedico.has(esp.nome); })
	                .forEach(function(esp) {
	                    var card = document.createElement('div');
	                    card.className = 'ag-esp-card';
	                    card.onclick = function() { window.selecionarEsp(card, esp.nome); };
	                    card.innerHTML =
	                        '<div class="ag-esp-name">' + esp.nome + '</div>' +
	                        '<div class="ag-esp-desc">' + (esp.descricao || '') + '</div>';
	                    grid.appendChild(card);
	                });
	        }).catch(function(err) {
	            console.error('Erro ao carregar especialidades:', err);
	        });
	    }

	    function selecionarDia(iso, dNum) {
	        state.dataSelecionada = iso;
	        state.horaSelecionada = null; // Reseta a hora ao mudar de dia
	        
	        var dataH = document.getElementById('fAg:dataConsultaHidden');
	        if (dataH) dataH.value = iso;
	        
	        var horaH = document.getElementById('fAg:horaConsultaHidden');
	        if (horaH) horaH.value = ''; 
	        renderizarCalendario();

	        var data = new Date(state.calAno, state.calMes, dNum);
	        var nomes = ['Domingo','Segunda','Terça','Quarta','Quinta','Sexta','Sábado'];
	        var sub = document.getElementById('horariosSub');
	        if (sub) sub.textContent = nomes[data.getDay()] + ', ' + dNum + '/' + (state.calMes+1);

	        buscarHorariosLivres(state.medicoId, iso);
	    }

	    function renderizarHorarios(horarios) {
	        var grid = document.getElementById('horariosGrid');
	        if (!grid) return;

	        grid.innerHTML = '';

	        if (!horarios || horarios.length === 0) {
	            grid.innerHTML = '<div class="ag-horarios-empty"><div class="ag-empty-icon">😔</div><div class="ag-empty-text">Sem horários disponíveis</div></div>';
	            return;
	        }

	        horarios.forEach(function(slot) {
	            var btn = document.createElement('button');
	            btn.type = 'button';
	            btn.className = 'ag-horario-btn';
	            if (state.horaSelecionada === slot) btn.classList.add('selected');
	            btn.textContent = slot;
	            
	            btn.onclick = function() {
	                document.querySelectorAll('.ag-horario-btn').forEach(function(b) { b.classList.remove('selected'); });
	                btn.classList.add('selected');
	                state.horaSelecionada = slot;
	                
	                var horaH = document.getElementById('fAg:horaConsultaHidden');
	                if (horaH) horaH.value = slot;
	                var dataH = document.getElementById('fAg:dataConsultaHidden');
	                if (dataH) dataH.value = state.dataSelecionada;

	                atualizarResumo('data', state.dataSelecionada + ' às ' + slot);
	                
	                var erroDataEl = document.getElementById('errData');
	                if (erroDataEl) {
	                    erroDataEl.style.setProperty('display', 'none', 'important'); 
	                    erroDataEl.classList.remove('visible');                      
	                    erroDataEl.classList.remove('active');                       
	                }
	                
	                if (typeof esconderErro === 'function') {
	                    esconderErro('errData', null);
	                }
	            };
	            grid.appendChild(btn);
	        });
	    }

	    window.navegarMes = function(dir) {
	        state.calMes += dir;
	        if (state.calMes < 0)  { state.calMes = 11; state.calAno--; }
	        if (state.calMes > 11) { state.calMes = 0;  state.calAno++; }
	        state.dataSelecionada = null;
	        state.horaSelecionada = null;
	        renderizarCalendario();
	        var grid = document.getElementById('horariosGrid');
	        if (grid) grid.innerHTML = '<div class="ag-horarios-empty"><div class="ag-empty-icon">📅</div><div class="ag-empty-text">Selecione uma data</div></div>';
	    };

	    /* ═══════════════════════════════════════════════
	       RESUMO SIDEBAR
	    ═══════════════════════════════════════════════ */
	    function atualizarResumo(campo, valor) {
	        if (campo === 'paciente') {
	            var el = document.getElementById('rPaciente');
	            var ic = document.getElementById('rIconPaciente');
	            if (el) { el.textContent = valor || 'pendente'; el.classList.toggle('pending', !valor); }
	            if (ic) { ic.className = 'ag-resumo-icon ' + (valor ? 'done' : 'pending'); ic.textContent = valor ? '✓' : '👤'; }
	        }
	        if (campo === 'especialidade') {
	            var el = document.getElementById('rEspecialidade');
	            var ic = document.getElementById('rIconEsp');
	            if (el) { el.textContent = valor; el.classList.remove('pending'); }
	            if (ic) { ic.className = 'ag-resumo-icon done'; ic.textContent = '✓'; }
	        }
	        if (campo === 'medico') {
	            var el = document.getElementById('rMedico');
	            var ic = document.getElementById('rIconMedico');
	            if (el) { el.textContent = valor; el.classList.remove('pending'); }
	            if (ic) { ic.className = 'ag-resumo-icon done'; ic.textContent = '✓'; }
	        }
	        if (campo === 'data') {
	            var el = document.getElementById('rData');
	            var ic = document.getElementById('rIconData');
	            if (el) { el.textContent = valor; el.classList.remove('pending'); }
	            if (ic) { ic.className = 'ag-resumo-icon done'; ic.textContent = '✓'; }
	        }
	    }

	    /* ═══════════════════════════════════════════════
	       SUBMIT/SALVAR AGENDAMENTO
	    ═══════════════════════════════════════════════ */
		window.salvarAgendamento = function() {
		        if (!validarStep(4)) return;

		        var respNomeEl = document.getElementById('respNome');
		        var respCpfEl = document.getElementById('respCpf');
		        var respNascEl = document.getElementById('respNascimento');
		        var respParentescoEl = document.getElementById('respParentesco');

		        var respDataIso = null;
		        if (respNascEl && respNascEl.value.trim() !== '') {
		            var p = respNascEl.value.split('/');
		            respDataIso = p[2] + '-' + p[1] + '-' + p[0];
		        }

		        var pacNascEl = document.getElementById('fAg:pacNascimento');
		        var pacDataIso = null;
		        if (pacNascEl && pacNascEl.value.trim() !== '') {
		            var p2 = pacNascEl.value.split('/');
		            pacDataIso = p2[2] + '-' + p2[1] + '-' + p2[0];
		        }

		        var payload = {
		            especialidade: state.especialidade,
		            medicoId: state.medicoId,
		            dataConsulta: state.dataSelecionada,
		            hora: state.horaSelecionada,
		            observacoes: document.getElementById('fAg:motivoConsulta') ? document.getElementById('fAg:motivoConsulta').value : null,
		            tipoConsulta: state.tipoConsulta,
		            
		            paciente: {
		                nome: document.getElementById('fAg:pacNome').value,
		                cpf: document.getElementById('fAg:pacCpf').value.replace(/\D/g, ''),
		                dataNascimento: pacDataIso, 
		                genero: document.getElementById('fAg:pacGenero').value,
		                telefone: document.getElementById('fAg:pacTelefone').value.replace(/\D/g, '')
		            },

		            respNome: (respNomeEl && respNomeEl.value.trim() !== '') ? respNomeEl.value : null,
		            respCpf: (respCpfEl && respCpfEl.value.trim() !== '') ? respCpfEl.value.replace(/\D/g, '') : null,
		            respDataNascimento: respDataIso,
		            respParentesco: (respParentescoEl && respParentescoEl.value !== '') ? respParentescoEl.value : null
		        };
		        
		        var btn = document.getElementById('btnSubmitJSF');
		        if (btn) btn.disabled = true;

		        // === CAPTURA INTELIGENTE E VALIDAÇÃO DO TOKEN (ATUALIZADO COM INJEÇÃO JSF) ===
		        // 1. Tenta pegar primeiro o token injetado diretamente da sessão do JSF
		        var token = window.TOKEN_SESSAO_VITTAE || null;

		        // 2. Se não achar, faz a varredura automática nos storages por segurança
		        if (!token || token === "" || token.indexOf('#{') === 0) {
		            token = localStorage.getItem('token_vittae') || 
		                    sessionStorage.getItem('token_vittae') || 
		                    localStorage.getItem('token') || 
		                    sessionStorage.getItem('token') || 
		                    localStorage.getItem('jwt') || 
		                    sessionStorage.getItem('jwt');
		        }

		        // 3. Se ainda assim não achar, tenta ler dos Cookies
		        if (!token) {
		            var match = document.cookie.match(new RegExp('(^| )token=([^;]+)'));
		            if (match) token = match[2];
		        }

		        // Limpa espaços ou aspas residuais que o JSF possa injetar
		        if (token) {
		            token = token.trim().replace(/^"|"$/g, '');
		        }

		        // Diagnóstico de credenciais no console do desenvolvedor
		        console.log("=== VERIFICAÇÃO DE CREDENCIAIS ===");
		        console.log("Token JWT recuperado:", token ? token.substring(0, 15) + "..." : "NULO/VAZIO");

		        if (!token || token === "" || token === "null") {
		            console.error("ERRO: Requisição bloqueada localmente. Nenhum token encontrado no Storage ou Sessão JSF.");
		            alert("Sessão inválida ou expirada. Por favor, realize o login novamente antes de prosseguir.");
		            if (btn) btn.disabled = false;
		            return; // Interrompe o envio
		        }

		        // === DISPARO DO AGENDAMENTO PARA O SPRING BOOT ===
		        fetch('http://localhost:9090/api/agendamentos', {
		            method: 'POST',
		            headers: {
		                'Content-Type': 'application/json',
		                'Authorization': 'Bearer ' + token 
		            },
		            body: JSON.stringify(payload)
		        })
		        .then(function(response) {
		            if (!response.ok) {
		                throw new Error('Erro ao salvar agendamento na API. Status: ' + response.status);
		            }
		            return response.json();
		        })
				.then(function(data) {
				            console.log('Agendamento salvo com sucesso no banco!', data);
				            
				            // 1. Abre o modal de sucesso na tela
				            var modal = document.getElementById('modalSucesso');
				            if (modal) {
				                modal.style.display = 'flex';
				            }

				            // 2. CORREÇÃO DO BOTÃO "NOVO AGENDAMENTO" (Para conseguir sair da tela)
				            // Procura o botão azul dentro do seu modal para dar uma função a ele
				            var btnNovoAgendamento = modal ? modal.querySelector('.ag-modal-btn, button') : null;
				            if (!btnNovoAgendamento) {
				                // Caso não ache por classe, tenta pegar pelo texto do botão
				                var botoes = document.querySelectorAll('button');
				                botoes.forEach(function(b) {
				                    if (b.textContent.trim() === 'Novo Agendamento') {
				                        btnNovoAgendamento = b;
				                    }
				                });
				            }

				            // Se encontrar o botão, programa ele para recarregar a página e limpar tudo
				            if (btnNovoAgendamento) {
				                btnNovoAgendamento.onclick = function() {
				                    // Opção A: Recarrega a página do zero para fazer um novo agendamento limpo
				                    window.location.reload();
				                    
				                    // Opção B (Caso queira mandar ele para a tela inicial do Paciente):
				                    // window.location.href = 'inicio.xhtml';
				                };
				            }
				        })
		        .catch(function(error) {
		            console.error('Falha no POST:', error);
		            alert('Não foi possível concluir o agendamento. Verifique suas credenciais.');
		        })
		        .finally(function() {
		            if (btn) btn.disabled = false;
		        });
		    }; // <-- Fecha a função salvarAgendamento corretamente

		    // Inicializa a tela
		    init();

		})();