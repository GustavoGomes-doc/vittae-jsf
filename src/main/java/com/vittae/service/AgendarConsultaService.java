package com.vittae.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.util.ConfigUtil;

public class AgendarConsultaService {

	private static final String API_URL = ConfigUtil.get("api.base.url") + "/api/agendamentos";
	private static final String API_MEDICOS = ConfigUtil.get("api.base.url") + "/api/medicos";


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

	public String buscarMedicosJson() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_MEDICOS)).GET().build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200) {
			return response.body();
		}
		throw new Exception("Falha ao buscar médicos. Status: " + response.statusCode());
	}

	// buscahorariosocupadosdiretodaspringbootparanoofazerdoubledbooking
	public List<String> buscarHorariosOcupados(Long medicoId, String data) throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		// requisicaogetcomparametrosofazendofiltronaapi
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(API_URL + "/horarios-ocupados?medicoId=" + medicoId + "&data=" + data)).GET().build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 200) {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(response.body(), new TypeReference<List<String>>() {
			});
		}
		throw new Exception("Falha ao buscar horários ocupados.");
	}

	public static class AgendamentoDTO {
		private String especialidade;
		private LocalDate dataConsulta;
		private LocalTime hora;
		private Long medicoId;
		private String observacoes;
		private PacienteDTO paciente;
		private String respNome;
		private String respCpf;
		private String respParentesco;;

		public AgendamentoDTO() {
		}

		public String getEspecialidade() {
			return especialidade;
		}

		public void setEspecialidade(String especialidade) {
			this.especialidade = especialidade;
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

		public String getRespNome() {
			return respNome;
		}

		public void setRespNome(String respNome) {
			this.respNome = respNome;
		}

		public String getRespCpf() {
			return respCpf;
		}

		public void setRespCpf(String respCpf) {
			this.respCpf = respCpf;
		}

		public String getRespParentesco() {
			return respParentesco;
		}

		public void setRespParentesco(String respParentesco) {
			this.respParentesco = respParentesco;
		}
	}

	public static class PacienteDTO {
		private String nome;
		private String cpf;
		private String telefone;
		private String genero;
		private String nacimiento;

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

		public String getGenero() {
			return genero;
		}

		public void setGenero(String genero) {
			this.genero = genero;
		}

		public String getNascimento() {
			return nacimiento;
		}

		public void setNascimento(String nascimento) {
			this.nacimiento = nascimento;
		}
	}
}