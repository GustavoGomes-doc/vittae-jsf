package com.vittae.view;

import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.dto.ConsultaAdminDTO;

@Named("todasConsultasBean")
@ViewScoped
public class TodasConsultasAdminBean implements Serializable {

	private static final String BASE_URL = "http://localhost:9090";

	private List<ConsultaAdminDTO> todasConsultas = new ArrayList<>();
	private ConsultaAdminDTO consultaSelecionada;

	private String filtroTexto = "";
	private String filtroStatus = "";
	private String filtroDataInicio = "";
	private String filtroDataFim = "";

	@PostConstruct
	public void init() {
		carregarConsultas();
	}

	private void carregarConsultas() {
		try {
			HttpSession session = obterSession();
			if (session == null)
				return;
			String token = (String) session.getAttribute("token");
			URL url = new URL(BASE_URL + "/api/agendamentos/todos");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + token);
			conn.setRequestProperty("Content-Type", "application/json");
			if (conn.getResponseCode() == 200) {
				todasConsultas = criarMapper().readValue(conn.getInputStream(),
						new TypeReference<List<ConsultaAdminDTO>>() {
						});
			}
			conn.disconnect();
		} catch (Exception e) {
			adicionarErro("Erro ao carregar consultas: " + e.getMessage());
		}
	}

	public List<ConsultaAdminDTO> getConsultasFiltradas() {
		return todasConsultas.stream().filter(c -> {
			boolean textoOk = filtroTexto == null || filtroTexto.isBlank() || contem(c.getNomePaciente(), filtroTexto)
					|| contem(c.getNomeMedico(), filtroTexto) || contem(c.getEspecialidade(), filtroTexto);
			boolean statusOk = filtroStatus == null || filtroStatus.isBlank()
					|| filtroStatus.equalsIgnoreCase(c.getStatus());
			boolean dataInicioOk = filtroDataInicio == null || filtroDataInicio.isBlank()
					|| (c.getDataConsulta() != null && c.getDataConsulta().compareTo(filtroDataInicio) >= 0);
			boolean dataFimOk = filtroDataFim == null || filtroDataFim.isBlank()
					|| (c.getDataConsulta() != null && c.getDataConsulta().compareTo(filtroDataFim) <= 0);
			return textoOk && statusOk && dataInicioOk && dataFimOk;
		}).collect(Collectors.toList());
	}

	private boolean contem(String campo, String busca) {
		return campo != null && campo.toLowerCase().contains(busca.toLowerCase());
	}

	public void limparFiltros() {
		filtroTexto = "";
		filtroStatus = "";
		filtroDataInicio = "";
		filtroDataFim = "";
	}

	public void verDetalhes(ConsultaAdminDTO consulta) {
		this.consultaSelecionada = consulta;
	}

	public void cancelarConsulta(ConsultaAdminDTO consulta) {
		if (consulta == null)
			return;
		try {
			HttpSession session = obterSession();
			if (session == null)
				return;
			String token = (String) session.getAttribute("token");
			URL url = new URL(BASE_URL + "/api/agendamentos/" + consulta.getId() + "/cancelar");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Authorization", "Bearer " + token);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			try (OutputStream os = conn.getOutputStream()) {
				os.write("{\"status\":\"CANCELADA\"}".getBytes(StandardCharsets.UTF_8));
			}
			int code = conn.getResponseCode();
			conn.disconnect();
			if (code == 200 || code == 204) {
				consulta.setStatus("CANCELADA");
				adicionarInfo("Consulta de " + consulta.getNomePaciente() + " cancelada com sucesso.");
			} else {
				adicionarErro("Não foi possível cancelar. Código: " + code);
			}
		} catch (Exception e) {
			adicionarErro("Erro ao cancelar consulta: " + e.getMessage());
		}
	}

	public String getStatusClass(String status) {
		if (status == null)
			return "default";
		return switch (status.toUpperCase()) {
		case "AGENDADA" -> "warning";
		case "CONFIRMADA" -> "info";
		case "REALIZADA" -> "success";
		case "CANCELADA" -> "danger";
		default -> "default";
		};
	}

	private HttpSession obterSession() {
		return (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
	}

	private ObjectMapper criarMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}

	private void adicionarErro(String msg) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
	}

	private void adicionarInfo(String msg) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
	}

	public List<ConsultaAdminDTO> getTodasConsultas() {
		return todasConsultas;
	}

	public ConsultaAdminDTO getConsultaSelecionada() {
		return consultaSelecionada;
	}

	public void setConsultaSelecionada(ConsultaAdminDTO c) {
		this.consultaSelecionada = c;
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

	public String getFiltroDataInicio() {
		return filtroDataInicio;
	}

	public void setFiltroDataInicio(String v) {
		this.filtroDataInicio = v;
	}

	public String getFiltroDataFim() {
		return filtroDataFim;
	}

	public void setFiltroDataFim(String v) {
		this.filtroDataFim = v;
	}
}