package com.example.tictactoe.settings

import Agent
import Player

data class AgentData (
    override var player: Player = Agent.createAgent(
        cellType = CellType.CIRCLE,
        diff = AgentDifficulties.MEDIUM
    ) as Player,
    override var cellTypeImg: CellTypeImg = CellTypeImg.CIRCLE_BLACK,
    override var winCellTypeImg: CellTypeImg = CellTypeImg.CIRCLE_RED,
    override var listOfWinMoves: MutableList<Pair<Int, Int>> = mutableListOf()

) : PlayerData

