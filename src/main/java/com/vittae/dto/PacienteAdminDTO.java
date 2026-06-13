package com.vittae.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PacienteAdminDTO {
	private Long id;
	private String nome;
	private String cpf;
	private String email;
	private String telefone;
	private LocalDate dataNascimento;
	private String genero;
	private long qtdConsultas;

	public Long getId() {
		return id;
	}

	public void setId(Long v) {
		this.id = v;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String v) {
		this.email = v;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String v) {
		this.telefone = v;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate v) {
		this.dataNascimento = v;
	}

	public String getGenero() {
		return genero;
	}

	public void setGenero(String v) {
		this.genero = v;
	}

	public long getQtdConsultas() {
		return qtdConsultas;
	}

	public void setQtdConsultas(long v) {
		this.qtdConsultas = v;
	}
}