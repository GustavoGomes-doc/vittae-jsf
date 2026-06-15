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
import com.vittae.dto.UsuarioPerfilDTO;
import com.vittae.dto.UsuarioPerfilDTO.EspecialidadeDTO;
import com.vittae.util.ConfigUtil;

@Named("medicosAdminBean")
@ViewScoped
public class MedicosAdminBean implements Serializable {

	private static final String BASE_URL = ConfigUtil.get("api.base.url");	

	private List<UsuarioPerfilDTO> todosMedicos = new ArrayList<>();
	private List<EspecialidadeDTO> especialidades = new ArrayList<>();
	private UsuarioPerfilDTO medicoSelecionado;

	private String filtroTexto = "";
	private String filtroEspecialidade = "";

	@PostConstruct
	public void init() {
		carregarMedicos();
		carregarEspecialidades();
	}

	private void carregarMedicos() {
		try {
			HttpSession session = obterSession();
			if (session == null)
				return;
			String token = (String) session.getAttribute("token");
			URL url = new URL(BASE_URL + "/api/medicos");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + token);
			conn.setRequestProperty("Content-Type", "application/json");
			if (conn.getResponseCode() == 200) {
				todosMedicos = criarMapper().readValue(conn.getInputStream(),
						new TypeReference<List<UsuarioPerfilDTO>>() {
						});
			}
			conn.disconnect();
		} catch (Exception e) {
			adicionarErro("Erro ao carregar médicos: " + e.getMessage());
		}
	}

	private void carregarEspecialidades() {
		try {
			URL url = new URL(BASE_URL + "/api/especialidades");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == 200) {
				especialidades = criarMapper().readValue(conn.getInputStream(),
						new TypeReference<List<EspecialidadeDTO>>() {
						});
			}
			conn.disconnect();
		} catch (Exception e) {
			/* silencioso */ }
	}

	public List<UsuarioPerfilDTO> getMedicosFiltrados() {
		return todosMedicos.stream().filter(m -> {
			boolean textoOk = filtroTexto == null || filtroTexto.isBlank() || contem(m.getNome(), filtroTexto)
					|| contem(m.getCrm(), filtroTexto) || temEspecialidade(m, filtroTexto);
			boolean espOk = filtroEspecialidade == null || filtroEspecialidade.isBlank()
					|| temEspecialidade(m, filtroEspecialidade);
			return textoOk && espOk;
		}).collect(Collectors.toList());
	}

	private boolean contem(String campo, String busca) {
		return campo != null && campo.toLowerCase().contains(busca.toLowerCase());
	}

	private boolean temEspecialidade(UsuarioPerfilDTO medico, String busca) {
		if (medico.getEspecialidades() == null)
			return false;
		return medico.getEspecialidades().stream().anyMatch(e -> contem(e.getNome(), busca));
	}

	public void limparFiltros() {
		filtroTexto = "";
		filtroEspecialidade = "";
	}

	public void verDetalhes(UsuarioPerfilDTO medico) {
		this.medicoSelecionado = medico;
	}

	public void excluirMedico(UsuarioPerfilDTO medico) {
		if (medico == null)
			return;
		try {
			HttpSession session = obterSession();
			if (session == null)
				return;
			String token = (String) session.getAttribute("token");
			URL url = new URL(BASE_URL + "/api/medicos/" + medico.getId());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("DELETE");
			conn.setRequestProperty("Authorization", "Bearer " + token);
			int code = conn.getResponseCode();
			conn.disconnect();
			if (code == 200 || code == 204) {
				todosMedicos.removeIf(m -> m.getId().equals(medico.getId()));
				adicionarInfo("Médico " + medico.getNome() + " excluído com sucesso.");
			} else {
				adicionarErro("Não foi possível excluir o médico. Código: " + code);
			}
		} catch (Exception e) {
			adicionarErro("Erro ao excluir médico: " + e.getMessage());
		}
	}

	public String formatarEspecialidades(UsuarioPerfilDTO medico) {
		if (medico == null || medico.getEspecialidades() == null || medico.getEspecialidades().isEmpty())
			return "—";
		return medico.getEspecialidades().stream().map(EspecialidadeDTO::getNome).collect(Collectors.joining(", "));
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

	public List<UsuarioPerfilDTO> getTodosMedicos() {
		return todosMedicos;
	}

	public List<EspecialidadeDTO> getEspecialidades() {
		return especialidades;
	}

	public UsuarioPerfilDTO getMedicoSelecionado() {
		return medicoSelecionado;
	}

	public void setMedicoSelecionado(UsuarioPerfilDTO m) {
		this.medicoSelecionado = m;
	}

	public String getFiltroTexto() {
		return filtroTexto;
	}

	public void setFiltroTexto(String v) {
		this.filtroTexto = v;
	}

	public String getFiltroEspecialidade() {
		return filtroEspecialidade;
	}

	public void setFiltroEspecialidade(String v) {
		this.filtroEspecialidade = v;
	}
}