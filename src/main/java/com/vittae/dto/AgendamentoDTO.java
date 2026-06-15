package com.vittae.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AgendamentoDTO {

	private String especialidade;
	private String observacoes;
	private LocalDate dataConsulta;
	private LocalTime hora;
	private Long medicoId;
	private PacienteDTO paciente;

	private String respNome;
	private String respCpf;
	private String respParentesco;
	private String respDataNascimento; // FIX: campo adicionado

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

	public String getRespDataNascimento() {
		return respDataNascimento;
	}

	public void setRespDataNascimento(String respDataNascimento) {
		this.respDataNascimento = respDataNascimento;
	}

	public static class PacienteDTO {
		private String nome;
		private String cpf;
		private String telefone;
		private String genero;
		private String nascimento; // JS deve mandar a chave "nascimento"

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
			return nascimento;
		}

		public void setNascimento(String nascimento) {
			this.nascimento = nascimento;
		}
	}
}