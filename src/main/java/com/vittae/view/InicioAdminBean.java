package com.vittae.view;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.vittae.dto.AdminConsultaDTO;
import com.vittae.model.Consulta;
import com.vittae.model.Usuario;
import com.vittae.model.enums.Perfil;
import com.vittae.model.enums.Status;
import com.vittae.service.ConsultaService;
import com.vittae.service.UsuarioService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Named("inicioAdminBean")
@RequestScoped
public class InicioAdminBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private UsuarioService usuarioService;

	@Inject
	private ConsultaService consultaService;

	private long totalMedicos;
	private long totalPacientes;
	private long consultasHoje;
	private long consultasPendentes;
	private long consultasConfirmadas;

	private List<Usuario> ultimosMedicos = new ArrayList<>();
	private List<AdminConsultaDTO> consultasRecentes = new ArrayList<>();

	@PostConstruct
	public void inicializar() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
				.getExternalContext().getSession(false);
		String token = session != null ? (String) session.getAttribute("token") : null;

		this.totalMedicos = usuarioService.contarPorPerfilComToken(Perfil.MEDICO, token);
		this.totalPacientes = usuarioService.contarPorPerfilComToken(Perfil.PACIENTE, token);
		this.ultimosMedicos = usuarioService.buscarUltimosMedicosComToken(5, token);
		carregarConsultas(token);
	}

	private void carregarConsultas(String token) {
		try {
			List<AdminConsultaDTO> consultas = consultaService.listarTodasAdmin(token);
			this.consultasHoje = consultas.stream()
					.filter(c -> LocalDate.now().equals(c.getDataConsulta()))
					.count();
			this.consultasPendentes = consultas.stream()
					.filter(c -> "PENDENTE".equals(c.getStatus()))
					.count();
			this.consultasConfirmadas = consultas.stream()
					.filter(c -> "REALIZADA".equals(c.getStatus()))
					.count();
			this.consultasRecentes = consultas.stream()
					.sorted(Comparator.comparing(AdminConsultaDTO::getDataConsulta,
							Comparator.nullsLast(Comparator.reverseOrder())))
					.limit(5)
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Erro ao carregar consultas: " + e.getMessage());
			this.consultasHoje = 0;
			this.consultasPendentes = 0;
			this.consultasConfirmadas = 0;
			this.consultasRecentes = new ArrayList<>();
		}
	}
}