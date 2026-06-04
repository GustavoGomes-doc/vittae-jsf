package com.vittae.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.vittae.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vittae.model.enums.Status;

// Classes puras, prontas para virar JSON
@JsonIgnoreProperties(ignoreUnknown = true)
public class Consulta {

	private Long id;
	private Medico medico;
	private Paciente paciente;
	private Status status;
	private LocalDate dataConsulta;
	private LocalTime hora;
	private BigDecimal valorConsulta;
	private Especialidade especialidade;
	
	private String respNome;
	
	private String respCpf;
	
	private String respParentesco;
	

	public Consulta() {
	}

	public Consulta(Medico medico, Paciente paciente, Status status, LocalDate dataAgendado, LocalDate dataConsulta,
			BigDecimal valorConsulta, LocalTime hora, Especialidade especialidade, String respNome, String respCpf, String respParentesco) {
		this.medico = medico;
		this.paciente = paciente;
		this.status = status;
		this.dataConsulta = dataConsulta;
		this.hora = hora;
		this.valorConsulta = valorConsulta;
		this.especialidade = especialidade;
		this.respCpf = respCpf;
		this.respNome = respNome;
		this.respParentesco = respParentesco;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Medico getMedico() {
		return medico;
	}

	public BigDecimal getValorConsulta() {
		return valorConsulta;
	}

	public void setValorConsulta(BigDecimal valorConsulta) {
		this.valorConsulta = valorConsulta;
	}

	public void setMedico(Medico medico) {
		this.medico = medico;
	}

	public Paciente getPaciente() {
		return paciente;
	}

	public void setPaciente(Paciente paciente) {
		this.paciente = paciente;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
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

	public Especialidade getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(Especialidade especialidade) {
		this.especialidade = especialidade;
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