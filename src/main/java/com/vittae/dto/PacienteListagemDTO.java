package com.vittae.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vittae.model.Consulta;
import com.vittae.model.enums.Status;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PacienteListagemDTO {

	private Long id;
	private String nomePaciente;
	private LocalDate dataConsulta;
	private LocalTime hora;
	private String especialidade;
	private Status status;
	private String observacoes;
	private LocalDate dataNascimento;
	private int qtdConsultas;
	private String nomeResponsavel;
	private String telefonePaciente;

	public PacienteListagemDTO() {
	}

	public PacienteListagemDTO(Consulta c) {
		this.id = c.getId();
		this.nomePaciente = c.getPaciente() != null ? c.getPaciente().getNome() : "—";
		this.dataConsulta = c.getDataConsulta();
		this.hora = c.getHora();
		this.especialidade = c.getEspecialidade() != null ? c.getEspecialidade().getNome() : "—";
		this.status = c.getStatus();
		this.observacoes = c.getObservacoes();
		this.dataNascimento = c.getPaciente() != null ? c.getPaciente().getDataNascimento() : null;
		this.nomeResponsavel = c.getPaciente() != null && c.getPaciente().getResponsavel() != null
				? c.getPaciente().getResponsavel().getNome()
				: null;
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

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String v) {
		this.observacoes = v;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate v) {
		this.dataNascimento = v;
	}
	
	public int getQtdConsultas() {
		return qtdConsultas;
	}

	public void setQtdConsultas(int v) {
		this.qtdConsultas = v;
	}

	public String getNomeResponsavel() {
		return nomeResponsavel;
	}

	public void setNomeResponsavel(String v) {
		this.nomeResponsavel = v;
	}

	public String getTelefonePaciente() {
		return telefonePaciente;
	}

	public void setTelefonePaciente(String v) {
		this.telefonePaciente = v;
	}
}