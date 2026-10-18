package com.example;

import com.example.service.DataInitService;
import com.example.service.PerformanceTestService;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class App {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-cache-pu");
        try {
            DataInitService dataInitService = new DataInitService(emf);
            PerformanceTestService performanceTestService = new PerformanceTestService(emf);

            dataInitService.initData();

            System.out.println("Test sans cache");
            performanceTestService.testPerformanceSansCache();

            System.out.println();
            System.out.println("Test avec JOIN FETCH");
            performanceTestService.testPerformanceAvecJoinFetch();

            System.out.println();
            System.out.println("Test avec Entity Graph");
            performanceTestService.testPerformanceAvecEntityGraph();

            System.out.println();
            System.out.println("Test avec cache de second niveau");
            performanceTestService.testPerformanceAvecCache();
        } finally {
            emf.close();
        }
    }
}
