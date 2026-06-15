package com.vittae.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsultaAdminDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String nomePaciente;
	private String nomeMedico;
	private String especialidade;
	private String dataConsulta; 
	private String hora;
	private String status;
	private BigDecimal valorConsulta;
	private String observacoes;
	private Long pacienteId;

	// Responsável (menor de idade)
	private String respNome;
	private String respCpf;
	private String respParentesco;
	private String respDataNascimento;

	// ── Getters e Setters ───────────────────────────────────────────────────

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomePaciente() {
		return nomePaciente;
	}

	public void setNomePaciente(String v) {
		this.nomePaciente = v;
	}

	public String getNomeMedico() {
		return nomeMedico;
	}

	public void setNomeMedico(String v) {
		this.nomeMedico = v;
	}

	public String getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(String v) {
		this.especialidade = v;
	}

	public String getDataConsulta() {
		return dataConsulta;
	}

	public void setDataConsulta(String v) {
		this.dataConsulta = v;
	}

	public String getHora() {
		return hora;
	}

	public void setHora(String v) {
		this.hora = v;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public BigDecimal getValorConsulta() {
		return valorConsulta;
	}

	public void setValorConsulta(BigDecimal v) {
		this.valorConsulta = v;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String v) {
		this.observacoes = v;
	}

	public Long getPacienteId() {
		return pacienteId;
	}

	public void setPacienteId(Long v) {
		this.pacienteId = v;
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

	public String getRespDataNascimento() {
		return respDataNascimento;
	}

	public void setRespDataNascimento(String v) {
		this.respDataNascimento = v;
	}
}