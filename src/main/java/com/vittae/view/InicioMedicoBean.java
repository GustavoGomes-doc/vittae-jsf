package com.vittae.view;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
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

@Named("inicioMedicoBean")
@ViewScoped
public class InicioMedicoBean implements Serializable {

	private static final String API_URL = ConfigUtil.get("api.base.url") + "/api/agendamentos/medico/";

	private List<PacienteListagemDTO> todasConsultas = new ArrayList<>();

	// Stats
	private long consultasHoje;
	private long consultasPendentes;
	private long consultasRealizadas;
	private long pacientesAtendidos;

	// Painéis
	private List<PacienteListagemDTO> proximasConsultasHoje = new ArrayList<>();
	private List<PacienteListagemDTO> consultasRecentes = new ArrayList<>();

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
		carregarDados();
	}

	public void carregarDados() {
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

				calcularStats();
				calcularPaineis();
			}
		} catch (Exception e) {
			System.err.println("InicioMedicoBean.carregarDados: " + e.getMessage());
		}
	}

	// ── Cálculos ────────────────────────────────────────────────────────────

	private void calcularStats() {
		LocalDate hoje = LocalDate.now();

		consultasHoje = todasConsultas.stream().filter(c -> hoje.equals(c.getDataConsulta())).count();

		consultasPendentes = todasConsultas.stream()
				.filter(c -> c.getStatus() != null && c.getStatus().toString().equalsIgnoreCase("PENDENTE")).count();

		consultasRealizadas = todasConsultas.stream()
				.filter(c -> c.getStatus() != null && c.getStatus().toString().equalsIgnoreCase("REALIZADA")).count();

		// Pacientes únicos pelo nome (substitua por pacienteId se o DTO tiver)
		pacientesAtendidos = todasConsultas.stream().map(PacienteListagemDTO::getNomePaciente).distinct().count();
	}

	private void calcularPaineis() {
		LocalDate hoje = LocalDate.now();

		// Próximas consultas de hoje — status PENDENTE, ordenadas por hora
		proximasConsultasHoje = todasConsultas.stream().filter(c -> hoje.equals(c.getDataConsulta()))
				.filter(c -> c.getStatus() != null && c.getStatus().toString().equalsIgnoreCase("PENDENTE"))
				.sorted((a, b) -> {
					if (a.getHora() == null)
						return 1;
					if (b.getHora() == null)
						return -1;
					return a.getHora().compareTo(b.getHora());
				}).limit(5).collect(Collectors.toList());

		// Consultas recentes — as 5 últimas (qualquer status), data desc
		consultasRecentes = todasConsultas.stream().filter(c -> c.getDataConsulta() != null).sorted((a, b) -> {
			int cmp = b.getDataConsulta().compareTo(a.getDataConsulta());
			if (cmp != 0)
				return cmp;
			if (b.getHora() == null)
				return -1;
			if (a.getHora() == null)
				return 1;
			return b.getHora().compareTo(a.getHora());
		}).limit(5).collect(Collectors.toList());
	}

	// ── Helpers ─────────────────────────────────────────────────────────────

	private HttpSession getSession() {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc == null)
			return null;
		return (HttpSession) fc.getExternalContext().getSession(false);
	}

	// ── Getters ─────────────────────────────────────────────────────────────

	public long getConsultasHoje() {
		return consultasHoje;
	}

	public long getConsultasPendentes() {
		return consultasPendentes;
	}

	public long getConsultasRealizadas() {
		return consultasRealizadas;
	}

	public long getPacientesAtendidos() {
		return pacientesAtendidos;
	}

	public List<PacienteListagemDTO> getProximasConsultasHoje() {
		return proximasConsultasHoje;
	}

	public List<PacienteListagemDTO> getConsultasRecentes() {
		return consultasRecentes;
	}
}