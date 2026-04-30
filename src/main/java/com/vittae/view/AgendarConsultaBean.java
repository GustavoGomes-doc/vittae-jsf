package com.vittae.view;

<<<<<<< Updated upstream
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.service.AgendarConsultaService;
import com.vittae.service.AgendarConsultaService.AgendamentoDTO;
import com.vittae.service.AgendarConsultaService.PacienteDTO;

@Named("agendamentoBean")
@ViewScoped
public class AgendarConsultaBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private AgendarConsultaService service;

	// ── Step 1: dados da consulta ──────────────────────────────────────────
	private String especialidade;
	private String tipoConsulta;
	private String dataConsulta; // vem como String do input date
	private String horaConsulta; // vem como String do input time

	// ── Step 2: médico selecionado ─────────────────────────────────────────
	private Long medicoId;
	private List<MedicoUI> listaMedicos = new ArrayList<>();

	// ── Step 3: dados do paciente ──────────────────────────────────────────
	private String pacienteNome;
	private String pacienteCpf;
	private String pacienteTelefone;
	private String pacienteNascimento;
	private String pacienteSexo;
	private String observacoes;

	@PostConstruct
	public void init() {
		this.service = new AgendarConsultaService();
		carregarMedicos();
	}

	/**
	 * Busca médicos do Spring Boot e popula a lista pra o step 2.
	 */
	private void carregarMedicos() {
		try {
			String json = service.buscarMedicosJson();
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

			// Desserializa o JSON em lista de MedicoUI
			List<MedicoUI> medicos = mapper.readValue(json, new TypeReference<List<MedicoUI>>() {
			});
			this.listaMedicos = medicos;

		} catch (Exception e) {
			System.err.println("Erro ao carregar médicos: " + e.getMessage());
			this.listaMedicos = new ArrayList<>();
		}
	}

	/**
	 * Chamado pelo botão de submit do JSF no step 3.
	 */
	public String salvarAgendamento() {
		try {
			AgendamentoDTO dto = new AgendamentoDTO();

			dto.setEspecialidade(especialidade);
			dto.setTipoConsulta(tipoConsulta);
			dto.setMedicoId(medicoId);
			dto.setObservacoes(observacoes);

			// Converte String da tela pra LocalDate/LocalTime
			if (dataConsulta != null && !dataConsulta.isEmpty()) {
				dto.setDataConsulta(LocalDate.parse(dataConsulta));
				dto.setDataAgendado(LocalDate.now());
			}
			if (horaConsulta != null && !horaConsulta.isEmpty()) {
				dto.setHora(LocalTime.parse(horaConsulta));
			}

			// Monta o paciente
			PacienteDTO paciente = new PacienteDTO();
			paciente.setNome(pacienteNome);
			paciente.setCpf(pacienteCpf != null ? pacienteCpf.replaceAll("\\D", "") : "");
			paciente.setTelefone(pacienteTelefone);
			dto.setPaciente(paciente);

			service.salvarAgendamento(dto);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Consulta agendada com sucesso."));

			return "/views/pacientes/agendarConsulta?faces-redirect=true";

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao agendar: " + e.getMessage()));
			return null;
		}
	}

	// ── Classe auxiliar pra representar médico na lista do step 2 ──────────
	public static class MedicoUI {
		private Long id;
		private String nome;
		private String especialidade;
		private String localizacao;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public String getEspecialidade() {
			return especialidade;
		}

		public void setEspecialidade(String especialidade) {
			this.especialidade = especialidade;
		}

		public String getLocalizacao() {
			return localizacao;
		}

		public void setLocalizacao(String localizacao) {
			this.localizacao = localizacao;
		}
	}

	// ── Getters e Setters ──────────────────────────────────────────────────

	public String getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(String especialidade) {
		this.especialidade = especialidade;
	}

	public String getTipoConsulta() {
		return tipoConsulta;
	}

	public void setTipoConsulta(String tipoConsulta) {
		this.tipoConsulta = tipoConsulta;
	}

	public String getDataConsulta() {
		return dataConsulta;
	}

	public void setDataConsulta(String dataConsulta) {
		this.dataConsulta = dataConsulta;
	}

	public String getHoraConsulta() {
		return horaConsulta;
	}

	public void setHoraConsulta(String horaConsulta) {
		this.horaConsulta = horaConsulta;
	}

	public Long getMedicoId() {
		return medicoId;
	}

	public void setMedicoId(Long medicoId) {
		this.medicoId = medicoId;
	}

	public List<MedicoUI> getListaMedicos() {
		return listaMedicos;
	}

	public void setListaMedicos(List<MedicoUI> listaMedicos) {
		this.listaMedicos = listaMedicos;
	}

	public String getPacienteNome() {
		return pacienteNome;
	}

	public void setPacienteNome(String pacienteNome) {
		this.pacienteNome = pacienteNome;
	}

	public String getPacienteCpf() {
		return pacienteCpf;
	}

	public void setPacienteCpf(String pacienteCpf) {
		this.pacienteCpf = pacienteCpf;
	}

	public String getPacienteTelefone() {
		return pacienteTelefone;
	}

	public void setPacienteTelefone(String pacienteTelefone) {
		this.pacienteTelefone = pacienteTelefone;
	}

	public String getPacienteNascimento() {
		return pacienteNascimento;
	}

	public void setPacienteNascimento(String pacienteNascimento) {
		this.pacienteNascimento = pacienteNascimento;
	}

	public String getPacienteSexo() {
		return pacienteSexo;
	}

	public void setPacienteSexo(String pacienteSexo) {
		this.pacienteSexo = pacienteSexo;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}
=======
import com.vittae.model.Consulta;
import com.vittae.model.Medico;
import com.vittae.model.Paciente;
// ... imports do seu service e do spring/jsf ...
import java.util.List;

// Adicione as anotações do seu framework (Spring ou JSF puro)
// @Component ou @Named
// @Scope("view") ou @ViewScoped
public class AgendarConsultaBean {

    private Consulta consultaNova = new Consulta();
    
    
    private String especialidades;
    private List<Medico> listaMedicos;
    
   
    private int passoAtual = 1;

    

    public void buscarMedicos() {
        // Aqui você vai no Service e preenche a listaMedicos
        // listaMedicos = medicoService.buscar(especialidadeBusca);
    }

    public void selecionarMedico(Medico medicoEscolhido) {
        // Guarda o médico dentro da nossa consultaNova
        this.consultaNova.setMedico(medicoEscolhido);
        proximoPasso();
    }
    
    public void finalizarAgendamento() {
        // Aqui você manda a 'consultaNova' para o Service salvar no banco
    }

    public void proximoPasso() {
        if (passoAtual < 3) passoAtual++;
    }

    public void voltarPasso() {
        if (passoAtual > 1) passoAtual--;
    }

	public List<Medico> getListaMedicos() {
		return listaMedicos;
	}

	public void setListaMedicos(List<Medico> listaMedicos) {
		this.listaMedicos = listaMedicos;
	}

	public String getEspecialidades() {
		return especialidades;
	}

	public void setEspecialidades(String especialidades) {
		this.especialidades = especialidades;
	}

    // --- GETTERS E SETTERS ---
    // Crie os getters e setters apenas destas variáveis acima 
    // (consultaNova, especialidadeBusca, listaMedicos, passoAtual)
>>>>>>> Stashed changes
}