
# Comparação Técnica: JpaRepository vs. EntityManager

## Introdução
Apresentação das diferenças técnicas entre `JpaRepository` e `EntityManager` no Spring Data JPA, destacando cenários de uso, vantagens e desvantagens.

---

## Visão Geral

### JpaRepository
- Interface de alto nível do Spring Data JPA.
- Simplifica operações CRUD e consultas com abstração.
- Integração automática com transações e cache.
- **Exemplo**:
  ```java
  public interface UserRepository extends JpaRepository<User, Long> {
      List<User> findAll();
  }
  ```

### EntityManager
- Interface de baixo nível do JPA para controle granular.
- Necessita gerenciamento manual de transações.
- Flexibilidade para operações avançadas.
- **Exemplo**:
  ```java
  public void createUser(User user) {
      EntityManager entityManager = entityManagerFactory.createEntityManager();
      entityManager.getTransaction().begin();
      entityManager.persist(user);
      entityManager.getTransaction().commit();
  }
  ```
  
---
## Cache e Otimização

### JpaRepository:

- Integração automática com o cache do JPA.
- O cache de primeiro nível é gerenciado automaticamente pelo EntityManager e armazena entidades dentro de uma sessão de persistência.
- Melhora a performance ao evitar consultas repetidas ao banco de dados para as mesmas entidades.

### Exemplo:
```java
Product p1 = entityManager.find(Product.class, 1L);  // Primeira consulta ao banco
Product p2 = entityManager.find(Product.class, 1L);  // Retorna do cache do EntityManager
```

### EntityManager:

- Cache de primeiro nível (L1) é ativado automaticamente dentro de um EntityManager ativo.
- Cache de segundo nível (L2), opcional, pode ser configurado e compartilhado entre diferentes EntityManagers para otimizar consultas mais complexas.
- Possui controle manual sobre quando limpar o cache `flush() e clear()`, útil para operações de batch.
### Exemplo de controle manual:
```java
entityManager.flush();  // Sincroniza as entidades com o banco de dados
entityManager.clear();  // Limpa o contexto de persistência, liberando memória
```

---

## Comparação Técnica

| Característica          | JpaRepository              | EntityManager          |
|-------------------------|----------------------------|------------------------|
| **Abstração**           | Alta                       | Baixa                 |
| **Métodos CRUD**        | Prontos                    | Manual                |
| **Consultas Customizadas** | Por anotações (@Query)    | JPQL/SQL manual ou Criteria API |
| **Gestão de Transações** | Automática                 | Manual (begin, commit, rollback) |
| **Controle do Ciclo de Vida** | Simplificado           | Granular              |
| **Performance**         | Cache otimizado            | Customização possível |

---


## Comparação do Criteria API - Consulta Complexa

- **JpaRepository com @Query**:

  Usando a anotação `@Query`, você pode definir consultas **JPQL** diretamente no repositório. Para consultas mais complexas, você ainda pode usar `JOIN`, `WHERE`, e outras cláusulas, mas a flexibilidade é limitada em comparação com o **Criteria API**.

  **Exemplo de consulta com @Query**:
  ```java
  @Query("SELECT u FROM User u JOIN u.orders o WHERE o.status = :status AND u.registrationDate > :date")
  List<User> findUsersWithOrders(@Param("status") String status, @Param("date") LocalDate date);
  ```

  **Explicação**:
  - **Join**: A consulta faz um **JOIN** entre a entidade `User` e a entidade `Order`.
  - **Filtros**: Filtra os resultados com base no `status` do pedido e a data de registro do usuário.
  - **Limitações**: A consulta é fixa e não permite modificações dinâmicas com base nas condições em tempo de execução sem criar múltiplas consultas ou lógica adicional.


- **EntityManager com Criteria API**:

  O **Criteria API** oferece mais flexibilidade, permitindo a construção dinâmica da consulta. Isso é útil quando você precisa ajustar a consulta dependendo das condições em tempo de execução.

  
  Imaginando que você tenha um cenário onde precisa buscar usuários com base em diferentes filtros (como status, data de registro e nome). Esses filtros podem ser opcionais e podem ser passados para o método, permitindo que a consulta seja construída dinamicamente com base no que é fornecido.

  **Exemplo de consulta com Criteria API**:
  ```java
  public List<User> findUsersByCriteria(String status, LocalDate startDate, String name) {
  CriteriaBuilder cb = entityManager.getCriteriaBuilder();
  CriteriaQuery<User> cq = cb.createQuery(User.class);
  Root<User> userRoot = cq.from(User.class);
  
      // Lista para armazenar as condições dinâmicas
      List<Predicate> predicates = new ArrayList<>();
      
      // Adiciona condição para o status, se fornecido
      if (status != null) {
          Predicate statusPredicate = cb.equal(userRoot.get("status"), status);
          predicates.add(statusPredicate);
      }
      
      // Adiciona condição para a data de registro, se fornecida
      if (startDate != null) {
          Predicate datePredicate = cb.greaterThanOrEqualTo(userRoot.get("registrationDate"), startDate);
          predicates.add(datePredicate);
      }
      
      // Adiciona condição para o nome, se fornecido
      if (name != null && !name.isEmpty()) {
          Predicate namePredicate = cb.like(userRoot.get("name"), "%" + name + "%");
          predicates.add(namePredicate);
      }
  
      // Combina todas as condições (predicates)
      cq.where(cb.and(predicates.toArray(new Predicate[0])));
  
      // Executa a consulta
      return entityManager.createQuery(cq).getResultList();
  }
  ```

  **Explicação**:
  - **Join Dinâmico**: Usando a **Criteria API**, o `JOIN` é feito programaticamente, dando controle sobre a criação de associações.
  - **Filtros Dinâmicos**: A consulta é construída dinamicamente, permitindo que você adicione ou modifique condições de filtro conforme necessário.
  - **Flexibilidade**: A construção programática oferece maior flexibilidade para modificar a consulta com base em parâmetros de tempo de execução.
  - **Uso do Predicate**: O Predicate permite que você defina as condições de maneira flexível, como equal, greaterThanOrEqualTo, like, etc., para cada campo.

### Resumo:

| Característica                  | JpaRepository com @Query | EntityManager com Criteria API  |
|----------------------------------|--------------------------|----------------------------------|
| **Complexidade das Consultas**   | Boa para consultas fixas  | Alta flexibilidade, consultas dinâmicas |
| **Sintaxe**                      | JPQL, simples e legível   | Programática, mais verbosa e complexa |
| **Modificação em tempo de execução** | Limitada (múltiplas consultas ou lógica adicional) | Alta flexibilidade, ajustes dinâmicos possíveis |

---

## Cenários Práticos

### 1. Consulta por ID

**JpaRepository**:
```java
Optional<User> user = userRepository.findById(1L);
```

**EntityManager**:
```java
User user = entityManager.find(User.class, 1L);
```

### 2. Atualização de Entidade

**JpaRepository**:
```java
User user = userRepository.findById(1L).orElseThrow();
user.setName("Jane Doe");
userRepository.save(user);
```

**EntityManager**:
```java
entityManager.getTransaction().begin();
User user = entityManager.find(User.class, 1L);
user.setName("Jane Doe");
entityManager.merge(user);
entityManager.getTransaction().commit();
```

### 3. Consultas Customizadas

**JpaRepository**:
```java
@Query("SELECT u FROM User u WHERE u.name = :name")
List<User> findByName(@Param("name") String name);
```

**EntityManager**:
```java
List<User> users = entityManager.createQuery("SELECT u FROM User u WHERE u.name = :name", User.class)
    .setParameter("name", "John Doe")
    .getResultList();
```

---

## Controle e Flexibilidade

### Transações
- **JpaRepository**: Transações automáticas com `@Transactional`.
- **EntityManager**: Necessita gerenciamento manual:
  ```java
  entityManager.getTransaction().begin();
  // Operações
  entityManager.getTransaction().commit();
  ```

### Cache e Otimização
- **JpaRepository**: Integração com cache do JPA, otimizado para CRUD.
- **EntityManager**: Controle manual permite ajuste para cenários específicos.

---

## Testes de Performance (PoC)

### Objetivo
Comparar desempenho entre `JpaRepository` e `EntityManager` em consultas simples e complexas.

### Ferramentas Utilizadas
- **K6**: K6 para medir tempo de execução e throughput.

### Metodologia
1. Criar 10.000 registros na base de dados.
2. Executar operações de leitura, escrita e consultas complexas.
3. Medir tempo de execução e uso de recursos.

### Cenários Testados
- **CRUD Simples**: Inserção, leitura, atualização e exclusão de entidades.
- **Consultas Complexas**: Consultas com filtros e joins.

### Resultados Esperados
- **JpaRepository**: Melhor desempenho em CRUD básico devido ao cache integrado.
- **EntityManager**: Performance superior em operações complexas com controle direto.

---

## Considerações de Performance

### JpaRepository
- Ótimo para cenários padrão com CRUD básico.
- Abstração reduz o tempo de desenvolvimento.

### EntityManager
- Preferível para cenários avançados e controle detalhado.
- Requer maior esforço, mas oferece flexibilidade.

---

## Conclusão

### Quando Usar JpaRepository:
- Desenvolvimento rápido com foco em CRUD básico.
- Integração nativa com o ecossistema Spring.
- Consultas simples

### Quando Usar EntityManager:
- Controle avançado sobre transações e ciclo de vida.
- Operações específicas que exigem desempenho ajustado.

A extração de desempenho do Entity Manager só se deve ao programar todo o seu ciclo de vida.
A escolha depende das necessidades da aplicação e do nível de controle requerido.

```java
@PersistenceContext
private EntityManager entityManager;

public void batchInsert(List<Product> products) {
int batchSize = 50;  // Definindo um tamanho de lote para otimização
int i = 0;

    for (Product product : products) {
        entityManager.persist(product);  // Inserção da entidade

        if (i % batchSize == 0 && i > 0) {
            // Limpa o contexto de persistência para evitar consumo excessivo de memória
            entityManager.flush();
            entityManager.clear();
        }

        i++;
    }

    // Garantindo que o último lote seja persistido
    entityManager.flush();
    entityManager.clear();
}
```

---

## Referências
- [Documentação oficial do Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Artigo sobre JpaRepository e EntityManager](https://medium.com/@mgm06bm/jparepository-and-entitymanager-in-spring-data-jpa-e12daad579a7)
