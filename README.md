# SOSI – Sistema Operacional de Serviços de Indaiatuba

Aplicativo Android que conecta **cidadãos** e **prefeitura**: o cidadão registra problemas da cidade — buracos, postes apagados, semáforos quebrados, lixo acumulado — e acompanha cada etapa até a solução. A prefeitura recebe as ocorrências, atualiza o status e responde ao cidadão em tempo real.

> Projeto Integrador do curso de **Desenvolvimento de Software Multiplataforma (DSM)** – Fatec, disciplina de Desenvolvimento Mobile I.

---

## ✨ Funcionalidades

### 👤 Cidadão
- Cadastro com e-mail e senha
- Login com e-mail e senha **ou com a conta Google**
- Recuperação de senha por e-mail ("Esqueci minha senha")
- Abertura de ocorrências com motivo (elogio, reclamação ou sugestão), categoria, endereço, data e descrição
- Acompanhamento das próprias ocorrências, com status colorido e resposta da prefeitura **atualizados em tempo real**
- Perfil com edição de nome, celular e CPF

### 🏛️ Prefeitura
- Login exclusivo da prefeitura
- Painel com **todas** as ocorrências recebidas
- Filtros por status: Todos, Em análise, Em andamento e Concluído
- Atualização do status e resposta ao cidadão

### 📋 Geral
- Menu lateral (☰) que se adapta ao tipo de usuário
- Telas "Sobre" e "Contato"

---

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| **Java** | Linguagem do app |
| **Android Studio** | Ambiente de desenvolvimento |
| **XML** | Layouts das telas |
| **Firebase Authentication** | Login com e-mail/senha e Google |
| **Firebase Realtime Database** | Banco de dados em tempo real |

---

---

## 🔒 Segurança

As regras do Firebase Realtime Database garantem que:
- cada cidadão **só lê e altera os próprios dados**;
- cada cidadão **só vê as próprias ocorrências**;
- apenas a **prefeitura** vê todas as ocorrências e pode respondê-las.

---
