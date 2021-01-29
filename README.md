# Multi-Agent Maze Solver

[Project Presentation](https://github.com/Tiagocv64/aiad-maze-solver/blob/master/presentation.pdf)

O sistema pretende simular um conjunto de agentes que procuram encontrar a saída do labirinto o mais rapidamente possível. No entanto, ao longo do labirinto existem portas que apenas se encontram abertas enquanto um agente está por cima do interruptor correspondente.

Para que os agentes possam chegar à meta, devem comunicar entre si fornecendo informação útil sobre o labirinto e negociando a possibilidade de um agente “abrir” a porta a outro agente, colocando-se em cima do interruptor da respetiva porta.

Neste sistema existem 3 tipos de agentes: egoístas, razoáveis e solidários. Cada um tem um tipo de comportamento diferente relativamente à forma como interage com os outros agentes.

## Instructions
#### Build
```
gradle build
```

#### Run
```
gradle run -q --console=plain
```
