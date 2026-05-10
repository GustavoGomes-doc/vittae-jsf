package com.vittae.model;

public class Especialidade {

	private Long id;
	private String nome;
	//private List<Medico> medicos;

	public Especialidade() {
	}

	public Especialidade(String nome) {
		this.nome = nome;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	//public List<Medico> getMedicos() {
		//return medicos;
	//}

	//public void setMedicos(List<Medico> medicos) {
	//	this.medicos = medicos;
	//}
}