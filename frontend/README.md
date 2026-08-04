# Frontend MatchDev

Interface responsiva em React 19, TypeScript, Vinext e Vite para analisar currículos, importar vagas, visualizar o ranking de compatibilidade e acompanhar candidaturas.

## Executar no Windows

Pré-requisito: Node.js 22.13 ou superior. Depois de instalar o Node, feche e abra o PowerShell para que o comando `npm` entre no PATH.

```powershell
cd E:\matchdev\frontend
npm install
npm run dev
```

Abra o endereço mostrado pelo Vite, normalmente <http://localhost:5173>. A API Java continua em <http://localhost:8080>; são endereços diferentes.

O script `dev` é compatível com PowerShell, CMD, Linux e macOS. A configuração local ativa `nodejs_compat` automaticamente para o runtime Cloudflare usado pelo Vinext.

## Modos de uso

- **Demonstração:** clique em **Explorar demonstração** para testar todas as telas sem ligar o backend.
- **Dados reais:** mantenha o Java em `localhost:8080` e o FastAPI em `localhost:8000`, crie uma conta e complete o perfil.

## Validação

```powershell
npm run lint
npm test
```

O teste executa o build, valida o artefato do Worker e verifica os metadados renderizados.
