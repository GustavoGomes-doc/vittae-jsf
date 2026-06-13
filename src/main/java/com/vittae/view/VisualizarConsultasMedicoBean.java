package com.vittae.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.model.enums.Status;
import com.vittae.service.ConsultaService;
import com.vittae.util.ConfigUtil;

@Named("visualizarConsultasMedicoBean")
@ViewScoped
public class VisualizarConsultasMedicoBean implements Serializable {

	private List<MedicoConsultaDTO> consultas;
	private String filtroTexto;
	private String filtroStatus;
	private boolean exibirModalCancelamento = false;
	private Long idParaCancelar;
	private Long medicoId;
	private String token;

	private ConsultaService service = new ConsultaService();

	@PostConstruct
	public void init() {
	    filtroStatus = "todas";

	    FacesContext fc = FacesContext.getCurrentInstance();
	    HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
	    HttpSession session = req.getSession(false);

	    if (session != null) {
	        medicoId = (Long) session.getAttribute("usuarioId");
	        token = (String) session.getAttribute("token");
	    }

	    carregarConsultas();
	}

	public void carregarConsultas() {
	    try {
	        consultas = buscarConsultasDoMedico(medicoId); 
	    } catch (Exception e) {
	        consultas = new ArrayList<>();
	        addErro("Erro ao carregar consultas: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	private List<MedicoConsultaDTO> buscarConsultasDoMedico(Long medicoId) throws Exception {
	    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
	    java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
	    		.uri(java.net.URI.create(ConfigUtil.get("api.base.url") + "/api/agendamentos/medico/" + medicoId))
	        .header("Authorization", "Bearer " + token) // <- token aqui
	        .GET()
	        .build();

		java.net.http.HttpResponse<String> response = client.send(request,
				java.net.http.HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200) {
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			return mapper.readValue(response.body(),
					mapper.getTypeFactory().constructCollectionType(List.class, MedicoConsultaDTO.class));
		}
		throw new Exception("Status: " + response.statusCode());
	}

	//filtros
	public List<MedicoConsultaDTO> getConsultasFiltradas() {
		if (consultas == null)
			return new ArrayList<>();
		return consultas.stream().filter(this::passaFiltroStatus).filter(this::passaFiltroTexto)
				.collect(Collectors.toList());
	}

	private boolean passaFiltroStatus(MedicoConsultaDTO c) {
		if (filtroStatus == null || filtroStatus.equalsIgnoreCase("todas"))
			return true;
		return c.getStatus() != null && c.getStatus().toString().equalsIgnoreCase(filtroStatus);
	}

	private boolean passaFiltroTexto(MedicoConsultaDTO c) {
		if (filtroTexto == null || filtroTexto.trim().isEmpty())
			return true;
		String texto = filtroTexto.trim().toLowerCase();
		boolean nomeOk = c.getNomePaciente() != null && c.getNomePaciente().toLowerCase().contains(texto);
		boolean especOk = c.getEspecialidade() != null && c.getEspecialidade().toLowerCase().contains(texto);
		return nomeOk || especOk;
	}

	public void filtrar() {
	}

	//cancelamento
	public void prepararCancelamentoAction(Long id) {
		this.idParaCancelar = id;
		this.exibirModalCancelamento = true;
	}

	public void confirmarCancelamento() {
	    if (idParaCancelar != null) {
	        try {	
	            com.vittae.dto.ConsultaDTO dto = new com.vittae.dto.ConsultaDTO();
	            dto.setId(idParaCancelar);
	            dto.setStatus(Status.CANCELADA);
	            service.cancelar(idParaCancelar, dto);
	            carregarConsultas();
	            addInfo("Consulta cancelada com sucesso!");
	        } catch (Exception e) {
	            addErro("Erro ao cancelar: " + e.getMessage());
	        }
	    }
	    fecharModal();
	}

	public void fecharModal() {
		this.exibirModalCancelamento = false;
		this.idParaCancelar = null;
	}

	//helpers
	private void addInfo(String msg) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
	}

	private void addErro(String msg) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
	}
	
	public static class MedicoConsultaDTO {
		private Long id;
		private String nomePaciente;
		private java.time.LocalDate dataConsulta;
		private java.time.LocalTime hora;
		private String especialidade;
		private Status status;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getNomePaciente() {
			return nomePaciente;
		}

		public void setNomePaciente(String v) {
			this.nomePaciente = v;
		}

		public java.time.LocalDate getDataConsulta() {
			return dataConsulta;
		}

		public void setDataConsulta(java.time.LocalDate v) {
			this.dataConsulta = v;
		}

		public java.time.LocalTime getHora() {
			return hora;
		}

		public void setHora(java.time.LocalTime v) {
			this.hora = v;
		}

		public String getEspecialidade() {
			return especialidade;
		}

		public void setEspecialidade(String v) {
			this.especialidade = v;
		}

		public Status getStatus() {
			return status;
		}

		public void setStatus(Status v) {
			this.status = v;
		}
	}


	public String getFiltroTexto() {
		return filtroTexto;
	}

	public void setFiltroTexto(String v) {
		this.filtroTexto = v;
	}

	public String getFiltroStatus() {
		return filtroStatus;
	}

	public void setFiltroStatus(String v) {
		this.filtroStatus = v;
	}

	public boolean isExibirModalCancelamento() {
		return exibirModalCancelamento;
	}

	public void setExibirModalCancelamento(boolean v) {
		this.exibirModalCancelamento = v;
	}
}