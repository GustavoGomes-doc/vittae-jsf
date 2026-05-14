const hoje = new Date();
            const opcoes = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
            document.getElementById('dataHoje').textContent =
                hoje.toLocaleDateString('pt-BR', opcoes);

document.addEventListener('DOMContentLoaded', () => {
	const hoje = new Date();
	            const opcoes = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
	            document.getElementById('dataHoje').textContent =
	                hoje.toLocaleDateString('pt-BR', opcoes);
});