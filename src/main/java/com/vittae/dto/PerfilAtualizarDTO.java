package com.vittae.dto;

import java.math.BigDecimal;

public class PerfilAtualizarDTO {
	private String email;
	private String telefone;
	private String senhaAtual;
	private String novaSenha;
	private BigDecimal valorConsulta;
	private Integer tempoConsultaMinutos;

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

	public String getSenhaAtual() {
		return senhaAtual;
	}

	public void setSenhaAtual(String senhaAtual) {
		this.senhaAtual = senhaAtual;
	}

	public String getNovaSenha() {
		return novaSenha;
	}

	public void setNovaSenha(String novaSenha) {
		this.novaSenha = novaSenha;
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
}