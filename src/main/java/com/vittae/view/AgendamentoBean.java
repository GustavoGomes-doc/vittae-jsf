package com.vittae.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vittae.model.Medico;
import com.vittae.model.Paciente;
import com.vittae.model.enums.Status;


public class AgendamentoBean {

    private int id;
    private LocalDate dataAgendado;
    private LocalDate dataConsulta;
    private LocalTime hora;
    private BigDecimal valorConsulta;
    private Status status;
    
    
    

    @JsonIgnore // Evita o loop infinito com Paciente
    // @ManyToOne e @JoinColumn ficam aqui se usar JPA
    private Paciente paciente;

    @JsonIgnore // Evita o loop infinito com Médico
    // @ManyToOne e @JoinColumn ficam aqui se usar JPA
    private Medico medico;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public BigDecimal getValorConsulta() {
		return valorConsulta;
	}

	public void setValorConsulta(BigDecimal valorConsulta) {
		this.valorConsulta = valorConsulta;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

    
}