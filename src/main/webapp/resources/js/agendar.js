(function () {
    "use strict";
	
    var state = {
        step: 1,
        pacienteNome: null,
        especialidade: null,
        meicoId: null,
        meicoNome: null,
        meicoValor: null,
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
        monitorarNascimentoResponsavel();
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
       MONITORAR NASCIMENTO (RESPONSÁVEL)
    ═══════════════════════════════════════════════ */
    function monitorarNascimentoResponsavel() {
        var campo = document.getElementById('respNascimento');
        if (!campo) return;

        var hint = document.getElementById('respIdadeHint');
        if (!hint) {
            hint = document.createElement('span');
            hint.id = 'respIdadeHint';
            hint.className = 'ag-menor-hint';
            campo.parentNode.insertBefore(hint, campo.nextSibling);
        }

        campo.addEventListener('input', function() {
            var v = campo.value;
            var errEl = document.getElementById('errRespNasc');

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

            hint.classList.add('visible');

            if (idade < 18) {
                hint.textContent = '⚠ ' + idade + ' anos • Responsável deve ter +18 anos';
                hint.style.color = '#f87171';
                mostrarErro('errRespNasc', campo);
                if (errEl) errEl.innerHTML = '<i class="fas fa-exclamation-circle"></i> O responsável deve ser maior de idade (+18)';
            } else {
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

        if (state.step === 4 && state.meicoId) {
            carregarDisponibilidade(state.meicoId);
        }
    }

    function validarStep(step) {
        if (step === 1) return validarDados();
        if (step === 2) return validarEspecialidade();
        if (step === 3) return validarMeico();
        if (step === 4) return validarDataHora();
        return true;
    }

    /* ═══════════════════════════════════════════════
       VALIDAÇÕES STEP 1
    ═══════════════════════════════════════════════ */
    function validarDados() {
        var ok = true;

        var nome = document.getElementById('fAg:pacNome');
        if (!nome || nome.value.trim().split(/\s+/).length < 2) {
            mostrarErro('errNome', nome); ok = false;
        } else { esconderErro('errNome', nome); }

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

        var cpf = document.getElementById('fAg:pacCpf');
        if (!cpf || cpf.value.replace(/\D/g,'').length !== 11) {
            mostrarErro('errCpf', cpf); ok = false;
        } else { esconderErro('errCpf', cpf); }

        var sexo = document.getElementById('fAg:pacGenero');
        if (!sexo || !sexo.value) {
            mostrarErro('errSexo', sexo); ok = false;
        } else { esconderErro('errSexo', sexo); }

        var tel = document.getElementById('fAg:pacTelefone');
        if (!tel || tel.value.replace(/\D/g,'').length < 10) {
            mostrarErro('errTel', tel); ok = false;
        } else { esconderErro('errTel', tel); }

        var t1 = document.getElementById('termoVerdade');
        var t2 = document.getElementById('termoLgpd');
        if (!t1 || !t1.checked) { mostrarErro('errTermo1', null); ok = false; }
        else { esconderErro('errTermo1', null); }
        if (!t2 || !t2.checked) { mostrarErro('errTermo2', null); ok = false; }
        else { esconderErro('errTermo2', null); }

        var bloco = document.getElementById('blocoResponsavel');
        if (bloco && bloco.classList.contains('visible') && idadePaciente !== null && idadePaciente < 18) {

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

            var rParent = document.getElementById('respParentesco');
            if (!rParent || !rParent.value) {
                mostrarErro('errRespParent', rParent); ok = false;
            } else { esconderErro('errRespParent', rParent); }
        }

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

    function validarMeico() {
        var hidden = document.getElementById('fAg:meicoIdHidden');
        if (!hidden || !hidden.value) {
            mostrarErro('errMeico', null); return false;
        }
        esconderErro('errMeico', null);
        return true;
    }

	function validarDataHora() {
	    var dataH = document.getElementById('fAg:dataConsultaHidden');
	    var horaH = document.getElementById('fAg:horaConsultaHidden');
	    var tipoH = document.getElementById('fAg:tipoConsultaHidden');

	    if (!state.dataSelecionada && dataH && dataH.value) {
	        state.dataSelecionada = dataH.value;
	    }
	    if (!state.horaSelecionada && horaH && horaH.value) {
	        state.horaSelecionada = horaH.value;
	    }

	    console.log("=== VERIFICAÇÃO DE DADOS ===");
	    console.log("State Data:", state.dataSelecionada, " | InputHidden Data:", dataH ? dataH.value : "não achou input");
	    console.log("State Hora:", state.horaSelecionada, " | InputHidden Hora:", horaH ? horaH.value : "não achou input");

	    var temData = state.dataSelecionada || (dataH && dataH.value);
	    var temHora = state.horaSelecionada || (horaH && horaH.value);

	    if (!temData || !temHora) {
	        mostrarErro('errData', null); 
	        return false;
	    }
	    
	    esconderErro('errData', null);

	    if (dataH) dataH.value = state.dataSelecionada || dataH.value;
	    if (horaH) horaH.value = state.horaSelecionada || horaH.value;
	    
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
       ESPECIALIDADE & MÉico
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

        carregarMeicosPorEsp(nome);
    };

    function carregarMeicosPorEsp(especialidade) {
        var grid = document.getElementById('meicosGrid');
        var sub  = document.getElementById('subMeico');
        if (!grid) return;

        grid.innerHTML = '<div style="text-align:center;padding:32px;color:#a5b4fc">⏳ Carregando méicos...</div>';

        fetch(window.API_BASE_URL + '/api/meicos?especialidade=' + encodeUicomponent(especialidade))
            .then(function(r) { return r.json(); })
            .then(function(meicos) {
                renderizarCardsMeicos(meicos);
                if (sub) sub.innerHTML = 'Profissionais em <strong>' + especialidade + '</strong>. Escolha o ideal para você.';
            })
            .catch(function(err) {
                console.error('Erro ao carregar méicos:', err);
                grid.innerHTML = '<div style="text-align:center;padding:32px;color:#f87171">Erro ao carregar méicos. Tente novamente.</div>';
            });
    }

    function renderizarCardsMeicos(meicos) {
        var grid = document.getElementById('meicosGrid');
        if (!grid) return;
        grid.innerHTML = '';

        if (!meicos || meicos.length === 0) {
            grid.innerHTML = '<div style="text-align:center;padding:32px;color:#a5b4fc">Nenhum méico disponível para essa especialidade.</div>';
            return;
        }

        meicos.forEach(function(med) {
            var iniciais = (med.nome || '').split(' ').map(function(p) { return p[0]; }).join('').slice(0,2).toUpperCase();
            var cores = ['#4f46e5','#7c3aed','#2563eb','#0891b2','#059669','#d97706'];
            var cor   = cores[med.id % cores.length] || '#4f46e5';

            var card = document.createElement('div');
            card.className = 'ag-meico-card';
            card.setAttribute('data-nome', med.nome || '');
            card.setAttribute('data-crm',  med.crm  || '');
            card.setAttribute('data-id',   med.id   || '');
            card.setAttribute('data-valor', med.valorConsulta || '');
            card.onclick = function() { window.selecionarMeico(card); };

            var fotoHtml = med.foto
                ? '<img src="data:image/jpeg;base64,' + med.foto + '" style="width:100%;height:100%;object-fit:cover;border-radius:50%" />'
                : iniciais;

			var tagsHtml = (med.especialidades || []).map(function(e) {
			    var nome = typeof e === 'string' ? e : e.nome;
			    return '<span class="ag-meico-tag"><i class="fas fa-stethoscope"></i> ' + nome + '</span>';
			}).join('');

            var valor = med.valorConsulta ? parseFloat(med.valorConsulta).toFixed(2).replace('.',',') : '—';

            card.innerHTML =
                '<div class="ag-meico-check"><i class="fas fa-check"></i></div>' +
                '<div class="ag-meico-avatar" style="background:' + cor + '">' + fotoHtml + '</div>' +
                '<div class="ag-meico-nome">' + (med.nome || '') + '</div>' +
                '<div class="ag-meico-crm">CRM ' + (med.crm || '') + '</div>' +
                '<div class="ag-meico-tags">' + tagsHtml + '</div>' +
                '<div class="ag-meico-info">' +
                    '<div class="ag-meico-info-item"><span class="ag-meico-info-label">Duração</span><span class="ag-meico-info-value">' + (med.tempoConsultaMinutos || '—') + ' min</span></div>' +
                    '<div class="ag-meico-info-item"><span class="ag-meico-info-label">Consulta</span><span class="ag-meico-info-value">R$ ' + valor + '</span></div>' +
                    '<div class="ag-meico-info-item"><span class="ag-meico-info-label">Telefone</span><span class="ag-meico-info-value">' + (med.telefone || '—') + '</span></div>' +
                '</div>';

            grid.appendChild(card);
        });
    }

	window.selecionarMeico = function(card) {
	        document.querySelectorAll('.ag-meico-card').forEach(function(c) {
	            c.classList.remove('selected');
	        });
	        card.classList.add('selected');

	        var id    = card.getAttribute('data-id');
	        var nome  = card.getAttribute('data-nome');
	        var valor = card.getAttribute('data-valor');

	        var hidden = document.getElementById('fAg:meicoIdHidden');
	        if (hidden) hidden.value = id;

	        state.meicoId    = id;
	        state.meicoNome  = nome;
	        state.meicoValor = valor;

	        atualizarResumo('meico', nome);
	        atualizarResumo('valor', valor);
	        esconderErro('errMeico', null);

	        var sub = document.getElementById('subDataHora');
	        if (sub) sub.textContent = 'Com ' + nome + '.';
	    };

	    window.filtrarMeicos = function() {
	        var q = (document.getElementById('buscaMeico').value || '').toLowerCase();
	        document.querySelectorAll('.ag-meico-card').forEach(function(c) {
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
	    function carregarDisponibilidade(meicoId) {
	        fetch(window.API_BASE_URL + '/api/meicos/' + meicoId + '/horarios-livres')
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

	    function buscarHorariosLivres(meicoId, dataISO) {
	        var grid = document.getElementById('horariosGrid');
	        if (grid) grid.innerHTML = '<div class="ag-horarios-empty"><div class="ag-emptyicon">⏳</div><div class="ag-empty-text">Carregando horários...</div></div>';

	        fetch(window.API_BASE_URL + '/api/meicos/' + meicoId + '/horarios-livres?data=' + dataISO)
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
	            fetch(window.API_BASE_URL + '/api/especialidades').then(function(r) { return r.json(); }),
	            fetch(window.API_BASE_URL + '/api/meicos').then(function(r) { return r.json(); })
	        ]).then(function(results) {
	            var especialidades = results[0];
	            var meicos = results[1];

	            var comMeico = new Set();
			meicos.forEach(function(med) {
			    (med.especialidades || []).forEach(function(e) {
			        comMeico.add(typeof e === 'string' ? e : e.nome);
			    });
			});

	            var grid = document.getElementById('espGrid');
	            if (!grid) return;
	            grid.innerHTML = '';

	            especialidades
	                .filter(function(esp) { return comMeico.has(esp.nome); })
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
	        state.horaSelecionada = null;
	        
	        var dataH = document.getElementById('fAg:dataConsultaHidden');
	        if (dataH) dataH.value = iso;
	        
	        var horaH = document.getElementById('fAg:horaConsultaHidden');
	        if (horaH) horaH.value = ''; 
	        renderizarCalendario();

	        var data = new Date(state.calAno, state.calMes, dNum);
	        var nomes = ['Domingo','Segunda','Terça','Quarta','Quinta','Sexta','Sábado'];
	        var sub = document.getElementById('horariosSub');
	        if (sub) sub.textContent = nomes[data.getDay()] + ', ' + dNum + '/' + (state.calMes+1);

	        buscarHorariosLivres(state.meicoId, iso);
	    }

	    function renderizarHorarios(horarios) {
	        var grid = document.getElementById('horariosGrid');
	        if (!grid) return;

	        grid.innerHTML = '';

	        if (!horarios || horarios.length === 0) {
	            grid.innerHTML = '<div class="ag-horarios-empty"><div class="ag-emptyicon">😔</div><div class="ag-empty-text">Sem horários disponíveis</div></div>';
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
	        if (grid) grid.innerHTML = '<div class="ag-horarios-empty"><div class="ag-emptyicon">📅</div><div class="ag-empty-text">Selecione uma data</div></div>';
	    };

	    /* ═══════════════════════════════════════════════
	       RESUMO SIDEBAR
	    ═══════════════════════════════════════════════ */
	    function atualizarResumo(campo, valor) {
	        if (campo === 'paciente') {
	            var el = document.getElementById('rPaciente');
	            var ic = document.getElementById('iconPaciente');
	            if (el) { el.textContent = valor || 'pendente'; el.classList.toggle('pending', !valor); }
	            if (ic) { ic.className = 'ag-resumoicon ' + (valor ? 'done' : 'pending'); ic.textContent = valor ? '✓' : '👤'; }
	        }
	        if (campo === 'especialidade') {
	            var el = document.getElementById('rEspecialidade');
	            var ic = document.getElementById('iconEsp');
	            if (el) { el.textContent = valor; el.classList.remove('pending'); }
	            if (ic) { ic.className = 'ag-resumoicon done'; ic.textContent = '✓'; }
	        }
	        if (campo === 'meico') {
	            var el = document.getElementById('rMeico');
	            var ic = document.getElementById('iconMeico');
	            if (el) { el.textContent = valor; el.classList.remove('pending'); }
	            if (ic) { ic.className = 'ag-resumoicon done'; ic.textContent = '✓'; }
	        }
	        if (campo === 'data') {
	            var el = document.getElementById('rData');
	            var ic = document.getElementById('iconData');
	            if (el) { el.textContent = valor; el.classList.remove('pending'); }
	            if (ic) { ic.className = 'ag-resumoicon done'; ic.textContent = '✓'; }
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
		            meicoId: state.meicoId,
		            dataConsulta: state.dataSelecionada,
		            hora: state.horaSelecionada,
		            observacoes: document.getElementById('fAg:motivoConsulta') ? document.getElementById('fAg:motivoConsulta').value : null,
		            tipoConsulta: state.tipoConsulta,
		            
					paciente: {
					    nome: document.getElementById('fAg:pacNome').value,
					    cpf: document.getElementById('fAg:pacCpf').value.replace(/\D/g, ''),
					    nascimento: pacDataIso,        // ← CORRIGIDO
					    genero: document.getElementById('fAg:pacGenero').value,
					    telefone: document.getElementById('fAg:pacTelefone').value.replace(/\D/g, '')
					},

					respNome: (respNomeEl && respNomeEl.value.trim() !== '') ? respNomeEl.value : null,
					respCpf: (respCpfEl && respCpfEl.value.trim() !== '') ? respCpfEl.value.replace(/\D/g, '') : null,
					respDataNascimento: respDataIso,   // já estava certo, só garantir que o DTO tem o campo
					respParentesco: (respParentescoEl && respParentescoEl.value !== '') ? respParentescoEl.value : null
		        };
		        
		        var btn = document.getElementById('btnSubmitJSF');
		        if (btn) btn.disabled = true;

		        var token = window.TOKEN_SESSAO_VITTAE || null;

		        if (!token || token === "" || token.indexOf('#{') === 0) {
		            token = localStorage.getItem('token_vittae') || 
		                    sessionStorage.getItem('token_vittae') || 
		                    localStorage.getItem('token') || 
		                    sessionStorage.getItem('token') || 
		                    localStorage.getItem('jwt') || 
		                    sessionStorage.getItem('jwt');
		        }

		        if (!token) {
		            var match = document.cookie.match(new RegExp('(^| )token=([^;]+)'));
		            if (match) token = match[2];
		        }

		        if (token) {
		            token = token.trim().replace(/^"|"$/g, '');
		        }

		        console.log("=== VERIFICAÇÃO DE CREDENCIAIS ===");
		        console.log("Token JWT recuperado:", token ? token.substring(0, 15) + "..." : "NULO/VAZIO");

		        if (!token || token === "" || token === "null") {
		            console.error("ERRO: Nenhum token encontrado.");
		            alert("Sessão inválida ou expirada. Por favor, realize o login novamente antes de prosseguir.");
		            if (btn) btn.disabled = false;
		            return;
		        }

		        fetch(window.API_BASE_URL + '/api/agendamentos', {
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
				            
				            var resumo = document.getElementById('modalResumoTexto');
				            if (resumo) {
				                resumo.innerHTML =
				                    '<strong>Paciente:</strong> ' + state.pacienteNome + '<br>' +
				                    '<strong>Especialidade:</strong> ' + state.especialidade + '<br>' +
				                    '<strong>Meico:</strong> ' + state.meicoNome + '<br>' +
				                    '<strong>Data:</strong> ' + state.dataSelecionada + ' as ' + state.horaSelecionada;
				            }
				            
				            var modal = document.getElementById('modalSucesso');
				            if (modal) {
				                modal.style.display = 'flex';
				            }

			            var btnNovoAgendamento = modal ? modal.querySelector('.ag-modal-btn, button') : null;
			            if (!btnNovoAgendamento) {
			                var botoes = document.querySelectorAll('button');
			                botoes.forEach(function(b) {
			                    if (b.textContent.trim() === 'Novo Agendamento') {
			                        btnNovoAgendamento = b;
			                    }
			                });
			            }

			            if (btnNovoAgendamento) {
			                btnNovoAgendamento.onclick = function() {
			                    window.location.reload();
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
		    };

		    init();

		})();