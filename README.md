# Vittae — Front-end (JSF)

Interface web do **Vittae**, sistema de agendamento de consultas médicas. Aplicação server-side em JSF que consome a API REST do back-end para oferecer telas próprias para cada perfil de usuário: administrador, médico e paciente.

Este é o front-end do projeto. A API que ele consome está em: [vittae-back](https://github.com/GustavoGomes-doc/vittae-back).

## Funcionalidades

- **Login** e sessão autenticada via token JWT obtido da API.
- **Telas por perfil**, cada uma com seu próprio fluxo:
  - **Paciente**: agendar consulta (com seleção de especialidade, médico e horário livre), visualizar e cancelar suas consultas.
  - **Médico**: cadastrar disponibilidade de horários, ver sua agenda e seus pacientes, marcar consultas como realizadas.
  - **Admin**: cadastrar médicos, visualizar todos os pacientes, médicos e consultas do sistema.
- **Cadastro de usuários** e gerenciamento de perfil (dados pessoais, troca de senha).
- **Controle de acesso por página**: um filtro de servlet bloqueia o acesso a telas que não correspondem ao perfil logado.

## Tecnologias

- **Java 17**
- **JSF (Jakarta/JavaServer Faces, Mojarra)** para as views (`.xhtml`)
- **CDI (Weld)** para injeção de dependência dos managed beans
- **Servlet API** + **JSTL**
- **Jackson** para consumir a API REST do back-end (serialização/desserialização de DTOs)
- **Lombok**, **Log4j2**
- **Maven** (empacotado como `.war`)
- CSS e JavaScript próprios por tela (sem framework front-end)

## Arquitetura

A aplicação é totalmente **desacoplada do back-end**: não acessa banco de dados diretamente, apenas consome a API Spring via HTTP.

```
View (.xhtml)  →  Managed Bean (view/)  →  Service (service/)  →  API REST (vittae-back)
```

- `view/*Bean.java`: managed beans JSF, um por tela (ex.: `AgendarConsultaBean`, `CadastrarMedicoBean`, `AdminConsultasBean`), responsáveis pela lógica de cada página.
- `service/*Service.java`: camada que monta as chamadas HTTP para a API e converte o JSON de resposta em DTOs (`ConsultaDTO`, `MedicoEnvioDTO`, etc.).
- `util/filter/LoginFilter`: protege as rotas internas, redirecionando para o login quando não há sessão/token válido.
- `WEB-INF/template/LayoutPadrao.xhtml`: template reaproveitado entre as páginas, mantendo cabeçalho/menu consistentes por perfil.

## Estrutura de telas

```
views/
├── login/          → autenticação
├── cadastrar/       → cadastro de novo usuário/paciente
├── pacientes/       → agendar consulta, ver consultas
├── medicos/         → agenda, disponibilidade, pacientes do médico
├── admin/           → cadastro de médicos, visão geral de médicos/pacientes/consultas
└── comum/           → perfil do usuário logado
```

## Como executar

```bash
mvn clean package
# faça o deploy do .war gerado (target/vittae.war) em um servidor de aplicação
# compatível (ex.: Tomcat com suporte a JSF/CDI, ou WildFly)
```

Configure em `src/main/resources/config.properties` a URL onde a API (`vittae-back`) está rodando.

## Projeto relacionado

- Back-end: [vittae-back](https://github.com/GustavoGomes-doc/vittae-back) — API REST que fornece os dados e regras de negócio.
