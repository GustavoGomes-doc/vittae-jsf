package com.vittae.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vittae.model.enums.Perfil;


public class Medico extends Usuario {

	private byte[] foto;
	private String crm;
	private String ufCrm;
	private Integer tempoConsultaMinutos;
	private LocalDate dataNascimento;
	private BigDecimal valorConsulta; 
	private String telefone;
	
	@JsonManagedReference
	private List<String> especialidades;
	
	private List<Disponibilidade> disponibilidades;

	private List<Consulta> consultas;

	public Medico() {
		super();
		this.setPerfil(Perfil.MEDICO);
	}

	public Medico(byte[] foto, LocalDate dataNascimento, String crm, String cep, BigDecimal valorConsulta, String ufCrm, Integer tempoConsultaMinutos, List<String> especialidades, String telefone) {
		this.foto = foto;
		this.dataNascimento = dataNascimento;
		this.crm = crm;
		this.valorConsulta = valorConsulta;
		this.ufCrm = ufCrm;
		this.tempoConsultaMinutos = tempoConsultaMinutos;
		this.especialidades = especialidades;
		this.telefone = telefone;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}


	public String getUfCrm() {
		return ufCrm;
	}

	public void setUfCrm(String ufCrm) {
		this.ufCrm = ufCrm;
	}

	public Integer getTempoConsultaMinutos() {
		return tempoConsultaMinutos;
	}

	public void setTempoConsultaMinutos(Integer tempoConsultaMinutos) {
		this.tempoConsultaMinutos = tempoConsultaMinutos;
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getCrm() {
		return crm;
	}

	public void setCrm(String crm) {
		this.crm = crm;
	}
	
	public BigDecimal getValorConsulta() {
		return valorConsulta;
	}

	public void setValorConsulta(BigDecimal valorConsulta) {
		this.valorConsulta = valorConsulta;
	}

	public List<String> getEspecialidades() {
        return especialidades;
    }

	public void setEspecialidades(List<String> especialidades) {
        this.especialidades = especialidades;
    }

	public List<Disponibilidade> getDisponibilidades() {
		return disponibilidades;
	}

	public void setDisponibilidades(List<Disponibilidade> disponibilidades) {
		this.disponibilidades = disponibilidades;
	}

}