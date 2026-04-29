package com.vittae.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.vittae.model.enums.DiaSemana;

public class Disponibilidade {

	private Long id;

	private LocalTime horaInicio;
	private LocalTime horaFim;

	private DiaSemana diaSemana;
	
	@JsonBackReference
	private Medico medico;

	public Disponibilidade() {
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}

	public LocalTime getHoraFim() {
		return horaFim;
	}

	public void setHoraFim(LocalTime horaFim) {
		this.horaFim = horaFim;
	}

	public DiaSemana getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }

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
}