package com.vittae.model.dao;

import com.vittae.model.Medico; // Supondo que sua entidade chame Medico
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.List;

public class MedicoDao implements Serializable {

    // O CDI vai lá no EntityManagerProducer e traz a conexão pra cá automaticamente!
    @Inject
    private EntityManager em;

    public void salvar(Medico medico) {
        EntityTransaction trx = em.getTransaction();
        
        try {
            trx.begin(); // Inicia a transação com o banco
            
            if (medico.getId() == null) {
                em.persist(medico); // Se não tem ID, é um INSERT (Novo médico)
            } else {
                em.merge(medico); // Se já tem ID, é um UPDATE (Atualizando médico)
            }
            
            trx.commit(); // Salva de fato no banco de dados
            
        } catch (Exception e) {
            if (trx != null && trx.isActive()) {
                trx.rollback(); // Se der erro, desfaz tudo para não corromper o banco
            }
            throw e; // Joga o erro para cima (para o Service ver o que aconteceu)
        }
    }

    // Exemplo de uma busca (Equivalente ao findAll() do Spring)
    public List<Medico> buscarTodos() {
        // Usa JPQL para buscar os dados
        return em.createQuery("from Medico", Medico.class).getResultList();
    }
}