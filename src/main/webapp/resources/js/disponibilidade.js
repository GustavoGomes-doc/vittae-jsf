(function () {
    "use strict";

    var DIAS_LABEL = {
        'SEGUNDA': 'Segunda',
        'TERCA':   'Terça',
        'QUARTA':  'Quarta',
        'QUINTA':  'Quinta',
        'SEXTA':   'Sexta',
        'SABADO':  'Sábado'
    };

    function pad(n) { return n < 10 ? '0' + n : '' + n; }

    function gerarSlots(horaInicio, horaFim, intervaloMin) {
        var slots = [];
        var partsI = horaInicio.split(':');
        var partsF = horaFim.split(':');
        var minI = parseInt(partsI[0], 10) * 60 + parseInt(partsI[1], 10);
        var minF = parseInt(partsF[0], 10) * 60 + parseInt(partsF[1], 10);
        for (var m = minI; m < minF; m += intervaloMin) {
            slots.push(pad(Math.floor(m / 60)) + ':' + pad(m % 60));
        }
        return slots;
    }

    function renderizarSemana() {
        ['SEGUNDA','TERCA','QUARTA','QUINTA','SEXTA','SABADO'].forEach(function(dia) {
            var col = document.getElementById('slot' + dia);
            if (col) col.innerHTML = '';
        });

        var token    = document.querySelector('meta[name="token"]');
        var medicoId = document.querySelector('meta[name="medicoId"]');
        if (!token || !medicoId) return;

        fetch(window.API_BASE_URL + '/api/disponibilidade/' + medicoId.content, {
            headers: { 'Authorization': 'Bearer ' + token.content }
        })
        .then(function(r) { return r.json(); })
        .then(function(disps) {
            fetch(window.API_BASE_URL + '/api/usuarios/' + medicoId.content, {
                headers: { 'Authorization': 'Bearer ' + token.content }
            })
            .then(function(r) { return r.json(); })
            .then(function(medico) {
                var intervalo = medico.tempoConsultaMinutos || 30;
                disps.forEach(function(disp) {
                    var col = document.getElementById('slot' + disp.diaSemana);
                    if (!col) return;
                    var slots = gerarSlots(disp.horaInicio, disp.horaFim, intervalo);
                    slots.forEach(function(slot) {
                        var el = document.createElement('div');
                        el.className = 'disp-slot';
                        el.textContent = slot;
                        col.appendChild(el);
                    });
                });
            });
        })
        .catch(function(err) {
            console.error('Erro ao renderizar semana:', err);
        });
    }

    function injetarMetas() {
        var tokenInput  = document.querySelector('input[id$="tokenHidden"]');
        var medicoInput = document.querySelector('input[id$="medicoIdHidden"]');

        if (tokenInput && medicoInput) {
            var metaToken = document.createElement('meta');
            metaToken.name = 'token';
            metaToken.content = tokenInput.value;
            document.head.appendChild(metaToken);

            var metaMedico = document.createElement('meta');
            metaMedico.name = 'medicoId';
            metaMedico.content = medicoInput.value;
            document.head.appendChild(metaMedico);
        }

        renderizarSemana();
    }

    document.addEventListener('DOMContentLoaded', function () {
        injetarMetas();

        document.querySelectorAll('.input-hora-disp').forEach(function (input) {
            input.addEventListener('input', function () {
                var raw = this.value.replace(/\D/g, '').slice(0, 4);
                if (raw.length === 0) { this.value = ''; return; }
                if (raw.length === 1 && parseInt(raw, 10) >= 3) {
                    raw = '0' + raw;
                    this.value = raw + ':';
                    return;
                }
                if (raw.length <= 2) {
                    this.value = raw.length === 2 ? raw + ':' : raw;
                } else {
                    this.value = raw.slice(0, 2) + ':' + raw.slice(2);
                }
            });
        });
    });

})();