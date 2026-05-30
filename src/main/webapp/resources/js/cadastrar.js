function toggleHorario(checkbox, divId) {
    const div = document.getElementById(divId);
    if (div) div.style.display = checkbox.checked ? 'flex' : 'none';
}

document.addEventListener('DOMContentLoaded', () => {

    const form = document.getElementById('formCadastro');
    const cpfInput = document.getElementById('formCadastro:cpfMedico');
    const cepInput = document.getElementById('formCadastro:cepMedico');
    const fotoInput = document.getElementById('formCadastro:fotoMedico');
    const telefoneInput = document.getElementById('formCadastro:telefoneMedico');
    const fotoCirculo = document.getElementById('fotoCirculo');
    const fotoPreview = document.getElementById('fotoPreview');
    const fotoIcone = document.getElementById('fotoIcone');

    const ESPECIALIDADES = [
        'Cardiologia', 'Dermatologia', 'Pediatria', 'Ortopedia',
        'Ginecologia', 'Oftalmologia', 'Neurologia', 'Psiquiatria',
        'Endocrinologia', 'Urologia', 'Otorrinolaringologia',
        'Gastroenterologia', 'Clínico Geral', 'Oncologia',
        'Reumatologia', 'Infectologia'
    ];

    let especialidadesSelecionadas = [];

    if (fotoCirculo) fotoCirculo.addEventListener('click', () => fotoInput.click());

    if (fotoInput) {
        fotoInput.addEventListener('change', function () {
            const file = this.files[0];
            if (!file) return;
            if (file.size > 5 * 1024 * 1024) {
                mostrarToast('Foto maior que 5 MB. Escolha outra.', 'erro');
                return;
            }
            const reader = new FileReader();
            reader.onload = e => {
                fotoPreview.src = e.target.result;
                fotoPreview.style.display = 'block';
                fotoIcone.style.display = 'none';
            };
            reader.readAsDataURL(file);
        });
    }

    // MÁSCARAS
    if (cpfInput) {
        cpfInput.addEventListener('input', function () {
            let v = this.value.replace(/\D/g, '').slice(0, 11);
            v = v.replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
            this.value = v;
        });
    }

    if (cepInput) {
        cepInput.addEventListener('input', function () {
            let v = this.value.replace(/\D/g, '').slice(0, 8);
            if (v.length > 5) v = v.slice(0, 5) + '-' + v.slice(5);
            this.value = v;
        });
    }

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
    }

    // CHIPS
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
            if (especialidadesSelecionadas.length >= 3) {
                mostrarToast('Máximo de 3 especialidades permitidas.', 'erro');
                return;
            }
            chip.classList.add('ativo');
            especialidadesSelecionadas.push(nome);
        }
        const hiddenInput = document.getElementById('formCadastro:especialidadesHidden');
        if (hiddenInput) hiddenInput.value = especialidadesSelecionadas.join(',');
        atualizarChips();
        if (especialidadesSelecionadas.length > 0) ocultarErro('especialidade');
    }

    function atualizarChips() {
        const max = especialidadesSelecionadas.length >= 3;
        document.querySelectorAll('.chip').forEach(c => {
            if (!c.classList.contains('ativo')) c.classList.toggle('bloqueado', max);
        });
        const contador = document.getElementById('chipsContador');
        if (contador) contador.textContent = `${especialidadesSelecionadas.length}/3 selecionadas`;
    }

    // VALIDAÇÃO
    function exibirErro(sufixo, msg) {
        const span = document.getElementById(`err-${sufixo}`);
        if (span) { if (msg) span.textContent = msg; span.classList.add('visivel'); }
    }
    function ocultarErro(sufixo) {
        const span = document.getElementById(`err-${sufixo}`);
        if (span) span.classList.remove('visivel');
    }
    function marcarInputErro(inputId, temErro) {
        const el = document.getElementById(inputId);
        if (el) el.classList.toggle('input-invalido', temErro);
    }

    function validarFormulario() {
        let valido = true;

        const nomeEl = document.getElementById('formCadastro:nomeMedico');
        const nome = nomeEl ? nomeEl.value.trim() : '';
        marcarInputErro('formCadastro:nomeMedico', !nome);
        if (!nome) { exibirErro('nome'); valido = false; } else ocultarErro('nome');

        const cpf = cpfInput ? cpfInput.value.replace(/\D/g, '') : '';
        marcarInputErro('formCadastro:cpfMedico', cpf.length !== 11);
        if (cpf.length !== 11) { exibirErro('cpf'); valido = false; } else ocultarErro('cpf');

        const telefone = telefoneInput ? telefoneInput.value.replace(/\D/g, '') : '';
        marcarInputErro('formCadastro:telefoneMedico', telefone.length < 10);
        if (telefone.length < 10) { exibirErro('telefone'); valido = false; } else ocultarErro('telefone');

        const nascEl = document.getElementById('formCadastro:dataNascimento');
        const nasc = nascEl ? nascEl.value : '';
        marcarInputErro('formCadastro:dataNascimento', !nasc);
        if (!nasc) { exibirErro('nasc'); valido = false; } else ocultarErro('nasc');

        const crmEl = document.getElementById('formCadastro:crmMedico');
        const crm = crmEl ? crmEl.value.trim() : '';
        marcarInputErro('formCadastro:crmMedico', !crm);
        if (!crm) { exibirErro('crm'); valido = false; } else ocultarErro('crm');

        const ufEl = document.getElementById('formCadastro:ufCrm');
        const uf = ufEl ? ufEl.value : '';
        marcarInputErro('formCadastro:ufCrm', !uf);
        if (!uf) { exibirErro('uf'); valido = false; } else ocultarErro('uf');

        const cep = cepInput ? cepInput.value.replace(/\D/g, '') : '';
        marcarInputErro('formCadastro:cepMedico', cep.length !== 8);
        if (cep.length !== 8) { exibirErro('cep'); valido = false; } else ocultarErro('cep');

        const valorEl = document.getElementById('formCadastro:valorConsulta');
        const valor = valorEl ? parseFloat(valorEl.value) : 0;
        marcarInputErro('formCadastro:valorConsulta', isNaN(valor) || valor <= 0);
        if (isNaN(valor) || valor <= 0) { exibirErro('valor'); valido = false; } else ocultarErro('valor');

        const tempoEl = document.getElementById('formCadastro:tempoConsulta');
        const tempo = tempoEl ? parseInt(tempoEl.value) : 0;
        marcarInputErro('formCadastro:tempoConsulta', isNaN(tempo) || tempo < 5);
        if (isNaN(tempo) || tempo < 5) { exibirErro('tempo'); valido = false; } else ocultarErro('tempo');

        if (especialidadesSelecionadas.length === 0) {
            exibirErro('especialidade'); valido = false;
        } else ocultarErro('especialidade');

		const checkboxes = document.querySelectorAll('#formCadastro input[type="checkbox"]');
		const diasMarcados = Array.from(checkboxes).some(el => el.checked);

        if (!diasMarcados) {
            exibirErro('disp', 'Selecione ao menos um dia de disponibilidade.');
            valido = false;
        } else ocultarErro('disp');

        const emailEl = document.getElementById('formCadastro:emailMedico');
        const email = emailEl ? emailEl.value.trim() : '';
        const emailValido = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
        marcarInputErro('formCadastro:emailMedico', !emailValido);
        if (!emailValido) { exibirErro('email'); valido = false; } else ocultarErro('email');

        const senhaEl = document.getElementById('formCadastro:senhaMedico');
        const senha = senhaEl ? senhaEl.value : '';
        marcarInputErro('formCadastro:senhaMedico', senha.length < 6);
        if (senha.length < 6) { exibirErro('senha'); valido = false; } else ocultarErro('senha');

        return valido;
    }

    // SUBMIT
    if (form) {
        form.addEventListener('submit', function (e) {
            if (!validarFormulario()) {
                e.preventDefault();
                mostrarToast('Por favor, corrija os erros no formulário.', 'erro');
            }
        });
    }

    // TOAST
    function mostrarToast(mensagem, tipo = 'sucesso') {
        const toast = document.getElementById('toast');
        if (!toast) return;
        toast.textContent = mensagem;
        toast.className = `toast ${tipo} visivel`;
        setTimeout(() => toast.classList.remove('visivel'), 3000);
    }

    // INIT
    inicializarChips();

}); // fim do DOMContentLoaded