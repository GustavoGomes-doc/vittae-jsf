package com.vittae.model;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "paciente")
@PrimaryKeyJoinColumn(name = "id_usuario")
public class Paciente extends Usuario {

	private LocalDate dataNascimento;
	private String telefone;
	private String endereco;
	private String cidade;

	@OneToOne
	@JoinColumn(name = "id_responsavel")
	private Paciente responsavel;

	@OneToMany(mappedBy = "paciente")
	private List<Consulta> consultas;

	public Paciente() {
	}

	public Paciente(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public Paciente getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(Paciente responsavel) {
		this.responsavel = responsavel;
	}

	public List<Consulta> getConsultas() {
		return consultas;
	}

	public void setConsultas(List<Consulta> consultas) {
		this.consultas = consultas;
	}
}