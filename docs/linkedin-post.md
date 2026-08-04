# Post de lançamento — LinkedIn

Hoje quero apresentar o MatchDev, um projeto que desenvolvi para tornar a busca por vagas de tecnologia mais estratégica.

A ideia surgiu de um problema real: encontramos muitas oportunidades, mas gastamos tempo demais tentando entender quais realmente combinam com nosso perfil.

No MatchDev, a pessoa pode importar um currículo em PDF, mapear suas tecnologias, colar a descrição de uma vaga e receber uma análise explicável de compatibilidade de 0 a 100. O sistema também mostra habilidades atendidas e ausentes, organiza um ranking de oportunidades e permite acompanhar cada candidatura em um funil.

Na parte técnica, construí uma arquitetura com Java 17, Spring Boot, Spring Security, JWT, PostgreSQL, Flyway, Python, FastAPI, React, TypeScript, Docker e testes automatizados.

Uma decisão importante do projeto foi não fazer scraping de plataformas. A descrição da vaga é fornecida pelo próprio usuário e o link original fica apenas como referência.

Mais do que calcular um número, a proposta é ajudar a decidir onde vale a pena se candidatar agora e quais competências desenvolver para as próximas oportunidades.

Demonstração pública em modo demo:
[https://matchdev-app.joaoantoniosouza123.chatgpt.site](https://matchdev-app.joaoantoniosouza123.chatgpt.site)

Código-fonte:
[https://github.com/joazao231/matchdev](https://github.com/joazao231/matchdev)

Feedbacks sobre a solução e a arquitetura serão muito bem-vindos.

\#Java #SpringBoot #Python #FastAPI #React #TypeScript #Docker #DesenvolvimentoBackend #OpenToWork
