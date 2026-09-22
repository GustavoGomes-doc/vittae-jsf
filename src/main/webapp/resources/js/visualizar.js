var filtroAtivo = 'todas';
var textoBusca = '';

function setFiltro(status, el) {
    filtroAtivo = status;
    document.querySelectorAll('.filter-tab').forEach(function(t) { t.classList.remove('active'); });
    el.classList.add('active');
    aplicarFiltros();
}

function filtrar() {
    textoBusca = document.querySelector('.search-input').value.toLowerCase();
    aplicarFiltros();
}

function aplicarFiltros() {
    var cards = document.querySelectorAll('.consulta-card');
    var visiveis = 0;

    cards.forEach(function(card) {
        var status = (card.getAttribute('data-status') || '').toUpperCase();
        var medico = (
            (card.getAttribute('data-medico') || '') + ' ' +
            (card.getAttribute('data-especialidade') || '')
        ).toLowerCase();

        var okStatus = (filtroAtivo === 'todas') ||
            (filtroAtivo === 'pendente'  && status === 'PENDENTE') ||
            (filtroAtivo === 'realizada' && status === 'REALIZADA') ||
            (filtroAtivo === 'cancelada' && status === 'CANCELADA');

        var okTexto = textoBusca === '' || medico.indexOf(textoBusca) !== -1;

        if (okStatus && okTexto) {
            card.style.display = '';
            visiveis++;
        } else {
            card.style.display = 'none';
        }
    });

    var countEl = document.getElementById('count-num');
    if (countEl) countEl.textContent = visiveis;

    var empty = document.getElementById('empty-state');
    if (visiveis === 0) {
        if (!empty) {
            var div = document.createElement('div');
            div.id = 'empty-state';
            div.className = 'empty-state';
            div.innerHTML =
                '<div class="emptyicon">' +
                '<i class="fas fa-calendar-times" style="font-size: 32px; color: #7C3AED;"></i>' +
                '</div>' +
                '<p class="empty-title">Nenhuma consulta encontrada</p>' +
                '<p class="empty-text">Tente ajustar os filtros ou a busca</p>';
            var lista = document.getElementById('lista-consultas');
            if (lista) lista.appendChild(div);
        }
    } else if (empty) {
        empty.remove();
    }
}

function cancelarConsulta(id) {
    if (confirm('Deseja realmente cancelar esta consulta?')) {

        var token = window.TOKEN_SESSAO_VITTAE || '';
        if (!token || token.indexOf('#{') === 0) {
            token = sessionStorage.getItem('token_vittae') || localStorage.getItem('token_vittae') ||
                    sessionStorage.getItem('token')        || localStorage.getItem('token') || '';
        }

        fetch(window.API_BASE_URL + '/api/agendamentos/' + id + '/cancelar', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            }
        })
        .then(function(response) {
            if (response.ok) {
                var cards = document.querySelectorAll('.consulta-card');
                cards.forEach(function(card) {
                    if (card.getAttribute('data-id') == id) {
                        card.setAttribute('data-status', 'CANCELADA');
                        card.style.display = '';
                    }
                });
                alert('Consulta cancelada com sucesso!');
                aplicarFiltros();
            } else {
                alert('Erro ao cancelar consulta.');
            }
        })
        .catch(function(err) {
            alert('Erro de conexão: ' + err);
        });
    }
}

function remarcarConsulta(id) {
    window.location.href = 'agendarConsulta.xhtml?remarcar=true&id=' + id;
}