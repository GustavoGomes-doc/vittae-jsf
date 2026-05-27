document.addEventListener('DOMContentLoaded', () => {
    const navItems = document.querySelectorAll('.nav-item');
    const sections = document.querySelectorAll('.content-section');
	
	// Adiciona no Admin.js, dentro do DOMContentLoaded
	document.querySelectorAll('.nav-item').forEach(link => {
	    if (link.href && window.location.href.includes(link.getAttribute('href'))) {
	        link.classList.add('active');
	    }
	});


    document.querySelectorAll('.btn-main-small, .btn-main').forEach(btn => {
        btn.addEventListener('click', function () {
            const originalText = this.innerText;
            if (originalText === "Atender") {
                this.innerText = "Iniciando...";
                this.style.opacity = "0.7";
                setTimeout(() => {
                    alert("Abrindo Prontuário Eletrônico...");
                    this.innerText = originalText;
                    this.style.opacity = "1";
                }, 800);
            }
        });
    });
});