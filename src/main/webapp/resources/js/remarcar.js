(function () {
    "use strict";

    var remarcarState = {
        medicoId: null,
        consultaId: null,
        dataSelecionada: null,
        horaSelecionada: null,
        diasDisponiveis: [],
        calAno: null,
        calMes: null
    };

    var DIAS_SEMANA_MAP = {
        0: 'DOMINGO', 1: 'SEGUNDA', 2: 'TERCA',
        3: 'QUARTA',  4: 'QUINTA',  5: 'SEXTA', 6: 'SABADO'
    };

    var MESES_PT = ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
                    'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'];

    function pad(n) { return n < 10 ? '0' + n : n; }

    // ── Abre o modal de remarcação ──────────────────────────────
	window.abrirModalRemarcar = function(consultaId, medicoId) {
	    remarcarState.consultaId    = consultaId;
	    remarcarState.medicoId      = medicoId;
	    remarcarState.dataSelecionada = null;
	    remarcarState.horaSelecionada = null;
	    remarcarState.diasDisponiveis = [];

	    var hoje = new Date();
	    remarcarState.calAno = hoje.getFullYear();
	    remarcarState.calMes = hoje.getMonth();

	    document.getElementById('remarcarOverlay').style.display = 'flex';

	    limparHorarios();
	    // NÃO renderiza calendário ainda — espera a disponibilidade
	    document.getElementById('remarcarCalGrid').innerHTML = '<div style="text-align:center;padding:20px;color:#9ca3af">⏳ Carregando...</div>';
	    document.getElementById('remarcarCalTitulo').textContent = '';
	    carregarDisponibilidadeRemarcar(medicoId);
	};

    window.fecharModalRemarcar = function() {
        document.getElementById('remarcarOverlay').style.display = 'none';
    };

    // ── Disponibilidade do médico ───────────────────────────────
    function carregarDisponibilidadeRemarcar(medicoId) {
        fetch(window.API_BASE_URL + '/api/medicos/' + medicoId + '/horarios-livres')
            .then(function(r) { return r.json(); })
            .then(function(data) {
                remarcarState.diasDisponiveis = data.diasDisponiveis || [];
                renderizarCalendarioRemarcar();
            })
            .catch(function() {
                remarcarState.diasDisponiveis = [];
                renderizarCalendarioRemarcar();
            });
    }

    function diasDisponiveisMesRemarcar(ano, mes) {
        var result = [];
        var total  = new Date(ano, mes + 1, 0).getDate();
        for (var d = 1; d <= total; d++) {
            var diaSemana = DIAS_SEMANA_MAP[new Date(ano, mes, d).getDay()];
            if (remarcarState.diasDisponiveis.indexOf(diaSemana) !== -1) result.push(d);
        }
        return result;
    }

    // ── Calendário ─────────────────────────────────────────────
    function renderizarCalendarioRemarcar() {
        var grid   = document.getElementById('remarcarCalGrid');
        var titulo = document.getElementById('remarcarCalTitulo');
        if (!grid || !titulo) return;

        titulo.textContent = MESES_PT[remarcarState.calMes] + ' ' + remarcarState.calAno;

        var diasOk    = diasDisponiveisMesRemarcar(remarcarState.calAno, remarcarState.calMes);
        var primeiro  = new Date(remarcarState.calAno, remarcarState.calMes, 1).getDay();
        var total     = new Date(remarcarState.calAno, remarcarState.calMes + 1, 0).getDate();
        var hoje      = new Date();
        var hojeStr   = hoje.getFullYear() + '-' + pad(hoje.getMonth()+1) + '-' + pad(hoje.getDate());

        grid.innerHTML = '';

        ['D','S','T','Q','Q','S','S'].forEach(function(d) {
            var div = document.createElement('div');
            div.className = 'rc-dow';
            div.textContent = d;
            grid.appendChild(div);
        });

        for (var e = 0; e < primeiro; e++) {
            var vazio = document.createElement('div');
            vazio.className = 'rc-day empty';
            grid.appendChild(vazio);
        }

        for (var d = 1; d <= total; d++) {
            var iso      = remarcarState.calAno + '-' + pad(remarcarState.calMes+1) + '-' + pad(d);
            var passado  = iso < hojeStr;
            var temSlot  = diasOk.indexOf(d) !== -1;
            var selected = remarcarState.dataSelecionada === iso;

            var div = document.createElement('div');
            div.className = 'rc-day';
            div.textContent = d;

            if (passado || !temSlot) {
                div.classList.add('disabled');
            } else {
                div.classList.add('has-slots');
                if (selected) div.classList.add('selected');
                (function(isoDate, dNum) {
                    div.onclick = function() { selecionarDiaRemarcar(isoDate, dNum); };
                })(iso, d);
            }

            grid.appendChild(div);
        }
    }

    window.navegarMesRemarcar = function(dir) {
        remarcarState.calMes += dir;
        if (remarcarState.calMes < 0)  { remarcarState.calMes = 11; remarcarState.calAno--; }
        if (remarcarState.calMes > 11) { remarcarState.calMes = 0;  remarcarState.calAno++; }
        remarcarState.dataSelecionada = null;
        remarcarState.horaSelecionada = null;
        renderizarCalendarioRemarcar();
        limparHorarios();
    };

    function selecionarDiaRemarcar(iso, dNum) {
        remarcarState.dataSelecionada = iso;
        remarcarState.horaSelecionada = null;
        renderizarCalendarioRemarcar();

        var nomes = ['Domingo','Segunda','Terça','Quarta','Quinta','Sexta','Sábado'];
        var sub = document.getElementById('remarcarHorariosSub');
        if (sub) sub.textContent = nomes[new Date(remarcarState.calAno, remarcarState.calMes, dNum).getDay()] + ', ' + pad(dNum) + '/' + pad(remarcarState.calMes+1);

        buscarHorariosRemarcar(remarcarState.medicoId, iso);
    }

    // ── Horários ───────────────────────────────────────────────
    function buscarHorariosRemarcar(medicoId, dataISO) {
        var grid = document.getElementById('remarcarHorariosGrid');
        if (grid) grid.innerHTML = '<div class="rc-horarios-empty">⏳ Carregando horários...</div>';

        fetch(window.API_BASE_URL + '/api/medicos/' + medicoId + '/horarios-livres?data=' + dataISO)
            .then(function(r) { return r.json(); })
            .then(function(data) { renderizarHorariosRemarcar(data.horariosLivres || []); })
            .catch(function()   { renderizarHorariosRemarcar([]); });
    }

    function renderizarHorariosRemarcar(horarios) {
        var grid = document.getElementById('remarcarHorariosGrid');
        if (!grid) return;
        grid.innerHTML = '';

        if (!horarios.length) {
            grid.innerHTML = '<div class="rc-horarios-empty">😔 Sem horários disponíveis</div>';
            return;
        }

        horarios.forEach(function(slot) {
            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'rc-horario-btn';
            if (remarcarState.horaSelecionada === slot) btn.classList.add('selected');
            btn.textContent = slot;
            btn.onclick = function() {
                document.querySelectorAll('.rc-horario-btn').forEach(function(b) { b.classList.remove('selected'); });
                btn.classList.add('selected');
                remarcarState.horaSelecionada = slot;
                document.getElementById('remarcarErro').style.display = 'none';
            };
            grid.appendChild(btn);
        });
    }

    function limparHorarios() {
        var grid = document.getElementById('remarcarHorariosGrid');
        if (grid) grid.innerHTML = '<div class="rc-horarios-empty">📅 Selecione uma data</div>';
        var sub = document.getElementById('remarcarHorariosSub');
        if (sub) sub.textContent = 'Selecione uma data no calendário';
    }

    // ── Confirmar remarcação ────────────────────────────────────
    window.confirmarRemarcarJS = function() {
        if (!remarcarState.dataSelecionada || !remarcarState.horaSelecionada) {
            document.getElementById('remarcarErro').style.display = 'block';
            return;
        }

        var token = window.TOKEN_SESSAO_VITTAE || '';
        if (!token || token.indexOf('#{') === 0) {
            token = sessionStorage.getItem('token_vittae') || localStorage.getItem('token_vittae') ||
                    sessionStorage.getItem('token') || localStorage.getItem('token') || '';
        }

        var payload = {
            dataConsulta: remarcarState.dataSelecionada,
            hora:         remarcarState.horaSelecionada,
            status:       'PENDENTE'
        };

        var btnConfirmar = document.getElementById('btnConfirmarRemarcar');
        if (btnConfirmar) btnConfirmar.disabled = true;

        fetch(window.API_BASE_URL + '/api/agendamentos/' + remarcarState.consultaId, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(payload)
        })
        .then(function(r) {
            if (!r.ok) throw new Error('Status ' + r.status);
            window.fecharModalRemarcar();
            // Recarrega a lista via JSF AJAX ou reload simples
            window.location.reload();
        })
        .catch(function(err) {
            alert('Erro ao remarcar: ' + err.message);
        })
        .finally(function() {
            if (btnConfirmar) btnConfirmar.disabled = false;
        });
    };

})();
