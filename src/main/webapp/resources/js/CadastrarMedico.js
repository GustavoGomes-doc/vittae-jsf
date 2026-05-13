function toggleHorario(checkbox, divId) {
    const div = document.getElementById(divId);
    if (div) div.style.display = checkbox.checked ? 'flex' : 'none';
}

document.addEventListener('DOMContentLoaded', () => {

    const form          = document.getElementById('formCadastro');
    const cpfInput      = document.getElementById('formCadastro:cpfMedico');
    const cepInput      = document.getElementById('formCadastro:cepMedico');
    const fotoInput     = document.getElementById('formCadastro:fotoMedico');
    const telefoneInput = document.getElementById('formCadastro:telefoneMedico');
    const fotoCirculo   = document.getElementById('fotoCirculo');
    const fotoPreview   = document.getElementById('fotoPreview');
    const fotoIcone     = document.getElementById('fotoIcone');

    const ESPECIALIDADES = [
        'Cardiologia','Dermatologia','Pediatria','Ortopedia',
        'Ginecologia','Oftalmologia','Neurologia','Psiquiatria',
        'Endocrinologia','Urologia','Otorrinolaringologia',
        'Gastroenterologia','Clínico Geral','Oncologia',
        'Reumatologia','Infectologia'
    ];

    let especialidadesSelecionadas = [];

    // ── FOTO ─────────────────────────────────────────────────

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

    // ── MÁSCARAS ─────────────────────────────────────────────

    if (cpfInput) {
        cpfInput.addEventListener('input', function () {
            let v = this.value.replace(/\D/g, '').slice(0, 11);
            v = v.replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d)/, '$1.$2')
                 .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
            this.value = v;
        });
        cpfInput.addEventListener('blur', function () {
            const cpf = this.value.replace(/\D/g, '');
            const ok = cpf.length === 11;
            marcarErro('formCadastro:cpfMedico', 'cpf', !ok, 'CPF incompleto');
        });
    }

    if (cepInput) {
        cepInput.addEventListener('input', function () {
            let v = this.value.replace(/\D/g, '').slice(0, 8);
            if (v.length > 5) v = v.slice(0, 5) + '-' + v.slice(5);
            this.value = v;
        });
        cepInput.addEventListener('blur', function () {
            const cep = this.value.replace(/\D/g, '');
            marcarErro('formCadastro:cepMedico', 'cep', cep.length !== 8, 'CEP inválido');
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
        telefoneInput.addEventListener('blur', function () {
            const tel = this.value.replace(/\D/g, '');
            marcarErro('formCadastro:telefoneMedico', 'telefone', tel.length < 10, 'Telefone inválido');
        });
    }

    // Nome
    const nomeInput = document.getElementById('formCadastro:nomeMedico');
    if (nomeInput) {
        nomeInput.addEventListener('blur', function () {
            const ok = this.value.trim().length > 0;
            marcarErro('formCadastro:nomeMedico', 'nome', !ok, 'Nome é obrigatório');
        });
    }

    // CRM: só números, máx 6
    const crmInput = document.getElementById('formCadastro:crmMedico');
    if (crmInput) {
        crmInput.setAttribute('maxlength', '6');
        crmInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 6);
        });
        crmInput.addEventListener('blur', function () {
            const ok = /^\d{1,6}$/.test(this.value.trim());
            marcarErro('formCadastro:crmMedico', 'crm', !ok, 'CRM deve ter até 6 dígitos numéricos');
        });
    }

    // UF
    const ufEl = document.getElementById('formCadastro:ufCrm');
    if (ufEl) {
        ufEl.addEventListener('change', function () {
            marcarErro('formCadastro:ufCrm', 'uf', !this.value, 'Selecione a UF do CRM');
        });
    }

    // Valor da consulta: máscara 0,00
	const valorInput = document.getElementById('formCadastro:valorConsulta');
	if (valorInput) {
	    valorInput.addEventListener('input', function (e) {
	        let value = e.target.value.replace(/\D/g, "");
	        value = (value / 100).toFixed(2) + "";
	        value = value.replace(".", ",");
	        value = value.replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1.");
	        e.target.value = value;
	    });
	}

    // Duração: só inteiros, máx 3 dígitos
    const duracaoInput = document.getElementById('formCadastro:tempoConsulta');
    if (duracaoInput) {
        duracaoInput.setAttribute('maxlength', '3');
        duracaoInput.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 3);
        });
        duracaoInput.addEventListener('blur', function () {
            const t = parseInt(this.value);
            marcarErro('formCadastro:tempoConsulta', 'tempo',
                isNaN(t) || t < 5 || t > 999, 'Duração deve ser entre 5 e 999 minutos');
        });
    }

    // Data de nascimento: máscara DD/MM/AAAA + validação 24 anos
	const dataNascInput = document.getElementById('formCadastro:dataNascimento');
	if (dataNascInput) {
	    dataNascInput.addEventListener('blur', function () {
	        const partes = this.value.split('/');
	        let erro = true;
	        if (partes.length === 3 && partes[2].length === 4) {
	            const dataNasc = new Date(partes[2], partes[1] - 1, partes[0]);
	            const hoje = new Date();
	            
	            // Cálculo preciso de idade
	            let idade = hoje.getFullYear() - dataNasc.getFullYear();
	            const m = hoje.getMonth() - dataNasc.getMonth();
	            if (m < 0 || (m === 0 && hoje.getDate() < dataNasc.getDate())) {
	                idade--;
	            }
	            
	            erro = isNaN(dataNasc.getTime()) || idade < 24;
	        }
	        marcarErro('formCadastro:dataNascimento', 'nasc', erro, 'O médico deve ter no mínimo 24 anos');
	    });
	}

        dataNascInput.addEventListener('blur', function () {
            const partes = this.value.split('/');
            let erro = true;
            if (partes.length === 3 && partes[2].length === 4) {
                const nascDate = new Date(+partes[2], +partes[1] - 1, +partes[0]);
                const hoje = new Date();
                const idade = hoje.getFullYear() - nascDate.getFullYear()
                    - (hoje < new Date(hoje.getFullYear(), nascDate.getMonth(), nascDate.getDate()) ? 1 : 0);
                erro = isNaN(nascDate.getTime()) || idade < 24;
            }
            marcarErro('formCadastro:dataNascimento', 'nasc', erro, 'O médico deve ter no mínimo 24 anos');
        });
    

    // Email
    const emailInput = document.getElementById('formCadastro:emailMedico');
    if (emailInput) {
        emailInput.addEventListener('blur', function () {
            const ok = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.value.trim());
            marcarErro('formCadastro:emailMedico', 'email', !ok, 'E-mail inválido');
        });
    }

    // Senha
    const senhaInput = document.getElementById('formCadastro:senhaMedico');
    if (senhaInput) {
        senhaInput.addEventListener('blur', function () {
            marcarErro('formCadastro:senhaMedico', 'senha',
                this.value.length < 6, 'Mínimo 6 caracteres');
        });
    }

    // ── CHIPS ─────────────────────────────────────────────────

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

    // ── HELPERS DE ERRO ───────────────────────────────────────

    function marcarErro(inputId, sufixo, temErro, msg) {
        const el = document.getElementById(inputId);
        if (el) el.classList.toggle('input-invalido', temErro);
        temErro ? exibirErro(sufixo, msg) : ocultarErro(sufixo);
    }

    function exibirErro(sufixo, msg) {
        const span = document.getElementById(`err-${sufixo}`);
        if (!span) return;
        if (msg) span.innerHTML = `<i class="fas fa-exclamation-circle"></i> ${msg}`;
        span.classList.add('visivel');
    }

    function ocultarErro(sufixo) {
        const span = document.getElementById(`err-${sufixo}`);
        if (span) span.classList.remove('visivel');
    }

    // ── VALIDAÇÃO COMPLETA NO SUBMIT ──────────────────────────

    function validarTudo() {
        let valido = true;

        const nome = nomeInput ? nomeInput.value.trim() : '';
        if (!nome) { marcarErro('formCadastro:nomeMedico', 'nome', true, 'Nome é obrigatório'); valido = false; }
        else marcarErro('formCadastro:nomeMedico', 'nome', false);

        const cpf = cpfInput ? cpfInput.value.replace(/\D/g, '') : '';
        if (cpf.length !== 11) { marcarErro('formCadastro:cpfMedico', 'cpf', true, 'CPF incompleto'); valido = false; }
        else marcarErro('formCadastro:cpfMedico', 'cpf', false);

        const tel = telefoneInput ? telefoneInput.value.replace(/\D/g, '') : '';
        if (tel.length < 10) { marcarErro('formCadastro:telefoneMedico', 'telefone', true, 'Telefone inválido'); valido = false; }
        else marcarErro('formCadastro:telefoneMedico', 'telefone', false);

        // Data + 24 anos
        const nascVal = dataNascInput ? dataNascInput.value : '';
        const partes = nascVal.split('/');
        let nascOk = false;
        if (partes.length === 3 && partes[2].length === 4) {
            const nascDate = new Date(+partes[2], +partes[1] - 1, +partes[0]);
            const hoje = new Date();
            const idade = hoje.getFullYear() - nascDate.getFullYear()
                - (hoje < new Date(hoje.getFullYear(), nascDate.getMonth(), nascDate.getDate()) ? 1 : 0);
            nascOk = !isNaN(nascDate.getTime()) && idade >= 24;
        }
        if (!nascOk) { marcarErro('formCadastro:dataNascimento', 'nasc', true, 'O médico deve ter no mínimo 24 anos'); valido = false; }
        else marcarErro('formCadastro:dataNascimento', 'nasc', false);

        const crm = crmInput ? crmInput.value.trim() : '';
        if (!/^\d{1,6}$/.test(crm)) { marcarErro('formCadastro:crmMedico', 'crm', true, 'CRM deve ter até 6 dígitos numéricos'); valido = false; }
        else marcarErro('formCadastro:crmMedico', 'crm', false);

        const uf = ufEl ? ufEl.value : '';
        if (!uf) { marcarErro('formCadastro:ufCrm', 'uf', true, 'Selecione a UF do CRM'); valido = false; }
        else marcarErro('formCadastro:ufCrm', 'uf', false);

        const cep = cepInput ? cepInput.value.replace(/\D/g, '') : '';
        if (cep.length !== 8) { marcarErro('formCadastro:cepMedico', 'cep', true, 'CEP inválido'); valido = false; }
        else marcarErro('formCadastro:cepMedico', 'cep', false);

        const valorRaw = valorInput ? valorInput.value.replace(',', '.') : '';
        const valor = parseFloat(valorRaw);
        if (isNaN(valor) || valor <= 0) { marcarErro('formCadastro:valorConsulta', 'valor', true, 'Informe um valor maior que zero'); valido = false; }
        else marcarErro('formCadastro:valorConsulta', 'valor', false);

        const tempo = duracaoInput ? parseInt(duracaoInput.value) : 0;
        if (isNaN(tempo) || tempo < 5 || tempo > 999) { marcarErro('formCadastro:tempoConsulta', 'tempo', true, 'Duração deve ser entre 5 e 999 minutos'); valido = false; }
        else marcarErro('formCadastro:tempoConsulta', 'tempo', false);

        if (especialidadesSelecionadas.length === 0) {
            exibirErro('especialidade', 'Selecione ao menos uma especialidade'); valido = false;
        } else ocultarErro('especialidade');

        const diasMarcados = Array.from(
            document.querySelectorAll('#formCadastro input[type="checkbox"]')
        ).some(el => el.checked);
        if (!diasMarcados) { exibirErro('disp', 'Selecione ao menos um dia de disponibilidade'); valido = false; }
        else ocultarErro('disp');

        const email = emailInput ? emailInput.value.trim() : '';
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { marcarErro('formCadastro:emailMedico', 'email', true, 'E-mail inválido'); valido = false; }
        else marcarErro('formCadastro:emailMedico', 'email', false);

        const senha = senhaInput ? senhaInput.value : '';
        if (senha.length < 6) { marcarErro('formCadastro:senhaMedico', 'senha', true, 'Mínimo 6 caracteres'); valido = false; }
        else marcarErro('formCadastro:senhaMedico', 'senha', false);

        return valido;
    }

    // ── SUBMIT ────────────────────────────────────────────────

    if (form) {
        form.addEventListener('submit', function (e) {
            // Converte vírgula → ponto antes de enviar
            if (valorInput) valorInput.value = valorInput.value.replace(',', '.');

            if (!validarTudo()) {
                e.preventDefault();
                mostrarToast('Por favor, corrija os erros no formulário.', 'erro');
                // Rola até o primeiro erro
                const primeiro = document.querySelector('.input-invalido');
                if (primeiro) primeiro.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }

    // ── TOAST ─────────────────────────────────────────────────

    function mostrarToast(mensagem, tipo = 'sucesso') {
        const toast = document.getElementById('toast');
        if (!toast) return;
        toast.textContent = mensagem;
        toast.className = `toast ${tipo} visivel`;
        setTimeout(() => toast.classList.remove('visivel'), 3000);
    }

    // ── INIT ──────────────────────────────────────────────────

    inicializarChips();

}); // fim do DOMContentLoaded