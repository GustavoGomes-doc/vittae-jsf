package com.vittae.view;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

	private String especialidade;
	private String tipoConsulta;
	private String dataConsulta;
	private String horaConsulta;
	private Long medicoId;
	private List<MedicoUI> listaMedicos = new ArrayList<>();
	private String pacienteNome;
	private String pacienteCpf;
	private String pacienteTelefone;
	private String pacienteNascimento;
	private String pacienteGenero;
	private String observacoes;

	@PostConstruct
	public void init() {
		this.service = new AgendarConsultaService();
		carregarMedicos();
	}

	private void carregarMedicos() {
		try {
			String json = service.buscarMedicosJson();
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			this.listaMedicos = mapper.readValue(json, new TypeReference<List<MedicoUI>>() {
			});
		} catch (Exception e) {
			System.err.println("Erro ao carregar médicos: " + e.getMessage());
			this.listaMedicos = new ArrayList<>();
		}
	}

	public String salvarAgendamento() {
		try {
			AgendamentoDTO dto = new AgendamentoDTO();
			dto.setEspecialidade(especialidade);
			dto.setTipoConsulta(tipoConsulta);
			dto.setMedicoId(medicoId);
			dto.setObservacoes(observacoes);

			if (dataConsulta != null && !dataConsulta.isEmpty()) {
				dto.setDataConsulta(LocalDate.parse(dataConsulta));
				dto.setDataAgendado(LocalDate.now());
			}
			if (horaConsulta != null && !horaConsulta.isEmpty()) {
				dto.setHora(LocalTime.parse(horaConsulta));
			}

			PacienteDTO paciente = new PacienteDTO();
			paciente.setNome(pacienteNome);
			paciente.setCpf(pacienteCpf != null ? pacienteCpf.replaceAll("\\D", "") : "");
			paciente.setTelefone(pacienteTelefone);
			dto.setPaciente(paciente);
			paciente.setGenero(pacienteGenero); // ← adicionar
			paciente.setNascimento(pacienteNascimento);

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

	// ── MedicoUI ──
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class MedicoUI {
		private Long id;
		private String nome;
		private String crm;
		private String telefone;
		private List<String> especialidades;
		private Integer tempoConsultaMinutos;
		private Double valorConsulta;
		private String foto;

		public String getIniciais() {
			if (nome == null)
				return "MD";
			String[] p = nome.trim().split("\\s+");
			return p.length >= 2 ? ("" + p[0].charAt(0) + p[p.length - 1].charAt(0)).toUpperCase()
					: nome.substring(0, 2).toUpperCase();
		}

		public String getCorAvatar() {
			String[] cores = { "#7c3aed", "#059669", "#dc2626", "#d97706", "#2563eb", "#db2777" };
			return cores[Math.abs(nome.hashCode()) % cores.length];
		}

		public Integer getTempoConsultaMinutos() {
			return tempoConsultaMinutos;
		}

		public void setTempoConsultaMinutos(Integer tempoConsultaMinutos) {
			this.tempoConsultaMinutos = tempoConsultaMinutos;
		}

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

		public String getCrm() {
			return crm;
		}

		public void setCrm(String crm) {
			this.crm = crm;
		}

		public String getTelefone() {
			return telefone;
		}

		public void setTelefone(String telefone) {
			this.telefone = telefone;
		}

		public List<String> getEspecialidades() {
			return especialidades;
		}

		public void setEspecialidades(List<String> especialidades) {
			this.especialidades = especialidades;
		}

		public Double getValorConsulta() {
			return valorConsulta;
		}

		public void setValorConsulta(Double valorConsulta) {
			this.valorConsulta = valorConsulta;
		}

		public String getFoto() {
			return foto;
		}

		public void setFoto(String foto) {
			this.foto = foto;
		}
	}

	// ── Getters e Setters do Bean ──
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

	public String getPacienteGenero() {
		return pacienteGenero;
	}

	public void setPacienteGenero(String pacienteGenero) {
		this.pacienteGenero = pacienteGenero;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}
}