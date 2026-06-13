package com.vittae.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import com.vittae.model.Consulta;
import com.vittae.model.enums.Status;

public class PacienteListagemDTO {

	private Long id;
	private String nomePaciente;
	private LocalDate dataConsulta;
	private LocalTime hora;
	private String especialidade; 
	private Status status;
	
	public PacienteListagemDTO() {}

	public PacienteListagemDTO(Consulta c) {
		this.id = c.getId();
		this.nomePaciente = c.getPaciente() != null ? c.getPaciente().getNome() : "—";
		this.dataConsulta = c.getDataConsulta();
		this.hora = c.getHora();
		this.especialidade = c.getEspecialidade() != null ? c.getEspecialidade().getNome() : "—";
		this.status = c.getStatus();
	}

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

	public String getEspecialidade() {
		return especialidade;
	}

	public void setEspecialidade(String v) {
		this.especialidade = v;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status v) {
		this.status = v;
	}
}