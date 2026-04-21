	package com.vittae.service;
	
	import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.mindrot.jbcrypt.BCrypt;          // substitui o BCryptPasswordEncoder do Spring Security

import com.vittae.dto.CadastrarMedicoDTO;
import com.vittae.model.Disponibilidade;
import com.vittae.model.Especialidade;
import com.vittae.model.Medico;
import com.vittae.model.enums.DiaSemana;
	
	@ApplicationScoped              // substitui @Service do Spring
	public class CadastrarMedicoService {
	
	    @Inject
	    private EntityManager em;   // substitui o Repository do Spring Data
	
	    // ── SALVAR ────────────────────────────────────────────────────────────
	    
	    public Medico salvarDTO(CadastrarMedicoDTO dto) {
	        EntityTransaction tx = em.getTransaction();
	        try {
	            tx.begin();

	            Medico medico = new Medico();
	            medico.setNome(dto.getNome());
	            medico.setCpf(dto.getCpf());
	            medico.setEmail(dto.getEmail());
	            medico.setSenha(BCrypt.hashpw(dto.getSenha(), BCrypt.gensalt()));
	            medico.setDataNascimento(dto.getDataNascimento());
	            medico.setCrm(dto.getCrm());
	            medico.setUfCrm(dto.getUfCrm());
	            medico.setRqe(dto.getRqe());
	            medico.setCep(dto.getCep());
	            medico.setValorConsulta(dto.getValorConsulta());
	            medico.setTempoConsultaMinutos(dto.getTempoConsultaMinutos());
	            medico.setBiografia(dto.getBiografia());
	            if (dto.getFoto() != null) medico.setFoto(dto.getFoto());

	            if (dto.getEspecialidades() != null) {
	                List<Especialidade> especialidades = new ArrayList<>();
	                for (String nomeEsp : dto.getEspecialidades()) {
	                    List<Especialidade> resultado = em.createQuery(
	                        "SELECT e FROM Especialidade e WHERE e.nome = :nome", Especialidade.class)
	                        .setParameter("nome", nomeEsp)
	                        .getResultList();

	                    Especialidade esp;
	                    if (!resultado.isEmpty()) {
	                        esp = resultado.get(0);
	                    } else {
	                        esp = new Especialidade();
	                        esp.setNome(nomeEsp);
	                        em.persist(esp);
	                    }
	                    especialidades.add(esp);
	                }
	                medico.setEspecialidades(especialidades);
	            }

	            em.persist(medico);
	            em.flush();

	            if (dto.getDisponibilidades() != null) {
	                List<Disponibilidade> disponibilidades = new ArrayList<>();
	                for (CadastrarMedicoDTO.DisponibilidadeDTO dtoDisp : dto.getDisponibilidades()) {
	                    Disponibilidade disp = new Disponibilidade();
	                    disp.setDiaSemana(DiaSemana.valueOf(dtoDisp.getDiaSemana()));
	                    disp.setHoraInicio(dtoDisp.getHoraInicio());
	                    disp.setHoraFim(dtoDisp.getHoraFim());
	                    disp.setMedico(medico);
	                    disponibilidades.add(disp);
	                    em.persist(disp);
	                }
	                medico.setDisponibilidades(disponibilidades);
	            }

	            tx.commit();
	            return medico;

	        } catch (Exception e) {
	            if (tx.isActive()) tx.rollback();
	            throw e;
	        }
	    }
	
	    // ── LISTAR — substitui cadastrarMedicoRepository.findAll() ────────────
	
	    public List<Medico> listarTodos() {
	        return em.createQuery("SELECT m FROM Medico m", Medico.class).getResultList();
	    }
	
	    // ── BUSCAR POR ID — substitui cadastrarMedicoRepository.findById() ────
	
	    public Optional<Medico> buscarPorId(Long id) {
	        Medico medico = em.find(Medico.class, id);
	        return Optional.ofNullable(medico);
	    }
	
	    // ── ATUALIZAR — igual ao seu código, só troca o repository ───────────
	
	 // ── ATUALIZAR ────────────────────────────────────────────────────────
	    public Medico atualizar(Long id, Medico dadosNovos) {
	        EntityTransaction tx = em.getTransaction();
	        try {
	            tx.begin();
	            Medico medico = em.find(Medico.class, id);
	            if (medico == null) throw new RuntimeException("Médico não encontrado");

	            if (dadosNovos.getNome() != null)  medico.setNome(dadosNovos.getNome());
	            if (dadosNovos.getCrm() != null)   medico.setCrm(dadosNovos.getCrm());
	            if (dadosNovos.getEmail() != null) medico.setEmail(dadosNovos.getEmail());

	            Medico resultado = em.merge(medico);
	            tx.commit();
	            return resultado;
	        } catch (Exception e) {
	            if (tx.isActive()) tx.rollback();
	            throw e;
	        }
	    }

	    // ── DELETAR ──────────────────────────────────────────────────────────
	    public void deletar(Long id) {
	        EntityTransaction tx = em.getTransaction();
	        try {
	            tx.begin();
	            Medico medico = em.find(Medico.class, id);
	            if (medico != null) em.remove(medico);
	            tx.commit();
	        } catch (Exception e) {
	            if (tx.isActive()) tx.rollback();
	            throw e;
	        }
	    }
	}	  