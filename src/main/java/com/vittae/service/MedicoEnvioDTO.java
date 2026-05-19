package com.vittae.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MedicoEnvioDTO {

    private String nome;
    private String cpf;
    private String email;
    private String senha;
    private LocalDate dataNascimento;
    private String crm;
    private String ufCrm;
    private BigDecimal valorConsulta;
    private Integer tempoConsultaMinutos;
    private List<String> especialidades;
    private List<DisponibilidadeEnvioDTO> disponibilidades;
    public byte[] foto;


    public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getSenha() {
		return senha;
	}


	public void setSenha(String senha) {
		this.senha = senha;
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

	public String getUfCrm() {
		return ufCrm;
	}

	public void setUfCrm(String ufCrm) {
		this.ufCrm = ufCrm;
	}

	public BigDecimal getValorConsulta() {
		return valorConsulta;
	}

	public void setValorConsulta(BigDecimal valorConsulta) {
		this.valorConsulta = valorConsulta;
	}

	public Integer getTempoConsultaMinutos() {
		return tempoConsultaMinutos;
	}

	public void setTempoConsultaMinutos(Integer tempoConsultaMinutos) {
		this.tempoConsultaMinutos = tempoConsultaMinutos;
	}

	public List<String> getEspecialidades() {
		return especialidades;
	}

	public void setEspecialidades(List<String> especialidades) {
		this.especialidades = especialidades;
	}

	public List<DisponibilidadeEnvioDTO> getDisponibilidades() {
		return disponibilidades;
	}

	public void setDisponibilidades(List<DisponibilidadeEnvioDTO> disponibilidades) {
		this.disponibilidades = disponibilidades;
	}


	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}




	public static class DisponibilidadeEnvioDTO {
        private String diaSemana;
        private String horaInicio;
        private String horaFim;
        
        
		public String getDiaSemana() {
			return diaSemana;
		}
		public void setDiaSemana(String diaSemana) {
			this.diaSemana = diaSemana;
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

        
    }
}