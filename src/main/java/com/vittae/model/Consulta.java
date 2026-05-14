package com.vittae.model;

import com.vittae.model.enums.Status;
import java.time.LocalDate;
import java.time.LocalTime;

// Classes puras, prontas para virar JSON
public class Consulta {

	private Long id;
	private Medico medico;
	private Paciente paciente;
	private Status status;
	private LocalDate dataAgendado;
	private LocalDate dataConsulta;
	private LocalTime hora;

	public Consulta() {
	}
	public Consulta(Medico medico, Paciente paciente, Status status, LocalDate dataAgendado, LocalDate dataConsulta,
			LocalTime hora) {
		this.medico = medico;
		this.paciente = paciente;
		this.status = status;
		this.dataAgendado = dataAgendado;
		this.dataConsulta = dataConsulta;
		this.hora = hora;
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
}