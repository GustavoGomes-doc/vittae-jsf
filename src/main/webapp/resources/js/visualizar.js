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
        var status = card.getAttribute('data-status');
        var medico = (card.getAttribute('data-medico') + ' ' + card.getAttribute('data-especialidade')).toLowerCase();

		var okStatus = (filtroAtivo === 'todas') ||
		            (filtroAtivo === 'confirmadas' && status === 'confirmada') ||
		            (filtroAtivo === 'pendentes' && status === 'pendente') ||
		            (filtroAtivo === 'concluidas' && status === 'concluida');

		        var okTexto = textoBusca === '' || medico.indexOf(textoBusca) !== -1;
				
		        if (okStatus && okTexto) {
		            card.style.display = '';
		            visiveis++;
		        } else {
		            card.style.display = 'none';
		        }
});

document.getElementById('count-num').textContent = visiveis;

// Substitua o trecho do empty state no seu JS por este:
var empty = document.getElementById('empty-state');
if (visiveis === 0) {
    if (!empty) {
        var div = document.createElement('div');
        div.id = 'empty-state';
        div.className = 'empty-state';
        div.innerHTML =
            '<div class="empty-icon">' +
            '<i class="fas fa-calendar-times" style="font-size: 32px; color: #7C3AED;"></i>' +
            '</div>' +
            '<p class="empty-title">Nenhuma consulta encontrada</p>' +
            '<p class="empty-text">Tente ajustar os filtros ou a busca</p>';
        document.getElementById('lista-consultas').appendChild(div);
    }
} else if (empty) {
    empty.remove();
}

function cancelarConsulta(id) {
    if (confirm('Deseja realmente cancelar esta consulta?')) {
        fetch('http://localhost:8083/api/agendamentos/' + id + '/cancelar', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        })
        .then(function(response) {
            if (response.ok) {
                // Esconde o card visualmente
                var cards = document.querySelectorAll('.consulta-card');
                cards.forEach(function(card) {
                    if (card.getAttribute('data-id') == id) {
                        card.style.display = 'none';
                    }
                });
                alert('Consulta cancelada com sucesso!');
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
}