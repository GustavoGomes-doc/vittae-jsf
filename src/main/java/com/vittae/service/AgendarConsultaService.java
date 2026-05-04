package com.vittae.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class AgendarConsultaService {

	// mesma porta do seu Spring Boot — ajusta se precisar
	private static final String API_URL = "http://localhost:8081/api/agendamentos";
	private static final String API_MEDICOS = "http://localhost:8081/api/medicos";

	/**
	 * Envia o agendamento pro Spring Boot via POST JSON. O AgendamentoDTO espelha
	 * exatamente o que o ConsultaService do Spring espera.
	 */
	public void salvarAgendamento(AgendamentoDTO dto) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		String json = mapper.writeValueAsString(dto);
		System.out.println("Enviando agendamento JSON: " + json);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL))
				.header("Content-Type", "application/json").POST(BodyPublishers.ofString(json)).build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200 && response.statusCode() != 201) {
			throw new Exception("Falha ao agendar. Status: " + response.statusCode() + " - " + response.body());
		}

		System.out.println("Agendamento salvo com sucesso!");
	}

	/**
	 * Busca a lista de médicos do Spring Boot pra exibir no step 2.
	 */
	public String buscarMedicosJson() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_MEDICOS)).GET().build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200) {
			return response.body();
		}
		throw new Exception("Falha ao buscar médicos. Status: " + response.statusCode());
	}

	// ── DTO interno que espelha o AgendamentoDTO do Spring ──────────────────

	public static class AgendamentoDTO {
		private String tipoConsulta;
		private String especialidade;
		private LocalDate dataAgendado;
		private LocalDate dataConsulta;
		private LocalTime hora;
		private Long medicoId;
		private String observacoes;
		private PacienteDTO paciente;

		public AgendamentoDTO() {
		}

		public String getTipoConsulta() {
			return tipoConsulta;
		}

		public void setTipoConsulta(String tipoConsulta) {
			this.tipoConsulta = tipoConsulta;
		}

		public String getEspecialidade() {
			return especialidade;
		}

		public void setEspecialidade(String especialidade) {
			this.especialidade = especialidade;
		}

		public LocalDate getDataAgendado() {
			return dataAgendado;
		}

		public void setDataAgendado(LocalDate dataAgendado) {
			this.dataAgendado = dataAgendado;
		}

		public LocalDate getDataConsulta() {
			return dataConsulta;
		}

		public void setDataConsulta(LocalDate dataConsulta) {
			this.dataConsulta = dataConsulta;
		}

		public LocalTime getHora() {
			return hora;
		}

		public void setHora(LocalTime hora) {
			this.hora = hora;
		}

		public Long getMedicoId() {
			return medicoId;
		}

		public void setMedicoId(Long medicoId) {
			this.medicoId = medicoId;
		}

		public String getObservacoes() {
			return observacoes;
		}

		public void setObservacoes(String observacoes) {
			this.observacoes = observacoes;
		}

		public PacienteDTO getPaciente() {
			return paciente;
		}

		public void setPaciente(PacienteDTO paciente) {
			this.paciente = paciente;
		}
	}

	public static class PacienteDTO {
		private String nome;
		private String cpf;
		private String telefone;

		public PacienteDTO() {
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public String getCpf() {
			return cpf;
		}

		public void setCpf(String cpf) {
			this.cpf = cpf;
		}

		public String getTelefone() {
			return telefone;
		}

		public void setTelefone(String telefone) {
			this.telefone = telefone;
		}
	}
}
