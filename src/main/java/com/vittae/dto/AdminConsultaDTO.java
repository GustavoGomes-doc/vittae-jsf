package com.vittae.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminConsultaDTO {
	private Long id;
	private String nomePaciente;
	private String nomeMedico;
	private String especialidade;
	private LocalDate dataConsulta;
	private LocalTime hora;
	private String status;
	private BigDecimal valorConsulta;
	private Long pacienteId;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String v) {
		this.status = v;
	}

	public BigDecimal getValorConsulta() {
		return valorConsulta;
	}

	public void setValorConsulta(BigDecimal v) {
		this.valorConsulta = v;
	}

	public Long getPacienteId() {
		return pacienteId;
	}

	public void setPacienteId(Long v) {
		this.pacienteId = v;
	}
}