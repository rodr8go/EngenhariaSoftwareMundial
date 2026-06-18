# TorneoApp — Gestão de Torneios Desportivos

Aplicação Java com Swing UI para gestão de torneios desportivos.

## Estrutura do Projeto

```
TorneoApp/
├── pom.xml                          ← Maven build (recomendado)
└── src/main/java/torneoapp/
    ├── Main.java                    ← Ponto de entrada
    ├── model/                       ← Classes do domínio
    │   ├── Participante.java        ← Classe abstrata (superclasse)
    │   ├── Jogador.java             ← extends Participante
    │   ├── Treinador.java           ← extends Participante
    │   ├── Equipa.java
    │   ├── Estadio.java
    │   ├── Setor.java
    │   ├── Jogo.java
    │   ├── Torneio.java
    │   ├── Bilhete.java
    │   ├── Voluntario.java
    │   ├── Patrocinador.java
    │   ├── ContratoPatrocinio.java
    │   ├── EventoJogo.java          ← Classe abstrata
    │   ├── EventoColetivo.java      ← extends EventoJogo
    │   └── EventoPessoal.java       ← extends EventoJogo
    ├── service/
    │   └── DataStore.java           ← Singleton com dados em memória + dados de exemplo
    ├── ui/                          ← Painéis Swing
    │   ├── MainFrame.java           ← Janela principal + sidebar
    │   ├── DashboardPanel.java
    │   ├── EstadiosPanel.java
    │   ├── EquipasPanel.java
    │   ├── TorneioPanel.java
    │   ├── ResultadosPanel.java
    │   ├── FinanceiroPanel.java
    │   ├── PatrociniosPanel.java
    │   └── BilheteiraPanel.java
    └── util/
        └── UITheme.java             ← Cores, estilos, componentes reutilizáveis
```

## Como abrir no IntelliJ IDEA

### Opção A — Maven (recomendado)
1. Abrir IntelliJ IDEA
2. `File → Open` → selecionar a pasta `TorneoApp`
3. IntelliJ deteta o `pom.xml` automaticamente → clicar **"Trust Project"**
4. Aguardar indexação
5. Correr `Main.java` (botão ▶ verde ao lado da classe)

### Opção B — Projeto simples (sem Maven)
1. `File → New → Project from Existing Sources`
2. Selecionar a pasta `TorneoApp`
3. Escolher **"Create project from existing sources"**
4. Definir o SDK (Java 17+)
5. Marcar `src/main/java` como **Sources Root** (botão direito → Mark Directory as → Sources Root)
6. Correr `Main.java`

## Requisitos
- Java 17 ou superior
- IntelliJ IDEA (Community ou Ultimate)

## Funcionalidades Implementadas

| Módulo | Funcionalidades |
|--------|----------------|
| **Dashboard** | Estatísticas financeiras, próximos jogos, melhores marcadores |
| **Estádios** | CRUD completo, gestão de setores, proteção contra remoção com jogos calendarizados |
| **Equipas** | CRUD, gestão de jogadores e treinadores por equipa |
| **Torneio** | Criar torneios, calendarizar jogos, registar dados pós-jogo, voluntários |
| **Resultados** | Resultados e leaderboards (marcadores, assistências, cartões) |
| **Financeiro** | Dashboard financeiro, ranking patrocinadores, receita por jogo |
| **Patrocínios** | CRUD patrocinadores, criar contratos de patrocínio |
| **Bilheteira** | Vender bilhetes, definir preçário por setor, aplicar desconto, validação de precedência |

## Casos de Uso Implementados (dos diagramas)

- ✅ R1: Registar/Consultar/Editar/Remover Estádio (com condição "Antes da calendarização")
- ✅ R2: Registar Equipa (com <<include>> Adicionar Treinador e Jogadores)
- ✅ R2: Consultar/Editar/Remover Equipas, Jogadores, Treinadores (com condição "Antes da calendarização")
- ✅ R3: Calendarizar Jogos (com validação de precedência: equipas e estádios registados primeiro)
- ✅ R3: Registar dados Pós-Jogo, Adicionar Voluntários
- ✅ R4: Definir Preçário por Setor (<<precedes>> Vender Bilhete)
- ✅ R4: Vender Bilhete (com <<extend>> Aplicar Desconto)
- ✅ R5: Registar Patrocinador, Criar Contrato
- ✅ Consultar Dashboard Financeiro, Estatísticas e Resultados
