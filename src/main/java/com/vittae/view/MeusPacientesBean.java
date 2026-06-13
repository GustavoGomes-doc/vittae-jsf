package com.vittae.view;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.dto.PacienteListagemDTO;
import com.vittae.util.ConfigUtil;

@Named("meusPacientesBean")
@ViewScoped
public class MeusPacientesBean implements Serializable {

	private static final String API_URL = ConfigUtil.get("api.base.url") + "/api/agendamentos/medico/";

	private List<PacienteListagemDTO> todasConsultas = new ArrayList<>();
	private List<PacienteListagemDTO> consultasFiltradas = new ArrayList<>();

	private String filtroTexto = "";
	private String filtroStatus = "todas";

	private Long medicoId;
	private String token;

	// ── Init ────────────────────────────────────────────────────────────────

	@PostConstruct
	public void init() {
		HttpSession session = getSession();
		if (session != null) {
			Object idObj = session.getAttribute("usuarioId");
			medicoId = idObj instanceof Long ? (Long) idObj : Long.valueOf(idObj.toString());
			token = (String) session.getAttribute("token");
		}
		carregarConsultas();
	}

	public void carregarConsultas() {
		if (medicoId == null || token == null)
			return;
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL + medicoId))
					.header("Authorization", "Bearer " + token).GET().build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

				todasConsultas = mapper.readValue(response.body(),
						mapper.getTypeFactory().constructCollectionType(List.class, PacienteListagemDTO.class));

				// Ordena por data desc, hora desc
				todasConsultas.sort(Comparator.comparing(PacienteListagemDTO::getDataConsulta,
						Comparator.nullsLast(Comparator.reverseOrder()))
						.thenComparing(PacienteListagemDTO::getHora, Comparator.nullsLast(Comparator.reverseOrder())));

				aplicarFiltros();
			}
		} catch (Exception e) {
			System.err.println("MeusPacientesBean.carregarConsultas: " + e.getMessage());
		}
	}

	// ── Filtros ─────────────────────────────────────────────────────────────

	public void filtrar() {
		aplicarFiltros();
	}

	private void aplicarFiltros() {
		consultasFiltradas = todasConsultas.stream().filter(this::passaFiltroStatus).filter(this::passaFiltroTexto)
				.collect(Collectors.toList());
	}

	private boolean passaFiltroStatus(PacienteListagemDTO c) {
		if (filtroStatus == null || filtroStatus.equalsIgnoreCase("todas"))
			return true;
		return c.getStatus() != null && c.getStatus().toString().equalsIgnoreCase(filtroStatus);
	}

	private boolean passaFiltroTexto(PacienteListagemDTO c) {
		if (filtroTexto == null || filtroTexto.trim().isEmpty())
			return true;
		String texto = filtroTexto.trim().toLowerCase();
		boolean nomeOk = c.getNomePaciente() != null && c.getNomePaciente().toLowerCase().contains(texto);
		boolean espOk = c.getEspecialidade() != null && c.getEspecialidade().toLowerCase().contains(texto);
		return nomeOk || espOk;
	}

	// ── Helpers ─────────────────────────────────────────────────────────────

	private HttpSession getSession() {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc == null)
			return null;
		return (HttpSession) fc.getExternalContext().getSession(false);
	}

	// ── Getters / Setters ───────────────────────────────────────────────────

	public List<PacienteListagemDTO> getConsultasFiltradas() {
		return consultasFiltradas;
	}

	public int getTotalConsultas() {
		return todasConsultas.size();
	}

	public int getTotalFiltradas() {
		return consultasFiltradas.size();
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
}