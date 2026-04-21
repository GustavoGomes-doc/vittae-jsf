package com.vittae.dto;

import java.time.LocalDate;
import java.util.List;

public class CadastrarMedicoDTO {

	private String nome;
	private String cpf;
	private String email;
	private String senha;
	private LocalDate dataNascimento;
	private String crm;
	private String ufCrm;
	private String rqe;
	private String cep;
	private String telefone;
	private double valorConsulta;
	private Integer tempoConsultaMinutos;
	private String biografia;
	private List<String> especialidades;
	private List<DisponibilidadeDTO> disponibilidades;

	private byte[] foto;

	// E crie os getters e setters:
	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}

	// Getters e Setters
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getCrm() {
		return crm;
	}

	public void setCrm(String crm) {
		this.crm = crm;
	}

	public String getUfCrm() {
		return ufCrm;
	}

	public void setUfCrm(String ufCrm) {
		this.ufCrm = ufCrm;
	}

	public String getRqe() {
		return rqe;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public void setRqe(String rqe) {
		this.rqe = rqe;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public double getValorConsulta() {
		return valorConsulta;
	}

	public void setValorConsulta(double valorConsulta) {
		this.valorConsulta = valorConsulta;
	}

	public Integer getTempoConsultaMinutos() {
		return tempoConsultaMinutos;
	}

	public void setTempoConsultaMinutos(Integer tempoConsultaMinutos) {
		this.tempoConsultaMinutos = tempoConsultaMinutos;
	}

	public String getBiografia() {
		return biografia;
	}

	public void setBiografia(String biografia) {
		this.biografia = biografia;
	}

	public List<String> getEspecialidades() {
		return especialidades;
	}

	public void setEspecialidades(List<String> especialidades) {
		this.especialidades = especialidades;
	}

	public List<DisponibilidadeDTO> getDisponibilidades() {
		return disponibilidades;
	}

	public void setDisponibilidades(List<DisponibilidadeDTO> disponibilidades) {
		this.disponibilidades = disponibilidades;
	}

	// ── DTO interno ───────────────────────────────────────────
	public static class DisponibilidadeDTO {
		private String diaSemana;
		private String horaInicio;
		private String horaFim;

		public String getDiaSemana() {
			return diaSemana;
		}

		public void setDiaSemana(String diaSemana) {
			this.diaSemana = diaSemana;
		}

		public String getHoraInicio() {
			return horaInicio;
		}

		public void setHoraInicio(String horaInicio) {
			this.horaInicio = horaInicio;
		}

		public String getHoraFim() {
			return horaFim;
		}

		public void setHoraFim(String horaFim) {
			this.horaFim = horaFim;
		}

	}
}

//package com.vittae.dto;
//
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.*;
//import java.time.LocalDate;
//import java.util.List;
//
//public class CadastrarMedicoDTO {
//
//	@NotBlank(message = "Nome é obrigatório") // n nulo
//	@Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
//	private String nome;
//
//	@NotBlank(message = "CPF é obrigatório")
//	@Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 dígitos numéricos") // valida com regex
//	private String cpf;
//
//	@NotBlank(message = "Email é obrigatório")
//	@Email(message = "Email inválido") // validacao email jararta
//	private String email;
//
//	@NotBlank(message = "Senha é obrigatória")
//	@Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
//	private String senha;
//
//
//	@NotNull(message = "Data de nascimento é obrigatória") // n nulo
//	@Past(message = "Data de nascimento deve ser uma data no passado") // garante que a data de nasc é no passado
//	private LocalDate dataNascimento;
//
//	@NotBlank(message = "CRM é obrigatório")
//	@Pattern(regexp = "\\d{4,6}", message = "CRM deve conter entre 4 e 6 dígitos")
//	private String crm;
//
//	@NotBlank(message = "UF do CRM é obrigatória")
//	@Size(min = 2, max = 2, message = "UF deve ter exatamente 2 caracteres")
//	private String ufCrm;
//
//	// rqe é opcional, mas se vier deve ter formato válido
//	@Pattern(regexp = "\\d+", message = "RQE deve conter apenas números")
//	private String rqe;
//
//	@NotBlank(message = "CEP é obrigatório")
//	@Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 dígitos")
//	private String cep;
//
//	@Positive(message = "Valor da consulta deve ser maior que zero") // > que 0
//	private double valorConsulta;
//
//	@NotNull(message = "Tempo de consulta é obrigatório")
//	@Min(value = 15, message = "Tempo mínimo de consulta é 15 minutos")
//	@Max(value = 240, message = "Tempo máximo de consulta é 240 minutos")
//	private Integer tempoConsultaMinutos;
//
//	@Size(max = 1000, message = "Biografia deve ter no máximo 1000 caracteres")
//	private String biografia;
//
//
//	@NotEmpty(message = "Informe ao menos uma especialidade") // lista n pode ser nula nem vazia
//	private List<String> especialidades;
//
//	@NotEmpty(message = "Informe ao menos uma disponibilidade")
//	@Valid // faz a validação entrar dentro de cada DisponibilidadeDTO da lista
//	private List<DisponibilidadeDTO> disponibilidades;
//
//
//	public String getNome() {
//		return nome;
//	}
//
//	public void setNome(String nome) {
//		this.nome = nome;
//	}
//
//	public String getCpf() {
//		return cpf;
//	}
//
//	public void setCpf(String cpf) {
//		this.cpf = cpf;
//	}
//
//	public String getEmail() {
//		return email;
//	}
//
//	public void setEmail(String email) {
//		this.email = email;
//	}
//
//	public String getSenha() {
//		return senha;
//	}
//
//	public void setSenha(String senha) {
//		this.senha = senha;
//	}
//
//	public LocalDate getDataNascimento() {
//		return dataNascimento;
//	}
//
//	public void setDataNascimento(LocalDate dataNascimento) {
//		this.dataNascimento = dataNascimento;
//	}
//
//	public String getCrm() {
//		return crm;
//	}
//
//	public void setCrm(String crm) {
//		this.crm = crm;
//	}
//
//	public String getUfCrm() {
//		return ufCrm;
//	}
//
//	public void setUfCrm(String ufCrm) {
//		this.ufCrm = ufCrm;
//	}
//
//	public String getRqe() {
//		return rqe;
//	}
//
//	public void setRqe(String rqe) {
//		this.rqe = rqe;
//	}
//
//	public String getCep() {
//		return cep;
//	}
//
//	public void setCep(String cep) {
//		this.cep = cep;
//	}
//
//	public double getValorConsulta() {
//		return valorConsulta;
//	}
//
//	public void setValorConsulta(double valorConsulta) {
//		this.valorConsulta = valorConsulta;
//	}
//
//	public Integer getTempoConsultaMinutos() {
//		return tempoConsultaMinutos;
//	}
//
//	public void setTempoConsultaMinutos(Integer tempoConsultaMinutos) {
//		this.tempoConsultaMinutos = tempoConsultaMinutos;
//	}
//
//	public String getBiografia() {
//		return biografia;
//	}
//
//	public void setBiografia(String biografia) {
//		this.biografia = biografia;
//	}
//
//	public List<String> getEspecialidades() {
//		return especialidades;
//	}
//
//	public void setEspecialidades(List<String> especialidades) {
//		this.especialidades = especialidades;
//	}
//
//	public List<DisponibilidadeDTO> getDisponibilidades() {
//		return disponibilidades;
//	}
//
//	public void setDisponibilidades(List<DisponibilidadeDTO> disponibilidades) {
//		this.disponibilidades = disponibilidades;
//	}
//
//	// ── DTO interno ───────────────────────────────────────────
//
//	public static class DisponibilidadeDTO {
//
//		@NotBlank(message = "Dia da semana é obrigatório")
//		@Pattern(regexp = "SEGUNDA|TERCA|QUARTA|QUINTA|SEXTA|SABADO|DOMINGO", message = "Dia inválido. Use: SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO ou DOMINGO")
//		private String diaSemana;
//
//		@NotBlank(message = "Hora de início é obrigatória")
//		@Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "Hora de início inválida. Use o formato HH:mm")
//		private String horaInicio;
//
//		@NotBlank(message = "Hora de fim é obrigatória")
//		@Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "Hora de fim inválida. Use o formato HH:mm")
//		private String horaFim;
//
//		public String getDiaSemana() {
//			return diaSemana;
//		}
//
//		public void setDiaSemana(String diaSemana) {
//			this.diaSemana = diaSemana;
//		}
//
//		public String getHoraInicio() {
//			return horaInicio;
//		}
//
//		public void setHoraInicio(String horaInicio) {
//			this.horaInicio = horaInicio;
//		}
//
//		public String getHoraFim() {
//			return horaFim;
//		}
//
//		public void setHoraFim(String horaFim) {
//			this.horaFim = horaFim;
//		}
//	}
//}