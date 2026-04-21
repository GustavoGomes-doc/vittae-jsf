package com.vittae.model;

import java.time.LocalDate;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "medico")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Medico extends Usuario {
    // resto do código igual...

	@Lob
	private byte[] foto;
	
	@Column(columnDefinition = "TEXT")
	private String biografia;
	
	@Column(nullable = false)
	private String crm;
	
	@Column(length = 2)
	private String ufCrm;
	
	private String rqe;
	
	private int tempoConsultaMinutos;
	private LocalDate dataNascimento;
	private String cep;
	private double valorConsulta; 
	private String telefone;

	@ManyToMany
	@JoinTable(name = "medico_especialidade", joinColumns = @JoinColumn(name = "id_medico"), inverseJoinColumns = @JoinColumn(name = "id_especialidade"))
	private List<Especialidade> especialidades;

	@OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Disponibilidade> disponibilidades;

	@OneToMany(mappedBy = "medico")
	private List<Consulta> consultas;

	public Medico() {
	}

	public Medico(byte[] foto, LocalDate dataNascimento, String crm, String cep, double valorConsulta, String ufCrm, int tempoConsultaMinutos, String rqe, String biografia, List<Especialidade> especialidades, String telefone) {
		this.foto = foto;
		this.dataNascimento = dataNascimento;
		this.crm = crm;
		this.cep = cep;
		this.valorConsulta = valorConsulta;
		this.ufCrm = ufCrm;
		this.rqe = rqe;
		this.biografia = biografia;
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

	public String getBiografia() {
		return biografia;
	}

	public void setBiografia(String biografia) {
		this.biografia = biografia;
	}

	public String getUfCrm() {
		return ufCrm;
	}

	public void setUfCrm(String ufCrm) {
		this.ufCrm = ufCrm;
	}

	public String getRqe() {
		return rqe;
	}

	public void setRqe(String rqe) {
		this.rqe = rqe;
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

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public double getValorConsulta() {
		return valorConsulta;
	}

	public void setValorConsulta(double valorConsulta) {
		this.valorConsulta = valorConsulta;
	}

	public List<Especialidade> getEspecialidades() {
		return especialidades;
	}

	public void setEspecialidades(List<Especialidade> especialidades) {
		this.especialidades = especialidades;
	}

	public List<Disponibilidade> getDisponibilidades() {
		return disponibilidades;
	}

	public void setDisponibilidades(List<Disponibilidade> disponibilidades) {
		this.disponibilidades = disponibilidades;
	}

	public List<Consulta> getConsultas() {
		return consultas;
	}

	public void setConsultas(List<Consulta> consultas) {
		this.consultas = consultas;
	}
}