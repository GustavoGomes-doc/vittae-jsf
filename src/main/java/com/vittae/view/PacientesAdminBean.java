package com.vittae.view;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
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
import com.vittae.dto.PacienteAdminDTO;
import com.vittae.util.ConfigUtil;

@Named("pacientesAdminBean")
@ViewScoped
public class PacientesAdminBean implements Serializable {

	private static final String BASE_URL = ConfigUtil.get("api.base.url");

	private List<PacienteAdminDTO> todosPacientes = new ArrayList<>();
	private List<ConsultaAdminDTO> consultasDoPaciente = new ArrayList<>();

	private PacienteAdminDTO pacienteSelecionado;
	private String filtroTexto = "";

	@PostConstruct
	public void init() {
		carregarPacientes();
	}

	private void carregarPacientes() {
		try {
			HttpSession session = obterSession();
			if (session == null)
				return;
			String token = (String) session.getAttribute("token");
			URL url = new URL(BASE_URL + "/api/pacientes/admin");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + token);
			conn.setRequestProperty("Content-Type", "application/json");
			if (conn.getResponseCode() == 200) {
				todosPacientes = criarMapper().readValue(conn.getInputStream(),
						new TypeReference<List<PacienteAdminDTO>>() {
						});
			}
			conn.disconnect();
		} catch (Exception e) {
			adicionarErro("Erro ao carregar pacientes: " + e.getMessage());
		}
	}

	public List<PacienteAdminDTO> getPacientesFiltrados() {
		if (filtroTexto == null || filtroTexto.isBlank())
			return todosPacientes;
		return todosPacientes.stream().filter(p -> contem(p.getNome(), filtroTexto) || contem(p.getCpf(), filtroTexto)
				|| contem(p.getEmail(), filtroTexto)).collect(Collectors.toList());
	}

	private boolean contem(String campo, String busca) {
		return campo != null && campo.toLowerCase().contains(busca.toLowerCase());
	}

	public void limparFiltros() {
		filtroTexto = "";
	}

	public void verDetalhes(PacienteAdminDTO paciente) {
		this.pacienteSelecionado = paciente;
	}

	public void verHistorico(PacienteAdminDTO paciente) {
		this.pacienteSelecionado = paciente;
		carregarConsultasDoPaciente(paciente.getId());
	}

	public void excluirPaciente(PacienteAdminDTO paciente) {
		if (paciente == null)
			return;
		try {
			HttpSession session = obterSession();
			if (session == null)
				return;
			String token = (String) session.getAttribute("token");
			URL url = new URL(BASE_URL + "/api/usuarios/" + paciente.getId());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setRequestProperty("Authorization", "Bearer " + token);
			int code = conn.getResponseCode();
			conn.disconnect();
			if (code == 200 || code == 204) {
				todosPacientes.removeIf(p -> p.getId().equals(paciente.getId()));
				adicionarInfo("Paciente " + paciente.getNome() + " excluído com sucesso.");
			} else {
				adicionarErro("Não foi possível excluir o paciente. Código: " + code);
			}
		} catch (Exception e) {
			adicionarErro("Erro ao excluir paciente: " + e.getMessage());
		}
	}

	private void carregarConsultasDoPaciente(Long pacienteId) {
		consultasDoPaciente = new ArrayList<>();
		try {
			HttpSession session = obterSession();
			if (session == null)
				return;
			String token = (String) session.getAttribute("token");
			URL url = new URL(BASE_URL + "/api/agendamentos/pacientes/" + pacienteId);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + token);
			if (conn.getResponseCode() == 200) {
				consultasDoPaciente = criarMapper().readValue(conn.getInputStream(),
						new TypeReference<List<ConsultaAdminDTO>>() {
						});
			}
			conn.disconnect();
		} catch (Exception e) {
			adicionarErro("Erro ao carregar histórico: " + e.getMessage());
		}
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

	public List<PacienteAdminDTO> getTodosPacientes() {
		return todosPacientes;
	}

	public List<ConsultaAdminDTO> getConsultasDoPaciente() {
		return consultasDoPaciente;
	}

	public PacienteAdminDTO getPacienteSelecionado() {
		return pacienteSelecionado;
	}

	public void setPacienteSelecionado(PacienteAdminDTO p) {
		this.pacienteSelecionado = p;
	}

	public String getFiltroTexto() {
		return filtroTexto;
	}

	public void setFiltroTexto(String v) {
		this.filtroTexto = v;
	}
}