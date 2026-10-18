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

## Points d'analyse

- Test sans cache : 11 requêtes SQL (1 + 10), c'est le problème N+1.
- Test `JOIN FETCH` et Entity Graph : 1 requête SQL.
- Test avec cache : la lecture 2 doit exécuter 0 requête SQL, avec des hits de cache de requêtes et de cache de second niveau.
- Le cache de second niveau ne met pas les collections en cache par défaut : c'est pourquoi `@Cache` est placé aussi sur la collection `livres`.
- `DISTINCT` évite les doublons d'auteurs avec `JOIN FETCH`.

Conclusions :

- Le problème N+1 est confirmé : 1 requête pour les auteurs puis 10 requêtes pour les livres, soit 11 requêtes.
- `JOIN FETCH` et l'Entity Graph ramènent le chargement à une seule requête SQL.
- Avec le cache de second niveau et le cache de requêtes, la deuxième lecture n'exécute aucune requête SQL et devient plus rapide.

## Captures d'écran

### Initialisation et test sans cache (problème N+1)

<img width="419" height="120" alt="cash1" src="https://github.com/user-attachments/assets/e724209f-532d-481b-bda3-4f3d22999307" />

### Test avec JOIN FETCH

<img width="482" height="103" alt="cash2" src="https://github.com/user-attachments/assets/58863091-7650-4a7e-8261-1dd32fa2f0e8" />

### Test avec Entity Graph

<img width="376" height="93" alt="cash3" src="https://github.com/user-attachments/assets/c750168b-f4e9-4c10-9605-a2522e553fee" />

### Test avec cache de second niveau
<img width="531" height="90" alt="cash4" src="https://github.com/user-attachments/assets/7cf3f998-c770-47ab-94aa-e198a7fd5a05" />

<img width="540" height="89" alt="cash5" src="https://github.com/user-attachments/assets/6ef9e203-c7f9-49af-a57e-684a69efefa3" />

