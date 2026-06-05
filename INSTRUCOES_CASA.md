# Como Continuar o Projeto em Casa (CofiCall)

Este guia prático explica o que precisa de fazer para configurar o seu ambiente doméstico e continuar a trabalhar no desenvolvimento da aplicação **CofiCall**.

---

## 🛠️ 1. Requisitos para o PC de Casa

Antes de começar, certifique-se de que tem as seguintes ferramentas instaladas no computador de casa:

1. **Java Development Kit (JDK) 17**: 
   * O projeto está configurado para correr com o Java 17.
   * Pode descarregá-lo de [Adoptium (Eclipse Temurin)](https://adoptium.net/) ou instalar diretamente pelo Android Studio.
2. **Android Studio**:
   * Recomenda-se a versão mais recente estável (como a Ladybug).
3. **Git**:
   * Necessário para clonar e atualizar o código. Descarregue em [git-scm.com](https://git-scm.com/).

---

## 📥 2. Como Obter o Código

No computador de casa, abra o terminal (ou Git Bash) na pasta onde deseja guardar o projeto e execute:

```bash
git clone https://github.com/Proenca88/CofiCall.git
```

---

## 🚀 3. Abrir e Correr o Projeto

1. Abra o **Android Studio**.
2. Escolha a opção **Open** (Abrir) e selecione a pasta **`CofiCallApp`** (localizada dentro do diretório que acabou de clonar).
3. Aguarde que o Android Studio descarregue o Gradle e sincronize o projeto (este processo pode demorar alguns minutos na primeira inicialização).
4. Crie/inicie um Emulador Android (AVD) através do **Device Manager** do Android Studio.
5. Clique no botão de Play (**Run**) para compilar e instalar a aplicação no emulador.

> [!NOTE]
> **Modo de Demonstração (Sem Firebase)**:
> Graças à lógica de fallback automático que implementámos, a aplicação deteta a ausência das chaves do Firebase locais e ativa automaticamente o **Modo Mock**. Isso significa que pode testar todos os ecrãs, login simulado, registo, lista de colaboradores e favoritos em casa sem precisar de configurar nenhuma credencial ou servidor!

---

## 🔄 4. Como Sincronizar o Trabalho

Para garantir que o código se mantém atualizado entre o computador de casa e o do trabalho, siga este fluxo:

### A) Quando terminar de trabalhar em casa:
No terminal da pasta do projeto no seu PC de casa, execute:
```bash
# 1. Adicionar todas as alterações
git add .

# 2. Registar o commit com uma mensagem explicativa
git commit -m "feat: melhorias feitas em casa"

# 3. Enviar para o GitHub
git push origin main
```

### B) Quando voltar ao PC do trabalho (no dia seguinte):
Antes de começar a escrever código no computador do trabalho, abra o terminal no projeto e puxe as alterações feitas em casa:
```bash
git pull origin main
```

### C) Quando terminar no trabalho e for para casa:
No PC do trabalho, faça o commit e push antes de sair:
```bash
git add .
git commit -m "feat: progresso feito no escritório"
git push origin main
```
E em casa, basta correr um `git pull origin main` para começar com o código atualizado.
