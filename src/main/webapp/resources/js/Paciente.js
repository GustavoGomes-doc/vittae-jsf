(function () {
    "use strict";

    let currentStep = 1;
    let selectedDoctorId = null;

    function init() {
        setupEventListeners();
        setMinDate();
        updateProgressBar();
    }

    function setupEventListeners() {
        // Usa getElementById buscando pelas IDs geradas pelo JSF (incluindo o prefixo do form)
        const prevBtn = document.getElementById("prevBtn");
        const nextBtn = document.getElementById("nextBtn");
        const submitBtnVisual = document.getElementById("submitBtnVisual");
        const searchInput = document.getElementById("searchInput");

        if (prevBtn) prevBtn.addEventListener("click", previousStep);
        if (nextBtn) nextBtn.addEventListener("click", nextStep);
        if (submitBtnVisual) submitBtnVisual.addEventListener("click", validateAndSubmit);
        if (searchInput) searchInput.addEventListener("input", searchDoctors);

        // MÁSCARAS
        document.querySelectorAll('.mask-cpf').forEach(el => {
            el.addEventListener("input", function (e) {
                let value = e.target.value.replace(/\D/g, "");
                if (value.length <= 11) {
                    value = value.replace(/(\d{3})(\d)/, "$1.$2")
                         .replace(/(\d{3})(\d)/, "$1.$2")
                         .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
                    e.target.value = value;
                }
            });
        });

        document.querySelectorAll('.mask-phone').forEach(el => {
            el.addEventListener("input", function (e) {
                let value = e.target.value.replace(/\D/g, "");
                if (value.length <= 11) {
                    value = value.replace(/(\d{2})(\d)/, "($1) $2")
                         .replace(/(\d{5})(\d)/, "$1-$2");
                    e.target.value = value;
                }
            });
        });

        document.querySelectorAll('.mask-date').forEach(el => {
            el.addEventListener("input", function (e) {
                let value = e.target.value.replace(/\D/g, "");
                if (value.length <= 8) {
                    value = value.replace(/(\d{2})(\d)/, "$1/$2")
                         .replace(/(\d{2})(\d)/, "$1/$2");
                    e.target.value = value;
                }
            });
        });
    }

    function setMinDate() {
        // Pega o ID gerado pelo h:form (ex: formAgendamento:consultaDate)
        const dateInput = document.getElementById("formAgendamento:consultaDate");
        if (dateInput) {
            const today = new Date().toISOString().split("T")[0];
            dateInput.setAttribute("min", today);
        }
    }

    // FUNÇÃO GLOBAL CHAMADA PELO XHTML AO CLICAR NO CARD DO MÉDICO
    window.selecionarMedico = function(cardElement, medicoId) {
        selectedDoctorId = medicoId;
        
        // Coloca o ID do médico no campo hidden do JSF para ir pro Java
        document.getElementById("formAgendamento:medicoSelecionadoId").value = medicoId;

        // Estilização visual (adiciona classe selected)
        const allCards = document.querySelectorAll(".doctor-card");
        allCards.forEach(card => card.classList.remove("selected"));
        cardElement.classList.add("selected");
    };

    function searchDoctors() {
        const searchInput = document.getElementById("searchInput");
        if (!searchInput) return;

        const query = searchInput.value.toLowerCase().trim();
        const allCards = document.querySelectorAll(".doctor-card");

        allCards.forEach(card => {
            const nomeMedico = card.getAttribute("data-nome").toLowerCase();
            if (nomeMedico.includes(query)) {
                card.style.display = "flex";
            } else {
                card.style.display = "none";
            }
        });
    }

    // NAVEGAÇÃO ENTRE OS PASSOS
    function updateProgressBar() {
        const progress = ((currentStep - 1) / 2) * 100;
        const progressBar = document.getElementById("progressBar");
        if (progressBar) progressBar.style.width = progress + "%";

        for (let i = 1; i <= 3; i++) {
            const circle = document.getElementById("circle" + i);
            if (!circle) continue;

            if (i < currentStep) {
                circle.classList.add("completed");
                circle.classList.remove("active");
                circle.innerHTML = "✓";
            } else if (i === currentStep) {
                circle.classList.add("active");
                circle.classList.remove("completed");
                circle.innerHTML = "<span>" + i + "</span>";
            } else {
                circle.classList.remove("active", "completed");
                circle.innerHTML = "<span>" + i + "</span>";
            }
        }
    }

    function showStep(step) {
        document.querySelectorAll(".form-step").forEach(el => el.classList.remove("active"));
        const currentStepEl = document.getElementById("step" + step);
        if (currentStepEl) currentStepEl.classList.add("active");

        const prevBtn = document.getElementById("prevBtn");
        const nextBtn = document.getElementById("nextBtn");
        const submitBtnVisual = document.getElementById("submitBtnVisual");

        if (prevBtn) prevBtn.disabled = step === 1;

        if (step === 3) {
            if (nextBtn) nextBtn.style.display = "none";
            if (submitBtnVisual) submitBtnVisual.style.display = "inline-block";
        } else {
            if (nextBtn) nextBtn.style.display = "inline-block";
            if (submitBtnVisual) submitBtnVisual.style.display = "none";
        }

        updateProgressBar();
    }

    function validateCurrentStep() {
		if (currentStep === 1) {
		    const espEl = document.getElementById("formAgendamento:especialidadeType");
		    const typeEl = document.getElementById("formAgendamento:consultaType");
		    const dateEl = document.getElementById("formAgendamento:consultaDate");
		    const timeEl = document.getElementById("formAgendamento:consultaTime");

		    if (!espEl.value || !typeEl.value || !dateEl.value || !timeEl.value) {
		        alert("Preencha todos os campos da consulta!");
		        return false;
		    }
		    return true;
		}

        if (currentStep === 2) {
            if (!selectedDoctorId) {
                alert("Selecione um médico!");
                return false;
            }
            return true;
        }

        if (currentStep === 3) {
            const cpfEl = document.getElementById("formAgendamento:patientCPF");
            if (cpfEl.value.replace(/\D/g, "").length !== 11) {
                alert("CPF inválido!");
                return false;
            }
            return true;
        }
        return true;
    }

    function nextStep() {
        if (validateCurrentStep() && currentStep < 3) {
            currentStep++;
            showStep(currentStep);
        }
    }

    function previousStep() {
        if (currentStep > 1) {
            currentStep--;
            showStep(currentStep);
        }
    }

    function validateAndSubmit() {
        if (validateCurrentStep()) {
            const submitBtnVisual = document.getElementById("submitBtnVisual");
            submitBtnVisual.innerText = "Enviando...";
            submitBtnVisual.disabled = true;

            // A MÁGICA: Clica no botão invisível do JSF para ele ir para o Java
            document.getElementById("formAgendamento:btnSubmeterJSF").click();
        }
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();