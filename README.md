# hibernate-cache-lab

TP 7 : activation du cache de second niveau, optimisation des performances et suppression du problème N+1 avec JOIN FETCH et Entity Graph.

## Technologies

- Java 8 ou supérieur
- Maven
- JPA 2.2 et Hibernate 5.4.32
- Cache de second niveau : Ehcache 3 via JCache (`hibernate-jcache`)
- Base de données embarquée H2 (en mémoire)

## Structure du projet

```
hibernate-cache-lab
├── pom.xml
└── src/main
    ├── java/com/example
    │   ├── App.java
    │   ├── model
    │   │   ├── Auteur.java
    │   │   └── Livre.java
    │   └── service
    │       ├── DataInitService.java
    │       └── PerformanceTestService.java
    └── resources
        ├── ehcache.xml
        └── META-INF/persistence.xml
```

## Ce que fait le projet

| Étape | Description |
|---|---|
| Entités | `Auteur` (OneToMany) et `Livre` (ManyToOne LAZY), relation bidirectionnelle |
| Données | 10 auteurs avec 5 livres chacun (`DataInitService`) |
| Test 1 | Sans cache : problème N+1 (1 requête pour les auteurs + 1 requête par auteur) |
| Test 2 | `JOIN FETCH` : une seule requête SQL |
| Test 3 | Entity Graph `Auteur.livres` : une seule requête SQL sans modifier le JPQL |
| Test 4 | Cache de second niveau et cache de requêtes : la 2e lecture évite la base |

Les tests 1 à 3 désactivent le cache (`CacheRetrieveMode.BYPASS` et `CacheStoreMode.BYPASS`) afin de mesurer uniquement l'effet de la requête.
Le test 4 vide le cache puis lit deux fois les mêmes données. Les statistiques Hibernate (`hibernate.generate_statistics`) affichent le nombre de requêtes SQL, les hits et les misses du cache.

## Configuration du cache

- `persistence.xml` : active le cache de second niveau, le cache de requêtes et la fabrique `JCacheRegionFactory` avec le fournisseur Ehcache.
- `ehcache.xml` : définit les régions `Auteur`, `Livre`, la collection `Auteur.livres`, ainsi que les régions techniques `default-update-timestamps-region` et `default-query-results-region`.
- Les entités et la collection `livres` sont annotées avec `@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)`.

## Exécution

Dans l'IDE : ouvrir le dossier comme projet Maven puis lancer `com.example.App`.

En ligne de commande :

```
mvn clean compile exec:java
```

## Points d'analyse

- Test sans cache : 11 requêtes SQL (1 + 10), c'est le problème N+1.
- Test `JOIN FETCH` et Entity Graph : 1 requête SQL.
- Test avec cache : la lecture 2 doit exécuter 0 requête SQL, avec des hits de cache de requêtes et de cache de second niveau.
- Le cache de second niveau ne met pas les collections en cache par défaut : c'est pourquoi `@Cache` est placé aussi sur la collection `livres`.
- `DISTINCT` évite les doublons d'auteurs avec `JOIN FETCH`.
