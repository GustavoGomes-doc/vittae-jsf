package com.vittae.view;

import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.Part;

import com.vittae.model.Disponibilidade;
import com.vittae.model.Medico;
import com.vittae.model.enums.DiaSemana;
import com.vittae.service.CadastrarMedicoService;

@Named
@ViewScoped
public class CadastrarMedicoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Medico dto;
	private CadastrarMedicoService service;
	private List<DiaUI> listaDias;
	private List<String> listaUfs;
	private Part foto;
	private String especialidadesSelecionadas;
	private String dataNascimentoStr;

	@PostConstruct
	public void init() {
		this.dto = new Medico();
		this.service = new CadastrarMedicoService();

		this.listaUfs = Arrays.asList("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
				"PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

		this.listaDias = new ArrayList<>();
		listaDias.add(new DiaUI("Segunda-feira", "SEGUNDA"));
		listaDias.add(new DiaUI("Terça-feira", "TERCA"));
		listaDias.add(new DiaUI("Quarta-feira", "QUARTA"));
		listaDias.add(new DiaUI("Quinta-feira", "QUINTA"));
		listaDias.add(new DiaUI("Sexta-feira", "SEXTA"));
		listaDias.add(new DiaUI("Sábado", "SABADO"));
	}

	public void salvar() {
	    FacesContext ctx = FacesContext.getCurrentInstance();
	    boolean valido = true;

	    // 1. Validação de idade mínima (24 anos)
	    if (dto.getDataNascimento() == null) {
	        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
	            "Data inválida", "A data de nascimento é obrigatória."));
	        valido = false;
	    } else {
	        long idade = ChronoUnit.YEARS.between(dto.getDataNascimento(), LocalTime.now());
	        if (idade < 24) {
	            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
	                "Médico muito jovem", "O profissional deve ter no mínimo 24 anos."));
	            valido = false;
	        }
	    }

	    // 2. Validação de CRM (apenas números, máx 6)
	    if (dto.getCrm() == null || !dto.getCrm().matches("\\d{1,6}")) {
	        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
	            "CRM inválido", "O CRM deve conter apenas números (máx. 6 dígitos)."));
	        valido = false;
	    }

	    // 3. Validação de Valor da Consulta (Dinheiro)
	    if (dto.getValorConsulta() == null || dto.getValorConsulta().compareTo(BigDecimal.ZERO) <= 0) {
	        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
	            "Valor inválido", "O valor da consulta deve ser maior que zero."));
	        valido = false;
	    } else if (dto.getValorConsulta().compareTo(new BigDecimal("1000.00")) > 0) {
	        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
	            "Valor excedido", "O valor da consulta não pode ultrapassar R$ 1.000,00."));
	        valido = false;
	    }
	    
	    // 4. Validação de Duração
	    if (dto.getTempoConsultaMinutos() == null || dto.getTempoConsultaMinutos() <= 20 || dto.getTempoConsultaMinutos() > 90) {
	        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
	            "Duração inválida", "A duração deve ser entre 1 e 999 minutos."));
	        valido = false;
	    }

	    // Se houver qualquer erro acima, para a execução aqui
	    if (!valido) {
	        return;
	    }

	    try {
	        // Foto
	        if (foto != null) {
	            InputStream input = foto.getInputStream();
	            dto.setFoto(input.readAllBytes());
	        }

	        // Especialidades
	        if (especialidadesSelecionadas != null && !especialidadesSelecionadas.isEmpty()) {
	            List<String> listaEsp = Arrays.stream(especialidadesSelecionadas.split(","))
	                .map(String::trim)
	                .collect(Collectors.toList());
	            dto.setEspecialidades(listaEsp);
	        }

	        // Disponibilidades
	        DateTimeFormatter parser = DateTimeFormatter.ofPattern("HH:mm");
	        List<Disponibilidade> disponibilidades = listaDias.stream()
	            .filter(DiaUI::isSelecionado)
	            .map(dia -> {
	                Disponibilidade d = new Disponibilidade();
	                d.setDiaSemana(DiaSemana.valueOf(dia.getValorEnum()));
	                if (dia.getHoraInicio() != null && !dia.getHoraInicio().isEmpty())
	                    d.setHoraInicio(LocalTime.parse(dia.getHoraInicio(), parser));
	                if (dia.getHoraFim() != null && !dia.getHoraFim().isEmpty())
	                    d.setHoraFim(LocalTime.parse(dia.getHoraFim(), parser));
	                d.setMedico(dto);
	                return d;
	            }).collect(Collectors.toList());
	        dto.setDisponibilidades(disponibilidades);

	        // Salva UMA única vez
	        service.salvarMedico(dto);

	        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
	            "Sucesso!", "Médico cadastrado com sucesso."));

	        init(); // limpa o formulário

	    } catch (Exception e) {
	        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
	            "Erro", "Erro ao processar dados: " + e.getMessage()));
	    }
	}

	public Medico getDto() {
		return dto;
	}

	public void setDto(Medico dto) {
		this.dto = dto;
	}

	public List<DiaUI> getListaDias() {
		return listaDias;
	}

	public void setListaDias(List<DiaUI> listaDias) {
		this.listaDias = listaDias;
	}

	public List<String> getListaUfs() {
		return listaUfs;
	}

	public void setListaUfs(List<String> listaUfs) {
		this.listaUfs = listaUfs;
	}

	public Part getFoto() {
		return foto;
	}

	public void setFoto(Part foto) {
		this.foto = foto;
	}

	public String getEspecialidadesSelecionadas() {
		return especialidadesSelecionadas;
	}

	public void setEspecialidadesSelecionadas(String especialidadesSelecionadas) {
		this.especialidadesSelecionadas = especialidadesSelecionadas;
	}

	public String getDataNascimentoStr() {
		return dataNascimentoStr;
	}

	public void setDataNascimentoStr(String dataNascimentoStr) {
		this.dataNascimentoStr = dataNascimentoStr;
	}

	public static class DiaUI {
		private String label;
		private String valorEnum;
		private boolean selecionado;
		private String horaInicio;
		private String horaFim;

		public DiaUI(String label, String valorEnum) {
			this.label = label;
			this.valorEnum = valorEnum;
		}

		public String getLabel() {
			return label;
		}

		public String getValorEnum() {
			return valorEnum;
		}

		public boolean isSelecionado() {
			return selecionado;
		}

		public void setSelecionado(boolean selecionado) {
			this.selecionado = selecionado;
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