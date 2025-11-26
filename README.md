# 🕵️ HideAndSeek

Um plugin de Esconde-Esconde com mecânica de "Infeção" e "A Fera", desenvolvido para eventos da ADR Studios.

# 📖 Sobre o Projeto

A Fera (Procurador): Um jogador é escolhido e fica preso numa sala de espera por 2 minutos.
A Caçada: Após o tempo de espera, a Fera é libertada no mapa.
Infeção Instantânea: Se um Escondedor for encontrado (morto ou cair em armadilhas), ele transforma-se imediatamente num Procurador para ajudar a caçar os restantes. Não há tela de morte.
Radar: Em momentos críticos da partida, os Escondedores brilham (Glow) para evitar "campers".

# ✨ Funcionalidades Principais

🔁 Troca de Time Instantânea: Sem respawn screen. Morreu? Virou Procurador com kit equipado e pronto para caçar.
⏳ Sala de Espera Dedicada: O Procurador inicial aguarda num local separado enquanto os Escondedores se posicionam.
🛡️ Proteção de Queda: Ninguém recebe dano de queda durante a partida.
📡 Sistema de Radar: Alerta visual e sonoro onde todos os Escondedores ficam com efeito GLOW (brilho) aos 15min, 10min, 5min e 1min restantes.
🎒 Kits Configuráveis: Menu GUI in-game (/hs itens) para definir o inventário inicial de cada equipa.
🚫 Blacklist: Sistema para impedir que certos jogadores (ex: Staff em serviço) sejam sorteados como a Fera inicial.
👻 Chat de Espectadores: Jogadores que não estão a participar ou estão na Blacklist possuem um chat separado.
🎨 Visual Polido: Scoreboard sem flickering, Títulos na tela, ActionBars e Sons imersivos (Dragão, Pliims, Trovões).

# 🎮 Como Jogar (Ciclo Automático)

Aguardando: O jogo espera o número mínimo de jogadores (Configurável).
Sorteio: Um jogador aleatório (fora da Blacklist) é escolhido como Procurador.

# Escondendo (2 Minutos):

O Procurador vai para a Sala de Espera.
Os Escondedores vão para o Spawn do Mapa.
A Fera Saiu (20 Minutos): O Procurador é libertado. O PvP é ativado (apenas entre equipas opostas).

# Fim de Jogo:

Procuradores vencem: Se encontrarem todos.
Escondedores vencem: Se o tempo acabar e houver sobreviventes.

# 🛠️ Comandos e Permissões

A permissão base para administração é hs.admin. Jogadores comuns têm acesso apenas ao /hs info e /hs help.

/hs start Força o início imediato do ciclo de jogo.
/hs itens <kit> Abre o menu GUI para definir itens (pegador ou escondedor).
/hs blacklist <add/remove> <nick> Gere quem não pode ser a Fera inicial.
/hs pegadorspawn set Define onde a Fera nasce (Início do Mapa).
/hs setpegadorespera Define a sala fechada onde a Fera espera 2 min.
/hs escondedorspawn set Define onde os Escondedores nascem.
/hs endspawn set Define o Lobby final pós-jogo.
/hs setminplayers <qtd> Define o mínimo de jogadores para iniciar.
/hs settempopartida <min> Define a duração da caçada.

# ⚙️ Instalação e Configuração

Coloque o arquivo .jar na pasta plugins do seu servidor.
Reinicie o servidor para gerar a pasta de configuração.

# Defina os Spawns Obrigatórios (Ordem sugerida):
Vá para a sala fechada/jaula e digite: /hs setpegadorespera
Vá para o início do mapa (onde a fera sai) e digite: /hs pegadorspawn set
Vá para onde os escondedores devem nascer e digite: /hs escondedorspawn set
Vá para o Lobby principal do servidor e digite: /hs endspawn set

# Configure os Kits:

Encha o seu inventário com os itens que deseja para o Pegador.
Digite /hs itens pegador, coloque os itens no baú e feche.
Repita o processo para /hs itens escondedor.

# 📄 Configuração Padrão (config.yml)

tempos:
  min_players: 2
  tempo_esconder: 120 # Segundos (2 min)
  tempo_jogo: 1200    # Segundos (20 min)

blacklist: []

# Kits e Locais são salvos automaticamente aqui pelo plugin.
kits:
  pegador: []
  escondedor: []


# 🤝 Contribuição

Desenvolvido por Lucas Corrêa
