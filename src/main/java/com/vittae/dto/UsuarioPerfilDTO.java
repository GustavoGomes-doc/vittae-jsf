package com.vittae.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsuarioPerfilDTO {
	private Long id;
	private String nome;
	private String cpf;
	private String email;
	private String telefone;
	private String perfil;
	private LocalDate dataNascimento;

	// Campos exclusivos do médico
	private String crm;
	private String ufCrm;
	private BigDecimal valorConsulta;
	private Integer tempoConsultaMinutos;
	private List<EspecialidadeDTO> especialidades;

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getPerfil() {
		return perfil;
	}

	public void setPerfil(String perfil) {
		this.perfil = perfil;
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

	public BigDecimal getValorConsulta() {
		return valorConsulta;
	}

	public void setValorConsulta(BigDecimal valorConsulta) {
		this.valorConsulta = valorConsulta;
	}

	public Integer getTempoConsultaMinutos() {
		return tempoConsultaMinutos;
	}

	public void setTempoConsultaMinutos(Integer tempoConsultaMinutos) {
		this.tempoConsultaMinutos = tempoConsultaMinutos;
	}

	public List<EspecialidadeDTO> getEspecialidades() {
		return especialidades;
	}

	public void setEspecialidades(List<EspecialidadeDTO> especialidades) {
		this.especialidades = especialidades;
	}

	public static class EspecialidadeDTO {
		private Long id;
		private String nome;
		private String descricao;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public String getDescricao() {
			return descricao;
		}

		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
	}
}