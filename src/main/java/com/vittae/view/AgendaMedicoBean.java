package com.vittae.view;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.YearMonth;
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

@Named("agendaMedicoBean")
@ViewScoped
public class AgendaMedicoBean implements Serializable {

	private static final String API_URL = ConfigUtil.get("api.base.url") + "/api/medicos/";

	private List<PacienteListagemDTO> todasConsultas = new ArrayList<>();

	// Mês/ano sendo visualizado
	private int mes;
	private int ano;

	// Células do calendário — 42 posições (6 semanas × 7 dias)
	// Cada célula é um LocalDate (pode ser do mês anterior/próximo para preencher
	// grid)
	private List<LocalDate> celulas = new ArrayList<>();

	private Long medicoId;
	private String token;

	private static final String[] MESES_PT = { "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho",
			"Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };

	// ── Init ────────────────────────────────────────────────────────────────

	@PostConstruct
	public void init() {
		HttpSession session = getSession();
		if (session != null) {
			Object idObj = session.getAttribute("usuarioId");
			medicoId = idObj instanceof Long ? (Long) idObj : Long.valueOf(idObj.toString());
			token = (String) session.getAttribute("token");
		}
		LocalDate hoje = LocalDate.now();
		mes = hoje.getMonthValue();
		ano = hoje.getYear();

		carregarConsultas();
		gerarCelulas();
	}

	// ── Navegação ───────────────────────────────────────────────────────────

	public void mesAnterior() {
		YearMonth ym = YearMonth.of(ano, mes).minusMonths(1);
		mes = ym.getMonthValue();
		ano = ym.getYear();
		gerarCelulas();
	}

	public void proximoMes() {
		YearMonth ym = YearMonth.of(ano, mes).plusMonths(1);
		mes = ym.getMonthValue();
		ano = ym.getYear();
		gerarCelulas();
	}

	// ── Dados ───────────────────────────────────────────────────────────────

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
			}
		} catch (Exception e) {
			System.err.println("MinhaAgendaBean.carregarConsultas: " + e.getMessage());
		}
	}

	// ── Calendário ──────────────────────────────────────────────────────────

	private void gerarCelulas() {
		celulas.clear();
		YearMonth ym = YearMonth.of(ano, mes);
		LocalDate primeiroDia = ym.atDay(1);

		// Domingo = 7 no DayOfWeek ISO, mas queremos índice 0
		int inicioDow = primeiroDia.getDayOfWeek().getValue() % 7; // Dom=0, Seg=1 ... Sab=6

		// Preenche dias do mês anterior
		LocalDate cursor = primeiroDia.minusDays(inicioDow);
		for (int i = 0; i < 42; i++) {
			celulas.add(cursor);
			cursor = cursor.plusDays(1);
		}
	}

	// ── Consultas por dia (chamado pelo XHTML via EL) ────────────────────────

	public List<PacienteListagemDTO> consultasDoDia(LocalDate dia) {
		if (dia == null || todasConsultas == null)
			return new ArrayList<>();
		return todasConsultas.stream().filter(c -> dia.equals(c.getDataConsulta())).sorted((a, b) -> {
			if (a.getHora() == null)
				return 1;
			if (b.getHora() == null)
				return -1;
			return a.getHora().compareTo(b.getHora());
		}).collect(Collectors.toList());
	}

	// ── Helpers de display ──────────────────────────────────────────────────

	public boolean pertenceAoMes(LocalDate dia) {
		return dia != null && dia.getMonthValue() == mes && dia.getYear() == ano;
	}

	public boolean eHoje(LocalDate dia) {
		return dia != null && dia.equals(LocalDate.now());
	}

	public String getTituloMes() {
		return MESES_PT[mes] + " " + ano;
	}

	public String cssDoStatus(PacienteListagemDTO c) {
		if (c.getStatus() == null)
			return "agenda-slot-pendente";
		switch (c.getStatus().toString().toUpperCase()) {
		case "REALIZADA":
			return "agenda-slot-realizada";
		case "CANCELADA":
			return "agenda-slot-cancelada";
		default:
			return "agenda-slot-pendente";
		}
	}

	// ── Session helper ──────────────────────────────────────────────────────

	private HttpSession getSession() {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc == null)
			return null;
		return (HttpSession) fc.getExternalContext().getSession(false);
	}

	// ── Getters ─────────────────────────────────────────────────────────────

	public List<LocalDate> getCelulas() {
		return celulas;
	}

	public int getMes() {
		return mes;
	}

	public int getAno() {
		return ano;
	}
}