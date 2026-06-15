package com.vittae.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.dto.AdminConsultaDTO;
import com.vittae.dto.ConsultaDTO;
import com.vittae.model.enums.Status;
import com.vittae.util.ConfigUtil;

@Named
@ApplicationScoped
public class ConsultaService {

	private static final String API_URL = ConfigUtil.get("api.base.url") + "/api/agendamentos";
	private static final String API_MEDICOS = ConfigUtil.get("api.base.url") + "/api/medicos";

	private ObjectMapper mapper() {
		ObjectMapper m = new ObjectMapper();
		m.registerModule(new JavaTimeModule());
		m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return m;
	}

	private String obterTokenSessao() {
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			if (ctx != null && ctx.getExternalContext() != null && ctx.getExternalContext().getSessionMap() != null) {
				return (String) ctx.getExternalContext().getSessionMap().get("token");
			}
		} catch (Exception e) {
			System.err.println("Erro ao recuperar token da sessão: " + e.getMessage());
		}
		return null;
	}

	// ── SALVAR AGENDAMENTO ───────────────────────────────────────────────────
	public void salvarAgendamento(AgendamentoDTO dto) throws Exception {
		String json = mapper().writeValueAsString(dto);
		System.out.println("Enviando agendamento JSON: " + json);

		String token = obterTokenSessao();
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest.Builder rb = HttpRequest.newBuilder().uri(URI.create(API_URL)).header("Content-Type",
				"application/json");
		if (token != null && !token.isEmpty())
			rb.header("Authorization", "Bearer " + token);

		HttpResponse<String> response = client.send(rb.POST(BodyPublishers.ofString(json)).build(),
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200 && response.statusCode() != 201)
			throw new Exception("Falha ao agendar. Status: " + response.statusCode() + " - " + response.body());

		System.out.println("Agendamento salvo com sucesso!");
	}

	// ── BUSCAR MÉDICOS (JSON bruto pro JS) ──────────────────────────────────
	public String buscarMedicosJson() throws Exception {
		String token = obterTokenSessao();
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest.Builder rb = HttpRequest.newBuilder().uri(URI.create(API_MEDICOS)).GET();
		if (token != null && !token.isEmpty())
			rb.header("Authorization", "Bearer " + token);

		HttpResponse<String> response = client.send(rb.build(), HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200)
			return response.body();
		throw new Exception("Falha ao buscar médicos. Status: " + response.statusCode());
	}

	// ── LISTAR CONSULTAS DO PACIENTE LOGADO ─────────────────────────────────
	public List<ConsultaDTO> listarTodas() throws Exception {
		String token = obterTokenSessao();
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest.Builder rb = HttpRequest.newBuilder().uri(URI.create(API_URL)).GET();
		if (token != null && !token.isEmpty())
			rb.header("Authorization", "Bearer " + token);

		HttpResponse<String> response = client.send(rb.build(), HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200)
			return mapper().readValue(response.body(),
					mapper().getTypeFactory().constructCollectionType(List.class, ConsultaDTO.class));

		throw new Exception("Erro ao buscar consultas. Status: " + response.statusCode());
	}

	// ── LISTAR TODAS (ADMIN) ─────────────────────────────────────────────────
	public List<AdminConsultaDTO> listarTodasAdmin(String token) throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(ConfigUtil.get("api.base.url") + "/api/agendamentos/todos"))
				.header("Authorization", "Bearer " + token).GET().build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200)
			return mapper().readValue(response.body(),
					mapper().getTypeFactory().constructCollectionType(List.class, AdminConsultaDTO.class));

		throw new Exception("Erro ao buscar consultas admin. Status: " + response.statusCode());
	}

	// ── CANCELAR ────────────────────────────────────────────────────────────
	public void cancelar(Long id, ConsultaDTO consulta) throws Exception {
		String token = obterTokenSessao();
		HttpClient client = HttpClient.newHttpClient();

		String json = "{\"status\":\"CANCELADA\"}";

		HttpRequest.Builder rb = HttpRequest.newBuilder().uri(URI.create(API_URL + "/" + id)).header("Content-Type",
				"application/json");
		if (token != null && !token.isEmpty())
			rb.header("Authorization", "Bearer " + token);

		HttpResponse<String> response = client.send(rb.PUT(HttpRequest.BodyPublishers.ofString(json)).build(),
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200)
			throw new Exception("Erro ao cancelar. Status: " + response.statusCode() + " - " + response.body());
	}

	// ── REMARCAR ────────────────────────────────────────────────────────────
	public void remarcar(Long id, ConsultaDTO consulta) throws Exception {
		String json = mapper().writeValueAsString(consulta);
		String token = obterTokenSessao();
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest.Builder rb = HttpRequest.newBuilder().uri(URI.create(API_URL + "/" + id)).header("Content-Type",
				"application/json");
		if (token != null && !token.isEmpty())
			rb.header("Authorization", "Bearer " + token);

		HttpResponse<String> response = client.send(rb.PUT(HttpRequest.BodyPublishers.ofString(json)).build(),
				HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200)
			throw new Exception("Erro ao remarcar. Status: " + response.statusCode() + " - " + response.body());
	}

	// ── DTOs internos ────────────────────────────────────────────────────────
	public static class AgendamentoDTO {
		private String tipoConsulta;
		private String especialidade;
		private LocalDate dataAgendado;
		private LocalDate dataConsulta;
		private LocalTime hora;
		private Long medicoId;
		private String observacoes;
		private PacienteDTO paciente;
		private String respNome;
		private String respCpf;
		private String respParentesco;
		private LocalDate respDataNascimento;

		public AgendamentoDTO() {
		}

		public String getTipoConsulta() {
			return tipoConsulta;
		}

		public void setTipoConsulta(String v) {
			this.tipoConsulta = v;
		}

		public String getEspecialidade() {
			return especialidade;
		}

		public void setEspecialidade(String v) {
			this.especialidade = v;
		}

		public LocalDate getDataAgendado() {
			return dataAgendado;
		}

		public void setDataAgendado(LocalDate v) {
			this.dataAgendado = v;
		}

		public LocalDate getDataConsulta() {
			return dataConsulta;
		}

		public void setDataConsulta(LocalDate v) {
			this.dataConsulta = v;
		}

		public LocalTime getHora() {
			return hora;
		}

		public void setHora(LocalTime v) {
			this.hora = v;
		}

		public Long getMedicoId() {
			return medicoId;
		}

		public void setMedicoId(Long v) {
			this.medicoId = v;
		}

		public String getObservacoes() {
			return observacoes;
		}

		public void setObservacoes(String v) {
			this.observacoes = v;
		}

		public PacienteDTO getPaciente() {
			return paciente;
		}

		public void setPaciente(PacienteDTO v) {
			this.paciente = v;
		}

		public String getRespNome() {
			return respNome;
		}

		public void setRespNome(String v) {
			this.respNome = v;
		}

		public String getRespCpf() {
			return respCpf;
		}

		public void setRespCpf(String v) {
			this.respCpf = v;
		}

		public String getRespParentesco() {
			return respParentesco;
		}

		public void setRespParentesco(String v) {
			this.respParentesco = v;
		}

		public LocalDate getRespDataNascimento() {
			return respDataNascimento;
		}

		public void setRespDataNascimento(LocalDate v) {
			this.respDataNascimento = v;
		}
	}

	public static class PacienteDTO {
		private String nome;
		private String cpf;
		private String telefone;
		private String genero;
		private String nascimento;

		public PacienteDTO() {
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String v) {
			this.nome = v;
		}

		public String getCpf() {
			return cpf;
		}

		public void setCpf(String v) {
			this.cpf = v;
		}

		public String getTelefone() {
			return telefone;
		}

		public void setTelefone(String v) {
			this.telefone = v;
		}

		public String getGenero() {
			return genero;
		}

		public void setGenero(String v) {
			this.genero = v;
		}

		public String getNascimento() {
			return nascimento;
		}

		public void setNascimento(String v) {
			this.nascimento = v;
		}
	}
}