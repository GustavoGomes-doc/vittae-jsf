package com.vittae.model;

import com.vittae.model.enums.DiaSemana;

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
@Table(name = "disponibilidade")
public class Disponibilidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String horaInicio;
	private String horaFim;

	@Enumerated(EnumType.STRING)
	private DiaSemana diaSemana;

	@ManyToOne
	@JoinColumn(name = "id_medico")
	private Medico medico;

	public Disponibilidade() {
	}

	public Disponibilidade(String horaInicio, String horaFim, DiaSemana diaSemana, Medico medico) {
		this.horaInicio = horaInicio;
		this.horaFim = horaFim;
		this.diaSemana = diaSemana;
		this.medico = medico;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public DiaSemana getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(DiaSemana diaSemana) {
		this.diaSemana = diaSemana;
	}

	public Medico getMedico() {
		return medico;
	}

	public void setMedico(Medico medico) {
		this.medico = medico;
	}
}