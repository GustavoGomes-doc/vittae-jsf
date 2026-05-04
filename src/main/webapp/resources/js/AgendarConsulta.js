(function () {
    "use strict";
	
    var state = {
        step: 1,
        pacienteNome: null,
        especialidade: null,
        medicoId: null,
        medicoNome: null,
        medicoValor: null,
        dataSelecionada: null,  // formato YYYY-MM-DD
        horaSelecionada: null,  // formato HH:MM
        tipoConsulta: null,
        disponibilidades: [],   // lista de {diaSemana, horaInicio, horaFim}
        calAno: null,
        calMes: null
    };

    var hoje = new Date();
    state.calAno = hoje.getFullYear();
    state.calMes = hoje.getMonth(); // 0-based

    var DIAS_SEMANA_MAP = {
        0: 'DOMINGO',   // não usado no enum, mas por segurança
        1: 'SEGUNDA',
        2: 'TERCA',
        3: 'QUARTA',
        4: 'QUINTA',
        5: 'SEXTA',
        6: 'SABADO'
    };

    var MESES_PT = ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
                    'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'];

    /* ═══════════════════════════════════════════════
       INIT
    ═══════════════════════════════════════════════ */
    function init() {
        aplicarMascaras();
        monitorarNascimento();
        renderizarCalendario();
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
       MENOR DE IDADE
    ═══════════════════════════════════════════════ */
    function monitorarNascimento() {
        var campo = document.getElementById('fAg:pacNascimento');
        if (!campo) return;
        campo.addEventListener('input', function() {
            var v = campo.value;
            if (v.length < 10) { esconderMenor(); return; }
            var partes = v.split('/');
            if (partes.length !== 3) { esconderMenor(); return; }
            var data = new Date(partes[2], partes[1]-1, partes[0]);
            if (isNaN(data.getTime())) { esconderMenor(); return; }
            var idade = calcularIdade(data);
            var hint = document.getElementById('menorHint');
            var bloco = document.getElementById('blocoResponsavel');
            if (idade < 18) {
                hint.textContent = '⚠ ' + idade + ' anos • Menor de idade';
                hint.classList.add('visible');
                bloco.classList.add('visible');
                atualizarResumo('paciente', campo.value ? obterNomePaciente() : null);
            } else {
                esconderMenor();
            }
        });
    }

    function esconderMenor() {
        var hint = document.getElementById('menorHint');
        var bloco = document.getElementById('blocoResponsavel');
        if (hint) hint.classList.remove('visible');
        if (bloco) bloco.classList.remove('visible');
    }

    function calcularIdade(dataNasc) {
        var hoje = new Date();
        var idade = hoje.getFullYear() - dataNasc.getFullYear();
        var m = hoje.getMonth() - dataNasc.getMonth();
        if (m < 0 || (m === 0 && hoje.getDate() < dataNasc.getDate())) idade--;
        return idade;
    }

    /* ═══════════════════════════════════════════════
       CONVÊNIO
    ═══════════════════════════════════════════════ */
    window.toggleConvenio = function(val) {
        var campo = document.getElementById('campoCarteirinha');
        if (val === 'convenio') {
            campo.classList.add('visible');
            document.getElementById('numCarteirinha').placeholder = 'Número da carteirinha';
        } else {
            campo.classList.remove('visible');
        }
    };

    /* ═══════════════════════════════════════════════
       NAVEGAÇÃO STEPS
    ═══════════════════════════════════════════════ */
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
        // Panels
        for (var i = 1; i <= 4; i++) {
            var p = document.getElementById('panel' + i);
            if (p) p.classList.toggle('active', i === state.step);
        }

        // Stepper visual
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

            // linhas
            var linha = document.getElementById('sl' + i);
            if (linha) linha.classList.toggle('done', i < state.step);
        }

        // Botões
        var btnV = document.getElementById('btnVoltar');
        var btnP = document.getElementById('btnProximo');
        var btnS = document.getElementById('fAg:btnSubmitJSF');

        btnV.style.visibility = state.step === 1 ? 'hidden' : 'visible';

        if (state.step === 4) {
            btnP.style.display  = 'none';
            if (btnS) btnS.style.display = 'inline-flex';
        } else {
            btnP.style.display  = 'inline-flex';
            if (btnS) btnS.style.display = 'none';
        }

        // Carregar disponibilidade ao entrar no step 4
        if (state.step === 4 && state.medicoId) {
            carregarDisponibilidade(state.medicoId);
        }
    }

    /* ═══════════════════════════════════════════════
       VALIDAÇÕES
    ═══════════════════════════════════════════════ */
    function validarStep(step) {
        if (step === 1) return validarDados();
        if (step === 2) return validarEspecialidade();
        if (step === 3) return validarMedico();
        if (step === 4) return validarDataHora();
        return true;
    }

    function validarDados() {
        var ok = true;

        var nome = document.getElementById('fAg:pacNome');
        if (!nome || nome.value.trim().split(' ').length < 2) {
            mostrarErro('errNome', nome); ok = false;
        } else { esconderErro('errNome', nome); }

        var nasc = document.getElementById('fAg:pacNascimento');
        if (!nasc || nasc.value.length < 10) {
            mostrarErro('errNasc', nasc); ok = false;
        } else { esconderErro('errNasc', nasc); }

        var cpf = document.getElementById('fAg:pacCpf');
        if (!cpf || cpf.value.replace(/\D/g,'').length !== 11) {
            mostrarErro('errCpf', cpf); ok = false;
        } else { esconderErro('errCpf', cpf); }

        var sexo = document.getElementById('fAg:pacSexo');
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

        // Menor de idade: validar responsável
        var bloco = document.getElementById('blocoResponsavel');
        if (bloco && bloco.classList.contains('visible')) {
            var rNome = document.getElementById('respNome');
            if (!rNome || !rNome.value.trim()) {
                mostrarErro('errRespNome', rNome); ok = false;
            } else { esconderErro('errRespNome', rNome); }

            var rCpf = document.getElementById('respCpf');
            if (!rCpf || rCpf.value.replace(/\D/g,'').length !== 11) {
                mostrarErro('errRespCpf', rCpf); ok = false;
            } else { esconderErro('errRespCpf', rCpf); }

            var rParent = document.getElementById('respParentesco');
            if (!rParent || !rParent.value) {
                mostrarErro('errRespParent', rParent); ok = false;
            } else { esconderErro('errRespParent', rParent); }
        }
		
		var motivo = document.getElementById('motivoConsulta');
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
        var dataH = document.getElementById('fAg:dataConsultaHidden');
        var horaH = document.getElementById('fAg:horaConsultaHidden');
        var tipoH = document.getElementById('fAg:tipoConsultaHidden');

        if (!state.dataSelecionada || !state.horaSelecionada || !state.tipoConsulta) {
            mostrarErro('errData', null); return false;
        }
        esconderErro('errData', null);

        if (dataH) dataH.value = state.dataSelecionada;
        if (horaH) horaH.value = state.horaSelecionada;
        if (tipoH) tipoH.value = state.tipoConsulta;
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
       ESPECIALIDADE
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

        // filtrar médicos pela especialidade
        filtrarMedicosPorEsp(nome);
    };

    /* ═══════════════════════════════════════════════
       MÉDICO
    ═══════════════════════════════════════════════ */
    window.selecionarMedico = function(card, id, nome, valor) {
        document.querySelectorAll('.ag-medico-card').forEach(function(c) {
            c.classList.remove('selected');
        });
        card.classList.add('selected');

        var hidden = document.getElementById('fAg:medicoIdHidden');
        if (hidden) hidden.value = id;

        state.medicoId    = id;
        state.medicoNome  = nome;
        state.medicoValor = valor;

        atualizarResumo('medico', nome);
        atualizarResumo('valor', valor);
        esconderErro('errMedico', null);

        // Atualizar subtítulo step 4
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

    function filtrarMedicosPorEsp(esp) {
        document.querySelectorAll('.ag-medico-card').forEach(function(c) {
            var tags = c.querySelectorAll('.ag-medico-tag');
			
			if (tags.length === 0) { c.style.display = ''; return; }
			
            var temEsp = false;
            tags.forEach(function(t) {
                if (t.textContent.trim().toLowerCase() === esp.toLowerCase()) temEsp = true;
            });
            c.style.display = temEsp ? '' : 'none';
        });
        var sub = document.getElementById('subMedico');
        if (sub) sub.innerHTML = 'Profissionais em <strong>' + esp + '</strong>. Escolha o ideal para você.';
    }

    /* ═══════════════════════════════════════════════
       TIPO CONSULTA
    ═══════════════════════════════════════════════ */
    window.selecionarTipo = function(card, tipo) {
        document.querySelectorAll('.ag-tipo-card').forEach(function(c) {
            c.classList.remove('selected');
        });
        card.classList.add('selected');
        state.tipoConsulta = tipo;
        esconderErro('errTipo', null);
    };

    /* ═══════════════════════════════════════════════
       CALENDÁRIO
    ═══════════════════════════════════════════════ */
    function carregarDisponibilidade(medicoId) {
        fetch('http://localhost:8081/api/disponibilidade/' + medicoId)
            .then(function(r) { return r.json(); })
            .then(function(data) {
                state.disponibilidades = data;
                renderizarCalendario();
            })
            .catch(function(err) {
                console.error('Erro ao carregar disponibilidade:', err);
                state.disponibilidades = [];
                renderizarCalendario();
            });
    }

    function diasDisponiveisMes(ano, mes) {
        var diasComSlots = [];
        var totalDias = new Date(ano, mes + 1, 0).getDate();
        for (var d = 1; d <= totalDias; d++) {
            var data = new Date(ano, mes, d);
            var diaSemanaJS = data.getDay(); // 0=dom, 1=seg...
            var diaSemanaEnum = DIAS_SEMANA_MAP[diaSemanaJS];
            var temSlot = state.disponibilidades.some(function(disp) {
                return disp.diaSemana === diaSemanaEnum;
            });
            if (temSlot) diasComSlots.push(d);
        }
        return diasComSlots;
    }

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

        // cabeçalho
        ['D','S','T','Q','Q','S','S'].forEach(function(d) {
            var div = document.createElement('div');
            div.className = 'ag-cal-dow';
            div.textContent = d;
            grid.appendChild(div);
        });

        // células vazias
        for (var e = 0; e < primeiroDia; e++) {
            var vazio = document.createElement('div');
            vazio.className = 'ag-cal-day empty';
            grid.appendChild(vazio);
        }

        // dias
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
                if (temSlot) div.classList.add('has-slots');
                if (selected) div.classList.add('selected');
                (function(iso, dNum) {
                    div.onclick = function() { selecionarDia(iso, dNum); };
                })(isoData, d);
            }

            grid.appendChild(div);
        }
    }

    function selecionarDia(iso, dNum) {
        state.dataSelecionada = iso;
        state.horaSelecionada = null;
        renderizarCalendario();
        renderizarHorarios(iso);

        // Atualizar subtítulo dos horários
        var data = new Date(state.calAno, state.calMes, dNum);
        var nomes = ['Domingo','Segunda','Terça','Quarta','Quinta','Sexta','Sábado'];
        var sub = document.getElementById('horariosSub');
        if (sub) sub.textContent = nomes[data.getDay()] + ', ' + dNum + '/' + (state.calMes+1);
    }

    function renderizarHorarios(iso) {
        var grid = document.getElementById('horariosGrid');
        if (!grid) return;

        var data = new Date(iso + 'T00:00:00');
        var diaSemanaEnum = DIAS_SEMANA_MAP[data.getDay()];

        var slots = [];
        state.disponibilidades.forEach(function(disp) {
            if (disp.diaSemana !== diaSemanaEnum) return;
            var hInicio = parseInt(disp.horaInicio.split(':')[0]);
            var mInicio = parseInt(disp.horaInicio.split(':')[1]);
            var hFim    = parseInt(disp.horaFim.split(':')[0]);
            var mFim    = parseInt(disp.horaFim.split(':')[1]);
            var cur = hInicio * 60 + mInicio;
            var fim = hFim * 60 + mFim;
            while (cur < fim) {
                slots.push(pad(Math.floor(cur/60)) + ':' + pad(cur%60));
                cur += 30;
            }
        });

        grid.innerHTML = '';

        if (slots.length === 0) {
            grid.innerHTML = '<div class="ag-horarios-empty"><div class="ag-empty-icon">😔</div><div class="ag-empty-text">Sem horários disponíveis</div></div>';
            return;
        }

        slots.forEach(function(slot) {
            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'ag-horario-btn';
            if (state.horaSelecionada === slot) btn.classList.add('selected');
            btn.textContent = slot;
            btn.onclick = function() {
                document.querySelectorAll('.ag-horario-btn').forEach(function(b) { b.classList.remove('selected'); });
                btn.classList.add('selected');
                state.horaSelecionada = slot;
                atualizarResumo('data', state.dataSelecionada + ' às ' + slot);
                esconderErro('errData', null);
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
        if (campo === 'valor') {
            var block = document.getElementById('rValorBlock');
            var el    = document.getElementById('rValor');
            if (block) block.style.display = valor ? 'flex' : 'none';
            if (el)    el.textContent = valor ? 'R$ ' + parseFloat(valor).toFixed(2).replace('.',',') : '—';
        }
    }

    /* ═══════════════════════════════════════════════
       UTILS
    ═══════════════════════════════════════════════ */
    function pad(n) { return n < 10 ? '0' + n : '' + n; }

    /* ═══════════════════════════════════════════════
       START
    ═══════════════════════════════════════════════ */
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();