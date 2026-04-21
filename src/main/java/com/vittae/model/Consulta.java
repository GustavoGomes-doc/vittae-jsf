package com.vittae.model;

import com.vittae.model.enums.Status;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "consulta")
public class Consulta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_medico")
	private Medico medico;

	@ManyToOne
	@JoinColumn(name = "id_paciente")
	private Paciente paciente;

	@Enumerated(EnumType.STRING)
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