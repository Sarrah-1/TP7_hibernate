package com.example.service;

import com.example.model.Auteur;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;

public class PerformanceTestService {

    private final EntityManagerFactory emf;
    private final Statistics statistics;

    public PerformanceTestService(EntityManagerFactory emf) {
        this.emf = emf;
        this.statistics = emf.unwrap(SessionFactory.class).getStatistics();
    }

    public void testPerformanceSansCache() {
        preparer();
        EntityManager em = creerEntityManagerSansCache();
        try {
            long debut = System.nanoTime();
            TypedQuery<Auteur> query = em.createQuery("SELECT a FROM Auteur a", Auteur.class);
            List<Auteur> auteurs = query.getResultList();
            int livres = compterLivres(auteurs);
            afficher("Durée sans cache (problème N+1)", debut, auteurs.size(), livres);
        } finally {
            em.close();
        }
    }

    public void testPerformanceAvecJoinFetch() {
        preparer();
        EntityManager em = creerEntityManagerSansCache();
        try {
            long debut = System.nanoTime();
            TypedQuery<Auteur> query = em.createQuery(
                    "SELECT DISTINCT a FROM Auteur a JOIN FETCH a.livres", Auteur.class);
            List<Auteur> auteurs = query.getResultList();
            int livres = compterLivres(auteurs);
            afficher("Durée avec JOIN FETCH", debut, auteurs.size(), livres);
        } finally {
            em.close();
        }
    }

    public void testPerformanceAvecEntityGraph() {
        preparer();
        EntityManager em = creerEntityManagerSansCache();
        try {
            long debut = System.nanoTime();
            EntityGraph<?> graph = em.getEntityGraph("Auteur.livres");
            TypedQuery<Auteur> query = em.createQuery("SELECT DISTINCT a FROM Auteur a", Auteur.class);
            query.setHint("javax.persistence.fetchgraph", graph);
            List<Auteur> auteurs = query.getResultList();
            int livres = compterLivres(auteurs);
            afficher("Durée avec Entity Graph", debut, auteurs.size(), livres);
        } finally {
            em.close();
        }
    }

    public void testPerformanceAvecCache() {
        preparer();
        lireAvecCache("Durée avec cache (lecture 1 : depuis la base)");
        lireAvecCache("Durée avec cache (lecture 2 : depuis le cache)");
    }

    private void lireAvecCache(String titre) {
        statistics.clear();
        EntityManager em = emf.createEntityManager();
        try {
            long debut = System.nanoTime();
            TypedQuery<Auteur> query = em.createQuery(
                    "SELECT DISTINCT a FROM Auteur a JOIN FETCH a.livres", Auteur.class);
            query.setHint("org.hibernate.cacheable", true);
            List<Auteur> auteurs = query.getResultList();
            int livres = compterLivres(auteurs);
            afficher(titre, debut, auteurs.size(), livres);
        } finally {
            em.close();
        }
    }

    private void preparer() {
        emf.getCache().evictAll();
        statistics.clear();
    }

    private EntityManager creerEntityManagerSansCache() {
        EntityManager em = emf.createEntityManager();
        em.setProperty("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
        em.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
        return em;
    }

    private int compterLivres(List<Auteur> auteurs) {
        int total = 0;
        for (Auteur auteur : auteurs) {
            total += auteur.getLivres().size();
        }
        return total;
    }

    private void afficher(String titre, long debut, int nbAuteurs, int nbLivres) {
        double duree = (System.nanoTime() - debut) / 1_000_000.0;
        System.out.println();
        System.out.println(titre + " : " + String.format("%.2f", duree) + " ms");
        System.out.println("Auteurs : " + nbAuteurs + " | Livres : " + nbLivres);
        System.out.println("Requêtes SQL exécutées : " + statistics.getPrepareStatementCount());
        System.out.println("Cache 2e niveau : hits = " + statistics.getSecondLevelCacheHitCount()
                + ", misses = " + statistics.getSecondLevelCacheMissCount()
                + ", puts = " + statistics.getSecondLevelCachePutCount());
        System.out.println("Cache de requêtes : hits = " + statistics.getQueryCacheHitCount()
                + ", misses = " + statistics.getQueryCacheMissCount());
    }
}
