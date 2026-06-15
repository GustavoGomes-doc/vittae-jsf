package com.vittae.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.util.ConfigUtil;

public class AgendarConsultaService {

	private static final String API_URL = ConfigUtil.get("api.base.url") + "/api/agendamentos";
	private static final String API_MEDICOS = ConfigUtil.get("api.base.url") + "/api/medicos";

	// ── SALVAR ──────────────────────────────────────────────────────────────
	public void salvarAgendamento(AgendamentoDTO dto, String token) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		String json = mapper.writeValueAsString(dto);
		System.out.println("Enviando agendamento JSON: " + json);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL))
				.header("Content-Type", "application/json").header("Authorization", "Bearer " + token)
				.POST(BodyPublishers.ofString(json)).build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200 && response.statusCode() != 201) {
			throw new Exception("Falha ao agendar. Status: " + response.statusCode() + " - " + response.body());
		}

		System.out.println("Agendamento salvo com sucesso!");
	}

	// ── BUSCAR MÉDICOS ───────────────────────────────────────────────────────
	public String buscarMedicosJson() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_MEDICOS)).GET().build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200) {
			return response.body();
		}
		throw new Exception("Falha ao buscar médicos. Status: " + response.statusCode());
	}

	// ── DTOs ─────────────────────────────────────────────────────────────────
	public static class AgendamentoDTO {
		private String especialidade;
		private LocalDate dataConsulta;
		private LocalTime hora;
		private Long medicoId;
		private String observacoes;
		private PacienteDTO paciente;

		// responsável (só preenchido quando paciente é menor de idade)
		private String respNome;
		private String respCpf;
		private String respParentesco;

		public AgendamentoDTO() {
		}

		public String getEspecialidade() {
			return especialidade;
		}

		public void setEspecialidade(String v) {
			this.especialidade = v;
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