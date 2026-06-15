var filtroAtivo = 'todas';
var textoBusca = '';
var _dadosConsulta = {};

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
        var paciente = (
            (card.getAttribute('data-paciente') || '') + ' ' +
            (card.getAttribute('data-especialidade') || '')
        ).toLowerCase();

        var okStatus = (filtroAtivo === 'todas') ||
            (filtroAtivo === 'pendente'  && status === 'PENDENTE') ||
            (filtroAtivo === 'realizada' && status === 'REALIZADA') ||
            (filtroAtivo === 'cancelada' && status === 'CANCELADA');

        var okTexto = textoBusca === '' || paciente.indexOf(textoBusca) !== -1;

        if (okStatus && okTexto) {
            card.style.display = '';
            visiveis++;
        } else {
            card.style.display = 'none';
        }
    });

    var countEl = document.getElementById('count-num');
    if (countEl) countEl.textContent = visiveis;

    var empty = document.getElementById('empty-state-js');
    if (visiveis === 0) {
        if (!empty) {
            var div = document.createElement('div');
            div.id = 'empty-state-js';
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

// ── Relatório / PDF ──────────────────────────────────────────────────────────

function abrirModalRelatorio(id, paciente, especialidade, data, hora, status, telefone) {
    _dadosConsulta = { id: id, paciente: paciente, especialidade: especialidade, data: data, hora: hora, status: status, telefone: telefone };
    document.getElementById('relatorioTexto').value = '';
    document.getElementById('relatorioMedicamentos').value = '';
    document.getElementById('modalRelatorio').style.display = 'flex';
}

function fecharModalRelatorio() {
    document.getElementById('modalRelatorio').style.display = 'none';
}

function gerarPDF() {
    var { jsPDF } = window.jspdf;
    var doc = new jsPDF();

    var purple      = [92, 33, 182];
    var lightPurple = [237, 233, 254];
    var dark        = [30, 27, 75];
    var gray        = [107, 114, 128];
    var white       = [255, 255, 255];

    doc.setFillColor.apply(doc, purple);
    doc.rect(0, 0, 210, 40, 'F');

    doc.setTextColor.apply(doc, white);
    doc.setFontSize(22);
    doc.setFont('helvetica', 'bold');
    doc.text('Vittae', 14, 16);

    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.text('Sistema de Agendamento de Consultas', 14, 24);

    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text('RELATÓRIO DE CONSULTA', 14, 34);

    var hoje = new Date().toLocaleDateString('pt-BR');
    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.text('Emitido em: ' + hoje, 148, 34);

    var y = 52;

    function secao(titulo) {
        doc.setFillColor.apply(doc, lightPurple);
        doc.roundedRect(12, y - 5, 186, 9, 2, 2, 'F');
        doc.setTextColor.apply(doc, purple);
        doc.setFontSize(9);
        doc.setFont('helvetica', 'bold');
        doc.text(titulo, 16, y + 1);
        y += 12;
    }

    function linha(label, valor) {
        doc.setFont('helvetica', 'bold');
        doc.setTextColor.apply(doc, gray);
        doc.setFontSize(9);
        doc.text(label, 16, y);
        doc.setFont('helvetica', 'normal');
        doc.setTextColor.apply(doc, dark);
        doc.text(valor || '—', 58, y);
        y += 7;
    }

    secao('DADOS DA CONSULTA');
    linha('Paciente:',      _dadosConsulta.paciente);
    linha('Especialidade:', _dadosConsulta.especialidade);
    linha('Data:',          _dadosConsulta.data);
    linha('Horário:',       _dadosConsulta.hora);
    linha('Status:',        _dadosConsulta.status);

    y += 6;
    secao('RELATÓRIO / OBSERVAÇÕES');

    var relatorio = document.getElementById('relatorioTexto').value.trim() || 'Nenhuma observação registrada.';
    doc.setTextColor.apply(doc, dark);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    var relLines = doc.splitTextToSize(relatorio, 178);
    doc.text(relLines, 16, y);
    y += relLines.length * 6 + 10;

    secao('MEDICAMENTOS PRESCRITOS');

    var meds = document.getElementById('relatorioMedicamentos').value.trim() || 'Nenhum medicamento prescrito.';
    doc.setTextColor.apply(doc, dark);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    var medLines = doc.splitTextToSize(meds, 178);
    doc.text(medLines, 16, y);
    y += medLines.length * 6 + 14;

    doc.setDrawColor.apply(doc, lightPurple);
    doc.setLineWidth(0.5);
    doc.line(12, y, 198, y);
    y += 6;
    doc.setTextColor.apply(doc, gray);
    doc.setFontSize(8);
    doc.text('Este documento foi gerado pelo sistema Vittae e tem validade informativa.', 14, y);

    var nomeArquivo = 'relatorio_' + (_dadosConsulta.paciente || 'consulta').replace(/\s+/g, '_') + '.pdf';
    doc.save(nomeArquivo);

    // ── WhatsApp ─────────────────────────────────────────────────────────
    if (_dadosConsulta.telefone) {
        var numero = _dadosConsulta.telefone.replace(/\D/g, '');
        if (!numero.startsWith('55')) numero = '55' + numero;
        var msg = encodeURIComponent('Olá ' + _dadosConsulta.paciente + ', segue o relatório da sua consulta em ' + _dadosConsulta.data + '.');
        window.open('https://wa.me/' + numero + '?text=' + msg, '_blank');
    }

    // ── Marca como REALIZADA via botão JSF oculto ─────────────────────────
    if (_dadosConsulta.status !== 'REALIZADA' && _dadosConsulta.status !== 'CANCELADA') {
        var hidden = document.getElementById('formConsultasMeico:idConsultaRealizar');
        var btn = document.querySelector('.btn-realizar-oculto');
        console.log('hidden:', hidden, 'btn:', btn);
        if (hidden && btn) {
            hidden.value = _dadosConsulta.id;
            btn.click();
        }
    }
    
    fecharModalRelatorio();
}