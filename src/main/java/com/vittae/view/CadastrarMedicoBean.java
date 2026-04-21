package com.vittae.view;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

import com.vittae.dto.CadastrarMedicoDTO;
import com.vittae.dto.CadastrarMedicoDTO.DisponibilidadeDTO;
import com.vittae.model.Medico;
import com.vittae.service.CadastrarMedicoService;

@Named("cadastrarMedicoBean")
@ViewScoped
public class CadastrarMedicoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CadastrarMedicoService cadastrarMedicoService;

	private CadastrarMedicoDTO dto = new CadastrarMedicoDTO();
	private String especialidadesSelecionadas;
	private javax.servlet.http.Part foto;

	// ── Checkboxes de disponibilidade ────────────────────────
	private boolean dispSegunda;
	private boolean dispTerca;
	private boolean dispQuarta;
	private boolean dispQuinta;
	private boolean dispSexta;
	private boolean dispSabado;

	// ── Horários por dia ─────────────────────────────────────
	private String horaInicioSegunda;
	private String horaFimSegunda;

	private String horaInicioTerca;
	private String horaFimTerca;

	private String horaInicioQuarta;
	private String horaFimQuarta;

	private String horaInicioQuinta;
	private String horaFimQuinta;

	private String horaInicioSexta;
	private String horaFimSexta;

	private String horaInicioSabado;
	private String horaFimSabado;

	// ── Lista de UFs ─────────────────────────────────────────
	private final List<String> listaUfs = List.of("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT",
			"MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

	public String salvar() {
		try {
			// 1. TRATAMENTO DA FOTO
			if (this.foto != null && this.foto.getSize() > 0) {
			    // Verifica tamanho antes de tentar salvar (max 1MB)
			    if (this.foto.getSize() > 1_000_000) {
			        FacesContext.getCurrentInstance().addMessage(null,
			            new FacesMessage(FacesMessage.SEVERITY_WARN, 
			                "Foto muito grande (máx 1MB). Médico será cadastrado sem foto.", null));
			    } else {
			        try (InputStream is = this.foto.getInputStream();
			             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			            int nRead;
			            byte[] data = new byte[16384];
			            while ((nRead = is.read(data, 0, data.length)) != -1) {
			                buffer.write(data, 0, nRead);
			            }
			            dto.setFoto(buffer.toByteArray());
			        }
			    }
			}
			// 2. ESPECIALIDADES
			if (especialidadesSelecionadas != null && !especialidadesSelecionadas.isEmpty()) {
				dto.setEspecialidades(Arrays.asList(especialidadesSelecionadas.split(",")));
			}

			// 3. DISPONIBILIDADES — usa horários digitados pelo usuário
			List<DisponibilidadeDTO> disponibilidades = new ArrayList<>();
			if (dispSegunda)
				disponibilidades.add(criarDisp("SEGUNDA", horaInicioSegunda, horaFimSegunda));
			if (dispTerca)
				disponibilidades.add(criarDisp("TERCA", horaInicioTerca, horaFimTerca));
			if (dispQuarta)
				disponibilidades.add(criarDisp("QUARTA", horaInicioQuarta, horaFimQuarta));
			if (dispQuinta)
				disponibilidades.add(criarDisp("QUINTA", horaInicioQuinta, horaFimQuinta));
			if (dispSexta)
				disponibilidades.add(criarDisp("SEXTA", horaInicioSexta, horaFimSexta));
			if (dispSabado)
				disponibilidades.add(criarDisp("SABADO", horaInicioSabado, horaFimSabado));
			dto.setDisponibilidades(disponibilidades);

			// 4. SALVAR
			cadastrarMedicoService.salvarDTO(dto);

			// 5. SUCESSO
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Médico cadastrado com sucesso!", null));

			dto = new CadastrarMedicoDTO();
			especialidadesSelecionadas = null;
			return "/views/admin/TemplateAdmin?faces-redirect=true";

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao cadastrar: " + e.getMessage(), null));
			e.printStackTrace();
			return null;
		}
	}

	private DisponibilidadeDTO criarDisp(String dia, String inicio, String fim) {
		DisponibilidadeDTO d = new DisponibilidadeDTO();
		d.setDiaSemana(dia);
		d.setHoraInicio(inicio);
		d.setHoraFim(fim);
		return d;
	}

	public List<Medico> listar() {
		return cadastrarMedicoService.listarTodos();
	}

	// ── Getters e Setters ─────────────────────────────────────

	public CadastrarMedicoDTO getDto() {
		return dto;
	}

	public void setDto(CadastrarMedicoDTO dto) {
		this.dto = dto;
	}

	public String getEspecialidadesSelecionadas() {
		return especialidadesSelecionadas;
	}

	public void setEspecialidadesSelecionadas(String e) {
		this.especialidadesSelecionadas = e;
	}

	public javax.servlet.http.Part getFoto() {
		return foto;
	}

	public void setFoto(javax.servlet.http.Part foto) {
		this.foto = foto;
	}

	public List<String> getListaUfs() {
		return listaUfs;
	}

	// checkboxes
	public boolean isDispSegunda() {
		return dispSegunda;
	}

	public void setDispSegunda(boolean v) {
		this.dispSegunda = v;
	}

	public boolean isDispTerca() {
		return dispTerca;
	}

	public void setDispTerca(boolean v) {
		this.dispTerca = v;
	}

	public boolean isDispQuarta() {
		return dispQuarta;
	}

	public void setDispQuarta(boolean v) {
		this.dispQuarta = v;
	}

	public boolean isDispQuinta() {
		return dispQuinta;
	}

	public void setDispQuinta(boolean v) {
		this.dispQuinta = v;
	}

	public boolean isDispSexta() {
		return dispSexta;
	}

	public void setDispSexta(boolean v) {
		this.dispSexta = v;
	}

	public boolean isDispSabado() {
		return dispSabado;
	}

	public void setDispSabado(boolean v) {
		this.dispSabado = v;
	}

	// horários
	public String getHoraInicioSegunda() {
		return horaInicioSegunda;
	}

	public void setHoraInicioSegunda(String v) {
		this.horaInicioSegunda = v;
	}

	public String getHoraFimSegunda() {
		return horaFimSegunda;
	}

	public void setHoraFimSegunda(String v) {
		this.horaFimSegunda = v;
	}

	public String getHoraInicioTerca() {
		return horaInicioTerca;
	}

	public void setHoraInicioTerca(String v) {
		this.horaInicioTerca = v;
	}

	public String getHoraFimTerca() {
		return horaFimTerca;
	}

	public void setHoraFimTerca(String v) {
		this.horaFimTerca = v;
	}

	public String getHoraInicioQuarta() {
		return horaInicioQuarta;
	}

	public void setHoraInicioQuarta(String v) {
		this.horaInicioQuarta = v;
	}

	public String getHoraFimQuarta() {
		return horaFimQuarta;
	}

	public void setHoraFimQuarta(String v) {
		this.horaFimQuarta = v;
	}

	public String getHoraInicioQuinta() {
		return horaInicioQuinta;
	}

	public void setHoraInicioQuinta(String v) {
		this.horaInicioQuinta = v;
	}

	public String getHoraFimQuinta() {
		return horaFimQuinta;
	}

	public void setHoraFimQuinta(String v) {
		this.horaFimQuinta = v;
	}

	public String getHoraInicioSexta() {
		return horaInicioSexta;
	}

	public void setHoraInicioSexta(String v) {
		this.horaInicioSexta = v;
	}

	public String getHoraFimSexta() {
		return horaFimSexta;
	}

	public void setHoraFimSexta(String v) {
		this.horaFimSexta = v;
	}

	public String getHoraInicioSabado() {
		return horaInicioSabado;
	}

	public void setHoraInicioSabado(String v) {
		this.horaInicioSabado = v;
	}

	public String getHoraFimSabado() {
		return horaFimSabado;
	}

	public void setHoraFimSabado(String v) {
		this.horaFimSabado = v;
	}
}